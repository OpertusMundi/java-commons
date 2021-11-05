package eu.opertusmundi.common.model.catalogue.client;

import java.io.Serializable;
import java.util.UUID;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DraftFromAssetCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The publisher key
     *
     * If this is a vendor account (with role `ROLE_VENDOR_PROVIDER`), the
     * publisher key is the unique key of the parent account. If this is a
     * provider account (with role `ROLE_PROVIDER`), this is the unique key of
     * the authenticated user.
     */
    @JsonIgnore
    private UUID publisherKey;

    /**
     * The authenticated user key
     */
    @JsonIgnore
    private UUID ownerKey;

    @NotEmpty
    private String pid;

    /**
     * True if the record must be locked when the command executes
     */
    @JsonIgnore
    private boolean locked;

}
