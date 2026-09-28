# Kategooriate nimekirja päring

**Teenus:** `GET /api/categories?contentLang={contentLang}`

**Kasutav vaade:** `TrainingFormView.vue` (`/training-form`), `TrainingsView.vue` (`/trainings`)

![Mockup](../../mock-wireframe/pdf-images/TrainingFormView-state-new-training.png)

## Sisend

Query parameeter on kohustuslik (`@RequestParam` ilma `required = false`-ta), samamoodi nagu teenuses `GET /api/trainings`:

| Parameeter | Tüüp | Kirjeldus |
|---|---|---|
| `contentLang` | String (`et`/`en`) | Määrab tagastatava `categoryName` välja keele (`category_translation.language_id` kaudu, valitud `language.code` järgi) |

Teenusel puudub request body.

## Väljund

**Response (200 OK):** Kõigi süsteemis olevate kategooriate nimekiri `contentLang` keeles.

`CategoryDto.java` (uus DTO, luuakse kausta `controller/common/dto/`, kõrvuti olemasoleva `FundingTypeDto`-ga — sama kujuga jagatud DTO muster):

```json
[
  {
    "categoryId": 1,
    "categoryName": "Programmeerimine"
  },
  {
    "categoryId": 2,
    "categoryName": "Disain"
  },
  {
    "categoryId": 3,
    "categoryName": "Juhtimine"
  }
]
```

Väljade selgitused:

- `categoryId` — `category.id`.
- `categoryName` — tõlgitud nimi (`category_translation.name`), valitud `contentLang` parameetri järgi määratud `language.code` alusel.
- Tulemus sisaldab kõiki kategooriaid, sorteeritud `category.id` järgi kasvavalt (lühikeste valikunimekirjade ühtne reegel).

## Eesmärk

Teenust kasutab kaks vaadet: `TrainingFormView` (roll Admin) laeb kategooriad "Kategooria" rippmenüü valikuteks uue koolituse lisamise vormil, päring tehakse vormi avatud tõlke keelega (base põhikeeles `et`); `TrainingsView` (kõik rollid, sh külastajad) laeb kategooriad "Koolituse kategooria" filtri valikuteks, kasutades `localStorage`-ist võetud `contentLang` väärtust. Mõlemal juhul on tegu staatilise valikute nimekirjaga, mida kuvatakse kasutajale kategooria valimiseks/filtreerimiseks.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

### category

Kategooria põhikirje (tõlgitavat teksti ei hoia).

```sql
CREATE TABLE category
(
    id         serial    NOT NULL,
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL,
    created_by int       NOT NULL,
    CONSTRAINT category_pk PRIMARY KEY (id)
);
```

Näidisandmed (`docs/database/3_import.sql`): id 1, 2, 3 (kõik `created_by = 1`).

### category_translation

Kategooria tõlgitud nimi keele kaupa.

```sql
CREATE TABLE category_translation
(
    id          serial       NOT NULL,
    category_id int          NOT NULL,
    language_id int          NOT NULL,
    name        varchar(255) NOT NULL,
    created_at  timestamp    NOT NULL,
    updated_at  timestamp    NOT NULL,
    CONSTRAINT category_translation_pk PRIMARY KEY (id),
    CONSTRAINT category_translation_uq UNIQUE (category_id, language_id)
);
```

Näidisandmed (`docs/database/3_import.sql`):

| category_id | language_id | name |
|---|---|---|
| 1 | 1 (et) | Programmeerimine |
| 1 | 2 (en) | Programming |
| 2 | 1 (et) | Disain |
| 2 | 2 (en) | Design |
| 3 | 1 (et) | Juhtimine |
| 3 | 2 (en) | Management |

### language

`contentLang` kasutab `language.code` väärtust (`et`/`en`).

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

Näidisandmed: id=1 → code `et` (`is_main_language = true`), id=2 → code `en`.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Ootamatu serveri viga | 500 Internal Server Error | Standardne `ApiError` vastus (`message`, `errorCode`) |

Mockupi märge ütleb `Veateated: —`. Tundmatu `contentLang` väärtuse korral (nt kui väärtust ei leidu `language` tabelis) ei ole tegu veaolukorraga — tulemuseks on tühi list, sarnaselt teenusele `GET /api/trainings`. Kui `contentLang` parameeter puudub päringust täielikult, tagastab Spring vaikimisi `400 Bad Request` (puuduva kohustusliku `@RequestParam` korral), kuid ilma projekti `ApiError`/`errorCode` kujuta — see on Springi enda vaikekäitumine, mitte äriloogikaga veaolukord.

## Vastuvõtu kriteeriumid

- [ ] Endpoint `GET /api/categories` on olemas ja tagastab `CategoryDto` listi
- [ ] `contentLang` on kohustuslik query parameeter
- [ ] Vastus sisaldab kõiki `category` tabeli kirjeid koos `contentLang` keelele vastava `categoryName`-iga (`category_translation` kaudu)
- [ ] Tulemus on sorteeritud `category.id` järgi kasvavalt
- [ ] Tundmatu `contentLang` väärtuse korral tagastatakse tühi list, mitte viga
- [ ] 500 Internal Server Error korral tagastatakse standardne `ApiError` vastus
- [ ] Automaattestid katavad õnnestunud päringu (et ja en keeles) ja tundmatu `contentLang` väärtuse juhtumi

## Avatud küsimused

1. **`contentLang` lubatud väärtused mockupi märgetes lahknevad.** `training-form-view-state-new-training-markmed.md` kirjeldab `contentLang: String — categoryName keel ("et"/"en"/"ru")`, aga `trainings-view-markmed.md` kirjeldab sama teenust kui `("et"/"en")`. Andmebaasis (`docs/database/3_import.sql`) on tõlked ainult `et` ja `en` keeles — vene keel esineb ainult mockupis. Käesolev task on koostatud kokkuleppe järgi kahe keelega (`et`/`en`); `ru` väärtust andmenäidistesse ei lisatud ja `contentLang="ru"` annab (nagu iga tundmatu väärtus) lihtsalt tühja listi, mitte viga. Kui vene keel on tegelikult vajalik, tuleb see andmebaasi ja mockupisse eraldi lisada.
2. **Sorteerimine — otsustatud:** `category.id` järgi kasvavalt (lühikeste valikunimekirjade ühtne reegel; sama nagu `GET /api/languages`, `GET /api/funding-types`, `GET /api/locations`).
3. **`CategoryDto` asukoht ja kuju** — kood veel ei sisalda kategooriate DTO-d ega controllerit. Otsustati (analoogia `FundingTypeDto`/`SystemLanguageDto`-ga) luua `CategoryDto` (`categoryId`, `categoryName`) kausta `controller/common/dto/`, kuna teenust kasutab kaks eri ressursipaketti (training-form ja trainings). Kui edaspidi luuakse eraldi `controller/category/` pakett, tuleks DTO asukoht üle vaadata.
