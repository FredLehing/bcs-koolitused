# Koolituse liigutamine mustandisse

**Teenus:** `PUT /api/training/{trainingId}/unpublish`

**Kasutav vaade:** `TrainingFormView.vue` (`/training-form?trainingId={id}&trainingTranslationId={id}`, `state: "update"` ja `state: "new-translation"`)

![Mockup](../../mock-wireframe/pdf-images/TrainingFormView-state-update.png)

## Sisend

**Path variable:**

| Nimi | Tüüp | Kirjeldus |
|---|---|---|
| `trainingId` | Integer | Mustandisse liigutatava koolituse ID (`training.id`) |

Query parameetreid ja request body't teenusel pole. See on **tegevusteenus** (vt `docs/mock-wireframe/kokkulepped/mock-wireframe-markmete-struktuur.md`, jaotis 3, "Erand — tegevusteenused"): path'i lõpus on tegusõna `unpublish` ja backend teab ise, millise väärtuse ta staatusele määrab — klient midagi ei saada.

## Väljund

**Response (200 OK):** Tühi vastus (ainult staatuskood 200 OK).

## Eesmärk

Admin muudab `TrainingFormView` vaates (nii olemasoleva koolituse muutmise kui uue tõlke lisamise olekus) publitseeritud koolituse tagasi mustandiks, kui koolitus tuleb ajutiselt avalikust nimekirjast eemaldada (nt enne sisu täiendamist). Vaates kuvatakse koolituse staatuse järgi kas nupp "Publitseeri" (kui `status = "U"`) või "Liiguta mustandisse" (kui `status = "P"`); mõlemal nupul on kinnituse modal ("Kas soovid koolituse mustandisse liigutada? See kaob avalikust nimekirjast."). Pärast unpublish'i kaob koolitus `TrainingsView` avalikust nimekirjast (`GET /api/trainings`) ja `CourseView` vaatest.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

### training

Koolituse põhikirje. Selle teenusega muudetakse ainult `status` ja `updated_at` veergu — muid välju ei puudutata.

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
    is_promoted           boolean   NOT NULL,
    CONSTRAINT course_pk PRIMARY KEY (id)
);
```

`status` väärtused: `"U"` = mustand (unpublished), `"P"` = publitseeritud. Kasuta eraldi `TrainingStatus` enumit (`UNPUBLISHED("U")`, `PUBLISHED("P")`) — **mitte** olemasolevat `ApiStatus` enumit, kus `"D"` tähendab kustutatud. Mustandi täht on teadlikult `"U"`, mitte `"D"`, et vältida segadust `ApiStatus.STATUS_DELETED`-ga — mustand ei ole kustutatud koolitus.

Näidisandmed: koolitused 1 ("Java algkursus") ja 2 ("Projektijuhtimise põhitõed") on `3_import.sql`-is `status = 'P'` — nende peal saab kontrollida tavajuhtumit (publitseeritust mustandiks muutmine). Idempotentse käitumise kontrollimiseks (juba mustandis koolituse unpublish) tuleb kõigepealt mõni koolitus `publish`/`unpublish` teenustega või otse andmebaasis `status = 'U'` peale viia, kuna näidisandmetes ühtegi `'U'` staatusega koolitust ei ole.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `trainingId` ei leidu andmebaasist | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'trainingId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

Esimene rida on mockupi märkmetest (`Veateated`) ja vastab olemasolevale mustrile `PrimaryKeyNotFoundException` + `getValid<Entiteet>By(Integer id)` (vt `backend/CLAUDE.md`) — sama meetodit (nt `getValidTrainingBy(Integer trainingId)`) saab jagada `GET /api/training/{trainingId}`, `PUT /api/training/{trainingId}`, publish ja unpublish teenuste vahel.

## Vastuvõtu kriteeriumid

- [ ] Endpoint `PUT /api/training/{trainingId}/unpublish` on olemas, ei võta request body't ega tagasta response body't
- [ ] Õnnestunud päring tagastab 200 OK
- [ ] Koolituse `status` muudetakse väärtuseks `"U"` (`TrainingStatus.UNPUBLISHED`)
- [ ] `updated_at` uuendatakse hetke ajale, kui staatus tegelikult muutub
- [ ] Idempotentne: juba mustandis koolituse (`status = "U"`) unpublish tagastab 200 OK, staatust ei muudeta uuesti ja viga ei teki
- [ ] Koolituse staatus kasutab `TrainingStatus` enumit, mitte `ApiStatus`-t
- [ ] Olematu `trainingId` → 404 `PRIMARY_KEY_NOT_FOUND`
- [ ] Teenusel on automaattestid (sh idempotentse kutse test)

## Avatud küsimused

1. **`TrainingStatus` enum — otsustatud:** luuakse `POST /api/training` taski käigus (esimene teenus, mis seda vajab). Kui see task tehakse enne, loo enum siin (`UNPUBLISHED("U")`, `PUBLISHED("P")`) — **mitte** `ApiStatus` (seal `"D"` = kustutatud).
2. **`updated_at` idempotentsel kutsel.** Pole selge, kas juba mustandis koolituse korduval `unpublish` kutsel tuleb `updated_at` siiski uuendada (kuna endpoint kutsuti), või jätta muutmata, kuna reaalset muutust ei toimu ("midagi ei muutu" kokkuleppe järgi). Task eeldab, et `updated_at` uueneb ainult tegeliku staatusemuutuse korral — kui soovitud käitumine on teine, tuleb see täpsustada.
3. **`getValidTrainingBy` meetod ja üldine training kirjutusteenistus (`TrainingService`) on veel implementeerimata** — praegune `TrainingService` sisaldab ainult `findFilteredTrainings`-t. See task eeldab, et vastav leiu-/uuendusloogika lisatakse selle või seotud taskide (`GET /api/training/{trainingId}`, `PUT /api/training/{trainingId}`) käigus.
4. **Näidisandmetes puudub mustandis (`status = 'U'`) koolitus.** Idempotentse unpublish-juhtumi manuaalseks testimiseks tuleb enne mõni koolitus staatusesse `'U'` viia (kas `publish`/`unpublish` teenustega või otse andmebaasis) — `3_import.sql` ei sisalda hetkel ühtegi mustandis koolitust.
