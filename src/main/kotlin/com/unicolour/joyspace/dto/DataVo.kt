package com.unicolour.joyspace.dto

import java.util.*

data class HomePageVo(var advert: List<Advert>? = null,
                      var productTypes: List<ProductType>? = null)

data class Advert(var name: String? = null,
                  var describe: String? = null,
                  var contentUrl: String? = null)

data class ProductType(var id: Int? = null,
                       var name: String? = null,
                       var describe: String? = null,
                       var iconUrl: String? = null)

data class ProductVo(var id: Int? = null,
                     var name: String? = null,
                     var type: Int? = null,
                     var version: Int? = null,
                     var templateWidth: Double? = null,
                     var templateHeight: Double? = null,
                     var displaySize: String? = null,//12.0f x 44.0f mm"
                     var idPhotoMaskImageUrl: String? = null,
                     var imageRequired: Int? = null,//miniImageCount
                     var remark: String? = null,//备注
                     var price: Int? = null,//默认价格
                     var thumbnailImageUrl: String? = null,
                     var previewImageUrls: List<String>? = null,
                     var templateImages: List<TemplateImage>? = null,
                     var templateUrl: String? = null)

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
                    var isValid:Int? = null)

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

data class UserInfoVo(var nickName:String? = null,
                    var imageUrl:String? = null,
                    var unPayCount:Int? = null,
                    var handlingCount:Int? = null)