package com.test.mongodb_ssl_local;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

@SpringBootApplication
public class MongoDbSslLocalTestApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication app = new SpringApplication(MongoDbSslLocalTestApplication.class);
        app.run(args);

        String connectionString = AppConfig.connectionString; // 로컬 MongoDB 주소와 포트
        String clientJksFilePath = AppConfig.clientJksFilePath; // 클라이언트 JKS 파일 경로
        char[] jksPassword = AppConfig.jksPassword.toCharArray(); // JKS 파일 비밀번호
        String caPemFilePath = AppConfig.caPemFilePath; // CA 인증서 파일 경로

        // JKS 파일 로딩
        assert clientJksFilePath != null;
        FileInputStream jksInputStream = new FileInputStream(clientJksFilePath);
        KeyStore jksKeyStore = KeyStore.getInstance("JKS");
        jksKeyStore.load(jksInputStream, jksPassword);
        jksInputStream.close();

        // KeyManagerFactory 초기화
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(jksKeyStore, jksPassword);
//
        // CA 인증서 로딩
        assert caPemFilePath != null;
        FileInputStream caInputStream = new FileInputStream(caPemFilePath);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate caCertificate = (X509Certificate) certificateFactory.generateCertificate(caInputStream);
        caInputStream.close();

        KeyStore caKeyStore = KeyStore.getInstance("JKS"); // CA 인증서를 JKS 형식으로 로딩
        caKeyStore.load(null, null);
        caKeyStore.setCertificateEntry("CA_test", caCertificate);
        caInputStream.close();
//
        // TrustManagerFactory 초기화
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(caKeyStore);


        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        String rootUserName = AppConfig.rootUserName;           // MongoDB의 root 사용자 이름
        char[] rootPassword = AppConfig.rootPassword.toCharArray();
        // TLS 설정 적용
//        MongoCredential credential = MongoCredential.createCredential(rootUserName, "admin", rootPassword);
        assert connectionString != null;
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .applyToSslSettings(builder ->
                        builder.enabled(true)
                                .context(sslContext))
                .build();

        try (MongoClient mongoClient = MongoClients.create(settings)) {
            System.out.println("Connected to MongoDB over TLS/SSL!");
        } catch (Exception e) {
            System.err.println("Error while connecting to MongoDB: " + e.getMessage());
        }

//        SpringApplication.run(MongoDbSslLocalTestApplication.class, args);
    }

}
