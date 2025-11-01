package com.example.jira.web.dto;

public class TicketAssignedEvent extends TicketEvent {
    public TicketAssignedEvent() {
        super();
        setEventType("TICKET_ASSIGNED");
    }
}