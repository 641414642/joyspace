package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.dto.CommonRequestResult
import com.unicolour.joyspace.dto.GraphQLRequestResult
import com.unicolour.joyspace.dto.OrderInput
import com.unicolour.joyspace.dto.ResultCode
import com.unicolour.joyspace.model.PrintOrder
import com.unicolour.joyspace.model.PrintOrderItem
import com.unicolour.joyspace.model.PrintOrderState
import com.unicolour.joyspace.model.UserImageFile
import com.unicolour.joyspace.service.PrintOrderService
import graphql.schema.DataFetcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigInteger
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.*
import javax.transaction.Transactional

@Service
open class PrintOrderServiceImpl : PrintOrderService {
    @Autowired
    lateinit var printStationDao: PrintStationDao

    @Autowired
    lateinit var userLoginSessionDao: UserLoginSessionDao

    @Autowired
    lateinit var printOrderDao: PrintOrderDao

    @Autowired
    lateinit var userDao: UserDao

    @Autowired
    lateinit var printOrderItemDao: PrintOrderItemDao

    @Autowired
    lateinit var userImageFileDao: UserImageFileDao

    @Autowired
    lateinit var productDao: ProductDao

    @Autowired
    lateinit var secureRandom: SecureRandom

    @Value("\${com.unicolour.wxAppId}")
    lateinit var wxAppId: String

    @Value("\${com.unicolour.wxMchId}")  //商户号
    lateinit var wxMchId: String

    @Transactional
    override fun createOrder(orderInput: OrderInput): CommonRequestResult {
        val session = userLoginSessionDao.findOne(orderInput.sessionId)

        if (session == null) {
            return CommonRequestResult(1, "用户未登录")
        }

        val printStation = printStationDao.findOne(orderInput.printStationId)

        if (printStation == null) {
            return CommonRequestResult(2, "没有找到指定的自助机")
        }

        val order = PrintOrder()
        order.orderNo = createOrderNo()
        order.companyId = printStation.companyId
        order.createTime = Calendar.getInstance()
        order.updateTime = order.createTime
        order.printStationId = orderInput.printStationId
        order.userId = session.userId
        order.state = PrintOrderState.CREATED.value

        printOrderDao.save(order)

        for (orderItem in orderInput.orderItems) {
            val newOrderItem = PrintOrderItem()
            newOrderItem.copies = orderItem.copies
            newOrderItem.printOrder = order
            newOrderItem.product = productDao.findOne(orderItem.productId)
            newOrderItem.userImageFile = userImageFileDao.findOne(orderItem.imageFileId)

            printOrderItemDao.save(newOrderItem)
        }

        return CommonRequestResult()
    }

    private fun createOrderNo(): String {
        val dateTime = SimpleDateFormat("yyyyMMdd").format(Date())
        val randomStr = BigInteger(4 * 8, secureRandom).toString(36).toUpperCase()
        var orderNo:String
        do {
            orderNo = "$dateTime-$randomStr"
        } while (printOrderDao.existsByOrderNo(orderNo))

        return orderNo
    }

    override fun getImageFilesDataFetcher(): DataFetcher<Array<UserImageFile>> {
        return DataFetcher { env ->
            val printOrderItem = env.getSource<PrintOrderItem>()
            arrayOf(printOrderItem.userImageFile)
        }
    }

    override fun getPrintOrderDataFetcher(): DataFetcher<PrintOrder> {
        return DataFetcher { env ->
            val printStationId = env.getArgument<Int>("printStationId")
            val idAfter = env.getArgument<Int>("idAfter")

            printOrderDao.findFirstByPrintStationIdAndIdAfter(printStationId, idAfter)
        }
    }

    override fun startPayment(orderId: Int) {
        val order = printOrderDao.findOne(orderId)
        val user = userDao.findOne(order.userId)

        var nonceStr = BigInteger(32 * 8, secureRandom).toString(36).toUpperCase()
        if (nonceStr.length > 32) {
            nonceStr = nonceStr.substring(0, 32)
        }
        val notifyUrl = "https://joyspace.uni-colour.com/wxpay/notify"
        val ipAddress = java.net.InetAddress.getByName("joyspace.uni-colour.com")

        val totalFee = 1   //XXX
        val sign = ""

        val requestParams =
                "<xml>" +
                "   <appid>$wxAppId</appid>" +
                "   <attach>支付测试</attach>" +
                "   <body>优利绚彩-照片打印</body>" +
                "   <mch_id>$wxMchId</mch_id>" +
                "   <nonce_str>$nonceStr</nonce_str>" +
                "   <notify_url>$notifyUrl</notify_url>" +
                "   <openid>${user.wxOpenId}</openid>" +
                "   <out_trade_no>${order.orderNo}</out_trade_no>" +
                "   <spbill_create_ip>${ipAddress.hostAddress}</spbill_create_ip>" +
                "   <total_fee>$totalFee</total_fee>" +
                "   <trade_type>JSAPI</trade_type>" +
                "   <sign>$sign</sign>" +
                "</xml>"
    }

    override fun getUpdateOrderStateDataFetcher(state: PrintOrderState): DataFetcher<GraphQLRequestResult> {
        return DataFetcher { env ->
            val printStationId = env.getArgument<Int>("printStationId")
            val printOrderId = env.getArgument<Int>("printOrderId")

            val order = printOrderDao.findOne(printOrderId)
            if (order != null) {
                order.state = state.value
                printOrderDao.save(order)
                GraphQLRequestResult(ResultCode.SUCCESS)
            }
            else {
                GraphQLRequestResult(ResultCode.PRINTER_ORDER_NOT_FOUND)
            }
        }
    }
}
