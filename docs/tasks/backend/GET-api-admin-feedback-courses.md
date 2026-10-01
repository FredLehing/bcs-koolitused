# Toimunud toimumiskordade valik tagasiside filtrisse

**Seis:** implementeeritud 02.10.2026. Serveri autentimine/rollikontroll jääb tööde järjekorras märgitud kasutuselevõtu sõltuvuseks.

**Teenus:** `GET /api/admin-feedback-courses`

**Kasutav vaade:** `AdminFeedbacksView.vue` (`/admin-feedbacks`)

> Mockupi pilt lisatakse hiljem. Aluseks on kinnitatud [HTML-mock](../../mock-wireframe/loo-mock-vaade/admin-feedbacks-view/admin-feedbacks-view-labimang.html), [skeemid](../../mock-wireframe/loo-mock-vaade/admin-feedbacks-view/admin-feedbacks-view-skeemid.md) ja [märkmed](../../mock-wireframe/markmed/admin-feedbacks-view-markmed.md).

## Sisend

`contentLang`: valikuline String, puudumisel või tundmatu koodi puhul põhikeel. Request body puudub. Näide: `GET /api/admin-feedback-courses?contentLang=et`.

## Väljund

**Response (200 OK):** `List<AdminFeedbackCourseDto>`.

```json
[
  {
    "courseId": 15,
    "trainingId": 3,
    "trainingTitle": "Spring Boot veebiarendus",
    "startDate": "2026-09-28",
    "endDate": "2026-09-30"
  },
  {
    "courseId": 16,
    "trainingId": 15,
    "trainingTitle": "Vali-IT Noorem AI arendaja",
    "startDate": "2026-08-10",
    "endDate": "2026-09-25"
  },
  {
    "courseId": 14,
    "trainingId": 8,
    "trainingTitle": "SQL ja andmebaasid",
    "startDate": "2026-09-21",
    "endDate": "2026-09-23"
  },
  {
    "courseId": 12,
    "trainingId": 7,
    "trainingTitle": "Agiilne meeskonnajuhtimine",
    "startDate": "2026-09-14",
    "endDate": "2026-09-15"
  },
  {
    "courseId": 3,
    "trainingId": 1,
    "trainingTitle": "Java algkursus",
    "startDate": "2026-09-07",
    "endDate": "2026-09-11"
  },
  {
    "courseId": 7,
    "trainingId": 1,
    "trainingTitle": "Java algkursus",
    "startDate": "2026-06-08",
    "endDate": "2026-06-12"
  }
]
```

Valik sisaldab `course.end_date < LocalDate.now(Clock)` ja O/F staatusega toimumiskordi, ka ilma tagasisideta. Järjestus endDate DESC, courseId DESC. Koolituse soft delete ei eemalda ajaloolist toimumiskorda. Tühi valik → `[]`.

## Eesmärk

Admin valib konkreetse toimunud toimumiskorra, et võrrelda tagasisidet ja vastamismäära. Koolituse pealkiri tuleb kuvatavas keeles, puudumisel põhikeeles. Valik ei sõltu ülejäänud filtritest.

Kõik teenused on admini ärifunktsioonid. Olemasolev backend ei määra usaldusväärset serveripoolset autentimise/rollikontrolli lepingut; `SessionStorageService` või kliendilt saadud userId ei tõenda õigusi. Selle puudujäägi lahendamine on eraldi kasutuselevõtu sõltuvus, mitte nende taskide väljamõeldud 401/403 leping.

Uued teenused koondatakse `AdminFeedbackController` alla, äriloogika service-kihis ja andmepäringud `persistance` kihis. DTO-d ei tagasta entity'sid. Tõlgete fallback kasutab `language.is_main_language`; tundmatu või puuduv contentLang kasutab põhikeelt. Kui põhikeel või kohustuslik põhikeelne tõlge puudub, on tegu andmeinvariandi/serveriveaga. Ajatemplid tagastatakse UTC Instant-na, kuupäevad LocalDate-na. DML-i kohalikud timestamp-id tuleb JPA lugemisel ühtselt tõlgendada Europe/Tallinn ajana; näidetes on vahe +03:00.

## Seotud andmebaasi tabelid

Vt [DDL](../../database/2_create.sql) ja [näidisandmed](../../database/3_import.sql). Olemasolev skeem on piisav; uut tabelit ega versiooniveergu ei lisata.

### `course`

```sql
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
ALTER TABLE course
    ADD CONSTRAINT course_created_by
        FOREIGN KEY (created_by)
            REFERENCES "user" (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;
ALTER TABLE course
    ADD CONSTRAINT course_room
        FOREIGN KEY (room_id)
            REFERENCES room (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;
ALTER TABLE course
    ADD CONSTRAINT course_session_training
        FOREIGN KEY (training_id)
            REFERENCES training (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;
```

### `training`

```sql
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
ALTER TABLE training
    ADD CONSTRAINT course_category
        FOREIGN KEY (category_id)
            REFERENCES category (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;
ALTER TABLE training
    ADD CONSTRAINT course_location
        FOREIGN KEY (location_id)
            REFERENCES location (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;
ALTER TABLE training
    ADD CONSTRAINT course_user
        FOREIGN KEY (user_id)
            REFERENCES "user" (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;
ALTER TABLE training
    ADD CONSTRAINT training_language
        FOREIGN KEY (training_language_id)
            REFERENCES language (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;
```

### `training_translation`

```sql
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
ALTER TABLE training_translation
    ADD CONSTRAINT training_translation_language
        FOREIGN KEY (language_id)
            REFERENCES language (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;
ALTER TABLE training_translation
    ADD CONSTRAINT training_translation_training
        FOREIGN KEY (training_id)
            REFERENCES training (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;
```

### `language`

```sql
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
```

Näidisandmed: toimumiskorrad 7, 3, 12, 14, 15, 16; tagasiside 3 kuulub registreerumisele 11, osalejale Anna Saar ja SQL-i toimumiskorrale 14. Kokku 30 tagasisidet, 150 vastust; kriteeriumide ID-d 1–5. Kontrollnäited kasutavad kuupäeva 01.10.2026 ja Europe/Tallinn ajavööndit.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Ootamatu serveriviga | 500 Internal Server Error | Standardne Spring Booti serverivea vastus vastavalt konfiguratsioonile; praegune RestExceptionHandler ei taga ootamatu vea korral ApiError-kuju. |

Teenusel puuduvad kohustuslikud sisendid ja ID-põhine üksikkirje otsing; tundmatu keel kasutab põhikeelt. Seetõttu eraldi 400/404 ärivead puuduvad.

## Vastuvõtu kriteeriumid

- [x] Endpoint `GET /api/admin-feedback-courses` on olemas ja Swagger kirjeldab sisendid, DTO-d ja vead.
- [x] 200 vastus vastab DTO-le; näites valik 15, 14, 12, 3, 7.
- [x] Lõppkuupäev täna või tulevikus ning C/D toimumiskord jäävad valikust välja.
- [x] Tagasisideta courseId=15 jääb valikusse; tühi tulemus on [].
- [x] Koolituse kustutamine ei peida varasemat O/F toimumiskorda.
- [x] Ootamatu serveriviga annab 500; kliendile ei lubata olematut ühtset ApiError-lepingut.
- [x] Teenusel on automaattestid, kasutades fikseeritud Clock-i, tõlke fallbacki ja kirjeldatud servajuhte.
