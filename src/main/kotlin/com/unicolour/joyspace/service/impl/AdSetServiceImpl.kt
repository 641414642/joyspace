package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.AdImageFileDao
import com.unicolour.joyspace.dao.AdSetDao
import com.unicolour.joyspace.model.AdImageFile
import com.unicolour.joyspace.model.AdSet
import com.unicolour.joyspace.service.AdSetService
import com.unicolour.joyspace.service.ManagerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import java.util.regex.Pattern
import javax.servlet.http.Part
import javax.transaction.Transactional
import kotlin.collections.HashMap

@Component
open class AdSetServiceImpl : AdSetService {
    @Value("\${com.unicolour.joyspace.assetsDir}")
    lateinit var assetsDir: String

    @Value("\${com.unicolour.joyspace.baseUrl}")
    lateinit var baseUrl: String

    @Autowired
    lateinit var adSetDao: AdSetDao

    @Autowired
    lateinit var adImageFileDao: AdImageFileDao

    @Autowired
    lateinit var managerService : ManagerService

    override fun getAdImageUrl(adImageFile: AdImageFile): String {
        return "$baseUrl/assets/ad/${adImageFile.adSet.id}/images/${adImageFile.fileName}.${adImageFile.fileType}"
    }

    private fun saveAdImageFiles(imgFiles: List<Pair<Part, Int>>, adSet: AdSet, seq: Int) {
        var seq1 = seq
        for (imgFile in imgFiles) {
            val uuid = UUID.randomUUID().toString().replace("-", "")
            val file = File(assetsDir, "/ad/${adSet.id}/images/${uuid}")
            file.parentFile.mkdirs()

            imgFile.first.write(file.absolutePath)

            val pb = ProcessBuilder("magick", "identify", file.absolutePath)

            val process = pb.start()

            var retStr = ""
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                retStr = reader.readText()
            }

            val retCode = process.waitFor()

            if (retCode != 0) {
                file.delete()
                throw IOException("not valid image file")
            } else {
                val patternStr = Pattern.quote(file.absolutePath) + "\\s(\\w+)\\s(\\d+)x(\\d+)\\s.*"
                val pattern = Pattern.compile(patternStr)
                val matcher = pattern.matcher(retStr)

                matcher.find()

                var imgType = matcher.group(1).toLowerCase()
                if (imgType == "jpeg") {
                    imgType = "jpg"
                }
                val imgWid = matcher.group(2).toInt()
                val imgHei = matcher.group(3).toInt()

                val adImageFile = AdImageFile()
                adImageFile.adSet = adSet
                adImageFile.fileName = uuid
                adImageFile.description = ""
                adImageFile.duration = imgFile.second
                adImageFile.fileType = imgType
                adImageFile.width = imgWid
                adImageFile.height = imgHei
                adImageFile.sequence = seq1++

                val fileWithExt = File(file.parent, "${file.name}.$imgType")
                file.renameTo(fileWithExt)

                adImageFileDao.save(adImageFile)
            }
        }
    }

    @Transactional
    override fun createAdSet(name: String, imgFiles: List<Pair<Part, Int>>) {
        val loginManager = managerService.loginManager
        val now = Calendar.getInstance()

        val adSet = AdSet()
        adSet.name = name
        adSet.createTime = now
        adSet.updateTime = now
        adSet.companyId = loginManager!!.companyId

        adSetDao.save(adSet)

        saveAdImageFiles(imgFiles, adSet, 1)
    }

    @Transactional
    override fun updateAdSet(id: Int, name: String, imgFiles: List<Pair<Part, Int>>, adSetIdDurationMap: HashMap<Int, Int>): Boolean {
        val adSet = adSetDao.findOne(id)
        if (adSet == null) {
            return false
        }
        else {
            adSet.name = name
            adSet.updateTime = Calendar.getInstance()

            adSetDao.save(adSet)

            saveAdImageFiles(imgFiles, adSet, adSet.imageFiles.map { it.sequence }.max() ?: 0 + 1)

            adSetIdDurationMap.forEach { k, v ->
                val adImg = adImageFileDao.findOne(k)
                adImg.duration = v
                adImageFileDao.save(adImg)
            }

            return true
        }
    }
}