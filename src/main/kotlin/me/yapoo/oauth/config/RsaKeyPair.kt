package me.yapoo.oauth.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

@ConstructorBinding
@ConfigurationProperties(prefix = "key-pair")
data class RsaKeyPair(
    private val privateKey: String,
    private val publicKey: String
) {
    val public: RSAPublicKey = KeyFactory.getInstance("RSA")
        .generatePublic(X509EncodedKeySpec(Base64.getDecoder().decode(publicKey))) as RSAPublicKey
    val private: RSAPrivateKey = KeyFactory.getInstance("RSA")
        .generatePrivate(PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey))) as RSAPrivateKey
}
