package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.dto.*
import com.unicolour.joyspace.exception.ProcessException
import com.unicolour.joyspace.model.*
import com.unicolour.joyspace.service.*
import com.unicolour.joyspace.util.format
import graphql.schema.DataFetcher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.transaction.Transactional
import kotlin.collections.HashMap
import java.security.PublicKey
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.security.Signature


@Service
open class PrintStationServiceImpl : PrintStationService {
    companion object {
        val logger = LoggerFactory.getLogger(PrintStationServiceImpl::class.java)
    }

    @Value("\${com.unicolour.joyspace.assetsDir}")
    lateinit var assetsDir: String

    @Value("\${com.unicolour.joyspace.baseUrl}")
    lateinit var baseUrl: String

    @Autowired
    lateinit var managerService : ManagerService

    @Autowired
    lateinit var managerDao : ManagerDao

    @Autowired
    lateinit var companyDao: CompanyDao

    @Autowired
    lateinit var positionDao : PositionDao

    @Autowired
    lateinit var positionService: PositionService

    @Autowired
    lateinit var adSetDao : AdSetDao

    @Autowired
    lateinit var printStationTaskDao: PrintStationTaskDao

    @Autowired
    lateinit var printStationDao: PrintStationDao

    @Autowired
    lateinit var printStationActivationCodeDao: PrintStationActivationCodeDao

    @Autowired
    lateinit var priceListService: PriceListService

    @Autowired
    lateinit var productService: ProductService

    @Autowired
    lateinit var productDao: ProductDao

    @Autowired
    lateinit var printStationProductDao: PrintStationProductDao

    @Autowired
    lateinit var printerTypeDao: PrinterTypeDao

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var transactionTemplate: TransactionTemplate

    @Autowired
    lateinit var printStationLoginSessionDao: PrintStationLoginSessionDao

    @Autowired
    lateinit var adSetService: AdSetService

