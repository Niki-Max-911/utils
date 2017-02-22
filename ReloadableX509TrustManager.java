package com.greenhouse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;


/**
 * https://jcalcote.wordpress.com/2010/06/22/managing-a-dynamic-java-trust-store/
 * Created by niki.max on 22.02.2017.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReloadableX509TrustManager {

    private static final String LOCAL_KEY_SORE_PATH = "javax.net.ssl.trustStore";

    private X509TrustManager trustManager;

    private List<Certificate> dynamicallyLoadedCerts = new ArrayList<>();

    @SneakyThrows
    private void configureSSLContextAsGlobal() {
        TrustManager[] trustManagers = {trustManager};
//        SSLContext sc = SSLContext.getInstance("SSL");
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustManagers, new java.security.SecureRandom());
        SSLContext.setDefault(sc);//set global ssl context
    }

    @PostConstruct
    @SneakyThrows
    private void reloadTrustManager() {
        // load local keystore
        KeyStore keyStore = getLocalKeyStore();

        // add all dynamically certs to KeyStore
        dynamicallyLoadedCerts.forEach(cert -> {
                    try {
                        keyStore.setCertificateEntry(UUID.randomUUID().toString(), cert);
                    } catch (KeyStoreException e) {
                        e.printStackTrace();
                    }
                }//set random cert name
        );

        // initialize a new TMF with the keyStore we just loaded
        TrustManagerFactory trustManagerFactory
                = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);

        // cast X509 trust manager from factory
        trustManager = Stream.of(trustManagerFactory.getTrustManagers())
                .filter(trm -> trm instanceof X509TrustManager)
                .map(trm -> (X509TrustManager) trm)
                .findFirst()
                .orElseThrow(() -> new NoSuchAlgorithmException("No X509TrustManager in TrustManagerFactory"));
    }

    public static void addServerCertAndReload(String certLocation) {
        ReloadableX509TrustManager reloadableX509TrustManager = new ReloadableX509TrustManager();
        Certificate certificate = readCertificate(certLocation);
        reloadableX509TrustManager.dynamicallyLoadedCerts.add(certificate);
        reloadableX509TrustManager.reloadTrustManager();
        reloadableX509TrustManager.configureSSLContextAsGlobal();
    }

    @SneakyThrows
    private static KeyStore getLocalKeyStore() {
        String keyStorePath = System.getProperty("java.home") + "\\lib\\security\\cacerts";
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (InputStream in = new FileInputStream(keyStorePath)) {
            keyStore.load(in, null);
        }
        return keyStore;
    }

    @SneakyThrows
    private static Certificate readCertificate(String location) {
        try (InputStream stream = ReloadableX509TrustManager.class.getClassLoader().getResourceAsStream(location)) {
            return CertificateFactory.getInstance("X.509").generateCertificate(stream);
        }
    }
}
