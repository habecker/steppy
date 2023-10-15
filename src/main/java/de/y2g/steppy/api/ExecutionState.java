package de.y2g.steppy.api;

import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"unchecked"})
class ExecutionState {
    private final ConcurrentHashMap<String, Object> state = new ConcurrentHashMap<>();

    <T> void setState(String runtimeStepInstanceId, String varName, T value) {
        state.put(createIdentifier(runtimeStepInstanceId, varName), value);
    }

    <T> T getState(String runtimeStepInstanceId, String varName) {
        return (T) state.get(createIdentifier(runtimeStepInstanceId, varName));
    }

    private String createIdentifier(String runtimeStepInstanceId, String varName) {
        return String.format("%s-%s", runtimeStepInstanceId, varName);
    }
}
