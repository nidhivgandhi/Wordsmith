# Wordsmith Roadmap

A Spring Boot 4 (Java 21) REST API for novelists: pick a story structure
(Three-Act, Save the Cat), and its beats become an editable outline for your novel.
Postgres + PostGIS, Flyway migrations, schema owned in SQL (`ddl-auto: validate`).

> **Guiding rule:** a shipped Pillar 1+2 beats an unshipped Pillar 1+2+3.
> If time gets tight, cut Pillar 3 (Week 3 features) before cutting deployment —
> a deployed project with a strong README is what reviewers actually click.

## Pillar 1 — Story structures & outlining ✅
- [x] Story structures + beats, seeded via Flyway migrations
- [x] Novel creation: instantiate an editable beat per structure beat
- [x] Update beats (notes / status) via PATCH
- [x] Global exception handling (`@RestControllerAdvice`) — clean 400/404 + consistent `ApiError` body
- [x] Bean Validation on request DTOs
- [x] Second structure: Save the Cat (V4 migration)
- [x] Web-layer slice tests (`@WebMvcTest`) for structure & novel endpoints

## Pillar 2 — Community & auth (Week 2)
- [x] **Geospatial community search (headline feature):** writing groups with a
      `GEOGRAPHY(POINT, 4326)` location (V5); `GET /api/groups/search` finds groups
      within X miles via `ST_DWithin` over a GiST index, nearest first
- [x] JWT authentication (register / login, BCrypt-hashed passwords, `OncePerRequestFilter`)
- [x] Novel ownership (V6) — only the owner can read/modify their novels; a novel
      belonging to someone else returns 404, not 403, so ids cannot be enumerated
- [x] Replaced permit-all `SecurityConfig` with real rules: auth endpoints, structures
      and group *reads* are public; everything else authenticated by default

## Pillar 3 — Habits (Week 3, cut first if tight)
- [ ] Writing goals + word-count logging
- [ ] Streaks
- [ ] Scheduled reminder job

## Ship it (next — before the frontend)
- [x] Testcontainers so tests don't need a live DB (unblocks CI).
      `mvn test` = unit + slice, no Docker. `mvn verify` also runs `*IT` against a
      real PostGIS container, including the `ST_DWithin` query.
- [x] GitHub Actions CI (`mvn verify` on push/PR, then a Docker build gated on tests)
- [x] Dockerize the app (multi-stage, non-root, verified running locally)
- [x] README with architecture diagram + geospatial and auth write-ups
- [ ] **Deploy live** — `render.yaml` is committed; needs the Render Blueprint
      connected to the GitHub repo (a manual, account-level step)

## Buffer / stretch
- [ ] AI feature: analyze an outline for structural gaps (analysis only, never prose)

## After shipping
- [ ] Frontend, so the app is actually usable day to day
- [ ] Pillar 3 (goals / word counts / streaks / reminders)
- [ ] Edit a novel's structure in place instead of creating a new novel

## Known follow-ups
- Testcontainers must stay at **1.21.4 or newer**: 1.21.3 cannot talk to Docker
  Engine 29+ (every strategy fails with HTTP 400 on `/info`, reported misleadingly
  as "Could not find a valid Docker environment").
- `docker-compose.yml` currently lives under `src/main/java/` — move it to the
  project root when Dockerizing.
- `JWT_SECRET` must be set in any real deployment; the default in `application.yml`
  is a known dev value and anyone holding it can mint valid tokens.
- Groups can be created but not yet edited or deleted; `writing_groups.owner_id`
  exists so those endpoints can enforce ownership when they are added.
- No token refresh or logout. Tokens are valid until they expire (24h default) and
  cannot be revoked — fine for now, worth revisiting if this grows real users.
