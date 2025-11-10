package com.example.jira.web.service;

import com.example.jira.web.dto.TicketAssignedEvent;
import com.example.jira.web.dto.TicketClosedEvent;
import com.example.jira.web.dto.TicketCreatedEvent;
import com.example.jira.web.exceptions.TicketNotFoundException;
import com.example.jira.web.exceptions.TicketValidationException;
import com.example.jira.web.model.Ticket;
import com.example.jira.web.model.TicketHistory;
import com.example.jira.web.model.TicketStatus;
import com.example.jira.web.repository.TicketHistoryRepository;
import com.example.jira.web.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketHistoryRepository historyRepository;
    private final TicketEventPublisher eventPublisher;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TicketService.class);

    public TicketService(TicketRepository ticketRepository, 
                        TicketHistoryRepository historyRepository,
                        TicketEventPublisher eventPublisher) {
        this.ticketRepository = ticketRepository;
        this.historyRepository = historyRepository;
        this.eventPublisher = eventPublisher;
    }

    private void addHistory(Ticket ticket, String fieldName, String oldValue, String newValue) {
        TicketHistory history = new TicketHistory(ticket, fieldName, oldValue, newValue, "system");
        historyRepository.save(history);
        logger.debug("Historique ajouté pour le ticket {}: {} -> {}", ticket.getId(), oldValue, newValue);
    }

    private void validateTicket(Ticket ticket) {
        if (ticket == null) {
            logger.error("Tentative de création d'un ticket null");
            throw new TicketValidationException("Le ticket ne peut pas être null");
        }
        
        if (ticket.getTitle() == null || ticket.getTitle().trim().isEmpty()) {
            logger.error("Tentative de création d'un ticket sans titre");
            throw new TicketValidationException("Le titre du ticket est obligatoire");
        }
        
        if (ticket.getTitle().length() > 255) {
            logger.error("Titre du ticket trop long : {} caractères", ticket.getTitle().length());
            throw new TicketValidationException("Le titre du ticket ne doit pas dépasser 255 caractères");
        }
        
        if (ticket.getDescription() != null && ticket.getDescription().length() > 1000) {
            logger.error("Description du ticket trop longue : {} caractères", ticket.getDescription().length());
            throw new TicketValidationException("La description du ticket ne doit pas dépasser 1000 caractères");
        }

        logger.debug("Validation basique du ticket réussie");
    }

    private void checkDuplicateTicket(Ticket ticket) {
        if (ticketRepository.existsByTitle(ticket.getTitle())) {
            logger.error("Tentative de création d'un ticket en doublon : {}", ticket.getTitle());
            throw new TicketValidationException("Un ticket avec le même titre existe déjà");
        }
        logger.debug("Vérification des doublons réussie");
    }

    private void initializeTicketFields(Ticket ticket) {
        logger.debug("Initialisation des champs par défaut du ticket");
        
        // Initialiser le status
        ticket.setStatus(TicketStatus.CREATED);
        
        // Initialiser la priorité par défaut si non spécifiée
        if (ticket.getPriority() == null) {
            ticket.setPriority("MEDIUM");
        }
        
        logger.debug("Champs du ticket initialisés : status={}, priority={}", 
                    ticket.getStatus(), ticket.getPriority());
    }

    public List<Ticket> getAllTickets() {
        logger.debug("Récupération de tous les tickets de la base de données");
        List<Ticket> tickets = ticketRepository.findAll();
        logger.info("Nombre de tickets trouvés : {}", tickets.size());
        return tickets;
    }

    public Ticket getTicketById(Long id) {
        logger.debug("Recherche du ticket avec l'ID : {}", id);
        return ticketRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Ticket non trouvé avec l'ID : {}", id);
                    return new TicketNotFoundException("Ticket non trouvé avec l'ID : " + id);
                });
    }

    @Transactional
    public synchronized Ticket createTicket(Ticket ticket) {
        logger.debug("Début de la création d'un ticket : {}", ticket);
        
        // Validation du ticket
        validateTicket(ticket);
        
        // Vérifier si un ticket avec le même titre existe déjà
        checkDuplicateTicket(ticket);

        // Initialisation des champs par défaut
        initializeTicketFields(ticket);
        Ticket savedTicket = ticketRepository.save(ticket);
        
        // Ajouter l'historique de création
        addHistory(savedTicket, "status", null, savedTicket.getStatus().toString());
        if (savedTicket.getPriority() != null) {
            addHistory(savedTicket, "priority", null, savedTicket.getPriority());
        }
        
        try {
            TicketCreatedEvent event = new TicketCreatedEvent();
            event.setTicketId(savedTicket.getId());
            event.setTicketTitle(savedTicket.getTitle());
            event.setTicketDescription(savedTicket.getDescription());
            event.setStatus(savedTicket.getStatus().toString());
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            // Log l'erreur mais ne pas empêcher la création du ticket
            System.err.println("Erreur lors de la publication de l'événement: " + e.getMessage());
        }
        
        return savedTicket;
    }

    @Transactional
    public Ticket assignTicket(Long id, String assignee) {
        Ticket ticket = getTicketById(id);
        String oldAssignee = ticket.getAssignee();
        ticket.setAssignee(assignee);
        Ticket savedTicket = ticketRepository.save(ticket);

        // Ajouter l'historique du changement d'assignation
        addHistory(savedTicket, "assignee", oldAssignee, assignee);
        
        // Si le statut a changé (passage à ASSIGNED), on l'historise aussi
        if (savedTicket.getStatus() == TicketStatus.ASSIGNED) {
            addHistory(savedTicket, "status", TicketStatus.CREATED.toString(), TicketStatus.ASSIGNED.toString());
        }

        TicketAssignedEvent event = new TicketAssignedEvent();
        event.setTicketId(savedTicket.getId());
        event.setTicketTitle(savedTicket.getTitle());
        event.setStatus(savedTicket.getStatus().toString());
        event.setAssignee(savedTicket.getAssignee());
        eventPublisher.publishEvent(event);

        return savedTicket;
    }

    @Transactional
    public Ticket updateTicketStatus(Long id, TicketStatus status) {
        Ticket ticket = getTicketById(id);
        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(status);
        Ticket savedTicket = ticketRepository.save(ticket);

        // Ajouter l'historique du changement de statut
        addHistory(savedTicket, "status", oldStatus.toString(), status.toString());

        if (status == TicketStatus.CLOSED) {
            TicketClosedEvent event = new TicketClosedEvent();
            event.setTicketId(savedTicket.getId());
            event.setTicketTitle(savedTicket.getTitle());
            event.setStatus(savedTicket.getStatus().toString());
            event.setAssignee(savedTicket.getAssignee());
            eventPublisher.publishEvent(event);
        }

        return savedTicket;
    }

    public void deleteTicket(Long id) {
        if (!ticketRepository.existsById(id)) {
            throw new TicketNotFoundException("Ticket not found with id: " + id);
        }
        ticketRepository.deleteById(id);
    }
}
