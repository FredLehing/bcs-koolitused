# Koolitaja andmebaasi ja entity'de muudatused

**Teenus:** — (eeltöö; olemasolev `GET /api/lecturers` muutub)

**Kasutavad vaated:** `AdminLecturersView.vue` (`/admin-lecturers`), `LecturerFormView.vue` (`/lecturer-form`), `LecturerCard.vue` (`/training`, `/admin-training-courses`, `/course-form`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-lecturers-view/admin-lecturers-view-labimang.html` ja otsuseid `docs/mock-wireframe/loo-mock-vaade/admin-lecturers-view/admin-lecturers-view-skeemid.md` (jaotised "Otsused" ja 1) ning `docs/mock-wireframe/loo-mock-vaade/admin-training-courses-view/admin-training-courses-view-skeemid.md` (jaotis 1, `lecturer_photo`).

Kõik teised koolitajate taskid sõltuvad sellest. Tee see esimesena.

## Sisend

Teenuse sisendeid pole — task muudab andmebaasi skeemi, seed-andmeid ja entity'sid.

## Väljund

### 1. `docs/database/2_create.sql`

**`lecturer`:** veerg `photo` eemaldatakse (pilt liigub tabelisse `lecturer_photo`), lisandub `status`.

```sql
CREATE TABLE lecturer
(
    id         serial       NOT NULL,
    full_name  varchar(255) NOT NULL,
    status     varchar(1)   NOT NULL,
    created_at timestamp    NOT NULL,
    updated_at timestamp    NOT NULL,
    created_by int          NOT NULL,
    CONSTRAINT lecturer_pk PRIMARY KEY (id)
);
```

**`lecturer_translation`:** `bio` asemel samad väljad nagu `training_translation`-il.

```sql
CREATE TABLE lecturer_translation
(
    id                serial       NOT NULL,
    lecturer_id       int          NOT NULL,
    language_id       int          NOT NULL,
    title             varchar(255) NOT NULL,
    short_description varchar(255) NOT NULL,
    description       text         NOT NULL,
    created_at        timestamp    NOT NULL,
    updated_at        timestamp    NOT NULL,
    CONSTRAINT lecturer_translation_pk PRIMARY KEY (id),
    CONSTRAINT lecturer_translation_uq UNIQUE (lecturer_id, language_id)
);
```

| Väli | Tähendus | Kus kuvatakse |
|---|---|---|
| `title` | ametinimetus, nt "Lektor/konsultant" | koolitaja kaardil nime all, admini nimekirjas |
| `short_description` | 1–2 lauset | koolitaja kaardil |
| `description` | pikk tutvustus (HTML, rich text) | tulevikus avalikul koolitajate lehel |

**Uus tabel `lecturer_photo`** (1:1 `lecturer`-iga; rida puudub = pilti pole):

```sql
-- Table: lecturer_photo (1:1 lecturer'iga; rida puudub = pilti pole)
CREATE TABLE lecturer_photo
(
    id           serial      NOT NULL,
    lecturer_id  int         NOT NULL,
    photo        bytea       NOT NULL,
    content_type varchar(50) NOT NULL,
    created_at   timestamp   NOT NULL,
    updated_at   timestamp   NOT NULL,
    CONSTRAINT lecturer_photo_pk PRIMARY KEY (id),
    CONSTRAINT lecturer_photo_uq UNIQUE (lecturer_id)
);

-- Reference: lecturer_photo_lecturer (table: lecturer_photo)
ALTER TABLE lecturer_photo
    ADD CONSTRAINT lecturer_photo_lecturer
        FOREIGN KEY (lecturer_id)
            REFERENCES lecturer (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;
```

Miks eraldi tabel: JPA laeb `bytea` välja entity'ga alati kaasa. Eraldi tabeliga ei loeta pilte "Vali koolitaja" otsingus ega nimekirjas. `content_type` (`image/png`, `image/jpeg`, `image/webp`) on vajalik, et frontend saaks koostada `data:{contentType};base64,{photo}` URL-i.

**Uus view `admin_lecturer_summary`** (kasutab `GET-api-admin-lecturers.md`). SQL: `admin-lecturers-view-skeemid.md`, jaotis 1 "View `admin_lecturer_summary`". Lisa view faili lõppu teiste view'de juurde.

### 2. `docs/database/3_import.sql`

Nimed, ruumid ja koolituste vaikimisi koolitajad on `3_import.sql`-is **juba uuendatud** (praeguse skeemi järgi: `photo = ''::bytea`, `bio`). Selles taskis viiakse koolitajate read uue skeemi kujule:

```sql
-- Table: lecturer
INSERT INTO lecturer (id, full_name, status, created_at, updated_at, created_by) VALUES
    (1, 'Rain Tüür', 'A', '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1),
    (2, 'Merje Vaide', 'A', '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1),
    (3, 'Kersti Laidvee', 'A', '2026-09-20 10:00:00', '2026-09-20 10:00:00', 1),
    (4, 'Virve Räni', 'D', '2026-07-20 09:00:00', '2026-09-01 16:00:00', 1),
    (5, 'Tarmo Rosenfeldt', 'A', '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1),
    (6, 'Margus Sakk', 'A', '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1),
    (7, 'Andres Liitmaa', 'A', '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1),
    (8, 'Meelis Teern', 'A', '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1),
    (9, 'Tarmo Kallas', 'A', '2026-09-22 11:00:00', '2026-09-22 11:00:00', 1);

-- Table: lecturer_translation (title = amet, short_description = peamised spetsialiseerumised)
INSERT INTO lecturer_translation (id, lecturer_id, language_id, title, short_description, description, created_at, updated_at) VALUES
    (1, 1, 1, 'Lektor/konsultant', 'Tarkvaraarendus, Java, HTML, CSS, JavaScript, SQL, Git, Spring Boot, REST API, PostgreSQL, JPA (Hibernate), MapStruct, JUnit, Swagger, Gradle, Confluence, Jira. Vali Tarkvaraarendus! programm.', '<p>Tarkvaraarendus, Java, HTML, CSS, JavaScript, SQL, Git, Spring Boot, REST API, PostgreSQL, JPA (Hibernate), MapStruct, JUnit, Swagger, Gradle, Confluence, Jira. Vali Tarkvaraarendus! programm.</p>', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (2, 1, 2, 'Lecturer/consultant', 'Software development, Java, HTML, CSS, JavaScript, SQL, Git, Spring Boot, REST API, PostgreSQL, JPA (Hibernate), MapStruct, JUnit, Swagger, Gradle, Confluence, Jira. Vali Tarkvaraarendus! programme.', '<p>Software development, Java, HTML, CSS, JavaScript, SQL, Git, Spring Boot, REST API, PostgreSQL, JPA (Hibernate), MapStruct, JUnit, Swagger, Gradle, Confluence, Jira. Vali Tarkvaraarendus! programme.</p>', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (3, 2, 1, 'Projektijuht/lektor', 'Microsoft Office rakendused, andmeanalüüs, tarkvaraarendus, veebiarendus. Täiskasvanute koolitaja tase 6.', '<p>Microsoft Office rakendused, andmeanalüüs, tarkvaraarendus, veebiarendus. Täiskasvanute koolitaja tase 6.</p>', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (4, 2, 2, 'Project manager/lecturer', 'Microsoft Office applications, data analysis, software development, web development. Adult educator, level 6.', '<p>Microsoft Office applications, data analysis, software development, web development. Adult educator, level 6.</p>', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (5, 3, 1, 'Lektor/konsultant', 'Adobe Photoshop, Illustrator, InDesign, Acrobat, Canva, Figma, e-turundus, e-õppe disain, Office rakendused.', '<p>Adobe Photoshop, Illustrator, InDesign, Acrobat, Canva, Figma, e-turundus, e-õppe disain, Office rakendused.</p>', '2026-09-20 10:00:00', '2026-09-20 10:00:00'),
    (6, 4, 1, 'Lektor/konsultant', 'Andmeanalüüs, Power BI, SQL, Tableau, SAP BO, Excel, Vali Andmetarkus! programm.', '<p>Andmeanalüüs, Power BI, SQL, Tableau, SAP BO, Excel, Vali Andmetarkus! programm.</p>', '2026-07-20 09:00:00', '2026-07-20 09:00:00'),
    (7, 5, 1, 'Lektor/konsultant', 'Microsoft 365, MS Teams, SharePoint, Planner, Viva Goals, Office rakendused, küberturvalisus. Täiskasvanute koolitaja tase 6.', '<p>Microsoft 365, MS Teams, SharePoint, Planner, Viva Goals, Office rakendused, küberturvalisus. Täiskasvanute koolitaja tase 6.</p>', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (8, 5, 2, 'Lecturer/consultant', 'Microsoft 365, MS Teams, SharePoint, Planner, Viva Goals, Office applications, cybersecurity. Adult educator, level 6.', '<p>Microsoft 365, MS Teams, SharePoint, Planner, Viva Goals, Office applications, cybersecurity. Adult educator, level 6.</p>', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (9, 6, 1, 'Lektor/konsultant', 'Power BI, Bizagi, Power Automate, Access, Visio, Office rakendused, kasutajakoolitused ja andmeanalüüs.', '<p>Power BI, Bizagi, Power Automate, Access, Visio, Office rakendused, kasutajakoolitused ja andmeanalüüs.</p>', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (10, 6, 2, 'Lecturer/consultant', 'Power BI, Bizagi, Power Automate, Access, Visio, Office applications, user training and data analysis.', '<p>Power BI, Bizagi, Power Automate, Access, Visio, Office applications, user training and data analysis.</p>', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (11, 7, 1, 'Spetsialisti valdkonna lektor/konsultant', 'Microsofti ametlikud sertifitseerimiskoolitused (MOC), serverid, pilvetehnoloogiad, Microsoft Certified Trainer.', '<p>Microsofti ametlikud sertifitseerimiskoolitused (MOC), serverid, pilvetehnoloogiad, Microsoft Certified Trainer.</p>', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (12, 7, 2, 'Specialist lecturer/consultant', 'Official Microsoft certification courses (MOC), servers, cloud technologies, Microsoft Certified Trainer.', '<p>Official Microsoft certification courses (MOC), servers, cloud technologies, Microsoft Certified Trainer.</p>', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (13, 8, 1, 'Lektor/konsultant', 'Tarkvaraarendus, Java, JavaScript, HTML, CSS, Vue, Angular, Vali Tarkvaraarendus! programm.', '<p>Tarkvaraarendus, Java, JavaScript, HTML, CSS, Vue, Angular, Vali Tarkvaraarendus! programm.</p>', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (14, 8, 2, 'Lecturer/consultant', 'Software development, Java, JavaScript, HTML, CSS, Vue, Angular, Vali Tarkvaraarendus! programme.', '<p>Software development, Java, JavaScript, HTML, CSS, Vue, Angular, Vali Tarkvaraarendus! programme.</p>', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (15, 9, 1, 'Lektor/konsultant', 'SharePoint, Power Platform, veebiarendus, UX (kasutajakogemus), veebiliideste arendus.', '<p>SharePoint, Power Platform, veebiarendus, UX (kasutajakogemus), veebiliideste arendus.</p>', '2026-09-22 11:00:00', '2026-09-22 11:00:00');

-- Table: lecturer_photo (Rain Tüüri foto docs/mock-wireframe/lecturer-photos/rain-tuur.jpg, 200×200 JPEG; teistel pilti pole)
INSERT INTO lecturer_photo (id, lecturer_id, photo, content_type, created_at, updated_at) VALUES
    (1, 1, decode('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', 'base64'), 'image/jpeg', '2026-07-15 09:00:00', '2026-07-15 09:00:00');
```

Lisaks faili lõppu teiste kõrvale: `SELECT setval(pg_get_serial_sequence('lecturer_photo', 'id'), (SELECT MAX(id) FROM lecturer_photo));` (`lecturer` ja `lecturer_translation` järjestused on seal juba olemas).

| Koolitaja | Status | Pilt | Tõlked | Koolitusi (vaikimisi koolitaja) | Tulevasi toimumiskordi | Olukord |
|---|---|---|---|---|---|---|
| Rain Tüür (1) | `A` | ✓ | et, en | 3 (1, 3, 8) | 2 (course 1, 5) | pildiga; prügikast keelatud |
| Merje Vaide (2) | `A` | — | et, en | 2 (2, 7) | 1 (course 2) | prügikast keelatud |
| Kersti Laidvee (3) | `A` | — | et ✗ | 1 (6) | 0 | puuduv en tõlge |
| Virve Räni (4) | `D` | — | et ✗ | 0 | 0 | kustutatud, "Näita kustutatud" + "Taasta" |
| Tarmo Rosenfeldt (5) | `A` | — | et, en | 1 (13) | 0 |  |
| Margus Sakk (6) | `A` | — | et, en | 2 (9, 12) | 0 |  |
| Andres Liitmaa (7) | `A` | — | et, en | 1 (10) | 0 |  |
| Meelis Teern (8) | `A` | — | et, en | 2 (4, 11) | 0 |  |
| Tarmo Kallas (9) | `A` | — | et ✗ | 1 (5) | 0 | puuduv en tõlge, saab kustutada |

Koolituste vaikimisi koolitajad (`training.default_lecturer_id`, juba `3_import.sql`-is, spetsialiseerumiste järgi): 1 Java algkursus → Rain Tüür; 2 Projektijuhtimise põhitõed → Merje Vaide; 3 Spring Boot veebiarendus → Rain Tüür; 4 Vue.js → Meelis Teern; 5 UX disaini alused → Tarmo Kallas; 6 Figma praktikum → Kersti Laidvee; 7 Agiilne meeskonnajuhtimine → Merje Vaide; 8 SQL ja andmebaasid → Rain Tüür; 9 Exceli algkursus → Margus Sakk; 10 Docker ja konteinerid → Andres Liitmaa; 11 Git ja GitHub → Meelis Teern; 12 Python andmeanalüüsiks → Margus Sakk; 13 Tehisaru töövahendid arendajale → Tarmo Rosenfeldt; 14 Photoshopi algkursus (kustutatud) → Kersti Laidvee. Toimumiskorrad on kalendri seed-andmete ettepanekust (`admin-training-courses-view-skeemid.md`, jaotis 2).

Ametid ja spetsialiseerumised on BCS Koolituse lektorite nimekirjast; `description` on esialgu sama tekst `<p>`-s (täiendatakse hiljem). Inglise tõlked on tehtud eestikeelse teksti põhjal.

### 3. Backend

- **`LecturerStatus` enum** (base-paketis, nagu `TrainingStatus`): `ACTIVE("A")`, `DELETED("D")`, meetod `getCode()`.
- **`Lecturer` entity:** väli `photo` eemaldatakse, lisandub `status` (`@Size(max = 1)`, `@NotNull`).
- **`LecturerTranslation` entity:** väli `bio` eemaldatakse, lisanduvad `title`, `shortDescription`, `description` (`@Size(max = 255)` kahel esimesel).
- **Uus entity `LecturerPhoto`** (nt `persistance/lecturer/photo/LecturerPhoto.java`): `lecturer` (`@OneToOne` või `@ManyToOne`, `lecturer_id`), `photo` (`byte[]`), `contentType`, auditeerimise väljad (`@CreatedDate`, `@LastModifiedDate`, vt `backend/CLAUDE.md` "Ajatemplid").
- **`LecturerPhotoRepository`** meetodiga `findLecturerPhotoBy(Integer lecturerId)` → `Optional<LecturerPhoto>` (nimi `backend/CLAUDE.md` "Repositooriumi meetodi nimetamine" järgi).
- **`GET /api/lecturers` muutub:** `LecturerDto`-st eemaldatakse `lecturerPhoto` (`{ lecturerId, lecturerName }`) ja `LecturerMapper`-ist `bytesToBase64` (Base64 teisendus liigub pildi mapperisse). Frontendis `MockDatabase.js` lektorite `lecturerPhoto` väljad eemaldatakse. Muudatus ainult aktiivsete koolitajate tagastamiseks on taskis `lecturer-deleted-status.md`.

## Eesmärk

Koolitajal tuleb samasugune tõlkesüsteem nagu koolitusel (ametinimetus, lühikirjeldus, kirjeldus), soft delete (`status`) ja pilt eraldi tabelis koos tüübiga. See on eeltöö koolitajate nimekirjale, koolitaja vormile ja koolitaja kaardile.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`: `lecturer`, `lecturer_translation`, `lecturer_photo` (uus), `training.default_lecturer_id` ja `course.lecturer_id` (viitavad `lecturer`-ile, ei muutu).

## Veaolukorrad

Task ei lisa teenuseid. Olemasoleva `GET /api/lecturers` veaolukorrad ei muutu (500 ootamatu vea korral).

## Vastuvõtu kriteeriumid

- [ ] `2_create.sql`: `lecturer.photo` eemaldatud, `lecturer.status` lisatud; `lecturer_translation` väljad `title`, `short_description`, `description`; tabel `lecturer_photo` koos unikaalsuse ja FK-ga; view `admin_lecturer_summary`
- [ ] `3_import.sql`: 9 koolitajat, 15 tõlget, Rain Tüüri pilt, `setval` `lecturer_photo` jaoks
- [ ] Andmebaas luuakse skriptidest vigadeta uuesti (`1_reset` → `2_create` → `3_import`) — **kontrollib kasutaja**
- [ ] `LecturerStatus` enum (`A`, `D`), `Lecturer` ja `LecturerTranslation` entity'd vastavad tabelitele, `LecturerPhoto` entity ja repositoorium olemas
- [ ] `GET /api/lecturers` vastuses pole `lecturerPhoto`; frontendi mock uuendatud
- [ ] Olemasolevad testid läbivad (`./gradlew test`)
