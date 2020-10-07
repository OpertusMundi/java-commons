package eu.opertusmundi.common.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = GenericMessageCode.class)
public interface MessageCode {

    String key();

}
