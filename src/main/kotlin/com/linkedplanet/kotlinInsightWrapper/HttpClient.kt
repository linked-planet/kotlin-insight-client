package com.linkedplanet.kotlinInsightWrapper

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.auth.basic.*
import io.ktor.client.features.json.*

fun httpClient(username: String, password: String) =
    HttpClient(Apache) {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
        install(BasicAuth) {
            this.username = username
            this.password = password
        }
    }