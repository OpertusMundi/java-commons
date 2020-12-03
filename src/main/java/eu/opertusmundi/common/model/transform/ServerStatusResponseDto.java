package eu.opertusmundi.common.model.transform;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerStatusResponseDto {

    private String comment;

    private boolean completed;

    private Double executionTime;

    private LocalDateTime requested;

    private boolean success;

}
