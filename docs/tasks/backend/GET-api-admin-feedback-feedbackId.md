# Admini avatud tagasiside vastused ja versioon

**Seis:** implementeeritud 02.10.2026. Serveri autentimine/rollikontroll jääb tööde järjekorras märgitud kasutuselevõtu sõltuvuseks.

**Teenus:** `GET /api/admin-feedback/{feedbackId}`

**Kasutav vaade:** `AdminFeedbacksView.vue` (`/admin-feedbacks`)

> Mockupi pilt lisatakse hiljem. Aluseks on kinnitatud [HTML-mock](../../mock-wireframe/loo-mock-vaade/admin-feedbacks-view/admin-feedbacks-view-labimang.html), [skeemid](../../mock-wireframe/loo-mock-vaade/admin-feedbacks-view/admin-feedbacks-view-skeemid.md) ja [märkmed](../../mock-wireframe/markmed/admin-feedbacks-view-markmed.md).

## Sisend

`feedbackId`: kohustuslik positiivne Integer path variable. `contentLang`: valikuline String query, puudumisel/tundmatu koodi puhul põhikeel. Body puudub. Näide: `GET /api/admin-feedback/3?contentLang=et`.

Uus query/header valideerimine tagastab `400 INCORRECT_INPUT`, message `väljanimi: vigane väärtus`. Esimese vea korral piisab ühest väljast. Tühjad valikulised tekstfiltrid tähendavad filtri puudumist; tühja kohustuslikku headerit see erand ei puuduta. Numbrite, kuupäevade ja tüübiteisenduse vead peavad samuti jõudma sama 400 lepinguni. See on implementeeritud admini API sisendikontrolli ja RestExceptionHandler-i IncorrectInputException handleriga; olemasolev request DTO valideerimine säilib.

## Väljund

**Response (200 OK):** `AdminFeedbackDto`.

```json
{
  "feedbackId": 3,
  "courseParticipantId": 11,
  "courseId": 14,
  "trainingId": 8,
  "trainingTitle": "SQL ja andmebaasid",
  "participantName": "Anna Saar",
  "startDate": "2026-09-21",
  "endDate": "2026-09-23",
  "status": "U",
  "createdAt": "2026-09-24T07:00:00Z",
  "answersUpdatedAt": "2026-09-28T11:00:00Z",
  "answersVersion": "aa50de31307af532a06f996a2a161fab5d0316fd66d3abe7d0bc0f22dac37678",
  "criteria": [
    {
      "feedbackCriteriaId": 1,
      "title": "Koolitus vastas ootustele",
      "description": "Koolituse sisu, tase ja maht vastasid koolituse kirjelduse põhjal tekkinud ootustele.",
      "score": 5,
      "feedbackText": "Tempo oli minu jaoks liiga kiire."
    },
    {
      "feedbackCriteriaId": 2,
      "title": "Koolitaja oli pädev",
      "description": "Koolitaja valdas teemat põhjalikult ning selgitas seda arusaadavalt ja näidetega.",
      "score": 8,
      "feedbackText": null
    },
    {
      "feedbackCriteriaId": 3,
      "title": "Õppematerjalid olid asjakohased",
      "description": "Õppematerjalid ja harjutused toetasid teema omandamist ning on kasutatavad ka pärast koolitust.",
      "score": 4,
      "feedbackText": "Vajaksin rohkem algajatele sobivaid näiteid."
    },
    {
      "feedbackCriteriaId": 4,
      "title": "Õpikeskkond ja korraldus olid sobivad",
      "description": "Koolitusruum või veebikeskkond, ajakava ja info edastamine toetasid õppimist.",
      "score": 7,
      "feedbackText": null
    },
    {
      "feedbackCriteriaId": 5,
      "title": "Soovitaksin koolitust kolleegidele",
      "description": "Julgeksin seda koolitust soovitada kolleegidele või teistele samade vajadustega inimestele.",
      "score": 5,
      "feedbackText": null
    }
  ]
}
```

Vaid tegelikult antud vastused, sh hiljem D-kriteeriumiks muutunud küsimused. Hiljem lisatud vastamata kriteerium ei ole osaleja vastus. Järjestus sequence ASC, ID ASC. Kasutada olemasolevat `FeedbackCriteriaItemDto`, vajadusel tõsta `controller/common/dto` alla ja uuendada ka osaleja API importi; väljakujuga identset DTO-d ei dubleerita. Detail loetakse kooskõlalise hetktõmmisena koos versiooniga, järgides [konkurentsikontrolli taski](feedback-review-concurrency.md). Puuduvad vastused → criteria=[], answersUpdatedAt=null, kuid ka tühjal hulgal on versioon.

Versioon ei sõltu kuvatud keelest, tõlgetest ega staatusest. Lugemine ei muuda staatust ega auditiveerge. Detail on admini teenus, mitte osaleja omanikukontrolliga teenuse ümberkasutus.

## Eesmärk

Admin avab rea vastused enne ülevaadatuks märkimist. Vastuste ja kontrollsumma ühine hetktõmmis võimaldab hiljem kontrollida, et üle vaadatakse täpselt loetud vastused.

Kõik teenused on admini ärifunktsioonid. Olemasolev backend ei määra usaldusväärset serveripoolset autentimise/rollikontrolli lepingut; `SessionStorageService` või kliendilt saadud userId ei tõenda õigusi. Selle puudujäägi lahendamine on eraldi kasutuselevõtu sõltuvus, mitte nende taskide väljamõeldud 401/403 leping.

