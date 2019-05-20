package proxy.https;

import model.FakeCertificate;
import org.apache.log4j.Logger;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.crypto.tls.Certificate;
import org.bouncycastle.crypto.tls.DefaultTlsServer;
import org.bouncycastle.crypto.tls.DefaultTlsSignerCredentials;
import org.bouncycastle.crypto.tls.TlsSignerCredentials;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import util.CertUtil;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class FakeTlsServer extends DefaultTlsServer {

    private static final Logger LOGGER = Logger.getLogger(FakeTlsServer.class);
    private static final Map<String, FakeCertificate> CERTIFICATE_CACHE = new ConcurrentHashMap<>();

    private String host;

    FakeTlsServer(String host) {
        this.host = host;
    }

    @Override
    protected TlsSignerCredentials getRSASignerCredentials() throws IOException {
        try {
            FakeCertificate fakeCertificate = CERTIFICATE_CACHE.computeIfAbsent(host, CertUtil::createFakeCertificate);
            if (Objects.isNull(fakeCertificate)) {
                throw new IllegalArgumentException("Fake certificate is null! Check generation of certificate!");
            }
            X509CertificateHolder x509CertificateHolder =
                    new X509CertificateHolder(fakeCertificate.getCertificate().getEncoded());
            Certificate bcCert = new Certificate(new org.bouncycastle.asn1.x509.Certificate[]{
                    x509CertificateHolder.toASN1Structure()
            });
            return new DefaultTlsSignerCredentials(context, bcCert,
                    PrivateKeyFactory.createKey(fakeCertificate.getPrivateKey().getEncoded()));
        } catch (CertificateEncodingException e) {
            LOGGER.error(e.getMessage(), e);
            throw new IOException(e);
        }
    }

    @Override
    public void notifyAlertRaised(short i, short i1, String s, Exception e) {
        if (e != null) {
            LOGGER.error(s, e);
        }
    }

}
