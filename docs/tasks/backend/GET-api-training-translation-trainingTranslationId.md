# Koolituse tõlke andmete päring

**Teenus:** `GET /api/training-translation/{trainingTranslationId}`

**Kasutav vaade:** `TrainingFormView.vue` (`/training-form`, `state: "update"` — avatud tõlge; `state: "new-translation"` — põhikeele tõlge eeltäitmiseks)

![Mockup](../../mock-wireframe/pdf-images/TrainingFormView-state-update.png)

## Sisend

**Path variable:**

| Nimi | Tüüp | Kirjeldus |
|---|---|---|
| `trainingTranslationId` | Integer | Päritava tõlke ID (`training_translation.id`) |

Teenusel puuduvad query parameetrid ja request body.

## Väljund

**Response (200 OK):** `TrainingTranslationDto.java` — ühe koolituse tõlke (ühes keeles) täielikud andmed.

```json
{
  "trainingTranslationId": 1,
  "trainingId": 1,
  "languageId": 1,
  "languageCode": "et",
  "title": "Java algkursus",
  "shortDescription": "Java programmeerimise alused algajatele.",
  "description": "Kursusel õpitakse Java süntaksit, objektorienteeritud programmeerimist ja põhilisi andmestruktuure."
}
```

Näidise andmed vastavad `3_import.sql` reale: `training_translation` id=1, koolitus 1 ("Java algkursus"), keel 1 (et).

Väljade selgitused:

- `trainingTranslationId` — `training_translation.id`.
- `trainingId` — `training_translation.training_id`, viide koolitusele.
- `languageId` — `training_translation.language_id`, tõlke keel.
- `languageCode` — tõlke keele kood (`language.code`, nt `"et"`/`"en"`), tuletatud `languageId` järgi.
- `title`, `shortDescription`, `description` — tõlgitud tekstiväljad; `description` võib sisaldada HTML-i (richtext).

**Teenust kasutatakse `TrainingFormView`-s kahes olekus erinevalt** (vt mockupi märkmed):

- **`state: "update"`** — laetakse URL-i `trainingTranslationId` järgi **avatud (muudetav) tõlge**. Vastuse `languageCode` järgi valib frontend vormi rippmenüüde (kategooria, rahastustüübid jm) sisu laadimiseks kasutatava `contentLang` väärtuse — st vastuse `languageCode` juhib, mis keeles ülejäänud vormi andmed kuvatakse.
- **`state: "new-translation"`** — teenust kutsutakse koolituse **põhikeele** tõlke `trainingTranslationId`-ga (leitakse eelnevalt `GET /api/training/{trainingId}/training-translations` vastusest, kus `isMainLanguage = true`). Vastust kasutatakse ainult uue tõlke vormi väljade (`title`, `shortDescription`, `description`) eeltäitmiseks — admin tõlgib need käsitsi või "Tee AI tõlge" abil uude keelde. Vormi rippmenüüd laaditakse siiski sihtkeeles, mitte vastuse `languageCode` järgi.

## Eesmärk

`TrainingFormView` vaates kuvab admin koolituse tõlgitud teksti. Olekus `"update"` päritakse ja kuvatakse konkreetne juba olemasolev tõlge, mida saab muuta ("Salvesta" → `PUT /api/training/{trainingId}`) või mille põhjal saab AI-tõlget teha teistesse keeltesse. Olekus `"new-translation"` kasutatakse sama teenust koolituse salvestatud põhikeele teksti lugemiseks, et sellega uue tõlke vorm eeltäita, enne kui admin selle uude keelde tõlgib ja salvestab (`POST /api/training/{trainingId}/training-translation`).

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

### training_translation

