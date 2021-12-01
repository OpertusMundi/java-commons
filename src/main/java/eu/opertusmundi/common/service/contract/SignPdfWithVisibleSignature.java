package eu.opertusmundi.common.service.contract;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.naming.InvalidNameException;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.apache.pdfbox.util.Matrix;
import org.springframework.util.Assert;

class SignPdfWithVisibleSignature extends SignPdf
{
    private static final Locale defaultLocale = Locale.getDefault();
    
    private static final MessageFormat textFormat = 
        new MessageFormat("Signed by: {0}\nSigned on: {1,date,d MMM yyyy HH:mm}", defaultLocale);
    
    /**
     * The background color. May be <tt>null</tt>.
     */
    private Color backgroundColor;
    
    /**
     * The background image for the rectangle of the visible signature. May be <tt>null</tt>.
     */
    private File imageFile;
    
    /** 
     * The alpha (transparency) level for the background image. May be <tt>null</tt>.
     */
    private Float imageAlpha;
    
    /**
     * The scale factor for the background image (same for x and y dimension). May be <tt>null</tt>
     * to indicate that no scaling is needed.
     */
    private Float imageScaleFactor;
    
    private PDFont font = PDType1Font.HELVETICA;
    
    private float fontSize = 9.0f;
    
    private Color fontColor = Color.BLACK;
    
    /**
     * The rectangle that will enclose the visible signature form.
     */
    private Rectangle2D userRect;
    
    public void setBackgroundColor(Color color)
    {
        this.backgroundColor = color;
    }
    
    public void setImageFile(File imageFile)
    {
        this.imageFile = imageFile;
    }
    
    public void setImageAlpha(Float imageAlpha)
    {
        this.imageAlpha = imageAlpha;
    }
    
    public void setImageScaleFactor(Float factor)
    {
        this.imageScaleFactor = factor;
    }
    
    public void setFont(PDFont font)
    {
        this.font = font;
    }
    
    public void setFontSize(float fontSize)
    {
        this.fontSize = fontSize;
    }
    
    public void setFontColor(Color fontColor)
    {
        this.fontColor = fontColor;
    }
    
    public void setUserRect(Rectangle2D userRect)
    {
        this.userRect = userRect;
    }
    
    public SignPdfWithVisibleSignature(KeyStore keystore, String alias, char[] password)
        throws Exception
    {
        super(keystore, alias, password);
    }

    @Override
    protected SignatureOptions createSignatureOptions(PDDocument document, PDSignature signature) 
        throws InvalidNameException, IOException
    {
        Assert.notNull(userRect, "A rectangle must be specified");
        
        final SignatureOptions signatureOptions = super.createSignatureOptions(document, signature);
        final PDRectangle rect = createSignatureRectangle(document, userRect);
        
        signatureOptions.setVisualSignature(createVisualSignatureTemplate(document, 0, rect, signature));
        signatureOptions.setPage(0);
        
        return signatureOptions;
    }
    
    /**
     * Translate the rectangle as given by the user to a rectangle understood by PDF.
     * 
     * @param document
     * @param userRect A human-friendly rectangle (coordinates start from the top left of a page;
     *    height/width are interpreted regardless of page rotation)
     * @return A PDF rectangle (coordinates start from the bottom)
     */
    private PDRectangle createSignatureRectangle(PDDocument document, Rectangle2D userRect)
    {
        float x = (float) userRect.getX();
        float y = (float) userRect.getY();
        float width = (float) userRect.getWidth();
        float height = (float) userRect.getHeight();
        PDPage page = document.getPage(0);
        PDRectangle pageRect = page.getCropBox();
        PDRectangle rect = new PDRectangle();
        // signing should be at the same position regardless of page rotation.
        switch (page.getRotation())
        {
            case 90:
                rect.setLowerLeftY(x);
                rect.setUpperRightY(x + width);
                rect.setLowerLeftX(y);
                rect.setUpperRightX(y + height);
                break;
            case 180:
                rect.setUpperRightX(pageRect.getWidth() - x);
                rect.setLowerLeftX(pageRect.getWidth() - x - width);
                rect.setLowerLeftY(y);
                rect.setUpperRightY(y + height);
                break;
            case 270:
                rect.setLowerLeftY(pageRect.getHeight() - x - width);
                rect.setUpperRightY(pageRect.getHeight() - x);
                rect.setLowerLeftX(pageRect.getWidth() - y - height);
                rect.setUpperRightX(pageRect.getWidth() - y);
                break;
            case 0:
            default:
                rect.setLowerLeftX(x);
                rect.setUpperRightX(x + width);
                rect.setLowerLeftY(pageRect.getHeight() - y - height);
                rect.setUpperRightY(pageRect.getHeight() - y);
                break;
        }
        return rect;
    }

