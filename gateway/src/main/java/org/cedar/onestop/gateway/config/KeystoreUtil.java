package org.cedar.onestop.gateway.config;

import java.io.File;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

class KeystoreUtil {

  private PrivateKey privateKey;
  private X509Certificate certificate;

  KeystoreUtil(String keyStore, String keyStorePassword, String keyAlias, String keyPassword, String keyStoreType) throws KeyStoreException, UnrecoverableEntryException, NoSuchAlgorithmException {
    KeyStore.Builder jksBuilder = KeyStore.Builder.newInstance(
        keyStoreType,
        null,
        new File(keyStore),
        new KeyStore.PasswordProtection(keyStorePassword.toCharArray())
    );
    KeyStore jks = jksBuilder.getKeyStore();
    KeyStore.ProtectionParameter protectionParameter = jksBuilder.getProtectionParameter(keyAlias);
    KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) (jks.getEntry(keyAlias, protectionParameter));
        privateKey = privateKeyEntry.getPrivateKey();
    certificate = (X509Certificate)(privateKeyEntry.getCertificate());
  }

  String printBase64(byte[] byteArray) {
    return Base64.getEncoder().encodeToString(byteArray);
  }

  RSAPrivateKey rsaPrivateKey() {
    return (RSAPrivateKey) privateKey;
  }

  RSAPublicKey rsaPublicKey() {
    return (RSAPublicKey)(certificate.getPublicKey());
  }

  String base64PrivateKey() {
    return printBase64(privateKey.getEncoded());
  }

  String base64PublicKey() throws CertificateEncodingException {
    return printBase64(certificate.getEncoded());
  }
}
