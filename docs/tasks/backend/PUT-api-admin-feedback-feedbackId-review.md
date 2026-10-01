# Tagasiside märkimine üle vaadatuks

**Seis:** implementeeritud 02.10.2026. Serveri autentimine/rollikontroll jääb tööde järjekorras märgitud kasutuselevõtu sõltuvuseks.

**Teenus:** `PUT /api/admin-feedback/{feedbackId}/review`

**Kasutav vaade:** `AdminFeedbacksView.vue` (`/admin-feedbacks`)

> Mockupi pilt lisatakse hiljem. Aluseks on kinnitatud [HTML-mock](../../mock-wireframe/loo-mock-vaade/admin-feedbacks-view/admin-feedbacks-view-labimang.html), [skeemid](../../mock-wireframe/loo-mock-vaade/admin-feedbacks-view/admin-feedbacks-view-skeemid.md) ja [märkmed](../../mock-wireframe/markmed/admin-feedbacks-view-markmed.md).

## Sisend

`feedbackId`: kohustuslik positiivne Integer path variable. Kohustuslik header `X-Answers-Version`: avatud detaili täpne läbipaistmatu answersVersion. Body ja request DTO puuduvad. Näide: `PUT /api/admin-feedback/3/review`, header `X-Answers-Version: aa50de31307af532a06f996a2a161fab5d0316fd66d3abe7d0bc0f22dac37678`.

Puuduv/tühi või mitte 64 väikese hex-märgi kujul versioon annab 400. Korrektse kujuga, kuid erinev versioon annab 409.

Uus query/header valideerimine tagastab `400 INCORRECT_INPUT`, message `väljanimi: vigane väärtus`. Esimese vea korral piisab ühest väljast. Tühjad valikulised tekstfiltrid tähendavad filtri puudumist; tühja kohustuslikku headerit see erand ei puuduta. Numbrite, kuupäevade ja tüübiteisenduse vead peavad samuti jõudma sama 400 lepinguni. See on implementeeritud admini API sisendikontrolli ja RestExceptionHandler-i IncorrectInputException handleriga; olemasolev request DTO valideerimine säilib.

## Väljund

**Response (200 OK):** tühi vastus, ainult staatuskood 200.

Transaktsioonis võtta sama feedback rea lukk kui osaleja muutmisel; lugeda vastused, võrrelda versiooni ja alles siis N/U → H. Sama versiooniga H → H on idempotentne ning ei muuda auditiveerge. Erinev versioon annab 409 ka H korral. Puuduv kirje 404; midagi osaliselt ei salvestata. Muutunud päise updatedAt täidab auditing, vastuste hindeid/tekste/aegu ei muudeta. Eeldus: [konkurentsikontrolli task](feedback-review-concurrency.md) valmis.

## Eesmärk

Admin kinnitab, et on näinud avatud vastuseid. Teenus väldib osaleja vahepeal muudetud vastuste märkimist üle vaadatuks; sihtstaatus on alati H.

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

Näidisandmed: toimumiskorrad 7, 3, 12, 14, 15, 16; tagasiside 3 kuulub registreerumisele 11, osalejale Anna Saar ja SQL-i toimumiskorrale 14. Kokku 30 tagasisidet, 150 vastust; kriteeriumide ID-d 1–5. Kontrollnäited kasutavad kuupäeva 01.10.2026 ja Europe/Tallinn ajavööndit.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Vigane feedbackId või puuduv/tühi/vigane X-Answers-Version | 400 Bad Request | `{ "message": "väljanimi: vigane väärtus", "errorCode": "INCORRECT_INPUT" }` |
| feedbackId=123 kirje puudub | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'feedbackId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Vastuste versioon erineb | 409 Conflict | `{ "message": "Tagasiside vastused on vahepeal muutunud. Vaata vastused uuesti üle.", "errorCode": "FEEDBACK_ANSWERS_CHANGED" }` |
| Ootamatu serveriviga | 500 Internal Server Error | Standardne Spring Booti serverivea vastus vastavalt konfiguratsioonile; praegune RestExceptionHandler ei taga ootamatu vea korral ApiError-kuju. |

404 tuleneb olemasolevast `getValid<Entity>By` / `PrimaryKeyNotFoundException` mustrist. Uued 400/409 lepingud on selle arenduse osa.

## Vastuvõtu kriteeriumid

- [x] Endpoint `PUT /api/admin-feedback/{feedbackId}/review` on olemas ja Swagger kirjeldab sisendid, DTO-d ja vead.
- [x] Kehtiv versioon muudab N/U → H ja tagastab tühja 200; H sama versiooniga jääb muutmata.
- [x] Ülevaatus ei muuda vastuseid, answersUpdatedAt ega answersVersion.
- [x] Puuduv/vigane header või ID annab 400 INCORRECT_INPUT; olematu kirje 404 PRIMARY_KEY_NOT_FOUND.
- [x] Vahepeal muudetud vastused annavad 409 FEEDBACK_ANSWERS_CHANGED ja ükski andmeväli ei muutu.
- [x] Automaatne auditing muudab päise aega üksnes tegelikul staatusmuudatusel.
- [x] Samaaegse osaleja PUT korral pole võimalik kontrolli ja H salvestamise vahele lukuta muudatust teha.
- [x] Ootamatu serveriviga annab 500; kliendile ei lubata olematut ühtset ApiError-lepingut.
- [x] Teenusel on automaattestid, kasutades fikseeritud Clock-i, tõlke fallbacki ja kirjeldatud servajuhte.
