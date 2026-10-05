# Admini tagasisidede tabel ja kogu hulga kokkuvõtted

**Seis:** implementeeritud 02.10.2026. Serveri autentimine/rollikontroll jääb tööde järjekorras märgitud kasutuselevõtu sõltuvuseks.

**Teenus:** `GET /api/admin-feedbacks`

**Kasutav vaade:** `AdminFeedbacksView.vue` (`/admin-feedbacks`)

> Mockupi pilt lisatakse hiljem. Aluseks on kinnitatud [HTML-mock](../../mock-wireframe/loo-mock-vaade/admin-feedbacks-view/admin-feedbacks-view-labimang.html), [skeemid](../../mock-wireframe/loo-mock-vaade/admin-feedbacks-view/admin-feedbacks-view-skeemid.md) ja [märkmed](../../mock-wireframe/markmed/admin-feedbacks-view-markmed.md).

## Sisend

Kõik parameetrid on query's, request body puudub. Näide: `GET /api/admin-feedbacks?contentLang=et&page=0&limit=5`.

| Parameeter | Tüüp / vaikeväärtus | Reegel |
|---|---|---|
| contentLang | String / põhikeel | Puudumisel/tundmatu koodi korral põhikeel |
| searchText | String / tühi | Iga tühikuga eraldatud sõna koolituse pealkirja ja osaleja nime liittekstis, tõstutundetu; SQL LIKE metamärgid on sõnasõnaline tekst |
| courseId | Integer / puudub | Positiivne olemasolev ID, puuduv kirje 404 |
| status | String / puudub | N, U, H, pending (N/U koos) |
| comments | String / puudub | yes = sisuline kommentaar olemas; no = puudub |
| from, until | LocalDate / puudub | ISO yyyy-MM-dd; from ≤ until; endDate ≥ from, startDate ≤ until |
| low | Integer / puudub | Ainult 3, 5 või 7; tagasiside MIN(score) ≤ low |
| sortBy | String / default | default, createdAt, answersUpdatedAt, trainingTitle, participantName, averageScore, minimumScore, status |
| sortDirection | String / desc | asc või desc; default-järjestuses suunda ei rakendata |
| page | Integer / 0 | Vähemalt 0 |
| limit | Integer / 5 | 1–100 |

Uus query/header valideerimine tagastab `400 INCORRECT_INPUT`, message `väljanimi: vigane väärtus`. Esimese vea korral piisab ühest väljast. Tühjad valikulised tekstfiltrid tähendavad filtri puudumist; tühja kohustuslikku headerit see erand ei puuduta. Numbrite, kuupäevade ja tüübiteisenduse vead peavad samuti jõudma sama 400 lepinguni. See on implementeeritud admini API sisendikontrolli ja RestExceptionHandler-i IncorrectInputException handleriga; olemasolev request DTO valideerimine säilib.

## Väljund

**Response (200 OK):** `AdminFeedbackPageDto`; read `AdminFeedbackSummaryDto`, koondkriteeriumid `FeedbackCriteriaSummaryDto`, vastamismäär `CourseFeedbackResponseRateDto`. Vastus näitab kogu esimest lehte, mitte vaid üht näidisrida.

