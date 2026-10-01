# Registreerumiste halduse andmebaasi ja entity muudatused

**Teenus:** — (eeltöö; teenuseid ei lisa)

**Kasutav vaade:** `AdminRegistrationsView.vue`, `AdminRegistrationView.vue`

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-registrations-view/admin-registrations-view-labimang.html` ja skeeme `admin-registrations-view-skeemid.md` (jaotis 1).

Kõik registreerumiste taskid sõltuvad sellest. Andmebaasi skripte käivitab ja kontrollib kasutaja (`1_reset` → `2_create` → `3_import`).

## Sisend

Andmebaasi ettepanek failist `docs/mock-wireframe/loo-mock-vaade/admin-registrations-view/admin-registrations-view-skeemid.md`, jaotis 1.

## Väljund

### 1. `docs/database/2_create.sql`

Tabelisse `course_participant` uus veerg (pärast `notes`):

```sql
    -- admini märkmed (osaleja enda lisainfo on notes)
    admin_notes     text       NULL,
```

Uus view `admin_registration_summary` (pärast `admin_enquiry_summary`-t, sest kasutab `admin_training_summary`-t):

```sql
-- Registreerumised (admin): üks rida registreerumise ja tõlkekeele kohta; koolituse nimi puudumisel põhikeeles
-- Kustutatud toimumiskorrad (course_status D) ja kustutatud koolitused (training_status D) välistab nimekirja päring
CREATE VIEW admin_registration_summary AS
SELECT row_number() OVER (ORDER BY cp.id, ats.content_language_code) AS id,
       cp.id                                                         AS course_participant_id,
       ats.content_language_code,
       cp.status,
       cp.has_paid,
       cp.requires_laptop,
       cp.notes,
       cp.admin_notes,
       cp.created_at,
       cp.updated_at,
       pa.name                                                       AS participant_name,
       pr.email,
       pr.phone,
       u.email                                                       AS account_email,
       c.id                                                          AS course_id,
       ats.title                                                     AS training_title,
       ats.status                                                    AS training_status,
       c.start_date                                                  AS course_start_date,
       c.end_date                                                    AS course_end_date,
       c.status                                                      AS course_status,
       c.end_date < current_date                                     AS is_past
FROM course_participant cp
         JOIN participant pa ON pa.id = cp.participant_id
         JOIN profile pr ON pr.id = pa.profile_id
         JOIN "user" u ON u.id = pa.user_id
         JOIN course c ON c.id = cp.course_id
         JOIN admin_training_summary ats ON ats.training_id = c.training_id;
```

### 2. `docs/database/3_import.sql`

`course_participant` read saavad veeru `admin_notes`: rida 4 → `'Arve saadetud 15.09.'`, rida 5 → `'Teatas telefoni teel 25.09.'`, ülejäänud `NULL`.

### 3. Backend

- `CourseParticipant` entity: väli `adminNotes` (`admin_notes`, nullable).
- View entity `persistance/view/adminregistrationsummary/AdminRegistrationSummary` (`@Immutable`) + repositoorium + mapper (sama muster nagu `AdminEnquirySummary`).

## Eesmärk

Admini registreerumiste vaated loevad kõik andmed ühest view'st: osaleja (nimi, profiili kontakt, konto e-post), toimumiskord ja koolituse nimi kasutajaliidese keeles (puudumisel põhikeeles). Admin kirjutab oma märkmed eraldi veergu, osaleja enda lisainfo (`notes`) jääb puutumata.

## Seotud andmebaasi tabelid

`course_participant`, `participant`, `profile`, `"user"`, `course`, view `admin_training_summary` (ainult lugemine).

## Veaolukorrad

—

## Vastuvõtu kriteeriumid

- [ ] `2_create.sql` ja `3_import.sql` käivituvad veata (kontrollib kasutaja)
- [ ] `CourseParticipant.adminNotes`, `AdminRegistrationSummary` + repositoorium + mapper on olemas
- [ ] Olemasolevad testid lähevad läbi
