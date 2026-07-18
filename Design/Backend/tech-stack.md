# Backend Tech Stack

Status: Draft — pending review

## Scope

This records the concrete tooling decisions for the backend, filling in what [Arch/ARCH-01](../../Arch/ARCH-01-components.md) and [Arch/ARCH-04](../../Arch/ARCH-04-deployment.md) left at the "Python/FastAPI" level. These are project-wide decisions, not specific to any one vertical slice.

## Decided

| Concern | Choice | Why |
|---|---|---|
| Web framework | **FastAPI** | Already decided in ARCH-00/01 — async-friendly, auto-generates OpenAPI docs (useful since two separate clients integrate against this API). |
| Database access | **SQLAlchemy 2.0** (ORM) | Mainstream, well-documented, works cleanly with FastAPI and Alembic. |
| Migrations | **Alembic** | Standard pairing with SQLAlchemy; autogenerates migrations from model changes. |
| Database | **PostgreSQL** | Already decided in ARCH-03 — needed for real concurrent multi-client access. |
| Scheduler | **APScheduler** | Simpler, in-process, no extra infrastructure. Chosen over Celery+Redis (ARCH-04's other option) to keep the stack small while the project is still one vertical slice deep — can be swapped later if job volume/reliability needs outgrow it. |
| Testing | **pytest** + FastAPI's `TestClient` (httpx-based) | Standard for this stack; lets endpoint tests run without a live server. |
| Dependency management | **`requirements.txt` + a plain virtualenv** | No extra tooling to learn (no Poetry/Pipenv) — keeps onboarding simple for anyone new to the project. |
| App server | **uvicorn** | Standard ASGI server for FastAPI, referenced implicitly by ARCH-04's Docker layout. |

## Exact dependency versions

Pinned, not left as "latest" — reproducibility matters more than always having the newest release for a project multiple people set up independently.

```
# requirements.txt
fastapi==0.115.0
uvicorn[standard]==0.30.6
sqlalchemy==2.0.35
alembic==1.13.2
psycopg[binary]==3.2.1
pydantic==2.9.2
python-multipart==0.0.9      # required by FastAPI for Form/File uploads
apscheduler==3.10.4

# requirements-dev.txt (on top of the above)
pytest==8.3.3
httpx==0.27.2                 # backs FastAPI's TestClient
pytest-cov==5.0.0
```

## Project scaffold

```
med-verify-backend/
  app/
    api/
      routes/
        scan.py
    schemas/
      scan.py
    services/
      scan_service.py
      medicine_resolution_service.py
      user_medicine_service.py
      expiry_service.py
      interaction_service.py
      dosage_service.py
    repositories/
      medicine_repository.py
      user_medicine_repository.py
      dosage_reference_repository.py
      interaction_warning_repository.py
    utils/
      text.py
      clock.py
      image_storage.py
    models/
      orm.py
    db.py                      # engine/session setup, get_db() dependency
    config.py                  # settings (uploads dir, DB URL, etc.)
    main.py                    # FastAPI app instance, router registration
  alembic/
    versions/
    env.py
  tests/
    conftest.py
    unit/
      test_expiry_service.py
      test_medicine_resolution_service.py
      test_user_medicine_service.py
      test_interaction_service.py
      test_dosage_service.py
      test_scan_service.py
    api/
      test_scan_route.py
  requirements.txt
  requirements-dev.txt
  alembic.ini
```

This mirrors the [unit breakdown](units/README.md) directly — each unit's "Location" maps onto exactly one file here, and each unit's test cases map onto exactly one file under `tests/`.

## Test fixtures (`tests/conftest.py`)

```python
@pytest.fixture
def db() -> Session:
    """
    A SQLAlchemy session against a real Postgres test database (not sqlite —
    schema uses Postgres-specific defaults like gen_random_uuid()), wrapped
    in a transaction that's rolled back after each test for isolation.
    """

@pytest.fixture
def frozen_today(monkeypatch) -> date:
    """Patches app.utils.clock.get_today to return a fixed date (2026-07-18),
    for deterministic Expiry unit tests."""
    fixed = date(2026, 7, 18)
    monkeypatch.setattr("app.utils.clock.get_today", lambda: fixed)
    return fixed

@pytest.fixture
def test_client(db) -> TestClient:
    """FastAPI TestClient with get_db overridden to use the `db` fixture above."""
```

Every "Test cases" table across the [unit breakdown](units/README.md) assumes these fixtures exist — a unit test is expected to look like:

```python
def test_expired_medicine_returns_true(frozen_today):
    result = check_expiry(printed_expiry_date=date(2026, 6, 1))
    assert result.expired is True
    assert result.expiring_soon is False
```

## Deferred on purpose

Per [Arch/ARCH-01](../../Arch/ARCH-01-components.md)'s integration-adapter pattern, these are **not** being chosen yet — the adapter interface exists specifically so the service layer never hardcodes a vendor:

- Translation provider.
- Text-to-speech provider.
- AI chat model (Phase 3, Sarvam AI prioritized — not relevant yet).

(OCR is not in this list — it runs on-device in the Android app, not through a backend adapter. See [Design/Android/units/OCR](../Android/units/OCR/README.md) for its own deferred vendor choice.)

For the first vertical slice (medicine scan), none of the above are exercised yet — the backend's own adapter-based deferrals (translation/TTS) only become relevant once REQ-03 label-reading is implemented.

## Open questions

- At what point does APScheduler need to be revisited in favor of Celery+Redis? No concrete trigger defined yet — revisit once REQ-06/REQ-08 (reminders) are actually being implemented, since that's where job volume first shows up.
