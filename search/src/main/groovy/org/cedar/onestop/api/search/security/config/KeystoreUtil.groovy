package org.cedar.onestop.api.search.security.config

import javax.xml.bind.DatatypeConverter
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

class KeystoreUtil {

    private KeyStore.Builder jksBuilder
    private KeyStore jks
    private KeyStore.ProtectionParameter protectionParameter
    private KeyStore.PrivateKeyEntry privateKeyEntry
    private PrivateKey privateKey
    private X509Certificate certificate

    KeystoreUtil(String keyStore, String keyStorePassword, String keyAlias, String keyPassword, String keyStoreType) {
        jksBuilder = KeyStore.Builder.newInstance(
                keyStoreType,
                null,
                new File(keyStore),
                new KeyStore.PasswordProtection(keyStorePassword.toCharArray())
        )
        jks = jksBuilder.getKeyStore()
        protectionParameter = jksBuilder.getProtectionParameter(keyAlias)
        privateKeyEntry = jks.getEntry(keyAlias, protectionParameter) as KeyStore.PrivateKeyEntry
        privateKey = privateKeyEntry.privateKey
        certificate = privateKeyEntry.certificate as X509Certificate
    }

    String printBase64(byte[] byteArray) {
        return DatatypeConverter.printBase64Binary(byteArray)
    }

    RSAPrivateKey rsaPrivateKey() {
        return privateKey as RSAPrivateKey
    }

    RSAPublicKey rsaPublicKey() {
        return certificate.publicKey as RSAPublicKey
    }

    String base64PrivateKey() {
        return printBase64(privateKey.encoded)
    }

    String base64PublicKey() {
        return printBase64(certificate.encoded)
    }
}
