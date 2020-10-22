package org.cedar.onestop.gateway.config;

import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

class PrivateKeyJwtKeystore {

  private PrivateKey privateKey;
  private X509Certificate certificate;

  PrivateKeyJwtKeystore(String keyStore, String keyStorePassword, String keyAlias, String keyPassword, String keyStoreType) throws KeyStoreException, UnrecoverableEntryException, NoSuchAlgorithmException, IOException {

    File keyStoreFile = new File(keyStore);

    KeyStore.Builder jksBuilder = KeyStore.Builder.newInstance(
        keyStoreType,
        null,
        keyStoreFile,
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

//public class ClientAuthenticationPrivateKeyJwtKeyStore {
//
//  private PrivateKey privateKey;
//  private X509Certificate certificate;
//
//  public ClientAuthenticationPrivateKeyJwtKeyStore(String keyStore, String keyStorePassword, String keyAlias, String keyPassword, String keyStoreType) throws KeyStoreException, NoSuchAlgorithmException, IOException, CertificateException, UnrecoverableEntryException {
//
//    KeyStore jks = KeyStore.getInstance(keyStoreType);
//    InputStream keyStoreStream = new FileInputStream(keyStore);
//    jks.load(keyStoreStream, keyStorePassword.toCharArray());
//
//    KeyStore.ProtectionParameter protectionParameter = null;
//    if(keyPassword != null) {
//      protectionParameter = new KeyStore.PasswordProtection(keyPassword.toCharArray());
//    }
//    KeyStore.Entry entry = jks.getEntry(keyAlias, protectionParameter);
//    KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) entry;
//
//    privateKey = privateKeyEntry.getPrivateKey();
//    certificate = (X509Certificate)(privateKeyEntry.getCertificate());
//  }
//
//  RSAPrivateKey rsaPrivateKey() {
//    return (RSAPrivateKey) privateKey;
//  }
//
//  RSAPublicKey rsaPublicKey() {
//    return (RSAPublicKey)(certificate.getPublicKey());
//  }
//
//  String base64PrivateKey() {
//    return printBase64(privateKey.getEncoded());
//  }
//
//  String base64PublicKey() throws CertificateEncodingException {
//    return printBase64(certificate.getEncoded());
//  }
//
//  static String printBase64(byte[] byteArray) {
//    return Base64.getEncoder().encodeToString(byteArray);
//  }
//
//}

/// NEWEST
//import java.io.FileInputStream;
//    import java.io.IOException;
//    import java.security.*;
//    import java.security.cert.Certificate;
//    import java.security.cert.CertificateException;
//
//public class ClientAuthenticationPrivateKeyJwtKeyPair {
//
//  private KeyPair keyPair;
//
//  public ClientAuthenticationPrivateKeyJwtKeyPair(String keyStorePath, String keyStorePassword, String keyAlias, String keyPassword, String keyStoreType) throws KeyStoreException, NoSuchAlgorithmException, IOException, CertificateException, UnrecoverableEntryException {
//
//    String type = KeyStore.getDefaultType();
//    if (keyStoreType != null) {
//      type = keyStoreType;
//    }
//    KeyStore keyStore = KeyStore.getInstance(type);
//
//    FileInputStream keyStoreStream = new FileInputStream(keyStorePath);
//    keyStore.load(keyStoreStream, keyStorePassword.toCharArray());
//
//    char[] password = null;
//    if (keyPassword != null) {
//      password = keyPassword.toCharArray();
//    }
//    Key key = keyStore.getKey(keyAlias, password);
//
//    if (key instanceof PrivateKey) {
//      Certificate certificate = keyStore.getCertificate(keyAlias);
//      PublicKey publicKey = certificate.getPublicKey();
//      this.keyPair = new KeyPair(publicKey, (PrivateKey) key);
//    }
//  }
//
//  public KeyPair getKeyPair() {
//    return keyPair;
//  }
//
//}
