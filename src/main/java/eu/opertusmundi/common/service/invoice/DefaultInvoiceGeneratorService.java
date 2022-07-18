package eu.opertusmundi.common.service.invoice;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.OrderEntity;
import eu.opertusmundi.common.domain.OrderItemEntity;
import eu.opertusmundi.common.domain.PayInEntity;
import eu.opertusmundi.common.domain.PayInOrderItemEntity;
import eu.opertusmundi.common.model.account.CustomerDto;
import eu.opertusmundi.common.model.account.CustomerIndividualDto;
import eu.opertusmundi.common.repository.PayInRepository;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Service
@Transactional
public class DefaultInvoiceGeneratorService implements InvoiceGeneratorService {

    @Autowired
    private InvoiceFileManager invoiceFileManager;

    private static float headerFontSize = 12.0f;
    private static float footerFontSize = 7.0f;
    private static float textFontSize   = 9.0f;
    private static float titleFontSize  = 12.0f;
    private static Color color          = Color.BLACK;
    private static Color color2         = Color.BLUE;

    @Value("${opertusmundi.contract.logo}")
    private String logoFilename;

    @Value("${opertusmundi.contract.font-regular}")
    private String regularFont;

    @Value("${opertusmundi.contract.font-bold}")
    private String boldFont;

    @Value("${opertusmundi.contract.font-italic}")
    private String italicFont;

    @Value("${opertusmundi.contract.font-bold-italic}")
    private String boldItalicFont;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private PayInRepository payInRepository;

    @Getter
    private static class Fonts {

        private final PDFont title;
        private final PDFont subTitle;
        private final PDFont sectionTitle;
        private final PDFont text;
        private final PDFont textBold;
        private final PDFont textItalic;
        private final PDFont textBoldItalic;
        private final PDFont header;
        private final PDFont footer;

        public Fonts(PDFont regular, PDFont bold, PDFont italic, PDFont boldItalic) {
            title          = subTitle = sectionTitle = textBold = bold;
            text           = header = footer = regular;
            textItalic     = italic;
            textBoldItalic = boldItalic;
        }
    }

	@RequiredArgsConstructor(staticName = "of")
    @Getter
    private static class RenderContext implements AutoCloseable {

        @NonNull
        private final PDDocument document;

        @NonNull
        private final byte[] logo;

        @NonNull
        private final Fonts fonts;

        private PDPage page;

        private PDPageContentStream content;

        @Setter
        private float currentOffset = -100;

        @Setter
        private boolean previousBlockIsListItem;

        public PDPageContentStream addPage() throws IOException {
            Assert.notNull(document, "Expected a non-null document");

            if (this.content != null) {
                this.content.close();
                this.content = null;
            }

            this.page = new PDPage(PDRectangle.A4);
            document.addPage(this.page);
            this.content = new PDPageContentStream(document, this.page);

            return content;
        }

        @Override
        public void close() {
            try {
                if (this.content != null) {
                    this.content.close();
                }
            } catch (final Exception ex) {
                // TODO: Handle error ...
            }
        }

    }

	private void drawLine(
	        PDPageContentStream contentStream, float lineWidth, float sx, float sy, float linePosition, float pageWidth
	    ) throws IOException {
	        /* Needed for header and footer */
	        contentStream.setLineWidth(lineWidth);
	        contentStream.moveTo(sx, sy + linePosition);
	        contentStream.lineTo(pageWidth, sy + linePosition);
	        contentStream.stroke();
	    }

	private float VerticalOffset(String move, float pageHeight) {
        float offset = 0;

        if (move.equals("top")) {
            offset = +pageHeight / 2 - 20;
        } else if (move.equals("bottom")) {
            offset = -pageHeight / 2 + 15;
        }

        return offset;
    }

