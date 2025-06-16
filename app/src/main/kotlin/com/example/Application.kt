package com.example

import com.example.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.File
import java.security.KeyStore
import java.security.Security

fun main() {

    Security.addProvider(BouncyCastleProvider())
    val keyStoreFile = File("keystore.p12")
    val keyStorePassword = "password".toCharArray()

    val keyStore = KeyStore.getInstance("PKCS12").apply {
        load(keyStoreFile.inputStream(), keyStorePassword)
    }

    val environment = applicationEngineEnvironment {
        sslConnector(
            keyStore = keyStore,
            keyAlias = "ktorLocal",
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

    embeddedServer(Netty, environment).start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureSecurity()
    val dataSource = configureDatabases()
    configureRouting(dataSource)
}