    /** 
     * Create a template PDF document with empty signature and return it as a stream.
     */
    private InputStream createVisualSignatureTemplate(
        PDDocument sourceDocument, int pageNum, PDRectangle rect, PDSignature signature) 
            throws IOException, InvalidNameException
    {
        try (PDDocument templateDocument = new PDDocument()) 
        {
            PDPage page = new PDPage(sourceDocument.getPage(pageNum).getMediaBox());
            templateDocument.addPage(page);
            PDAcroForm acroForm = new PDAcroForm(templateDocument);
            templateDocument.getDocumentCatalog().setAcroForm(acroForm);
            PDSignatureField signatureField = new PDSignatureField(acroForm);
            PDAnnotationWidget widget = signatureField.getWidgets().get(0);
            List<PDField> acroFormFields = acroForm.getFields();
            acroForm.setSignaturesExist(true);
            acroForm.setAppendOnly(true);
            acroForm.getCOSObject().setDirect(true);
            acroFormFields.add(signatureField);

            widget.setRectangle(rect);

            // from PDVisualSigBuilder.createHolderForm()
            PDStream stream = new PDStream(templateDocument);
            PDFormXObject form = new PDFormXObject(stream);
            PDResources res = new PDResources();
            form.setResources(res);
            form.setFormType(1);
            PDRectangle bbox = new PDRectangle(rect.getWidth(), rect.getHeight());
            float height = bbox.getHeight();
            Matrix initialScale = null;
            switch (sourceDocument.getPage(pageNum).getRotation())
            {
                case 90:
                    form.setMatrix(AffineTransform.getQuadrantRotateInstance(1));
                    initialScale = Matrix.getScaleInstance(bbox.getWidth() / bbox.getHeight(), 
                        bbox.getHeight() / bbox.getWidth());
                    height = bbox.getWidth();
                    break;
                case 180:
                    form.setMatrix(AffineTransform.getQuadrantRotateInstance(2)); 
                    break;
                case 270:
                    form.setMatrix(AffineTransform.getQuadrantRotateInstance(3));
                    initialScale = Matrix.getScaleInstance(bbox.getWidth() / bbox.getHeight(), 
                        bbox.getHeight() / bbox.getWidth());
                    height = bbox.getWidth();
                    break;
                case 0:
                default:
                    break;
            }
            form.setBBox(bbox);

            // from PDVisualSigBuilder.createAppearanceDictionary()
            PDAppearanceDictionary appearance = new PDAppearanceDictionary();
            appearance.getCOSObject().setDirect(true);
            PDAppearanceStream appearanceStream = new PDAppearanceStream(form.getCOSObject());
            appearance.setNormalAppearance(appearanceStream);
            widget.setAppearance(appearance);

            try (PDPageContentStream cs = new PDPageContentStream(templateDocument, appearanceStream))
            {
                if (initialScale != null) {
                    cs.transform(initialScale);
                }

                // Fill background (usually just for debugging, to see the rectangle size and position)
                
                if (backgroundColor != null) {
                    cs.setNonStrokingColor(backgroundColor);
                    cs.addRect(-5000, -5000, 10000, 10000);
                    cs.fill();
                }
                
                // Show background image (on top of the background color)
                
                if (imageFile != null) {
                    cs.saveGraphicsState();
                    // Set image transparency if needed
                    if (imageAlpha != null) {
                        PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
                        graphicsState.getCOSObject().setItem(COSName.BM, COSName.MULTIPLY); // graphicsState.setBlendMode(BlendMode.MULTIPLY) doesn't work yet, maybe in later version 
                        graphicsState.setNonStrokingAlphaConstant(imageAlpha.floatValue());
                        cs.setGraphicsStateParameters(graphicsState);
                    }
                    if (imageScaleFactor != null) {
                        float s = imageScaleFactor.floatValue();
                        cs.transform(Matrix.getScaleInstance(s, s));
                    }
                    PDImageXObject image = PDImageXObject.createFromFileByExtension(imageFile, templateDocument);
                    cs.drawImage(image, 0, 0);
                    cs.restoreGraphicsState();
                }

                // Show text
                
                cs.beginText();
                cs.setFont(font, fontSize);
                cs.setNonStrokingColor(fontColor);
                cs.newLineAtOffset(fontSize, height - 1.25f * fontSize);
                cs.setLeading(1.5f * fontSize); /* line height */

                String name = getSubjectCN();
                Date date = signature.getSignDate().getTime();

                String text = textFormat.format(new Object[] {name, date});
                String[] textLines = text.split("\\R");
                for (int i = 0; i < textLines.length; ++i) {
                    if (i > 0) 
                        cs.newLine();
                    cs.showText(textLines[i]);
                }
                
                cs.endText();
            }

            // no need to set annotations and /P entry
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            templateDocument.save(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        }
    }
}
