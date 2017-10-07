package com.unicolour.joyspace.controller.api

import com.unicolour.joyspace.service.GraphQLService
import graphql.GraphQL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*


@RestController
class AppUserController {

    @Autowired
    lateinit var graphQLService: GraphQLService

    @RequestMapping("/app/user/sendVerifyCode", method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun sendVerifyCode(@RequestParam("phoneNumber") phoneNumber: String) : Any? {
        val schema = graphQLService.getGraphQLSchema()
        val graphQL = GraphQL.newGraphQL(schema).build()

        val query =
"""
mutation {
	sendVerifyCode(phoneNumber:"$phoneNumber") {
		state: result
		msg: description
	}
}
"""
        val queryResult = graphQL.execute(query, null, null, emptyMap())
        val data:Map<String, Any> = queryResult.getData()
        return data["sendVerifyCode"]
    }

    @RequestMapping("/app/user/register", method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun registerUser(
            @RequestParam("nickName", required=false, defaultValue = "") nickName: String,
            @RequestParam("password") password: String,
            @RequestParam("phoneNumber") phoneNumber: String,
            @RequestParam("verifyCode") verifyCode: String,
            @RequestParam("email", required=false, defaultValue = "") email: String?
    ) : Any? {
        val schema = graphQLService.getGraphQLSchema()
        val graphQL = GraphQL.newGraphQL(schema).build()

        val query =
"""
mutation {
	userRegister(
            nickName: "$nickName",
            password: "$password",
            phoneNumber: "$phoneNumber",
            verifyCode: "$verifyCode",
            email: "$email")
    {
		state: result
		msg: description
	}
}
"""
        val queryResult = graphQL.execute(query, null, null, emptyMap())
        val data:Map<String, Any> = queryResult.getData()
        return data["userRegister"]
    }

    @RequestMapping("/app/user/resetPassword", method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun resetPassword(
            @RequestParam("userName", required = false) userName: String?,
            @RequestParam("phoneNumber", required = false) phoneNumber: String?,
            @RequestParam("newPassword", required = true) newPassword: String,
            @RequestParam("verifyCode", required = true) verifyCode: String) : Any? {
        val schema = graphQLService.getGraphQLSchema()
        val graphQL = GraphQL.newGraphQL(schema).build()

        val query =
"""
mutation {
	resetPassword(
            userName: "$userName",
            phoneNumber: "$phoneNumber",
            newPassword: "$newPassword",
            verifyCode: "$verifyCode")
    {
		state: result
		msg: description
	}
}
"""
        val queryResult = graphQL.execute(query, null, null, emptyMap())
        val data:Map<String, Any> = queryResult.getData()
        return data["resetPassword"]
    }

    @RequestMapping("/app/user/login", method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun userLogin(
            @RequestParam("nickName", required = false) nickName: String?,
            @RequestParam("phoneNumber", required = false) phoneNumber: String?,
            @RequestParam("password") password: String
    ) : Any? {
        val schema = graphQLService.getGraphQLSchema()
        val graphQL = GraphQL.newGraphQL(schema).build()

        val nickNameVal = nickName ?: ""
        val phoneNumberVal = phoneNumber ?: ""
        val query =
"""
mutation {
	login(
            nickName: "$nickNameVal",
            phoneNumber: "$phoneNumberVal",
            password: "$password")
    {
        state: result
        msg: description
        result: session {
            token: sessionId
            userinfo: userInfo {
                nickname: nickName
                avatar
                phone
                email
            }
        }
	}
}
"""
        val queryResult = graphQL.execute(query, null, null, emptyMap())
        val data:Map<String, Any> = queryResult.getData()
        return data["login"]
    }
}