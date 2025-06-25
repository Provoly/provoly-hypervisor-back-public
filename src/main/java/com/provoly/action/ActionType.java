package com.provoly.action;

import java.util.Arrays;

public enum ActionType {
    ASKED_SERVICE,
    ALERT_ELECTED,
    OTHER,
    PHONE,
    SMS,
    EMAIL;

    public static boolean isDefaultActionType(String type) {
        return Arrays.stream(values()).anyMatch(val -> val.name().equals(type.toUpperCase()));
    }

}
