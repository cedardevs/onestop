package org.cedar.onestop.api.metadata.security

import net.shibboleth.utilities.java.support.resolver.CriteriaSet
import net.shibboleth.utilities.java.support.resolver.Criterion
import net.shibboleth.utilities.java.support.resolver.ResolverException
import org.opensaml.core.criterion.EntityIdCriterion
import org.opensaml.security.credential.Credential
import org.opensaml.security.credential.impl.KeyStoreCredentialResolver

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.KeyStore

class SPCredentialsParam {
    private static String keyStorePath
    private static String keyStorePassword
    private static String alias
    private static String keyPassword

    static String getKeyStorePath() {
        return keyStorePath
    }

    static void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath
    }

    static String getKeyStorePassword() {
        return keyStorePassword
    }

    static void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword
    }

    static String getAlias() {
        return alias
    }

    static void setAlias(String alias) {
        this.alias = alias
    }

    static String getKeyPassword() {
        return keyPassword
    }

    static String setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword
    }

    static void print() {
        println("\n\nkeyStorePath: ${keyStorePath}\nkeyStorePassword: ${keyStorePassword}\nalias: ${alias}\nkeyPassword: ${keyPassword}")
    }
}

class SPCredentials {
//    private static final String KEY_STORE_PASSWORD = "password"
//    private static final String KEY_STORE_ENTRY_PASSWORD = "password"
//    private static final String KEY_STORE_PATH = "/SPKeystore.jks"
//    private static final String KEY_ENTRY_ID = "SPKey"


    private static final Credential credential

    static {

        keyStorePath = SPCredentialsParam.getKeyStorePath()
        keyStorePassword = SPCredentialsParam.getKeyStorePassword()
        alias = SPCredentialsParam.getAlias()
        keyPassword = SPCredentialsParam.getKeyPassword()

        println("SPCredential: SHOW ME DA MONEY!\n-------------------")
        println("keyStorePath: ${keyStorePath}, keyStorePassword: ${keyStorePassword}, alias: ${alias}, keyPassword: ${keyPassword}")

        try {
            KeyStore keystore = readKeystoreFromFile(keyStorePath, keyStorePassword)

            println("keystore: ${keystore.getCertificate(alias).toString()}")

            Map<String, String> passwordMap = new HashMap<String, String>()
            passwordMap.put(alias, keyPassword)
            KeyStoreCredentialResolver resolver = new KeyStoreCredentialResolver(keystore, passwordMap)

            println("resolver: ${resolver.getProperties().toString()}")

            Criterion criterion = new EntityIdCriterion("1")
            CriteriaSet criteriaSet = new CriteriaSet()
            criteriaSet.add(criterion)

            println("criteriaSet: ${criteriaSet.getProperties().toString()}")

            credential = resolver.resolveSingle(criteriaSet)

        } catch (ResolverException e) {
            throw new RuntimeException("Something went wrong reading credentials", e)
        }
    }

    private static keyStorePath
    private static keyStorePassword
    private static alias
    private static keyPassword

    private static KeyStore readKeystoreFromFile(String keyStore, String keyStorePassword) {
        try {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType())
//            InputStream inputStream = SPCredentials.class.getResourceAsStream(pathToKeyStore)
            Path keyStorePath = Paths.get(keyStore)
            InputStream inputStream = Files.newInputStream(keyStorePath)
            keystore.load(inputStream, keyStorePassword.toCharArray())
            inputStream.close()
            return keystore
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong reading keystore", e)
        }
    }

    static Credential getCredential() {
        return credential
    }
}
