package util;

import model.FakeCertificate;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import org.jetbrains.annotations.Nullable;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public abstract class CertUtil {

    private static final Logger LOGGER = Logger.getLogger(CertUtil.class);
    public static final String WEB_FILTER_ROOT_CRT = "WebFilterRoot.crt";
    private static final String WEB_FILTER_ROOT_PEM = "WebFilterRoot.pem";

    private static X509Certificate getCertificateFromFile(String fileName) throws CertificateException, IOException {
        try (InputStream inputStream = CertUtil.class.getResourceAsStream("/cert/" + fileName)) {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certificateFactory.generateCertificate(inputStream);
        }
    }

    /**
     * Метод чтения приватного ключа из файлов расширения PEM, и только из них!
     *
     * @param fileName имя PEM-файла с расширением
     * @return приватный ключ из PEM-файла
     */
    private static PrivateKey getPrivateKeyFromPemFile(String fileName) throws IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
        try (InputStream inputStream = CertUtil.class.getResourceAsStream("/cert/" + fileName);
             PemReader pemReader = new PemReader(new InputStreamReader(inputStream))) {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
            PemObject pemObject = pemReader.readPemObject();
            byte[] content = pemObject.getContent();
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(content);
            return keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        }
    }

    @Nullable
    public static FakeCertificate createFakeCertificate(String hostName) {
        try {
            X509Certificate rootCertificate = getCertificateFromFile(WEB_FILTER_ROOT_CRT);
            PrivateKey rootPrivateKey = getPrivateKeyFromPemFile(WEB_FILTER_ROOT_PEM);

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            X500Principal x500Principal = new X500Principal("CN=" + hostName);
            PKCS10CertificationRequest pkcs10CertificationRequest = new PKCS10CertificationRequest(
                    "SHA256WithRSA",
                    x500Principal,
                    keyPair.getPublic(),
                    null,
                    keyPair.getPrivate());

            X509V3CertificateGenerator x509V3CertificateGenerator = new X509V3CertificateGenerator();

            x509V3CertificateGenerator.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
            x509V3CertificateGenerator.setIssuerDN(rootCertificate.getSubjectX500Principal());
            x509V3CertificateGenerator.setNotBefore(new Date());
            x509V3CertificateGenerator.setNotAfter(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365)));
            x509V3CertificateGenerator.setSubjectDN(x500Principal);
            x509V3CertificateGenerator.setPublicKey(pkcs10CertificationRequest.getPublicKey("BC"));
            x509V3CertificateGenerator.setSignatureAlgorithm("SHA256WithRSAEncryption");

            x509V3CertificateGenerator.addExtension(X509Extension.authorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure(rootCertificate));
            x509V3CertificateGenerator.addExtension(X509Extension.subjectKeyIdentifier, false, new SubjectKeyIdentifierStructure(pkcs10CertificationRequest.getPublicKey("BC")));
            x509V3CertificateGenerator.addExtension(X509Extension.basicConstraints, true, new BasicConstraints(false));
            x509V3CertificateGenerator.addExtension(X509Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));
            x509V3CertificateGenerator.addExtension(X509Extension.extendedKeyUsage, false, new ExtendedKeyUsage(new KeyPurposeId[]{
                    KeyPurposeId.id_kp_serverAuth
            }));

            GeneralName generalName = new GeneralName(GeneralName.dNSName, hostName);
            GeneralNames subjectAltNames = new GeneralNames(generalName);
            x509V3CertificateGenerator.addExtension(X509Extension.subjectAlternativeName, false, subjectAltNames);

            X509Certificate fakeCreatedCertificate = x509V3CertificateGenerator.generate(rootPrivateKey);
            return new FakeCertificate(fakeCreatedCertificate, keyPair.getPrivate());
        } catch (SignatureException | InvalidKeySpecException | NoSuchAlgorithmException | CertificateException | InvalidKeyException | IOException | NoSuchProviderException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

}
