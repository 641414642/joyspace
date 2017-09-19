package com.unicolour.joyspace.controller.api

import com.unicolour.joyspace.service.GraphQLService
import graphql.GraphQL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*


@RestController
class AppUserController {

    @Autowired
    lateinit var graphQLService: GraphQLService

    @RequestMapping("/app/user/sendRegVerifyCode", method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun sendRegVerifyCode(@RequestParam("phoneNumber") phoneNumber: String) : Any? {
        val schema = graphQLService.getGraphQLSchema()
        val graphQL = GraphQL.newGraphQL(schema).build()

        val query =
"""
mutation {
	sendRegVerifyCode(phoneNumber:"$phoneNumber") {
		state: result
		msg: description
	}
}
"""
        val queryResult = graphQL.execute(query, null, null, emptyMap())
        val data:Map<String, Any> = queryResult.getData()
        return data["sendRegVerifyCode"]
    }

    @RequestMapping("/app/user/requestResetPassword", method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun requestResetPassword(@RequestParam("phoneNumber") phoneNumber: String) : Any? {
        val schema = graphQLService.getGraphQLSchema()
        val graphQL = GraphQL.newGraphQL(schema).build()

        val query =
"""
mutation {
	requestResetPassword(phoneNumber:"$phoneNumber") {
		state: result
		msg: description
	}
}
"""
        val queryResult = graphQL.execute(query, null, null, emptyMap())
        val data:Map<String, Any> = queryResult.getData()
        return data["requestResetPassword"]
    }

    @RequestMapping("/app/user/register", method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun registerUser(
            @RequestParam("userName") userName: String,
            @RequestParam("password") password: String,
            @RequestParam("phoneNumber") phoneNumber: String,
            @RequestParam("verifyCode") verifyCode: String,
            @RequestParam("email") email: String?
    ) : Any? {
        val schema = graphQLService.getGraphQLSchema()
        val graphQL = GraphQL.newGraphQL(schema).build()

        val query =
"""
mutation {
	userRegister(
            userName: "$userName",
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
            @RequestParam("userName", required = false) userName: String?,
            @RequestParam("phoneNumber", required = false) phoneNumber: String?,
            @RequestParam("password") password: String
    ) : Any? {
        val schema = graphQLService.getGraphQLSchema()
        val graphQL = GraphQL.newGraphQL(schema).build()

        val userNameVal = userName ?: ""
        val phoneNumberVal = phoneNumber ?: ""
        val query =
"""
mutation {
	login(
            userName: "$userNameVal",
            phoneNumber: "$phoneNumberVal",
            password: "$password")
    {
        state: result
        msg: description
        result: session {
            token: sessionId
            userInfo {
                nickname: userName
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