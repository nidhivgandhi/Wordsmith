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
- [ ] **Geospatial community search (headline feature):** writing groups with a
      location; "find groups within X miles" using PostGIS
- [ ] JWT authentication (register / login, hashed passwords)
- [ ] Novel ownership — only the owner can read/modify their novels
- [ ] Replace permit-all `SecurityConfig` with real authorization rules

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
