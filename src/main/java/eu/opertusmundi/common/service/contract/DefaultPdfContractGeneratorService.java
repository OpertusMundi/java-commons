package eu.opertusmundi.common.service.contract;


import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

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
import org.elasticsearch.index.mapper.SearchAsYouTypeFieldMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.MasterSectionHistoryEntity;
import eu.opertusmundi.common.domain.OrderEntity;
import eu.opertusmundi.common.domain.OrderItemEntity;
import eu.opertusmundi.common.domain.ProviderTemplateContractHistoryEntity;
import eu.opertusmundi.common.domain.ProviderTemplateSectionHistoryEntity;
import eu.opertusmundi.common.model.contract.ContractParametersDto;
import eu.opertusmundi.common.model.contract.ContractSectionSubOptionDto;
import eu.opertusmundi.common.model.contract.EnumContract;
import eu.opertusmundi.common.model.contract.consumer.PrintConsumerContractCommand;
import eu.opertusmundi.common.repository.OrderRepository;
import eu.opertusmundi.common.repository.contract.ProviderTemplateContractHistoryRepository;
import lombok.Getter;
import lombok.Setter;

@Service
@Getter
@Setter
public class DefaultPdfContractGeneratorService implements PdfContractGeneratorService{
	
    @Autowired
    private ResourceLoader resourceLoader;
  
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
	private ProviderTemplateContractHistoryRepository	contractRepository;
    
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
	
	private static byte[] 					logo;
	private static PDDocument 				document;
	private static PDDocumentInformation	pdd;
	
	private PDPageContentStream currentPDPageContentStream;
	private PDPage 				currentPage;
	private float 				currentOffset;
	private boolean				previousBlockIsListItem;
	
	private static PDFont	titleFont;
	private static float 	titleFontSize;
	private static PDFont 	subTitleFont;
	private static float 	subTitleFontSize;
	private static PDFont 	sectionTitleFont;
	private static float 	sectionTitleFontSize;
	private static float 	sectionSubTitleFontSize;
	private static PDFont 	textFont;
	private static PDFont	textBoldFont;
	private static PDFont	textItalicFont;
	private static PDFont	textBoldItalicFont;
	private static float 	textFontSize;
	private static PDFont 	headerFont;
	private static float 	headerFontSize;
	private static PDFont	footerFont;
	private static float 	footerFontSize;
	private static Color 	color;
	private static Color 	color2;
	private static float 	textLeading;	
	
	private static Map<String, String> keywords;
	
	@Getter
	@Setter
	private static class ParsedLine {
		private int lineNumber;
		private String text;
		private String font;
		
		public ParsedLine(int lineNumber, String text, String font, float lineSize) {
			this.lineNumber = lineNumber;
			this.text = text;
			this.font = font;
		}
	}
	
	@Getter
	@Setter
	private static class Section {		
		private String sectionTitle;
		private ArrayList<Block> blocks;
		
		public Section(String sectionTitle) {
			this.sectionTitle = sectionTitle;
			blocks = new ArrayList<Block>();
		}
	}
	
	@Getter
	@Setter
	private static class Block {
		private String text;
		private ArrayList<BlockStyle> blockStyles;

		public Block(String text) {
			this.text = text;
			blockStyles = new ArrayList<BlockStyle>();
		}

		public void addBlockStyle(BlockStyle blockStyle) {
			this.blockStyles.add(blockStyle);
		}
	}
	
	@Getter
	@Setter
	private static class BlockStyle {
		private int 	offset;
		private int 	length;
		private String 	style;
		
		public BlockStyle() {		
		}
		
		public BlockStyle(int offset, int length, String style) {
			this.offset = offset;
			this.length = length;
			this.style 	= style;	
		}
	}

	public void init() throws IOException {
		/* Initialize all variables that are related to the pdf formatting*/
		document 				= new PDDocument(); 
		
		Properties prop 		= new Properties();
		String propFileName 	= "/config/application.properties";
		InputStream inputStream = getClass().getResourceAsStream(propFileName);	 
		if (inputStream != null) {
			prop.load(inputStream);
		} else {
			throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
		}
		
		try (
			InputStream logoIs 				= resourceLoader.getResource(logoFilename).getInputStream();
			InputStream regularFontIs 		= resourceLoader.getResource(regularFont).getInputStream();
			InputStream boldFontIs 			= resourceLoader.getResource(boldFont).getInputStream();
			InputStream italicFontIs 		= resourceLoader.getResource(italicFont).getInputStream();
			InputStream boldItalicFontIs 	= resourceLoader.getResource(boldItalicFont).getInputStream();				
		) {
			logo = logoIs.readAllBytes();		
			titleFont 			= subTitleFont	=	sectionTitleFont	=	textBoldFont	=	PDType0Font.load(document, boldFontIs);
			textFont 			= headerFont 	= 	footerFont 			= 	PDType0Font.load(document, regularFontIs);
			textItalicFont		= PDType0Font.load(document, italicFontIs);
			textBoldItalicFont	= PDType0Font.load(document, boldItalicFontIs);	
		}
		
		titleFontSize 					= 32.0f;
		subTitleFontSize 				= 20.0f;
		sectionTitleFontSize 			= 12.0f;
		sectionSubTitleFontSize			= 10.0f;
		textFontSize 					= 9.0f;
		headerFontSize 					= 7.0f;
		footerFontSize 					= 7.0f;
		color 							= Color.BLACK;
		color2							= Color.BLUE;
		textLeading 					= 1.5f*textFontSize;	
		this.currentPDPageContentStream	= null;
		this.currentPage 				= null;
		this.currentOffset 				= -100;
	}
	
	public void addMetadata() {
		pdd =  document.getDocumentInformation();
		pdd.setAuthor("Topio Market");       
		pdd.setTitle("Topio Market Contract"); 
		pdd.setCreator("Topio Market"); 
		pdd.setSubject("Contract"); 
	    pdd.setKeywords("topio, contract, pdf documnet"); 	
	}
	
	private static String subStringUsingLength(String myString, int start, int length) {
	    return myString.substring(start, Math.min(start + length, myString.length()));
	}
	
	private static float HorizontalOffset(String move, float stringWidth, float pageWidth) {
		float offset = 0;
		if (move.equals("left")) {
			offset = -pageWidth/2+stringWidth;
		}
		else if (move.equals("right")) {
			offset = +pageWidth/2-stringWidth;
		}
		return offset;
	}

	private static void createKeywordMapping() {
		keywords = new HashMap<String, String>();
		keywords.put("[sellerName]",				"Corporate name");
		keywords.put("[sellerAddress]", 			"Professional address");
		keywords.put("[sellerEmail]", 				"Contact email");
		keywords.put("[sellerContactPerson]",		"Contact person");
		keywords.put("[sellerCompanyRegNumber]", 	"Company registration number");
		keywords.put("[sellerVAT]", 				"EU VAT number");
		keywords.put("[clientName]", 				"Corporate name");
		keywords.put("[clientAddress]", 			"Professional address");
		keywords.put("[clientEmail]", 				"Contact email");
		keywords.put("[clientContactPerson]", 		"Contact person");
		keywords.put("[clientCompanyRegNumber]", 	"Company registration number");
		keywords.put("[clientVAT]", 				"EU VAT number");
		keywords.put("[ProductId]", 				"Product ID");
		keywords.put("[ProductName]", 				"Product Name");
		keywords.put("[ProductDescription]", 		"Product description");
		keywords.put("[PastVersionsIncluded]", 		"Past Versions included");
		keywords.put("[UpdatesIncluded]", 			"Updates included");
		keywords.put("[EstimatedDeliveryDate]", 	"Estimated delivery date");
		keywords.put("[DeliveryMediaFormat]", 		"Media and format of delivery");
		keywords.put("[ApplicableFees]", 			"Applicable fees");
		keywords.put("[INSERT HYPERLINK TO TOPIO’S T&CS]", "https://beta.topio.market/");
		keywords.put("[Date]", new SimpleDateFormat("dd MMM yyyy").format(new Date()));
		keywords.put("[Years]", "2" + " Years");
	}
	
