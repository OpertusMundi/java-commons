package eu.opertusmundi.common.service.contract;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.multipdf.Overlay;
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
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.ibm.icu.text.DecimalFormat;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.MasterContractHistoryEntity;
import eu.opertusmundi.common.domain.MasterSectionHistoryEntity;
import eu.opertusmundi.common.domain.OrderEntity;
import eu.opertusmundi.common.domain.OrderItemEntity;
import eu.opertusmundi.common.domain.ProviderTemplateContractHistoryEntity;
import eu.opertusmundi.common.domain.ProviderTemplateSectionHistoryEntity;
import eu.opertusmundi.common.model.catalogue.client.EnumContractType;
import eu.opertusmundi.common.model.catalogue.client.EnumDeliveryMethod;
import eu.opertusmundi.common.model.contract.ContractParametersDto;
import eu.opertusmundi.common.model.contract.ContractParametersDto.PricingModel;
import eu.opertusmundi.common.model.contract.ContractSectionSubOptionDto;
import eu.opertusmundi.common.model.contract.consumer.ConsumerContractCommand;
import eu.opertusmundi.common.model.contract.provider.ProviderContractCommand;
import eu.opertusmundi.common.model.pricing.DiscountRateDto;
import eu.opertusmundi.common.model.pricing.EnumContinent;
import eu.opertusmundi.common.model.pricing.EnumPricingModel;
import eu.opertusmundi.common.model.pricing.PrePaidTierDto;
import eu.opertusmundi.common.repository.OrderRepository;
import eu.opertusmundi.common.repository.contract.MasterContractHistoryRepository;
import eu.opertusmundi.common.repository.contract.ProviderTemplateContractHistoryRepository;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Service
public class DefaultPdfContractGeneratorService implements PdfContractGeneratorService {

    private static float titleFontSize           = 32.0f;
    private static float subTitleFontSize        = 15.0f;
    private static float sectionTitleFontSize    = 12.0f;
    private static float sectionSubTitleFontSize = 10.0f;
    private static float textFontSize            = 9.0f;
    private static float headerFontSize          = 7.0f;
    private static float footerFontSize          = 7.0f;
    private static Color color                   = Color.BLACK;
    private static Color color2                  = Color.BLUE;
    private static float textLeading             = 1.5f * textFontSize;

    private static final String NORMAL    = "NORMAL";
    private static final String BOLD      = "BOLD";
    private static final String ITALIC    = "ITALIC";
    private static final String UNDERLINE = "UNDERLINE";

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

    @Value("${opertusmundi.contract.watermark}")
    private String watermark;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProviderTemplateContractHistoryRepository contractRepository;

    @Autowired
    private MasterContractHistoryRepository masterContractRepository;

    private Map<String, String> keywords;

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

    @Getter
    @Setter
    private static class ParsedLine {
        private int    lineNumber;
        private String text;
        private String font;

        public ParsedLine(int lineNumber, String text, String font, float lineSize) {
            this.lineNumber = lineNumber;
            this.text       = text;
            this.font       = font;
        }
    }

    @Getter
    @Setter
    private static class Section {
        private String      sectionTitle;
        private List<Block> blocks;

        public Section(String sectionTitle) {
            this.sectionTitle = sectionTitle;
            blocks            = new ArrayList<Block>();
        }
    }

    @Getter
    @Setter
    private static class Block {
        private String                text;
        private ArrayList<BlockStyle> blockStyles;

        public Block(String text) {
            this.text   = text;
            blockStyles = new ArrayList<BlockStyle>();
        }

        public void addBlockStyle(BlockStyle blockStyle) {
            this.blockStyles.add(blockStyle);
        }
    }

    @Getter
    @Setter
    private static class BlockStyle {
        private int    offset;
        private int    length;
        private String style;

        public BlockStyle() {
        }

        public BlockStyle(int offset, int length, String style) {
            this.offset = offset;
            this.length = length;
            this.style  = style;
        }
    }

    private void addMetadata(RenderContext ctx) {
        final PDDocumentInformation pdd = ctx.getDocument().getDocumentInformation();

        pdd.setAuthor("Topio Market");
        pdd.setTitle("Topio Market Contract");
        pdd.setCreator("Topio Market");
        pdd.setSubject("Contract");
        pdd.setKeywords("topio, contract, pdf document");
    }

    private String subStringUsingLength(String myString, int start, int length) {
        final int endIndex = Math.min(start + length, myString.length());
        if (start > endIndex) {
            // Content must not be empty!
            return " ";
        }
        return myString.substring(start, endIndex);
    }

