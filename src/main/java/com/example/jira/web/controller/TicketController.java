package com.example.jira.web.controller;

import com.example.jira.web.model.Ticket;
import com.example.jira.web.model.TicketStatus;
import com.example.jira.web.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin
public class TicketController {

    private final TicketService ticketService;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TicketController.class);

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public List<Ticket> getAllTickets() {
        logger.info("Récupération de tous les tickets");
        List<Ticket> tickets = ticketService.getAllTickets();
        logger.info("Nombre de tickets trouvés : {}", tickets.size());
        return tickets;
    }

    @GetMapping("/{id}")
    public Ticket getTicketById(@PathVariable Long id) {
        logger.info("Récupération du ticket avec l'ID : {}", id);
        Ticket ticket = ticketService.getTicketById(id);
        logger.info("Ticket trouvé : {}", ticket);
        return ticket;
    }

    @PostMapping("/create")
    public ResponseEntity<Ticket> createTicket(@RequestBody Ticket ticket) {
        logger.info("Création d'un nouveau ticket : {}", ticket);
        Ticket createdTicket = ticketService.createTicket(ticket);
        logger.info("Ticket créé avec succès : {}", createdTicket);
        return ResponseEntity.ok(createdTicket);
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<Ticket> assignTicket(@PathVariable Long id, @RequestParam String assignee) {
        Ticket updatedTicket = ticketService.assignTicket(id, assignee);
        return ResponseEntity.ok(updatedTicket);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Ticket> updateTicketStatus(@PathVariable Long id, @RequestParam TicketStatus status) {
        Ticket updatedTicket = ticketService.updateTicketStatus(id, status);
        return ResponseEntity.ok(updatedTicket);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.ok().build();
    }
}
