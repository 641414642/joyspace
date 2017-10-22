package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.dto.*
import com.unicolour.joyspace.model.*
import com.unicolour.joyspace.service.ManagerService
import com.unicolour.joyspace.service.PriceListService
import com.unicolour.joyspace.service.PrintStationService
import com.unicolour.joyspace.service.ProductService
import graphql.schema.DataFetcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.util.*
import javax.transaction.Transactional
import kotlin.collections.HashMap

@Service
open class PrintStationServiceImpl : PrintStationService {
    @Autowired
    lateinit var managerService : ManagerService

    @Autowired
    lateinit var managerDao : ManagerDao

    @Autowired
    lateinit var positionDao : PositionDao

    @Autowired
    lateinit var printStationDao: PrintStationDao

    @Autowired
    lateinit var priceListService: PriceListService

    @Autowired
    lateinit var productService: ProductService

    @Autowired
    lateinit var productDao: ProductDao

    @Autowired
    lateinit var printStationProductDao: PrintStationProductDao

    @Autowired
    lateinit var cityDao: CityDao

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var transactionTemplate: TransactionTemplate

    @Autowired
    lateinit var printStationLoginSessionDao: PrintStationLoginSessionDao

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
                "name" -> "自助机${printStation.id}"
                "address" -> printStation.position.address
                "latitude" -> printStation.position.latitude
                "longitude" -> printStation.position.longitude
                "transportation" -> printStation.position.transportation
                "images" -> {
                    val context = env.getContext<HashMap<String, Any>>()
                    val baseUrl = context["baseUrl"]
                    printStation.position.imageFiles
                            .map { "${baseUrl}/assets/position/images/${it.id}.${it.fileType}" }
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
                transactionTemplate.execute { login(printStationId, password) }
            }
        }

    private fun login(printStationId: Int, password: String): PrintStationLoginResult {
        val printStation = printStationDao.findOne(printStationId)

        if (printStation != null) {
            if (passwordEncoder.matches(password, printStation.password)) {
                var session = printStationLoginSessionDao.findByPrintStationId(printStation.id)
                if (session != null) {
                    printStationLoginSessionDao.delete(session)
                }

                session = PrintStationLoginSession()
                session.id = UUID.randomUUID().toString().replace("-", "")
                session.printStationId = printStation.id
                session.expireTime = Calendar.getInstance()
                session.expireTime.add(Calendar.SECOND, 3600)
                printStationLoginSessionDao.save(session)

                return PrintStationLoginResult(sessionId = session.id)
            }
            else {
                return PrintStationLoginResult(result = 2)
            }
        }
        else {
            return PrintStationLoginResult(result = 1)
        }
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

        return priceMap;
    }

    @Transactional
    override fun createPrintStation(password: String, wxQrCode: String, positionId: Int, selectedProductIds: Set<Int>): PrintStation? {
        val loginManager = managerService.loginManager
        if (loginManager == null) {
            return null
        }

        val manager = managerDao.findOne(loginManager.managerId)

        val printStation = PrintStation()
        printStation.wxQrCode = wxQrCode
        printStation.company = manager.company
        printStation.position = positionDao.findOne(positionId)
        printStation.city = printStation.position.city
        printStation.password = passwordEncoder.encode(password)

        printStationDao.save(printStation)

        for (productId in selectedProductIds) {
            val printStationProduct = PrintStationProduct()
            printStationProduct.product = productDao.findOne(productId)
            printStationProduct.printStation = printStation

            printStationProductDao.save(printStationProduct);
        }

        return printStation
    }

    @Transactional
    override fun updatePrintStation(id: Int, password: String, wxQrCode: String, positionId: Int, selectedProductIds: Set<Int>): Boolean {
        val printStation = printStationDao.findOne(id)

        if (printStation != null) {
            printStation.wxQrCode = wxQrCode
            printStation.position = positionDao.findOne(positionId)
            printStation.city = printStation.position.city
            if (!password.isNullOrEmpty()) {
                printStation.password = passwordEncoder.encode(password)
            }

            printStationDao.save(printStation)

            printStationProductDao.deleteByPrintStationId(id)

            for (productId in selectedProductIds) {
                val printStationProduct = PrintStationProduct()
                printStationProduct.product = productDao.findOne(productId)
                printStationProduct.printStation = printStation

                printStationProductDao.save(printStationProduct);
            }
            return true
        }
        else {
            return false
        }
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

                val city = cityDao.findByLocation(longitude, latitude)
                if (city == null) {
                    PrintStationFindResult(
                            result = ResultCode.CITY_NOT_FOUND.value,
                            description = ResultCode.CITY_NOT_FOUND.desc,
                            printStations = emptyList())
                }
                else {
                    PrintStationFindResult(printStations = printStationDao.findByCityId(city.id))
                }
            }
        }

    override val nearestDataFetcher: DataFetcher<PrintStationFindResultSingle>
        get() {
            return DataFetcher { env ->
                val longitude = env.getArgument<Double>("longitude")
                val latitude = env.getArgument<Double>("latitude")

                val city = cityDao.findByLocation(longitude, latitude)
                if (city == null) {
                    PrintStationFindResultSingle(
                            result = ResultCode.CITY_NOT_FOUND.value,
                            description = ResultCode.CITY_NOT_FOUND.desc,
                            printStation = null)
                }
                else {
                    val printStations = printStationDao.findByCityId(city.id)
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
}

