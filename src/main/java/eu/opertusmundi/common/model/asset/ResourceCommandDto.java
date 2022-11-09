package eu.opertusmundi.common.model.asset;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public abstract class ResourceCommandDto {

    public ResourceCommandDto(UUID publisherKey, UUID ownerKey, UUID draftKey) {
        super();
        this.publisherKey = publisherKey;
        this.ownerKey     = ownerKey;
        this.draftKey     = draftKey;
    }

    /**
     * The publisher key
     *
     * If this is a vendor account (with role `ROLE_VENDOR_PROVIDER`), the
     * publisher key is the unique key of the parent account. If this is a
     * provider account (with role `ROLE_PROVIDER`, this is the unique key of
     * the authenticated user.
     */
    @JsonIgnore
    private UUID publisherKey;

    /**
     * The authenticated user key
     */
    @JsonIgnore
    private UUID ownerKey;

    /**
     * Draft key is set by the calling service
     */
    @JsonIgnore
    private UUID draftKey;

}
