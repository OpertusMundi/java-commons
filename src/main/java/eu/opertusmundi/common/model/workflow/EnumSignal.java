package eu.opertusmundi.common.model.workflow;

import lombok.Getter;

@Getter
public enum EnumSignal {
    CANCEL_PUBLISH_ASSET("cancel_signal", "cancelSignal"),
    ;

    /**
     * The signal name as declared in the workflow definition
     */
    private String signalName;

    /**
     * The variable name to store the signal type
     */
    private String variableName;

    EnumSignal(String signalName, String variableName) {
        this.signalName   = signalName;
        this.variableName = variableName;
    }
}
