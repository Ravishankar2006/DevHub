package com.devhub.ai;

public enum AiActionType {
    CREATE_TASK, UPDATE_TASK, DELETE_TASK,
    CREATE_HABIT, UPDATE_HABIT, DELETE_HABIT,
    CREATE_NOTE, UPDATE_NOTE, DELETE_NOTE,
    CREATE_CALENDAR_EVENT, UPDATE_CALENDAR_EVENT, DELETE_CALENDAR_EVENT,
    CREATE_GOAL, UPDATE_GOAL, DELETE_GOAL,
    CREATE_PROJECT, UPDATE_PROJECT, DELETE_PROJECT;

    public static AiActionType fromToolName(String toolName) {
        return AiActionType.valueOf(toolName.toUpperCase());
    }

    public String toToolName() {
        return name().toLowerCase();
    }
}
