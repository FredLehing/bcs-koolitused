# Kustutatud ja mustandis koolituste peitmine olemasolevates teenustes

**Teenused (olemasolevad, muudetakse):**

- `GET /api/trainings`
- `GET /api/training/{trainingId}`
- `GET /api/training/{trainingId}/training-translations`
- `GET /api/training-translation/{trainingTranslationId}`
- `PUT /api/training/{trainingId}`
- `POST /api/training/{trainingId}/training-translation`

**Kasutavad vaated:** `TrainingsView.vue` (`/trainings`), `HomeView.vue` (`/`), `TrainingView.vue` (`/training`), `TrainingFormView.vue` (`/training-form`)

Taust: `AdminTrainingsView` tõi sisse soft delete'i (`training.status = 'D'`, vt `DELETE-api-training-trainingId.md`). Otsused: `docs/mock-wireframe/loo-mock-vaade/admin-trainings-view/admin-trainings-view-skeemid.md`, jaotis "Otsused".

## Sisend

Teenuste sisendid ei muutu.

## Väljund

Õnnestunud vastused ei muutu. Muutub, **milliseid koolitusi teenused leiavad**:

### 1. Avalik nimekiri näitab ainult publitseeritud koolitusi

`GET /api/trainings` (avalik `TrainingsView` ja `HomeView`) tagastab edaspidi ainult `status = 'P'` koolitused. **Praegu staatust ei filtreerita üldse** — mustandid on avalikus nimekirjas juba nähtavad ja pärast soft delete'i oleksid seal ka kustutatud koolitused.

- `training_summary` view'sse lisatakse veerg `t.status` (`docs/database/2_create.sql`) ja `TrainingSummary` entity'sse väli `status`.
- `TrainingSummarySpecifications`-isse tuleb tingimus (nt `hasStatus(String status)`), mida `TrainingService.findFilteredTrainings` rakendab alati väärtusega `TrainingStatus.PUBLISHED.getCode()`.
- Query parameetreid juurde ei tule — avalik vaade ei vali staatust.

### 2. Kustutatud koolitus on teistele teenustele nagu olematu

Kui koolituse `status = 'D'`, annavad järgmised teenused sama vea nagu olematu ID korral — `404 PRIMARY_KEY_NOT_FOUND`:

| Teenus | Praegune leiu-meetod | Muudatus |
|---|---|---|
| `GET /api/training/{trainingId}` | `TrainingService.getValidTrainingBy` | kasuta aktiivse koolituse meetodit |
| `GET /api/training/{trainingId}/training-translations` | `TrainingService.getValidTrainingBy` | kasuta aktiivse koolituse meetodit |
| `PUT /api/training/{trainingId}` | `TrainingService.getValidTrainingBy` | kasuta aktiivse koolituse meetodit |
| `POST /api/training/{trainingId}/training-translation` | `TrainingService.getValidTrainingBy` | kasuta aktiivse koolituse meetodit |
| `GET /api/training-translation/{trainingTranslationId}` | `TrainingTranslationService.getValidTrainingTranslationBy` | 404, kui tõlke koolitus on kustutatud (`trainingTranslationId` väljaga sõnumis) |

- `TrainingService`-isse lisatakse olemasoleva `getValidTrainingBy` kõrvale meetod, mis leiab ainult aktiivse koolituse (`status <> 'D'`), nt `getValidActiveTrainingBy(Integer trainingId)` (repositooriumis nt `findByIdAndStatusNot(...)` või `@Query`). Sõnum on sama: `Ei leidnud primary keyd 'trainingId' väärtusega: <id>`.
- **`getValidTrainingBy` jääb alles** — seda kasutavad teenused, mis peavad leidma ka kustutatud koolituse: `DELETE /api/training/{trainingId}`, `PUT /api/training/{trainingId}/restore`, `PUT /api/training/{trainingId}/publish` ja `/unpublish` (viimased kaks keelduvad `403 TRAINING_DELETED`, vt nende taskid).
- `GET /api/training/{trainingId}/ai-translation` pole veel implementeeritud — reegel on lisatud selle taski (`GET-api-training-trainingId-ai-translation.md`).

Frontendis muudatusi pole vaja: `TrainingView` ja `TrainingFormView` suunavad 404 korral juba praegu üldisele veavaatele.

## Eesmärk

Kustutatud koolitus peab kaduma igalt poolt peale admini tabeli: avalikust nimekirjast, koolituse vaatest (`/training`) ja vormist (`/training-form`). Kustutatud koolitust ei muudeta enne taastamist — admin taastab selle `AdminTrainingsView` tabelis. Samuti ei tohi avalikus nimekirjas olla mustandeid, mis on praegune viga.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

### training_summary (view, muudetakse)

Lisatakse veerg `status`:

```sql
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
```

### training

`status`: `U` = mustand, `P` = publitseeritud, `D` = kustutatud (`TrainingStatus`). Näidisandmed: `3_import.sql` koolitused 1–8 on kõik `P` — mustandi ja kustutatud koolituse testiks muuda andmeid testis.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Koolitus (`trainingId`) on kustutatud — teenused tabelist "2." | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'trainingId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Tõlke (`trainingTranslationId`) koolitus on kustutatud | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'trainingTranslationId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

## Vastuvõtu kriteeriumid

- [ ] `training_summary` view'l on veerg `status` ja `TrainingSummary` entity'l vastav väli
- [ ] `GET /api/trainings` tagastab ainult `status = 'P'` koolitused (ka `totalElements` / `totalPages` arvestavad seda)
- [ ] `TrainingService`-is on aktiivse koolituse leiu-meetod; `getValidTrainingBy` jääb alles
- [ ] Kustutatud koolituse korral annavad `GET /api/training/{trainingId}`, `GET .../training-translations`, `PUT /api/training/{trainingId}` ja `POST .../training-translation` 404 `PRIMARY_KEY_NOT_FOUND`
- [ ] `GET /api/training-translation/{trainingTranslationId}` annab 404, kui tõlke koolitus on kustutatud
- [ ] Mustandi (`U`) korral töötavad samad teenused nagu enne (admin muudab mustandit vormis)
- [ ] `DELETE`, `restore`, `publish` ja `unpublish` leiavad ka kustutatud koolituse
- [ ] Muudetud teenustel on automaattestid kustutatud koolituse juhtumi kohta

## Sõltuvused

- `TrainingStatus.DELETED` (`DELETE-api-training-trainingId.md`).
