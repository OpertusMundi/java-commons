package eu.opertusmundi.common.model.catalogue.elastic;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateIndexCommand {

    private JsonNode mappings;

    private JsonNode settings;

}
