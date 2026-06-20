package edu.mai.nextsolution;

import java.util.Collections;
import java.util.List;

public record SearchResult(String checkable, Status status, String message, List<Figurant> figurants_red, List<Figurant> figurants_yellow, List<Figurant> figurants_green) {
    public enum Status {
        APPROVED, REJECTED, MANUAL_REVIEW
    }

    public SearchResult(String checkable, Status status, String message) {
        this(checkable, status, message, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    public SearchResult(String checkable, Status status, String message, List<Figurant> figurants_red, List<Figurant> figurants_yellow, List<Figurant> figurants_green) {
        this.checkable = checkable;
        this.status = status;
        this.message = message;
        this.figurants_green = figurants_green == null ? Collections.emptyList() : figurants_green;
        this.figurants_yellow = figurants_yellow == null ? Collections.emptyList() : figurants_yellow;
        this.figurants_red = figurants_red == null ? Collections.emptyList() : figurants_red;
    }
}
