package com.example.jira.web.dto;

public class TicketClosedEvent extends TicketEvent {
    public TicketClosedEvent() {
        super();
        setEventType("TICKET_CLOSED");
    }
}