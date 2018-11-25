package proxy.model;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class FakeCertificate {

    private X509Certificate certificate;
    private PrivateKey privateKey;

    public FakeCertificate(X509Certificate certificate, PrivateKey privateKey) {
        this.certificate = certificate;
        this.privateKey = privateKey;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }
}
