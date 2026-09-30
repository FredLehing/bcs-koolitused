# Toimumiskorra staatused, view `course_summary` ja seed-andmed

**Teenus:** — (eeltöö koolituse kalendrile)

**Kasutavad vaated:** `AdminTrainingCoursesView.vue` (`/admin-training-courses`), `CourseFormView.vue` (`/course-form`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-training-courses-view/admin-training-courses-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/admin-training-courses-view-markmed.md` (JSON-näited).

Eeldab taski `training-lecturers-multiple.md` (`course_lecturer`, `course.lecturer_id` eemaldatud). Kõik kalendri teenused sõltuvad sellest.

## Sisend

Teenuse sisendeid pole.

## Väljund

**1. `CourseStatus` enum** (base-paketis, nagu `TrainingStatus`): `UNPUBLISHED("U")` Mustand, `OPEN("O")` Avatud, `FULL("F")` Täis (käsitsi), `CANCELLED("X")` Tühistatud, `DELETED("D")` Kustutatud (soft delete). "Toimunud" tuleneb kuupäevast (`end_date < täna`), mitte staatusest. `course.status` jääb `varchar(3)`.

**2. View `course_summary`** `2_create.sql`-i — SQL: `docs/mock-wireframe/loo-mock-vaade/admin-training-courses-view/admin-training-courses-view-skeemid.md`, jaotis 2 (`lecturer_names` `course_lecturer` kaudu, `room_name`, `is_past`, `days_from_today`, `has_notes`, `has_meeting_link`, `participant_count`). Backendis `@Immutable` view entity (eeskuju `AdminTrainingSummary`).

**3. Seed (`3_import.sql`)** — `'AVA'` → `'O'` ja uued toimumiskorrad koolitusele 1 (täna = 2026-09-30):

```sql
INSERT INTO course (id, training_id, room_id, number_of_days, number_of_academic_hours, price, status, start_date, end_date, notes, meeting_link, created_at, updated_at, created_by) VALUES
    (1, 1, 1, 5, 40, 490.0000, 'O', '2026-10-05', '2026-10-09', 'Kaasa sülearvuti.', NULL, '2026-08-02 10:00:00', '2026-08-02 10:00:00', 1),
    (2, 2, NULL, 3, 24, 350.0000, 'O', '2026-11-02', '2026-11-04', NULL, 'https://meet.vali-it.ee/pm-kursus', '2026-08-06 11:00:00', '2026-08-06 11:00:00', 1),
    (3, 1, 1, 5, 40, 490.0000, 'O', '2026-09-07', '2026-09-11', 'Grupp oli täis, järgmine kord suurem ruum.', NULL, '2026-08-15 10:00:00', '2026-08-15 10:00:00', 1),
    (4, 1, 2, 5, 40, 490.0000, 'X', '2026-10-19', '2026-10-23', 'Tühistatud koolitaja haiguse tõttu.', NULL, '2026-08-15 10:00:00', '2026-08-15 10:00:00', 1),
    (5, 1, NULL, 5, 40, 520.0000, 'F', '2026-11-16', '2026-11-20', NULL, 'https://meet.vali-it.ee/java-nov', '2026-08-15 10:00:00', '2026-08-15 10:00:00', 1),
    (6, 1, 1, 5, 40, 520.0000, 'U', '2026-12-07', '2026-12-11', NULL, NULL, '2026-08-15 10:00:00', '2026-08-15 10:00:00', 1),
    (7, 1, 2, 5, 40, 450.0000, 'O', '2026-06-08', '2026-06-12', NULL, 'https://meet.vali-it.ee/java-jun', '2026-05-15 10:00:00', '2026-05-15 10:00:00', 1),
    (8, 1, 1, 5, 40, 490.0000, 'D', '2026-11-30', '2026-12-04', NULL, NULL, '2026-08-15 10:00:00', '2026-08-15 10:00:00', 1);

-- course_lecturer (read 1–3 lisab training-lecturers-multiple.md)
INSERT INTO course_lecturer (id, course_id, lecturer_id, sort_order) VALUES
    (4, 3, 1, 1), (5, 4, 8, 1), (6, 5, 1, 1), (7, 7, 8, 1), (8, 8, 1, 1);

-- course_participant: lisaks osaleja möödunud toimumiskorrale
    (2, 3, 1, 'Osales septembris.', true, true, 'REG', '2026-09-01 12:00:00', '2026-09-01 12:00:00');
```

Olukorrad: avatud (1), möödunud (3, 7), tühistatud (4), täis ja veebis (5), mustand ilma koolitajata (6), kustutatud (8, ei kuvata); koolitusel 10 toimumiskordi pole. Kontrolli `setval` read.

## Eesmärk

Kalendri teenustel on staatuste enum, view ja näidisandmed kõigi olukordade jaoks.

## Seotud andmebaasi tabelid

`course`, `course_lecturer`, `course_participant`, `room` (ruumid juba `3_import.sql`-is: Assauwe, Bremeni, Eppingi, Hellemanni, Landskrone, Megede), `lecturer`.

## Veaolukorrad

Task ei lisa teenuseid.

## Vastuvõtu kriteeriumid

- [ ] `CourseStatus` enum (U, O, F, X, D)
- [ ] View `course_summary` ja view entity
- [ ] Seed: 8 toimumiskorda, `course_lecturer`, `course_participant`; andmebaas luuakse vigadeta uuesti — **kontrollib kasutaja**
- [ ] Olemasolevad testid läbivad
