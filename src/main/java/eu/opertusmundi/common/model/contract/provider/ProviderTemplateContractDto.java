package eu.opertusmundi.common.model.contract.provider;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.opertusmundi.common.model.account.SimpleAccountDto;
import eu.opertusmundi.common.model.contract.helpdesk.MasterContractHistoryDto;
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
public class ProviderTemplateContractDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private Integer id;

    @JsonIgnore
    private UUID contractRootKey;

    @JsonProperty("parentId")
    @JsonInclude(Include.NON_NULL)
    @Schema(description = "Parent template id")
    private UUID contractParentKey;

    @Schema(description = "Unique key")
    private UUID key;

    @Schema(description = "Master template key")
    private UUID templateKey;

    @Schema(hidden = true, description = "The owner of this contract")
    @JsonInclude(Include.NON_NULL)
    private SimpleAccountDto owner;

    @Schema(description = "Title")
    @NotEmpty
    private String title;

    @Schema(description = "Subtitle")
    private String subtitle;

    @Schema(description = "Version")
    private String version;

    @Schema(description = "Date of creation in ISO format")
    private ZonedDateTime createdAt;

    @Schema(description = "Date of last update in ISO format")
    private ZonedDateTime modifiedAt;

    @Schema(description = "Sections")
    @ArraySchema(
        arraySchema = @Schema(
            description = "Contract sections"
        ),
        minItems = 0
    )
    @JsonInclude(Include.NON_EMPTY)
    private List<ProviderTemplateSectionDto> sections;

    @Schema(description = "Parent master contract")
    @JsonInclude(Include.NON_NULL)
    private MasterContractHistoryDto masterContract;

    @Schema(description = "True if this is the default contract")
    private boolean defaultContract;
    
    @Schema(description = "True if the provider has read and accepted the default contract")
    private boolean defaultContractAccepted;
    
    public void removeHelpdeskData() {
        this.owner = null;
    }
}
