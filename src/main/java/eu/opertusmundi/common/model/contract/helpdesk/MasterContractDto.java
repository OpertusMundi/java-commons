package eu.opertusmundi.common.model.contract.helpdesk;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.opertusmundi.common.model.account.helpdesk.SimpleHelpdeskAccountDto;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Getter
@Setter
public class MasterContractDto implements Serializable {

    protected static final long serialVersionUID = 1L;

    @Hidden
    @JsonInclude(Include.NON_NULL)
    protected Integer id;

    @Hidden
    @JsonIgnore
    protected Integer contractRootId;

    @Hidden
    @JsonProperty("parentId")
    @JsonInclude(Include.NON_NULL)
    protected Integer contractParentId;

    @Schema(description = "Unique key")
    protected UUID key;

    @Schema(hidden = true, description = "The owner of this contract")
    @JsonInclude(Include.NON_NULL)
    protected SimpleHelpdeskAccountDto owner;

    @Schema(description = "Title")
    protected String title;

    @Schema(description = "Subtitle")
    protected String subtitle;

    @Schema(description = "Version")
    protected String version;

    @Schema(description = "Creation date in ISO format")
    protected ZonedDateTime createdAt;

    @Schema(description = "Date of last update in ISO format")
    protected ZonedDateTime modifiedAt;

    @Schema(description = "Sections")
    @ArraySchema(arraySchema = @Schema(description = "Contract sections"), minItems = 0)
    @JsonInclude(Include.NON_EMPTY)
    protected List<MasterSectionDto> sections;

    public void removeHelpdeskData() {
        this.id    = null;
        this.owner = null;
        this.contractParentId = null;
        this.contractRootId = null;
    }

}
