package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.model.*
import com.unicolour.joyspace.service.ManagerService
import com.unicolour.joyspace.service.PositionService
import graphql.schema.DataFetcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
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
    @Value("\${com.unicolour.joyspace.assetsDir}")
    lateinit var assetsDir: String

    @Autowired
    lateinit var managerService : ManagerService

    @Autowired
    lateinit var managerDao : ManagerDao

    @Autowired
    lateinit var positionDao : PositionDao

    @Autowired
    lateinit var priceListDao : PriceListDao

    @Autowired
    lateinit var cityDao : CityDao

    @Autowired
    lateinit var positionImgFileDao : PositionImageFileDao

    @Transactional
    override fun createPosition(name: String, address: String, transportation: String, longitude: Double, latitude: Double, priceListId: Int): Position? {
        val loginManager = managerService.loginManager
        if (loginManager == null) {
            return null
        }
        
        val manager = managerDao.findOne(loginManager.managerId)
        val city = cityDao.findByLocation(longitude, latitude)

        val position = Position()
        position.name = name
        position.address = address
        position.transportation = transportation
        position.company = manager.company
        position.latitude = latitude
        position.longitude = longitude
        position.city = city!!
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
        val city = cityDao.findByLocation(longitude, latitude)

        if (position != null) {
            position.name = name
            position.address = address
            position.transportation = transportation
            position.latitude = latitude
            position.longitude = longitude
            position.city = city!!
            position.priceList =
                    if (priceListId <= 0)
                        null
                    else
                        priceListDao.findOne(priceListId)

            positionDao.save(position)
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