package eu.opertusmundi.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.rest.dto.VariableValueDto;

import eu.opertusmundi.common.model.workflow.EnumSignal;

public class BpmInstanceVariablesBuilder {

    private final Map<String, VariableValueDto> variables = new HashMap<>();

    private BpmInstanceVariablesBuilder() {

    }

    public static BpmInstanceVariablesBuilder builder() {
        return new BpmInstanceVariablesBuilder();
    }

    public BpmInstanceVariablesBuilder variableAsBoolean(String name, Boolean value) {
        this.variable("Boolean", name, value);
        return this;
    }

    public BpmInstanceVariablesBuilder variableAsString(String name, String value) {
        this.variable("String", name, value);
        return this;
    }

    public BpmInstanceVariablesBuilder variableAsInteger(String name, Integer value) {
        this.variable("Integer", name, value);
        return this;
    }

    public BpmInstanceVariablesBuilder variableAsUuid(String name, UUID value) {
        this.variable("String", name, value);
        return this;
    }

    public BpmInstanceVariablesBuilder variable(EnumSignal signal) {
        this.variable("String", signal.getVariableName(), signal.name());
        return this;
    }

    public BpmInstanceVariablesBuilder variable(String type, String name, Object value) {
        final VariableValueDto v = new VariableValueDto();

        v.setValue(value);
        v.setType(type);

        variables.put(name, v);

        return this;
    }

    public Map<String, VariableValueDto> build() {
        return this.variables;
    }

    public Map<String, Object> buildValues() {
        return this.variables.entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> e.getValue().getValue()
        ));
    }


}
