package com.devhub.ai;

import java.util.HashMap;
import java.util.Map;

public record AgentToolResult(boolean success, Map<String, Object> payload) {

    public static AgentToolResult ok(Map<String, Object> payload) {
        Map<String, Object> withStatus = new HashMap<>(payload);
        withStatus.put("status", "success");
        return new AgentToolResult(true, withStatus);
    }

    public static AgentToolResult error(String message) {
        return new AgentToolResult(false, Map.of("status", "error", "message", message));
    }
}
