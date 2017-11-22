package com.unicolour.joyspace.controller.api

import com.unicolour.joyspace.dao.UserDao
import com.unicolour.joyspace.dto.WxLoginResult
import com.unicolour.joyspace.service.GraphQLService
import com.unicolour.joyspace.service.UserService
import graphql.GraphQL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*


@RestController
class ApiUserController {
    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var graphQLService: GraphQLService

    @RequestMapping("/api/user/login", method = arrayOf(RequestMethod.POST))
    fun wxUserLogin(@RequestParam("code") code: String) : ResponseEntity<WxLoginResult> {
        val result = userService.wxLogin(code)
        return ResponseEntity.ok(result)
    }

    //recordUserInfo(nickName: String, avatarUrl: String, gender: Int, province: String, city: String, country: String) : RequestResult!
    @RequestMapping("/api/user/reg", method = arrayOf(RequestMethod.POST))
    fun recordUserInfo(
            @RequestParam("sessionId") sessionId: String,
            @RequestParam("nickName") nickName : String?,
            @RequestParam("avatarUrl") avatarUrl: String?,
            @RequestParam("gender") gender: Int?,
            @RequestParam("province") province: String?,
            @RequestParam("language") language: String?,
            @RequestParam("city") city: String?,
            @RequestParam("country") country: String?) : Any? {

        val schema = graphQLService.getGraphQLSchema()
        val graphQL = GraphQL.newGraphQL(schema).build()

        val nickNameStr = if (nickName == null) "null" else "\"$nickName\""
        val avatarUrlStr = if (avatarUrl == null) "null" else "\"$avatarUrl\""
        val languageStr = if (language == null) "null" else "\"$language\""
        val genderVal = gender ?: 0
        val provinceStr = if (province == null) "null" else "\"$province\""
        val cityStr = if (city == null) "null" else "\"$city\""
        val countryStr = if (country == null) "null" else "\"$country\""

        val query =
                """
mutation {
	recordUserInfo(sessionId:"$sessionId",
                   nickName: $nickNameStr,
                   avatarUrl: $avatarUrlStr,
                   language: $languageStr,
                   province: $provinceStr,
                   city: $cityStr,
                   country: $countryStr,
                   gender: $genderVal) {
        errcode:result
        errmsg:description
	}
}
"""
        val context = HashMap<String, Any>()

        val queryResult = graphQL.execute(query, null, context, emptyMap())
        val data:Map<String, Any> = queryResult.getData()
        return data["userCouponList"]
    }

}
