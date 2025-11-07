package com.example

import com.example.plugins.configureDatabases
import com.example.plugins.configureDependencyInjection
import com.example.plugins.configureRouting
import com.example.plugins.configureSecurity
import com.example.plugins.configureSerialization
import io.ktor.server.application.Application
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.netty.Netty
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.File
import java.security.KeyStore
import java.security.Security

fun main() {
    Security.addProvider(BouncyCastleProvider())
    val keyStoreFile = File("keystore.p12")
    val keyStorePassword = "GoldSPENDER".toCharArray()

    val keyStore = KeyStore.getInstance("PKCS12").apply {
        load(keyStoreFile.inputStream(), keyStorePassword)
    }

    val environmentHttps = applicationEngineEnvironment {
        sslConnector(
            keyStore = keyStore,
            keyAlias = "ktor",
            keyStorePassword = { keyStorePassword },
            privateKeyPassword = { keyStorePassword }
        ) {
            port = 8443
            keyStorePath = keyStoreFile
            host = "0.0.0.0"
        }
        module {
            module()
        }
    }

    embeddedServer(Netty, environmentHttps).start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureSecurity()
    val dataSource = configureDatabases()
    configureDependencyInjection()
    configureRouting(dataSource)
}
