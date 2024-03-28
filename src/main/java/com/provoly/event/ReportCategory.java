package com.provoly.event;

public enum ReportCategory {
    REPORT;

    public static boolean isReportCategory(String category) {
        try {
            valueOf(category);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }
}
