package com.example.jira.web.dto;

public class TicketCreatedEvent extends TicketEvent {
    public TicketCreatedEvent() {
        super();
        setEventType("TICKET_CREATED");
    }
}