```json
{
  "page": 0,
  "totalPages": 6,
  "totalElements": 30,
  "needsReviewCount": 14,
  "lowScoreFeedbackCount": 4,
  "overallAverageScore": 8.653333333333334,
  "courseResponseRate": null,
  "criteriaAverages": [
    {
      "feedbackCriteriaId": 1,
      "title": "Koolitus vastas ootustele",
      "averageScore": 8.566666666666666,
      "answerCount": 30
    },
    {
      "feedbackCriteriaId": 2,
      "title": "Koolitaja oli pädev",
      "averageScore": 9.0,
      "answerCount": 30
    },
    {
      "feedbackCriteriaId": 3,
      "title": "Õppematerjalid olid asjakohased",
      "averageScore": 8.566666666666666,
      "answerCount": 30
    },
    {
      "feedbackCriteriaId": 4,
      "title": "Õpikeskkond ja korraldus olid sobivad",
      "averageScore": 8.4,
      "answerCount": 30
    },
    {
      "feedbackCriteriaId": 5,
      "title": "Soovitaksin koolitust kolleegidele",
      "averageScore": 8.733333333333333,
      "answerCount": 30
    }
  ],
  "content": [
    {
      "feedbackId": 18,
      "courseParticipantId": 29,
      "courseId": 16,
      "trainingId": 15,
      "trainingTitle": "Vali-IT Noorem AI arendaja",
      "participantName": "Kristjan Oja",
      "startDate": "2026-08-10",
      "endDate": "2026-09-25",
      "createdAt": "2026-09-28T08:00:00Z",
      "answersUpdatedAt": "2026-09-30T12:00:00Z",
      "averageScore": 9.0,
      "minimumScore": 8,
      "commentCount": 1,
      "status": "U"
    },
    {
      "feedbackId": 12,
      "courseParticipantId": 23,
      "courseId": 16,
      "trainingId": 15,
      "trainingTitle": "Vali-IT Noorem AI arendaja",
      "participantName": "Jaan Org",
      "startDate": "2026-08-10",
      "endDate": "2026-09-25",
      "createdAt": "2026-09-28T06:00:00Z",
      "answersUpdatedAt": "2026-09-30T12:00:00Z",
      "averageScore": 9.0,
      "minimumScore": 8,
      "commentCount": 1,
      "status": "U"
    },
    {
      "feedbackId": 23,
      "courseParticipantId": 34,
      "courseId": 16,
      "trainingId": 15,
      "trainingTitle": "Vali-IT Noorem AI arendaja",
      "participantName": "Merle Ilves",
      "startDate": "2026-08-10",
      "endDate": "2026-09-25",
      "createdAt": "2026-09-27T10:00:00Z",
      "answersUpdatedAt": "2026-09-27T10:00:00Z",
      "averageScore": 9.2,
      "minimumScore": 8,
      "commentCount": 0,
      "status": "N"
    },
    {
      "feedbackId": 20,
      "courseParticipantId": 31,
      "courseId": 16,
      "trainingId": 15,
      "trainingTitle": "Vali-IT Noorem AI arendaja",
      "participantName": "Markus Lill",
      "startDate": "2026-08-10",
      "endDate": "2026-09-25",
      "createdAt": "2026-09-27T09:00:00Z",
      "answersUpdatedAt": "2026-09-27T09:00:00Z",
      "averageScore": 9.2,
      "minimumScore": 8,
      "commentCount": 0,
      "status": "N"
    },
    {
      "feedbackId": 11,
      "courseParticipantId": 22,
      "courseId": 16,
      "trainingId": 15,
      "trainingTitle": "Vali-IT Noorem AI arendaja",
      "participantName": "Liis Kuusk",
      "startDate": "2026-08-10",
      "endDate": "2026-09-25",
      "createdAt": "2026-09-27T06:00:00Z",
      "answersUpdatedAt": "2026-09-27T06:00:00Z",
      "averageScore": 9.2,
      "minimumScore": 8,
      "commentCount": 0,
      "status": "N"
    }
  ]
}
```

Üks rida = üks feedback. Kaasatakse mineviku toimumiskorra tagasiside; ajaloolist kirjet ei peideta hilisema training/course soft delete või registreerumise C tõttu. Olemasolev tuleviku courseId annab tühja tagasisidehulga, mitte 404; vastamismäär arvutatakse siiski valitud olemasolevast toimumiskorrast.

Kõik koondandmed, kriteeriumide keskmised ja totalElements arvutatakse kogu sama filtreeritud hulga pealt. Leht ja kokkuvõtted loetakse kooskõlalise hetktõmmisena. Liitumised ei tohi korrutada feedback'e ega vastuseid. SQL päringute arv ei tohi kasvada lehel olevate ridade arvuga; enne rea avamist lisadetailipäringuid ei tehta.

