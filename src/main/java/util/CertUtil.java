package util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import proxy.model.FakeCertificate;
import sun.security.rsa.RSAPrivateCrtKeyImpl;
import sun.security.tools.keytool.CertAndKeyGen;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

public abstract class CertUtil {

    private static final Logger LOGGER = Logger.getLogger(CertUtil.class);

    private static X509Certificate getCertificateFromFile(String fileName) throws java.security.cert.CertificateException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certificateFactory.generateCertificate(CertUtil.class.getResourceAsStream("/cert/" + fileName));
    }

    private static PrivateKey getPrivateKeyFromFile(String fileName) throws IOException, InvalidKeyException {
        byte[] privateKeyEncodedBytes = IOUtils.toByteArray(CertUtil.class.getResourceAsStream("/cert/" + fileName));
        String privateKeyEncodedString = new String(privateKeyEncodedBytes);
        privateKeyEncodedString = privateKeyEncodedString.replace("-----BEGIN PRIVATE KEY-----" + System.lineSeparator(), "");
        privateKeyEncodedString = privateKeyEncodedString.replace("-----END PRIVATE KEY-----", "");
        Base64 base64 = new Base64();
        return RSAPrivateCrtKeyImpl.newKey(base64.decode(privateKeyEncodedString));
    }

    public static FakeCertificate createFakeCertificate(String hostName) throws CertificateException, IOException, InvalidKeyException, NoSuchProviderException, NoSuchAlgorithmException, SignatureException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        X509Certificate rootCertificate = getCertificateFromFile("root.crt");
        PrivateKey rootPrivateKey = getPrivateKeyFromFile("root.pem");
        CertAndKeyGen certAndKeyGen = new CertAndKeyGen("RSA", "SHA256WithRSA", null);
        certAndKeyGen.generate(2048);

        X500Principal x500Principal = new X500Principal("CN=" + hostName);
        PKCS10CertificationRequest pkcs10CertificationRequest = new PKCS10CertificationRequest(
                "SHA256WithRSA",
                x500Principal,
                certAndKeyGen.getPublicKey(),
                null,
                certAndKeyGen.getPrivateKey());

        X509V3CertificateGenerator x509V3CertificateGenerator = new X509V3CertificateGenerator();

        x509V3CertificateGenerator.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        x509V3CertificateGenerator.setIssuerDN(rootCertificate.getSubjectX500Principal());
        x509V3CertificateGenerator.setNotBefore(new Date());
        x509V3CertificateGenerator.setNotAfter(new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1_000L));
        x509V3CertificateGenerator.setSubjectDN(x500Principal);
        x509V3CertificateGenerator.setPublicKey(pkcs10CertificationRequest.getPublicKey("BC"));
        x509V3CertificateGenerator.setSignatureAlgorithm("SHA256WithRSAEncryption");

        x509V3CertificateGenerator.addExtension(X509Extension.authorityKeyIdentifier, true, new AuthorityKeyIdentifierStructure(rootCertificate));
        x509V3CertificateGenerator.addExtension(X509Extension.subjectKeyIdentifier, true, new SubjectKeyIdentifierStructure(pkcs10CertificationRequest.getPublicKey("BC")));
        x509V3CertificateGenerator.addExtension(X509Extension.basicConstraints, true, new BasicConstraints(false));
        x509V3CertificateGenerator.addExtension(X509Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));
        x509V3CertificateGenerator.addExtension(X509Extension.extendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.anyExtendedKeyUsage));

        GeneralName generalName = new GeneralName(GeneralName.dNSName, hostName);
        GeneralNames subjectAltNames = new GeneralNames(generalName);
        x509V3CertificateGenerator.addExtension(X509Extension.subjectAlternativeName, true, subjectAltNames);

        X509Certificate fakeCreatedCertificate = x509V3CertificateGenerator.generate(rootPrivateKey);
        return new FakeCertificate(fakeCreatedCertificate, certAndKeyGen.getPrivateKey());
    }

}
