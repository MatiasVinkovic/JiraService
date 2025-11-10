# JiraService — README explicatif

Ce projet est une petite application Spring Boot qui implémente un service de gestion de "tickets" (similaire à un ticket Jira simplifié). Ce README explique l'architecture, les classes principales, les endpoints exposés et comment présenter le code à votre groupe.

## Objectif

L'application permet de créer, récupérer, modifier (assigner / changer le statut) et supprimer des tickets. Elle utilise Spring Data JPA pour la persistance et publie des événements internes lorsque des actions importantes surviennent (création, assignation, fermeture).

## Structure du projet (fichiers clés)

- `com.example.jira.JiraApplication` : point d'entrée Spring Boot.
- `web/controller/TicketController` : REST API exposant les endpoints `/api/tickets`.
- `web/service/TicketService` : logique métier principale (validation, création, assignation, changement de statut, publication d'événements).
- `web/service/TicketEventPublisher` : couche pour publier des événements Spring (wrapping ApplicationEventPublisher).
- `web/service/TicketEventListener` : écoute certains événements (ex. création) et logge/traite ce qui est nécessaire.
- `web/model/Ticket` : entité JPA représentant un ticket (champs, comportements liés au changement d'état, dates).
- `web/model/TicketHistory` : entité pour historiser les changements (lié à un `Ticket`).
- `web/model/TicketStatus` : énumération des statuts (CREATED, ASSIGNED, IN_PROGRESS, BLOCKED, RESOLVED, CLOSED).
- `web/repository/TicketRepository` : repository JPA pour `Ticket`.
- `web/repository/TicketHistoryRepository` : repository JPA pour `TicketHistory`.
- `web/dto/*Event` : classes DTO d'événements héritant de `TicketEvent` (TicketCreatedEvent, TicketAssignedEvent, TicketClosedEvent).
- `web/exceptions/*` : exceptions custom et `GlobalExceptionHandler` pour retourner des erreurs HTTP structurées.
- `web/config/JpaConfig` : configuration JPA minimale (auditing stub).

## Modèle de données (points importants)

- `Ticket` contient : id, title, description, status (enum), priority (LOW/MEDIUM/HIGH), assignee, createdDate, assignedDate, lastUpdatedDate, closedDate.
- Validation sur les champs : title non vide, longueur max pour title & description, priority limité à LOW/MEDIUM/HIGH.
- Changement d'assignee met à jour `assignedDate` et peut changer le status en `ASSIGNED` si le ticket était `CREATED`.
- Changement de statut met à jour `lastUpdatedDate` et, si `CLOSED`, renseigne `closedDate`.
- `TicketHistory` permet d'enregistrer les modifications (champ, ancienne valeur, nouvelle valeur, qui a modifié, date).

## Logique métier (résumé)

- `TicketService.createTicket` :
  - Valide les données (titre obligatoire, longueurs, etc.).
  - Vérifie les doublons (`existsByTitle`).
  - Initialise des valeurs par défaut (status `CREATED`, priorité `MEDIUM` si non fournie).
  - Sauvegarde le ticket via le repository.
  - Publie un `TicketCreatedEvent` (sans bloquer la création si la publication échoue).

- `assignTicket` : assigne un ticket à un utilisateur, sauvegarde et publie `TicketAssignedEvent`.

- `updateTicketStatus` : met à jour le statut, sauvegarde ; si le statut devient `CLOSED` publie `TicketClosedEvent`.

- `getAllTickets`, `getTicketById`, `deleteTicket` : opérations CRUD basiques.

## API REST (endpoints)

Base : `/api/tickets`

- GET `/api/tickets` : retourne la liste de tous les tickets.
- GET `/api/tickets/{id}` : récupère un ticket par id.
- POST `/api/tickets/create` : crée un ticket — corps JSON correspondant à l'entité `Ticket` (title, description, priority optionnel, assignee optionnel).
- PUT `/api/tickets/{id}/assign?assignee=NAME` : assigne le ticket.
- PUT `/api/tickets/{id}/status?status=STATUS` : met à jour le statut (valeurs autorisées : CREATED, ASSIGNED, IN_PROGRESS, BLOCKED, RESOLVED, CLOSED).
- DELETE `/api/tickets/{id}` : supprime un ticket.

Exemples curl (à présenter) :

```bash
# Créer un ticket
curl -X POST -H "Content-Type: application/json" -d '{"title":"Bug A","description":"Description du bug"}' http://localhost:8080/api/tickets/create

# Lister tous les tickets
curl http://localhost:8080/api/tickets

# Assigner
curl -X PUT "http://localhost:8080/api/tickets/1/assign?assignee=Alice"

# Fermer
curl -X PUT "http://localhost:8080/api/tickets/1/status?status=CLOSED"
```

## Gestion des erreurs

- `TicketNotFoundException` → 404
- Validation (Bean Validation) → 400 avec liste des champs invalides (gérée par `GlobalExceptionHandler`).
- Erreurs non prévues → 500 (message générique)

## Événements internes

- Lors de la création, assignation et fermeture, la couche `TicketEventPublisher` publie des événements Spring basés sur des DTO (`TicketEvent` et sous-classes).
- `TicketEventListener` écoute l'événement de création et logge l'information. Ce pattern permet d'ajouter ultérieurement d'autres listeners (notifications, analytics, historisation) sans coupler la logique métier.

## Points techniques et éléments à mentionner lors de la présentation

- Le projet utilise Spring Boot + Spring Data JPA.
- Les entités JPA (`@Entity`) sont dans `web/model` et les repositories `JpaRepository` fournissent CRUD.
- Validation déclarative via annotations (`@NotBlank`, `@Size`, `@Pattern`).
- Publication d'événements via `ApplicationEventPublisher` et `@EventListener`.
- `TicketService.createTicket` est `synchronized` pour éviter des conditions de concurrence simples sur la création (approche simple ; discuter des limites).
- `JpaConfig` contient un `AuditorAware` qui renvoie `system` — expliquer que c'est un stub et qu'on peut lier un utilisateur authentifié pour l'audit.

## Pour lancer l'application (local)

1. Assurez-vous d'avoir Java 11+ et Maven installés.
2. Dans le répertoire racine du projet :

```bash
mvn spring-boot:run
```

L'application démarre sur `http://localhost:8080` par défaut (si la configuration des ports n'a pas été modifiée). Si vous avez une base de données externe, vérifier `src/main/resources/application.properties`.

## Suggestions pour la démo en groupe

- Montrez d'abord le modèle (`Ticket`) pour expliquer les champs et la logique des dates.
- Ouvrez `TicketService` pour expliquer validations, initialisations et publication d'événements.
- Montrez `TicketController` et faites un test live en appelant `POST /api/tickets/create` puis `GET /api/tickets`.
- Expliquez comment les exceptions sont gérées par `GlobalExceptionHandler` (montrer un exemple de 400 en envoyant un titre vide).
- Discutez brièvement des améliorations possibles : authentification, audits réels, tests unitaires/intégration, gestion robuste des événements (retry, queue), tests de concurrence.

## Fichiers additionnels

- `src/main/resources/schema.sql` : script SQL initial (si utilisé pour démarrer la BD).
- `pom.xml` : dépendances Maven (Spring Boot Starter Web, Data JPA, etc.).

## Remarques finales

Ce README est conçu pour vous aider à présenter le code en situation de groupe. Si vous voulez, je peux :

- Ajouter une section "slides" courte avec 5-6 points clés pour la présentation.
- Générer des exemples JSON plus détaillés pour la création et la validation des tickets.
- Ajouter un petit guide de tests unitaires/integ (ex : tests pour `TicketService`).

Dites-moi si vous voulez que j'ajoute l'un de ces items ou que j'adapte le README pour une version anglaise.
