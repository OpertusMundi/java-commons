package eu.opertusmundi.common.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecordLockDto {

    private long id;

    private EnumRecordLock recordType;

    private int recordId;

    private int ownerId;

    private String ownerEmail;

}
