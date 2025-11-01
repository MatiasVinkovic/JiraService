package com.example.jira.web.service;

import com.example.jira.web.dto.TicketCreatedEvent;
import com.example.jira.web.model.Ticket;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class TicketEventListener {

    private final TicketService ticketService;

    public TicketEventListener(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @EventListener
    public void handleTicketCreatedEvent(TicketCreatedEvent event) {
        // Log l'événement sans créer un nouveau ticket pour éviter la boucle infinie
        System.out.println("Ticket créé : " + event.getTicketTitle());
    }
}