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
                          var address: String? = null,
                          var longitude: Double? = null,
                          var latitude: Double? = null,
                          var wxQrCode: String? = null,
                          var positionId: String? = null,
                          var companyId: String? = null,
                          var status: Int? = null)

data class CouponVo(var id: Int? = null,
                    var name: String? = null,
                    var code: String? = null,
                    var begin: Date? = null,
                    var expire: Date? = null,
                    var minExpense: Int? = null,
                    var discount: Int? = null,
                    var available: Int? = null)

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

data class TemplateVo(var id: Int? = null,
                      var version: String? = null,
                      var name: String? = null,
                      var type: Int? = null,
                      var scenes: List<Scene>? = null)

data class Scene(var id: Int? = null,
                 var name: String? = null,
                 var type: String? = null,
                 var width: Double? = null,
                 var height: Double? = null,
                 var layers: List<Layer>? = null)

data class Layer(var id: Int? = null,
                 var type: String? = null,
                 var images: List<Img>? = null)

data class Img(var id: Int? = null,
               var type: String? = null,
               var x: Double? = null,
               var y: Double? = null,
               var width: Double? = null,
               var height: Double? = null,
               var angleClip: Double? = null,
               var bgcolor: String? = null,
               var resourceURL: String? = null)

data class OrderStatusVo(var orderItems: List<OrderItemS>? = null)
data class OrderItemS(var images: List<ImageS>? = null)
data class ImageS(var status:Int? = null)
