package com.example.jira.web.service;

import com.example.jira.web.dto.TicketEvent;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;

@Service
public class TicketEventPublisher {
    
    private final ApplicationEventPublisher eventPublisher;

    public TicketEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publishEvent(TicketEvent event) {
        eventPublisher.publishEvent(event);
    }
}