	private static float VerticalOffset(String move, float pageHeight) {
		float offset = 0;
		if (move.equals("top")) {
			offset = +pageHeight/2-20;
		}
		else if (move.equals("bottom")) {
			offset = -pageHeight/2+15;
		}
		return offset;
	}
	
	private static HashMap<Integer, Integer> getLengthPerLine(List<ParsedLine> parsedLines){
		/* Find the length of each line, needed for the justification
		 * Return a hashmap where key is the line number and value is the length of the line*/
    	int currentLineNumber	= 0;
    	int currentLength		= 0;
    	HashMap<Integer, Integer> LineLengths = new HashMap<Integer, Integer>();
    	for (int i = 0 ; i < parsedLines.size() ; i++) {
    		int lineNumber 	= parsedLines.get(i).lineNumber;
    		String text		= parsedLines.get(i).text;
    		if (lineNumber == currentLineNumber) {
    			currentLength += text.length();
    		}
    		else {
    			LineLengths.put(currentLineNumber, currentLength);
    			currentLineNumber 	= lineNumber;
    			currentLength 		= text.length();
    		}	
    	}
    	LineLengths.put(currentLineNumber, currentLength);
    	return LineLengths;
	}
	
	private static HashMap<Integer, Float> getWidthPerLine(List<ParsedLine> parsedLines) throws IOException {
		/* Find the width of each line, needed for the justification
		 * Return a hashmap where key is the line number and value is the width of the line */
	   	int currentLineNumber	= 0;
    	float currentWidth		= 0;
    	HashMap<Integer, Float> LineWidths = new HashMap<Integer, Float>();
    	for (int i = 0 ; i < parsedLines.size() ; i++) {
        	String font 	= parsedLines.get(i).font;
        	int lineNumber 	= parsedLines.get(i).lineNumber;
        	String text		= parsedLines.get(i).text;
  			float size;
        	if (font.equals("BOLD") || (font.contains("BOLD") && font.contains("UNDERLINE"))) {
        		size = textFontSize*textBoldFont.getStringWidth(text)/1000;
        	}
        	else if (font.equals("ITALIC") || (font.contains("ITALIC") && font.contains("UNDERLINE"))) {
        		size = textFontSize*textItalicFont.getStringWidth(text)/1000;
        	}
        	else if (font.contains("BOLD") && font.contains("ITALIC")) {
        		size = textFontSize*textBoldItalicFont.getStringWidth(text)/1000;
        	}
        	else {
        		size = textFontSize*textFont.getStringWidth(text)/1000;    	
        	}
    		if (lineNumber == currentLineNumber) {
    			currentWidth += size;
    		}
    		else {
    			LineWidths.put(currentLineNumber, currentWidth);
    			currentLineNumber 	= lineNumber;
    			currentWidth 		= size;
    		}	
    	}
    	LineWidths.put(currentLineNumber, currentWidth);
    	return LineWidths;
	}
	
	public void underlineWord(PDPageContentStream contentStream, String text, float size, float lineWidth, float sx, float sy, float linePosition) throws IOException {
	    contentStream.setLineWidth(lineWidth);
	    contentStream.moveTo(sx, sy + linePosition);
	    contentStream.lineTo(sx + size, sy + linePosition);
	    contentStream.stroke();
	}
		
	public void drawLine(PDPageContentStream contentStream, float lineWidth, float sx, float sy, float linePosition, float pageWidth) throws IOException {
		/* Needed for header and footer*/
	    contentStream.setLineWidth(lineWidth);
	    contentStream.moveTo(sx, sy + linePosition);
	    contentStream.lineTo(pageWidth, sy + linePosition);
	    contentStream.stroke();
	}

