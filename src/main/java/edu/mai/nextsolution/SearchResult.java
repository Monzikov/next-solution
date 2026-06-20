package edu.mai.nextsolution;

import java.util.Collections;
import java.util.List;

public record SearchResult(String checkable, Status status, String message, List<Figurant> figurants) {
    public enum Status {
        APPROVED, REJECTED, MANUAL_REVIEW
    }

    public SearchResult(String checkable, Status status, String message) {
        this(checkable, status, message, Collections.emptyList());
    }

    public SearchResult(String checkable, Status status, String message, List<Figurant> figurants) {
        this.checkable = checkable;
        this.status = status;
        this.message = message;
        this.figurants = figurants == null ? Collections.emptyList() : figurants;
    }
}
