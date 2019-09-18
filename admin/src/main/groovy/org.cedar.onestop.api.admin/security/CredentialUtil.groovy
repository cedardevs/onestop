package org.cedar.onestop.api.admin.security

import org.opensaml.security.credential.Credential
import org.opensaml.security.x509.BasicX509Credential

import javax.xml.bind.DatatypeConverter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate

class CredentialUtil {

    static String keyStorePath
    static String keyStorePassword
    static String alias
    static String keyPassword

    static String x509Certificate

    static Credential credential

    static String info() {
        return "\nCredentialUtil {" +
                "\n\tkeyStorePath: ${keyStorePath}" +
                "\n\tkeyStorePassword: ${keyStorePassword}" +
                "\n\talias: ${alias}" +
                "\n\tkeyPassword: ${keyPassword}" +
                "\n}\n"
    }

    static Credential buildCredential() {
        KeyStore keyStore = buildKeyStore(keyStorePath, keyStorePassword)
        KeyStore.PasswordProtection protectionParameter = new KeyStore.PasswordProtection(keyStorePassword.toCharArray())
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, protectionParameter)
        PrivateKey privateKey = privateKeyEntry.getPrivateKey()
        X509Certificate cert = (X509Certificate) privateKeyEntry.getCertificate()

        // save for later so that we can add the proper encoded public x509 cert into AuthnRequest's KeyInfo section
        x509Certificate = DatatypeConverter.printBase64Binary(cert.getEncoded())

        BasicX509Credential credential = new BasicX509Credential(cert, privateKey)

        this.credential = credential

        return credential
    }

    private static KeyStore buildKeyStore(String keyStoreConfig, String keyStorePassword) {
        Path keyStorePath = Paths.get(keyStoreConfig)
        KeyStore keyStore = KeyStore.getInstance("JKS")
        InputStream keyStoreStream = Files.newInputStream(keyStorePath)
        keyStore.load(keyStoreStream, keyStorePassword.toCharArray())
        return keyStore
    }
}