	private static List<String> ParseTitle(String text, float width) throws IOException {
	  List<String> lines = new ArrayList<String>();
	  int lastSpace = -1;
	  while (text.length() > 0) {
	      int spaceIndex = text.indexOf(' ', lastSpace+1);
	      if (spaceIndex < 0) {
	          spaceIndex = text.length();
	      }
	      String subString = text.substring(0, spaceIndex);
	      float size = textFontSize * textFont.getStringWidth(subString)/1000;
	      if (size > width) {
	          if (lastSpace < 0){
	              lastSpace = spaceIndex;
	          }
	          subString = text.substring(0, lastSpace);
	          lines.add(subString);
	          text = text.substring(lastSpace).trim();
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
	
    private static List<ParsedLine> parseLines(Block block, float width) throws IOException {
    	/* Get the text of the block*/
        String text 			= block.getText();
    	List<ParsedLine> lines 	= new ArrayList<ParsedLine>();
        
    	/* Initialize variables*/
        int lineCounter 	= 0;
        String currentLine 	= "";
        float totalSize 	= 0;
        
        /* For all blocks*/
        for (int i = 0 ; i < block.getBlockStyles().size() ; i++) {
        	
        	/* Get information of each block*/
        	BlockStyle 	blockStyle = block.getBlockStyles().get(i);
        	int offset		= blockStyle.getOffset();
        	int length		= blockStyle.getLength();
        	String style	= blockStyle.getStyle();
        	
        	/* Calculate the size of the current substring*/
        	float currentSize = 0;
        	String currentSubstring = subStringUsingLength(text, offset, length);
        	
   	       	if (style.contains("BOLD") || (style.contains("BOLD") && style.contains("UNDERLINE"))) {
        		currentSize = textFontSize*textBoldFont.getStringWidth(currentSubstring)/1000;
   	       	}
        	else if (style.contains("ITALIC") || (style.contains("ITALIC") && style.contains("UNDERLINE"))) {
        		currentSize = textFontSize*textItalicFont.getStringWidth(currentSubstring)/1000;
        	}
        	else if (style.contains("BOLD") && style.contains("ITALIC")) {
        		currentSize = textFontSize*textBoldItalicFont.getStringWidth(currentSubstring)/1000;
        	}
        	else {
        		currentSize = textFontSize*textFont.getStringWidth(currentSubstring)/1000; 
        	}
        	
        	/* If the current substring fits into the current line*/
        	if (totalSize + currentSize <= width) {
        		
        		/* Append the current line*/
        		currentLine	+= currentSubstring;
        		totalSize 	+= currentSize;
        		ParsedLine parsedLine = new ParsedLine(lineCounter, currentLine, style, currentSize);
        		lines.add(parsedLine);

        		/* The next block information will be written into a new line*/
    			currentLine = "";
        		if (totalSize == width)  {
        			totalSize = 0;
        			lineCounter++;
        		}        		
        	}
        	
        	/* If the current substring does not fit entirely into the current line, append the current line word by word until it is full*/
        	else if (totalSize + currentSize > width) {
        		
        		/* Initialize last space position*/
        		int lastSpace = -1;
 
        		/* While the current substring has not been entirely parsed*/
        		while (currentSubstring.length() > 0) {
        			/* Find the position of the first(first loop)/next space*/
        			int spaceIndex = currentSubstring.indexOf(' ', lastSpace+1);
        			/* If there are no spaces the current substring is a word*/
        			if (spaceIndex < 0) {
        		          spaceIndex = currentSubstring.length();
        			}
        			/* Get the word*/
        			String currentWord = currentSubstring.substring(0, spaceIndex);
        			
                	/* Calculate the size of the current word*/
                	float currentWordSize = 0;
           	       	if (style.contains("BOLD") || (style.contains("BOLD") && style.contains("UNDERLINE"))) {
           	       		currentWordSize = textFontSize*textBoldFont.getStringWidth(currentWord)/1000;
           	       	}
                	else if (style.contains("ITALIC") || (style.contains("ITALIC") && style.contains("UNDERLINE"))) {
                		currentWordSize = textFontSize*textItalicFont.getStringWidth(currentWord)/1000;
                	}
                	else if (style.contains("BOLD") && style.contains("ITALIC")) {
                		currentWordSize = textFontSize*textBoldItalicFont.getStringWidth(currentWord)/1000;
                	}
                	else {
                		currentWordSize = textFontSize*textFont.getStringWidth(currentWord)/1000;  
                	}
                	
        		    /* If the current word does not fit in the current line*/
                	if (totalSize + currentWordSize > width) {
                		
                		/* If the last space is not defined, then last space will be the current one*/
                		if (lastSpace < 0){
                			lastSpace = spaceIndex;
                		}
                		/* Keep the substring until the last space until now (Exclude the last word that does not fit in the line)*/
                		currentWord = currentSubstring.substring(0, lastSpace);
                    	/* Calculate the size of the current word*/
               	       	if (style.contains("BOLD") || (style.contains("BOLD") && style.contains("UNDERLINE"))) {
               	       		currentWordSize = textFontSize*textBoldFont.getStringWidth(currentWord)/1000;
               	       	}
                    	else if (style.contains("ITALIC") || (style.contains("ITALIC") && style.contains("UNDERLINE"))) {
                    		currentWordSize = textFontSize*textItalicFont.getStringWidth(currentWord)/1000;
                    	}
                    	else if (style.contains("BOLD") && style.contains("ITALIC")) {
                    		currentWordSize = textFontSize*textBoldItalicFont.getStringWidth(currentWord)/1000;
                    	}
                    	else {
                    		currentWordSize = textFontSize*textFont.getStringWidth(currentWord)/1000;  
                    	}
               	       	if (totalSize + currentWordSize <= width) {
	                		/* Append the current line*/	
	                		currentLine	+= currentWord;
	                		totalSize 	+= currentWordSize;
	                		ParsedLine parsedLine = new ParsedLine(lineCounter, currentLine, style, currentWordSize);
	                		lines.add(parsedLine);
	                		/* Initialize variables for the remaining part that has not been parsed and will be written in the next line*/
	                		currentLine	= "";
	                		totalSize 	= 0;
	                		lineCounter++;
	                		/* Update the current substring and remove the parsed part of it*/
	                		if (i != block.getBlockStyles().size()-1 && !style.equals(block.getBlockStyles().get(i+1).style)) {
	                			currentSubstring = currentSubstring.substring(lastSpace);
	                		}
	                		else {
	                			currentSubstring = currentSubstring.substring(lastSpace).trim();
	                		}
	        		        lastSpace = -1;
               	       	}
               	       	else {
               	       		currentLine = currentWord;
               	       		totalSize 	= currentWordSize;
               	       		lineCounter++;
               	       		ParsedLine parsedLine = new ParsedLine(lineCounter, currentLine, style, currentWordSize);
	                		/* Update the current substring and remove the parsed part of it*/
	        		        currentSubstring = currentSubstring.substring(lastSpace);
	        		        lastSpace = -1;
               	       		continue;
               	       	}
                	} 
                	
                	/* Else if the current substring fits in the current line and it is the last one*/
                	else if (spaceIndex == currentSubstring.length()) {
                		/* Append the current line*/
                		currentLine	+= currentWord;
                		totalSize 	+= currentWordSize;
                		ParsedLine parsedLine = new ParsedLine(lineCounter, currentLine, style, currentWordSize);
                		lines.add(parsedLine);
                		/* Initialize variables for next loop (not needed)*/
                		currentLine			= "";
                		if (totalSize == width)  {
                			totalSize = 0;
                			lineCounter++;
                		} 
                		currentSubstring 	= "";                	
                	} 
 
                	/* Else if the current substring fits in the current line keep the position of the last space until now that the substring fits in the line  */
                	else {
                		lastSpace = spaceIndex;
        		     }
        		}     		
        	}
        }
        
        return lines;
    }
    
    public List<Section> addOrderInformation(List<Section> allSections, ContractParametersDto contractParametersDto) {
    	/* Replaces all automated keywords with the provider, consumer and product info while rendering*/	
    	/* For all sections and all blocks*/
    	for (int k = 0 ; k < allSections.size() ; k++) {
	    	for (int i = 0 ; i < allSections.get(k).getBlocks().size() ; i++) {
	    		String initialText 	= allSections.get(k).getBlocks().get(i).text;
	    		String newText 		= null;
	    		for (int j = 0 ; j < allSections.get(k).getBlocks().get(i).getBlockStyles().size() ; j++) {
	    		
	    			/* If the block contains a bold-underlined part which is a master template contract keyword*/
	    			if (allSections.get(k).getBlocks().get(i).getBlockStyles().get(j).style.contains("BOLD") &&
	    				allSections.get(k).getBlocks().get(i).getBlockStyles().get(j).style.contains("UNDERLINE")) {
	    				for (String key : keywords.keySet()) {
	    					if (allSections.get(k).getBlocks().get(i).getText().contains(key)) {
	    						
	    						/* Set style as BOLD for the words that the keywords will be replaced with*/
	    						allSections.get(k).getBlocks().get(i).getBlockStyles().get(j).style = "BOLD";
	    						int initialLength	= allSections.get(k).getBlocks().get(i).text.length();
	    						
	    						/* Replace the keyword with the appropriate final word from the hashmap with the keywords*/
	    						allSections.get(k).getBlocks().get(i).text = allSections.get(k).getBlocks().get(i).getText().replace(key, keywords.get(key));
	    						newText = allSections.get(k).getBlocks().get(i).text;
	    						int newLength 		= allSections.get(k).getBlocks().get(i).text.length();
	    						int charDiff 		= initialLength - newLength;
	    						
	    						/* Set new offset and length for the blockstyle that describes the new words*/
	    						for (int l = j ; l < allSections.get(k).getBlocks().get(i).getBlockStyles().size() ; l++) {
	    							if (l != j) {
	    								allSections.get(k).getBlocks().get(i).getBlockStyles().get(l).offset -= (charDiff);
	    							}
	    							else {
	    								allSections.get(k).getBlocks().get(i).getBlockStyles().get(l).length -= (charDiff);
	    							}
	    						} 
	    					}
	    				}
	    			}
	    		}
	    		
				BlockStyle info = new BlockStyle();
				
				/*Get provider, consumer and product info*/
				ContractParametersDto.Provider prov = contractParametersDto.getProvider();
				ContractParametersDto.Consumer cons = contractParametersDto.getConsumer();
				ContractParametersDto.Product  prod = contractParametersDto.getProduct();
				
				/* Append all keyword blocks with the corresponding information and update their blockstyles accordingly */
				if (initialText.contains("[sellerName]")) {
					info.offset	= allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).length+allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).offset;
					info.length	= prov.getCorporateName().length();
					info.style 	= "NORMAL";
					allSections.get(k).getBlocks().get(i).text = allSections.get(k).getBlocks().get(i).text.trim()+": "+prov.getCorporateName();
					allSections.get(k).getBlocks().get(i).getBlockStyles().add(info);
				}
				else if (initialText.contains("[sellerAddress]")) {
					info.offset	= allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).length+allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).offset;
					info.length	= prov.getProfessionalAddress().length();
					info.style 	= "NORMAL";
					allSections.get(k).getBlocks().get(i).text = allSections.get(k).getBlocks().get(i).text.trim()+": "+prov.getProfessionalAddress();
					allSections.get(k).getBlocks().get(i).getBlockStyles().add(info);
				}
				else if (initialText.contains("[sellerEmail]")) {
					info.offset	= allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).length+allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).offset;
					info.length	= prov.getContactEmail().length();
					info.style 	= "NORMAL";
					allSections.get(k).getBlocks().get(i).text = allSections.get(k).getBlocks().get(i).text.trim()+": "+prov.getContactEmail();
					allSections.get(k).getBlocks().get(i).getBlockStyles().add(info);    					
				}
				else if (initialText.contains("[sellerContactPerson]")) {
					info.offset	= allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).length+allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).offset;
					info.length	= prov.getContactPerson().length();
					info.style 	= "NORMAL";
					allSections.get(k).getBlocks().get(i).text = allSections.get(k).getBlocks().get(i).text.trim()+": "+prov.getContactPerson();
					allSections.get(k).getBlocks().get(i).getBlockStyles().add(info);      					
				}
				else if (initialText.contains("[sellerCompanyRegNumber]")) {
					info.offset	= allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).length+allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).offset;
					info.length	= prov.getCopmanyRegistrationNumber().length();
					info.style 	= "NORMAL";
					allSections.get(k).getBlocks().get(i).text = allSections.get(k).getBlocks().get(i).text.trim()+": "+prov.getCopmanyRegistrationNumber();
					allSections.get(k).getBlocks().get(i).getBlockStyles().add(info);        					
				}
				else if (initialText.contains("[sellerVAT]")) {
					info.offset	= allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).length+allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).offset;
					info.length	= prov.getEuVatNumber().length();
					info.style 	= "NORMAL";
					allSections.get(k).getBlocks().get(i).text = allSections.get(k).getBlocks().get(i).text.trim()+": "+prov.getEuVatNumber();
					allSections.get(k).getBlocks().get(i).getBlockStyles().add(info);       					
				}
				else if (initialText.contains("[clientName]")) {
					info.offset	= allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).length+allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).offset;
					info.length	= cons.getCorporateName().length();
					info.style 	= "NORMAL";
					allSections.get(k).getBlocks().get(i).text = allSections.get(k).getBlocks().get(i).text.trim()+": "+cons.getCorporateName();
					allSections.get(k).getBlocks().get(i).getBlockStyles().add(info);
				}
				else if (initialText.contains("[clientAddress]")) {
					info.offset	= allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).length+allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).offset;
					info.length	= cons.getProfessionalAddress().length();
					info.style 	= "NORMAL";
					allSections.get(k).getBlocks().get(i).text = allSections.get(k).getBlocks().get(i).text.trim()+": "+cons.getProfessionalAddress();
					allSections.get(k).getBlocks().get(i).getBlockStyles().add(info);
				}
				else if (initialText.contains("[clientEmail]")) {
					info.offset	= allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).length+allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).offset;
					info.length	= cons.getContactEmail().length();
					info.style 	= "NORMAL";
					allSections.get(k).getBlocks().get(i).text = allSections.get(k).getBlocks().get(i).text.trim()+": "+cons.getContactEmail();
					allSections.get(k).getBlocks().get(i).getBlockStyles().add(info);    					
				}
				else if (initialText.contains("[clientContactPerson]")) {
					info.offset	= allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).length+allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).offset;
					info.length	= cons.getContactPerson().length();
					info.style 	= "NORMAL";
					allSections.get(k).getBlocks().get(i).text = allSections.get(k).getBlocks().get(i).text.trim()+": "+cons.getContactPerson();
					allSections.get(k).getBlocks().get(i).getBlockStyles().add(info);      					
				}
				else if (initialText.contains("[clientCompanyRegNumber]")) {
					info.offset	= allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).length+allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).offset;
					info.length	= cons.getCopmanyRegistrationNumber().length();
					info.style 	= "NORMAL";
					allSections.get(k).getBlocks().get(i).text = allSections.get(k).getBlocks().get(i).text.trim()+": "+cons.getCopmanyRegistrationNumber();
					allSections.get(k).getBlocks().get(i).getBlockStyles().add(info);        					
				}
				else if (initialText.contains("[clientVAT]")) {
					info.offset	= allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).length+allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).offset;
					info.length	= cons.getEuVatNumber().length();
					info.style 	= "NORMAL";
					allSections.get(k).getBlocks().get(i).text = allSections.get(k).getBlocks().get(i).text.trim()+": "+cons.getEuVatNumber();
					allSections.get(k).getBlocks().get(i).getBlockStyles().add(info);       					
				}
				else if (initialText.contains("[ProductId]")) {
					info.offset	= allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).length+allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).offset;
					info.length	= prod.getProductID().length();
					info.style 	= "NORMAL";
					allSections.get(k).getBlocks().get(i).text = allSections.get(k).getBlocks().get(i).text.trim()+": "+prod.getProductID();
					allSections.get(k).getBlocks().get(i).getBlockStyles().add(info);   
				}
				else if (initialText.contains("[ProductName]")) {
					info.offset	= allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).length+allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).offset;
					info.length	= prod.getProductName().length();
					info.style 	= "NORMAL";
					allSections.get(k).getBlocks().get(i).text = allSections.get(k).getBlocks().get(i).text.trim()+": "+prod.getProductName();
					allSections.get(k).getBlocks().get(i).getBlockStyles().add(info);       					
				}
				else if (initialText.contains("[ProductDescription]")) {
					info.offset	= allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).length+allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).offset;
					info.length	= prod.getProductDescription().length();
					info.style 	= "NORMAL";
					allSections.get(k).getBlocks().get(i).text = allSections.get(k).getBlocks().get(i).text.trim()+": "+prod.getProductDescription();
					allSections.get(k).getBlocks().get(i).getBlockStyles().add(info);       					
				}
				else if (initialText.contains("[PastVersionsIncluded]")) {
					info.offset	= allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).length+allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).offset;
					info.length	= prod.getPastVersionIncluded().length();
					info.style 	= "NORMAL";
					allSections.get(k).getBlocks().get(i).text = allSections.get(k).getBlocks().get(i).text.trim()+": "+prod.getPastVersionIncluded();
					allSections.get(k).getBlocks().get(i).getBlockStyles().add(info);       					
				}
				else if (initialText.contains("[UpdatesIncluded]")) {
					info.offset	= allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).length+allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).offset;
					info.length	= prod.getUpdatesIncluded().length();
					info.style 	= "NORMAL";
					allSections.get(k).getBlocks().get(i).text = allSections.get(k).getBlocks().get(i).text.trim()+": "+prod.getUpdatesIncluded();
					allSections.get(k).getBlocks().get(i).getBlockStyles().add(info);       					
				}
				else if (initialText.contains("[EstimatedDeliveryDate]")) {
					info.offset	= allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).length+allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).offset;
					info.length	= prod.getEstimatedDeliveryDate().length();
					info.style 	= "NORMAL";
					allSections.get(k).getBlocks().get(i).text = allSections.get(k).getBlocks().get(i).text.trim()+": "+prod.getEstimatedDeliveryDate();
					allSections.get(k).getBlocks().get(i).getBlockStyles().add(info);       					
				}
				else if (initialText.contains("[DeliveryMediaFormat]")) {
					info.offset	= allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).length+allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).offset;
					info.length	= prod.getMediaAndFormatOfDelivery().length();
					info.style 	= "NORMAL";
					allSections.get(k).getBlocks().get(i).text = allSections.get(k).getBlocks().get(i).text.trim()+": "+prod.getMediaAndFormatOfDelivery();
					allSections.get(k).getBlocks().get(i).getBlockStyles().add(info);       					
				}
				else if (initialText.contains("[ApplicableFees]")) {
					info.offset	= allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).length+allSections.get(k).getBlocks().get(i).getBlockStyles().get(allSections.get(k).getBlocks().get(i).getBlockStyles().size()-1).offset;
					info.length	= prod.getApplicableFees().length();
					info.style 	= "NORMAL";
					allSections.get(k).getBlocks().get(i).text = allSections.get(k).getBlocks().get(i).text.trim()+": "+prod.getApplicableFees();
					allSections.get(k).getBlocks().get(i).getBlockStyles().add(info);       					
				}
	    	}
	    }  
    	
    	/* Return the final sections that should be written*/
    	return allSections;
    }
	
    
    public void addBlock(Block block, boolean justify, boolean newPage, boolean endOfSection, int blockNo) throws IOException {    	 	
    	
    	/* Create a new blank page and a contentStream for that page or keep on writing to the current one*/
		if (newPage) {
			if (this.currentPDPageContentStream != null)
				this.currentPDPageContentStream.close();
			this.currentPage = new PDPage(PDRectangle.A4);
			document.addPage(this.currentPage);
			this.currentPDPageContentStream = new PDPageContentStream(document, this.currentPage);
		}
		else {
			this.currentPage = this.getCurrentPage();
			this.currentPDPageContentStream = this.getCurrentPDPageContentStream();
		}
		
		/* Calculate the starting position*/
        PDRectangle mediaBox	= this.currentPage.getMediaBox();
        float marginY 			= 72;
        float marginX 			= 72;
        float width 			= mediaBox.getWidth()-2*marginX;
        float startX 			= mediaBox.getLowerLeftX()+marginX;
        float startY 			= mediaBox.getUpperRightY()-marginY;
        
    	/* Split the text into lines based on the block style*/
    	List<ParsedLine> parsedLines		= parseLines(block, width);

		/* Get the length of each line - Needed for the justification*/
    	HashMap<Integer, Integer> lengthPerLine	= getLengthPerLine(parsedLines);
    	/* Get the width of each line - Needed for the justification*/
    	HashMap<Integer, Float> widthPerLine 	= getWidthPerLine(parsedLines);
    	
        /* Initialize or get the offset that keeps the page's free space*/
        float offsetY;
        if (newPage) {
        	offsetY = startY;
        }
        else {
        	offsetY = this.getCurrentOffset();
        }
        float offsetX = 0;
    	
    	/* Set starting position and text leading*/
        this.currentPDPageContentStream.beginText();
        this.currentPDPageContentStream.newLineAtOffset(startX, offsetY);   
        this.currentPDPageContentStream.setLeading(textLeading); 
        
        /* Check if the block consists of only one line. In such a case justification should not be applied*/
    	boolean blockIsOneLine = false;
    	if (parsedLines.get(0).lineNumber == parsedLines.get(parsedLines.size()-1).lineNumber) {
    		blockIsOneLine = true;
    	}
    	
    	int NumberOfBlockLines = parsedLines.get(parsedLines.size()-1).lineNumber;
  
        float totalOffsetXOfLine 		= 	0;
        boolean currentBlockisListItem	=	false;
        for (int i = 0 ; i < parsedLines.size() ; i++) {
        	
        	/* Get all needed information for the current line*/
        	String currentFont 	= parsedLines.get(i).font;
        	int currentLineNo	= parsedLines.get(i).lineNumber;
        	String currentText	= parsedLines.get(i).text;
        	int currentLength	= lengthPerLine.get(currentLineNo);
        	Float currentWidth	= widthPerLine.get(currentLineNo);
        	
        	if (currentText.equals(""))
        		continue;
        	
        	/* \u2022 bullet point*/
        	if (currentText.contains("\u2022")) {
        		currentBlockisListItem = true;
        	}

        	/* If a block is a list item set the appropriate textleading*/
            if (!currentBlockisListItem && isPreviousBlockIsListItem()) {
            	this.currentPDPageContentStream.newLineAtOffset(0, -textLeading/2);
            	offsetY -= (textLeading/2);
	            setCurrentOffset(offsetY);	            
            }
            if (currentBlockisListItem) {
    			setPreviousBlockIsListItem(true);
    		}
    		else {
    			setPreviousBlockIsListItem(false);
    		}     
            
        	float size = 0;
    		float charSpacing = 0;
    		if (justify){
    			if (currentLength > 1) {
    				float free = width - currentWidth;
    				/* The last line should have charSpacing = 0*/
    				if (free > 0 && parsedLines.get(i).lineNumber != NumberOfBlockLines && !blockIsOneLine) {
    					charSpacing = free/(currentLength-1);
    				}
    			}
    		}       	
    		
    		this.currentPDPageContentStream.setCharacterSpacing(charSpacing);
    		
    		/* Calculate the width of the of the current text
    		 * This information is needed for the cases where a line contains words with different fonts*/     	
   	       	if (currentFont.equals("BOLD") || (currentFont.contains("BOLD") && currentFont.contains("UNDERLINE"))) {
   	       		if (justify && !blockIsOneLine) {
   	       			size = (textFontSize*textBoldFont.getStringWidth(currentText)/1000)+(currentText.length()*charSpacing);
   	       		}
   	       		else {
   	       			size = (textFontSize*textBoldFont.getStringWidth(currentText)/1000);
   	       		}
   	       		this.currentPDPageContentStream.setFont(textBoldFont, textFontSize);
   	       	}
        	else if (currentFont.equals("ITALIC") || (currentFont.contains("ITALIC") && currentFont.contains("UNDERLINE"))) {
        		if (justify && !blockIsOneLine) {
        			size = (textFontSize*textItalicFont.getStringWidth(currentText)/1000)+(currentText.length()*charSpacing);
        		}
        		else {
        			size = (textFontSize*textItalicFont.getStringWidth(currentText)/1000);
        		}
        		this.currentPDPageContentStream.setFont(textItalicFont, textFontSize);
        	}
        	else if (currentFont.contains("BOLD") && currentFont.contains("ITALIC")) {
        		if (justify && !blockIsOneLine) {
        			size = (textFontSize*textBoldItalicFont.getStringWidth(currentText)/1000)+(currentText.length()*charSpacing);
        		}
        		else {
        			size = (textFontSize*textBoldItalicFont.getStringWidth(currentText)/1000);
        		}
        		this.currentPDPageContentStream.setFont(textBoldItalicFont, textFontSize);
        	}
        	else {
        		if (justify && !blockIsOneLine) {
        			size = (textFontSize*textFont.getStringWidth(currentText)/1000)+(currentText.length()*charSpacing); 
        		}
        		else {
        			size = (textFontSize*textFont.getStringWidth(currentText)/1000);
        		}
        		this.currentPDPageContentStream.setFont(textFont, textFontSize);
        	}
			
    		this.currentPDPageContentStream.showText(currentText);	
    		
    		/* Underline if needed*/
    		if (currentFont.contains("UNDERLINE")) {
    			this.currentPDPageContentStream.endText();
    			underlineWord(this.currentPDPageContentStream, currentText, size, 0.5f, marginX+totalOffsetXOfLine, offsetY, -2);
    		   	this.currentPDPageContentStream.beginText();
    		   	this.currentPDPageContentStream.newLineAtOffset(totalOffsetXOfLine+marginX, offsetY);   
    		   	this.currentPDPageContentStream.setCharacterSpacing(charSpacing);	
    		   	this.currentPDPageContentStream.setLeading(textLeading);
    		}
    		    			
    		/* If the current and the next text should be written in the same line, move the cursor horizontally after the already written current text*/
    		if (i < parsedLines.size()-1 && currentLineNo == parsedLines.get(i+1).lineNumber) {
    			this.currentPDPageContentStream.newLineAtOffset(size, 0);
    			totalOffsetXOfLine += size;	
    		}
    		
    		/* Else if the next text should be written in the next line, move the cursor at the beginning of the next row*/
    		else {
    			this.currentPDPageContentStream.newLineAtOffset(-totalOffsetXOfLine, -textLeading);
    			/* Initialization for next line*/
    			totalOffsetXOfLine = 0;
    			offsetY -= textLeading;
    		}
  	
    		/* In case of the current page is full*/
    		if (offsetY <= 82 && !(i < parsedLines.size()-1 && currentLineNo == parsedLines.get(i+1).lineNumber)) {
			
    			/* Close the contentStream of the current page*/
    			this.currentPDPageContentStream.endText();
    			if (this.currentPDPageContentStream != null) {
    				this.currentPDPageContentStream.close();
    			}
    			
    			/* Create a new blank page*/
    			this.currentPage = new PDPage(PDRectangle.A4);
    			document.addPage(this.currentPage);
    			this.currentPDPageContentStream = new PDPageContentStream(document, this.currentPage);
    			this.currentPDPageContentStream.beginText();
    			
    			/* Set font, starting position and text leading*/            	
       	       	if (currentFont.equals("BOLD") || (currentFont.contains("BOLD") && currentFont.contains("UNDERLINE"))) {
       	       		this.currentPDPageContentStream.setFont(textBoldFont, textFontSize);
       	       	}
            	else if (currentFont.equals("ITALIC") || (currentFont.contains("ITALIC") && currentFont.contains("UNDERLINE"))) {
            		this.currentPDPageContentStream.setFont(textItalicFont, textFontSize);
            	}
            	else if (currentFont.contains("BOLD") && currentFont.contains("ITALIC")) {
            		this.currentPDPageContentStream.setFont(textBoldItalicFont, textFontSize);
            	}
            	else {
            		this.currentPDPageContentStream.setFont(textFont, textFontSize);
            	}
           	         	
    			this.currentPDPageContentStream.newLineAtOffset(startX, startY);   
    			this.currentPDPageContentStream.setLeading(textLeading);
    			
    	    	/* Initialize the offset for the new page*/
    			offsetY = startY;
    		}
        }
 
    	/* When the entire text is written close the open contentStream*/
    	this.currentPDPageContentStream.endText();
		setCurrentPDPageContentStream(this.currentPDPageContentStream);
		setCurrentPage(this.currentPage);
		if (endOfSection == true) {
			setCurrentOffset(offsetY-32);
		}
		else if (currentBlockisListItem) {
			setCurrentOffset(offsetY-7);
		}
		else { 
			setCurrentOffset(offsetY-textLeading/3);
		}
    }
    
    
    public void addTitle(String type, String title, boolean justify, boolean newPage) throws IOException { 	
    	/* Create a new blank page and a contentStream for that page or keep on writing to the current one*/
		if (newPage || type.equals("Title")) {
			if (this.currentPDPageContentStream != null) {
				this.currentPDPageContentStream.close();
			}
			this.currentPage = new PDPage(PDRectangle.A4);
			document.addPage(this.currentPage);
			this.currentPDPageContentStream = new PDPageContentStream(document, this.currentPage);
		}
		else {
			this.currentPage = this.getCurrentPage();
			this.currentPDPageContentStream = this.getCurrentPDPageContentStream();
		}
		
		/* Calculate the starting position*/
        PDRectangle mediaBox = this.currentPage.getMediaBox();
        float marginY = 72;
        float marginX = 72;
        float width = mediaBox.getWidth()-2*marginX;
        float startX = 0;
        if (type.equals("Title")) {
        	startX = (mediaBox.getWidth() - titleFont.getStringWidth(title)*titleFontSize/1000f)/2f;
        }
        else if (type.equals("Subtitle")) {
        	startX = (mediaBox.getWidth() - subTitleFont.getStringWidth(title)*subTitleFontSize/1000f)/2f;
        }
        else if (type.equals("Sectiontitle") || type.equals("SectionSubTitle")) {
        	startX = mediaBox.getLowerLeftX()+marginX;
        }
        float startY = mediaBox.getUpperRightY()-marginY;
        
        /* Initialize the offset that keeps the page's free space*/
        float offsetY;
        if (newPage || type.equals("Title")) {
        	offsetY = startY-70;   
        }
        else {
        	offsetY = this.getCurrentOffset();
        }
        
    	/* Set fonts*/
        this.currentPDPageContentStream.beginText();
        if (type.equals("Title")) {
        	this.currentPDPageContentStream.setFont(titleFont, titleFontSize);
        }
        else if (type.equals("Subtitle")) {
        	this.currentPDPageContentStream.setFont(subTitleFont, subTitleFontSize);
        }
        else if (type.equals("Sectiontitle")) {
        	this.currentPDPageContentStream.setFont(sectionTitleFont, sectionTitleFontSize);
        }
        else if (type.equals("SectionSubTitle")) {
        	this.currentPDPageContentStream.setFont(sectionTitleFont, sectionSubTitleFontSize);
        }
        this.currentPDPageContentStream.newLineAtOffset(startX, offsetY);   
        this.currentPDPageContentStream.setLeading(textLeading);
    	 	
    	/* Read text line by line*/
    	List<String> lines = ParseTitle(title, width);
    	for (String line: lines) {

    		float charSpacing = 0;
    		
    		/* If allignment is true*/
    		if (justify){
    			if (line.length() > 1) {
    				float size = 0;
    		        if (type.equals("Title")) {
    		        	size = titleFontSize * titleFont.getStringWidth(line) / 1000;
    		        }
    		        else if (type.equals("Subtitle")) {
    		        	size = subTitleFontSize * subTitleFont.getStringWidth(line) / 1000;
    		        }
    		        else if (type.equals("Sectiontitle") || type.equals("SectionSubTitle")) {
    		        	size = sectionTitleFontSize * sectionTitleFont.getStringWidth(line) / 1000;
    		        }
    				float free = width - size;
    				if (free > 0 && !lines.get(lines.size()-1).equals(line)) {
    					charSpacing = free/(line.length()-1);
    				}
    			}
    		}
    		this.currentPDPageContentStream.setCharacterSpacing(charSpacing);
    		/* Write the new line*/
    		this.currentPDPageContentStream.showText(line);
    		/* Set starting position for the new line*/
    		this.currentPDPageContentStream.newLineAtOffset(0, -textLeading);
    		/* Reduce the remaining free space of the current page*/
    		offsetY -= textLeading;
    		
    		/* In case of the current page is full*/
    		if (offsetY <= marginY) {
    			/* Close the contentStream of the current page*/
    			this.currentPDPageContentStream.endText();
    			if (this.currentPDPageContentStream != null) {
    				this.currentPDPageContentStream.close();
    			}
    			/* Create a new blank page*/
    			this.currentPage = new PDPage(PDRectangle.A4);
    			document.addPage(this.currentPage);
    			this.currentPDPageContentStream = new PDPageContentStream(document, this.currentPage);
    			this.currentPDPageContentStream.beginText();
    			/* Set fonts*/
    	        if (type.equals("Title")) {
    	        	this.currentPDPageContentStream.setFont(titleFont, titleFontSize);
    	        }
    	        else if (type.equals("Subtitle")) {
    	        	this.currentPDPageContentStream.setFont(subTitleFont, subTitleFontSize);
    	        }
    	        else if (type.equals("Sectiontitle") || type.equals("SectionSubTitle")) {
    	        	this.currentPDPageContentStream.setFont(sectionTitleFont, sectionTitleFontSize);
    	        }
    			this.currentPDPageContentStream.newLineAtOffset(startX, startY);   
    			this.currentPDPageContentStream.setLeading(textLeading);
    	    	/* Initialize the offset for the new page*/
    			offsetY = startY;
    		}
    	}
    	
    	/* When the entire text is written close the open contentStream*/
    	this.currentPDPageContentStream.endText();
		setCurrentPDPageContentStream(this.currentPDPageContentStream);
		setCurrentPage(this.currentPage);
        if (type.equals("Title")) {
        	setCurrentOffset(offsetY-textLeading);
        }
        else if (type.equals("Subtitle")) {
        	setCurrentOffset(offsetY-marginY);
        }
        else if (type.equals("Sectiontitle")) {
        	setCurrentOffset(offsetY-12);
        }
        else if (type.equals("SectionSubTitle")) {
        	setCurrentOffset(offsetY-12);
        }
    }
    
	
	public void addHeaderAndFooter(String contractTitle) throws IOException {
		int currentPage = 0;
		
		/* Timestamp for the right part of the header*/
		String timeStamp = new SimpleDateFormat("dd MMM yyyy - HH:mm:ss").format(new Date()) + " EEST";
		String headerText = timeStamp;
		
		/* Iterate through all pages*/
		for(PDPage page : document.getPages()) {
			PDRectangle pageSize = page.getMediaBox();
			
			/* Calculate the widhts off all header and footer texts, needed for the calculation of their positions in the page*/
		    float stringWidthFooter 	= footerFont.getStringWidth((currentPage+1) + " / " + document.getNumberOfPages())*footerFontSize/1000f;
		    float stringWidthFooter2 	= footerFont.getStringWidth(contractTitle)*footerFontSize/1000f;
			float stringWidthHeader 	= headerFont.getStringWidth(headerText)*headerFontSize/1000f;

		    /* Get the width and height of the page*/
		    float pageWidth		=  pageSize.getWidth();
		    float pageHeight 	=  pageSize.getHeight();
		    
		    /* Calculate the center of the page based on header*/
		    float centerXHeader = (pageWidth - stringWidthHeader)/2f;
		    float centerYHeader = pageHeight/2f;   
		    
		    /* Calculate the center of the page based on footer*/
		    float centerXFooter	= (pageWidth - stringWidthFooter)/2f;
		    float centerYFooter = pageHeight/2f;

		    /* Append the content to the existing stream*/
		    try (PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, true, true)) {
		    /* Add header*/
		    	contentStream.beginText();
		     	/* Set header font, font size and font color*/
		    	contentStream.setFont(headerFont, headerFontSize);
		    	contentStream.setNonStrokingColor(color);	
		    	/* Set the position*/
		    	contentStream.setTextMatrix(Matrix.getTranslateInstance(pageWidth-72-stringWidthHeader, centerYHeader+VerticalOffset("top", pageHeight)-23));
		    	/* Write*/
		    	contentStream.showText(headerText);
		    	contentStream.endText();
		    	
		    	/* Create the PDFImageXObject object to add the topio logo*/
		    	PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, logo, logoFilename);
		    	contentStream.drawImage(pdImage, 72, centerYHeader+VerticalOffset("top", pageHeight)-27,37,14);	    	
    			drawLine(contentStream, 0.5f, 72, centerYHeader+VerticalOffset("top", pageHeight)-23, -8, pageWidth-72);	
		    	
		    /* Add footer*/
		    	contentStream.beginText();
		     	/* Set footer font, font size and font color*/
		    	contentStream.setFont(footerFont, footerFontSize);
		    	contentStream.setNonStrokingColor(color2);
		    	/* Set the position*/
		    	contentStream.setTextMatrix(Matrix.getTranslateInstance(pageWidth-72-stringWidthFooter, centerYFooter+VerticalOffset("bottom", pageHeight)+18));	            
		    	/* Write*/
		    	contentStream.showText((currentPage+1) + " / " + document.getNumberOfPages());
		    	
		    	contentStream.setNonStrokingColor(color);
		    	/* Set the position*/
		    	contentStream.setTextMatrix(Matrix.getTranslateInstance(72, centerYFooter+VerticalOffset("bottom", pageHeight)+18));	            
		    	/* Write*/
		    	contentStream.showText(contractTitle);

		    	contentStream.endText();
		    	drawLine(contentStream, 0.5f, 72, centerYHeader+VerticalOffset("bottom", pageHeight)+23, +8, pageWidth-72);
		    }
		    currentPage++;
		}
	}
	
	public ArrayList <Block> getOptionSuboptionBody(JSONArray blocks) throws JsonMappingException, JsonProcessingException{
		ArrayList<Block> allBlocks = new ArrayList<Block>();
		
		for (int i = 0, size = blocks.length(); i < size; i++) {
			int position		=	0;
			Block block			= 	null;
			boolean isListItem	=	false;
			JSONObject jobj = blocks.getJSONObject(i);
			
			/* Get type of the current block*/
			String type = jobj.getString("type");
			
			/* Delete characters that cannot been written in the PDF*/
			if (type.equals("unordered-list-item")) {
				block = new Block("\u2022" + " " + jobj.getString("text").replace("\n", "").replace("\r", "").replace("\u00A0",""));
				isListItem = true;
			}
			else {
				block = new Block(jobj.getString("text").replace("\n", "").replace("\r", "").replace("\u00A0","").replace("“", "\"").replace("”", "\""));
			}
			
			/* Get styles of the current block*/
			JSONArray styles = jobj.getJSONArray("inlineStyleRanges");
			
			/* If there is no style for the current text*/
			if (styles.isEmpty()) {
				BlockStyle blockStyle1 = new BlockStyle(0, block.getText().length(), "NORMAL");
				block.addBlockStyle(blockStyle1);
    			position = position + blockStyle1.getLength();
			}
			else {
				/* For all styles of the current block*/
        		for (int j = 0, length = styles.length(); j < length; j++) {
        			JSONObject style = styles.getJSONObject(j);
        			
        			/* Ignore styles. We always use the default ones*/
        			if (style.getString("style").contains("fontsize") 	||
        				style.getString("style").contains("color")		||
        				style.getString("style").contains("fontfamily")) {
        				continue;
        			}
        			else if (style.getInt("offset") == position) {
	        			ObjectMapper m 			= new ObjectMapper();
	        			BlockStyle blockStyle1 	= m.readValue(style.toString(), BlockStyle.class);
	        			block.addBlockStyle(blockStyle1);
	        			position 				= position + blockStyle1.getLength();
        			}
        			else if (style.getInt("offset") > position) {
        				/* First add/create normal style*/
        				BlockStyle blockStyle1 	= new BlockStyle(position, style.getInt("offset") - position , "NORMAL");
        				block.addBlockStyle(blockStyle1);
            			position 				= style.getInt("offset");
            			
            			ObjectMapper m 	= new ObjectMapper();
            			blockStyle1 	=  m.readValue(style.toString(), BlockStyle.class);
	        			block.addBlockStyle(blockStyle1);
	            		position 		= position + blockStyle1.getLength();
	        			
        			}
        			else if (style.getInt("offset") < position) {
        				/* We need to add style before without changing position*/
        				ObjectMapper m 			= new ObjectMapper();
	        			BlockStyle blockStyle1 	= m.readValue(style.toString(), BlockStyle.class);
	        			block.addBlockStyle(blockStyle1);
        			}
        		}
				
        		/* If there is more normal text at the end*/
        		if (block.getText().length() > position) {
        			BlockStyle blockStyle1 = new BlockStyle(position, block.getText().length() - position, "NORMAL");
    				block.addBlockStyle(blockStyle1);
        		}
			}
			
			/* If the current block is list item add block style for the bullet point*/
			if (isListItem) {
				for (int j = 0 ; j < block.getBlockStyles().size() ; j++) {
					int newOffset 	= block.getBlockStyles().get(j).getOffset()+2;
					int newLength 	= block.getBlockStyles().get(j).getLength()+2;
					String style	= block.getBlockStyles().get(j).getStyle();
					BlockStyle newBlockStyle	=	new BlockStyle(newOffset, newLength, style);
					block.getBlockStyles().set(j, newBlockStyle);
				}
				BlockStyle bulletBlockStyle = new BlockStyle(0, 2, "NORMAL");
				block.getBlockStyles().add(0, bulletBlockStyle);
				block.getBlockStyles().remove(block.getBlockStyles().size()-1);
			}	
			
    		allBlocks.add(block);
    		//providerSection.setBlocks(allBlocks);
		}
		return allBlocks;
	}
	
	public byte[] renderPDF(ContractParametersDto contractParametersDto, PrintConsumerContractCommand command, String filePath) throws IOException {	
		
		/* Get contract information*/
		UUID orderKey 					= command.getOrderKey();
		OrderEntity orderEntity 		= orderRepository.findOrderEntityByKey(orderKey).get();
		OrderItemEntity orderItemEntity = orderEntity.getItems().get(0);
		AccountEntity provider 			= orderItemEntity.getProvider();	
		Integer contractId 				= orderItemEntity.getContractTemplateId();
		String contractVersion 			= orderItemEntity.getContractTemplateVersion();

		/* Get contract*/

//		ProviderTemplateContractHistoryEntity contract = contractRepository.findById(1).get();
		ProviderTemplateContractHistoryEntity contract = contractRepository.findByIdAndVersion(provider.getKey(), contractId, contractVersion).get();
		
		
    	/* Get title and subtitles*/
    	String title 	= contract.getTitle();
    	String subtitle = contract.getSubtitle();
    	
    	/* Get all sections sorted by index*/
    	List<Section> allSections = new ArrayList<Section>();
    	List<ProviderTemplateSectionHistoryEntity> sortedSections = contract.getSectionsSorted();
    	
    	String prevIndex = "0";
    	String sectionIndex; 
    	for (ProviderTemplateSectionHistoryEntity section :sortedSections) {
    		
    		/* If section is not selected continue */ 
    		if (section.isOptional()){
    			continue;
    		}
    		
    		MasterSectionHistoryEntity masterSection = section.getMasterSection();
    	
    		ArrayList<Block> allBlocks = new ArrayList<Block>();
    		
    		/* Get Title and Index*/
    		/* Find proper index in case of previous omitted sections*/
    		String[] prevIndexArray = prevIndex.split("\\.");
    		String[] currIndexArray = masterSection.getIndex().split("\\.");
    		
    		if (prevIndex != "0" && currIndexArray.length > 1 && currIndexArray.length == prevIndexArray.length  && 
    					Integer.parseInt(currIndexArray[currIndexArray.length-1]) >
    					Integer.parseInt(prevIndexArray[prevIndexArray.length-1]) +1 ) {
    			currIndexArray[currIndexArray.length-1] = "" + (Integer.parseInt(prevIndexArray[prevIndexArray.length-1]) + 1);
    			sectionIndex = String.join(".", currIndexArray);
    		}
    		else {
        		sectionIndex	= masterSection.getIndex();
    		}
    		String sectionTitle = masterSection.getTitle();
    		
    		Section providerSection = null;
    		if (sectionTitle != null && !sectionTitle.isEmpty()) {
    			providerSection = new Section("Section " + sectionIndex + " - " + sectionTitle);
    		}
    		else {
    			providerSection = new Section("Section " + sectionIndex);
    		}
    		
    		String optionJson, subOptionJson;
    		optionJson 												= masterSection.getOptions().get(section.getOption()).getBody();
    		List<ContractSectionSubOptionDto> suboptions 			= new ArrayList<ContractSectionSubOptionDto>();
    		suboptions 												= masterSection.getOptions().get(section.getOption()).getSubOptions();    		
    		JSONObject obj = new JSONObject(optionJson);
    		
    		/* Get blocks*/
    		JSONArray blocks = obj.getJSONArray("blocks");
    		allBlocks = getOptionSuboptionBody(blocks);

    		/* Add sub option block separately if any exists*/
    		if (section.getSubOption() != null) {
    			subOptionJson 						= suboptions.get(section.getSubOption()).getBody();
    			obj 								= new JSONObject(subOptionJson);
        		blocks 								= obj.getJSONArray("blocks");
        		ArrayList<Block> suboptionBlocks 	= getOptionSuboptionBody(blocks);
        		allBlocks.addAll(suboptionBlocks);    		
        	}
    			
    		providerSection.setBlocks(allBlocks);
    		allSections.add(providerSection);
    		
    		prevIndex = sectionIndex;
    	}

    	/* Create the combined fonts*/
    	for (int k = 0 ; k < allSections.size() ; k++) {
	    	for (int i = 0 ; i < allSections.get(k).getBlocks().size() ; i++) {
	    		for (int j = 0 ; j < allSections.get(k).getBlocks().get(i).getBlockStyles().size() ; j++) {
	    			if (j > 0 && allSections.get(k).getBlocks().get(i).getBlockStyles().get(j).getOffset() == allSections.get(k).getBlocks().get(i).getBlockStyles().get(j-1).getOffset()) {
	    				allSections.get(k).getBlocks().get(i).getBlockStyles().get(j-1).setStyle(allSections.get(k).getBlocks().get(i).getBlockStyles().get(j-1).getStyle() + "-" + allSections.get(k).getBlocks().get(i).getBlockStyles().get(j).getStyle());
	    				allSections.get(k).getBlocks().get(i).getBlockStyles().remove(j);
	    			}
	    		}
	    	}
    	}
    	
    	/* If the contract type is a user contract, rebuild all blocks and blockstyles with the provider, consumer and product information*/
    	if (command.getType() == EnumContract.USER_CONTRACT) {
    		createKeywordMapping();
    		allSections = addOrderInformation(allSections, contractParametersDto);
    	}
		
		/* Add contract title and subtitle*/
		addTitle("Title", title, true, true);
		if (subtitle != null) {
			addTitle("Subtitle", subtitle, true, false);
		}
		else {
			addTitle("Subtitle", "" , true, false);
		}
		
		/* For all sections*/
    	for (int k = 0 ; k < allSections.size() ; k++) {		
    		
    		setPreviousBlockIsListItem(false);
    		
    		/* Add section title*/
    		String sectionTitle = allSections.get(k).getSectionTitle();   	

    		int occuranceOfDots = StringUtils.countOccurrencesOf(sectionTitle, ".");
    		if (occuranceOfDots == 0) {
    			addTitle("Sectiontitle", sectionTitle, true, false);
    		}
    		else {
    			addTitle("SectionSubTitle", sectionTitle, true, false);
    		}
    			
    		/* Add blocks*/
	    	for (int i = 0 ; i < allSections.get(k).getBlocks().size() ; i++) {
	    		if (allSections.get(k).getBlocks().size() == 1 && 
	    				(allSections.get(k).getBlocks().get(i).getText().equals(" ") 	||
	    				allSections.get(k).getBlocks().get(i).getText().equals(" ") 	|| 
	    				allSections.get(k).getBlocks().get(i).getText().equals("") 		|| 
	    				allSections.get(k).getBlocks().get(i).getText().equals("\n"))) {
	    			continue;
	    		}
	    		else if (i != allSections.get(k).getBlocks().size()-1) {
	    			addBlock(allSections.get(k).getBlocks().get(i), true, false, false, i);
	    		}
	    		else {
	    			addBlock(allSections.get(k).getBlocks().get(i), true, false, true, i);
	    		}
	    	}
    	}
		
    	/* Add metadata, header and footer*/
		addMetadata();
		addHeaderAndFooter(title);
		
		this.currentPDPageContentStream.close();
		
		/* Save the document*/
		document.save(filePath);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		document.save(byteArrayOutputStream);
		InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
		
		/* Close the document*/  
		document.close();
		
		return inputStream.readAllBytes();

	}
	

}