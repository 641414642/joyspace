package com.unicolour.joyspace.dto

import java.util.*

data class HomePageVo(var advert: List<Advert>? = null,
                      var productTypes: List<ProductType>? = null)

data class Advert(var name: String? = null,
                  var describe: String? = null,
                  var href:String? = null,
                  var contentUrl: String? = null)

data class ProductType(var id: Int? = null,
                       var name: String? = null,
                       var describe: String? = null,
                       var iconUrl: String? = null)

data class ProductVo(var id: Int? = null,
                     var name: String? = null,
                     var width: Double? = null,
                     var height: Double? = null,
                     var type:Int? = null,
                     var typeStr:String? = null,
                     var version: Int? = null,
                     var displaySize: String? = null,//12.0f x 44.0f mm"
                     var imageRequired: Int? = null,//miniImageCount
                     var remark: String? = null,//备注
                     var price: Int? = null,//默认价格
                     var thumbnailImageUrl: String? = null)

data class TemplateImage(var name: String? = null,
                         var x: Double? = null,
                         var y: Double? = null,
                         var width: Double? = null,
                         var height: Double? = null,
                         var url: String? = null)

data class PrintStationVo(var id: Int? = null,
                          var name:String? = null,
                          var address: String? = null,
                          var longitude: Double? = null,
                          var latitude: Double? = null,
                          var wxQrCode: String? = null,
                          var positionId: String? = null,
                          var companyId: String? = null,
                          var status: Int? = null,
                          var products: MutableList<PrintStationProduct>? = null,
                          var imgUrl: String? = null)

data class PrintStationProduct(var id: Int? = null,
                               var name: String? = null,
                               var type: String? = null,
                               var price: Int? = null)

data class CouponVo(var id: Int? = null,
                    var name: String? = null,
                    var code: String? = null,
                    var begin: Date? = null,
                    var expire: Date? = null,
                    var minExpense: Int? = null,
                    var discount: Int? = null,
                    var available: Int? = null)

data class OrderSimpleVo(var orderId: Int? = null,
                         var orderNo: String? = null,
                         var totalFee: Int? = null,
                         var discount: Int? = null,
                         var postage: Int? = null,
                         var creatTime:Calendar? = null,
                         var status:Int? = null,
                         var updateTime:Calendar? = null,
                         var name:String? = null,//产品类型+产品名称
                         var count:Int? = null,//该笔订单的张数
                         var productType:Int? = null,
                         var productTypeStr:String? = null,
                         var productImgUrl:String? = null,
                         var printType:Int? = null,//打印方式：0：现场取片；1：邮寄配送
                         var printStation: PrintStationVo? = null,
                         var coupon: CouponVo? = null,
                         var address: AddressVo? = null)

data class AddressVo(var province: String? = null,
                     var city: String? = null,
                     var area: String? = null,
                     var address: String? = null,
                     var phoneNum: String? = null,
                     var name: String? = null

)


data class OrderVo(var orderId: Int? = null,
                   var orderNo: String? = null,
                   var wxPayParams: WxPayParam? = null,
                   var totalFee: Int? = null,
                   var discount: Int? = null,
                   var postage: Int? = null,
                   var orderItems: List<OrderItem>? = null)

data class WxPayParam(var timeStamp: String? = null,
                      var nonceStr: String? = null,
                      var pkg: String? = null,
                      var paySign: String? = null)

data class OrderItem(var id: Int? = null,
                     var productId: Int? = null)


data class NoticeVo(var title: String? = null,
                    var context: String? = null,
                    var sendTime: Date? = null,
                    var imageUrl: String? = null)

data class UserInfoVo(var nickName: String? = null,
                      var imageUrl: String? = null,
                      var unPayCount: Int? = null,
                      var handlingCount: Int? = null)

data class TemplateVo(var id: Int = 0,
                      var version: Int = 0,
                      var name: String = "",
                      var type: Int = 0,
                      var scenes: List<Scene> = emptyList())

data class Scene(var id: Int = 0,
                 var name: String = "",
                 var type: String = "",
                 var width: Double = 0.0,
                 var height: Double = 0.0,
                 var layers: List<Layer> = emptyList())

data class Layer(var id: Int = 0,
                 var type: String = "",
                 var images: List<Img> = emptyList())

data class Img(var id: Int = 0,
               var type: String = "",
               var x: Double = 0.0,
               var y: Double = 0.0,
               var width: Double = 0.0,
               var height: Double = 0.0,
               var angleClip: Double = 0.0,
               var bgcolor: String = "",
               var resourceURL: String = "")

data class OrderStatusVo(var orderItems: List<OrderItemS>? = null)
data class OrderItemS(var images: List<ImageS>? = null)
data class ImageS(var status:Int? = null)
