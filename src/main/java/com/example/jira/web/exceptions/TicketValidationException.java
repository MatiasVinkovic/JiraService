package com.example.jira.web.exceptions;

public class TicketValidationException extends RuntimeException {
    public TicketValidationException(String message) {
        super(message);
    }
}