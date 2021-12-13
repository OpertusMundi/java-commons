package eu.opertusmundi.common.service.contract;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.OutputStream;
import java.security.KeyStore;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Service
@ConditionalOnBean(name = "signatoryKeyStore")
public class DefaultSignPdfService implements SignPdfService {

    @Autowired
    private ResourceLoader resourceLoader;

    private boolean externalSigning = false;

    protected boolean isExternalSigning()
    {
        return externalSigning;
    }

    protected void setExternalSigning(boolean externalSigning)
    {
        this.externalSigning = externalSigning;
    }

    @Value("${opertusmundi.contract.signpdf.key-store-password}")
    private String keyPassword; // same as keystore password

    @Value("${opertusmundi.contract.signpdf.signature-reason}")
    private String signatureReason;

    @Value("${opertusmundi.contract.signpdf.signature-location}")
    private String signatureLocation; // geographical location (analogous to locality in CSRs)

	@Value("${opertusmundi.contract.signpdf.key-alias}")
	private String keyAlias;

    @Autowired
    @Qualifier("signatoryKeyStore")
    private KeyStore keystore;

    private Rectangle2D rectangleForVisibleSignature;

    @Autowired
    private void setVisibleSignatureRect(
        @Value("${opertusmundi.contract.signpdf.visible-signature.rectangle}") float[] coords)
    {
        Assert.isTrue(coords.length == 4, "Expected exactly 4 floats as {x0, y0, w, h}");

        final float x0 = coords[0];
        final float y0 = coords[1];
        final float w = coords[2];
        final float h = coords[3];
        this.rectangleForVisibleSignature = new Rectangle2D.Float(x0, y0, w, h);
    }

    private Color backgroundColor;

    @Autowired
    private void setBackgroundColor(
        @Value("${opertusmundi.contract.signpdf.visible-signature.background-color:}") String colorAsHex)
    {
        if (!StringUtils.isEmpty(colorAsHex)) {
            Assert.isTrue(colorAsHex.startsWith("#"), "An RGB color in hex notation should start with `#`");
            this.backgroundColor = new Color(Integer.parseInt(colorAsHex.substring(1), 16));
        }
    }

    @Value("${opertusmundi.contract.signpdf.visible-signature.image.file:}")
    private String imageFileLocation;

    @Value("${opertusmundi.contract.signpdf.visible-signature.image.alpha:1.0}")
    private Float imageAlpha;

    @Value("${opertusmundi.contract.signpdf.visible-signature.image.scale-factor:}")
    private Float imageScaleFactor;

    @Override
    public void sign(PDDocument document, OutputStream output)
        throws Exception
    {
        final SignPdf signpdf = new SignPdf(keystore, keyAlias, keyPassword.toCharArray());

        signpdf.setExternalSigning(externalSigning);
        signpdf.setSignatureLocation(signatureLocation);
        signpdf.setSignatureReason(signatureReason);

        signpdf.signDetached(document, output);
    }

    @Override
    public void signWithVisibleSignature(PDDocument document, OutputStream output)
        throws Exception
    {
        final SignPdfWithVisibleSignature signpdf =
            new SignPdfWithVisibleSignature(keystore, keyAlias, keyPassword.toCharArray());

        signpdf.setExternalSigning(externalSigning);
        signpdf.setSignatureLocation(signatureLocation);
        signpdf.setSignatureReason(signatureReason);

        signpdf.setUserRect(rectangleForVisibleSignature);

        if (backgroundColor != null) {
            signpdf.setBackgroundColor(backgroundColor);
        }

        if (imageFileLocation != null) {
            final Resource imageFileResource =
                resourceLoader.getResource(imageFileLocation);
            final File imageFile = imageFileResource.getFile();
            if (imageFile.exists()) {
                signpdf.setImageFile(imageFile);
                signpdf.setImageAlpha(imageAlpha != null? imageAlpha.floatValue() : 1.0f);
                signpdf.setImageScaleFactor(imageScaleFactor != null? imageScaleFactor.floatValue(): 1.0f);
            }
        }

        signpdf.setFont(PDType1Font.HELVETICA_BOLD);
        signpdf.setFontSize(9.5f);

        signpdf.signDetached(document, output);
    }
}
