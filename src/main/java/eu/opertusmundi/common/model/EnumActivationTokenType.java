package eu.opertusmundi.common.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum EnumActivationTokenType {

    ACCOUNT,
    PROVIDER,
    ;

}
