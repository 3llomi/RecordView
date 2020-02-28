package com.devlomi.record_view;

class RecordDurationBoundariesException extends IllegalArgumentException {

    RecordDurationBoundariesException() {
        super("Minimum Duration must have a positive value, and must be smaller than the Maximum duration value if and only if the Max duration is set (has positive value)");
    }
}