    private Map<String, String> createKeywordMapping() {
      keywords = new HashMap<>();

        keywords.put("[sellerName]", "Supplier name");
        keywords.put("[sellerAddress]", "Professional address");
        keywords.put("[sellerEmail]", "Contact email");
        keywords.put("[sellerContactPerson]", "Contact person");
        keywords.put("[sellerCompanyRegNumber]", "Company registration number");
        keywords.put("[sellerVAT]", "EU VAT number");
        keywords.put("[clientName]", "Customer name");
        keywords.put("[clientAddress]", "Professional address");
        keywords.put("[clientEmail]", "Contact email");
        keywords.put("[clientContactPerson]", "Contact person");
        keywords.put("[clientCompanyRegNumber]", "Company registration number");
        keywords.put("[clientVAT]", "EU VAT number");
        keywords.put("[ProductId]", "Product ID");
        keywords.put("[ProductName]", "Product Name");
        keywords.put("[ProductDescription]", "Product description");
        keywords.put("[PastVersionsIncluded]", "Past Versions included");
        keywords.put("[UpdatesIncluded]", "Updates included");
        keywords.put("[EstimatedDeliveryDate]", "Estimated delivery date");
        keywords.put("[DeliveryMediaFormat]", "Media and format of delivery");
        keywords.put("[ApplicableFees]", "Applicable fees");
        keywords.put("[INSERT HYPERLINK TOWARDS TOPIO’S T&CS LATEST VERSION]", "https://beta.topio.market/terms");
        keywords.put("[Date]", new SimpleDateFormat("dd MMMM yyyy").format(new Date()));

        return keywords;
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

    private Map<Integer, Integer> getLengthPerLine(List<ParsedLine> parsedLines) {
        /*
         * Find the length of each line, needed for the justification. Return a
         * HashMap where key is the line number and value is the length of the
         * line
         */
        int                             currentLineNumber = 0;
        int                             currentLength     = 0;
        final HashMap<Integer, Integer> LineLengths       = new HashMap<Integer, Integer>();

        for (final ParsedLine line : parsedLines) {
            final int    lineNumber = line.lineNumber;
            final String text       = line.text;

            if (lineNumber == currentLineNumber) {
                currentLength += text.length();
            } else {
                LineLengths.put(currentLineNumber, currentLength);
                currentLineNumber = lineNumber;
                currentLength     = text.length();
            }
        }

        LineLengths.put(currentLineNumber, currentLength);

        return LineLengths;
    }

    private Map<Integer, Float> getWidthPerLine(RenderContext ctx, List<ParsedLine> parsedLines) throws IOException {
        /*
         * Find the width of each line, needed for the justification. Return a
         * HashMap where key is the line number and value is the width of the
         * line.
         */
        int                           currentLineNumber = 0;
        float                         currentWidth      = 0;
        final HashMap<Integer, Float> LineWidths        = new HashMap<>();

        for (final ParsedLine line : parsedLines) {
            final String font       = line.font;
            final int    lineNumber = line.lineNumber;
            final String text       = line.text;
            float        size;

            if (font.equals(BOLD) || (font.contains(BOLD) && font.contains(UNDERLINE))) {
                size = textFontSize * ctx.getFonts().getTextBold().getStringWidth(text) / 1000;

            } else if (font.equals(ITALIC) || (font.contains(ITALIC) && font.contains(UNDERLINE))) {
                size = textFontSize * ctx.getFonts().getTextItalic().getStringWidth(text) / 1000;

            } else if (font.contains(BOLD) && font.contains(ITALIC)) {
                size = textFontSize * ctx.getFonts().getTextBoldItalic().getStringWidth(text) / 1000;

            } else {
                size = textFontSize * ctx.getFonts().getText().getStringWidth(text) / 1000;

            }

            if (lineNumber == currentLineNumber) {
                currentWidth += size;
            } else {
                LineWidths.put(currentLineNumber, currentWidth);
                currentLineNumber = lineNumber;
                currentWidth      = size;
            }
        }

        LineWidths.put(currentLineNumber, currentWidth);

        return LineWidths;
    }

    private void underlineWord(
        PDPageContentStream contentStream, String text, float size, float lineWidth, float sx, float sy, float linePosition
    ) throws IOException {
        contentStream.setLineWidth(lineWidth);
        contentStream.moveTo(sx, sy + linePosition);
        contentStream.lineTo(sx + size, sy + linePosition);
        contentStream.stroke();
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

    private List<String> ParseTitle(RenderContext ctx, String text, float width) throws IOException {
        final List<String> lines     = new ArrayList<>();
        int                lastSpace = -1;

        while (text.length() > 0) {
            int spaceIndex = text.indexOf(' ', lastSpace + 1);
            if (spaceIndex < 0) {
                spaceIndex = text.length();
            }
            String      subString = text.substring(0, spaceIndex);
            final float size      = textFontSize * ctx.getFonts().getText().getStringWidth(subString) / 1000;
            if (size > width) {
                if (lastSpace < 0) {
                    lastSpace = spaceIndex;
                }
                subString = text.substring(0, lastSpace);
                lines.add(subString);
                text      = text.substring(lastSpace).trim();
                lastSpace = -1;
            } else if (spaceIndex == text.length()) {
                lines.add(text);
                text = "";
            } else {
                lastSpace = spaceIndex;
            }
        }
        return lines;
    }

    private List<ParsedLine> parseLines(RenderContext ctx, Block block, float width) throws IOException {
        /* Get the text of the block */
        final String           text  = block.getText();
        final List<ParsedLine> lines = new ArrayList<ParsedLine>();
        final Fonts            fonts = ctx.getFonts();

        /* Initialize variables */
        int    lineCounter = 0;
        String currentLine = "";
        float  totalSize   = 0;

        /* For all blocks */
        for (int i = 0; i < block.getBlockStyles().size(); i++) {

            /* Get information of each block */
            final BlockStyle blockStyle = block.getBlockStyles().get(i);
            final int        offset     = blockStyle.getOffset();
            final int        length     = blockStyle.getLength();
            final String     style      = blockStyle.getStyle();

            /* Calculate the size of the current substring */
            float  currentSize      = 0;
            String currentSubstring = subStringUsingLength(text, offset, length);

            if (style.contains(BOLD) || (style.contains(BOLD) && style.contains(UNDERLINE))) {
                currentSize = textFontSize * fonts.getTextBold().getStringWidth(currentSubstring) / 1000;
            } else if (style.contains(ITALIC) || (style.contains(ITALIC) && style.contains(UNDERLINE))) {
                currentSize = textFontSize * fonts.getTextItalic().getStringWidth(currentSubstring) / 1000;
            } else if (style.contains(BOLD) && style.contains(ITALIC)) {
                currentSize = textFontSize * fonts.getTextBoldItalic().getStringWidth(currentSubstring) / 1000;
            } else {
                currentSize = textFontSize * fonts.getText().getStringWidth(currentSubstring) / 1000;
            }

            /* If the current substring fits into the current line */
            if (totalSize + currentSize <= width) {

                /* Append the current line */
                currentLine += currentSubstring;
                totalSize   += currentSize;
                final ParsedLine parsedLine = new ParsedLine(lineCounter, currentLine, style, currentSize);
                lines.add(parsedLine);

                /* The next block information will be written into a new line */
                currentLine = "";
                if (totalSize == width) {
                    totalSize = 0;
                    lineCounter++;
                }
            }
            /*
             * If the current substring does not fit entirely into the current
             * line, append the current line word by word until it is full
             */
            else if (totalSize + currentSize > width) {

                /* Initialize last space position */
                int lastSpace = -1;

                /* While the current substring has not been entirely parsed */
                while (currentSubstring.length() > 0) {
                    /* Find the position of the first(first loop)/next space */
                    int spaceIndex = currentSubstring.indexOf(' ', lastSpace + 1);
                    /* If there are no spaces the current substring is a word */
                    if (spaceIndex < 0) {
                        spaceIndex = currentSubstring.length();
                    }
                    /* Get the word */
                    String currentWord = currentSubstring.substring(0, spaceIndex);

                    /* Calculate the size of the current word */
                    float currentWordSize = 0;
                    if (style.contains(BOLD) || (style.contains(BOLD) && style.contains(UNDERLINE))) {
                        currentWordSize = textFontSize * fonts.getTextBold().getStringWidth(currentWord) / 1000;
                    } else if (style.contains(ITALIC) || (style.contains(ITALIC) && style.contains(UNDERLINE))) {
                        currentWordSize = textFontSize * fonts.getTextItalic().getStringWidth(currentWord) / 1000;
                    } else if (style.contains(BOLD) && style.contains(ITALIC)) {
                        currentWordSize = textFontSize * fonts.getTextBoldItalic().getStringWidth(currentWord) / 1000;
                    } else {
                        currentWordSize = textFontSize * fonts.getText().getStringWidth(currentWord) / 1000;
                    }

                    /* If the current word does not fit in the current line */
                    if (totalSize + currentWordSize > width) {

                        /*
                         * If the last space is not defined, then last space
                         * will be the current one
                         */
                        if (lastSpace < 0) {
                            lastSpace = spaceIndex;
                        }
                        /*
                         * Keep the substring until the last space until now
                         * (Exclude the last word that does not fit in the line)
                         */
                        currentWord = currentSubstring.substring(0, lastSpace);
                        /* Calculate the size of the current word */
                        if (style.contains(BOLD) || (style.contains(BOLD) && style.contains(UNDERLINE))) {
                            currentWordSize = textFontSize * fonts.getTextBold().getStringWidth(currentWord) / 1000;
                        } else if (style.contains(ITALIC) || (style.contains(ITALIC) && style.contains(UNDERLINE))) {
                            currentWordSize = textFontSize * fonts.getTextItalic().getStringWidth(currentWord) / 1000;
                        } else if (style.contains(BOLD) && style.contains(ITALIC)) {
                            currentWordSize = textFontSize * fonts.getTextBoldItalic().getStringWidth(currentWord) / 1000;
                        } else {
                            currentWordSize = textFontSize * fonts.getText().getStringWidth(currentWord) / 1000;
                        }

                        if (totalSize + currentWordSize <= width) {
                            /* Append the current line */
                            currentLine += currentWord;
                            totalSize   += currentWordSize;
                            final ParsedLine parsedLine = new ParsedLine(lineCounter, currentLine, style, currentWordSize);
	                		if (!parsedLine.text.equals("")) {
	                			lines.add(parsedLine);
	                		}
                            /*
                             * Initialize variables for the remaining part that
                             * has not been parsed and will be written in the
                             * next line
                             */
                            currentLine = "";
                            totalSize   = 0;
                            lineCounter++;
                            /*
                             * Update the current substring and remove the
                             * parsed part of it
                             */
                            currentSubstring = StringUtils.trimLeadingWhitespace(currentSubstring.substring(lastSpace));
                            lastSpace = -1;
                        } else {
                            currentLine = currentWord;
                            totalSize   = currentWordSize;
                            lineCounter++;

                            /*
                             * Update the current substring and remove the
                             * parsed part of it
                             */
                            currentSubstring = currentSubstring.substring(lastSpace);
                            lastSpace        = -1;
                            continue;
                        }
                    }
                    /*
                     * Else if the current substring fits in the current line
                     * and it is the last one
                     */
                    else if (spaceIndex == currentSubstring.length()) {
                        /* Append the current line */
                        currentLine += currentWord;
                        totalSize   += currentWordSize;
                        final ParsedLine parsedLine = new ParsedLine(lineCounter, currentLine, style, currentWordSize);
                		if (!parsedLine.text.equals("")) {
                			lines.add(parsedLine);
                		}
                        /* Initialize variables for next loop (not needed) */
                        currentLine = "";
                        if (totalSize == width) {
                            totalSize = 0;
                            lineCounter++;
                        }
                        currentSubstring = "";
                    }

                    /*
                     * Else if the current substring fits in the current line
                     * keep the position of the last space until now that the
                     * substring fits in the line
                     */
                    else {
                        lastSpace = spaceIndex;
                    }
                }
            }
        }

        return lines;
    }

    private String normalizeText(String text) {
    	return text.replace("\n", "").replace("\r", "").replace("\u00A0", "");
    }

    private List<Section> addOrderInformation(
            List<Section> allSections, ContractParametersDto contractParametersDto, Map<String, String> keywords
        ) {

    		/*
    		 * Helper variable to handle special case
    		 * The first [sellerEmail] should be replaced with the "Contact Email" string.
    		 * In all other cases it should be replaced with the actual e-mail value.
    		 * */
    		int occurrenceOfSellerEmail = 0;
    		keywords.put("[SELLEREMAIL]", normalizeText(contractParametersDto.getProvider().getContactEmail()));

        	/* Replaces all automated keywords with the provider, consumer and product info while rendering*/
        	/* For all sections and all blocks*/
            for (final Section section : allSections) {
                for (final Block block : section.getBlocks()) {
    	    		String initialText 	= block.text;

    	    		for (int j = 0 ; j < block.getBlockStyles().size() ; j++) {
    	    			/* If the block contains a bold-underlined part which is a master template contract keyword*/
    	    			if (block.getBlockStyles().get(j).style.contains(BOLD) &&
    	    				block.getBlockStyles().get(j).style.contains(UNDERLINE)) {
    	    				for (String key : keywords.keySet()) {
    	    					if (block.getText().contains(key)) {

    	    						/* Special case*/
    	    						if (block.getText().contains("[sellerEmail]") && occurrenceOfSellerEmail == 0) {
    	    							occurrenceOfSellerEmail++;
    	    						} else if (block.getText().contains("[sellerEmail]") && occurrenceOfSellerEmail != 0) {
    	    							block.text 	= block.getText().replace("[sellerEmail]", "[SELLEREMAIL]");
    	    							initialText = normalizeText(block.text);
    	    							key = "[SELLEREMAIL]";
    	    						}

    	    						/* Set style as BOLD for the words that the keywords will be replaced with*/
    	    						block.getBlockStyles().get(j).style = BOLD;
    	    						final int initialLength	= normalizeText(block.text).trim().length();

    	    						/* Replace the keyword with the appropriate final word from the Hash<ap with the keywords*/
    	    						block.text = normalizeText(block.getText().replace(key, keywords.get(key)));
    	    						block.text = block.text.trim();

    	    						final int newLength 	= block.text.length();
    	    						final int charDiff 		= initialLength - newLength;

    	    						/* Set new offset and length for the block style that describes the new words*/
    	    						for (int l = j ; l < block.getBlockStyles().size() ; l++) {
    	    							if (l != j) {
    	    								block.getBlockStyles().get(l).offset -= (charDiff);
    	    							}
    	    							else {
    	    								block.getBlockStyles().get(l).length -= (charDiff);
    	    							}
    	    						}
    	    						break;
    	    					}
    	    				}
    	    			}
    	    		}

    				final BlockStyle info = new BlockStyle();

    				/*Get provider, consumer and product info*/
    				final ContractParametersDto.Provider 		prov = contractParametersDto.getProvider();
    				final ContractParametersDto.Consumer 		cons = contractParametersDto.getConsumer();
    				final ContractParametersDto.Product  		prod = contractParametersDto.getProduct();

    				/* Append all keyword blocks with the corresponding information and update their block styles accordingly */
    				if (initialText.contains("[sellerName]")) {
    					info.offset	= block.getBlockStyles().get(block.getBlockStyles().size()-1).length+block.getBlockStyles().get(block.getBlockStyles().size()-1).offset;
    					info.length	= normalizeText(prov.getCorporateName()).length()+2;
    					info.style 	= NORMAL;
    					block.text = block.text.trim()+": "+normalizeText(prov.getCorporateName());
    					block.getBlockStyles().add(info);
    				}
    				else if (initialText.contains("[sellerAddress]")) {
    					info.offset	= block.getBlockStyles().get(block.getBlockStyles().size()-1).length+block.getBlockStyles().get(block.getBlockStyles().size()-1).offset;
    					info.length	= normalizeText(prov.getProfessionalAddress()).length()+2;
    					info.style 	= NORMAL;
    					block.text = block.text.trim()+": "+normalizeText(prov.getProfessionalAddress());
    					block.getBlockStyles().add(info);
    				}
    				else if (initialText.contains("[sellerEmail]")) {
    					info.offset	= block.getBlockStyles().get(block.getBlockStyles().size()-1).length+block.getBlockStyles().get(block.getBlockStyles().size()-1).offset;
    					info.length	= normalizeText(prov.getContactEmail()).length()+2;
    					info.style 	= NORMAL;
    					block.text = block.text.trim()+": "+normalizeText(prov.getContactEmail());
    					block.getBlockStyles().add(info);
    				}
    				else if (initialText.contains("[sellerContactPerson]")) {
    					info.offset	= block.getBlockStyles().get(block.getBlockStyles().size()-1).length+block.getBlockStyles().get(block.getBlockStyles().size()-1).offset;
    					info.length	= normalizeText(prov.getContactPerson()).length()+2;
    					info.style 	= NORMAL;
    					block.text = block.text.trim()+": "+normalizeText(prov.getContactPerson());
    					block.getBlockStyles().add(info);
    				}
    				else if (initialText.contains("[sellerCompanyRegNumber]")) {
    					info.offset	= block.getBlockStyles().get(block.getBlockStyles().size()-1).length+block.getBlockStyles().get(block.getBlockStyles().size()-1).offset;
    					info.length	= normalizeText(prov.getCompanyRegistrationNumber()).length()+2;
    					info.style 	= NORMAL;
    					block.text = block.text.trim()+": "+normalizeText(prov.getCompanyRegistrationNumber());
    					block.getBlockStyles().add(info);
    				}
    				else if (initialText.contains("[sellerVAT]")) {
    					info.offset	= block.getBlockStyles().get(block.getBlockStyles().size()-1).length+block.getBlockStyles().get(block.getBlockStyles().size()-1).offset;
    					info.length	= normalizeText(prov.getEuVatNumber()).length()+2;
    					info.style 	= NORMAL;
    					block.text = block.text.trim()+": "+normalizeText(prov.getEuVatNumber());
    					block.getBlockStyles().add(info);
    				}
    				else if (initialText.contains("[clientName]")) {
    					info.offset	= block.getBlockStyles().get(block.getBlockStyles().size()-1).length+block.getBlockStyles().get(block.getBlockStyles().size()-1).offset;
    					info.length	= normalizeText(cons.getCorporateName()).length()+2;
    					info.style 	= NORMAL;
    					block.text = block.text.trim()+": "+normalizeText(cons.getCorporateName());
    					block.getBlockStyles().add(info);
    				}
    				else if (initialText.contains("[clientAddress]")) {
    					info.offset	= block.getBlockStyles().get(block.getBlockStyles().size()-1).length+block.getBlockStyles().get(block.getBlockStyles().size()-1).offset;
    					info.length	= normalizeText(cons.getProfessionalAddress()).length()+2;
    					info.style 	= NORMAL;
    					block.text = block.text.trim()+": "+normalizeText(cons.getProfessionalAddress());
    					block.getBlockStyles().add(info);
    				}
    				else if (initialText.contains("[clientEmail]")) {
    					info.offset	= block.getBlockStyles().get(block.getBlockStyles().size()-1).length+block.getBlockStyles().get(block.getBlockStyles().size()-1).offset;
    					info.length	= normalizeText(cons.getContactEmail()).length()+2;
    					info.style 	= NORMAL;
    					block.text = block.text.trim()+": "+normalizeText(cons.getContactEmail());
    					block.getBlockStyles().add(info);
    				}
    				else if (initialText.contains("[clientContactPerson]")) {
    					info.offset	= block.getBlockStyles().get(block.getBlockStyles().size()-1).length+block.getBlockStyles().get(block.getBlockStyles().size()-1).offset;
    					info.length	= normalizeText(cons.getContactPerson()).length()+2;
    					info.style 	= NORMAL;
    					block.text = block.text.trim()+": "+normalizeText(cons.getContactPerson());
    					block.getBlockStyles().add(info);
    				}
    				else if (initialText.contains("[clientCompanyRegNumber]")) {
    					info.offset	= block.getBlockStyles().get(block.getBlockStyles().size()-1).length+block.getBlockStyles().get(block.getBlockStyles().size()-1).offset;
    					info.length	= normalizeText(cons.getCompanyRegistrationNumber()).length()+2;
    					info.style 	= NORMAL;
    					block.text = block.text.trim()+": "+normalizeText(cons.getCompanyRegistrationNumber());
    					block.getBlockStyles().add(info);
    				}
    				else if (initialText.contains("[clientVAT]")) {
    					info.offset	= block.getBlockStyles().get(block.getBlockStyles().size()-1).length+block.getBlockStyles().get(block.getBlockStyles().size()-1).offset;
    					info.length	= normalizeText(cons.getEuVatNumber()).length()+2;
    					info.style 	= NORMAL;
    					block.text = block.text.trim()+": "+normalizeText(cons.getEuVatNumber());
    					block.getBlockStyles().add(info);
    				}
    				else if (initialText.contains("[ProductId]")) {
    					info.offset	= block.getBlockStyles().get(block.getBlockStyles().size()-1).length+block.getBlockStyles().get(block.getBlockStyles().size()-1).offset;
    					info.length	= normalizeText(prod.getId()).length()+2;
    					info.style 	= NORMAL;
    					block.text = block.text.trim()+": "+normalizeText(prod.getId());
    					block.getBlockStyles().add(info);
    				}
    				else if (initialText.contains("[ProductName]")) {
    					info.offset	= block.getBlockStyles().get(block.getBlockStyles().size()-1).length+block.getBlockStyles().get(block.getBlockStyles().size()-1).offset;
    					info.length	= normalizeText(prod.getName()).length()+2;
    					info.style 	= NORMAL;
    					block.text = block.text.trim()+": "+normalizeText(prod.getName());
    					block.getBlockStyles().add(info);
    				}
    				else if (initialText.contains("[ProductDescription]")) {
    					info.offset	= block.getBlockStyles().get(block.getBlockStyles().size()-1).length+block.getBlockStyles().get(block.getBlockStyles().size()-1).offset;
    					info.length	= normalizeText(prod.getDescription()).length()+2;
    					info.style 	= NORMAL;
    					block.text = block.text.trim()+": "+normalizeText(prod.getDescription());
    					block.getBlockStyles().add(info);
    				}
    				else if (initialText.contains("[PastVersionsIncluded]")) {
    					info.offset	= block.getBlockStyles().get(block.getBlockStyles().size()-1).length+block.getBlockStyles().get(block.getBlockStyles().size()-1).offset;
    					info.length	= normalizeText(prod.getPastVersionIncluded()).length()+2;
    					info.style 	= NORMAL;
    					block.text = block.text.trim()+": "+normalizeText(prod.getPastVersionIncluded());
    					block.getBlockStyles().add(info);
    				}
    				else if (initialText.contains("[UpdatesIncluded]")) {
    					info.offset	= block.getBlockStyles().get(block.getBlockStyles().size()-1).length+block.getBlockStyles().get(block.getBlockStyles().size()-1).offset;
    					info.length	= normalizeText(prod.getUpdatesIncluded()).length()+2;
    					info.style 	= NORMAL;
    					block.text = block.text.trim()+": "+normalizeText(prod.getUpdatesIncluded());
    					block.getBlockStyles().add(info);
    				}
    				else if (initialText.contains("[EstimatedDeliveryDate]")) {
    					info.offset	= block.getBlockStyles().get(block.getBlockStyles().size()-1).length+block.getBlockStyles().get(block.getBlockStyles().size()-1).offset;
    					info.length	= normalizeText(prod.getEstimatedDeliveryDate()).length()+2;
    					info.style 	= NORMAL;
    					block.text = block.text.trim()+": "+normalizeText(prod.getEstimatedDeliveryDate());
    					block.getBlockStyles().add(info);
    				}
    				else if (initialText.contains("[DeliveryMediaFormat]")) {
    					info.offset	= block.getBlockStyles().get(block.getBlockStyles().size()-1).length+block.getBlockStyles().get(block.getBlockStyles().size()-1).offset;
    					info.length	= normalizeText(prod.getMediaAndFormatOfDelivery()).length()+2;
    					info.style 	= NORMAL;
    					block.text = block.text.trim()+": "+normalizeText(prod.getMediaAndFormatOfDelivery());
    					block.getBlockStyles().add(info);
    				}
    				else if (initialText.contains("[ApplicableFees]")) {
    					info.offset	= block.getBlockStyles().get(block.getBlockStyles().size()-1).length+block.getBlockStyles().get(block.getBlockStyles().size()-1).offset;
    					info.length	= normalizeText(prod.getApplicableFees()).length()+2;
    					info.style 	= NORMAL;
    					block.text = block.text.trim()+": "+normalizeText(prod.getApplicableFees());
    					block.getBlockStyles().add(info);
    				}
    			}
    	    }

        	/* Return the final sections that should be written*/
        	return allSections;
        }

    private Block generateSingleStylePricingModelBlock(
    		String text, String style) {
		final Block block = new Block(text);
		final int offset 	= 0;
		final int length	= text.length();
		final BlockStyle blockStyle = new BlockStyle(offset, length, style);
		block.getBlockStyles().add(blockStyle);
		return block;
    }

    private Block generateMultiStylePricingModelBlock(
    		String text) {
    	final Block block = new Block(text);
    	int offset	 	= 0;
    	int length 		= 0;
    	String style 	= null;
    	BlockStyle blockStyle = null;

		int loopCounter = 0;
		for (int index1 = text.indexOf("["), index2 = text.indexOf("]") ; index1 >= 0 && index2 >= 0 ; index1 = text.indexOf("[", index1+1), index2 = text.indexOf("]", index2+1)) {
			if (loopCounter == 0) {
				offset = 0;
			} else {
				offset += length;
			}
			length	= index1-offset;
			style	= NORMAL;
			blockStyle = new BlockStyle(offset, length, style);
			block.getBlockStyles().add(blockStyle);

			if (loopCounter == 0) {
				offset = index1;
			} else {
				offset += length;
			}
			length	= index2-index1+1;
			blockStyle = new BlockStyle(offset, length, BOLD);
			block.getBlockStyles().add(blockStyle);

			blockStyle = new BlockStyle(offset, length, UNDERLINE);
			block.getBlockStyles().add(blockStyle);

			if (text.indexOf("[", index1+1) < 0) {
				offset 	= index2+1;
				length	= text.length()-index2-1;
				style	= NORMAL;
				blockStyle = new BlockStyle(offset, length, style);
				block.getBlockStyles().add(blockStyle);
			}

			loopCounter++;
		}

		return block;
    }

    private static String restrictionsToString(final Object[] oArray) {
    	String string = "";
    	if (oArray == null || oArray.length == 0) {
    		return "None";
    	}
    	if (oArray instanceof EnumContinent[]) {
	    	for (int i = 0 ; i < oArray.length ; i++) {
	    		if (oArray.length == 1) {
	    			string = ((EnumContinent) oArray[i]).getDescription().toString();
	    		} else if (i == oArray.length-1) {
	    			string += ((EnumContinent) oArray[i]).getDescription().toString();
	    		} else {
	    			string += ((EnumContinent) oArray[i]).getDescription().toString() + ", ";
	    		}
	    	}
    	}
    	if (oArray instanceof String[]) {
	    	for (int i = 0 ; i < oArray.length ; i++) {
	    		if (oArray.length == 1) {
	    			string = oArray[i].toString();
	    		} else if (i == oArray.length-1) {
	    			string += oArray[i].toString();
	    		} else {
	    			string += oArray[i].toString() + ", ";
	    		}
	    	}
    	}
    	return string;
    }

    private static String ratesToString(List<DiscountRateDto> discountRates, String units) {
    	String string 	= "";
    	final DecimalFormat df 	= new DecimalFormat("#,##0.00");
    	for (int i = 0 ; i < discountRates.size() ; i++) {
    		if (discountRates.size() == 1) {
    			string = df.format(discountRates.get(i).getDiscount()) + " % " + "(" + discountRates.get(i).getCount().toString() + " " + units + ")";
    		} else if (i == discountRates.size()-1) {
    			string += df.format(discountRates.get(i).getDiscount()) + " % " + "(" + discountRates.get(i).getCount().toString() + " " + units + ")";
    		} else {
    			string += df.format(discountRates.get(i).getDiscount()) + " % " + "(" + discountRates.get(i).getCount().toString() + " " + units + "), ";
    		}
    	}
    	return string;
    }

    private static String prepaidTiersToString(List<PrePaidTierDto> prepaidTiers, String units) {
    	String string 	= "";
    	final DecimalFormat df 	= new DecimalFormat("#,##0.00");
    	for (int i = 0 ; i < prepaidTiers.size() ; i++) {
    		if (prepaidTiers.size() == 1) {
    			string = df.format(prepaidTiers.get(i).getDiscount()) + " % " + "(" + prepaidTiers.get(i).getCount().toString() + " " + units + ")";
    		} else if (i == prepaidTiers.size()-1) {
    			string += df.format(prepaidTiers.get(i).getDiscount()) + " % " + "(" + prepaidTiers.get(i).getCount().toString() + " " + units + ")";
    		} else {
    			string += df.format(prepaidTiers.get(i).getDiscount()) + " % " + "(" + prepaidTiers.get(i).getCount().toString() + " " + units + "), ";
    		}
    	}
    	return string;
    }

    private Block generateDeliveryMethodBlock(EnumDeliveryMethod deliveryMethod) {
    	Block block = null;
    	String text = "";

		if (deliveryMethod == EnumDeliveryMethod.DIGITAL_PLATFORM) {
    		text = "Delivery will take place via TOPIO within three (3) working days following a proof of payment of the applicable price.";
    		block = generateSingleStylePricingModelBlock(text, NORMAL);
		} else if (deliveryMethod == EnumDeliveryMethod.DIGITAL_PROVIDER) {
    		text = "Delivery will take place via digital delivery directly by Supplier within three (3) working days following a proof of payment of the applicable price.";
    		block = generateSingleStylePricingModelBlock(text, NORMAL);
		} else if (deliveryMethod == EnumDeliveryMethod.PHYSICAL_PROVIDER) {
    		text = "Delivery will take place via physical media delivery directly by Supplier within three (3) working days following a proof of payment of the applicable price.";
    		block = generateSingleStylePricingModelBlock(text, NORMAL);
		}

		return block;
    }

    private Section generatePricingModelRestrictions(Section section, Block block, PricingModel pricingModel) {
    	String text = "";

		if (pricingModel.getDomainRestrictions().length == 0 &&
    		pricingModel.getConsumerRestrictionContinents().length == 0 &&
    		pricingModel.getConsumerRestrictionCountries().length == 0 &&
    		pricingModel.getCoverageRestrictionContinents().length == 0 &&
    		pricingModel.getCoverageRestrictionCountries().length == 0
		) {
            text  = "-" + " No restrictions.";
            block = generateSingleStylePricingModelBlock(text, NORMAL);
            section.getBlocks().add(block);
    	} else {
    		if (pricingModel.getDomainRestrictions().length > 0) {

    			keywords.put("[DomainRestrictions]", restrictionsToString(pricingModel.getDomainRestrictions()));
    	   		text = "-" + " Domain restrictions: " + "[DomainRestrictions].";
    	   		block = generateMultiStylePricingModelBlock(text);
    	   		section.getBlocks().add(block);
    		}
    		if (pricingModel.getConsumerRestrictionContinents().length > 0 && pricingModel.getConsumerRestrictionCountries().length > 0) {
    			keywords.put("[ConsumerRestrictions]", restrictionsToString(pricingModel.getConsumerRestrictionContinents()) + ", " + restrictionsToString(pricingModel.getConsumerRestrictionCountries()));
        		text = "-" + " Consumer restrictions: " + "[ConsumerRestrictions].";
        		block = generateMultiStylePricingModelBlock(text);
        		section.getBlocks().add(block);
    		} else if (pricingModel.getConsumerRestrictionContinents().length > 0) {
    			keywords.put("[ConsumerRestrictions]", restrictionsToString(pricingModel.getConsumerRestrictionContinents()));
        		text = "-" + " Consumer restrictions: " + "[ConsumerRestrictions].";
        		block = generateMultiStylePricingModelBlock(text);
        		section.getBlocks().add(block);
    		} else if (pricingModel.getConsumerRestrictionCountries().length > 0) {
    			keywords.put("[ConsumerRestrictions]", restrictionsToString(pricingModel.getConsumerRestrictionCountries()));
        		text = "-" + " Consumer restrictions: " + "[ConsumerRestrictions].";
        		block = generateMultiStylePricingModelBlock(text);
        		section.getBlocks().add(block);
    		}
    		if (pricingModel.getCoverageRestrictionContinents().length > 0 && pricingModel.getCoverageRestrictionCountries().length > 0) {
    			keywords.put("[CoverageRestrictions]", restrictionsToString(pricingModel.getCoverageRestrictionContinents()) + ", " + restrictionsToString(pricingModel.getCoverageRestrictionCountries()));
        		text = "-" + " Coverage restrictions: " + "[CoverageRestrictions].";
        		block = generateMultiStylePricingModelBlock(text);
        		section.getBlocks().add(block);
    		} else if (pricingModel.getCoverageRestrictionContinents().length > 0) {
    			keywords.put("[CoverageRestrictions]", restrictionsToString(pricingModel.getCoverageRestrictionContinents()));
        		text = "-" + " Coverage restrictions: " + "[CoverageRestrictions].";
        		block = generateMultiStylePricingModelBlock(text);
        		section.getBlocks().add(block);
    		} else if (pricingModel.getCoverageRestrictionCountries().length > 0) {
    			keywords.put("[CoverageRestrictions]", restrictionsToString(pricingModel.getCoverageRestrictionCountries()));
        		text = "-" + " Coverage restrictions: " + "[CoverageRestrictions].";
        		block = generateMultiStylePricingModelBlock(text);
        		section.getBlocks().add(block);
    		}
    	}
		return section;
    }

    private Section generatePricingModelSection(
    		PricingModel pricingModel, EnumDeliveryMethod deliveryMethod, String sectionTitle
        ) {

    	Section section = new Section(sectionTitle);

    	final String partPrice 		= "APPLICABLE PRICE";
    	final String partDelivery		= "DELIVERY";
    	final String partRestriction	= "RESTRICTIONS";

    	/* Fixed with and without updates updates*/
    	if (pricingModel.getPricingModelType() == EnumPricingModel.FIXED) {
    		/* Add applicable price*/
    		Block block = generateSingleStylePricingModelBlock(partPrice, BOLD);
    		section.getBlocks().add(block);

    		String text = pricingModel.getPricingModelDescription();
    		block = generateSingleStylePricingModelBlock(text, NORMAL);
    		section.getBlocks().add(block);

    		keywords.put("[PricingModelPrice]", pricingModel.getTotalPrice());
    		text = "-" +" Price: [PricingModelPrice] Euros.";
    		block = generateMultiStylePricingModelBlock(text);
    		section.getBlocks().add(block);

    		/* Add Delivery*/
    		if (deliveryMethod != null) {
	    		block = generateSingleStylePricingModelBlock(partDelivery, BOLD);
	    		section.getBlocks().add(block);

	    		block = generateDeliveryMethodBlock(deliveryMethod);
	    		section.getBlocks().add(block);
    		}

    		/* Add restrictions*/
    		block = generateSingleStylePricingModelBlock(partRestriction, BOLD);
    		section.getBlocks().add(block);
    		section = generatePricingModelRestrictions(section, block, pricingModel);
    	}

    	/* Fixed per rows*/
    	else if (pricingModel.getPricingModelType() == EnumPricingModel.FIXED_PER_ROWS) {
    		/* Add applicable price*/
    		Block block = generateSingleStylePricingModelBlock(partPrice, BOLD);
    		section.getBlocks().add(block);

    		String text = pricingModel.getPricingModelDescription();
    		block = generateSingleStylePricingModelBlock(text, NORMAL);
    		section.getBlocks().add(block);

    		keywords.put("[PricingModelPricePerRows]", pricingModel.getPricePerRows());
    		text = "-" + " Price per 1000 rows: [PricingModelPricePerRows] Euros.";
    		block = generateMultiStylePricingModelBlock(text);
    		section.getBlocks().add(block);

    		keywords.put("[PricingModelDiscount]", ratesToString(pricingModel.getDiscountRates(), "rows"));
    		text = "-" + " Discount rate applied: [PricingModelDiscount].";
    		block = generateMultiStylePricingModelBlock(text);
    		section.getBlocks().add(block);

    		keywords.put("[PricingModelPrice]", pricingModel.getTotalPrice());
    		text = "-" + " Price: [PricingModelPrice] Euros.";
    		block = generateMultiStylePricingModelBlock(text);
    		section.getBlocks().add(block);

    		/* Add Delivery*/
    		if (deliveryMethod != null) {
	    		block = generateSingleStylePricingModelBlock(partDelivery, BOLD);
	    		section.getBlocks().add(block);

	    		block = generateDeliveryMethodBlock(deliveryMethod);
	    		section.getBlocks().add(block);
    		}

    		/* Add restrictions*/
    		block = generateSingleStylePricingModelBlock(partRestriction, BOLD);
    		section.getBlocks().add(block);
    		section = generatePricingModelRestrictions(section, block, pricingModel);
    	}

    	/* Fixed for population*/
    	else if (pricingModel.getPricingModelType() == EnumPricingModel.FIXED_FOR_POPULATION) {
    		/* Add applicable price*/
    		Block block = generateSingleStylePricingModelBlock(partPrice, BOLD);
    		section.getBlocks().add(block);

    		String text = pricingModel.getPricingModelDescription();
    		block = generateSingleStylePricingModelBlock(text, NORMAL);
    		section.getBlocks().add(block);

    		keywords.put("[PricingModelPricePerPeople]", pricingModel.getPricePerPopulation());
    		text = "-" + " Price per 10.000 people: [PricingModelPricePerPeople] Euros.";
    		block = generateMultiStylePricingModelBlock(text);
    		section.getBlocks().add(block);

    		keywords.put("[PricingModelDiscount]", ratesToString(pricingModel.getDiscountRates(), "people"));
    		text = "-" + " Discount rate applied: [PricingModelDiscount].";
    		block = generateMultiStylePricingModelBlock(text);
    		section.getBlocks().add(block);

    		keywords.put("[PricingModelPrice]", pricingModel.getTotalPrice());
    		text = "-" + " Price: [PricingModelPrice] Euros.";
    		block = generateMultiStylePricingModelBlock(text);
    		section.getBlocks().add(block);

    		/* Add Delivery*/
    		if (deliveryMethod != null) {
	    		block = generateSingleStylePricingModelBlock(partDelivery, BOLD);
	    		section.getBlocks().add(block);

	    		block = generateDeliveryMethodBlock(deliveryMethod);
	    		section.getBlocks().add(block);
    		}

    		/* Add restrictions*/
    		block = generateSingleStylePricingModelBlock(partRestriction, BOLD);
    		section.getBlocks().add(block);
    		section = generatePricingModelRestrictions(section, block, pricingModel);
    	}

    	/* Per call*/
    	else if (pricingModel.getPricingModelType() == EnumPricingModel.PER_CALL) {
    		/* Add applicable price*/
    		Block block = generateSingleStylePricingModelBlock(partPrice, BOLD);
    		section.getBlocks().add(block);

    		String text = pricingModel.getPricingModelDescription();
    		block = generateSingleStylePricingModelBlock(text, NORMAL);
    		section.getBlocks().add(block);

    		keywords.put("[PricingModelPricePerCall]", pricingModel.getPricePerCall());
    		text = "-" + " Price per call: [PricingModelPricePerCall] Euros.";
    		block = generateMultiStylePricingModelBlock(text);
    		section.getBlocks().add(block);

    		if (pricingModel.getDiscountRates() != null) {
    			keywords.put("[PricingModelDiscount]", ratesToString(pricingModel.getDiscountRates(), "calls"));
	    		text = "-" + " Discount rate applied: [PricingModelDiscount].";
	    		block = generateMultiStylePricingModelBlock(text);
	    		section.getBlocks().add(block);
    		}

    		keywords.put("[PrepaidTiers]", prepaidTiersToString(pricingModel.getPrepaidTiers(), "calls"));
    		text = "-" + " Prepaid tiers discount: [PrepaidTiers].";
    		block = generateMultiStylePricingModelBlock(text);
    		section.getBlocks().add(block);

    		keywords.put("[PricingModelPrice]", pricingModel.getTotalPrice());
    		text = "-" + " Price: [PricingModelPrice] Euros.";
    		block = generateMultiStylePricingModelBlock(text);
    		section.getBlocks().add(block);

    		/* Add Delivery*/
    		if (deliveryMethod != null) {
	    		block = generateSingleStylePricingModelBlock(partDelivery, BOLD);
	    		section.getBlocks().add(block);

	    		block = generateDeliveryMethodBlock(deliveryMethod);
	    		section.getBlocks().add(block);
    		}

    		/* Add restrictions*/
    		block = generateSingleStylePricingModelBlock(partRestriction, BOLD);
    		section.getBlocks().add(block);
    		section = generatePricingModelRestrictions(section, block, pricingModel);
    	}

    	/* Per rows*/
    	else if (pricingModel.getPricingModelType() == EnumPricingModel.PER_ROW) {
    		/* Add applicable price*/
    		Block block = generateSingleStylePricingModelBlock(partPrice, BOLD);
    		section.getBlocks().add(block);

    		String text = pricingModel.getPricingModelDescription();
    		block = generateSingleStylePricingModelBlock(text, NORMAL);
    		section.getBlocks().add(block);

    		keywords.put("[PricingModelPricePerRows]", pricingModel.getPricePerRows());
    		text = "-" + " Price per row: [PricingModelPricePerRows] Euros.";
    		block = generateMultiStylePricingModelBlock(text);
    		section.getBlocks().add(block);

    		if (pricingModel.getDiscountRates() != null) {
    			keywords.put("[PricingModelDiscount]", ratesToString(pricingModel.getDiscountRates(), "rows"));
	    		text = "-" + " Discount rate applied: [PricingModelDiscount].";
	    		block = generateMultiStylePricingModelBlock(text);
	    		section.getBlocks().add(block);
    		}

    		keywords.put("[PrepaidTiers]", prepaidTiersToString(pricingModel.getPrepaidTiers(), "rows"));
    		text = "-" + " Prepaid tiers discount: [PrepaidTiers].";
    		block = generateMultiStylePricingModelBlock(text);
    		section.getBlocks().add(block);

    		keywords.put("[PricingModelPrice]", pricingModel.getTotalPrice());
    		text = "-" + " Price: [PricingModelPrice] Euros.";
    		block = generateMultiStylePricingModelBlock(text);
    		section.getBlocks().add(block);

    		/* Add Delivery*/
    		if (deliveryMethod != null) {
	    		block = generateSingleStylePricingModelBlock(partDelivery, BOLD);
	    		section.getBlocks().add(block);

	    		block = generateDeliveryMethodBlock(deliveryMethod);
	    		section.getBlocks().add(block);
    		}

    		/* Add restrictions*/
    		block = generateSingleStylePricingModelBlock(partRestriction, BOLD);
    		section.getBlocks().add(block);
    		section = generatePricingModelRestrictions(section, block, pricingModel);
    	}

    	/* Sentinel hub subscription*/
    	else if (pricingModel.getPricingModelType() == EnumPricingModel.SENTINEL_HUB_SUBSCRIPTION) {
    		/* Add applicable price*/
    		Block block = generateSingleStylePricingModelBlock(partPrice, BOLD);
    		section.getBlocks().add(block);

    		String text = pricingModel.getPricingModelDescription();
    		block = generateSingleStylePricingModelBlock(text, NORMAL);
    		section.getBlocks().add(block);

    		keywords.put("[MonthlyPriceExclTax]", pricingModel.getMonthlyPrice());
    		text = "-" + "Monthly price: [MonthlyPriceExclTax] Euros.";
    		block = generateMultiStylePricingModelBlock(text);
    		section.getBlocks().add(block);

    		keywords.put("[AnnualPriceExclTax]", pricingModel.getAnnualPrice());
    		text = "-" + "Annual price: [AnnualPriceExclTax] Euros.";
    		block = generateMultiStylePricingModelBlock(text);
    		section.getBlocks().add(block);

    		keywords.put("[PricingModelPrice]", pricingModel.getTotalPrice());
    		text = "-" + " Price: [PricingModelPrice] Euros.";
    		block = generateMultiStylePricingModelBlock(text);
    		section.getBlocks().add(block);

    		/* Add Delivery*/
    		if (deliveryMethod != null) {
	    		block = generateSingleStylePricingModelBlock(partDelivery, BOLD);
	    		section.getBlocks().add(block);

	    		block = generateDeliveryMethodBlock(deliveryMethod);
	    		section.getBlocks().add(block);
    		}

    		/* Add restrictions*/
    		block = generateSingleStylePricingModelBlock(partRestriction, BOLD);
    		section.getBlocks().add(block);
    		section = generatePricingModelRestrictions(section, block, pricingModel);
    	}

    	/* Free asset */
    	else if (pricingModel.getPricingModelType() == EnumPricingModel.FREE) {
    		/* Add applicable price*/
    		Block block = generateSingleStylePricingModelBlock(partPrice, BOLD);
    		section.getBlocks().add(block);

    		String text = "Free";
    		block = generateSingleStylePricingModelBlock(text, NORMAL);
    		section.getBlocks().add(block);

    		/* Add Delivery*/
    		if (deliveryMethod != null) {
	    		block = generateSingleStylePricingModelBlock(partDelivery, BOLD);
	    		section.getBlocks().add(block);

	    		block = generateDeliveryMethodBlock(deliveryMethod);
	    		section.getBlocks().add(block);
    		}

    		/* Add restrictions*/
    		block = generateSingleStylePricingModelBlock(partRestriction, BOLD);
    		section.getBlocks().add(block);
    		section = generatePricingModelRestrictions(section, block, pricingModel);
    	}

    	/* Placeholder pricing model */
    	else if (pricingModel.getPricingModelType() == EnumPricingModel.UNDEFINED) {

    		/* Add applicable price*/
    		Block block = generateSingleStylePricingModelBlock(partPrice, BOLD);
    		section.getBlocks().add(block);

    		String text = "[Pricing information]";
    		block = generateSingleStylePricingModelBlock(text, NORMAL);
    		section.getBlocks().add(block);

    		/* Add Delivery*/
	    	block = generateSingleStylePricingModelBlock(partDelivery, BOLD);
	    	section.getBlocks().add(block);

    		text = "[Delivery information]";
	    	block = generateSingleStylePricingModelBlock(text, NORMAL);
	    	section.getBlocks().add(block);

    		/* Add restrictions*/
    		block = generateSingleStylePricingModelBlock(partRestriction, BOLD);
    		section.getBlocks().add(block);
    		//section = generatePricingModelRestrictions(section, block, pricingModel);

    		text = "[Restriction information]";
    		block = generateSingleStylePricingModelBlock(text, NORMAL);
    		section.getBlocks().add(block);
    	}


    	return section;

    }

    private void addBlock(
        RenderContext ctx, Block block, boolean justify, boolean newPage, boolean endOfSection, int blockNo
    ) throws IOException {
        PDPage              page    = ctx.getPage();
        PDPageContentStream content = ctx.getContent();
        final Fonts         fonts   = ctx.getFonts();

        /*
         * Create a new blank page and a contentStream for that page or keep on
         * writing to the current one
         */
        if (newPage) {
            ctx.addPage();
        }

        /* Calculate the starting position */
        final PDRectangle mediaBox = page.getMediaBox();
        final float       marginY  = 72;
        final float       marginX  = 72;
        final float       width    = mediaBox.getWidth() - 2 * marginX;
        final float       startX   = mediaBox.getLowerLeftX() + marginX;
        final float       startY   = mediaBox.getUpperRightY() - marginY;

        /* Split the text into lines based on the block style */
        final List<ParsedLine> parsedLines = parseLines(ctx, block, width);

        /* Get the length of each line - Needed for the justification */
        final Map<Integer, Integer> lengthPerLine = getLengthPerLine(parsedLines);
        /* Get the width of each line - Needed for the justification */
        final Map<Integer, Float> widthPerLine = getWidthPerLine(ctx, parsedLines);

        /* Initialize or get the offset that keeps the page's free space */
        float offsetY;
        if (newPage) {
            offsetY = startY;
        } else {
            offsetY = ctx.getCurrentOffset();
        }

        /* Set starting position and text leading */
        content.beginText();
        content.newLineAtOffset(startX, offsetY);
        content.setLeading(textLeading);

        /*
         * Check if the block consists of only one line. In such a case
         * justification should not be applied
         */
        boolean blockIsOneLine = false;
        if (parsedLines.get(0).lineNumber == parsedLines.get(parsedLines.size() - 1).lineNumber) {
            blockIsOneLine = true;
        }

        final int NumberOfBlockLines = parsedLines.get(parsedLines.size() - 1).lineNumber;

        float   totalOffsetXOfLine     = 0;
        boolean currentBlockisListItem = false;
        for (int i = 0; i < parsedLines.size(); i++) {

            /* Get all needed information for the current line */
            final String currentFont   = parsedLines.get(i).font;
            final int    currentLineNo = parsedLines.get(i).lineNumber;
            final String currentText   = parsedLines.get(i).text;
            final int    currentLength = lengthPerLine.get(currentLineNo);
            final Float  currentWidth  = widthPerLine.get(currentLineNo);

            if (currentText.equals("")) {
                continue;
            }

            /* \u2022 bullet point */
            if (currentText.contains("\u2022")) {
                currentBlockisListItem = true;
            }

            /* If a block is a list item set the appropriate text leading */
            if (!currentBlockisListItem && ctx.isPreviousBlockIsListItem()) {
                content.newLineAtOffset(0, -textLeading / 2);
                offsetY -= (textLeading / 2);
                ctx.setCurrentOffset(offsetY);
            }
            if (currentBlockisListItem) {
                ctx.setPreviousBlockIsListItem(true);
            } else {
                ctx.setPreviousBlockIsListItem(false);
            }

            float size        = 0;
            float charSpacing = 0;
            if (justify) {
                if (currentLength > 1) {
                    final float free = width - currentWidth;
                    /* The last line should have charSpacing = 0 */
                    if (free > 0 && parsedLines.get(i).lineNumber != NumberOfBlockLines && !blockIsOneLine) {
                        charSpacing = free / (currentLength - 1);
                    }
                }
            }

            content.setCharacterSpacing(charSpacing);

            /*
             * Calculate the width of the of the current text This information
             * is needed for the cases where a line contains words with
             * different fonts
             */
            if (currentFont.equals(BOLD) || (currentFont.contains(BOLD) && currentFont.contains(UNDERLINE))) {
                if (justify && !blockIsOneLine) {
                    size = (textFontSize * fonts.getTextBold().getStringWidth(currentText) / 1000) + (currentText.length() * charSpacing);
                } else {
                    size = (textFontSize * fonts.getTextBold().getStringWidth(currentText) / 1000);
                }
                content.setFont(fonts.getTextBold(), textFontSize);
            } else if (currentFont.equals(ITALIC) || (currentFont.contains(ITALIC) && currentFont.contains(UNDERLINE))) {
                if (justify && !blockIsOneLine) {
                    size = (textFontSize * fonts.getTextItalic().getStringWidth(currentText) / 1000) + (currentText.length() * charSpacing);
                } else {
                    size = (textFontSize * fonts.getTextItalic().getStringWidth(currentText) / 1000);
                }
                content.setFont(fonts.getTextItalic(), textFontSize);
            } else if (currentFont.contains(BOLD) && currentFont.contains(ITALIC)) {
                if (justify && !blockIsOneLine) {
                    size = (textFontSize * fonts.getTextBoldItalic().getStringWidth(currentText) / 1000)
                            + (currentText.length() * charSpacing);
                } else {
                    size = (textFontSize * fonts.getTextBoldItalic().getStringWidth(currentText) / 1000);
                }
                content.setFont(fonts.getTextBoldItalic(), textFontSize);
            } else {
                if (justify && !blockIsOneLine) {
                    size = (textFontSize * fonts.getText().getStringWidth(currentText) / 1000) + (currentText.length() * charSpacing);
                } else {
                    size = (textFontSize * fonts.getText().getStringWidth(currentText) / 1000);
                }
                content.setFont(fonts.getText(), textFontSize);
            }

            content.showText(currentText);

            /* Underline if needed */
            if (currentFont.contains(UNDERLINE)) {
                content.endText();
                underlineWord(content, currentText, size, 0.5f, marginX + totalOffsetXOfLine, offsetY, -2);
                content.beginText();
                content.newLineAtOffset(totalOffsetXOfLine + marginX, offsetY);
                content.setCharacterSpacing(charSpacing);
                content.setLeading(textLeading);
            }

            /*
             * If the current and the next text should be written in the same
             * line, move the cursor horizontally after the already written
             * current text
             */
            if (i < parsedLines.size() - 1 && currentLineNo == parsedLines.get(i + 1).lineNumber) {
                content.newLineAtOffset(size, 0);
                totalOffsetXOfLine += size;
            }

            /*
             * Else if the next text should be written in the next line, move
             * the cursor at the beginning of the next row
             */
            else {
                content.newLineAtOffset(-totalOffsetXOfLine, -textLeading);
                /* Initialization for next line */
                totalOffsetXOfLine  = 0;
                offsetY            -= textLeading;
            }

            /* In case of the current page is full */
            if (offsetY <= 72 && !(i < parsedLines.size() - 1 && currentLineNo == parsedLines.get(i + 1).lineNumber)) {

                /* Close the contentStream of the current page */
                content.endText();

                /* Create a new blank page */
                ctx.addPage();

                page    = ctx.getPage();
                content = ctx.getContent();
                content.beginText();

                /* Set font, starting position and text leading */
                if (currentFont.equals(BOLD) || (currentFont.contains(BOLD) && currentFont.contains(UNDERLINE))) {
                    content.setFont(fonts.getTextBold(), textFontSize);
                } else if (currentFont.equals(ITALIC) || (currentFont.contains(ITALIC) && currentFont.contains(UNDERLINE))) {
                    content.setFont(fonts.getTextItalic(), textFontSize);
                } else if (currentFont.contains(BOLD) && currentFont.contains(ITALIC)) {
                    content.setFont(fonts.getTextBoldItalic(), textFontSize);
                } else {
                    content.setFont(fonts.getText(), textFontSize);
                }

                content.newLineAtOffset(startX, startY);
                content.setLeading(textLeading);

                /* Initialize the offset for the new page */
                offsetY = startY;
            }
        }

    	/* When the entire text is written close the open contentStream*/
    	content.endText();
		if (endOfSection == true) {
			ctx.setCurrentOffset(offsetY-20);
		}
		else if (currentBlockisListItem) {
		    ctx.setCurrentOffset(offsetY-7);
		}
		else {
		    ctx.setCurrentOffset(offsetY-textLeading/3);
		}
    }

    private void addTitle(RenderContext ctx, String type, String title, boolean justify, boolean newPage) throws IOException {
        PDPage              page    = ctx.getPage();
        PDPageContentStream content = ctx.getContent();
        final Fonts         fonts   = ctx.getFonts();

        /*
         * Create a new blank page and a contentStream for that page or keep on
         * writing to the current one
         */
        if (newPage || type.equals("Title")) {
            ctx.addPage();
            page    = ctx.getPage();
            content = ctx.getContent();
        }

        /* Calculate the starting position */
        final PDRectangle mediaBox = page.getMediaBox();
        final float       marginY  = 72;
        final float       marginX  = 72;
        final float       width    = mediaBox.getWidth() - 2 * marginX;
        float             startX   = 0;
        if (type.equals("Title")) {
            startX = (mediaBox.getWidth() - fonts.getTitle().getStringWidth(title) * titleFontSize / 1000f) / 2f;
        } else if (type.equals("Subtitle")) {
            startX = (mediaBox.getWidth() - fonts.getSubTitle().getStringWidth(title) * subTitleFontSize / 1000f) / 2f;
        } else if (type.equals("Sectiontitle") || type.equals("SectionSubTitle")) {
            startX = mediaBox.getLowerLeftX() + marginX;
        }
        final float startY = mediaBox.getUpperRightY() - marginY;

        /* Initialize the offset that keeps the page's free space */
        float offsetY;
        if (newPage || type.equals("Title")) {
        	if (type.equals("Title")) {
        		offsetY = startY - 70;
        	} else {
        		offsetY = startY;
        	}
        } else {
            offsetY = ctx.getCurrentOffset();
        }

        /* Set fonts */
        content.beginText();
        if (type.equals("Title")) {
            content.setFont(fonts.getTitle(), titleFontSize);
        } else if (type.equals("Subtitle")) {
            content.setFont(fonts.getSubTitle(), subTitleFontSize);
        } else if (type.equals("Sectiontitle")) {
            content.setFont(fonts.getSectionTitle(), sectionTitleFontSize);
        } else if (type.equals("SectionSubTitle")) {
            content.setFont(fonts.getSectionTitle(), sectionSubTitleFontSize);
        }
        content.newLineAtOffset(startX, offsetY);
        content.setLeading(textLeading);

        /* Read text line by line */
        final List<String> lines = ParseTitle(ctx, title, width);
        for (final String line : lines) {
            float charSpacing = 0;

            /* If alignment is true */
            if (justify) {
                if (line.length() > 1) {
                    float size = 0;
                    if (type.equals("Title")) {
                        size = titleFontSize * fonts.getTitle().getStringWidth(line) / 1000;
                    } else if (type.equals("Subtitle")) {
                        size = subTitleFontSize * fonts.getSubTitle().getStringWidth(line) / 1000;
                    } else if (type.equals("Sectiontitle") || type.equals("SectionSubTitle")) {
                        size = sectionTitleFontSize * fonts.getSectionTitle().getStringWidth(line) / 1000;
                    }
                    final float free = width - size;
                    if (free > 0 && !lines.get(lines.size() - 1).equals(line)) {
                        charSpacing = free / (line.length() - 1);
                    }
                }
            }
            content.setCharacterSpacing(charSpacing);
            /* Write the new line */
            content.showText(line);
            /* Set starting position for the new line */
            content.newLineAtOffset(0, -textLeading);
            /* Reduce the remaining free space of the current page */
            offsetY -= textLeading;

            /* In case of the current page is full */
            if (offsetY <= marginY) {
                /* Close the contentStream of the current page */
                content.endText();
                content.close();

                /* Create a new blank page */
                ctx.addPage();
                page    = ctx.getPage();
                content = ctx.getContent();

                content.beginText();
                /* Set fonts */
                if (type.equals("Title")) {
                    content.setFont(fonts.getTitle(), titleFontSize);
                } else if (type.equals("Subtitle")) {
                    content.setFont(fonts.getSubTitle(), subTitleFontSize);
                } else if (type.equals("Sectiontitle") || type.equals("SectionSubTitle")) {
                    content.setFont(fonts.getSectionTitle(), sectionTitleFontSize);
                }
                content.newLineAtOffset(startX, startY);
                content.setLeading(textLeading);
                /* Initialize the offset for the new page */
                offsetY = startY;
            }
        }

        /* When the entire text is written close the open contentStream */
        content.endText();

        if (type.equals("Title")) {
            ctx.setCurrentOffset(offsetY - textLeading);
        } else if (type.equals("Subtitle")) {
            ctx.setCurrentOffset(offsetY - marginY);
        } else if (type.equals("Sectiontitle")) {
            ctx.setCurrentOffset(offsetY - 6);
        } else if (type.equals("SectionSubTitle")) {
            ctx.setCurrentOffset(offsetY - 6);
        }
    }

    private void addHeaderAndFooter(RenderContext ctx, String contractTitle) throws IOException {
        final PDDocument document    = ctx.getDocument();
        final Fonts      fonts       = ctx.getFonts();
        int              currentPage = 0;

        /* Timestamp for the right part of the header */
        final String timeStamp  = new SimpleDateFormat("dd MMM yyyy - HH:mm:ss").format(new Date()) + " EEST";
        final String headerText = timeStamp;

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
                        centerYHeader + VerticalOffset("top", pageHeight) - 23));
                /* Write */
                contentStream.showText(headerText);
                contentStream.endText();

                /* Create the PDFImageXObject object to add the topio logo */
                final PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, ctx.getLogo(), logoFilename);
                contentStream.drawImage(pdImage, 72, centerYHeader + VerticalOffset("top", pageHeight) - 27, 37, 14);
                drawLine(contentStream, 0.5f, 72, centerYHeader + VerticalOffset("top", pageHeight) - 23, -8, pageWidth - 72);

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

