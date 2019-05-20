import org.apache.http.client.fluent.Request;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import util.CertUtil;

import java.io.IOException;
import java.security.Security;

class PerformanceTest {

    private static final String TEST_HOST = "www.sseu.ru";
    private static final String HTTPS_TEST_URL = "https://" + TEST_HOST + "/";
    private static final String HTTP_TEST_URL = "http://" + TEST_HOST + "/";
    private static final String HTTP_PROXY_URL = "localhost:3333";
    private static final String HTTPS_PROXY_URL = "localhost:3334";

    @BeforeAll
    static void init() {
        System.setProperty("javax.net.ssl.trustStore", "path_to_trustStore");
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    @Test
    void test1() {
        try {
            int count = 100;
            System.out.println("Среднее время загрузки страницы по HTTP протоколу: " + doNRequests(HTTP_TEST_URL, HTTP_PROXY_URL, count) + " мс");
            System.out.println("Среднее время загрузки страницы по HTTPS протоколу: " + doNRequests(HTTPS_TEST_URL, HTTPS_PROXY_URL, count) + " мс");
        } catch (IOException e) {
            Assertions.fail(e);
        }
    }

    @Test
    void test2() {
        int count = 100;
        System.out.println("Среднее время генерации сертификата: " + createCertificateAverageTime(count));
    }

    long createCertificateAverageTime(int count) {
        long average = 0;
        for (int i = 0; i < count; i++) {
            average += createCertificate();
        }
        return average / count;
    }

    long createCertificate() {
        long startTime = System.currentTimeMillis();
        CertUtil.createFakeCertificate(TEST_HOST);
        return System.currentTimeMillis() - startTime;
    }

    long doNRequests(String url, @Nullable String proxyUrl, int count) throws IOException {
        long average = 0;
        for (int i = 0; i < count; i++) {
            average += doRequest(url, proxyUrl);
        }
        return average / count;
    }

    long doRequest(String url, @Nullable String proxyUrl) throws IOException {
        long startTime = System.currentTimeMillis();
        if (proxyUrl != null) {
            Request.Get(url).viaProxy(proxyUrl).execute();
        } else {
            Request.Get(url).execute();
        }
        return System.currentTimeMillis() - startTime;
    }

}
