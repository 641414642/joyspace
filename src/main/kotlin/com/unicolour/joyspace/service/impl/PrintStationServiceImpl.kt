package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.dto.*
import com.unicolour.joyspace.model.Position
import com.unicolour.joyspace.model.PriceListItem
import com.unicolour.joyspace.model.PrintStation
import com.unicolour.joyspace.model.PrintStationProduct
import com.unicolour.joyspace.service.ManagerService
import com.unicolour.joyspace.service.PriceListService
import com.unicolour.joyspace.service.PrintStationService
import graphql.schema.DataFetcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

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
    lateinit var productDao: ProductDao

    @Autowired
    lateinit var printStationProductDao: PrintStationProductDao

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
    override fun createPrintStation(wxQrCode: String, positionId: Int, selectedProductIds: Set<Int>): PrintStation? {
        val loginManager = managerService.loginManager
        if (loginManager == null) {
            return null
        }

        val manager = managerDao.findOne(loginManager.managerId)

        val printStation = PrintStation()
        printStation.wxQrCode = wxQrCode
        printStation.company = manager.company
        printStation.position = positionDao.findOne(positionId)

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
    override fun updatePrintStation(id: Int, wxQrCode: String, positionId: Int, selectedProductIds: Set<Int>): Boolean {
        val printStation = printStationDao.findOne(id)

        if (printStation != null) {
            printStation.wxQrCode = wxQrCode
            printStation.position = positionDao.findOne(positionId)

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

    override val printStationDataFetcher: DataFetcher<PrintStationDetailDTO>
        get() {
            return DataFetcher { env ->
                val printStationId = env.getArgument<Int>("printStationId")
                val context = env.getContext<HashMap<String, Any>>()
                val baseUrl = context["baseUrl"] as String

                val printStation = printStationDao.findOne(printStationId)
                val priceMap = getPriceMap(printStation)

                val products = printStationProductDao.findByPrintStationId(printStationId).map { it.product.productToDTO(baseUrl, priceMap) }
                printStation.printStationToDetailDTO(products)
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
                                idPosDistMap.computeIfAbsent(printStation.positionId, { posId ->
                                    val pos = printStation.position
                                    distance(longitude, latitude, pos.longitude, pos.latitude)
                                }) < radius
                        }

                PrintStationFindResult(printStations = printStations.map { it.printStationToDTO() })
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
