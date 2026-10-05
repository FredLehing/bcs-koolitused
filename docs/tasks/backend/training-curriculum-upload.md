# Koolituse õppekava salvestamine koos tõlkega

**Teenused (muudetakse):** `POST /api/training`, `PUT /api/training/{trainingId}`, `POST /api/training/{trainingId}/training-translation`

**Kasutav vaade:** `TrainingFormView.vue` (`/training-form`; olekud `new-training`, `update`, `new-translation`)

> Mockupi pilt lisatakse hiljem. Seni vt plaani `docs/mock-wireframe/loo-mock-vaade/form-view/training-curriculum-plaan.md`, märkmeid `docs/mock-wireframe/markmed/training-form-view-state-*-markmed.md` (API märkmed `POST /api/training`, `PUT /api/training/{trainingId}`, `POST /api/training/{trainingId}/training-translation`) ja läbimängu `docs/mock-wireframe/loo-mock-vaade/form-view/training-form-view-labimang.html` (artifact https://claude.ai/artifact/3VjRgmnQK7Ck7qWVHuRK9b).

Eeldab taski `training-curriculum-db-changes.md`. **Muudab töötavat koodi** (`TrainingService`, kolm request DTO-d, olemasolevad testid). Muster nagu koolitaja pildil (`LecturerPhotoService`, `photo` / `isPhotoRemoved`, `@ValidBase64`).

## Sisend

Kolmele olemasolevale request DTO-le lisanduvad väljad (olemasolevad väljad ei muutu):

| Väli | Tüüp | DTO-d | Kohustuslik | Kirjeldus |
|---|---|---|---|---|
| `curriculum` | String (Base64) | kõik kolm | ei | Uus PDF-fail; `null` = faili ei lisata / ei muudeta. `@ValidBase64` |
| `isCurriculumRemoved` | Boolean | ainult `TrainingUpdateRequestDto` | ei (`null` = `false`) | `true` = õppekava eemaldatakse; `curriculum` peab siis olema `null` (kontroll nagu `LecturerUpdateRequestDto` `isPhotoRemoved` puhul) |
| `curriculumLabel` | String | kõik kolm | jah, `@NotBlank`, `@Size(max = 50)` | Sõna "Õppekava" **tõlke keeles** (frontendi i18n-ist, nt "Õppekava" / "Curriculum"); läheb failinimesse |

`TrainingCreateRequestDto` (`POST /api/training`, põhikeele tõlge):

```json
{
  "userId": 1,
  "categoryId": 1,
  "trainingLanguageId": 1,
  "locationId": 2,
  "lecturerIds": [6],
  "isOrderable": true,
  "isPromoted": false,
  "fundingTypeIds": [1, 2],
  "title": "Power BI edasijõudnutele",
  "shortDescription": "Andmemudelid, DAX ja interaktiivsed aruanded.",
  "description": "<p>Kursusel ehitatakse Power BI-s andmemudel, kirjutatakse DAX-valemeid ja luuakse interaktiivseid aruandeid.</p>",
  "curriculum": "JVBERi0xLjcKJeLjz9MK...",
  "curriculumLabel": "Õppekava"
}
```

`TrainingUpdateRequestDto` (`PUT /api/training/{trainingId}`, avatud tõlge) — failita salvestus, kus muutus ainult pealkiri:

```json
{
  "categoryId": 1,
  "trainingLanguageId": 1,
  "locationId": 1,
  "lecturerIds": [1, 8],
  "isOrderable": true,
  "isPromoted": true,
  "fundingTypeIds": [1],
  "trainingTranslationId": 1,
  "title": "Java algkursus",
  "shortDescription": "Java programmeerimise alused algajatele.",
  "description": "<p>Kursusel õpitakse Java süntaksit, objektorienteeritud programmeerimist ja põhilisi andmestruktuure.</p>",
  "curriculum": null,
  "isCurriculumRemoved": false,
  "curriculumLabel": "Õppekava"
}
```

`TrainingTranslationCreateRequestDto` (`POST /api/training/{trainingId}/training-translation`):

```json
{
  "languageId": 2,
  "title": "Power BI for Advanced Users",
  "shortDescription": "Data models, DAX and interactive reports.",
  "description": "<p>The course builds a data model in Power BI, writes DAX formulas and creates interactive reports.</p>",
  "curriculum": "JVBERi0xLjcKJeLjz9MK...",
  "curriculumLabel": "Curriculum"
}
```

## Väljund

Vastused ei muutu (`TrainingCreateResponseDto`, tühi vastus, `TrainingTranslationCreateResponseDto`).

**Õppekava loogika (uus teenus, nt `TrainingTranslationCurriculumService`, kutsutakse sama transaktsiooni sees pärast tõlke salvestamist):**

| Olukord | Tegevus |
|---|---|
| `curriculum ≠ null` | kontroll (vt allpool) → `training_translation_curriculum` rida lisatakse või asendatakse: `file`, `file_size` = baitide arv, `file_name` = `FileNameSanitizer.createCurriculumFileName(title, curriculumLabel, trainingId)` |
| `isCurriculumRemoved = true` (ainult PUT) | rida kustutatakse (kui oli) |
| muidu, rida on olemas (ainult PUT) | ainult `file_name` arvutatakse uuesti — pealkiri võis muutuda; `file` ei muutu |
| muidu, rida puudub | midagi ei tehta |

`title` on salvestatud tõlke pealkiri (sama päringu väärtus). Teiste keelte tõlgete õppekavasid ei puudutata.

**Faili kontroll** (järjekord nagu `LecturerPhotoService.createNormalizedPhoto`):

1. Base64 dekodeerimine (vigane Base64 jõuab siia harva — `@ValidBase64` püüab selle DTO tasemel kinni → 400).
2. Suurus > 10 MB (10 × 1024 × 1024 baiti) → 403 `CURRICULUM_TOO_LARGE`.
3. Fail ei alga baitidega `%PDF-` → 403 `CURRICULUM_TYPE_NOT_ALLOWED`.

Fail salvestatakse muutmata kujul (erinevalt pildist ei normaliseerita).

> **Päringu suurus:** 10 MB fail on Base64-na ~13,4 miljonit märki. Jacksoni stringi vaikepiir on 20 miljonit märki, Tomcat JSON body suurust vaikimisi ei piira — kontrolli testiga, et 10 MB fail läbi läheb ja ~10,5 MB annab 403 (mitte 400/500).

## Eesmärk

Admin lisab koolituse vormis igale tõlkele ühe PDF-faili "Õppekava". Fail salvestub samas päringus ja transaktsioonis mis tõlge, seega olek `new-training` ei vaja kahte sammu. Failinimi tehakse pealkirjast ja tõlke keele sõnast; `curriculumLabel` tuleb igal salvestamisel kaasa, et failinimi järgiks pealkirja muutust.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql` ja `training-curriculum-db-changes.md`: `training_translation`, `training_translation_curriculum` (uus). Seed'is õppekavasid pole — testi Swaggerist nt koolitusega 1 "Java algkursus" (tõlked 1 `et`, 2 `en`): oodatav failinimi `java-algkursus-oppekava.pdf` / `java-basics-curriculum.pdf`.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Fail ei ole PDF (algus pole `%PDF-`) | 403 Forbidden | `{ "message": "Lubatud on ainult PDF-fail", "errorCode": "CURRICULUM_TYPE_NOT_ALLOWED" }` |
| Fail on suurem kui 10 MB | 403 Forbidden | `{ "message": "Õppekava on liiga suur, lubatud kuni 10 MB", "errorCode": "CURRICULUM_TOO_LARGE" }` |
| `curriculum` on vigane Base64, `curriculumLabel` puudub / on tühi / üle 50 märgi, `isCurriculumRemoved = true` koos `curriculum`-iga | 400 Bad Request | `{ "message": "<väli>: <valideerimise teade>", "errorCode": "INCORRECT_INPUT" }` |
| Olemasolevad vead (404 `PRIMARY_KEY_NOT_FOUND`, 403 `TRANSLATION_EXISTS` jne) | nagu enne | muutmata |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

403 vead on plaanist (`training-curriculum-plaan.md`), 400 tuleneb olemasolevast mustrist (`RestExceptionHandler`, `@ValidBase64`). Vea korral ei salvestu midagi (ka tõlke muudatused rullitakse tagasi).

## Vastuvõtu kriteeriumid

- [ ] Kolmel request DTO-l on uued väljad ja valideerimine; Swaggeri kirjeldused uuendatud
- [ ] `POST /api/training` ja `POST …/training-translation` koos `curriculum`-iga loovad `training_translation_curriculum` rea õige `file_name` ja `file_size`-ga; ilma failita rida ei teki
- [ ] `PUT`: uus fail asendab vana; `isCurriculumRemoved = true` kustutab rea; failita salvestus uuendab ainult `file_name`-i (pealkirja muutus)
- [ ] Teiste keelte õppekavad ei muutu
- [ ] Mitte-PDF → 403 `CURRICULUM_TYPE_NOT_ALLOWED`; > 10 MB → 403 `CURRICULUM_TOO_LARGE`; vea korral tõlge ei muutu
- [ ] Valideerimisvead → 400 `INCORRECT_INPUT`
- [ ] Olemasolevad testid uuendatud (uus kohustuslik `curriculumLabel`), uued testid õppekava juhtudele
