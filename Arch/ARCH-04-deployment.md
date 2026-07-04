# ARCH-04 — Deployment

Status: Approved

## Approach

Self-hosted, containerized — chosen so the specific hosting location (home server, VPS, etc.) can change without an architecture change. Docker Compose ties the pieces together for a single-host deployment; nothing here assumes a specific cloud provider.

## Layout

```mermaid
%%{init: {"flowchart": {"curve": "basis"}, "themeVariables": {"fontSize": "15px"}} }%%
flowchart TB
    NET(["🌐 Internet"])

    subgraph HOST["🖥️ Self-hosted Server (Docker Compose)"]
        direction TB
        NGINX["🔒 nginx<br/>reverse proxy + TLS"]

        subgraph APPTIER["Application tier"]
            direction LR
            BACKEND["🐍 backend<br/>FastAPI app"]
            WORKER["⏱️ scheduler / worker<br/>reminders · escalation"]
        end

        subgraph DATATIER["Data tier"]
            direction LR
            REDIS[("🧰 redis<br/><i>only if Celery used</i>")]
            PG[("🗄️ postgres")]
        end

        NGINX == "HTTPS" ==> BACKEND
        BACKEND == "reads / writes" ==> PG
        BACKEND -. "enqueue" .-> REDIS
        WORKER == "reads / writes" ==> PG
        WORKER -. "dequeue" .-> REDIS
    end

    NET == "HTTPS" ==> NGINX

    classDef edge fill:#1f6feb,stroke:#123a75,color:#ffffff,stroke-width:1.5px;
    classDef app fill:#2ea043,stroke:#1a6b30,color:#ffffff,stroke-width:1.5px;
    classDef data fill:#8250df,stroke:#5a32a3,color:#ffffff,stroke-width:1.5px;
    classDef groupBox fill:transparent,stroke:#57606a,stroke-width:1px,stroke-dasharray: 4 3,color:#57606a,font-weight:bold;

    class NGINX edge;
    class BACKEND,WORKER app;
    class REDIS,PG data;
    class HOST,APPTIER,DATATIER groupBox;
```

<sub>🔵 Edge/proxy · 🟢 Application tier · 🟣 Data tier</sub>

## Components

- **nginx** — TLS termination and reverse proxy in front of the backend; the only container exposed to the internet.
- **backend** — the FastAPI app (API + service layer + integration adapters).
- **scheduler/worker** — runs reminder/escalation checks on a cadence, independent of API request traffic.
- **redis** — only needed if Celery is chosen as the scheduler/task-queue implementation; APScheduler wouldn't require it.
- **postgres** — the data layer (ARCH-03).

## Open questions

- Backup/restore strategy for PostgreSQL (this holds all patient medical data — needs a real plan, not an afterthought).
- Monitoring/alerting for the self-hosted server itself (uptime, disk space, certificate renewal).
- Final choice between APScheduler (simpler, in-process) and Celery+Redis (more robust, more moving parts) for the scheduler.
