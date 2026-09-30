# Koolitusruumi andmebaasi ja entity muudatused

**Teenus:** — (eeltöö; teenuseid ei lisa)

**Kasutav vaade:** `AdminRoomsView.vue`, `RoomFormView.vue`, `CourseFormView.vue`

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-rooms-view/admin-rooms-view-labimang.html` ja skeeme `admin-rooms-view-skeemid.md` (jaotis 1).

Kõik ruumide taskid sõltuvad sellest. Andmebaasi skripte käivitab ja kontrollib kasutaja (`1_reset` → `2_create` → `3_import`).

## Sisend

Andmebaasi ettepanek failist `docs/mock-wireframe/loo-mock-vaade/admin-rooms-view/admin-rooms-view-skeemid.md`, jaotis 1.

## Väljund

### 1. `docs/database/2_create.sql`

Tabel `room`: `status varchar(3)` → `varchar(1)` (`A` / `D`), lisanduvad auditiveerud ja välisvõti `room_created_by` → `"user"(id)`:

```sql
CREATE TABLE room
(
    id         serial       NOT NULL,
    name       varchar(255) NOT NULL,
    status     varchar(1)   NOT NULL,
    created_at timestamp    NOT NULL,
    updated_at timestamp    NOT NULL,
    created_by int          NOT NULL,
    CONSTRAINT room_pk PRIMARY KEY (id)
);
```

Uus view `admin_room_summary` (paigutus teiste admin-view'de juurde):

```sql
CREATE VIEW admin_room_summary AS
SELECT r.id                                          AS room_id,
       r.name,
       r.status,
       (SELECT count(*)
        FROM course c
        WHERE c.room_id = r.id
          AND c.status NOT IN ('D', 'X')
          AND c.end_date >= current_date)            AS upcoming_course_count,
       (SELECT count(*)
        FROM course c
        WHERE c.room_id = r.id
          AND c.status <> 'D')                       AS course_count,
       r.updated_at
FROM room r;
```

### 2. `docs/database/3_import.sql`

```sql
INSERT INTO room (id, name, status, created_at, updated_at, created_by) VALUES
    (1, 'Assauwe', 'A', '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1),
    (2, 'Bremeni', 'D', '2026-07-15 09:00:00', '2026-09-01 10:00:00', 1),
    (3, 'Eppingi', 'A', '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1),
    (4, 'Hellemanni', 'A', '2026-07-15 09:00:00', '2026-09-20 10:00:00', 1),
    (5, 'Landskrone', 'A', '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1),
    (6, 'Megede', 'A', '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1);
```

### 3. Backend

- `RoomStatus` enum (baaspakk, nagu `LecturerStatus`): `ACTIVE("A")`, `DELETED("D")`.
- `Room` entity: `status` pikkus 1; `createdAt` (`@CreatedDate`), `updatedAt` (`@LastModifiedDate`), `createdBy` (`@ManyToOne User`); klassil `@EntityListeners(AuditingEntityListener.class)`.
- View entity `persistance/view/adminroomsummary/AdminRoomSummary` (`@Immutable`, `@Id roomId`) + repositoorium + mapper (kasutab `GET-api-admin-rooms.md`).

## Eesmärk

Ruumi staatuse tähendus (`VAB` / `KIN`) oli lahtine. Nüüd on see sama nagu teistel haldustabelitel: `A` aktiivne, `D` kustutatud (soft delete). Auditiveerud näitavad nimekirjas "Uuendatud" kuupäeva.

## Seotud andmebaasi tabelid

`room`, `course` (ainult lugemine view-s). Seed: Assauwe — 2 tulevast toimumiskorda (kokku 3), Bremeni — kustutatud, 2 möödunud/tühistatud toimumiskorda.

## Veaolukorrad

—

## Vastuvõtu kriteeriumid

- [ ] `2_create.sql` ja `3_import.sql` käivituvad veata (kontrollib kasutaja)
- [ ] `RoomStatus` enum on olemas
- [ ] `Room` entity auditiveergudega; `createdAt`/`updatedAt` täidab auditeerimine
- [ ] `AdminRoomSummary` view entity + repositoorium
- [ ] Olemasolevad testid lähevad läbi