Koolituse tõlgitud tekstid, üks rida ühe koolituse ühe keele kohta.

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
```

Näidisandmed (`3_import.sql`): id 1–4, koolitustel 1 ja 2 on mõlemal et- ja en-tõlge, nt id=1 → `training_id=1, language_id=1` ("Java algkursus"), id=2 → `training_id=1, language_id=2` ("Java Basics").

### language

`languageCode` tuletatakse `training_translation.language_id` järgi sellest tabelist (`language.code`).

```sql
CREATE TABLE language
(
    id               serial      NOT NULL,
    code             varchar(2)  NOT NULL,
    name             varchar(50) NOT NULL,
    is_main_language boolean     NOT NULL,
    CONSTRAINT language_pk PRIMARY KEY (id),
    CONSTRAINT language_code_uq UNIQUE (code)
);
```

Näidisandmed: id=1 → `code = 'et'`, `is_main_language = true`; id=2 → `code = 'en'`, `is_main_language = false`.

Tabel `training` on siin ainult `trainingId` väärtuse edastamiseks vastuses — seda ei muudeta.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `trainingTranslationId` ei leidu andmebaasist | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'trainingTranslationId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

Esimene rida on mockupi märkmetest (`Veateated`) täpselt üle võetud (mõlemas olekus, `training-form-view-state-update-markmed.md` ja `training-form-view-state-new-translation-markmed.md`). Vastab mustrile `PrimaryKeyNotFoundException` + `getValidTrainingTranslationBy(Integer trainingTranslationId)` (vt `backend/CLAUDE.md`, "Entiteedi otsing ID järgi").

## Vastuvõtu kriteeriumid

- [ ] Endpoint `GET /api/training-translation/{trainingTranslationId}` on olemas
- [ ] Õnnestunud päring tagastab 200 OK ja `TrainingTranslationDto` (`trainingTranslationId`, `trainingId`, `languageId`, `languageCode`, `title`, `shortDescription`, `description`)
- [ ] `languageCode` vastab `training_translation.language_id`-le viitava `language` kirje `code` väärtusele
- [ ] `description` väli säilitab HTML-sisu muutumatuna (richtext ei filtreerita ega paeta)
- [ ] Olematu `trainingTranslationId` → 404 `PRIMARY_KEY_NOT_FOUND` sõnumiga "Ei leidnud primary keyd 'trainingTranslationId' väärtusega: \<id\>"
- [ ] Teenusel on automaattestid

## Avatud küsimused

1. **`TrainingTranslationDto` nimi ja kuju** — mockupi märkmetes ja frontendi mock-teenuses (`frontend/src/api-services/TrainingTranslationService.js`, `MockDatabase.getTrainingTranslation`) on DTO struktuur juba läbivalt kokku lepitud (`trainingTranslationId`, `trainingId`, `languageId`, `languageCode`, `title`, `shortDescription`, `description`). Backendis vastavat entiteeti/kontrollerit veel pole — kontrolli implementatsiooni ajal, kas jagatud DTO (`controller/common/dto/`) on vajalik, kuna sama kujuga vastust kasutab ka `POST /api/training/{trainingId}/training-translation` teenuse response (`trainingTranslationId` alamhulgana).
2. **Ressursi teekonna nimetus** — `training-translation` (ainsus) ei kattu ühegi olemasoleva controller'i baastee-osaga (`TrainingController` kasutab `/api/trainings` ja `/api/training`). Kas luua uus `TrainingTranslationController` eraldi `controller/training/translation/` paketti (analoogselt persistence struktuuriga `persistance/training/translation/`) — eeldan, et jah, kuid see tuleb implementatsiooni käigus kinnitada `docs/structure/backend-projekti-struktuur.md` järgi.
3. **`contentLang` roll oleku `"update"` puhul** — vaate märkmetest selgub, et frontend valib rippmenüüde keele vastuse `languageCode` järgi, mitte eraldi `contentLang` päringuparameetriga. See teenus ise `contentLang` parameetrit ei kasuta ega vaja — kinnitan seda siin, et implementeerimisel ei lisataks üleliigset parameetrit.
