package edu.mai.nextsolution;

import java.util.Collections;
import java.util.List;

public class CheckResult {
    public enum Status {
        APPROVED, REJECTED, MANUAL_REVIEW
    }

    private final Status status;
    private final String message;
    private final List<Figurant> figurants;

    public CheckResult(Status status, String message) {
        this(status, message, Collections.emptyList());
    }

    public CheckResult(Status status, String message, List<Figurant> figurants) {
        this.status = status;
        this.message = message;
        this.figurants = figurants == null ? Collections.emptyList() : figurants;
    }

    public Status getStatus() { return status; }
    public String getMessage() { return message; }
    public List<Figurant> getFigurants() { return figurants; }
}
