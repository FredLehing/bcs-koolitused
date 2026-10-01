# Koolituse õppekava (PDF) — plaan

Koolituse vormi (`/training-form`, olekud `new-training`, `update`, `new-translation`) laiendus: iga koolituse **tõlke** juurde saab üles laadida ühe PDF-faili, välja nimi on "Õppekava". Kokkulepe 2026-10-01.

Seotud failid: `training-form-view-skeemid.md` (vaate olekud ja andmevood), `training-form-view-labimang.html` (läbimäng), märkmed `docs/mock-wireframe/markmed/training-form-view-state-*-markmed.md`.

**Hiljem (selles plaanis ei tehta):** avalikus koolituse vaates (`/training`) on failinimi näha ja faili saab alla laadida. Allalaadimise teenus tehakse juba nüüd, seega hiljem lisandub ainult frontend.

---

## Otsused

- **Õppekava kuulub tõlke juurde.** Igal keelel on oma fail: avatud tõlkel (lipuke) on oma "Õppekava" väli. Üks fail tõlke kohta, mitte kohustuslik.
- **Eraldi tabel `training_translation_curriculum`** (1:1 `training_translation`-iga, rida puudub = õppekava pole). Faili baite (`bytea`) ei loeta koos tõlkega (nimekirjad, avalik vaade). Muster on sama mis `lecturer_photo` puhul.
- **Failinimi tehakse backendis ja salvestatakse** (veerg `file_name`): `<pealkiri>-<õppekava sõna>.pdf`, mõlemad osad puhastatud (vt "Failinime reegel"). Algset failinime ei salvestata.
- **Sõna "õppekava" tuleb frontendi i18n-ist tõlke keeles**, mitte kasutajaliidese keeles: inglise keelse kasutajaliidesega admin, kes muudab `et` tõlget, saab ikka `…-oppekava.pdf`. Frontend saadab sõna väljal `curriculumLabel`, backend seda ise ei tõlgi — keelte loetelu backendi koodi ei lähe.
- **Failinimi püsib pealkirjaga kooskõlas:** `curriculumLabel` saadetakse igal salvestamisel ja backend arvutab failinime uuesti ka siis, kui fail ise ei muutunud (nt muutus ainult pealkiri).
- **Üleslaadimine käib vormi salvestamisega samas päringus** (Base64 JSON-is, nagu koolitaja pilt: `photo` / `isPhotoRemoved`). Kõik on ühes transaktsioonis ja olek `new-training` ei vaja kahte sammu.
- **Kontroll:** ainult PDF (faili algus `%PDF-`), kuni **10 MB**. Vead nagu koolitaja pildil: 403 `CURRICULUM_TYPE_NOT_ALLOWED` / `CURRICULUM_TOO_LARGE` (`Error` enum). Frontend kontrollib sama juba faili valimisel.
- **"Eemalda" kinnitust ei küsi** — eemaldus jõustub alles salvestamisel ja kuni selleni saab "Tühista"-ga tagasi võtta.
- **"Tee AI tõlge" ja uue tõlke eeltäitmine faili ei puuduta.** Põhikeele õppekava uude tõlkesse ei kopeerita, iga keele fail laaditakse üles käsitsi.
- **Allalaadimine:** `GET /api/training-translation/{trainingTranslationId}/curriculum`. Kokkulepe oli "avalik ainult publitseeritud koolitusel", aga **backendis praegu autentimist pole** (nagu teistel admini teenustel) — vorm peab saama ka mustandi faili kätte. Seega: kättesaadav, kui koolitus pole kustutatud (`U` või `P`); avalik vaade `/training` näitab niikuinii ainult publitseeritud koolitusi. Piirang lisatakse koos autentimisega.

---

## Failinimi

### Reegel

1. Pealkiri ja `curriculumLabel` puhastatakse eraldi:
   - Unicode NFD + diakriitikute eemaldamine (`õ→o`, `ä→a`, `ö→o`, `ü→u`, `š→s`, `ž→z`);
   - väiketähtedeks (`Locale.ROOT`);
   - kõik, mis pole `a–z` ega `0–9`, → `-`; korduvad `-` üheks, otstest ära;
   - pealkirja osa kuni 100 märki (lõigatakse `-` kohalt).