    private List<Block> getOptionSuboptionBody(ArrayNode blocks) throws JsonMappingException, JsonProcessingException {
        final ArrayList<Block> allBlocks = new ArrayList<Block>();

        for (int i = 0, size = blocks.size(); i < size; i++) {
            int            position   = 0;
            Block          block      = null;
            boolean        isListItem = false;
            final JsonNode jobj       = blocks.get(i);

            /* Get type of the current block */
            final String type = jobj.get("type").asText();

            /* Delete characters that cannot been written in the PDF */
            if (type.equals("unordered-list-item")) {
                block      = new Block("\u2022" + " " + jobj.get("text").asText().replace("\n", "").replace("\r", "").replace("\u00A0", ""));
                isListItem = true;
            } else {
                block = new Block(jobj.get("text").asText().replace("\n", "").replace("\r", "").replace("\u00A0", "").replace("“", "\"")
                        .replace("”", "\""));
            }

            /* Get styles of the current block */
            final ArrayNode styles = (ArrayNode) jobj.get("inlineStyleRanges");

            /* If there is no style for the current text */
            if (styles.isEmpty()) {
                final BlockStyle blockStyle1 = new BlockStyle(0, block.getText().length(), NORMAL);
                block.addBlockStyle(blockStyle1);
                position = position + blockStyle1.getLength();
            } else {
                /* For all styles of the current block */
                for (int j = 0, length = styles.size(); j < length; j++) {
                    final JsonNode style = styles.get(j);

                    /* Ignore styles. We always use the default ones */
                    if (style.get("style").asText().contains("fontsize") ||
                        style.get("style").asText().contains("color")    ||
                        style.get("style").asText().contains("fontfamily")) {
                        continue;
                    } else if (style.get("offset").asInt() == position) {
                        final ObjectMapper m           = new ObjectMapper();
                        final BlockStyle   blockStyle1 = m.readValue(style.toString(), BlockStyle.class);
                        block.addBlockStyle(blockStyle1);
                        position = position + blockStyle1.getLength();
                    } else if (style.get("offset").asInt() > position) {
                        /* First add/create normal style */
                        BlockStyle blockStyle1 = new BlockStyle(position, style.get("offset").asInt() - position, NORMAL);
                        block.addBlockStyle(blockStyle1);
                        position = style.get("offset").asInt();

                        final ObjectMapper m = new ObjectMapper();
                        blockStyle1 = m.readValue(style.toString(), BlockStyle.class);
                        block.addBlockStyle(blockStyle1);
                        position = position + blockStyle1.getLength();

                    } else if (style.get("offset").asInt() < position) {
                        /*
                         * We need to add style before without changing position
                         */
                        final ObjectMapper m           = new ObjectMapper();
                        final BlockStyle   blockStyle1 = m.readValue(style.toString(), BlockStyle.class);
                        block.addBlockStyle(blockStyle1);
                    }
                }

                /* If there is more normal text at the end */
                if (block.getText().trim().length() > position) {
                    final BlockStyle blockStyle1 = new BlockStyle(position, block.getText().length() - position, NORMAL);
                    block.addBlockStyle(blockStyle1);
                }
            }

            /*
             * If the current block is list item add block style for the bullet
             * point
             */
            if (isListItem) {
                for (int j = 0; j < block.getBlockStyles().size(); j++) {
                    final int        newOffset     = block.getBlockStyles().get(j).getOffset() + 2;
                    final int        newLength     = block.getBlockStyles().get(j).getLength()/* + 2*/;
                    final String     style         = block.getBlockStyles().get(j).getStyle();
                    final BlockStyle newBlockStyle = new BlockStyle(newOffset, newLength, style);
                    block.getBlockStyles().set(j, newBlockStyle);
                }
                final BlockStyle bulletBlockStyle = new BlockStyle(0, 2, NORMAL);
                block.getBlockStyles().add(0, bulletBlockStyle);
            }

            allBlocks.add(block);
        }
        return allBlocks;
    }

