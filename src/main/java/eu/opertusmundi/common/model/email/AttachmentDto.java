package eu.opertusmundi.common.model.email;

import lombok.Getter;

public class AttachmentDto {


    @Getter
    private final String fileName;

    @Getter
    private final byte[] fileData;
    
    @Getter
    private final String fileType;

    public AttachmentDto(String fileName, byte[] fileData, String fileType) {
    	this.fileName = fileName;
    	this.fileData = fileData;
    	this.fileType = fileType;
    }

}