2. Failinimi = `<pealkiri>-<sõna>.pdf`.
3. Kui pealkirjast jääb tühi tekst (nt ainult sümbolid või kirillitsa), kasutatakse `koolitus-<trainingId>`; kui sõnast jääb tühi, jäetakse see osa ära.

### Näited

| Keel | Pealkiri | `curriculumLabel` | Failinimi |
|---|---|---|---|
| `et` | Tehisaru töövahendid arendajale | Õppekava | `tehisaru-toovahendid-arendajale-oppekava.pdf` |
| `en` | AI tools for developers | Curriculum | `ai-tools-for-developers-curriculum.pdf` |
| `et` | Java & Spring: algkursus (2026) | Õppekava | `java-spring-algkursus-2026-oppekava.pdf` |

Abiklass: `infrastructure/util/FileNameSanitizer.java` (staatiline meetod, ühiktestid näidete põhjal). Backendis seni sellist puhastust pole — `HtmlSanitizer` puhastab HTML-i, mitte failinimesid.

---

## Andmebaas

```sql
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
```

- FK `training_translation_id` → `training_translation.id`. `1_reset_database.sql` ei muutu (kustutab kogu skeemi).
- `3_import.sql`: seed'i ei lisata (testfaili baite SQL-i ei panda).
- `file_size` baitides — vormis kuvatakse "(1,2 MB)", et ei peaks faili lugema.

---

## API muudatused

### Request DTO-d: `TrainingCreateRequestDto`, `TrainingUpdateRequestDto`, `TrainingTranslationCreateRequestDto`

| Väli | Tüüp | Kohustuslik | Tähendus |
|---|---|---|---|
| `curriculum` | String (Base64) | ei | uus fail; `null` = faili ei muudeta |
| `isCurriculumRemoved` | Boolean | ainult `update` DTO-s | `true` = fail eemaldatakse (`curriculum` peab siis olema `null`) |
| `curriculumLabel` | String, `@NotBlank`, `@Size(max = 50)` | jah | sõna "õppekava" tõlke keeles; kasutatakse failinimes |

Backendi loogika salvestamisel (ühes transaktsioonis tõlkega):

- `curriculum ≠ null` → kontroll (Base64, `%PDF-`, ≤ 10 MB) → rida lisatakse või asendatakse, `file_name` ja `file_size` arvutatakse.
- `isCurriculumRemoved = true` → rida kustutatakse.
- muidu, kui rida on olemas → ainult `file_name` arvutatakse uuesti (pealkiri võis muutuda).
- Vead:
  - 403 `CURRICULUM_TYPE_NOT_ALLOWED` — "Lubatud on ainult PDF-fail" (faili algus pole `%PDF-`);
  - 403 `CURRICULUM_TOO_LARGE` — "Õppekava on liiga suur, lubatud kuni 10 MB";
  - 400 `INCORRECT_INPUT` — vigane Base64, `curriculumLabel` puudub, `isCurriculumRemoved` koos `curriculum`-iga.

### `TrainingTranslationDto` (`GET /api/training-translation/{trainingTranslationId}`)

| Väli | Tüüp | Tähendus |
|---|---|---|
| `curriculumFileName` | String / `null` | nt `tehisaru-toovahendid-arendajale-oppekava.pdf`; `null` = õppekava pole |
| `curriculumFileSize` | Integer / `null` | baitides |

### Uus: `GET /api/training-translation/{trainingTranslationId}/curriculum`

- Vastus: faili baidid, `Content-Type: application/pdf`, `Content-Disposition: attachment; filename="<file_name>"` (Springi `ContentDisposition`).
- `Cache-Control: no-cache` — fail võib sama URL-i all vahetuda.
- 404 `PRIMARY_KEY_NOT_FOUND`: tõlget pole, koolitus kustutatud (`D`) või õppekava pole.

---

## Frontend (`TrainingFormView.vue`)

- Tõlke väljade all (pärast "Kirjeldus") uus väli **"Õppekava (PDF)"**:
  - faili pole → failivalija (`accept="application/pdf"`) + vihje "PDF, kuni 10 MB";
  - fail on → failinimi lingina (allalaadimine) + suurus, nupud **"Vaheta"** ja **"Eemalda"**;
  - valitud, veel salvestamata fail → selle nimi märkega "salvestatakse koos vormiga", nupp "Tühista";
  - "Eemalda" (kinnitust ei küsi) → märge "eemaldatakse salvestamisel", nupp "Tühista".
