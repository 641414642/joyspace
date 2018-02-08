package com.unicolour.joyspace.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

//逆地址解析(坐标位置描述)，返回结果
@JsonIgnoreProperties(ignoreUnknown = true)
data class QQMapGeoDecodeResult(
        var status: Int = 0,
        var message: String = "",
        var result: GeoDecodeResultData? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeoDecodeResultData(
        var address_component: AddressComponent? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AddressComponent(
        var nation: String = "",
        var province: String = "",
        var city: String = "",
        var district: String = "",
        var street: String = "",
        var street_number: String = ""
)

/*
{
    "status": 0,
    "message": "query ok",
    "request_id": "03879a20-0ce7-11e8-940f-6c0b84ffcd2a",
    "result": {
        "location": {
            "lat": 39.992789,
            "lng": 116.396542
        },
        "address": "北京市朝阳区北辰东路15号",
        "formatted_addresses": {
            "recommend": "朝阳区国家体育场",
            "rough": "朝阳区国家体育场"
        },
        "address_component": {
            "nation": "中国",
            "province": "北京市",
            "city": "北京市",
            "district": "朝阳区",
            "street": "北辰东路",
            "street_number": "北辰东路15号"
        },
        "ad_info": {
            "nation_code": "156",
            "adcode": "110105",
            "city_code": "156110000",
            "name": "中国,北京市,北京市,朝阳区",
            "location": {
                "lat": 39.99279,
                "lng": 116.396545
            },
            "nation": "中国",
            "province": "北京市",
            "city": "北京市",
            "district": "朝阳区"
        },
        "address_reference": {
            "business_area": {
                "title": "奥运村",
                "location": {
                    "lat": 39.994862,
                    "lng": 116.392723
                },
                "_distance": 0,
                "_dir_desc": "内"
            },
            "famous_area": {
                "title": "奥运村",
                "location": {
                    "lat": 39.994862,
                    "lng": 116.392723
                },
                "_distance": 0,
                "_dir_desc": "内"
            },
            "crossroad": {
                "title": "湖景东路/国家体育场南路(路口)",
                "location": {
                    "lat": 39.989841,
                    "lng": 116.39856
                },
                "_distance": 365,
                "_dir_desc": "西北"
            },
            "town": {
                "title": "奥运村街道",
                "location": {
                    "lat": 39.99279,
                    "lng": 116.396545
                },
                "_distance": 0,
                "_dir_desc": "内"
            },
            "street_number": {
                "title": "北辰东路15号",
                "location": {
                    "lat": 39.99493,
                    "lng": 116.392715
                },
                "_distance": 7.7,
                "_dir_desc": ""
            },
            "street": {
                "title": "湖景东路",
                "location": {
                    "lat": 39.993332,
                    "lng": 116.399231
                },
                "_distance": 230.7,
                "_dir_desc": "西"
            },
            "landmark_l2": {
                "title": "国家体育场",
                "location": {
                    "lat": 39.992722,
                    "lng": 116.396599
                },
                "_distance": 0,
                "_dir_desc": "内"
            }
        }
    }
}
*/