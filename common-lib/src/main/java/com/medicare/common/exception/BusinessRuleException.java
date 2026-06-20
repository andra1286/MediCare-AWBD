package com.medicare.common.exception;

/** Thrown when a domain/business rule is violated (see BUSINESS_RULES.md). Maps to HTTP 422. */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
