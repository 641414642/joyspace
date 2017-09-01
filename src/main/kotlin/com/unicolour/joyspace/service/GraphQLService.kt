package com.unicolour.joyspace.service

import graphql.schema.GraphQLSchema

interface GraphQLService {
    fun getGraphQLSchema(): GraphQLSchema?
}