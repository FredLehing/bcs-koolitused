# Koolituse kustutamine (soft delete)

**Teenus:** `DELETE /api/training/{trainingId}`

**Kasutav vaade:** `AdminTrainingsView.vue` (`/admin-trainings`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-trainings-view/admin-trainings-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/admin-trainings-view-markmed.md`.

## Sisend

**Path variable:**

| Nimi | Tüüp | Kirjeldus |
|---|---|---|
| `trainingId` | Integer | Kustutatava koolituse ID (`training.id`) |

Query parameetreid ja request body't pole.

## Väljund

**Response (200 OK):** Tühi vastus (ainult staatuskood 200 OK).

## Eesmärk

Admin kustutab `AdminTrainingsView` tabelis koolituse prügikasti ikooniga (komponent `TrainingDeleteButton.vue`, kinnituse modal). Kustutamine on **soft delete**: rida jääb andmebaasi, ainult `training.status` muutub väärtuseks `"D"`. Kustutatud koolitus kaob avalikust nimekirjast ja nimede ettepanekutest, teised teenused käsitlevad seda nagu olematut (vt `training-deleted-status.md`), aga admini tabelis näeb seda filtriga "Kustutatud" ja selle saab taastada (`PUT-api-training-trainingId-restore.md`). Kustutada saab nii mustandit kui ka publitseeritud koolitust.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

### training

Muudetakse ainult `status` (ja auditeerimise kaudu `updated_at`). Tõlkeid (`training_translation`) ega rahastustüüpe (`training_funding_type`) ei kustutata.

```sql
CREATE TABLE training
(
    id                   serial    NOT NULL,
    user_id              int       NOT NULL,
    default_lecturer_id  int       NULL,
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
```

**`TrainingStatus` enumisse lisatakse `DELETED("D")`** (`ee.bcskoolitus.TrainingStatus`; seni `UNPUBLISHED("U")`, `PUBLISHED("P")`). Täht `"D"` on kooskõlas `ApiStatus.STATUS_DELETED`-iga, kuid koolituse juures kasutatakse ikka `TrainingStatus`-t.

`updated_at` uueneb auditeerimisega (`@LastModifiedDate`), kui staatus muudetakse entiteedi kaudu — käsitsi seda ei seata (vt `backend/CLAUDE.md`, "Ajatemplid").

Näidisandmed: `3_import.sql` koolitused 1–8 on kõik `status = 'P'`.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `trainingId` ei leidu andmebaasist | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'trainingId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

404 vastab olemasolevale mustrile `TrainingService.getValidTrainingBy(Integer trainingId)` + `PrimaryKeyNotFoundException`. See teenus kasutab **olemasolevat** `getValidTrainingBy`-d (leiab ka kustutatud koolituse), mitte aktiivse koolituse meetodit.

**Juba kustutatud koolituse** uuesti kustutamine tagastab 200 OK ja midagi ei muutu (idempotentne).

## Vastuvõtu kriteeriumid

- [ ] `TrainingStatus` enumis on `DELETED("D")`
- [ ] Endpoint `DELETE /api/training/{trainingId}` on olemas, ei võta ega tagasta body't
- [ ] Õnnestunud päring tagastab 200 OK ja määrab `training.status = "D"`
- [ ] `updated_at` uueneb (auditeerimine), tõlked ja rahastustüübid jäävad alles
- [ ] Nii mustandit (`U`) kui ka publitseeritud (`P`) koolitust saab kustutada
- [ ] Juba kustutatud koolituse kustutamine → 200 OK, staatus ei muutu
- [ ] Olematu `trainingId` → 404 `PRIMARY_KEY_NOT_FOUND`
- [ ] Teenusel on automaattestid

## Avatud küsimused

1. **Tulevased toimumiskorrad** — hiljem võiks backend keelduda (`403`) koolituse kustutamisest, millel on tulevasi toimumiskordi (`course`). Praegu seda kontrolli ei tehta; lisatakse, kui `course` teenused valmivad.
