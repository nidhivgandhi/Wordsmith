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

## Ship it (Week 3 — real deadline)
- [ ] Testcontainers so tests don't need a live DB (unblocks CI)
- [ ] GitHub Actions CI
- [ ] Dockerize the app
- [ ] Deploy live
- [ ] README with architecture diagram + write-ups of the geospatial & streak logic

## Buffer / stretch
- [ ] AI feature: analyze an outline for structural gaps (analysis only, never prose)

## Known follow-ups
- `WordsmithApplicationTests` is `@SpringBootTest` and needs a running Postgres;
  switch to Testcontainers when setting up CI.
- No integration test yet asserts `ST_DWithin` actually returns the right groups —
  that needs a real PostGIS, so it lands with Testcontainers. Slice tests cover
  binding, validation and response shape only.
- `docker-compose.yml` currently lives under `src/main/java/` — move it to the
  project root when Dockerizing.
- `JWT_SECRET` must be set in any real deployment; the default in `application.yml`
  is a known dev value and anyone holding it can mint valid tokens.
- Groups can be created but not yet edited or deleted; `writing_groups.owner_id`
  exists so those endpoints can enforce ownership when they are added.
- No token refresh or logout. Tokens are valid until they expire (24h default) and
  cannot be revoked — fine for now, worth revisiting if this grows real users.
