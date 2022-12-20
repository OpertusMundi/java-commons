package eu.opertusmundi.common.model.catalogue.server;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import eu.opertusmundi.common.model.catalogue.IdVersionPair;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(staticName = "of")
@Getter
public class HistoryIdVersionQuery {

    @JsonProperty("id_version_pairs")
    private final List<IdVersionPair> pairs;

}
