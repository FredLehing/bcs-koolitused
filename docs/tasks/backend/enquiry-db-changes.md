# Päringute andmebaasi ja entity muudatused

**Teenus:** — (eeltöö; teenuseid ei lisa)

**Kasutav vaade:** `AdminEnquiriesView.vue`, `AdminEnquiryView.vue`

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-enquiries-view/admin-enquiries-view-labimang.html` ja skeeme `admin-enquiries-view-skeemid.md` (jaotis 1).

Kõik päringute taskid sõltuvad sellest. Andmebaasi skripte käivitab ja kontrollib kasutaja (`1_reset` → `2_create` → `3_import`).

## Sisend

Andmebaasi ettepanek failist `docs/mock-wireframe/loo-mock-vaade/admin-enquiries-view/admin-enquiries-view-skeemid.md`, jaotis 1.

## Väljund

### 1. `docs/database/2_create.sql`

Tabelid ei muutu. Uus view `admin_enquiry_summary` (pärast `admin_training_summary`-t, sest kasutab seda):

```sql
CREATE VIEW admin_enquiry_summary AS
SELECT row_number() OVER (ORDER BY e.id, cl.id)                    AS id,
       e.id                                                        AS enquiry_id,
       cl.code                                                     AS content_language_code,
       e.training_id,
       ats.training_translation_id,
       ats.title                                                   AS training_title,
       e.course_id,
       c.start_date                                                AS course_start_date,
       c.end_date                                                  AS course_end_date,
       COALESCE(ot.name, mot.name)                                 AS option_name,
       p.first_name || ' ' || p.last_name                          AS full_name,
       p.email,
       p.phone,
       e.company_name,
       e.message,
       e.status,
       e.created_at
FROM enquiry e
         CROSS JOIN language cl
         JOIN language ml ON ml.is_main_language
         JOIN profile p ON p.id = e.profile_id
         JOIN admin_training_summary ats ON ats.training_id = e.training_id AND ats.content_language_code = cl.code
         LEFT JOIN course c ON c.id = e.course_id
         LEFT JOIN option_translation ot ON ot.option_id = e.option_id AND ot.language_id = cl.id
         LEFT JOIN option_translation mot ON mot.option_id = e.option_id AND mot.language_id = ml.id
WHERE cl.requires_translation;
```

### 2. `docs/database/3_import.sql`

Lisanduvad profiilid 3–4 ja päringud 3–4 (vt skeemide fail): Kadri Tamm (käsitletud, `AS Tarkvaramaja`) ja Martin Kask (uus, toimumiskord 5).

### 3. Backend

- `EnquiryStatus` enum (baaspakk): `NEW("U")`, `HANDLED("H")`.
- `Enquiry` entity: lisada puuduvad seosed `training` (`@ManyToOne`, `training_id`), `profile` (`profile_id`), `option` (`option_id`) — kõik `NOT NULL`.
- `EnquiryRepository`.
- View entity `persistance/view/adminenquirysummary/AdminEnquirySummary` (`@Immutable`) + repositoorium + mapper.

## Eesmärk

Admini päringute vaated loevad kõik andmed ühest view-st: koolituse nimi ja osalemisvorm tuleb kasutajaliidese keeles (puudumisel põhikeeles), kontaktandmed tabelist `profile`.

## Seotud andmebaasi tabelid

`enquiry`, `profile`, `option_translation`, `course`, view `admin_training_summary` (ainult lugemine).

## Veaolukorrad

—

## Vastuvõtu kriteeriumid

- [ ] `2_create.sql` ja `3_import.sql` käivituvad veata (kontrollib kasutaja)
- [ ] `EnquiryStatus`, `EnquiryRepository`, `AdminEnquirySummary` on olemas
- [ ] `Enquiry` entity-l on seosed `training`, `profile`, `option`
- [ ] Olemasolevad testid lähevad läbi