Rea keskmine = AVG olemasolevatest hinnetest; minimumScore = MIN, vastusteta kirjel mõlemad null. Kommentaaride arv loeb trimmitud mittetühja teksti; comments=yes nõuab arvu > 0, no = 0. Üldkeskmine on kõigi vastuste AVG, mitte feedback keskmiste AVG. Puuduv vastus ei ole 0. lowScoreFeedbackCount kasutab alati piiri 5, sõltumata low filtrist. Arvutused ja sort kasutavad ümardamata väärtusi. Vastuseid muudetud = MAX(course_participant_feedback.updated_at), vastusteta null; päise ülevaatamise aeg seda ei mõjuta.

Vaikimisi N/U koos enne H, grupis createdAt DESC, feedbackId DESC. Muude sortide võrdsed väärtused → feedbackId DESC; nullväärtused mõlemas suunas lõpus. Staatus asc N → U → H. Pealkirjad järjestatakse kuvatud keeles, nimed tõstutundetult (andmebaasi kokkulepitud kollatsioon); sorteerimisväljad on lubatud loend, kliendi teksti ei ühendata SQL-i.

criteriaAverages sisaldab aktiivseid kriteeriume ning filtreeritud hulgas vastatud D-kriteeriume. Järjestus sequence ASC, ID ASC, title UI keeles/fallback. Vastuseta kriteeriumil averageScore=null ja answerCount=0. Tühi tulemus → content=[], totalPages=0, totalElements/needsReviewCount/lowScoreFeedbackCount=0, overallAverageScore=null, aktiivsed kriteeriumid null-keskmisega. Vahemikust väljas leht annab content=[], säilitades kogu hulga koondid ja küsitud page.

courseResponseRate=null, kui courseId puudub. Muidu R-registreerumiste arv ning neist tagasiside esitanute arv; C jääb mõlemast välja. Muud filtrid ega page seda ei mõjuta. Nimetaja 0 → responsePercentage=null. Näide courseId=12:

```json
{
  "courseId": 12,
  "registeredCount": 5,
  "respondedCount": 4,
  "responsePercentage": 80.0
}
```

## Eesmärk

Admin leiab tähelepanu vajavad vastused ning näeb sama filtreeritud hulga kokkuvõtteid. Üks nimekirjapäring tagastab tabeli, leheküljed, kriteeriumide keskmised ja valitud toimumiskorra vastamismäära.

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
| Vigane filter, kuupäevavahemik, sort või leheküljestus | 400 Bad Request | `{ "message": "väljanimi: vigane väärtus", "errorCode": "INCORRECT_INPUT" }` |
| courseId=123 kirje puudub | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'courseId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Ootamatu serveriviga | 500 Internal Server Error | Standardne Spring Booti serverivea vastus vastavalt konfiguratsioonile; praegune RestExceptionHandler ei taga ootamatu vea korral ApiError-kuju. |

404 tuleneb olemasolevast `getValid<Entity>By` / `PrimaryKeyNotFoundException` mustrist. Uued 400/409 lepingud on selle arenduse osa.

## Vastuvõtu kriteeriumid

- [x] Endpoint `GET /api/admin-feedbacks` on olemas ja Swagger kirjeldab sisendid, DTO-d ja vead.
- [x] 200 vastus vastab DTO-le; esimese lehe ID-d 7, 3, 6, 5, 2; teine 8, 4, 9, 1.
- [x] DML-i algseisus kokku 30, ülevaatamata 14, madala hindega 4 ja keskmine 8.653333333333334.
- [x] Kõik filtrid koos, kattuvad kuupäevad, sõnapõhine otsing ja lubatud sordid annavad õige hulga.
- [x] Leheküljestus ei muuda koonde; dubleerivad join-id ega N+1 päringud puuduvad.
- [x] Course 12 vastamismäär 4/5, 14 6/6, 15 0/2; C ja muud filtrid määra ei mõjuta.
- [x] Tühi hulk, vastusteta feedback, puuduva tõlkega ja ajaloolise D-kriteeriumiga hulk on kaetud.
- [x] Vigane sisend annab 400 INCORRECT_INPUT, olematu courseId 404 PRIMARY_KEY_NOT_FOUND.
- [x] Ootamatu serveriviga annab 500; kliendile ei lubata olematut ühtset ApiError-lepingut.
- [x] Teenusel on automaattestid, kasutades fikseeritud Clock-i, tõlke fallbacki ja kirjeldatud servajuhte.
