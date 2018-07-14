package com.unicolour.joyspace.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.dto.*
import com.unicolour.joyspace.exception.ProcessException
import com.unicolour.joyspace.model.*
import com.unicolour.joyspace.model.PrintStationProduct
import com.unicolour.joyspace.service.*
import com.unicolour.joyspace.util.format
import graphql.schema.DataFetcher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.ImageIO
import javax.transaction.Transactional
import kotlin.collections.HashMap


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
    lateinit var smsService: SmsService

    @Autowired
    lateinit var printerStatRecordDao: PrinterStatRecordDao

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
    lateinit var iccConfigDao: IccConfigDao

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var transactionTemplate: TransactionTemplate

    @Autowired
    lateinit var printStationLoginSessionDao: PrintStationLoginSessionDao

    @Autowired
    lateinit var adSetService: AdSetService

    @Autowired
    lateinit var objectMapper: ObjectMapper

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
    override val loginDataFetcher: DataFetcher<PrintStationLoginResultOld>
        get() {
            return DataFetcher<PrintStationLoginResultOld> { env ->
                val printStationId = env.getArgument<Int>("printStationId")
                val password = env.getArgument<String>("password")
                val version = env.getArgument<Int?>("version")
                val uuid = env.getArgument<String?>("uuid") ?: ""
                val ret = transactionTemplate.execute {
                    login(printStationId, password, version, uuid)
                }

                PrintStationLoginResultOld(
                        result = ret.result,
                        sessionId = ret.sessionId,
                        printerType = ret.printerType.name,
                        resolution = ret.printerType.resolution
                )
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

            val newPubKey = loadPublicKey(printStationId)

            if (newPubKey != null) {
                val printStation = printStationDao.findOne(printStationId)
                printStation.loginSequence = null
                printStationDao.save(printStation)
            }

            newPubKey != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @Transactional
    override fun loginWithKey(printStationId: Int, signStr: String, version: Int?): PrintStationLoginResult {
        val printStation = printStationDao.findOne(printStationId)

        if (printStation == null) {
            return PrintStationLoginResult(result = 1)  //没有找到指定的自助机
        }

        if (printStation.uuid.isNullOrBlank()) {
            return PrintStationLoginResult(result = 2)
        }

        val pubKey = loadPublicKey(printStation.id)
        if (pubKey == null) {
            return PrintStationLoginResult(result = 2)     //没有公钥
        }

        val printerType = printerTypeDao.findOne(printStation.printerType)
        if (printerType == null) {
            PrintStationLoginResult(result = 4)  //未知的打印机类型
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

                if (printStation.lastLoginVersion != version) {
                    printStation.lastLoginVersion = version
                }

                printStationDao.save(printStation)

                val oldSession = printStationLoginSessionDao.findByPrintStationId(printStation.id)
                if (oldSession != null) {
                    printStationLoginSessionDao.delete(oldSession)
                }

                val newSession = PrintStationLoginSession()
                newSession.id = UUID.randomUUID().toString().replace("-", "")
                newSession.printStationId = printStation.id
                newSession.expireTime = Calendar.getInstance().apply { add(Calendar.SECOND, 3600) }
                printStationLoginSessionDao.save(newSession)

                return PrintStationLoginResult(sessionId = newSession.id, printerType = printerType.toDTO())
            }
        }

        return PrintStationLoginResult(result = 2)   //验证失败
    }

    @Transactional
    override fun login(printStationId: Int, password: String, version: Int?, uuid: String): PrintStationLoginResult {
        val printStation = printStationDao.findOne(printStationId)

        if (printStation == null) {
            return PrintStationLoginResult(result = 1)  //没有找到指定的自助机
        }

        val pubKey = loadPublicKey(printStation.id)
        if (pubKey != null) {
            return PrintStationLoginResult(result = 5)    //已经有公钥了，禁止密码登录
        }

        if (!passwordEncoder.matches(password, printStation.password)) {
            return PrintStationLoginResult(result = 2)   //密码错误
        }

        val session = printStationLoginSessionDao.findByPrintStationId(printStation.id)
        if (session != null) {
            val time = Calendar.getInstance().apply { add(Calendar.SECOND, 3600 - 30) }

            if (session.expireTime.timeInMillis > time.timeInMillis) {    //自助机30秒之内访问过后台
                if (!printStation.uuid.isNullOrBlank() && printStation.uuid != uuid) {  //其他自助机已经登录
                    return PrintStationLoginResult(result = 3)    //已经在其他机器上登录过
                }
            }
        }

        val printerType = printerTypeDao.findOne(printStation.printerType)
        if (printerType == null) {
            PrintStationLoginResult(result = 4)  //未知的打印机类型
        }

        var printStationChanged = false
        if (uuid.isNotEmpty()) {
            printStation.uuid = uuid
            printStationChanged = true
        }

        if (printStation.lastLoginVersion != version) {
            printStation.lastLoginVersion = version
            printStationChanged = true
        }

        if (printStationChanged) {
            printStationDao.save(printStation)
        }

        if (session != null) {
            printStationLoginSessionDao.delete(session)
        }

        val newSession = PrintStationLoginSession()
        newSession.id = UUID.randomUUID().toString().replace("-", "")
        newSession.printStationId = printStation.id
        newSession.expireTime = Calendar.getInstance().apply { add(Calendar.SECOND, 3600) }
        printStationLoginSessionDao.save(newSession)

        return PrintStationLoginResult(sessionId = newSession.id, printerType = printerType.toDTO())
    }

    override fun initPublicKey(printStationId: Int, uuid: String, pubKeyStr: String): Int {
        val printStation = printStationDao.findOne(printStationId)

        if (printStation == null) {
            return 1  //没有找到指定的自助机
        }

        if (printStation.uuid != uuid) {
            return 2  //uuid 不符合
        }

        val publicKey = loadPublicKey(printStationId)
        if (publicKey != null) {
            return 3  //已经初始化过
        }

        if (!savePublicKey(printStation.id, pubKeyStr)) {
            return 4  //保存公钥失败
        }
        else {
            printStation.loginSequence = null
            printStationDao.save(printStation)
            return 0  //成功
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
        var newIccFileName:String? = null
        var iccConfigs: List<IccConfigDTO>? = null

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

            if (currentVersion in 109..125) {
                newIccFileName = printerTypeDao.findOne(printStation.printerType)?.defaultIccFileName
            }
            else if (currentVersion > 125) {
                iccConfigs = iccConfigDao.findAll().map {
                    IccConfigDTO(
                            printerModel = it.printerModel,
                            osName = it.osName,
                            iccFileName = it.iccFileName
                    )
                }

                if (iccConfigs.isEmpty()) {
                    iccConfigs = null
                }
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
                adSet = adSetService.adSetToDTO(adSet),
                defaultIccFileName = newIccFileName,
                iccConfigs = iccConfigs
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
    override fun updatePrintStation(id: Int, printStationName: String, positionId: Int, transferProportion: Int,
                                    printerType: String, adSetId: Int, selectedProductIds: Set<Int>): Boolean {
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
            printStation.printerType = printerType

            if (adSetId > 0) {
                printStation.adSet = adSetDao.findOne(adSetId)
            }
            else if (adSetId == 0) {
                printStation.adSet = null
            }

            if (managerService.loginManagerHasRole("ROLE_SUPERADMIN")) {
                printStation.transferProportion = transferProportion
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
    override fun activatePrintStation(manager: Manager?, code: String, name: String, password: String,
                                      positionId: Int, selectedProductIds: Set<Int>, uuid: String) {
        val loginManager = manager ?: managerService.loginManager
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

        if (uuid.isNotEmpty()) {
            printStation.uuid = uuid
        }

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

    override fun addDownLoadUserImgTask(printStationId: Int, imgUrl: String): Boolean {
        val task = PrintStationTask()
        task.param = imgUrl
        task.printStationId = printStationId
        task.createTime = Calendar.getInstance()
        task.type = PrintStationTaskType.DOWNLOAD_IMG.value
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

    override fun updatePrintStationPrinterInfo(sessionId: String, printerInfo: PrinterInfoDTO): Boolean {
        val session = getPrintStationLoginSession(sessionId)
        if (session == null) {
            return false
        }

        val printStation = printStationDao.findOne(session.printStationId)
        if (printStation == null) {
            return false
        }

        printStation.printerModel = printerInfo.model
        printStationDao.save(printStation)

        val printerInfoDir = File(assetsDir, "printStation/printerInfo")
        printerInfoDir.mkdirs()

        objectMapper.writeValue(File(printerInfoDir, "${session.printStationId}.json"), printerInfo)
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

    override fun getHomeInitInfo(userName: String, password: String, printStationId: Int): HomeInitInfoDTO {
        val manager = managerService.login(userName, password)
        if (manager == null) {
            return HomeInitInfoDTO(result = ResultCode.MANAGER_NOT_LOG_IN.value)
        }
        else {
            val printStation = printStationDao.findOne(printStationId)
            if (printStation == null) {
                return HomeInitInfoDTO(result = ResultCode.PRINT_STATION_NOT_FOUND.value)
            }
            else if (printStation.companyId != manager.companyId) {
                return HomeInitInfoDTO(result = ResultCode.PRINT_STATION_NOT_BELONG_TO_COMPANY.value)
            }

            return HomeInitInfoDTO(
                    result = ResultCode.SUCCESS.value,
                    printerType = printStation.printerType
            )
        }
    }

    @Transactional
    override fun initHome(input: HomeInitInput): ResultCode {
        logger.info("home init, admin username: ${input.username}, printStationId: ${input.printStationId}, uuid: ${input.uuid}")

        val manager = managerService.login(input.username, input.password)
        if (manager == null) {
            logger.info("home init failed, admin login failed.")
            return ResultCode.MANAGER_NOT_LOG_IN
        } else {
            val printStation = printStationDao.findOne(input.printStationId)
            if (printStation == null) {
                logger.info("home init failed, print station not found.")
                return ResultCode.PRINT_STATION_NOT_FOUND
            } else if (printStation.companyId != manager.companyId) {
                logger.info("home init failed, print station not belong to company.")
                return ResultCode.PRINT_STATION_NOT_BELONG_TO_COMPANY
            }

            return if (savePublicKey(input.printStationId, input.publicKey)) {
                printStation.uuid = input.uuid
                printStationDao.save(printStation)

                ResultCode.SUCCESS
            }
            else {
                logger.info("home init failed, save public key failed.")
                ResultCode.OTHER_ERROR
            }
        }
    }

    @Transactional
    override fun recordPrinterStat(sessionId: String, printerSn: String, printerType: String, printerName: String, mediaCounter: Int, errorCode: Int): Boolean {
        val session = printStationLoginSessionDao.findOne(sessionId)
        if (session != null) {
            logger.info("Report printer stat, printerSerialNo: $printerSn, printerType: $printerType, printerName: $printerName, mediaCounter: $mediaCounter, errorCode: $errorCode")

            val printStation = printStationDao.findOne(session.printStationId)
            val position = printStation.position

            val lastRecord = printerStatRecordDao.findFirstByPrintStationIdOrderByIdDesc(printStation.id)
            if (lastRecord != null && lastRecord.mediaCounter == mediaCounter && lastRecord.errorCode == errorCode) {
                logger.info("Report printer stat, mediaCounter and errorCode not changed")
                return true
            }

            val manager = managerService.getCompanyManager(printStation.companyId)

            val record = PrinterStatRecord()

            record.reportTime = Calendar.getInstance()
            record.companyId = printStation.companyId
            record.positionId = printStation.positionId
            record.printStationId = printStation.id
            record.printerSerialNo = printerSn
            record.printerType = printerType
            record.printerName = printerName
            record.mediaCounter = mediaCounter
            record.errorCode = errorCode

            val printerTypeRecord = findPrinterType(printerType)
            val alertThresholds = printerTypeRecord?.mediaAlertThresholds?.splitToSequence(',')?.map { it.toIntOrNull() }

            val phoneNumber = manager?.cellPhone ?: manager?.phone
            if (phoneNumber != null && alertThresholds != null && !alertThresholds.contains(null)) {
                var mediaCounterThreshold = 0
                for (alertThreshold in alertThresholds) {
                    if ((lastRecord == null || lastRecord.mediaCounter >= alertThreshold!!) && mediaCounter < alertThreshold!!) {
                        mediaCounterThreshold = alertThreshold
                        break
                    }
                }

                if (mediaCounterThreshold > 0) {
                    val smsTpl = "【优利绚彩】您在%s的%d号设备，目前耗材已不足以打印%d张，请您提前准备更换耗材"

                    val sendResult = smsService.send(phoneNumber, String.format(smsTpl, position.name, printStation.id, mediaCounterThreshold))
                    if (sendResult.first != 3) {
                        logger.error("Send Printer Stat SMS error, PhoneNumber: $phoneNumber, ResponseCode: ${sendResult.first}, ResponseId: ${sendResult.second}")
                    } else {
                        logger.info("Send Printer Stat SMS success, PhoneNumber: $phoneNumber, ResponseCode: ${sendResult.first}, ResponseId: ${sendResult.second}")
                        record.sendToPhoneNumber = phoneNumber
                    }
                }
            }

            if (phoneNumber != null && record.printerType == "CY" && record.errorCode != 0) {
                val errorCodeObj = CyPrinterErrorCode.values().firstOrNull { it.value == record.errorCode }
                if (errorCodeObj != null && errorCodeObj.sendSms) {
                    val smsTpl = "【优利绚彩】您在%s的%d号设备，%s"

                    val sendResult = smsService.send(phoneNumber, String.format(smsTpl, position.name, printStation.id, errorCodeObj.message))
                    if (sendResult.first != 3) {
                        logger.error("Send Printer Stat SMS error, PhoneNumber: $phoneNumber, ResponseCode: ${sendResult.first}, ResponseId: ${sendResult.second}")
                    } else {
                        logger.info("Send Printer Stat SMS success, PhoneNumber: $phoneNumber, ResponseCode: ${sendResult.first}, ResponseId: ${sendResult.second}")
                        record.sendToPhoneNumber = phoneNumber
                    }
                }
            }

            printerStatRecordDao.save(record)

            return true
        }
        else {
            return false
        }
    }

    private fun findPrinterType(printerType: String): PrinterType? {
        var pTypeRecord = printerTypeDao.findOne(printerType)
        if (pTypeRecord == null && printerType.contains("EPSON")) {
            pTypeRecord = printerTypeDao.findOne("EPSON")
        }

        return pTypeRecord
    }

    override fun getPrintStationQrCodeUrl(printStationId: Int, noBackground: Boolean): String {
        val qrCodeImgFileName = if (noBackground) {
            "${printStationId}_nobg.png"
        } else {
            "$printStationId.png"
        }

        val psQrCodeImgFile = File(assetsDir, "printStation/qrCode/$qrCodeImgFileName")

        if (!psQrCodeImgFile.exists()) {
            psQrCodeImgFile.parentFile.mkdirs()
            createPrintStationQrCodeImageFile(printStationId, noBackground, psQrCodeImgFile)
        }

        return "$baseUrl/assets/printStation/qrCode/$qrCodeImgFileName"
    }

    private fun createPrintStationQrCodeImageFile(printStationId: Int, noBackground: Boolean, psQrCodeImgFile: File) {
        val psUrl = getPrintStationUrl(printStationId)

        val qrCodeAreaX = if (noBackground) 0 else 195
        val qrCodeAreaY = if (noBackground) 0 else 368
        val qrCodeAreaSize = 492

        val bgImg =
                if (noBackground) {
                    BufferedImage(qrCodeAreaSize, qrCodeAreaSize, BufferedImage.TYPE_INT_RGB)
                } else {
                    ImageIO.read(PrintStationServiceImpl::class.java.getResourceAsStream("/static/img/print_station_qr_code_bg.png"))
                }

        val graphics = bgImg.createGraphics()

        val hintMap = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
        hintMap[EncodeHintType.CHARACTER_SET] = "utf-8"
        hintMap[EncodeHintType.MARGIN] = 4
        hintMap[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.M

        val qrCodeWriter = QRCodeWriter()
        val byteMatrix = qrCodeWriter.encode(psUrl, BarcodeFormat.QR_CODE, qrCodeAreaSize, qrCodeAreaSize, hintMap)
        val matrixWidth = byteMatrix.width
        val matrixHeight = byteMatrix.height

        if (noBackground) {
            graphics.color = Color.WHITE
            graphics.fillRect(0, 0, qrCodeAreaSize, qrCodeAreaSize)
        }

        graphics.color = Color.BLACK
        for (x in 0 until matrixWidth) {
            for (y in 0 until matrixHeight) {
                if (byteMatrix.get(x, y)) {
                    graphics.fillRect(x + qrCodeAreaX, y + qrCodeAreaY, 1, 1)
                }
            }
        }

        if (!noBackground) {
            val labelFontSize = 48
            val labelAreaX = 473
            val labelAreaY = 1037
            val labelAreaW = 263
            val labelAreaH = 71

            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            graphics.font = Font(Font.SANS_SERIF, Font.BOLD, labelFontSize)
            val labelFontMetrics = graphics.fontMetrics
            val labelHei = labelFontMetrics.height
            val labelWid = labelFontMetrics.stringWidth(printStationId.toString())

            graphics.drawString(printStationId.toString(),
                    labelAreaX + (labelAreaW - labelWid) / 2,
                    labelAreaY + (labelAreaH - labelHei) / 2 + labelFontMetrics.getAscent())
            graphics.dispose()
        }

        ImageIO.write(bgImg, "png", psQrCodeImgFile)
    }
}

private fun PrinterType.toDTO(): PrinterTypeDTO {
    return PrinterTypeDTO(
            name = this.name,
            displayName = this.displayName,
            rollPaper = this.rollPaper,
            resolution = this.resolution
    )
}

