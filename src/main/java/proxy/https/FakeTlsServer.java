package proxy.https;

import model.FakeCertificate;
import org.apache.log4j.Logger;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.crypto.tls.*;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import util.CertUtil;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

public class FakeTlsServer extends DefaultTlsServer {

    private static final Logger LOGGER = Logger.getLogger(FakeTlsServer.class);
    private static final Map<String, FakeCertificate> CERTIFICATE_CACHE = new HashMap<>();

    private String host;
    private TlsServerProtocol tlsServerProtocol;

    FakeTlsServer(String host, TlsServerProtocol tlsServerProtocol) {
        this.host = host;
        this.tlsServerProtocol = tlsServerProtocol;
    }

    @Override
    public void init(TlsServerContext context) {
        super.init(context);
    }

    @Override
    protected TlsSignerCredentials getRSASignerCredentials() throws IOException {
        try {
            FakeCertificate fakeCertificate;
            if (CERTIFICATE_CACHE.containsKey(host)) {
                fakeCertificate = CERTIFICATE_CACHE.get(host);
            } else {
                fakeCertificate = CertUtil.createFakeCertificate(host);
                CERTIFICATE_CACHE.put(host, fakeCertificate);
            }
            X509CertificateHolder x509CertificateHolder =
                    new X509CertificateHolder(fakeCertificate.getCertificate().getEncoded());
            Certificate bcCert = new Certificate(new org.bouncycastle.asn1.x509.Certificate[]{
                    x509CertificateHolder.toASN1Structure()
            });
            return new DefaultTlsSignerCredentials(context, bcCert,
                    PrivateKeyFactory.createKey(fakeCertificate.getPrivateKey().getEncoded()));
        } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException
                | SignatureException | NoSuchProviderException e) {
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

    @Override
    public void notifyHandshakeComplete() throws IOException {
        String hello = "<h1>Hello!</h1>";
        tlsServerProtocol.getOutputStream().write(hello.getBytes());
        tlsServerProtocol.getOutputStream().flush();
    }

}
