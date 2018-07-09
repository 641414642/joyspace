package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.AdImageFileDao
import com.unicolour.joyspace.dao.AdSetDao
import com.unicolour.joyspace.dto.AdSetDTO
import com.unicolour.joyspace.dto.AdSetImageDTO
import com.unicolour.joyspace.dto.AdSetImageFileDTO
import com.unicolour.joyspace.model.AdImageFile
import com.unicolour.joyspace.model.AdSet
import com.unicolour.joyspace.service.AdSetService
import com.unicolour.joyspace.service.ImageService
import com.unicolour.joyspace.service.ManagerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.transaction.Transactional
import kotlin.collections.ArrayList

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

    @Autowired
    lateinit var imageServcie : ImageService

    private val dateTimeFormat: ThreadLocal<SimpleDateFormat> = ThreadLocal.withInitial { SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS") }

    override fun getAdImageUrl(adImageFile: AdImageFile): String {
        return "$baseUrl/assets/ad/${adImageFile.adSet.id}/images/${adImageFile.fileName}.${adImageFile.fileType}"
    }

    override fun getAdThumbImageUrl(adImageFile: AdImageFile): String {
        val thumbFile = File(assetsDir, "/ad/${adImageFile.adSetId}/images/${adImageFile.fileName}.thumb.jpg")
        if (!thumbFile.exists()) {
            val file = File(assetsDir, "/ad/${adImageFile.adSetId}/images/${adImageFile.fileName}.${adImageFile.fileType}")
            imageServcie.createThumbnailImageFile(file, "50x37^", thumbFile)
        }
        return "$baseUrl/assets/ad/${adImageFile.adSet.id}/images/${adImageFile.fileName}.thumb.jpg"
    }

    override fun adSetToDTO(adSet: AdSet?): AdSetDTO? {
        return if (adSet == null) {
            null
        } else {
            AdSetDTO(
                    id = adSet.id,
                    name = adSet.name,
                    updateTime = dateTimeFormat.get().format(Date(adSet.updateTime.timeInMillis)),
                    imageFiles = adSet.imageFiles.filter { it.enabled }.map {
                        AdSetImageFileDTO(
                                id = it.id,
                                fileName = it.fileName,
                                fileType = it.fileType,
                                width = it.width,
                                height = it.height,
                                duration = it.duration,
                                description = it.description,
                                sequence = it.sequence,
                                url = getAdImageUrl(it)
                        )
                    }
            )
        }
    }

    private fun saveAdImageFiles(imgFiles: List<AdSetImageDTO>, adSet: AdSet) {
        val imgFileIdSet = imgFiles.map { it.adImgId }.filter { it > 0 }.toSet()
        val imgFileToDelete = adSet.imageFiles.filter { !imgFileIdSet.contains(it.id) }

        var seq1 = 1

        for (imgFileDTO in imgFiles) {
            val adSetImagesDir = File(assetsDir, "/ad/${adSet.id}/images")
            adSetImagesDir.mkdirs()

            if (imgFileDTO.adImgId > 0) {
                val adImgFile = adImageFileDao.findOne(imgFileDTO.adImgId)

                if (adImgFile != null && adImgFile.adSetId == adSet.id) {
                    adImgFile.sequence = seq1++
                    adImgFile.duration = imgFileDTO.duration
                    adImgFile.enabled = imgFileDTO.enabled

                    if (imgFileDTO.uploadFileName != "") {
                        val uploadFile = File(assetsDir, "/ad/images/${imgFileDTO.uploadFileName}")
                        val uploadThumbFile = File(assetsDir, "/ad/images/${imgFileDTO.uploadFileName}.thumb.jpg")

                        val dimensionAndType = imageServcie.getImageFileDimensionAndType(uploadFile)

                        val uuid = UUID.randomUUID().toString().replace("-", "")

                        adImgFile.fileName = uuid
                        adImgFile.fileType = dimensionAndType.type
                        adImgFile.width = dimensionAndType.width
                        adImgFile.height = dimensionAndType.height

                        val file = File(assetsDir, "/ad/${adSet.id}/images/$uuid.${dimensionAndType.type}")
                        val thumbFile = File(assetsDir, "/ad/${adSet.id}/images/$uuid.thumb.jpg")

                        uploadFile.renameTo(file)
                        uploadThumbFile.renameTo(thumbFile)
                    }

                    adImageFileDao.save(adImgFile)
                }
            }
            else if (imgFileDTO.uploadFileName == "") {
                continue
            }
            else {
                val uploadFile = File(assetsDir, "/ad/images/${imgFileDTO.uploadFileName}")
                val uploadThumbFile = File(assetsDir, "/ad/images/${imgFileDTO.uploadFileName}.thumb.jpg")

                val dimensionAndType = imageServcie.getImageFileDimensionAndType(uploadFile)

                val uuid = UUID.randomUUID().toString().replace("-", "")

                val adImgFile = AdImageFile()
                adImgFile.adSet = adSet
                adImgFile.fileName = uuid
                adImgFile.description = ""
                adImgFile.duration = imgFileDTO.duration
                adImgFile.enabled = imgFileDTO.enabled
                adImgFile.fileType = dimensionAndType.type
                adImgFile.width = dimensionAndType.width
                adImgFile.height = dimensionAndType.height
                adImgFile.sequence = seq1++

                val file = File(assetsDir, "/ad/${adSet.id}/images/$uuid.${dimensionAndType.type}")
                val thumbFile = File(assetsDir, "/ad/${adSet.id}/images/$uuid.thumb.jpg")

                uploadFile.renameTo(file)
                uploadThumbFile.renameTo(thumbFile)

                adImageFileDao.save(adImgFile)
            }
        }

        //删除已经不存在的id对应的图片
        imgFileToDelete.forEach {
            val file = File(assetsDir, "/ad/${adSet.id}/images/${it.fileName}.${it.fileType}")
            val thumbFile = File(assetsDir, "/ad/${adSet.id}/images/${it.fileName}.thumb.jpg")

            file.delete()
            thumbFile.delete()

            adImageFileDao.delete(it.id)
        }
    }

    @Transactional
    override fun createAdSet(name: String, publicResource: Boolean, imgFiles: List<AdSetImageDTO>) {
        val isSuperAdmin = managerService.loginManagerHasRole("ROLE_SUPERADMIN")
        val loginManager = managerService.loginManager
        val now = Calendar.getInstance()

        val adSet = AdSet()
        adSet.name = name
        adSet.createTime = now
        adSet.updateTime = now
        adSet.companyId = if (publicResource && isSuperAdmin) 0 else loginManager!!.companyId
        adSet.imageFiles = ArrayList()

        adSetDao.save(adSet)

        saveAdImageFiles(imgFiles, adSet)
    }

    @Transactional
    override fun updateAdSet(id: Int, name: String, publicResource: Boolean, imgFiles: List<AdSetImageDTO>): Boolean {
        val isSuperAdmin = managerService.loginManagerHasRole("ROLE_SUPERADMIN")
        val loginManager = managerService.loginManager
        val adSet = adSetDao.findOne(id)
        return if (adSet == null) {
            false
        }
        else {
            adSet.name = name
            adSet.updateTime = Calendar.getInstance()
            adSet.companyId = if (publicResource && isSuperAdmin) 0 else adSet.companyId

            adSetDao.save(adSet)

            saveAdImageFiles(imgFiles, adSet)

            true
        }
    }

    override fun uploadAdSetImageFile(imageFile: MultipartFile?): Array<String>? {
        return if (imageFile != null) {
            val uuid = UUID.randomUUID().toString()
            val file = File(assetsDir, "/ad/images/$uuid")
            val thumbFile = File(assetsDir, "/ad/images/$uuid.thumb.jpg")
            file.parentFile.mkdirs()

            imageFile.transferTo(file)

            imageServcie.createThumbnailImageFile(file, "50x37^", thumbFile)

            arrayOf(uuid, "$baseUrl/assets/ad/images/${thumbFile.name}")
        }
        else {
            null
        }
    }
}