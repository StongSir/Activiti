package org.activiti.runtime.api.model.builders;

import java.util.ArrayList;
import java.util.List;

import org.activiti.runtime.api.model.ProcessInstance;
import org.activiti.runtime.api.model.payloads.RemoveProcessVariablesPayload;

public class RemoveVariablesPayloadBuilder {

    private String processInstanceId;
    private ProcessInstance processInstance;
    private List<String> variableNames = new ArrayList<>();
    private boolean localOnly = false;

    public RemoveVariablesPayloadBuilder withProcessInstanceId(String processDefinitionId) {
        this.processInstanceId = processDefinitionId;
        return this;
    }

    public RemoveVariablesPayloadBuilder withProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;
        return this;
    }

    public RemoveVariablesPayloadBuilder withVariableNames(String variableName) {
        if (variableNames == null) {
            variableNames = new ArrayList<>();
        }
        variableNames.add(variableName);
        return this;
    }

    public RemoveVariablesPayloadBuilder localOnly(boolean localOnly) {
        this.localOnly = localOnly;
        return this;
    }

    public RemoveVariablesPayloadBuilder withVariableNames(List<String> variableNames) {
        this.variableNames = variableNames;
        return this;
    }

    public RemoveProcessVariablesPayload build() {
        if (processInstance != null) {
            return new RemoveProcessVariablesPayload(processInstance.getId(),
                                                     variableNames,
                                                     localOnly);
        }
        return new RemoveProcessVariablesPayload(processInstanceId,
                                                 variableNames,
                                                 localOnly);
    }
}