    private val dateTimeFormat: ThreadLocal<SimpleDateFormat> = ThreadLocal.withInitial { SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS") }

    @Transactional
    override fun getPrintStationLoginSession(sessionId: String): PrintStationLoginSession? {
        val session = printStationLoginSessionDao.findOne(sessionId)
        if (session != null) {
            if (System.currentTimeMillis() > session.expireTime.timeInMillis) {
                printStationLoginSessionDao.delete(session)
                return null
            }
            else {
                session.expireTime = Calendar.getInstance()
                session.expireTime.add(Calendar.SECOND, 3600)
                printStationLoginSessionDao.save(session)

                return session
            }
        }
        else {
            return null
        }
    }

    override fun getDataFetcher(fieldName: String): DataFetcher<Any> {
        return DataFetcher<Any> { env ->
            val printStation = env.getSource<PrintStation>()
            when (fieldName) {
                "name" -> if (printStation.name.isBlank()) "自助机${printStation.id}" else printStation.name
                "address" -> printStation.position.address
                "latitude" -> printStation.position.latitude
                "longitude" -> printStation.position.longitude
                "transportation" -> printStation.position.transportation
                "images" -> {
                    printStation.position.imageFiles.map { "${baseUrl}/assets/position/images/${it.id}.${it.fileType}" }
                }
                "products" -> {
                    productService.getProductsOfPrintStation(printStation.id)
                }
                "distance" -> {
                    val context = env.getContext<HashMap<String, Any>>()
                    val refLatitude = context["refLatitude"] as Double?
                    val refLongitude = context["refLongitude"] as Double?
                    if (refLatitude != null && refLongitude != null) {
                        distance(refLongitude, refLatitude, printStation.position.longitude, printStation.position.latitude)
                    }
                    else {
                        null
                    }
                }
                else -> null
            }
        }
    }

    //登录
    override val loginDataFetcher: DataFetcher<PrintStationLoginResult>
        get() {
            return DataFetcher<PrintStationLoginResult> { env ->
                val printStationId = env.getArgument<Int>("printStationId")
                val password = env.getArgument<String>("password")
                val version = env.getArgument<Int?>("version")
                val uuid = env.getArgument<String?>("uuid") ?: ""
                transactionTemplate.execute { login(printStationId, password, version, uuid, "","") }
            }
        }

    private fun loadPublicKey(printStationId: Int): PublicKey? {
        try {
            val pubKeyFile = File(assetsDir, "printStation/key/$printStationId.key")
            if (!pubKeyFile.exists()) {
                return null
            }

            val keyBytes = pubKeyFile.readBytes()
            val ks = X509EncodedKeySpec(keyBytes)
            val kf = KeyFactory.getInstance("RSA")

            return kf.generatePublic(ks)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun savePublicKey(printStationId: Int, pubKeyStr: String): Boolean {
        return try {
            val pubKeyFile = File(assetsDir, "printStation/key/$printStationId.key")
            pubKeyFile.parentFile.mkdirs()
            pubKeyFile.writeBytes(Base64.getDecoder().decode(pubKeyStr))

            loadPublicKey(printStationId) != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun loginWithKey(printStation: PrintStation, signStr: String): Int {
        if (printStation.uuid.isNullOrBlank()) {
            return 4     //没有公钥
        }

        val pubKey = loadPublicKey(printStation.id)
        if (pubKey == null) {
            return 4     //没有公钥
        }

        val signBytes = Base64.getDecoder().decode(signStr)

        val currentSequence = printStation.loginSequence ?: 0
        for (i in 1 .. 10) {
            val sign = Signature.getInstance("SHA256withRSA")
            sign.initVerify(pubKey)

            val sequence = currentSequence + i
            val strToSign = "printStationId: ${printStation.id}, uuid: ${printStation.uuid}, sequence: $sequence"
            sign.update(strToSign.toByteArray())

            if (sign.verify(signBytes)) {
                printStation.loginSequence = sequence
                printStationDao.save(printStation)

                return 0
            }
        }

        return 5   //验证失败
    }

    private fun loginWithPassword(printStation: PrintStation, password: String, uuid: String?): Int {
        if (!passwordEncoder.matches(password, printStation.password)) {
            return 2   //密码错误
        }

        val session = printStationLoginSessionDao.findByPrintStationId(printStation.id)
        if (session != null) {
            val time = Calendar.getInstance().apply { add(Calendar.SECOND, 3600 - 30) }

            if (session.expireTime.timeInMillis > time.timeInMillis) {    //自助机30秒之内访问过后台
                if (!printStation.uuid.isNullOrBlank() && printStation.uuid != uuid) {  //其他自助机已经登录
                    return 3    //已经在其他机器上登录过
                }
            }
        }

        return 0
    }

    @Transactional
    override fun login(printStationId: Int, password: String, version: Int?, uuid: String,
                       pubKeyStr: String, signStr: String): PrintStationLoginResult {
        val printStation = printStationDao.findOne(printStationId)

        if (printStation == null) {
            return PrintStationLoginResult(result = 1)  //没有找到指定的自助机
        }

        val loginWithPassword: Boolean
        val loginResult: Int

        if (password.isEmpty()) {
            loginWithPassword = false
            loginResult = loginWithKey(printStation, signStr)
        }
        else {
            loginWithPassword = true
            loginResult = loginWithPassword(printStation, password, uuid)
        }

        if (loginResult == 0) {
            if (uuid.isNotEmpty()) {
                printStation.uuid = uuid
            }

            if (loginWithPassword && pubKeyStr.isNotEmpty()) {
                if (savePublicKey(printStation.id, pubKeyStr)) {
                    printStation.password = passwordEncoder.encode(UUID.randomUUID().toString())
                    printStationDao.save(printStation)
                }
                else {
                    return PrintStationLoginResult(result = 7)  //保存公钥失败
                }
            }

            var session = printStationLoginSessionDao.findByPrintStationId(printStationId)
            if (session != null) {
                printStationLoginSessionDao.delete(session)
            }

            session = PrintStationLoginSession()
            session.id = UUID.randomUUID().toString().replace("-", "")
            session.printStationId = printStation.id
            session.expireTime = Calendar.getInstance().apply { add(Calendar.SECOND, 3600) }
            printStationLoginSessionDao.save(session)

            if (printStation.lastLoginVersion != version) {
                printStation.lastLoginVersion = version
                printStationDao.save(printStation)
            }

            val printerType = printerTypeDao.findOne(printStation.printerType)

            return if (printerType == null) {
                PrintStationLoginResult(result = 6)  //未知的打印机类型
            } else {
                PrintStationLoginResult(sessionId = session.id, printerType = printStation.printerType, resolution = printerType.resolution)
            }
        }
        else {
            return PrintStationLoginResult(resolution = loginResult)
        }
    }

    override fun getPrintStationUpdateAndAdSet(
            sessionId: String, currentVersion: Int,
            currentAdSetId: Int, currentAdSetTimeStr: String): UpdateAndAdSetDTO {
        val currentAdSetTime =
                try {
                    dateTimeFormat.get().parse(currentAdSetTimeStr)
                }
                catch (e: Exception) {
                    null
                }

        var version = -1
        var adSet: AdSet? = null

        val loginSession = getPrintStationLoginSession(sessionId)
        if (loginSession != null) {
            val printStation = printStationDao.findOne(loginSession.printStationId)
            if (printStation?.updateToVersion != null) {
                version = printStation.updateToVersion!!
            }

            val ad = printStation?.adSet
            if (currentAdSetTime != null && ad != null &&
                    (ad.id != currentAdSetId || ad.updateTime.timeInMillis > currentAdSetTime.time)) {
                adSet = ad
            }
        }

        if (version == -1) {
            version =
                    try {
                        val versionFile = File(assetsDir, "home/current.txt")
                        versionFile.reader().use { it.readText().trim(' ', '\r', '\n', '\t').toInt() }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        0
                    }
        }

        return UpdateAndAdSetDTO(
                version = version,
                adSet = adSetService.adSetToDTO(adSet)
        )
    }

    override fun getPriceMap(printStation: PrintStation): Map<Int, Int> {
        val priceListItems: List<PriceListItem> = priceListService.getPriceListItems(printStation.position.priceListId)
        val defPriceListItems: List<PriceListItem> = priceListService.getPriceListItems(printStation.company.defaultPriceListId)

        val priceMap: MutableMap<Int, Int> = HashMap()
        for (priceListItem in defPriceListItems) {
            priceMap[priceListItem.productId] = priceListItem.price
        }

        for (priceListItem in priceListItems) {
            priceMap[priceListItem.productId] = priceListItem.price
        }

        return priceMap
    }

    override fun getPrintStationUrl(printStationId: Int): String {
        return "$baseUrl/printStation/$printStationId"
    }

    @Transactional
    override fun updatePrintStation(id: Int, printStationName: String, positionId: Int, transferProportion:Int, printerType: String, adSetId: Int, selectedProductIds: Set<Int>): Boolean {
        val printStation = printStationDao.findOne(id)

        if (printStation != null) {
            printStation.name = printStationName
            printStation.wxQrCode = "$baseUrl/printStation/${printStation.id}"
            printStation.position = positionDao.findOne(positionId)
            printStation.addressNation = printStation.position.addressNation
            printStation.addressProvince = printStation.position.addressProvince
            printStation.addressCity = printStation.position.addressCity
            printStation.addressDistrict = printStation.position.addressDistrict
            printStation.addressStreet = printStation.position.addressStreet

            if (managerService.loginManagerHasRole("ROLE_SUPERADMIN")) {
                printStation.transferProportion = transferProportion
                printStation.printerType = printerType
                if (adSetId > 0) {
                    printStation.adSet = adSetDao.findOne(adSetId)
                }
                else if (adSetId == 0) {
                    printStation.adSet = null
                }
            }

            printStationDao.save(printStation)

            printStationProductDao.deleteByPrintStationId(id)

            for (productId in selectedProductIds) {
                val printStationProduct = PrintStationProduct()
                printStationProduct.product = productDao.findOne(productId)
                printStationProduct.printStation = printStation

                printStationProductDao.save(printStationProduct)
            }
            return true
        }
        else {
            return false
        }
    }

    @Transactional
    override fun updatePrintStationPassword(id: Int, printStationPassword: String): Boolean {
        val printStation = printStationDao.findOne(id)

        if (printStation != null) {
            val loginManager = managerService.loginManager
            if (loginManager == null ||
                    printStation.companyId != loginManager.companyId && !managerService.loginManagerHasRole("ROLE_SUPERADMIN")) {
                return false
            }

            printStation.password = passwordEncoder.encode(printStationPassword)
            printStationDao.save(printStation)
            return true
        }
        else {
            return false
        }
    }

    @Transactional
    override fun activatePrintStation(code: String, name: String, password: String, positionId: Int, selectedProductIds: Set<Int>) {
        val loginManager = managerService.loginManager
        if (loginManager == null) {
            throw ProcessException(1, "")
        }

        val activationCode = printStationActivationCodeDao.findByCode(code)
        if (activationCode == null) {
            throw ProcessException(ResultCode.INVALID_ACTIVATION_CODE)
        }
        else if (activationCode.used) {
            throw ProcessException(ResultCode.ACTIVATION_CODE_USED)
        }

        val position = positionDao.findOne(positionId)

        val printStation = PrintStation()
        printStation.id = activationCode.printStationId
        printStation.name = name
        printStation.company = position.company
        printStation.position = position
        printStation.transferProportion =  activationCode.transferProportion
        printStation.printerType = activationCode.printerType
        printStation.adSet = if (activationCode.adSetId == null) null else adSetDao.findOne(activationCode.adSetId)
        printStation.addressNation = printStation.position.addressNation
        printStation.addressProvince = printStation.position.addressProvince
        printStation.addressCity = printStation.position.addressCity
        printStation.addressDistrict = printStation.position.addressDistrict
        printStation.addressStreet = printStation.position.addressStreet
        printStation.password = passwordEncoder.encode(password)
        printStation.status = PrintStationStatus.NORMAL.value

        printStationDao.save(printStation)

        printStation.wxQrCode = "$baseUrl/printStation/${printStation.id}"
        printStationDao.save(printStation)

        for (productId in selectedProductIds) {
            val printStationProduct = PrintStationProduct()
            printStationProduct.product = productDao.findOne(productId)
            printStationProduct.printStation = printStation

            printStationProductDao.save(printStationProduct)
        }

        activationCode.useTime = Calendar.getInstance()
        activationCode.used = true
        printStationActivationCodeDao.save(activationCode)
    }

    @Transactional
    override fun addUploadLogFileTask(printStationId: Int, filterStr: String): Boolean {
        val task = PrintStationTask()

        task.param = filterStr
        task.printStationId = printStationId
        task.createTime = Calendar.getInstance()
        task.type = PrintStationTaskType.UPLOAD_LOG.value

        printStationTaskDao.save(task)

        return true
    }

    override val printStationDataFetcher: DataFetcher<PrintStation>
        get() {
            return DataFetcher { env ->
                val printStationId = env.getArgument<Int>("printStationId")
                printStationDao.findOne(printStationId)
            }
        }

    override val byCityDataFetcher: DataFetcher<PrintStationFindResult>
        get() {
            return DataFetcher { env ->
                val longitude = env.getArgument<Double>("longitude")
                val latitude = env.getArgument<Double>("latitude")

                val addressComponent = positionService.getAddressComponent(longitude, latitude)
                if (addressComponent == null) {
                    PrintStationFindResult(
                            result = ResultCode.CITY_NOT_FOUND.value,
                            description = ResultCode.CITY_NOT_FOUND.desc,
                            printStations = emptyList())
                }
                else {
                    PrintStationFindResult(printStations = printStationDao.findByAddressCity(addressComponent.city))
                }
            }
        }

    override val nearestDataFetcher: DataFetcher<PrintStationFindResultSingle>
        get() {
            return DataFetcher { env ->
                val longitude = env.getArgument<Double>("longitude")
                val latitude = env.getArgument<Double>("latitude")

                val addressComponent = positionService.getAddressComponent(longitude, latitude)
                if (addressComponent == null) {
                    PrintStationFindResultSingle(
                            result = ResultCode.CITY_NOT_FOUND.value,
                            description = ResultCode.CITY_NOT_FOUND.desc,
                            printStation = null)
                }
                else {
                    val printStations = printStationDao.findByAddressCity(addressComponent.city)
                    val nearest = printStations.minBy { distance(longitude, latitude, it.position.longitude, it.position.latitude) }

                    if (nearest == null) {
                        PrintStationFindResultSingle(
                                result = ResultCode.PRINT_STATION_NOT_FOUND.value,
                                description = ResultCode.PRINT_STATION_NOT_FOUND.desc,
                                printStation = null)
                    }
                    else {
                        PrintStationFindResultSingle(printStation = nearest)
                    }
                }
            }
        }

    override val byDistanceDataFetcher: DataFetcher<PrintStationFindResult>
        get() {
            return DataFetcher { env ->
                val longitude = env.getArgument<Double>("longitude")
                val latitude = env.getArgument<Double>("latitude")
                val radius = env.getArgument<Double>("radius")

                val idPosDistMap = HashMap<Int, Double>()
                val printStations = printStationDao
                        .findAll()
                        .filter {
                            printStation ->
                                idPosDistMap.computeIfAbsent(printStation.positionId, { _ ->
                                    val pos = printStation.position
                                    distance(longitude, latitude, pos.longitude, pos.latitude)
                                }) < radius
                        }

                PrintStationFindResult(printStations = printStations)
            }
        }

    override val newAdSetDataFetcher: DataFetcher<AdSet?>
        get() {
            return DataFetcher { env ->
                val printStationSessionId = env.getArgument<String>("sessionId")
                val currentAdSetId = env.getArgument<Int>("currentAdSetId")
                val currentAdSetTime = dateTimeFormat.get().parse(env.getArgument<String>("currentAdSetTime"))

                val session = getPrintStationLoginSession(printStationSessionId)
                if (session == null) {
                    throw org.springframework.security.access.AccessDeniedException("PrintStation login session invalid")
                }
                else {
                    val printStation = printStationDao.findOne(session.printStationId)
                    val adSet = printStation?.adSet
                    if (adSet != null && (adSet.id != currentAdSetId || adSet.updateTime.timeInMillis > currentAdSetTime.time)) {
                        adSet
                    }
                    else {
                        null
                    }
                }
            }
        }

    override val currentSoftwareVersionDataFetcher: DataFetcher<Int>
        get() {
            return DataFetcher {
                try {
                    var version = 0
                    val context = it.getContext<HashMap<String, Any>>()
                    val sessionId = context["printStationLoginSessionId"] as? String?

                    if (sessionId != null) {
                        val loginSession = getPrintStationLoginSession(sessionId)
                        if (loginSession != null) {
                            val printStation = printStationDao.findOne(loginSession.printStationId)
                            if (printStation?.updateToVersion != null) {
                                version = printStation.updateToVersion!!
                            }
                        }
                    }

                    if (version == 0) {
                        val versionFile = File(assetsDir, "home/current.txt")
                        version = versionFile.reader().use { it.readText().trim(' ', '\r', '\n', '\t').toInt() }
                    }

                    version
                } catch (e: Exception) {
                    e.printStackTrace()
                    0
                }
            }
        }

    private val AVERAGE_RADIUS_OF_EARTH_M = 6371000

    fun distance(long1: Double, lat1: Double,
                 long2: Double, lat2: Double): Double {

        val latDistance = Math.toRadians(lat1 - lat2)
        val lngDistance = Math.toRadians(long1 - long2)

        val sinLatDist = Math.sin(latDistance / 2)
        val sinLongDist = Math.sin(lngDistance / 2)

        val a = sinLatDist * sinLatDist +
                Math.cos(Math.toRadians(lat1)) *
                Math.cos(Math.toRadians(lat2)) *
                sinLongDist * sinLongDist

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return AVERAGE_RADIUS_OF_EARTH_M * c
    }

    @Transactional
    override fun createPrintStationTask(printStationId: Int, type: PrintStationTaskType, param: String) {
        val task = PrintStationTask()
        task.createTime = Calendar.getInstance()
        task.fetched = false
        task.printStationId = printStationId
        task.type = type.value
        task.param = param

        printStationTaskDao.save(task)
    }

    override fun orderReprintTaskExists(printStationId: Int, orderId: Int): Boolean {
        return printStationTaskDao.existsByPrintStationIdAndParamAndFetchedIsFalse(printStationId, orderId.toString())
    }

    @Transactional
    override fun getUnFetchedPrintStationTasks(printStationSessionId: String, taskIdAfter: Int): List<PrintStationTask> {
        val session = getPrintStationLoginSession(printStationSessionId)
        return if (session != null) {
            val curTime = System.currentTimeMillis()
            val tasks = printStationTaskDao.findByPrintStationIdAndIdGreaterThanAndFetchedIsFalse(session.printStationId,taskIdAfter)
            for (task in tasks) {
                if (task.createTime.timeInMillis < curTime - 30 * 60 * 1000) {  //超过30分钟
                    logger.info("PrintStationTask expired, task id=${task.id}, printSatationId=${task.printStationId}, type=${task.type}, param=${task.param}, createTime=${task.createTime.format()}")

                    task.fetched = true
                    printStationTaskDao.save(task)
                }
            }

            tasks.filter { !it.fetched }
        }
        else {
            emptyList()
        }
    }

    @Transactional
    override fun printStationTaskFetched(printStationSessionId: String, taskId: Int): Boolean {
        val session = getPrintStationLoginSession(printStationSessionId)
        if (session != null) {
            val task = printStationTaskDao.findOne(taskId)
            if (task != null && task.printStationId == session.printStationId) {
                task.fetched = true
                printStationTaskDao.save(task)

                return true
            }
        }

        return false
    }

    @Transactional
    override fun printStationTaskFetched(printStationId: Int, printOrderId: Int) {
        val tasks = printStationTaskDao.findByPrintStationIdAndParam(printStationId, printOrderId.toString())
        tasks.forEach {
            if (!it.fetched) {
                it.fetched = true
                printStationTaskDao.save(tasks)
            }
        }
    }

    override fun uploadLog(printStationSessionId: String, fileName: String, logText: String): Boolean {
        if (fileName == "" ||
                fileName.contains('/') ||
                fileName.contains('\\') ||
                fileName.contains("..")) {
            return false
        }

        val session = getPrintStationLoginSession(printStationSessionId)
        if (session == null) {
            return false
        }

        val logDir = File(assetsDir, "printStation/log/${session.printStationId}")
        logDir.mkdirs()

        File(logDir, fileName).writeText(logText)

        return true
    }

    @Transactional
    override fun updatePrintStationStatus(printStationSessionId: String, status: PrintStationStatus, additionalInfo: String): Boolean {
        val session = getPrintStationLoginSession(printStationSessionId)
        return if (session != null) {
            val printStation = printStationDao.findOne(session.printStationId)
            printStation.status = status.value
            printStationDao.save(printStation)

            logger.info("PrintStation id:${session.printStationId}, status updated to $status, additionalInfo: $additionalInfo")
            true
        }
        else {
            false
        }
    }
}

