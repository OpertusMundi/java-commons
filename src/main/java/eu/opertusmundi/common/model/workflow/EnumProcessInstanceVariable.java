package eu.opertusmundi.common.model.workflow;

public enum EnumProcessInstanceVariable {
    START_USER_KEY("startUserKey"),
    ;

    private String value;

    EnumProcessInstanceVariable(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

}