# Aktiivsete koolituste nimede nimekiri (otsingu ettepanekud)

**Teenus:** `GET /api/training-titles`

**Kasutav vaade:** `AdminTrainingsView.vue` (`/admin-trainings`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-trainings-view/admin-trainings-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/admin-trainings-view-markmed.md`.

## Sisend

| Parameeter | Tüüp | Kirjeldus |
|---|---|---|
| `contentLang` | String (`et`/`en`) | Nimede keel. Kui koolitusel selles keeles tõlget pole, tagastatakse põhikeele nimi |

Teenusel puudub request body.

## Väljund

**Response (200 OK):** `List<TrainingTitleDto>` — kõigi aktiivsete (`status <> 'D'`) koolituste nimed, sorteeritud `title` järgi.

Näide `?contentLang=et` (`3_import.sql` andmed; kustutatud "Photoshopi algkursus" (14) ei tule):

```json
[
  { "trainingId": 7, "title": "Agiilne meeskonnajuhtimine" },
  { "trainingId": 10, "title": "Docker ja konteinerid" },
  { "trainingId": 9, "title": "Exceli algkursus" },
  { "trainingId": 6, "title": "Figma praktikum" },
  { "trainingId": 11, "title": "Git ja GitHub" },
  { "trainingId": 1, "title": "Java algkursus" },
  { "trainingId": 2, "title": "Projektijuhtimise põhitõed" },
  { "trainingId": 12, "title": "Python andmeanalüüsiks" },
  { "trainingId": 3, "title": "Spring Boot veebiarendus" },
  { "trainingId": 8, "title": "SQL ja andmebaasid" },
  { "trainingId": 13, "title": "Tehisaru töövahendid arendajale" },
  { "trainingId": 5, "title": "UX disaini alused" },
  { "trainingId": 4, "title": "Vue.js esmaspetsialist" }
]
```

Järjekord sõltub andmebaasi collation'ist (tõstutundlikul `C` collation'il oleks "SQL…" enne "Spring…"); piisab andmebaasi vaikimisi järjestusest.

## Eesmärk

`AdminTrainingsView` otsinguväli pakub trükkimise ajal ette olemasolevaid koolituste nimesid (`<datalist>`). Vaade laadib nimekirja korra avamisel, keele vahetamisel navbaris ning pärast kustutamist ja taastamist. Nimekiri on lihtne ja väike, seega filtreerib frontend selle ise — iga tähemärgi peale backendi ei kutsuta.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

Andmed tulevad view'st `admin_training_summary`, mis luuakse `GET-api-admin-trainings.md` taskis (sama nime valik ja põhikeele varuvariant):

```sql
SELECT training_id, title
FROM admin_training_summary
WHERE content_language_code = :contentLang
  AND status <> 'D'
ORDER BY title;
```

Päringu võib kirjutada JPQL-ina repositooriumi `@Query`-ga (nt `findActiveTrainingTitlesBy(String contentLang)`) või Spring Data meetodinimega; tulemus mapitakse `TrainingTitleDto`-ks.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

Muid veaolukordi pole: tundmatu `contentLang` või koolituste puudumine annab tühja listi.

## Vastuvõtu kriteeriumid

- [ ] Endpoint `GET /api/training-titles` on olemas ja tagastab `List<TrainingTitleDto>` (`trainingId`, `title`)
- [ ] Tagastatakse ainult aktiivsed koolitused (`status` `U` või `P`); kustutatud ei ole nimekirjas
- [ ] Iga koolitus on nimekirjas üks kord; puuduva `contentLang` tõlke korral põhikeele nimi
- [ ] Nimekiri on sorteeritud `title` järgi
- [ ] Teenusel on automaattestid

## Sõltuvused

- View `admin_training_summary` (`GET-api-admin-trainings.md`).
