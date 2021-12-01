package eu.opertusmundi.common.service.contract;

import java.io.OutputStream;

import org.apache.pdfbox.pdmodel.PDDocument;

public interface SignPdfService
{
    public void sign(PDDocument document, OutputStream output)
        throws Exception;
    
    public void signWithVisibleSignature(PDDocument document, OutputStream output)
        throws Exception;
}
