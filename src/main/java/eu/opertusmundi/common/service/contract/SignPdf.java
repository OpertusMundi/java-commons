package eu.opertusmundi.common.service.contract;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyStore;
import java.util.Calendar;

import javax.naming.InvalidNameException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;

class SignPdf extends BaseSignPdf
{   
    protected String signatureReason;
    
    protected String signatureLocation;
    
    public void setSignatureLocation(String signatureLocation)
    {
        this.signatureLocation = signatureLocation;
    }
    
    public void setSignatureReason(String signatureReason)
    {
        this.signatureReason = signatureReason;
    }
    
    public SignPdf(KeyStore keystore, String alias, char[] password) throws Exception 
    {
        super(keystore, alias, password);
    }

    public void signDetached(File inFile, File outFile) 
        throws IOException, InvalidNameException
    {
        if (inFile == null || !inFile.exists()) {
            throw new IllegalArgumentException("The document for signing does not exist");
        }
        
        try (
            FileOutputStream fos = new FileOutputStream(outFile); 
            PDDocument doc = PDDocument.load(inFile)) 
        {
            signDetached(doc, fos);
        }
    }

    public void signDetached(PDDocument document, OutputStream output)
            throws IOException, InvalidNameException
    {
        // Create signature dictionary
        final PDSignature signature = new PDSignature();
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
        
        // Extract the details from the signing certificate; or set them explicitly
        signature.setName(getSubjectCN());
        signature.setLocation(signatureLocation);
        signature.setReason(signatureReason);
        // Set the signing date (needed for valid signature)
        signature.setSignDate(Calendar.getInstance());
        
        final SignatureOptions signatureOptions = createSignatureOptions(document, signature);
       
        // Do not set SignatureInterface instance, if external signing used
        final SignatureInterface signatureInterface = isExternalSigning() ? null : this;
        
        // Register signature dictionary and sign interface
        document.addSignature(signature, signatureInterface, signatureOptions);
        
        if (isExternalSigning()) {
            ExternalSigningSupport externalSigning = document.saveIncrementalForExternalSigning(output);
            // Invoke external signature service
            byte[] cmsSignature = sign(externalSigning.getContent());
            // Set signature bytes received from the service
            externalSigning.setSignature(cmsSignature);
        } else {
            // Write incremental (only for signing purpose)
            document.saveIncremental(output);
        }
        
        // Do not close signatureOptions before saving, because some COSStream objects within
        // are transferred to the signed document.
        // Do not allow signatureOptions get out of scope before saving, because then the COSDocument
        // in signature options might by closed by gc, which would close COSStream objects prematurely.
        // See https://issues.apache.org/jira/browse/PDFBOX-3743
        signatureOptions.close();
    }
    
    protected SignatureOptions createSignatureOptions(PDDocument document, PDSignature signature)
        throws IOException, InvalidNameException
    {
        SignatureOptions signatureOptions = new SignatureOptions();
        
        // Size can vary, but should be enough for purpose.
        signatureOptions.setPreferredSignatureSize(SignatureOptions.DEFAULT_SIGNATURE_SIZE);
        
        return signatureOptions;
    }
}
