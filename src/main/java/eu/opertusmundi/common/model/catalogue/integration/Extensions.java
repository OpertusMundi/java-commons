package eu.opertusmundi.common.model.catalogue.integration;

import java.io.Serializable;

import javax.validation.Valid;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Extensions implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Sentinel hub custom properties")
    @Valid
    private SentinelHubProperties sentinelHub;

}
