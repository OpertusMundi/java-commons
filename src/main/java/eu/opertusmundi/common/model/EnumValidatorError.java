package eu.opertusmundi.common.model;

import lombok.Getter;

public enum EnumValidatorError {

    FileNotFound("File was not found"),
    FileExtensionNotSupported("File name extension is not supported"),
    NotEqual("The value of the property does not match the value of a related property"),
    NotUnique("The value of the property must be unique"),
    NotUpdatable("The value of the property can not be updated"),
    NotValid("The property value is not valid"),
    OptionNotFound("The selected value for the property is not allowed"),
    OptionNotEnabled("The value for the property is allowed but it is not enabled"),
    OptionNotSupported("The value for the property is not supported in the current context"),
    OperationNotSupported("The operation described by the property is not supported"),
    ResourceNotFound("The resource referenced by the property does not exist"),

    NotEmpty("The value must not be null or empty"),
    Size("The size of the collection is not supported"),
    ;

    @Getter
    private String description;

    EnumValidatorError(String description) {
        this.description = description;
    }

}
