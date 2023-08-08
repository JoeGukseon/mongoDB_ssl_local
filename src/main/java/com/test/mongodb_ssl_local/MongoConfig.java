package com.test.mongodb_ssl_local;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {
    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${app.clientJksFilePath}")
    private String keyStorePath;

    @Value("${app.jksPassword}")
    private String keyStorePassword;

    @Value("${app.caPemFilePath}")
    private String trustStorePath;

    @Value("${app.rootUserName}")
    private String rootUserName;

    @Value("${app.rootPassword}")
    private String rootPassword;

    @Override
    public MongoClientSettings mongoClientSettings() {
        try {
            FileInputStream jksInputStream = new FileInputStream(keyStorePath);
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(jksInputStream, keyStorePassword.toCharArray());
            jksInputStream.close();

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());

            FileInputStream caInputStream = new FileInputStream(trustStorePath);
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate caCertificate = (X509Certificate) certificateFactory.generateCertificate(caInputStream);
            caInputStream.close();

            KeyStore caKeyStore = KeyStore.getInstance("JKS"); // CA 인증서를 JKS 형식으로 로딩
            caKeyStore.load(null, null);
            caKeyStore.setCertificateEntry("CA_test", caCertificate);
            caInputStream.close();

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(caKeyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            MongoCredential credential = MongoCredential.createCredential(rootUserName, "admin", rootPassword.toCharArray());
            return MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(mongoUri))
                    .applyToSslSettings(builder ->
                            builder.enabled(true)
                                    .context(sslContext))
                    .credential(credential)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure MongoDB SSL settings", e);
        }
    }

    @Override
    protected String getDatabaseName() {
        return "Chatting";
    }
}
