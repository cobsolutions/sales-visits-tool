package com.sales.visits.app.sales.exception;

public class DuplicateNpiException extends RuntimeException {
    public DuplicateNpiException(String npi) {
        super("A physician with NPI " + npi + " already exists");
    }
}
