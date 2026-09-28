# Lektorite nimekirja otsing

**Teenus:** `GET /api/lecturers?search={search}`

**Kasutav vaade:** `TrainingFormView.vue` (`/training-form`, "Vali lektor" modal)

![Mockup](../../mock-wireframe/pdf-images/TrainingFormView-state-new-training.png)

## Sisend

| Parameeter | Tüüp | Kirjeldus |
|---|---|---|
| `search` | String | Otsingusõna lektori nimest (`lecturer.full_name`); valikuline (`@RequestParam(required = false, defaultValue = "")`) — tühi väärtus tagastab kõik lektorid |

Teenusel puudub request body.

Otsing tehakse backendis: `full_name` peab otsingusõna **sisaldama**, tõstutundetult (nt JPQL `LOWER(l.fullName) LIKE LOWER(CONCAT('%', :search, '%'))` või vastav natiivne `ILIKE %search%`).

## Väljund

**Response (200 OK):** Otsingutingimusele vastavate lektorite nimekiri.

`LecturerDto.java` (ettepanek — koodis veel ei eksisteeri):

```json
[
  {
    "lecturerId": 1,
    "lecturerName": "Mari Tamm",
    "lecturerPhoto": ""
  },
  {
    "lecturerId": 2,
    "lecturerName": "Jaan Kask",
    "lecturerPhoto": ""
  }
]
```

Väljade selgitused:

- `lecturerName` — pärineb veerust `lecturer.full_name`.
- `lecturerPhoto` — pärineb veerust `lecturer.photo` (`bytea`, `NOT NULL`), DTO-s String. Näidisandmetes (`3_import.sql`) on mõlema lektori `photo` väärtus `''::bytea` (tühi bait-massiiv), seega tagastatav `lecturerPhoto` on praeguste andmetega alati tühi string `""` — mitte `null`, sest veerg on `NOT NULL`. Teisendus tehakse Base64 kodeeringuga (`Base64.getEncoder().encodeToString(bytes)`) — **mitte** `StringBytesConverter`-iga, mis teeb UTF-8 teisenduse ja rikuks binaarse pildi.
- `lecturer_translation.bio` (lektori CV) **ei kuulu** selle teenuse vastusesse — "Vali lektor" modal näitab ainult nime ja pilti.
- Tulemus on sorteeritud `full_name` järgi tähestikuliselt (otsustatud: lektorid nime järgi, lühikesed valikunimekirjad id järgi).
- Tulemuseta otsing (mitte ükski lektor ei sisalda otsingusõna) tagastab tühja listi `[]`, mitte viga.

## Eesmärk

`TrainingFormView` vaates ("Lisa uus koolitus" olek, roll: Admin) avab kasutaja koolituse vormil "Vali lektor" modali, kus saab lektorit nime järgi otsida ja nimekirjast valida koolituse vaikimisi lektori (`defaultLecturerId` väljale). Lektori valimine ei ole kohustuslik — väli võib jääda tühjaks. Iga otsingusõna muutmisel modalis tehakse uus päring sellesama teenuse `search` parameetriga.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

### lecturer

Lektori põhikirje — nimi ja foto.

```sql
CREATE TABLE lecturer
(
    id         serial       NOT NULL,
    full_name  varchar(255) NOT NULL,
    photo      bytea        NOT NULL,
    created_at timestamp    NOT NULL,
    updated_at timestamp    NOT NULL,
    created_by int          NOT NULL,
    CONSTRAINT lecturer_pk PRIMARY KEY (id)
);
```

Näidisandmed (`docs/database/3_import.sql`):

| id | full_name | photo |
|---|---|---|
| 1 | Mari Tamm | `''::bytea` (tühi) |
| 2 | Jaan Kask | `''::bytea` (tühi) |

### lecturer_translation

Lektori tõlgitud CV (`bio`) keelte kaupa — **ei puutu sellesse teenusesse**, kuna vastus ei sisalda `bio` välja (vt "Väljund").

```sql
CREATE TABLE lecturer_translation
(
    id          serial    NOT NULL,
    lecturer_id int       NOT NULL,
    language_id int       NOT NULL,
    bio         text      NOT NULL,
    created_at  timestamp NOT NULL,
    updated_at  timestamp NOT NULL,
    CONSTRAINT lecturer_translation_pk PRIMARY KEY (id),
    CONSTRAINT lecturer_translation_uq UNIQUE (lecturer_id, language_id)
);
```

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Ootamatu serveri viga | 500 Internal Server Error | Standardne `ApiError` vastus |

Mockupi märkmetes on `Veateated: —`. `search` parameeter on valikuline String, mistõttu ei teki `@RequestParam` teisendusvigu nagu Integer-parameetrite puhul. Tulemuseta otsing ei ole veaolukord (vt "Väljund").

## Vastuvõtu kriteeriumid

- [ ] Endpoint `GET /api/lecturers` on olemas ja tagastab `LecturerDto` listi
- [ ] `search` parameter on valikuline; puuduva/tühja `search` korral tagastatakse kõik lektorid
- [ ] Otsing `full_name` järgi on tõstutundetu ja "sisaldab" tüüpi (nt `Tam` leiab "Mari Tamm")
- [ ] Otsingule mittevastav sisend tagastab tühja listi, mitte viga
- [ ] Tulemus on sorteeritud `full_name` järgi tähestikuliselt
- [ ] `lecturerPhoto` on `lecturer.photo` Base64 kujul (praeguste näidisandmetega tühi string); `StringBytesConverter`-it ei kasutata
- [ ] Vastus ei sisalda `bio` (CV) välja
- [ ] Kirjeldatud veaolukord (500 ootamatu vea korral) on käsitletud
- [ ] Teenusel on automaattestid

## Avatud küsimused

1. **`LecturerDto` puudub veel koodis.** Backend-paketis (`backend/src/main/java/ee/bcskoolitus/`) on ainult `persistance/lecturer/Lecturer.java` ja `persistance/lecturer/translation/LecturerTranslation.java` (entiteedid) — `controller/lecturer/`, `service/`, repositoorium ja mapper puuduvad täielikult, samuti endpoint ise. Task eeldab, et kogu kiht luuakse uuena, mustris nagu `FundingTypeDto`/`CategoryDto` (vt `controller/common/dto/FundingTypeDto.java`).
2. **`photo` (bytea) → String teisendus — otsustatud: Base64** (`Base64.getEncoder().encodeToString(bytes)`). Taust: Olemasolev abiklass `infrastructure/util/StringBytesConverter.java` teisendab baidimassiivi ja String'i vahel **otse UTF-8 kodeeringuga** (`new String(bytes, StandardCharsets.UTF_8)`), mitte tegeliku Base64 kodeeringuga. Kui `lecturer.photo` peaks tegelikkuses sisaldama binaarset pilti (mockupi näites `"lecturerPhoto": "BASE64-image-data"`), annaks see converter kehtetu/rikutud stringi, mitte korrektse Base64 väärtuse. Praeguste näidisandmetega (`''::bytea`, tühi) pole vahet näha, kuna tulemuseks on mõlemal juhul tühi string. Seetõttu kasutatakse päris Base64 kodeerimist.
3. **Järjestus — otsustatud:** `full_name` järgi tähestikuliselt (lektorid nime järgi; lühikesed valikunimekirjad nagu kategooriad ja keeled `id` järgi).