- Valimisel kontroll: tüüp PDF ja suurus ≤ 10 MB, muidu veateade välja all. Fail loetakse `FileReader`-iga Base64-ks.
- Salvestamisel saadetakse `curriculumLabel: this.$t('trainingForm.curriculum', {}, { locale: <avatud tõlke languageCode> })`.
- Pärast "Salvesta" laaditakse avatud tõlge uuesti (`GET /api/training-translation/{id}`), et näidata uut failinime.
- Olekus `new-translation` on väli tühi (põhikeele faili ei kopeerita).
- Uued i18n võtmed (`et` / `en`): `trainingForm.curriculum` ("Õppekava" / "Curriculum"), vihje, nupud, veateated.
- `TrainingService.js`: allalaadimise URL-i abifunktsioon.

---

## Tööde järjekord

| # | Töö | Taskifail | Keerukus | Märkus |
|---|---|---|---|---|
| 1 | Plaan, skeemid, märkmed, läbimäng | see fail, `training-form-view-skeemid.md`, `markmed/training-form-view-state-*-markmed.md`, `training-form-view-labimang.html` | — | ✅ tehtud 2026-10-01, artifactid avaldatud |
| 2 | DDL + entity + repository + `FileNameSanitizer` + `Error` koodid | `docs/tasks/backend/training-curriculum-db-changes.md` | lihtne | ✅ tehtud 2026-10-01 |
| 3 | Salvestamine: `POST /api/training`, `PUT /api/training/{trainingId}`, `POST …/training-translation` | `docs/tasks/backend/training-curriculum-upload.md` | keskmine | **muudab töötavat koodi** (3 DTO-d, `TrainingService`, testid) |
| 4 | `TrainingTranslationDto` väljad + `GET …/curriculum` | `docs/tasks/backend/GET-api-training-translation-trainingTranslationId-curriculum.md` | lihtne | pärast 2; testimiseks vaja 3 |
| 5 | Vormi väli `CurriculumUpload.vue` | `docs/tasks/frontend/training-form-curriculum.md` | keskmine | mockidega alustatav kohe; päris backendiga pärast 3–4 |
| 6 | (Hiljem) `/training` failinimi + allalaadimine | — | lihtne | teenus 4 on olemas |

Soovitus: 2 → 3 → 4 ühes harus järjest (sama andmemudel), 5 eraldi harus. Iga taski järel backend testid (`./gradlew test`), frontendi järel lint + build scratchpadis.

---

## Seis ja üleanne

**Seis (2026-10-01):** haru `RAIN-training-curriculum` (master'ist). Valmis: plaan, skeemid, märkmed, läbimäng (artifactid https://claude.ai/artifact/3VjRgmnQK7Ck7qWVHuRK9b ja kest https://claude.ai/artifact/SqxcgWP1BoxUGZqFfSZCPY) ja taskid 2–5. Task 2 kood valmis (SQL, entity, repository + projektsioon `TrainingTranslationCurriculumInfo`, `FileNameSanitizer` + testid, `Error` koodid; `./gradlew test` roheline; DB skriptid jooksutatud ja `bootRun` töötab).

**Järgmine samm:** task 3 `training-curriculum-upload.md`, siis 4, siis 5.

**Uue sessiooni alguses loe:** see fail (otsused + failinime reegel), taskifail, `backend/CLAUDE.md` / `frontend/CLAUDE.md`. Eeskuju koodis: `LecturerPhotoService`, `LecturerPhoto*`, `PhotoUpload.vue`, `@ValidBase64`, `isPhotoRemoved` kontroll `LecturerUpdateRequestDto`-s.

**Pea meeles:**
- `curriculumLabel` = sõna "Õppekava" **tõlke** keeles (`$t(…, {}, { locale })`), mitte kasutajaliidese keeles; saadetakse igal salvestamisel, failinimi arvutatakse uuesti.
- Fail baidid ainult eraldi tabelis; `GET /api/training-translation/{id}` loeb ainult nime ja suuruse.
- Backendis autentimist pole → allalaadimine töötab ka mustandil (teadlik otsus).
- Kest (`index.html` artifact) on avaldatud vana `index.html`-iga — master'i uuemad muudatused (profiilivaated, "Kontod") pole kestas; enne kesta uuesti avaldamist loe need failid läbi.
