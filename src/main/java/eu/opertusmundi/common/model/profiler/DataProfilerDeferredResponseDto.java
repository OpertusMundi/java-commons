package eu.opertusmundi.common.model.profiler;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class DataProfilerDeferredResponseDto {

    @JsonProperty("endpoint")
    private String resourceEndpoint;

    @JsonProperty("status")
    private String statusEndpoint;

    private String ticket;

}
