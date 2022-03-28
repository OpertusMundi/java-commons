package eu.opertusmundi.common.model.account;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
    @Type(name = "INDIVIDUAL", value = ConsumerIndividualCommandDto.class),
    @Type(name = "PROFESSIONAL", value = ConsumerProfessionalCommandDto.class),
})
@Schema(
    description = "Consumer registration command",
    required = true,
    discriminatorMapping = {
        @DiscriminatorMapping(value = "INDIVIDUAL", schema = ConsumerIndividualCommandDto.class),
        @DiscriminatorMapping(value = "PROFESSIONAL", schema = ConsumerProfessionalCommandDto.class)
    }
)
public class ConsumerCommandDto extends CustomerCommandDto {

    protected ConsumerCommandDto() {
        super(EnumMangopayUserType.UNDEFINED);
    }

    protected ConsumerCommandDto(EnumMangopayUserType type) {
        super(type);
    }

}