Uued teenused koondatakse `AdminFeedbackController` alla, äriloogika service-kihis ja andmepäringud `persistance` kihis. DTO-d ei tagasta entity'sid. Tõlgete fallback kasutab `language.is_main_language`; tundmatu või puuduv contentLang kasutab põhikeelt. Kui põhikeel või kohustuslik põhikeelne tõlge puudub, on tegu andmeinvariandi/serveriveaga. Ajatemplid tagastatakse UTC Instant-na, kuupäevad LocalDate-na. DML-i kohalikud timestamp-id tuleb JPA lugemisel ühtselt tõlgendada Europe/Tallinn ajana; näidetes on vahe +03:00.

## Seotud andmebaasi tabelid

Vt [DDL](../../database/2_create.sql) ja [näidisandmed](../../database/3_import.sql). Olemasolev skeem on piisav; uut tabelit ega versiooniveergu ei lisata.

### `feedback`

```sql
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
ALTER TABLE feedback
    ADD CONSTRAINT feedback_course_participant
        FOREIGN KEY (course_participant_id)
            REFERENCES course_participant (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;
```

### `course_participant_feedback`

```sql
CREATE TABLE course_participant_feedback
(
    id                   serial       NOT NULL,
    feedback_id          int          NOT NULL,
    feedback_criteria_id int          NOT NULL,
    score                int          NOT NULL,
    feedback_text        text NULL,
    created_at           timestamp    NOT NULL,
    updated_at           timestamp    NOT NULL,
    CONSTRAINT course_participant_feedback_pk PRIMARY KEY (id),
    -- tagasisides üks vastus kriteeriumi kohta
    CONSTRAINT course_participant_feedback_uq UNIQUE (feedback_id, feedback_criteria_id),
    CONSTRAINT course_participant_feedback_score_ck CHECK (score BETWEEN 1 AND 10)
);
ALTER TABLE course_participant_feedback
    ADD CONSTRAINT course_participant_feedback_feedback
        FOREIGN KEY (feedback_id)
            REFERENCES feedback (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;
ALTER TABLE course_participant_feedback
    ADD CONSTRAINT course_participant_feedback_feedback_criteria
        FOREIGN KEY (feedback_criteria_id)
            REFERENCES feedback_criteria (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;
```

### `feedback_criteria`

```sql
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
```

### `feedback_criteria_translation`

```sql
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
ALTER TABLE feedback_criteria_translation
    ADD CONSTRAINT feedback_criteria_translation_feedback_criteria
        FOREIGN KEY (feedback_criteria_id)
            REFERENCES feedback_criteria (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;
ALTER TABLE feedback_criteria_translation
    ADD CONSTRAINT feedback_criteria_translation_language
        FOREIGN KEY (language_id)
            REFERENCES language (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;
```

### `course_participant`

```sql
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
ALTER TABLE course_participant
    ADD CONSTRAINT course_participant_course
        FOREIGN KEY (course_id)
            REFERENCES course (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;
ALTER TABLE course_participant
    ADD CONSTRAINT course_participant_participant
        FOREIGN KEY (participant_id)
            REFERENCES participant (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;
```

### `participant`

```sql
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
ALTER TABLE participant
    ADD CONSTRAINT participant_profile
        FOREIGN KEY (profile_id)
            REFERENCES profile (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;
ALTER TABLE participant
    ADD CONSTRAINT participant_user
        FOREIGN KEY (user_id)
            REFERENCES "user" (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;
```

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
| Vigane või mittepositiivne feedbackId | 400 Bad Request | `{ "message": "väljanimi: vigane väärtus", "errorCode": "INCORRECT_INPUT" }` |
| feedbackId=123 kirje puudub | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'feedbackId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Ootamatu serveriviga | 500 Internal Server Error | Standardne Spring Booti serverivea vastus vastavalt konfiguratsioonile; praegune RestExceptionHandler ei taga ootamatu vea korral ApiError-kuju. |

404 tuleneb olemasolevast `getValid<Entity>By` / `PrimaryKeyNotFoundException` mustrist. Uued 400/409 lepingud on selle arenduse osa.

## Vastuvõtu kriteeriumid

- [x] Endpoint `GET /api/admin-feedback/{feedbackId}` on olemas ja Swagger kirjeldab sisendid, DTO-d ja vead.
- [x] Näidis feedbackId=3 tagastab kõik viis vastust, U-staatuse ja kanoonilise versiooni.
- [x] Avamine ei muuda staatust ega aega; H-detail on samuti loetav.
- [x] Ajaloolised vastused jäävad loetavaks hilisema kustutamise või registreerumise C korral.
- [x] Kustutatud vastatud kriteerium kaasatakse, uus vastamata kriteerium jäetakse välja.
- [x] Keelevahetus ei muuda versiooni; null ja tühi kommentaar jäävad vastuses eristatuks.
- [x] Tühi vastuste hulk ja samaaegne osaleja muudatus ei anna segatud hetktõmmist.
- [x] Vigane ID annab 400 INCORRECT_INPUT; olematu ID 404 PRIMARY_KEY_NOT_FOUND.
- [x] Ootamatu serveriviga annab 500; kliendile ei lubata olematut ühtset ApiError-lepingut.
- [x] Teenusel on automaattestid, kasutades fikseeritud Clock-i, tõlke fallbacki ja kirjeldatud servajuhte.
