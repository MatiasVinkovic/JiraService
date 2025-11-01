package com.example.jira.web.repository;

import com.example.jira.web.model.TicketHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long> {
    List<TicketHistory> findByTicket_IdOrderByChangedAtDesc(Long ticketId);
    void deleteByTicket_Id(Long ticketId);
}