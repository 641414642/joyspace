package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.dto.AddressComponent
import com.unicolour.joyspace.dto.QQMapGeoDecodeResult
import com.unicolour.joyspace.dto.ResultCode
import com.unicolour.joyspace.exception.ProcessException
import com.unicolour.joyspace.model.Position
import com.unicolour.joyspace.model.PositionImageFile
import com.unicolour.joyspace.service.ManagerService
import com.unicolour.joyspace.service.PositionService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import java.util.regex.Pattern
import javax.transaction.Transactional

@Service
open class PositionServiceImpl : PositionService {
    companion object {
        val logger = LoggerFactory.getLogger(PositionServiceImpl::class.java)
    }

    @Value("\${com.unicolour.joyspace.assetsDir}")
    lateinit var assetsDir: String

    @Value("\${com.unicolour.joyspace.qqMapKey}")
    lateinit var qqMapKey: String

    @Autowired
    lateinit var managerService : ManagerService

    @Autowired
    lateinit var managerDao : ManagerDao

    @Autowired
    lateinit var positionDao : PositionDao

    @Autowired
    lateinit var printStationDao: PrintStationDao

    @Autowired
    lateinit var priceListDao : PriceListDao

    @Autowired
    lateinit var positionImgFileDao : PositionImageFileDao

    @Autowired
    lateinit var restTemplate: RestTemplate

    override fun getAddressComponent(longitude: Double, latitude: Double): AddressComponent? {
        val resp = restTemplate.exchange(
                "https://apis.map.qq.com/ws/geocoder/v1/?location={location}&key={key}",
                HttpMethod.GET,
                null,
                QQMapGeoDecodeResult::class.java,
                mapOf(
                        "location" to "$latitude,$longitude",
                        "key" to qqMapKey
                )
        )

        return if (resp != null &&
                resp.statusCode == HttpStatus.OK &&
                resp.body?.result?.address_component != null) {
            resp.body?.result?.address_component
        }
        else {
            logger.warn("QQMap GeoDecode failed, location=$latitude,$longitude, result=$resp")
            null
        }
    }

    @Transactional
    override fun createPosition(name: String, address: String, transportation: String, longitude: Double, latitude: Double, priceListId: Int): Position? {
        val loginManager = managerService.loginManager
        if (loginManager == null) {
            return null
        }
        
        val manager = managerDao.findOne(loginManager.managerId)
        val addressComponent = getAddressComponent(longitude, latitude)

        if (addressComponent == null) {
            throw ProcessException(ResultCode.GEO_DECODE_FAILED)
        }

        val position = Position()
        position.name = name
        position.address = address
        position.transportation = transportation
        position.company = manager.company
        position.latitude = latitude
        position.longitude = longitude
        position.addressNation = addressComponent.nation
        position.addressProvince = addressComponent.province
        position.addressCity = addressComponent.city
        position.addressDistrict = addressComponent.district
        position.addressStreet = addressComponent.street
        position.priceList =
                if (priceListId <= 0)
                    null
                else
                    priceListDao.findOne(priceListId)

        positionDao.save(position)
        return position
    }

    @Transactional
    override fun updatePosition(id: Int, name: String, address: String, transportation: String, longitude: Double, latitude: Double, priceListId: Int): Boolean {
        val position = positionDao.findOne(id)
        val addressComponent = getAddressComponent(longitude, latitude)

        if (addressComponent == null) {
            throw ProcessException(ResultCode.GEO_DECODE_FAILED)
        }

        if (position != null) {
            position.name = name
            position.address = address
            position.transportation = transportation
            position.latitude = latitude
            position.longitude = longitude
            position.addressNation = addressComponent.nation
            position.addressProvince = addressComponent.province
            position.addressCity = addressComponent.city
            position.addressDistrict = addressComponent.district
            position.addressStreet = addressComponent.street
            position.priceList =
                    if (priceListId <= 0)
                        null
                    else
                        priceListDao.findOne(priceListId)

            positionDao.save(position)

            //更新属于此投放地点的所有自助机的地址信息
            val printStations = printStationDao.findByPositionId(position.id)
            for (printStation in printStations) {
                printStation.addressNation = position.addressNation
                printStation.addressProvince = position.addressProvince
                printStation.addressCity = position.addressCity
                printStation.addressDistrict = position.addressDistrict
                printStation.addressStreet = position.addressStreet
            }
            printStationDao.save(printStations)

            return true
        }
        else {
            return false
        }
    }

    @Transactional
    override fun uploadPositionImageFile(id: Int, imageFile: MultipartFile?): PositionImageFile? {
        val position = positionDao.findOne(id)
        if (position != null) {
            if (imageFile != null) {
                val uuid = UUID.randomUUID().toString()
                val file = File(assetsDir, "/position/images/$uuid")
                file.parentFile.mkdirs()

                imageFile.transferTo(file)

                val pb = ProcessBuilder("magick", "identify", file.absolutePath)

                val process = pb.start()

                var retStr:String = "";
                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    retStr = reader.readText()
                }

                val retCode = process.waitFor()

                if (retCode != 0) {
                    file.delete()
                    throw IOException("not valid image file")
                }
                else {
                    val patternStr = Pattern.quote(file.absolutePath) + "\\s(\\w+)\\s.*"
                    val pattern = Pattern.compile(patternStr)
                    val matcher = pattern.matcher(retStr)

                    matcher.find()

                    var imgType = matcher.group(1).toLowerCase()
                    if (imgType == "jpeg") {
                        imgType = "jpg"
                    }

                    val positionImgFile = PositionImageFile()
                    positionImgFile.position = position
                    positionImgFile.fileType = imgType
                    positionImgFileDao.save(positionImgFile)

                    val fileWithExt = File(assetsDir, "/position/images/${positionImgFile.id}.$imgType")
                    file.renameTo(fileWithExt)

                    return positionImgFile
                }
            }
        }

        return null
    }

    override fun deletePositionImageFile(imgFileId: Int): Boolean {
        val positionImgFile = positionImgFileDao.findOne(imgFileId)
        if (positionImgFile != null) {
            positionImgFileDao.delete(positionImgFile)
            val fileWithExt = File(assetsDir, "/position/images/${positionImgFile.id}.${positionImgFile.fileType}")
            fileWithExt.delete()

            return true
        }
        else {
            return false
        }
    }
}