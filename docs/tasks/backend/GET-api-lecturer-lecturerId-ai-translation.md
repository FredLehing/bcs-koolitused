# Koolitaja tõlke tegemine AI abil

**Teenus:** `GET /api/lecturer/{lecturerId}/ai-translation?languageId={id}`

**Kasutav vaade:** `LecturerFormView.vue` (`state: "new-translation"`; `state: "update"`, kui avatud tõlge ei ole põhikeeles) — nupp "Tee AI tõlge"

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-lecturers-view/admin-lecturers-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/lecturer-form-view-state-new-translation-markmed.md`.

**Sama lahendus nagu `GET-api-training-trainingId-ai-translation.md`** (Spring AI + Google Gemini, veakäsitlus, `Cache-Control: no-store`). Tee see **pärast** koolituse AI tõlget ja kasuta sama AI tõlke teenust (ühine meetod, mis tõlgib välja-komplekti), mitte koopiat. Frontend võib seni kasutada mock-vastust.

## Sisend

**Path variable:**

| Nimi | Tüüp | Kirjeldus |
|---|---|---|
| `lecturerId` | Integer | Koolitaja, kelle põhikeele tõlge tõlgitakse |

**Query parameeter:**

| Nimi | Tüüp | Kohustuslik | Kirjeldus |
|---|---|---|---|
| `languageId` | Integer | jah | **Sihtkeel** (`language.id`); ei tohi olla põhikeel |

Request body't pole.

## Väljund

**Response (200 OK):** `LecturerAiTranslationDto` (ettepanek; samad väljad nagu koolituse `AiTranslationDto` — kaalu ühist DTO-d `controller/common/dto/`-s).

```json
{
  "title": "Lecturer/consultant",
  "shortDescription": "Adobe Photoshop, Illustrator, InDesign, Acrobat, Canva, Figma, digital marketing, e-learning design, Office applications.",
  "description": "<p>Adobe Photoshop, Illustrator, InDesign, Acrobat, Canva, Figma, digital marketing, e-learning design, Office applications.</p>"
}
```

- Tõlgitakse **alati andmebaasi salvestatud põhikeele tõlge** (kõik kolm välja), mitte vormi sisu.
- Andmebaasi midagi ei salvestata — tulemus täidab ainult vormi; salvestab "Lisa tõlge" või "Salvesta".
- `description` HTML-märgendid säilitatakse; vastus puhastatakse `HtmlSanitizer`-iga.
- Päis `Cache-Control: no-store`.

## Eesmärk

Admin laseb `LecturerFormView`-s AI-l tõlkida koolitaja ametinimetuse, lühikirjelduse ja kirjelduse sihtkeelde ning parandab tulemust enne salvestamist. Salvestamata muudatuste korral küsib frontend enne üle kirjutamist kinnitust.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql` ja `lecturer-db-changes.md`: `lecturer_translation` (põhikeele rida), `language`.

Näidisandmed: Kersti Laidvee (3) → `languageId = 2` (en tõlge puudub).

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `lecturerId` ei leidu või koolitaja on kustutatud | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'lecturerId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| `languageId` ei leidu | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'languageId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| `languageId` on põhikeel | 403 Forbidden | `{ "message": "Põhikeelde ei saa AI tõlget teha", "errorCode": "MAIN_LANGUAGE_NOT_TRANSLATABLE" }` |
| AI teenus ei vasta, annab vea, keeldub või vastus on vigase kujuga | 503 Service Unavailable | `{ "message": "AI tõlketeenus ei ole hetkel kättesaadav", "errorCode": "AI_SERVICE_UNAVAILABLE" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

`MAIN_LANGUAGE_NOT_TRANSLATABLE` ja `AI_SERVICE_UNAVAILABLE` lisatakse `Error` enumisse koolituse AI tõlke taskis. Põhikeele tõlge on koolitajal alati olemas (luuakse koos koolitajaga ja tõlkeid ei kustutata), seega koolituse taski `MAIN_TRANSLATION_NOT_FOUND` siin vaja pole.

## Vastuvõtu kriteeriumid

- [ ] Endpoint `GET /api/lecturer/{lecturerId}/ai-translation` on olemas
- [ ] Tõlgib salvestatud põhikeele `title`, `shortDescription`, `description`; midagi ei salvestata
- [ ] Kasutab sama AI tõlke lahendust mis koolituse AI tõlge
- [ ] Põhikeel sihtkeelena → 403; olematu / kustutatud koolitaja või keel → 404; AI viga → 503
- [ ] Vastusel on `Cache-Control: no-store`
- [ ] Teenusel on automaattestid (AI klient mockitud)