    public List<BlockStyle> getBlockStylesSorted(List<BlockStyle> blockstyles) {
    	return blockstyles.stream().sorted((b1,b2) -> {
    		/* Get index of each section*/
    		final int offset1	=	b1.getOffset();
    		final int offset2	=	b2.getOffset();

    		if (offset1 > offset2) {
    			return 1;
    		}
    		else if (offset1 < offset2) {
    			return -1;
    		}
    		return 0;
    	}).collect(Collectors.toList());
    }

    @Override
    public byte[] renderConsumerPDF(ContractParametersDto contractParametersDto, ConsumerContractCommand command) throws IOException {
        final UUID             		orderKey        = command.getOrderKey();
        final OrderEntity      		orderEntity     = orderRepository.findByKey(orderKey).get();
        final OrderItemEntity  		orderItemEntity = orderEntity.getItems().get(0);
        final AccountEntity    		provider        = orderItemEntity.getProvider();
        final EnumContractType 		contractType    = orderItemEntity.getContractType();
        final Integer          		contractId      = orderItemEntity.getContractTemplateId();
        final String           		contractVersion = orderItemEntity.getContractTemplateVersion();
        final EnumDeliveryMethod 	deliveryMethod	= orderEntity.getDeliveryMethod();

        Assert.isTrue(contractType == EnumContractType.MASTER_CONTRACT, "Expected contract type to be MASTER_CONTRACT");
        // Get contract
        final ProviderTemplateContractHistoryEntity contract = contractRepository
    		.findByIdAndVersion(provider.getKey(), contractId, contractVersion)
    		.get();

        final byte[] result = renderPDF(contractParametersDto, command.isDraft(), contract, null, orderKey, deliveryMethod);

    	return result;
    }

