package eu.opertusmundi.common.service.contract;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.x500.X500Principal;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class BaseSignPdf implements SignatureInterface
{
    protected static Logger logger = LoggerFactory.getLogger(BaseSignPdf.class);
    
    private PrivateKey privateKey;
    
    private X509Certificate[] certificateChain;
    
    private boolean externalSigning = false;
        
    protected PrivateKey getPrivateKey()
    {
        return privateKey;
    }
    
    public Certificate[] getCertificateChain()
    {
        return certificateChain;
    }
    
    protected boolean isExternalSigning()
    {
        return externalSigning;
    }
    
    protected void setExternalSigning(boolean externalSigning)
    {
        this.externalSigning = externalSigning;
    }
    
    protected X500Principal getSubject()
    {
        return this.certificateChain[0].getSubjectX500Principal();
    }
    
    protected String getSubjectCN() throws InvalidNameException
    {
        X500Principal principal = getSubject();
        LdapName dn = new LdapName(principal.getName(X500Principal.CANONICAL));
        Rdn rdn = dn.getRdn(dn.size() - 1);
        return rdn.getType().equals("cn")? rdn.getValue().toString() : null;
    }
    
    public BaseSignPdf(KeyStore keystore, String alias, char[] password) throws Exception
    {
        this.privateKey = (PrivateKey) keystore.getKey(alias, password);
        this.certificateChain = Arrays.stream(keystore.getCertificateChain(alias))
            .toArray(X509Certificate[]::new);
        
        X509Certificate certificate = this.certificateChain[0];
        certificate.checkValidity();
        checkCertificateUsage(certificate);
    }
        
    /**
     * This method will be called from inside of the pdfbox and create the PKCS #7 signature.
     * The given InputStream contains the bytes that are given by the byte range.
     *<p>
     * This method is for internal use only (but is public because of implementing interface).
     *<p>
     * Use your favorite cryptographic library to implement PKCS #7 signature creation.
     * If you want to create the hash and the signature separately (e.g. to transfer only the hash
     * to an external application), read <a href="https://stackoverflow.com/questions/41767351">this
     * answer</a> or <a href="https://stackoverflow.com/questions/56867465">this answer</a>.
     *
     * @throws IOException
     */
    @Override
    public byte[] sign(InputStream content) throws IOException
    {
        try {
            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            X509Certificate cert = (X509Certificate) certificateChain[0];
            ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA256WithRSA")
                .setProvider("BC")
                .build(privateKey);
            gen.addSignerInfoGenerator(
                new JcaSignerInfoGeneratorBuilder(
                    new JcaDigestCalculatorProviderBuilder().build()).build(sha1Signer, cert));
            gen.addCertificates(new JcaCertStore(Arrays.asList(certificateChain)));
            
            CMSProcessableByteArray msg = new CMSProcessableByteArray(IOUtils.toByteArray(content));
            CMSSignedData signedData = gen.generate(msg, false);
            
            //// Use an external TimeStamp Authority (TSA)
            //// See https://svn.apache.org/viewvc/pdfbox/trunk/examples/src/main/java/org/apache/pdfbox/examples/signature/ValidationTimeStamp.java?revision=1818049&view=markup
            //if (tsaUrl != null && tsaUrl.length() > 0) {
            //    ValidationTimeStamp validation = new ValidationTimeStamp(tsaUrl);
            //    signedData = validation.addSignedTimeStamp(signedData);
            //}
            
            return signedData.getEncoded();
        } catch (GeneralSecurityException | CMSException | OperatorCreationException e) {
            throw new IOException(e);
        }
    }
    
    //
    // Utilities
    //
    
    /**
     * Log if the certificate is not valid for signature usage. Doing this
     * anyway results in Adobe Reader failing to validate the PDF.
     *
     * @param x509Certificate
     * @throws java.security.cert.CertificateParsingException
     */
    private static void checkCertificateUsage(X509Certificate x509Certificate)
        throws CertificateParsingException
    {
        // Check whether signer certificate is "valid for usage"
        // https://stackoverflow.com/a/52765021/535646
        // https://www.adobe.com/devnet-docs/acrobatetk/tools/DigSig/changes.html#id1
        boolean[] keyUsage = x509Certificate.getKeyUsage();
        if (keyUsage != null && !keyUsage[0] && !keyUsage[1]) {
            // (unclear what "signTransaction" is)
            // https://tools.ietf.org/html/rfc5280#section-4.2.1.3
            logger.error("Certificate key usage does not include digitalSignature nor nonRepudiation");
        }
        
        List<String> extendedKeyUsage = x509Certificate.getExtendedKeyUsage();
        if (extendedKeyUsage != null &&
            !extendedKeyUsage.contains(KeyPurposeId.id_kp_emailProtection.toString()) &&
            !extendedKeyUsage.contains(KeyPurposeId.id_kp_codeSigning.toString()) &&
            !extendedKeyUsage.contains(KeyPurposeId.anyExtendedKeyUsage.toString()) &&
            !extendedKeyUsage.contains("1.2.840.113583.1.1.5") &&
            // not mentioned in Adobe document, but tolerated in practice
            !extendedKeyUsage.contains("1.3.6.1.4.1.311.10.3.12")) {
            logger.error("Certificate extended key usage does not include " +
                "emailProtection, nor codeSigning, nor anyExtendedKeyUsage, " +
                "nor 'Adobe Authentic Documents Trust'");
        }
    }

    /**
     * Log if the certificate is not valid for timestamping.
     *
     * @param x509Certificate
     * @throws java.security.cert.CertificateParsingException
     */
    private static void checkTimeStampCertificateUsage(X509Certificate x509Certificate)
        throws CertificateParsingException
    {
        List<String> extendedKeyUsage = x509Certificate.getExtendedKeyUsage();
        // https://tools.ietf.org/html/rfc5280#section-4.2.1.12
        if (extendedKeyUsage != null &&
            !extendedKeyUsage.contains(KeyPurposeId.id_kp_timeStamping.toString())) {
            logger.error("Certificate extended key usage does not include timeStamping");
        }
    }

    /**
     * Log if the certificate is not valid for responding.
     *
     * @param x509Certificate
     * @throws java.security.cert.CertificateParsingException
     */
    private static void checkResponderCertificateUsage(X509Certificate x509Certificate)
        throws CertificateParsingException
    {
        List<String> extendedKeyUsage = x509Certificate.getExtendedKeyUsage();
        // https://tools.ietf.org/html/rfc5280#section-4.2.1.12
        if (extendedKeyUsage != null &&
            !extendedKeyUsage.contains(KeyPurposeId.id_kp_OCSPSigning.toString())) {
            logger.error("Certificate extended key usage does not include OCSP responding");
        }
    }

    /**
     * Gets the last relevant signature in the document, i.e. the one with the
     * highest offset.
     * 
     * @param document
     *            to get its last signature
     * @return last signature or null when none found
     * @throws IOException 
     */
    private static PDSignature getLastRelevantSignature(PDDocument document) throws IOException
    {
        SortedMap<Integer, PDSignature> sortedMap = new TreeMap<>();
        for (PDSignature signature : document.getSignatureDictionaries()) {
            int sigOffset = signature.getByteRange()[1];
            sortedMap.put(sigOffset, signature);
        }
        if (sortedMap.size() > 0) {
            PDSignature lastSignature = sortedMap.get(sortedMap.lastKey());
            COSBase type = lastSignature.getCOSObject().getItem(COSName.TYPE);
            if (type.equals(COSName.SIG) || type.equals(COSName.DOC_TIME_STAMP)) {
                return lastSignature;
            }
        }
        return null;
    }
}
