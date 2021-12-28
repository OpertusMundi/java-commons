package eu.opertusmundi.common.model.account;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
    @Type(name = "INDIVIDUAL", value = ConsumerIndividualCommandDto.class),
    @Type(name = "PROFESSIONAL", value = ConsumerProfessionalCommandDto.class),
})
public class ConsumerCommandDto extends CustomerCommandDto {

    protected ConsumerCommandDto() {
        super(EnumMangopayUserType.UNDEFINED);
    }

    protected ConsumerCommandDto(EnumMangopayUserType type) {
        super(type);
    }

}
