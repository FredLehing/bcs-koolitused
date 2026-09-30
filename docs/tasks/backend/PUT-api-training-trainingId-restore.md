# Kustutatud koolituse taastamine

**Teenus:** `PUT /api/training/{trainingId}/restore`

**Kasutav vaade:** `AdminTrainingsView.vue` (`/admin-trainings`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-trainings-view/admin-trainings-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/admin-trainings-view-markmed.md`.

## Sisend

**Path variable:**

| Nimi | Tüüp | Kirjeldus |
|---|---|---|
| `trainingId` | Integer | Taastatava koolituse ID (`training.id`) |

Query parameetreid ja request body't pole. See on **tegevusteenus** (vt `docs/mock-wireframe/kokkulepped/mock-wireframe-markmete-struktuur.md`, jaotis 3, "Erand — tegevusteenused"), nagu `publish` ja `unpublish`.

## Väljund

**Response (200 OK):** Tühi vastus (ainult staatuskood 200 OK).

## Eesmärk

Admin näeb `AdminTrainingsView` tabelis kustutatud koolitusi filtriga Staatus = "Kustutatud". Kustutatud real on ainult nupp "Taasta" (komponent `TrainingStatusButton.vue`, kinnituse modal). Taastamine viib koolituse **mustandisse** (`D` → `U`), mitte tagasi eelmisesse olekusse — avalikuks muutub see alles eraldi publitseerimisega. Nii ei ilmu taastatud koolitus kogemata avalikku nimekirja.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

### training

Muudetakse ainult `status` (ja auditeerimise kaudu `updated_at`). Struktuur vt `DELETE-api-training-trainingId.md`.

`status` väärtused (`TrainingStatus`): `UNPUBLISHED("U")`, `PUBLISHED("P")`, `DELETED("D")` (viimane lisatakse `DELETE-api-training-trainingId.md` taskis).

Näidisandmed: `3_import.sql`-is kustutatud koolitusi pole — testi jaoks kustuta enne mõni koolitus (`DELETE /api/training/{trainingId}`).

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `trainingId` ei leidu andmebaasist | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'trainingId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

404 tuleb olemasolevast mustrist `TrainingService.getValidTrainingBy(Integer trainingId)` — see leiab ka kustutatud koolituse (**mitte** aktiivse koolituse meetod, vt `training-deleted-status.md`).

**Kustutamata koolituse** (`U` või `P`) taastamine tagastab 200 OK ja midagi ei muutu — publitseeritud koolitust ei liigutata mustandisse.

## Vastuvõtu kriteeriumid

- [ ] Endpoint `PUT /api/training/{trainingId}/restore` on olemas, ei võta ega tagasta body't
- [ ] Kustutatud koolituse (`D`) taastamine → 200 OK, `status = "U"`, `updated_at` uueneb (auditeerimine)
- [ ] Mustandi (`U`) või publitseeritud (`P`) koolituse taastamine → 200 OK, staatus ei muutu
- [ ] Olematu `trainingId` → 404 `PRIMARY_KEY_NOT_FOUND`
- [ ] Teenusel on automaattestid

## Sõltuvused

- `TrainingStatus.DELETED` (`DELETE-api-training-trainingId.md`).
