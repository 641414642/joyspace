package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.model.*
import com.unicolour.joyspace.service.CompanyService
import com.unicolour.joyspace.service.TestService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import javax.persistence.EntityManager

@Service
class TestServiceImpl : TestService {
    @Autowired
    lateinit var em: EntityManager

    @Autowired
    lateinit var companyService: CompanyService

    @Autowired
    lateinit var companyDao: CompanyDao

    @Autowired
    lateinit var managerDao: ManagerDao

    @Autowired
    lateinit var positionDao: PositionDao

    @Autowired
    lateinit var printStationDao: PrintStationDao

    @Autowired
    lateinit var userDao: UserDao

    @Autowired
    lateinit var productDao: ProductDao

    @Autowired
    lateinit var priceListDao: PriceListDao

    @Autowired
    lateinit var priceListItemDao: PriceListItemDao

    @Autowired
    lateinit var printStationProductDao: PrintStationProductDao

    override fun clearOldTestDataAndCreateNewTestData() {
        if (!isTestDatabase()) {
            throw IllegalAccessException("不是测试数据库")
        }

        //清除旧数据
        printStationProductDao.deleteAll()
        printStationDao.deleteAll();
        userDao.deleteAll()
        positionDao.deleteAll()
        companyDao.findAll().toList().forEach { it.defaultPriceList = null; companyDao.save(it) }
        priceListItemDao.deleteAll()
        priceListDao.deleteAll()
        productDao.deleteAll()
        managerDao.deleteAll()
        companyDao.deleteAll()

        //创建新数据

        //投放商
        val company = companyService.createCompany("优利绚彩", null, "admin", "管理员", "", "", "123456");

        //用户
        val user = User()
        user.createTime = Calendar.getInstance()
        user.email = "zhangsan@sina.com"
        user.enabled = true
        user.fullName = "张三"
        user.phone = "13912345678"
        user.sex = USER_SEX_MALE
        user.userName = "zhangsan"
        user.wxOpenId = "ljAldSfg234lasW63dDdfsdf"
        userDao.save(user)

        //产品
        val product1 = Product()
        product1.defaultPrice = 500 //5元
        product1.enabled = true
        product1.minImageCount = 1
        product1.name = "五寸照片"
        product1.remark = "5寸彩色照片"
        product1.resolutionX = 5000
        product1.resolutionY = 3000
        product1.sn = "PHOTO_5"
        product1.type = ProductType.PHOTO.value
        productDao.save(product1)

        val product2 = Product()
        product2.defaultPrice = 600 //5元
        product2.enabled = true
        product2.minImageCount = 2
        product2.name = "六寸照片"
        product2.remark = "6寸彩色照片"
        product2.resolutionX = 6000
        product2.resolutionY = 4000
        product2.sn = "PHOTO_4"
        product2.type = ProductType.PHOTO.value
        productDao.save(product2)

        val product3 = Product()
        product3.defaultPrice = 700 //5元
        product3.enabled = true
        product3.minImageCount = 3
        product3.name = "七寸照片"
        product3.remark = "7寸彩色照片"
        product3.resolutionX = 7000
        product3.resolutionY = 5000
        product3.sn = "PHOTO_7"
        product3.type = ProductType.PASSPORT_PHOTO.value
        productDao.save(product3)

        //价格列表
        val priceListDef = PriceList()
        priceListDef.name = "缺省价目表"
        priceListDef.createTime = Calendar.getInstance()
        priceListDef.company = company
        priceListDao.save(priceListDef)

        val defListItem1 = PriceListItem()
        defListItem1.product = product1 //原价 500
        defListItem1.price = 400
        defListItem1.priceList = priceListDef
        priceListItemDao.save(defListItem1)

        val defListItem2 = PriceListItem()
        defListItem2.product = product2 //原价 600
        defListItem2.price = 500
        defListItem2.priceList = priceListDef
        priceListItemDao.save(defListItem2)

        val priceList618 = PriceList()
        priceList618.name = "618促销"
        priceList618.createTime = Calendar.getInstance()
        priceList618.company = company
        priceListDao.save(priceList618)

        val item1 = PriceListItem()
        item1.product = product1 //原价 500
        item1.price = 300
        item1.priceList = priceList618
        priceListItemDao.save(item1)

        company.defaultPriceList = priceListDef;
        companyDao.save(company)

        //添加投放位置
        val pos = Position()
        pos.company = company
        pos.address = "北京市西城区文津街1号"
        pos.name = "北海公园"
        pos.longitude = 116.39548
        pos.latitude = 39.932909
        pos.priceList = priceList618
        positionDao.save(pos)

        //自助机
        val ps = PrintStation()
        ps.company = company
        ps.position = pos
        ps.sn = "PS001"
        ps.wxQrCode = "https://mp.weixin.qq.com/a/~~wu3hXzBSt64~plUyoOB9Iyf8mEHP9BrkLA~~"
        printStationDao.save(ps)

        //自助机支持产品列表
        val psp1 = PrintStationProduct()
        psp1.printStation = ps
        psp1.product = product1
        printStationProductDao.save(psp1)

        val psp2 = PrintStationProduct()
        psp2.printStation = ps
        psp2.product = product2
        printStationProductDao.save(psp2)

        val psp3 = PrintStationProduct()
        psp3.printStation = ps
        psp3.product = product3
        printStationProductDao.save(psp3)
    }

    override fun isTestDatabase(): Boolean {
        val sql = "SELECT name FROM test_flag WHERE ID = 1"
        val query = em.createNativeQuery(sql)
        val name = query.singleResult as String
        return name == "TestDB"
    }

}