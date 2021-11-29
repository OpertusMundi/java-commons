package eu.opertusmundi.common.model.sinergise;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorDto {

    private int    status;
    private String reason;
    private String message;
    private String code;

}