    @Override
    public byte[] renderProviderPDF(ContractParametersDto contractParametersDto, ProviderContractCommand command) throws IOException {
        final ProviderTemplateContractHistoryEntity contract = contractRepository
            .findByKey(command.getProviderKey(), command.getContractKey())
            .get();

        final byte[]             result         = renderPDF(contractParametersDto, false, contract, null, null, null);

        return result;
    }

    @Override
    public byte[] renderMasterPDF(ContractParametersDto contractParametersDto, int masterContractId)
            throws IOException {
        final MasterContractHistoryEntity masterContract = masterContractRepository.findOneByActiveAndId(masterContractId).orElse(null);
        final byte[] result = renderPDF(contractParametersDto, false, null, masterContract, null, null);

        return result;
    }

    private byte[] renderPDF(
        ContractParametersDto contractParametersDto, boolean draft,
        ProviderTemplateContractHistoryEntity contract, MasterContractHistoryEntity masterContract,
        UUID orderKey, EnumDeliveryMethod deliveryMethod
    ) throws IOException {
        // Initialize all variables that are related to the PDF formatting
        final PDDocument          document = new PDDocument();
        final Map<String, String> keywords = this.createKeywordMapping();
        Fonts                     fonts;
        byte[]                    logo;

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

            List<Section>  allSections  = new ArrayList<Section>();
            final String title, subtitle;
            if (masterContract == null) {
                /* Get title and subtitles */
                title    = contract.getTitle();
                subtitle = ((orderKey != null ? "ORDER ID " + "(" + orderKey + ")" : contract.getSubtitle()));

                /* Get all sections sorted by index */
                final List<ProviderTemplateSectionHistoryEntity> sortedSections = contract.getSectionsSorted();
                allSections = addProviderSections(contractParametersDto, sortedSections, deliveryMethod);
            }
            else {

                /* Get title and subtitles */
                title    = masterContract.getTitle();
                subtitle = masterContract.getSubtitle();

                /* Get all sections sorted by index */
                final List<MasterSectionHistoryEntity> sortedSections = masterContract.getSectionsSorted();
                allSections = addMasterSections(contractParametersDto, sortedSections);
            }

            for (final Section section  : allSections) {
                for (final Block block : section.getBlocks()) {
                	final ArrayList<BlockStyle> sortedBlockStyles = (ArrayList<BlockStyle>) getBlockStylesSorted(block.getBlockStyles());
                	block.setBlockStyles(sortedBlockStyles);
                }
            }

            /* Create the combined fonts */
            for (final Section section : allSections) {
                for (final Block block : section.getBlocks()) {
                    for (int j = 0; j < block.getBlockStyles().size(); j++) {
                        if (j > 0 && block.getBlockStyles().get(j).getOffset() == block.getBlockStyles().get(j - 1).getOffset()) {
                            block.getBlockStyles().get(j - 1).setStyle(
                                    block.getBlockStyles().get(j - 1).getStyle() + "-" + block.getBlockStyles().get(j).getStyle());
                            block.getBlockStyles().remove(j);
                        }
                    }
                }
            }

            /*
             * If the contract type is a user contract, rebuild all blocks and
             * block styles with the provider, consumer and product information
             */
            createKeywordMapping();
            allSections = addOrderInformation(allSections, contractParametersDto, keywords);

            /* Add contract title and subtitle */
            addTitle(ctx, "Title", title, true, true);
            if (subtitle != null) {
                addTitle(ctx, "Subtitle", subtitle, true, false);
            } else {
                addTitle(ctx, "Subtitle", "", true, false);
            }

            /* For all sections */
            for (final Section section : allSections) {
                ctx.setPreviousBlockIsListItem(false);

                /* Add section title */
                final String sectionTitle = section.getSectionTitle();

                final int occurrenceOfDots = StringUtils.countOccurrencesOf(sectionTitle, ".");
                if (occurrenceOfDots == 0) {
                	if (ctx.getCurrentOffset() <= 72) {
                		addTitle(ctx, "Sectiontitle", sectionTitle, true, true);
                	} else {
                		addTitle(ctx, "Sectiontitle", sectionTitle, true, false);
                	}

                } else {
                	if (ctx.getCurrentOffset() <= 72) {
                		addTitle(ctx, "SectionSubTitle", sectionTitle, true, true);
                	} else {
                		addTitle(ctx, "SectionSubTitle", sectionTitle, true, false);
                	}
                }

                /* Add blocks */
                for (int i = 0; i < section.getBlocks().size(); i++) {
                    final Block block = section.getBlocks().get(i);

                    if (section.getBlocks().size() == 1 && (block.getText().equals(" ") || block.getText().equals("") || block.getText().equals("\n"))) {
                        continue;
                    } else if (i != section.getBlocks().size() - 1) {
                        addBlock(ctx, block, true, false, false, i);
                    } else {
                        addBlock(ctx, block, true, false, true, i);
                    }
                }
            }

            /* Add metadata, header and footer */
            addMetadata(ctx);
            if (orderKey != null) {
            	addHeaderAndFooter(ctx, title + " " + "(" + orderKey + ")");
            } else {
            	addHeaderAndFooter(ctx, title);
            }

            // Release RenderContext before closing the document
            ctx.close();

            // Optionally add an overlay page
            try (
                final Overlay     overlay     = draft ? new Overlay() : null;
            	final InputStream watermarkIs = resourceLoader.getResource(watermark).getInputStream();
            ) {
                // Add watermark if draft
                if (draft) {
                    final PDDocument defaultOverlayPDF = PDDocument.load(watermarkIs);

                    overlay.setInputPDF(document);
                    overlay.setOverlayPosition(Overlay.Position.BACKGROUND);
                    overlay.setDefaultOverlayPDF(defaultOverlayPDF);
                    overlay.overlay(new HashMap<Integer, String>());
                }

                try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                    document.save(byteArrayOutputStream);
                    document.close();

                    // Close overlay if exists. Overlay must be closed after
                    // releasing the document resources
                    if (overlay != null) {
                        overlay.close();
                    }

                    return byteArrayOutputStream.toByteArray();
                } finally {
                    if (document != null) {
                        document.close();
                    }
                }
            }
        }
    }


    ArrayList<Section> addProviderSections(ContractParametersDto contractParametersDto,
            List<ProviderTemplateSectionHistoryEntity> sortedSections,
            EnumDeliveryMethod deliveryMethod
            )
            throws JsonMappingException, JsonProcessingException {
        final ArrayList<Section> allSections = new ArrayList<Section>();
        String prevIndex = "0";
        String sectionIndex;
        for (final ProviderTemplateSectionHistoryEntity section : sortedSections) {

            /* If section is not selected continue */
            if (section.isOptional()) {
                continue;
            }

            final MasterSectionHistoryEntity masterSection = section.getMasterSection();

            /* Get Title and Index */
            /* Find proper index in case of previous omitted sections */
            final String[] prevIndexArray = prevIndex.split("\\.");
            final String[] currIndexArray = masterSection.getIndex().split("\\.");

            if (prevIndex != "0" && currIndexArray.length > 1 && currIndexArray.length == prevIndexArray.length
                    && Integer.parseInt(currIndexArray[currIndexArray.length - 1]) > Integer
                            .parseInt(prevIndexArray[prevIndexArray.length - 1]) + 1) {
                currIndexArray[currIndexArray.length - 1] = ""
                        + (Integer.parseInt(prevIndexArray[prevIndexArray.length - 1]) + 1);
                sectionIndex = String.join(".", currIndexArray);
            } else {
                sectionIndex = masterSection.getIndex();
            }
            final String sectionTitle = masterSection.getTitle();

            Section providerSection = null;
            if (sectionTitle != null && !sectionTitle.isEmpty()) {
                providerSection = new Section("Section " + sectionIndex + " - " + sectionTitle);
            } else {
                providerSection = new Section("Section " + sectionIndex);
            }

            if (sectionTitle != null && !sectionTitle.isEmpty()
                    && sectionTitle.toUpperCase().contains("PRICING MODEL")) {
                providerSection = generatePricingModelSection(contractParametersDto.getPricingModel(), deliveryMethod,
                        "Section " + sectionIndex + " - " + sectionTitle);
                allSections.add(providerSection);
                prevIndex = sectionIndex;
                continue;
            }

            String optionJson, subOptionJson;
            optionJson = masterSection.getOptions().get(section.getOption()).getBody();
            final List<ContractSectionSubOptionDto> suboptions = masterSection.getOptions().get(section.getOption())
                    .getSubOptions();
            JsonNode obj = objectMapper.readTree(optionJson);

            /* Get blocks */
            ArrayNode blocks = (ArrayNode) obj.get("blocks");
            final List<Block> allBlocks = getOptionSuboptionBody(blocks);

            /* Add sub option block separately if any exists */
            if (!CollectionUtils.isEmpty(suboptions) && !CollectionUtils.isEmpty(section.getSubOption())) {
                for (int i = 0; i < section.getSubOption().size(); i++) {
                    subOptionJson = suboptions.get(section.getSubOption().get(i)).getBody();
                    obj = objectMapper.readTree(subOptionJson);
                    blocks = (ArrayNode) obj.get("blocks");
                    final List<Block> suboptionBlocks = getOptionSuboptionBody(blocks);
                    allBlocks.addAll(suboptionBlocks);
                }
            }

            providerSection.setBlocks(allBlocks);
            allSections.add(providerSection);

            prevIndex = sectionIndex;
        }
        return allSections;
    }

    ArrayList<Section> addMasterSections(ContractParametersDto contractParametersDto,
            List<MasterSectionHistoryEntity> sortedSections)
            throws JsonMappingException, JsonProcessingException {
        final ArrayList<Section> allSections = new ArrayList<Section>();
        String sectionIndex;
        for (final MasterSectionHistoryEntity section : sortedSections) {

            final List<Block> allBlocks = new ArrayList<Block>();

            /* Get Title and Index */

            final String sectionTitle = section.getTitle();
            sectionIndex = section.getIndex();

            Section masterSection = null;
            if (sectionTitle != null && !sectionTitle.isEmpty()) {
                masterSection = new Section("Section " + sectionIndex + " - " + sectionTitle);
            } else {
                masterSection = new Section("Section " + sectionIndex);
            }

            String optionJson, subOptionJson;
            final int optionsSize = section.getOptions().size();
            for(int i = 0; i < optionsSize; i++) {
                if(optionsSize > 1) {
                    final Block b = new Block("Option " + (i+1));
                    b.addBlockStyle(new BlockStyle(0, 8, BOLD));
                    allBlocks.add(b);
                }
                optionJson = section.getOptions().get(i).getBody();
                JsonNode obj = objectMapper.readTree(optionJson);
                final List<ContractSectionSubOptionDto> subOptions = section.getOptions().get(i).getSubOptions();

                /* Get blocks */
                ArrayNode blocks = (ArrayNode) obj.get("blocks");
                allBlocks.addAll(getOptionSuboptionBody(blocks));
                /* Add sub options separately if any exists */
                if (!CollectionUtils.isEmpty(subOptions)) {
                    for (int j = 0; j < subOptions.size(); j++) {
                        if(subOptions.size() > 1) {
                            final Block c = new Block("Suboption " + (j+1));
                            c.addBlockStyle(new BlockStyle(0, 11, BOLD));
                            allBlocks.add(c);
                        }

                        subOptionJson = subOptions.get(j).getBody();
                        obj = objectMapper.readTree(subOptionJson);
                        blocks = (ArrayNode) obj.get("blocks");
                        final List<Block> suboptionBlocks = getOptionSuboptionBody(blocks);
                        allBlocks.addAll(suboptionBlocks);
                    }
                }

            }
            masterSection.setBlocks(allBlocks);
            allSections.add(masterSection);

        }
        return allSections;
    }
}
