# Outreach Engine

An automated B2B email outreach backend. Define a campaign, write a timed email sequence, enroll leads, and the engine sends each step on schedule, stopping the moment a lead unsubscribes.

Built for small businesses that don't have a sales team to do outbound prospecting by hand.

## Features

- **Campaigns and sequences.** Multi-step email sequences with per-step delays and `{{placeholder}}` personalization.
- **Scheduled dispatch.** A background scheduler sends due emails, safe to run on multiple instances at once.
- **Exactly-once sending.** Database-enforced idempotency means a crash or retry never sends the same email twice.
- **Compliance built in.** Every email carries a one-click unsubscribe link (RFC 8058). Opt-outs are permanent and checked before every send.
- **Multi-tenant.** Each user sees only their own campaigns, leads, and enrollments, enforced at the query level.
- **Local email capture.** Runs against MailHog, so no real email ever leaves your machine.
## Tech stack

Java 17 · Spring Boot 3 · Spring Security (JWT) · Spring Data JPA / Hibernate · PostgreSQL 16 · Flyway · Docker Compose · Testcontainers · Swagger / OpenAPI
 
---

## Quickstart

### Prerequisites

- Java 17+
- Docker Desktop, **running**
- Git
  No Maven install needed; the repo includes the Maven wrapper.

### 1. Clone

```bash
git clone https://github.com/Taihounoshujin/outreach-engine.git
cd outreach-engine
```

### 2. Start Postgres and MailHog

```bash
docker compose up -d
```

| Service | Address | Purpose |
|---|---|---|
| PostgreSQL | `localhost:5432` | Application database |
| MailHog SMTP | `localhost:1025` | Captures outgoing email |
| MailHog inbox | http://localhost:8025 | View every email the app sends |

Check both are up with `docker ps`.

### 3. Set a JWT secret

```bash
# macOS / Linux
export JWT_SECRET=$(openssl rand -base64 32)
```

```powershell
# Windows PowerShell
$env:JWT_SECRET = [Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Max 256 }))
```

### 4. Run

```bash
./mvnw spring-boot:run          # macOS / Linux
.\mvnw.cmd spring-boot:run      # Windows
```

The schema is created automatically on first startup.

**Want sample data?** Add `-Dspring-boot.run.profiles=dev`. This seeds a campaign, a lead, and an active enrollment, and the first email lands in MailHog about 10 seconds after startup.
 
---

## Try it in Swagger

Open http://localhost:8080/swagger-ui.html.

1. **Register** with `POST /api/auth/register`, then **log in** with `POST /api/auth/login` and copy the token.
2. Click **Authorize** and paste `Bearer <your-token>`.
3. **Create a campaign** with `POST /api/campaigns`:
```json
   { "name": "Columbia HVAC", "fromName": "Your Name", "fromEmail": "you@localhost", "dailyCap": 40 }
```
4. **Add sequence steps** with `POST /api/campaigns/{id}/steps`:
```json
   {
     "stepOrder": 1,
     "delayDays": 0,
     "subjectTemplate": "Quick question about {{company_name}}",
     "bodyTemplate": "<p>Hi {{contact_name}}, saw you're in {{industry}} around {{city}}.</p>"
   }
```
Available placeholders: `{{contact_name}}`, `{{company_name}}`, `{{industry}}`, `{{city}}`.
5. **Start the campaign** with `POST /api/campaigns/{id}/start`.
6. **Add a lead** with `POST /api/leads`.
7. **Enroll it** with `POST /api/campaigns/{id}/enrollments`:
```json
   { "leadIds": ["<lead-id>"] }
```
8. **Watch MailHog.** Within a minute the personalized email appears at http://localhost:8025.
9. **Unsubscribe.** Open the `List-Unsubscribe` link from the email's headers, then call `GET /api/campaigns/{id}/enrollments`. The enrollment now reads `UNSUBSCRIBED`.
---

## Running tests

```bash
./mvnw test
```

Integration tests run against a real PostgreSQL container via Testcontainers, so Docker must be running.

## Resetting

```bash
docker compose down -v    # wipes the database and the MailHog inbox
docker compose up -d
```

## Configuration

| Variable | Default | Description |
|---|---|---|
| `JWT_SECRET` | *(required)* | Token signing key |
| `DB_URL` | `jdbc:postgresql://localhost:5432/outreach` | Database URL |
| `DB_USER` / `DB_PASSWORD` | `outreach` / `outreach` | Database credentials |
| `SMTP_HOST` / `SMTP_PORT` | `localhost` / `1025` | Mail server |
| `UNSUBSCRIBE_BASE_URL` | `http://localhost:8080/u` | Base URL for opt-out links |
| `PHYSICAL_ADDRESS` | example address | Sender postal address |

## Project structure

```
src/main/java/com/rushil/outreach/
├── campaign/      Campaigns and sequence steps
├── lead/          Prospects
├── enrollment/    A lead's progress through a campaign (state machine)
├── scheduling/    Background dispatcher
├── sending/       Email port + SMTP adapter
├── suppression/   Opt-outs and the unsubscribe endpoint
├── user/          Registration and login
└── common/        Security, exceptions, shared base entities
```

## Status

Working: auth, campaigns, sequences, leads, enrollment, scheduled sending, and unsubscribe.

Planned: bounce and reply webhooks, campaign pause enforcement in the dispatcher, a physical address footer, and LLM-assisted personalization.