package eu.opertusmundi.common.model.workflow;

public enum EnumProcessInstanceVariable {
    START_USER_KEY("startUserKey"),
    BPMN_BUSINESS_ERROR_DETAILS("bpmnBusinessErrorDetails"),
    BPMN_BUSINESS_ERROR_MESSAGES("bpmnBusinessErrorMessages"),
    HELPDESK_ERROR_MESSAGE("helpdeskErrorMessage"),
    BPMN_ERROR_CODE("bpmnErrorCode")
    ;

    private String value;

    EnumProcessInstanceVariable(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

}