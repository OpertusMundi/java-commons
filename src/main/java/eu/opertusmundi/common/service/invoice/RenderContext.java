package eu.opertusmundi.common.service.invoice;

import java.io.IOException;
import java.util.Objects;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.slf4j.Logger;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor(staticName = "of")
@Getter
public class RenderContext implements AutoCloseable {

    @Getter
    public static class Fonts {

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

    @NonNull
    private final Logger logger;

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
        Objects.requireNonNull(document, "Expected a non-null document");

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
            logger.warn("Failed to close PDF page", ex);
        }
    }

}
