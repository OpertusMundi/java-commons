package eu.opertusmundi.common.model.profiler;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataProfilerStatusResponseDto {

    private String comment;

    private boolean completed;

    @JsonProperty("execution_time(s)")
    private String executionTime;

    private String requested;

    private boolean success;

}
