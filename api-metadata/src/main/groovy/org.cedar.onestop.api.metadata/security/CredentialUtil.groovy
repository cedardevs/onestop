package org.cedar.onestop.api.metadata.security

import org.opensaml.security.credential.Credential
import org.opensaml.security.x509.BasicX509Credential

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

    static void print() {
        println("\n" +
                "\nkeyStorePath: ${keyStorePath}" +
                "\nkeyStorePassword: ${keyStorePassword}" +
                "\nalias: ${alias}" +
                "\nkeyPassword: ${keyPassword}" +
                "\n")
    }

    static Credential buildCredential() {
        KeyStore keyStore = buildKeyStore(keyStorePath, keyStorePassword)
        KeyStore.PasswordProtection protectionParameter = new KeyStore.PasswordProtection(keyStorePassword.toCharArray())
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, protectionParameter)
        PrivateKey privateKey = privateKeyEntry.getPrivateKey()
        X509Certificate cert = (X509Certificate) privateKeyEntry.getCertificate()
        privateKeyEntry.getCertificate()
        BasicX509Credential credential = new BasicX509Credential(cert, privateKey)
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