	private void addHeaderAndFooter(RenderContext ctx, String contractTitle) throws IOException {
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
                        centerYHeader + VerticalOffset("top", pageHeight) -15));
                /* Write */
                contentStream.showText(headerText);
                contentStream.endText();

                /* Create the PDFImageXObject object to add the topio logo */
                final PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, ctx.getLogo(), logoFilename);
                contentStream.drawImage(pdImage, 60, centerYHeader + VerticalOffset("top", pageHeight) - 27, 74, 28);
                //drawLine(contentStream, 0.5f, 72, centerYHeader + VerticalOffset("top", pageHeight) - 23, -8, pageWidth - 72);

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
                contentStream.showText(contractTitle);

                contentStream.endText();
                drawLine(contentStream, 0.5f, 72, centerYHeader + VerticalOffset("bottom", pageHeight) + 23, +8, pageWidth - 72);
            }
            currentPage++;
        }
    }

    private void addMetadata(RenderContext ctx) {
        final PDDocumentInformation pdd = ctx.getDocument().getDocumentInformation();

        pdd.setAuthor("Topio Market");
        pdd.setTitle("Topio Market Contract");
        pdd.setCreator("Topio Market");
        pdd.setSubject("Contract");
        pdd.setKeywords("topio, contract, pdf documnet");
    }

    /**
     * Create invoice PDF
     *
     * @param payinKey
     * @return
     * @throws IOException
     */
    @Override
    public String generateInvoicePdf(UUID payinKey) throws IOException {
        final PayInEntity   payin                = payInRepository.findOneEntityByKey(payinKey).orElse(null);
        final OrderEntity   order                = ((PayInOrderItemEntity) payin.getItems().get(0)).getOrder();
        final AccountEntity consumer             = order.getConsumer();
        final CustomerDto   customerDto          = consumer.getProfile().getConsumer().toDto();
        final Integer       userId               = consumer.getId();
        final String        payinReferenceNumber = payin.getReferenceNumber();

        final Path   path = invoiceFileManager.resolvePath(userId, payinReferenceNumber);
        final byte[] data = renderPDF(payin, order, customerDto);
        this.save(path, data);

        order.setInvoicePrintedOn(ZonedDateTime.now());
        this.payInRepository.saveAndFlush(payin);

        return path.toString();
    }

    private byte[] renderPDF(PayInEntity payin, OrderEntity orderEntity, CustomerDto customerDto) throws IOException {
        final OrderItemEntity orderItemEntity      = orderEntity.getItems().get(0);
        final String          orderReferenceNumber = orderEntity.getReferenceNumber();
        // final AccountEntity provider = orderItemEntity.getProvider();
        final String                fullName     = orderEntity.getConsumer().getFullName();
        final CustomerIndividualDto customer     = (CustomerIndividualDto) customerDto;
        final String                address      = customer.getAddress().toString();
        final String                country      = customer.getCountryOfResidence();
        final String                assetName    = orderItemEntity.getDescription();
        final BigDecimal            priceExclTax = payin.getTotalPriceExcludingTax();
        final BigDecimal            tax          = payin.getTotalTax();
        final BigDecimal            totalPrice   = payin.getTotalPrice();
        final ZonedDateTime         orderDate    = orderEntity.getCreatedOn();

        // Initialize all variables that are related to the PDF formatting
        final PDDocument document = new PDDocument();
        Fonts            fonts;
        byte[]           logo;

        try (
            InputStream logoIs = resourceLoader.getResource(logoFilename).getInputStream();
            InputStream regularFontIs = resourceLoader.getResource(regularFont).getInputStream();
            InputStream boldFontIs = resourceLoader.getResource(boldFont).getInputStream();
            InputStream italicFontIs = resourceLoader.getResource(italicFont).getInputStream();
            InputStream boldItalicFontIs = resourceLoader.getResource(boldItalicFont).getInputStream();
        ) {
            logo = IOUtils.toByteArray(logoIs);

            final PDFont bold       = PDType0Font.load(document, boldFontIs);
            final PDFont regular    = PDType0Font.load(document, regularFontIs);
            final PDFont italic     = PDType0Font.load(document, italicFontIs);
            final PDFont boldItalic = PDType0Font.load(document, boldItalicFontIs);

            fonts = new Fonts(regular, bold, italic, boldItalic);
        }

        // Create rendering context
        try (final RenderContext ctx = RenderContext.of(document, logo, fonts)) {
            /* Get title and subtitles */
            final String title = "Invoice";

            ctx.addPage();
            final PDPage page = ctx.getPage();
            fonts = ctx.getFonts();
            final PDPageContentStream content = ctx.getContent();

            final PDRectangle pageSize = page.getMediaBox();

            /* Get the width and height of the page */
            final float pageWidth = pageSize.getWidth();
            // final float pageHeight = pageSize.getHeight();

            drawLine(content, 0.5f, 60, 690, 0, pageWidth - 72);

            content.beginText();
            content.setFont(fonts.textBold, titleFontSize);
            content.newLineAtOffset(60, 670);
            content.showText("Billing address");
            content.endText();

            content.beginText();
            content.newLineAtOffset(460, 670);
            content.showText("Sold by");
            content.endText();

            // Customer details

            content.beginText();
            content.setFont(fonts.getText(), textFontSize);
            content.setLeading(20f);
            content.newLineAtOffset(60, 650);
            content.showText(fullName);
            content.endText();

            content.beginText();
            content.newLineAtOffset(60, 635);
            content.showText(address);
            content.endText();

            content.beginText();
            content.newLineAtOffset(60, 620);
            content.showText(country);
            content.endText();

            // Marketplace details

            content.beginText();
            content.newLineAtOffset(460, 650);
            content.showText("Topio Market");
            content.endText();

            drawLine(content, 0.5f, 60, 600, 0, pageWidth - 72);

            // Order details

            content.beginText();
            content.setFont(fonts.textBold, titleFontSize);
            content.newLineAtOffset(60, 580);
            content.showText("Order details");
            content.endText();

            content.beginText();
            content.setFont(fonts.getText(), textFontSize);
            content.newLineAtOffset(60, 560);
            content.showText("Order date");
            content.endText();

            content.beginText();
            content.newLineAtOffset(60, 540);
            content.showText("Order #");
            content.endText();

            content.beginText();
            content.newLineAtOffset(200, 560);
            content.showText(orderDate.toString());
            content.endText();

            content.beginText();
            content.newLineAtOffset(200, 540);
            content.showText(orderReferenceNumber);
            content.endText();

            drawLine(content, 0.5f, 60, 520, 0, pageWidth - 72);

            // Invoice details

            content.beginText();
            content.setFont(fonts.textBold, titleFontSize);
            content.newLineAtOffset(60, 500);
            content.showText("Invoice details");
            content.endText();

            content.beginText();
            content.setFont(fonts.getTextBold(), textFontSize);
            content.newLineAtOffset(60, 480);
            content.showText("Description");
            content.endText();

            content.beginText();
            content.newLineAtOffset(300, 480);
            content.showText("Price (excl. VAT)");
            content.endText();

            content.beginText();
            content.newLineAtOffset(400, 480);
            content.showText("VAT");
            content.endText();

            content.beginText();
            content.newLineAtOffset(470, 480);
            content.showText("Total price");
            content.endText();

            content.beginText();
            content.setFont(fonts.getText(), textFontSize);
            content.newLineAtOffset(60, 460);
            content.showText(assetName);
            content.endText();

            content.beginText();
            content.newLineAtOffset(300, 460);
            content.showText(priceExclTax.toString());
            content.endText();

            content.beginText();
            content.newLineAtOffset(400, 460);
            content.showText(tax.toString());
            content.endText();

            content.beginText();
            content.newLineAtOffset(470, 460);
            content.showText(totalPrice.toString());
            content.endText();

            content.beginText();
            content.setFont(fonts.textBold, titleFontSize);
            content.newLineAtOffset(300, 430);
            content.showText("Invoice total");
            content.endText();

            content.beginText();
            content.newLineAtOffset(470, 430);
            content.showText(totalPrice.toString());
            content.endText();

            /* Add metadata, header and footer */
            addMetadata(ctx);
            addHeaderAndFooter(ctx, title);

            // Release RenderContext before closing the document
            ctx.close();
            try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                document.save(byteArrayOutputStream);
                document.close();

                return byteArrayOutputStream.toByteArray();
            }

        }
    }

    private void save(Path path, byte[] data) throws FileNotFoundException, IOException {
        try (final FileOutputStream fos = new FileOutputStream(path.toFile())) {
            fos.write(data);
        }
    }
}