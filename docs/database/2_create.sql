-- Created by Redgate Data Modeler (https://datamodeler.redgate-platform.com)
-- Last modification date: 2026-09-21 08:58:27.055

-- tables
-- Table: category
CREATE TABLE category
(
    id         serial    NOT NULL,
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL,
    created_by int       NOT NULL,
    CONSTRAINT category_pk PRIMARY KEY (id)
);

-- Table: category_translation
CREATE TABLE category_translation
(
    id          serial       NOT NULL,
    category_id int          NOT NULL,
    language_id int          NOT NULL,
    name        varchar(255) NOT NULL,
    created_at  timestamp    NOT NULL,
    updated_at  timestamp    NOT NULL,
    CONSTRAINT category_translation_pk PRIMARY KEY (id),
    CONSTRAINT category_translation_uq UNIQUE (category_id, language_id)
);

-- Table: certificate_template
CREATE TABLE certificate_template
(
    id         serial     NOT NULL,
    course_id  int        NOT NULL,
    status     varchar(3) NOT NULL,
    created_at timestamp  NOT NULL,
    updated_at timestamp  NOT NULL,
    created_by int        NOT NULL,
    CONSTRAINT certificate_template_pk PRIMARY KEY (id)
);

-- Table: course
CREATE TABLE course
(
    id                       serial        NOT NULL,
    training_id              int           NOT NULL,
    room_id                  int           NULL,
    number_of_days           int           NOT NULL,
    number_of_academic_hours int           NOT NULL,
    price                    decimal(9, 4) NOT NULL,
    status                   varchar(3)    NOT NULL,
    start_date               date          NOT NULL,
    end_date                 date          NOT NULL,
    notes                    text          NULL,
    meeting_link             varchar(255)  NULL,
    is_promoted              boolean       NOT NULL DEFAULT false,
    created_at               timestamp     NOT NULL,
    updated_at               timestamp     NOT NULL,
    created_by               int           NOT NULL,
    CONSTRAINT course_session_pk PRIMARY KEY (id)
);

-- Table: course_lecturer (toimumiskorra koolitajad; sort_order = kuvamise järjekord, 1 = esimene)
CREATE TABLE course_lecturer
(
    id          serial NOT NULL,
    course_id   int    NOT NULL,
    lecturer_id int    NOT NULL,
    sort_order  int    NOT NULL,
    CONSTRAINT course_lecturer_pk PRIMARY KEY (id),
    CONSTRAINT course_lecturer_uq UNIQUE (course_id, lecturer_id)
);

-- Table: course_participant
CREATE TABLE course_participant
(
    id              serial     NOT NULL,
    course_id       int        NOT NULL,
    participant_id  int        NOT NULL,
    notes           text       NOT NULL,
    -- admini märkmed (osaleja enda lisainfo on notes)
    admin_notes     text       NULL,
    has_paid        boolean    NOT NULL,
    requires_laptop boolean    NOT NULL,
    -- R = registreerunud, C = loobunud
    status          varchar(1) NOT NULL,
    created_at      timestamp  NOT NULL,
    updated_at      timestamp  NOT NULL,
    CONSTRAINT course_participant_pk PRIMARY KEY (id),
    -- osalejal üks rida toimumiskorra kohta
    CONSTRAINT course_participant_uq UNIQUE (course_id, participant_id)
);

-- Table: course_participant_feedback (vastus: ühe kriteeriumi hinne ja kommentaar)
CREATE TABLE course_participant_feedback
(
    id                   serial       NOT NULL,
    feedback_id          int          NOT NULL,
    feedback_criteria_id int          NOT NULL,
    score                int          NOT NULL,
    feedback_text        text         NULL,
    created_at           timestamp    NOT NULL,
    updated_at           timestamp    NOT NULL,
    CONSTRAINT course_participant_feedback_pk PRIMARY KEY (id),
    -- tagasisides üks vastus kriteeriumi kohta
    CONSTRAINT course_participant_feedback_uq UNIQUE (feedback_id, feedback_criteria_id),
    CONSTRAINT course_participant_feedback_score_ck CHECK (score BETWEEN 1 AND 10)
);

-- Table: enquiry
CREATE TABLE enquiry
(
    id           serial       NOT NULL,
    training_id  int          NOT NULL,
    profile_id   int          NOT NULL,
    course_id    int          NULL,
    message      varchar(255) NOT NULL,
    company_name varchar(255) NULL,
    status       char(1)      NOT NULL,
    created_at   timestamp    NOT NULL,
    updated_at   timestamp    NOT NULL,
    CONSTRAINT applicant_pk PRIMARY KEY (id)
);

-- Table: feedback (ühe registreerumise tagasiside päis)
CREATE TABLE feedback
(
    id                    serial     NOT NULL,
    course_participant_id int        NOT NULL,
    -- N = uus, U = osaleja muutis pärast admini ülevaatust, H = admin on üle vaadanud
    status                varchar(1) NOT NULL,
    created_at            timestamp  NOT NULL,
    updated_at            timestamp  NOT NULL,
    CONSTRAINT feedback_pk PRIMARY KEY (id),
    -- registreerumisel üks tagasiside
    CONSTRAINT feedback_uq UNIQUE (course_participant_id)
);

-- Table: feedback_criteria (tagasiside kriteerium; tekst tõlgetes)
CREATE TABLE feedback_criteria
(
    id         serial     NOT NULL,
    sequence   int        NOT NULL,
    -- A = aktiivne, D = kustutatud (soft delete)
    status     varchar(1) NOT NULL,
    created_at timestamp  NOT NULL,
    updated_at timestamp  NOT NULL,
    CONSTRAINT feedback_criteria_pk PRIMARY KEY (id)
);

-- Table: feedback_criteria_translation
CREATE TABLE feedback_criteria_translation
(
    id                   serial       NOT NULL,
    feedback_criteria_id int          NOT NULL,
    language_id          int          NOT NULL,
    title                varchar(50)  NOT NULL,
    description          varchar(255) NOT NULL,
    created_at           timestamp    NOT NULL,
    updated_at           timestamp    NOT NULL,
    CONSTRAINT feedback_criteria_translation_pk PRIMARY KEY (id),
    CONSTRAINT feedback_criteria_translation_uq UNIQUE (feedback_criteria_id, language_id)
);

-- Table: funding_type
CREATE TABLE funding_type
(
    id         serial      NOT NULL,
    code       varchar(50) NOT NULL,
    created_at timestamp   NOT NULL,
    updated_at timestamp   NOT NULL,
    created_by int         NOT NULL,
    CONSTRAINT funding_type_pk PRIMARY KEY (id),
    CONSTRAINT funding_type_code_uq UNIQUE (code)
);

-- Table: funding_type_translation
CREATE TABLE funding_type_translation
(
    id              serial       NOT NULL,
    funding_type_id int          NOT NULL,
    language_id     int          NOT NULL,
    name            varchar(255) NOT NULL,
    created_at      timestamp    NOT NULL,
    updated_at      timestamp    NOT NULL,
    CONSTRAINT funding_type_translation_pk PRIMARY KEY (id),
    CONSTRAINT funding_type_translation_uq UNIQUE (funding_type_id, language_id)
);

-- Table: language
CREATE TABLE language
(
    id                   serial      NOT NULL,
    code                 varchar(2)  NOT NULL,
    name                 varchar(50) NOT NULL,
    is_main_language     boolean     NOT NULL,
    requires_translation boolean     NOT NULL,
    flag_icon_code       varchar(10) NOT NULL,
    CONSTRAINT language_pk PRIMARY KEY (id),
    CONSTRAINT language_code_uq UNIQUE (code)
);

-- Põhikeel saab olla ainult üks
CREATE UNIQUE INDEX language_main_language_uq ON language (is_main_language) WHERE is_main_language;

-- Table: lecturer
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

-- Table: lecturer_translation
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

-- Table: location
CREATE TABLE location
(
    id         serial       NOT NULL,
    name       varchar(255) NOT NULL,
    address    text         NOT NULL,
    is_online  boolean      NOT NULL,
    created_at timestamp    NOT NULL,
    updated_at timestamp    NOT NULL,
    created_by int          NOT NULL,
    CONSTRAINT location_pk PRIMARY KEY (id)
);

-- Table: newsletter
CREATE TABLE newsletter
(
    id         serial       NOT NULL,
    email      varchar(255) NOT NULL,
    first_name varchar(255) NOT NULL,
    last_name  varchar(255) NOT NULL,
    status     varchar(1)   NOT NULL,
    CONSTRAINT newsletter_pk PRIMARY KEY (id)
);

-- Table: participant
CREATE TABLE participant
(
    id         serial       NOT NULL,
    user_id    int          NOT NULL,
    name       varchar(255) NOT NULL,
    profile_id int          NOT NULL,
    created_at timestamp    NOT NULL,
    CONSTRAINT participant_pk PRIMARY KEY (id),
    -- kasutajal üks oma osaleja
    CONSTRAINT participant_user_uq UNIQUE (user_id)
);

-- Table: participant_certificate
CREATE TABLE participant_certificate
(
    id             serial    NOT NULL,
    file           bytea     NOT NULL,
    participant_id int       NOT NULL,
    created_at     timestamp NOT NULL,
    CONSTRAINT participant_certificate_pk PRIMARY KEY (id)
);

-- Table: profile
CREATE TABLE profile
(
    id         serial       NOT NULL,
    first_name varchar(255) NOT NULL,
    last_name  varchar(255) NOT NULL,
    phone      varchar(20)  NOT NULL,
    email      varchar(255) NOT NULL,
    created_at timestamp    NOT NULL,
    updated_at timestamp    NOT NULL,
    CONSTRAINT person_pk PRIMARY KEY (id)
);

-- Table: role
CREATE TABLE role
(
    id   serial       NOT NULL,
    name varchar(255) NOT NULL,
    CONSTRAINT role_pk PRIMARY KEY (id)
);

-- Table: room
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

-- Table: training
CREATE TABLE training
(
    id                   serial    NOT NULL,
    user_id              int       NOT NULL,
    category_id          int       NOT NULL,
    training_language_id int       NOT NULL,
    location_id          int       NOT NULL,
    status               varchar(1) NOT NULL,
    created_at           timestamp NOT NULL,
    updated_at           timestamp NOT NULL,
    is_orderable         boolean   NOT NULL,
    is_promoted          boolean   NOT NULL,
    CONSTRAINT course_pk PRIMARY KEY (id)
);

-- Table: training_funding_type
CREATE TABLE training_funding_type
(
    id              serial NOT NULL,
    training_id     int    NOT NULL,
    funding_type_id int    NOT NULL,
    CONSTRAINT training_funding_type_pk PRIMARY KEY (id),
    CONSTRAINT training_funding_type_uq UNIQUE (training_id, funding_type_id)
);

-- Table: training_lecturer (koolituse koolitajad; sort_order = kuvamise järjekord, 1 = esimene)
CREATE TABLE training_lecturer
(
    id          serial NOT NULL,
    training_id int    NOT NULL,
    lecturer_id int    NOT NULL,
    sort_order  int    NOT NULL,
    CONSTRAINT training_lecturer_pk PRIMARY KEY (id),
    CONSTRAINT training_lecturer_uq UNIQUE (training_id, lecturer_id)
);

-- Table: training_translation
CREATE TABLE training_translation
(
    id                serial       NOT NULL,
    training_id       int          NOT NULL,
    language_id       int          NOT NULL,
    title             varchar(255) NOT NULL,
    short_description varchar(255) NOT NULL,
    description       text         NOT NULL,
    created_at        timestamp    NOT NULL,
    updated_at        timestamp    NOT NULL,
    CONSTRAINT training_translation_pk PRIMARY KEY (id),
    CONSTRAINT training_translation_uq UNIQUE (training_id, language_id)
);

-- Table: training_translation_curriculum (1:1 training_translation'iga; rida puudub = õppekava pole)
CREATE TABLE training_translation_curriculum
(
    id                      serial       NOT NULL,
    training_translation_id int          NOT NULL,
    file                    bytea        NOT NULL,
    file_name               varchar(255) NOT NULL,
    file_size               int          NOT NULL,
    created_at              timestamp    NOT NULL,
    updated_at              timestamp    NOT NULL,
    CONSTRAINT training_translation_curriculum_pk PRIMARY KEY (id),
    CONSTRAINT training_translation_curriculum_uq UNIQUE (training_translation_id)
);

-- Table: user
CREATE TABLE "user"
(
    id         serial       NOT NULL,
    role_id    int          NOT NULL,
    email      varchar(255) NOT NULL,
    password   varchar(255) NOT NULL,
    status     char(1)      NOT NULL,
    created_at timestamp    NOT NULL,
    CONSTRAINT user_pk PRIMARY KEY (id),
    CONSTRAINT user_email_uq UNIQUE (email)
);

CREATE INDEX user_role_idx_1 on "user" (role_id ASC);

-- foreign keys
-- Reference: application_course (table: enquiry)
ALTER TABLE enquiry
    ADD CONSTRAINT application_course
        FOREIGN KEY (course_id)
            REFERENCES course (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: application_training (table: enquiry)
ALTER TABLE enquiry
    ADD CONSTRAINT application_training
        FOREIGN KEY (training_id)
            REFERENCES training (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: category_created_by (table: category)
ALTER TABLE category
    ADD CONSTRAINT category_created_by
        FOREIGN KEY (created_by)
            REFERENCES "user" (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: category_translation_category (table: category_translation)
ALTER TABLE category_translation
    ADD CONSTRAINT category_translation_category
        FOREIGN KEY (category_id)
            REFERENCES category (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: category_translation_language (table: category_translation)
ALTER TABLE category_translation
    ADD CONSTRAINT category_translation_language
        FOREIGN KEY (language_id)
            REFERENCES language (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: certificate_participant (table: participant_certificate)
ALTER TABLE participant_certificate
    ADD CONSTRAINT certificate_participant
        FOREIGN KEY (participant_id)
            REFERENCES participant (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: certificate_template_course (table: certificate_template)
ALTER TABLE certificate_template
    ADD CONSTRAINT certificate_template_course
        FOREIGN KEY (course_id)
            REFERENCES course (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: certificate_template_created_by (table: certificate_template)
ALTER TABLE certificate_template
    ADD CONSTRAINT certificate_template_created_by
        FOREIGN KEY (created_by)
            REFERENCES "user" (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: course_category (table: training)
ALTER TABLE training
    ADD CONSTRAINT course_category
        FOREIGN KEY (category_id)
            REFERENCES category (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: course_created_by (table: course)
ALTER TABLE course
    ADD CONSTRAINT course_created_by
        FOREIGN KEY (created_by)
            REFERENCES "user" (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: course_location (table: training)
ALTER TABLE training
    ADD CONSTRAINT course_location
        FOREIGN KEY (location_id)
            REFERENCES location (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: course_lecturer_course (table: course_lecturer)
ALTER TABLE course_lecturer
    ADD CONSTRAINT course_lecturer_course
        FOREIGN KEY (course_id)
            REFERENCES course (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: course_lecturer_lecturer (table: course_lecturer)
ALTER TABLE course_lecturer
    ADD CONSTRAINT course_lecturer_lecturer
        FOREIGN KEY (lecturer_id)
            REFERENCES lecturer (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: course_participant_course (table: course_participant)
ALTER TABLE course_participant
    ADD CONSTRAINT course_participant_course
        FOREIGN KEY (course_id)
            REFERENCES course (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: course_participant_feedback_feedback (table: course_participant_feedback)
ALTER TABLE course_participant_feedback
    ADD CONSTRAINT course_participant_feedback_feedback
        FOREIGN KEY (feedback_id)
            REFERENCES feedback (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: course_participant_feedback_feedback_criteria (table: course_participant_feedback)
ALTER TABLE course_participant_feedback
    ADD CONSTRAINT course_participant_feedback_feedback_criteria
        FOREIGN KEY (feedback_criteria_id)
            REFERENCES feedback_criteria (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: course_participant_participant (table: course_participant)
ALTER TABLE course_participant
    ADD CONSTRAINT course_participant_participant
        FOREIGN KEY (participant_id)
            REFERENCES participant (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: course_room (table: course)
ALTER TABLE course
    ADD CONSTRAINT course_room
        FOREIGN KEY (room_id)
            REFERENCES room (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: course_session_training (table: course)
ALTER TABLE course
    ADD CONSTRAINT course_session_training
        FOREIGN KEY (training_id)
            REFERENCES training (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: course_user (table: training)
ALTER TABLE training
    ADD CONSTRAINT course_user
        FOREIGN KEY (user_id)
            REFERENCES "user" (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: enquiry_profile (table: enquiry)
ALTER TABLE enquiry
    ADD CONSTRAINT enquiry_profile
        FOREIGN KEY (profile_id)
            REFERENCES profile (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: feedback_course_participant (table: feedback)
ALTER TABLE feedback
    ADD CONSTRAINT feedback_course_participant
        FOREIGN KEY (course_participant_id)
            REFERENCES course_participant (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: feedback_criteria_translation_feedback_criteria (table: feedback_criteria_translation)
ALTER TABLE feedback_criteria_translation
    ADD CONSTRAINT feedback_criteria_translation_feedback_criteria
        FOREIGN KEY (feedback_criteria_id)
            REFERENCES feedback_criteria (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: feedback_criteria_translation_language (table: feedback_criteria_translation)
ALTER TABLE feedback_criteria_translation
    ADD CONSTRAINT feedback_criteria_translation_language
        FOREIGN KEY (language_id)
            REFERENCES language (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: funding_type_created_by (table: funding_type)
ALTER TABLE funding_type
    ADD CONSTRAINT funding_type_created_by
        FOREIGN KEY (created_by)
            REFERENCES "user" (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: funding_type_translation_funding_type (table: funding_type_translation)
ALTER TABLE funding_type_translation
    ADD CONSTRAINT funding_type_translation_funding_type
        FOREIGN KEY (funding_type_id)
            REFERENCES funding_type (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: funding_type_translation_language (table: funding_type_translation)
ALTER TABLE funding_type_translation
    ADD CONSTRAINT funding_type_translation_language
        FOREIGN KEY (language_id)
            REFERENCES language (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: lecturer_created_by (table: lecturer)
ALTER TABLE lecturer
    ADD CONSTRAINT lecturer_created_by
        FOREIGN KEY (created_by)
            REFERENCES "user" (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: room_created_by (table: room)
ALTER TABLE room
    ADD CONSTRAINT room_created_by
        FOREIGN KEY (created_by)
            REFERENCES "user" (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: lecturer_photo_lecturer (table: lecturer_photo)
ALTER TABLE lecturer_photo
    ADD CONSTRAINT lecturer_photo_lecturer
        FOREIGN KEY (lecturer_id)
            REFERENCES lecturer (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: lecturer_translation_language (table: lecturer_translation)
ALTER TABLE lecturer_translation
    ADD CONSTRAINT lecturer_translation_language
        FOREIGN KEY (language_id)
            REFERENCES language (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: lecturer_translation_lecturer (table: lecturer_translation)
ALTER TABLE lecturer_translation
    ADD CONSTRAINT lecturer_translation_lecturer
        FOREIGN KEY (lecturer_id)
            REFERENCES lecturer (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: location_created_by (table: location)
ALTER TABLE location
    ADD CONSTRAINT location_created_by
        FOREIGN KEY (created_by)
            REFERENCES "user" (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: participant_profile (table: participant)
ALTER TABLE participant
    ADD CONSTRAINT participant_profile
        FOREIGN KEY (profile_id)
            REFERENCES profile (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: participant_user (table: participant)
ALTER TABLE participant
    ADD CONSTRAINT participant_user
        FOREIGN KEY (user_id)
            REFERENCES "user" (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: training_funding_type_funding_type (table: training_funding_type)
ALTER TABLE training_funding_type
    ADD CONSTRAINT training_funding_type_funding_type
        FOREIGN KEY (funding_type_id)
            REFERENCES funding_type (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: training_funding_type_training (table: training_funding_type)
ALTER TABLE training_funding_type
    ADD CONSTRAINT training_funding_type_training
        FOREIGN KEY (training_id)
            REFERENCES training (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: training_language (table: training)
ALTER TABLE training
    ADD CONSTRAINT training_language
        FOREIGN KEY (training_language_id)
            REFERENCES language (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: training_lecturer_lecturer (table: training_lecturer)
ALTER TABLE training_lecturer
    ADD CONSTRAINT training_lecturer_lecturer
        FOREIGN KEY (lecturer_id)
            REFERENCES lecturer (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: training_lecturer_training (table: training_lecturer)
ALTER TABLE training_lecturer
    ADD CONSTRAINT training_lecturer_training
        FOREIGN KEY (training_id)
            REFERENCES training (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: training_translation_curriculum_training_translation (table: training_translation_curriculum)
ALTER TABLE training_translation_curriculum
    ADD CONSTRAINT training_translation_curriculum_training_translation
        FOREIGN KEY (training_translation_id)
            REFERENCES training_translation (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: training_translation_language (table: training_translation)
ALTER TABLE training_translation
    ADD CONSTRAINT training_translation_language
        FOREIGN KEY (language_id)
            REFERENCES language (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: training_translation_training (table: training_translation)
ALTER TABLE training_translation
    ADD CONSTRAINT training_translation_training
        FOREIGN KEY (training_id)
            REFERENCES training (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- Reference: user_role (table: user)
ALTER TABLE "user"
    ADD CONSTRAINT user_role
        FOREIGN KEY (role_id)
            REFERENCES role (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;

-- views
-- View: training_summary
-- NB! Lisatud käsitsi (mitte Redgate Data Modeleri poolt genereeritud) — skripti uuesti genereerimisel lisa see uuesti.
-- Üks rida = üks koolituse tõlge (training_translation).
-- Rahastustüübid (training_funding_type) ei ole vaates, et leheküljestamine loeks koolitusi, mitte rahastustüüpide ridu.
CREATE VIEW training_summary AS
SELECT row_number() OVER (ORDER BY tt.id)                      AS id,
       tt.id                                                   AS training_translation_id,
       tt.training_id,
       t.training_language_id,
       trl.code                                                AS training_language_code,
       trl.flag_icon_code                                      AS training_language_flag_icon_code,
       tt.language_id                                          AS translation_language_id,
       tl.code                                                 AS translation_language_code,
       tt.title,
       tt.short_description,
       t.category_id,
       ct.name                                                 AS category_name,
       t.status
FROM training_translation tt
         JOIN training t ON t.id = tt.training_id
         JOIN language trl ON trl.id = t.training_language_id
         JOIN language tl ON tl.id = tt.language_id
         LEFT JOIN category_translation ct ON ct.category_id = t.category_id AND ct.language_id = tt.language_id;

-- Admini koolituste tabel: üks rida koolituse ja tõlkekeele kohta; puuduva tõlke korral põhikeele pealkiri ja kategooria
CREATE VIEW admin_training_summary AS
SELECT row_number() OVER (ORDER BY t.id, cl.id)                    AS id,
       t.id                                                        AS training_id,
       cl.code                                                     AS content_language_code,
       COALESCE(tt.id, mtt.id)                                     AS training_translation_id,
       COALESCE(tt.title, mtt.title)                               AS title,
       t.category_id,
       COALESCE(ct.name, mct.name)                                 AS category_name,
       t.training_language_id,
       trl.code                                                    AS training_language_code,
       trl.flag_icon_code                                          AS training_language_flag_icon_code,
       t.status,
       CASE t.status WHEN 'U' THEN 1 WHEN 'P' THEN 2 ELSE 3 END   AS status_order,
       t.is_orderable,
       t.is_promoted,
       t.created_at,
       GREATEST(t.updated_at, (SELECT MAX(att.updated_at)
                               FROM training_translation att
                               WHERE att.training_id = t.id))     AS updated_at,
       mt.missing_translation_language_codes,
       mt.missing_translation_language_codes IS NULL               AS has_all_translations
FROM training t
         CROSS JOIN language cl
         JOIN language trl ON trl.id = t.training_language_id
         JOIN language ml ON ml.is_main_language
         LEFT JOIN training_translation tt ON tt.training_id = t.id AND tt.language_id = cl.id
         LEFT JOIN training_translation mtt ON mtt.training_id = t.id AND mtt.language_id = ml.id
         LEFT JOIN category_translation ct ON ct.category_id = t.category_id AND ct.language_id = cl.id
         LEFT JOIN category_translation mct ON mct.category_id = t.category_id AND mct.language_id = ml.id
         -- string_agg tühjast hulgast annab NULL, seega NULL = kõik tõlked olemas
         CROSS JOIN LATERAL (SELECT string_agg(rl.code, ',' ORDER BY rl.id) AS missing_translation_language_codes
                             FROM language rl
                             WHERE rl.requires_translation
                               AND NOT EXISTS (SELECT 1
                                               FROM training_translation rtt
                                               WHERE rtt.training_id = t.id
                                                 AND rtt.language_id = rl.id)) mt
WHERE cl.requires_translation;

-- Kõigi koolituste toimumiskorrad (admin): üks rida toimumiskorra ja tõlkekeele kohta; koolituse nimi puudumisel põhikeeles
-- Kustutatud toimumiskorrad (status D) ja kustutatud koolitused (training_status D) välistab päring
CREATE VIEW admin_course_summary AS
SELECT row_number() OVER (ORDER BY c.id, ats.content_language_code)   AS id,
       c.id                                                            AS course_id,
       ats.content_language_code,
       c.training_id,
       ats.training_translation_id,
       ats.title                                                       AS training_title,
       ats.status                                                      AS training_status,
       ats.category_id,
       ats.training_language_id,
       c.start_date,
       c.end_date,
       c.end_date < current_date                                       AS is_past,
       -- sorteerimiseks: tulevased lähimast, möödunud hiliseimast
       CASE WHEN c.end_date < current_date THEN current_date - c.start_date
            ELSE c.start_date - current_date END                       AS days_from_today,
       c.number_of_days,
       c.price,
       c.status,
       CASE c.status WHEN 'U' THEN 1 WHEN 'O' THEN 2 WHEN 'F' THEN 3 ELSE 4 END AS status_order,
       c.is_promoted,
       c.room_id IS NOT NULL                                           AS is_on_site,
       COALESCE(btrim(c.meeting_link), '') <> ''                       AS has_meeting_link,
       (SELECT count(*) FROM course_participant cp
        WHERE cp.course_id = c.id AND cp.status = 'R')                 AS participant_count,
       (SELECT count(*) FROM course_participant cp
        WHERE cp.course_id = c.id AND cp.status = 'R' AND cp.has_paid) AS paid_count,
       (SELECT count(*) FROM enquiry e WHERE e.course_id = c.id)       AS enquiry_count
FROM course c
         JOIN admin_training_summary ats ON ats.training_id = c.training_id;

-- Admini koolitajate nimekiri: üks rida koolitaja ja tõlkekeele kohta
CREATE VIEW admin_lecturer_summary AS
SELECT row_number() OVER (ORDER BY l.id, cl.id)                    AS id,
       l.id                                                        AS lecturer_id,
       cl.code                                                     AS content_language_code,
       COALESCE(lt.id, mlt.id)                                     AS lecturer_translation_id,
       l.full_name,
       COALESCE(lt.title, mlt.title)                               AS title,
       l.status,
       mt.missing_translation_language_codes,
       mt.missing_translation_language_codes IS NULL               AS has_all_translations,
       (SELECT count(*)
        FROM training_lecturer trl
                 JOIN training t ON t.id = trl.training_id
        WHERE trl.lecturer_id = l.id
          AND t.status <> 'D')                                     AS training_count,
       (SELECT count(*)
        FROM course_lecturer crl
                 JOIN course c ON c.id = crl.course_id
        WHERE crl.lecturer_id = l.id
          AND c.status NOT IN ('D', 'X')
          AND c.end_date >= current_date)                          AS upcoming_course_count,
       -- GREATEST ignoreerib NULL-e (pilti ei pruugi olla)
       GREATEST(l.updated_at,
                (SELECT MAX(alt.updated_at) FROM lecturer_translation alt WHERE alt.lecturer_id = l.id),
                (SELECT lp.updated_at FROM lecturer_photo lp WHERE lp.lecturer_id = l.id)) AS updated_at
FROM lecturer l
         CROSS JOIN language cl
         JOIN language ml ON ml.is_main_language
         LEFT JOIN lecturer_translation lt ON lt.lecturer_id = l.id AND lt.language_id = cl.id
         LEFT JOIN lecturer_translation mlt ON mlt.lecturer_id = l.id AND mlt.language_id = ml.id
         -- string_agg tühjast hulgast annab NULL, seega NULL = kõik tõlked olemas
         CROSS JOIN LATERAL (SELECT string_agg(rl.code, ',' ORDER BY rl.id) AS missing_translation_language_codes
                             FROM language rl
                             WHERE rl.requires_translation
                               AND NOT EXISTS (SELECT 1
                                               FROM lecturer_translation rlt
                                               WHERE rlt.lecturer_id = l.id
                                                 AND rlt.language_id = rl.id)) mt
WHERE cl.requires_translation;

-- Koolitusruumide nimekiri (admin): toimumiskordade arvud
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

-- Huviliste päringud (admin): üks rida päringu ja tõlkekeele kohta; koolituse nimi puudumisel põhikeeles
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
       p.first_name || ' ' || p.last_name                          AS full_name,
       p.email,
       p.phone,
       e.company_name,
       e.message,
       e.status,
       e.created_at
FROM enquiry e
         CROSS JOIN language cl
         JOIN profile p ON p.id = e.profile_id
         JOIN admin_training_summary ats ON ats.training_id = e.training_id AND ats.content_language_code = cl.code
         LEFT JOIN course c ON c.id = e.course_id
WHERE cl.requires_translation;

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

-- Koolituse kalender: toimumiskord koos koolitaja, ruumi ja osalejate arvuga
CREATE VIEW course_summary AS
SELECT c.id                                                         AS course_id,
       c.training_id,
       c.start_date,
       c.end_date,
       c.end_date < current_date                                    AS is_past,
       -- sorteerimiseks: tulevased lähimast, möödunud hiliseimast
       CASE WHEN c.end_date < current_date THEN current_date - c.start_date
            ELSE c.start_date - current_date END                    AS days_from_today,
       c.number_of_days,
       c.number_of_academic_hours,
       c.price,
       c.status,
       -- koolitajad sort_order järjekorras, nt 'Rain Tüür, Meelis Teern'; NULL = koolitajaid pole
       (SELECT string_agg(l.full_name, ', ' ORDER BY crl.sort_order)
        FROM course_lecturer crl
                 JOIN lecturer l ON l.id = crl.lecturer_id
        WHERE crl.course_id = c.id)                                 AS lecturer_names,
       c.room_id,
       r.name                                                       AS room_name,
       COALESCE(btrim(c.notes), '') <> ''                           AS has_notes,
       COALESCE(btrim(c.meeting_link), '') <> ''                    AS has_meeting_link,
       -- ainult registreerunud (R), loobunud (C) ei loe
       (SELECT count(*) FROM course_participant cp
        WHERE cp.course_id = c.id AND cp.status = 'R')              AS participant_count
FROM course c
         LEFT JOIN room r ON r.id = c.room_id;

-- Avalik koolituste kalender: üks rida toimumiskorra ja olemasoleva tõlke kohta (nagu training_summary);
-- ainult publitseeritud koolituse avatud või täis tulevased toimumiskorrad
CREATE VIEW public_course_summary AS
SELECT row_number() OVER (ORDER BY c.id, tt.id)                        AS id,
       c.id                                                            AS course_id,
       tl.code                                                         AS content_language_code,
       c.training_id,
       tt.id                                                           AS training_translation_id,
       tt.title,
       tt.short_description,
       t.category_id,
       ct.name                                                         AS category_name,
       t.training_language_id,
       trl.flag_icon_code                                              AS training_language_flag_icon_code,
       c.start_date,
       c.end_date,
       c.number_of_days,
       c.number_of_academic_hours,
       c.price,
       c.status,
       c.is_promoted,
       c.room_id IS NOT NULL                                           AS is_on_site,
       COALESCE(btrim(c.meeting_link), '') <> ''                       AS is_online,
       (SELECT string_agg(l.full_name, ', ' ORDER BY crl.sort_order)
        FROM course_lecturer crl
                 JOIN lecturer l ON l.id = crl.lecturer_id
        WHERE crl.course_id = c.id)                                    AS lecturer_names
FROM course c
         JOIN training t ON t.id = c.training_id
         JOIN training_translation tt ON tt.training_id = t.id
         JOIN language tl ON tl.id = tt.language_id
         JOIN language trl ON trl.id = t.training_language_id
         LEFT JOIN category_translation ct ON ct.category_id = t.category_id AND ct.language_id = tt.language_id
WHERE t.status = 'P'
  AND c.status IN ('O', 'F')
  AND c.start_date >= current_date;

-- Olemasoleva andmebaasi kommentaariveeru laiendamine.
-- Käivita eraldi, kui andmebaas loodi vana varchar(255) skeemiga.
-- Uuel andmebaasil on sama muudatus juba 2_create.sql failis.
BEGIN;
ALTER TABLE bcs_koolitused.course_participant_feedback
    ALTER COLUMN feedback_text TYPE text;
COMMIT;


-- End of file.