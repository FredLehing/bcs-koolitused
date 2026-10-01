# Tagasiside vastuste versioon ja samaaegne ülevaatamine

**Seis:** implementeeritud 02.10.2026. Serveri autentimine/rollikontroll jääb tööde järjekorras märgitud kasutuselevõtu sõltuvuseks.

**Seotud teenused:** olemasolev `PUT /api/user/{userId}/registration/{courseParticipantId}/feedback`, uus admini detail ja ülevaatamine.

**Kasutav vaade:** `AdminFeedbacksView.vue` ja osaleja olemasolev tagasisidevorm.

> Mockupi pilt lisatakse hiljem. Aluseks on kinnitatud [HTML-mock](../../mock-wireframe/loo-mock-vaade/admin-feedbacks-view/admin-feedbacks-view-labimang.html), [skeemid](../../mock-wireframe/loo-mock-vaade/admin-feedbacks-view/admin-feedbacks-view-skeemid.md) ja [märkmed](../../mock-wireframe/markmed/admin-feedbacks-view-markmed.md).

## Eesmärk

Admin peab märkima üle vaadatuks just need vastused, mida ta luges. Pelk status või MAX(updated_at) võrdlus ei piisa. Task täiendab olemasolevat osaleja PUT teenust ja määrab uute admini teenuste ühise versiooni/lukustamise lepingu; uusi DB veerge ei lisata.

## Sisend ja väljund

Osaleja PUT request/response jäävad [olemasoleva taski](PUT-api-user-userId-registration-courseParticipantId-feedback.md) ja tegeliku Controller/DTO järgi samaks. N/U/H töövoog jääb samaks: tegelik muudatus H → U; N → N, U → U; identsed vastused ei muuda staatust ega auditiveerge. Admini detail väljastab answersVersion, ülevaatus loeb X-Answers-Version headeri.

## Versiooni leping

Kanooniline UTF-8 JSON ilma tühikute/reavahetusteta: `[feedbackId,[[answerId,feedbackCriteriaId,score,feedbackText,updatedAtEpochSecond,updatedAtNano],...]]`. Vastused answerId ASC; ID-d/hinne/ajad on täisarvud. Tekst säilib täpselt, null on JSON null (ei muudeta tühjaks tekstiks). Unicode kirjutatakse UTF-8 märkidena, JSON nõutavad jutumärgi, kaldkriipsu ja juhtmärkide escapid säilivad; slash'i ei escape'ita. EpochSecond/Nano saadakse sama JPA Instant-i UTC väärtusest, mitte lokaalse aja stringist. Kontrollsumma = SHA-256 kanoonilistest baitidest, 64 väikest hex-märki. Tühja hulga kanooniline sisend `[feedbackId,[]]`.

Tõlked, küsimuse järjekord, status, feedback.updatedAt ja kõik UI väljad jäetakse kontrollsummast välja. Vastuse ID, kriteeriumi ID, hinne, täpne kommentaar või vastuse updatedAt muutus muudab versiooni. Näidis feedbackId=3:

```text
[3,[[11,1,5,"Tempo oli minu jaoks liiga kiire.",1790593200,0],[12,2,8,null,1790233200,0],[13,3,4,"Vajaksin rohkem algajatele sobivaid näiteid.",1790593200,0],[14,4,7,null,1790233200,0],[15,5,5,null,1790233200,0]]]
SHA-256: aa50de31307af532a06f996a2a161fab5d0316fd66d3abe7d0bc0f22dac37678
```

## Kooskõla ja vead

Detail peab lugema vastuste hulga ja nende versiooni ühest kooskõlalisest hetktõmmisest. Osaleja PUT ja admini review võtavad sama feedback rea pessimistic write luku enne vastuste lugemist/muutmist; detail järgib sama lukuprotokolli või samaväärset tõestatud snapshot-lahendust. Lukustus toimib andmebaasis ka mitme backend instantsi puhul, mitte Java protsessi mälus. Omanikuõiguste ja tagasiside lubatavuse olemasolevad kontrollid säilivad.

Admini review kontrollib versiooni ning muudab staatuse ühe transaktsiooni sees. Osaleja muudatuse või review ebaõnnestumisel rollback. Repositooriumimeetodite nimed väljendavad subjekti, päringud projekti JPQL/MapStruct/layering tavade järgi. Automaatset auditing'ut ei asendata käsitsi Instant.now-ga.

Lisada `ConflictException` → 409 handler ja `Error.FEEDBACK_ANSWERS_CHANGED`: „Tagasiside vastused on vahepeal muutunud. Vaata vastused uuesti üle.” Vea keha ApiError. Uute query/header vigade 400 leping on endpointide taskides; ootamatu 500 praeguses handleris ei ole alati ApiError.

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

## Vastuvõtu kriteeriumid

- [x] Olemasoleva osaleja teenuse API leping ja omanikukontroll säilivad.
- [x] Sama lukk kaitseb osaleja PUT-i ja admini review'd kõigis backend instantsides.
- [x] Versioon vastab kanoonilisele näitele; sama tekst null/tühi ning erinevad vastuse ID-d on eristatavad.
- [x] Keele, status'e või päise updatedAt muutus versiooni ei muuda.
- [x] Tegelik muudatus H → U; identne PUT ei muuda staatust ega vastuste/päise aega.
- [x] Kahe transaktsiooniga integratsioonitest tõendab mõlemat järjekorda: osaleja enne review → 409; review enne osalejat → lõpuks U.
- [x] Detail ei väljasta ühest versioonist teksti ja teisest versioonist kontrollsummat.
- [x] Rollback jätab andmed kooskõlaliseks; 409 annab ettenähtud ApiError-i.
