package eu.opertusmundi.common.service.invoice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import eu.opertusmundi.common.domain.PayInServiceBillingItemEntity;
import eu.opertusmundi.common.model.account.CustomerDto;
import eu.opertusmundi.common.model.account.CustomerIndividualDto;
import eu.opertusmundi.common.model.payment.EnumInvoiceType;
import eu.opertusmundi.common.model.payment.EnumPaymentItemType;
import eu.opertusmundi.common.repository.PayInRepository;
import eu.opertusmundi.common.service.invoice.RenderContext.Fonts;

@Service
@Transactional
public class DefaultInvoiceGeneratorService extends BaseInvoiceGeneratorService implements InvoiceGeneratorService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultInvoiceGeneratorService.class);


    private static float textFontSize   = 9.0f;
    private static float titleFontSize  = 12.0f;

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

    private final InvoiceFileManager invoiceFileManager;
    private final PayInRepository    payInRepository;
    private final ResourceLoader     resourceLoader;

    @Autowired
    public DefaultInvoiceGeneratorService(
        InvoiceFileManager invoiceFileManager,
        PayInRepository payInRepository,
        ResourceLoader resourceLoader
    ) {
        this.invoiceFileManager = invoiceFileManager;
        this.payInRepository    = payInRepository;
        this.resourceLoader     = resourceLoader;
    }

    @Override
    public String generateInvoicePdf(EnumInvoiceType type, UUID payinKey) throws IOException {
        final String result = switch (type) {
            case ORDER_INVOICE ->
                this.renderOrderInvoice(payinKey);
            case REFUND_INVOICE -> 
                this.renderRefundInvoice(payinKey);
            case SERVICE_BILLING_INVOICE ->
                this.renderServicePayoffInvoice(payinKey);
        };

        return result;
    }

    private void addMetadata(RenderContext ctx) {
        final PDDocumentInformation pdd = ctx.getDocument().getDocumentInformation();

        pdd.setAuthor("Topio Market");
        pdd.setTitle("Topio Market Invoice");
        pdd.setCreator("Topio Market");
        pdd.setSubject("Invoice");
        pdd.setKeywords("topio, invoice, pdf document");
    }

    private String renderOrderInvoice(UUID payinKey) throws IOException {
        final PayInEntity   payin                = payInRepository.findOneEntityByKey(payinKey).orElse(null);
        final OrderEntity   order                = ((PayInOrderItemEntity) payin.getItems().get(0)).getOrder();
        final AccountEntity consumer             = order.getConsumer();
        final CustomerDto   customerDto          = consumer.getProfile().getConsumer().toDto();
        final Integer       userId               = consumer.getId();
        final String        payinReferenceNumber = payin.getReferenceNumber();

        final Path   path = invoiceFileManager.resolvePath(userId, payinReferenceNumber);
        final byte[] data = renderOrderInvoice(payin, order, customerDto);
        this.save(path, data);

        payin.setInvoicePrintedOn(ZonedDateTime.now());
        this.payInRepository.saveAndFlush(payin);

        return path.toString();
    }

    private byte[] renderOrderInvoice(PayInEntity payin, OrderEntity orderEntity, CustomerDto customerDto) throws IOException {
        final OrderItemEntity       orderItemEntity      = orderEntity.getItems().get(0);
        final String                orderReferenceNumber = orderEntity.getReferenceNumber();
        final String                fullName             = orderEntity.getConsumer().getFullName();
        final CustomerIndividualDto customer             = (CustomerIndividualDto) customerDto;
        final String                address              = customer.getAddress().toString();
        final String                country              = customer.getCountryOfResidence();
        final String                assetName            = orderItemEntity.getAssetTitle();
        final BigDecimal            priceExclTax         = payin.getTotalPriceExcludingTax();
        final BigDecimal            tax                  = payin.getTotalTax();
        final BigDecimal            totalPrice           = payin.getTotalPrice();
        final ZonedDateTime         orderDate            = orderEntity.getCreatedOn();

        // Initialize all variables that are related to the PDF formatting
        final PDDocument    document = new PDDocument();
        RenderContext.Fonts fonts;
        byte[]              logo;

        try (
            InputStream logoIs           = resourceLoader.getResource(logoFilename).getInputStream();
            InputStream regularFontIs    = resourceLoader.getResource(regularFont).getInputStream();
            InputStream boldFontIs       = resourceLoader.getResource(boldFont).getInputStream();
            InputStream italicFontIs     = resourceLoader.getResource(italicFont).getInputStream();
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
        try (final RenderContext ctx = RenderContext.of(logger, document, logo, fonts)) {
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
            content.setFont(fonts.getTextBold(), titleFontSize);
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
            content.setFont(fonts.getTextBold(), titleFontSize);
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
            content.setFont(fonts.getTextBold(), titleFontSize);
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
            content.showText(this.formatCurrency(priceExclTax));
            content.endText();

            content.beginText();
            content.newLineAtOffset(400, 460);
            content.showText(this.formatCurrency(tax));
            content.endText();

            content.beginText();
            content.newLineAtOffset(470, 460);
            content.showText(this.formatCurrency(totalPrice));
            content.endText();

            content.beginText();
            content.setFont(fonts.getTextBold(), titleFontSize);
            content.newLineAtOffset(300, 430);
            content.showText("Invoice total");
            content.endText();

            content.beginText();
            content.newLineAtOffset(470, 430);
            content.showText(this.formatCurrency(totalPrice));
            content.endText();

            /* Add metadata, header and footer */
            addMetadata(ctx);
            addHeaderAndFooter(ctx, logoFilename, title);

            // Release RenderContext before closing the document
            ctx.close();
            try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                document.save(byteArrayOutputStream);
                document.close();

                return byteArrayOutputStream.toByteArray();
            }

        }
    }

    private String renderRefundInvoice(UUID payinKey) throws IOException {
        final PayInEntity   payin                 = payInRepository.findOneEntityByKey(payinKey).orElse(null);
        final AccountEntity consumer              = payin.getConsumer();
        final CustomerDto   customerDto           = consumer.getProfile().getConsumer().toDto();
        final Integer       userId                = consumer.getId();
        final String        refundReferenceNumber = payin.getRefund().getReferenceNumber();

        final Path   path = invoiceFileManager.resolvePath(userId, refundReferenceNumber);
        final byte[] data = renderRefundInvoice(payin, customerDto);
        this.save(path, data);

        payin.setInvoicePrintedOn(ZonedDateTime.now());
        this.payInRepository.saveAndFlush(payin);

        return path.toString();
    }

    private byte[] renderRefundInvoice(PayInEntity payin, CustomerDto customerDto) throws IOException {
        final String                payinReferenceNumber = payin.getReferenceNumber();
        final String                fullName             = payin.getConsumer().getFullName();
        final CustomerIndividualDto customer             = (CustomerIndividualDto) customerDto;
        final String                address              = customer.getAddress().toString();
        final String                country              = customer.getCountryOfResidence();

        final BigDecimal            priceExclTax         = payin.getTotalPriceExcludingTax();
        final BigDecimal            tax                  = payin.getTotalTax();
        final BigDecimal            totalPrice           = payin.getTotalPrice();
        final ZonedDateTime         orderDate            = payin.getExecutedOn();

        // Initialize all variables that are related to the PDF formatting
        final PDDocument    document = new PDDocument();
        RenderContext.Fonts fonts;
        byte[]              logo;

        try (
            InputStream logoIs           = resourceLoader.getResource(logoFilename).getInputStream();
            InputStream regularFontIs    = resourceLoader.getResource(regularFont).getInputStream();
            InputStream boldFontIs       = resourceLoader.getResource(boldFont).getInputStream();
            InputStream italicFontIs     = resourceLoader.getResource(italicFont).getInputStream();
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
        try (final RenderContext ctx = RenderContext.of(logger, document, logo, fonts)) {
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
            content.setFont(fonts.getTextBold(), titleFontSize);
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
            content.setFont(fonts.getTextBold(), titleFontSize);
            content.newLineAtOffset(60, 580);
            content.showText("Service billing details");
            content.endText();

            content.beginText();
            content.setFont(fonts.getText(), textFontSize);
            content.newLineAtOffset(60, 560);
            content.showText("Payment date");
            content.endText();

            content.beginText();
            content.newLineAtOffset(60, 540);
            content.showText("Payment #");
            content.endText();

            content.beginText();
            content.newLineAtOffset(200, 560);
            content.showText(orderDate.toString());
            content.endText();

            content.beginText();
            content.newLineAtOffset(200, 540);
            content.showText(payinReferenceNumber);
            content.endText();

            drawLine(content, 0.5f, 60, 520, 0, pageWidth - 72);

            // Invoice details

            content.beginText();
            content.setFont(fonts.getTextBold(), titleFontSize);
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

            // TODO: Add subscription / user service billing details
            content.beginText();
            content.setFont(fonts.getTextBold(), titleFontSize);
            content.newLineAtOffset(300, 430);
            content.showText("Invoice total");
            content.endText();

            content.beginText();
            content.newLineAtOffset(470, 430);
            content.showText(this.formatCurrency(totalPrice));
            content.endText();

            /* Add metadata, header and footer */
            addMetadata(ctx);
            addHeaderAndFooter(ctx, logoFilename, title);

            // Release RenderContext before closing the document
            ctx.close();
            try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                document.save(byteArrayOutputStream);
                document.close();

                return byteArrayOutputStream.toByteArray();
            }
        }
    }
    
    private String renderServicePayoffInvoice(UUID payinKey) throws IOException {
        final PayInEntity                         payin = payInRepository.findOneEntityByKey(payinKey).orElse(null);
        final List<PayInServiceBillingItemEntity> items = payin.getItems().stream()
            .filter(i -> i.getType() == EnumPaymentItemType.SERVICE_BILLING)
            .map(i -> (PayInServiceBillingItemEntity) i)
            .toList();

        Assert.isTrue(items.size() > 0, "Expected at least one subscription billing item");

        final AccountEntity consumer             = payin.getConsumer();
        final CustomerDto   customerDto          = consumer.getProfile().getConsumer().toDto();
        final Integer       userId               = consumer.getId();
        final String        payinReferenceNumber = payin.getReferenceNumber();

        final Path   path = invoiceFileManager.resolvePath(userId, payinReferenceNumber);
        final byte[] data = renderServicePayoffInvoice(payin, items, customerDto);
        this.save(path, data);

        payin.setInvoicePrintedOn(ZonedDateTime.now());
        this.payInRepository.saveAndFlush(payin);

        return path.toString();
    }

    private byte[] renderServicePayoffInvoice(PayInEntity payin, List<PayInServiceBillingItemEntity> items, CustomerDto customerDto) throws IOException {
        final String                payinReferenceNumber = payin.getReferenceNumber();
        final String                fullName             = payin.getConsumer().getFullName();
        final CustomerIndividualDto customer             = (CustomerIndividualDto) customerDto;
        final String                address              = customer.getAddress().toString();
        final String                country              = customer.getCountryOfResidence();

        final BigDecimal            priceExclTax         = payin.getTotalPriceExcludingTax();
        final BigDecimal            tax                  = payin.getTotalTax();
        final BigDecimal            totalPrice           = payin.getTotalPrice();
        final ZonedDateTime         orderDate            = payin.getExecutedOn();

        // Initialize all variables that are related to the PDF formatting
        final PDDocument    document = new PDDocument();
        RenderContext.Fonts fonts;
        byte[]              logo;

        try (
            InputStream logoIs           = resourceLoader.getResource(logoFilename).getInputStream();
            InputStream regularFontIs    = resourceLoader.getResource(regularFont).getInputStream();
            InputStream boldFontIs       = resourceLoader.getResource(boldFont).getInputStream();
            InputStream italicFontIs     = resourceLoader.getResource(italicFont).getInputStream();
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
        try (final RenderContext ctx = RenderContext.of(logger, document, logo, fonts)) {
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
            content.setFont(fonts.getTextBold(), titleFontSize);
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
            content.setFont(fonts.getTextBold(), titleFontSize);
            content.newLineAtOffset(60, 580);
            content.showText("Service billing details");
            content.endText();

            content.beginText();
            content.setFont(fonts.getText(), textFontSize);
            content.newLineAtOffset(60, 560);
            content.showText("Payment date");
            content.endText();

            content.beginText();
            content.newLineAtOffset(60, 540);
            content.showText("Payment #");
            content.endText();

            content.beginText();
            content.newLineAtOffset(200, 560);
            content.showText(orderDate.toString());
            content.endText();

            content.beginText();
            content.newLineAtOffset(200, 540);
            content.showText(payinReferenceNumber);
            content.endText();

            drawLine(content, 0.5f, 60, 520, 0, pageWidth - 72);

            // Invoice details

            content.beginText();
            content.setFont(fonts.getTextBold(), titleFontSize);
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

            // TODO: Add subscription / user service billing details
            content.beginText();
            content.setFont(fonts.getTextBold(), titleFontSize);
            content.newLineAtOffset(300, 430);
            content.showText("Invoice total");
            content.endText();

            content.beginText();
            content.newLineAtOffset(470, 430);
            content.showText(this.formatCurrency(totalPrice));
            content.endText();

            /* Add metadata, header and footer */
            addMetadata(ctx);
            addHeaderAndFooter(ctx, logoFilename, title);

            // Release RenderContext before closing the document
            ctx.close();
            try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                document.save(byteArrayOutputStream);
                document.close();

                return byteArrayOutputStream.toByteArray();
            }
        }
    }

}