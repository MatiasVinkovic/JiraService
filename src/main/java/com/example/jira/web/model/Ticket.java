package com.example.jira.web.model;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "ticket")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 3, max = 255, message = "Le titre doit contenir entre 3 et 255 caractères")
    @Column(nullable = false)
    private String title;
    
    @Column(length = 1000)
    @Size(max = 1000, message = "La description ne doit pas dépasser 1000 caractères")
    private String description;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Le statut est obligatoire")
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.CREATED;

    @Pattern(regexp = "^(LOW|MEDIUM|HIGH)$", message = "La priorité doit être LOW, MEDIUM ou HIGH")
    @Column(nullable = false)
    private String priority = "MEDIUM";

    @Size(max = 100, message = "Le nom de l'assigné ne doit pas dépasser 100 caractères")
    private String assignee;

    @Column(nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignedDate;

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdatedDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closedDate;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketHistory> history = new ArrayList<>();

    // Constructeur par défaut

    public Ticket() {
        this.createdDate = LocalDateTime.now();
        this.lastUpdatedDate = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
        this.lastUpdatedDate = LocalDateTime.now();
        if (status == TicketStatus.CLOSED) {
            this.closedDate = LocalDateTime.now();
        }
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
        this.lastUpdatedDate = LocalDateTime.now();
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
        if (assignee != null && !assignee.isEmpty()) {
            this.assignedDate = LocalDateTime.now();
            if (this.status == TicketStatus.CREATED) {
                this.status = TicketStatus.ASSIGNED;
            }
        }
        this.lastUpdatedDate = LocalDateTime.now();
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getAssignedDate() {
        return assignedDate;
    }

    public LocalDateTime getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public LocalDateTime getClosedDate() {
        return closedDate;
    }
}
