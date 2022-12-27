package eu.opertusmundi.common.service.invoice;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Locale;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import org.springframework.beans.factory.annotation.Value;

import com.ibm.icu.text.NumberFormat;

import eu.opertusmundi.common.service.invoice.RenderContext.Fonts;

public abstract class BaseInvoiceGeneratorService {

    @Value("${opertusmundi.contract.header-font-size:12.0f}")
    private float headerFontSize;

    @Value("${opertusmundi.contract.footer-font-size:7.0f}")
    private float footerFontSize;

    private static Color color  = Color.BLACK;
    private static Color color2 = Color.BLUE;

    protected float VerticalOffset(String move, float pageHeight) {
        float offset = 0;

        if (move.equals("top")) {
            offset = +pageHeight / 2 - 20;
        } else if (move.equals("bottom")) {
            offset = -pageHeight / 2 + 15;
        }

        return offset;
    }

    protected void drawLine(
        PDPageContentStream contentStream, float lineWidth, float sx, float sy, float linePosition, float pageWidth
    ) throws IOException {
        /* Needed for header and footer */
        contentStream.setLineWidth(lineWidth);
        contentStream.moveTo(sx, sy + linePosition);
        contentStream.lineTo(pageWidth, sy + linePosition);
        contentStream.stroke();
    }

    protected void addHeaderAndFooter(RenderContext ctx, String logoFilename, String title) throws IOException {
        final PDDocument document    = ctx.getDocument();
        final Fonts      fonts       = ctx.getFonts();
        int              currentPage = 0;

        final String headerText = "Invoice";

        /* Iterate through all pages */
        for (final PDPage page : document.getPages()) {
            final PDRectangle pageSize = page.getMediaBox();

            /*
             * Calculate the widths off all header and footer texts, needed for
             * the calculation of their positions in the page
             */
            final float stringWidthFooter = fonts.getFooter().getStringWidth((currentPage + 1) + " / " + document.getNumberOfPages())
                    * footerFontSize / 1000f;
            final float stringWidthHeader = fonts.getHeader().getStringWidth(headerText) * headerFontSize / 1000f;

            /* Get the width and height of the page */
            final float pageWidth  = pageSize.getWidth();
            final float pageHeight = pageSize.getHeight();

            /* Calculate the center of the page based on header */
            final float centerYHeader = pageHeight / 2f;

            /* Calculate the center of the page based on footer */
            final float centerYFooter = pageHeight / 2f;

            /* Append the content to the existing stream */
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, true, true)) {
                /* Add header */
                contentStream.beginText();
                /* Set header font, font size and font color */
                contentStream.setFont(fonts.getHeader(), headerFontSize);
                contentStream.setNonStrokingColor(color);
                /* Set the position */
                contentStream.setTextMatrix(Matrix.getTranslateInstance(pageWidth - 72 - stringWidthHeader,
                        centerYHeader + VerticalOffset("top", pageHeight) - 15));
                /* Write */
                contentStream.showText(headerText);
                contentStream.endText();

                /* Create the PDFImageXObject object to add the topio logo */
                final PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, ctx.getLogo(), logoFilename);
                contentStream.drawImage(pdImage, 60, centerYHeader + VerticalOffset("top", pageHeight) - 27, 74, 28);
                // drawLine(contentStream, 0.5f, 72, centerYHeader +
                // VerticalOffset("top", pageHeight) - 23, -8, pageWidth - 72);

                /* Add footer */
                contentStream.beginText();
                /* Set footer font, font size and font color */
                contentStream.setFont(fonts.getFooter(), footerFontSize);
                contentStream.setNonStrokingColor(color2);
                /* Set the position */
                contentStream.setTextMatrix(Matrix.getTranslateInstance(pageWidth - 72 - stringWidthFooter,
                        centerYFooter + VerticalOffset("bottom", pageHeight) + 18));
                /* Write */
                contentStream.showText((currentPage + 1) + " / " + document.getNumberOfPages());

                contentStream.setNonStrokingColor(color);
                /* Set the position */
                contentStream.setTextMatrix(Matrix.getTranslateInstance(72, centerYFooter + VerticalOffset("bottom", pageHeight) + 18));
                /* Write */
                contentStream.showText(title);

                contentStream.endText();
                drawLine(contentStream, 0.5f, 72, centerYHeader + VerticalOffset("bottom", pageHeight) + 23, +8, pageWidth - 72);
            }
            currentPage++;
        }
    }

    protected String formatCurrency(BigDecimal value) {
        final var locale       = new Locale("el", "GR");
        final var numberFormat = NumberFormat.getCurrencyInstance(locale);

        return numberFormat.format(value);
    }

    protected void save(Path path, byte[] data) throws FileNotFoundException, IOException {
        try (final FileOutputStream fos = new FileOutputStream(path.toFile())) {
            fos.write(data);
        }
    }

}
