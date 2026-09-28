# Toimumiskohtade nimekirja päring

**Teenus:** `GET /api/locations`

**Kasutav vaade:** `TrainingFormView.vue` (`/training-form`)

![Mockup](../../mock-wireframe/pdf-images/TrainingFormView-state-new-training.png)

## Sisend

Teenusel puuduvad sisendid — ei query parameetreid ega request body't.

## Väljund

**Response (200 OK):** Kõigi toimumiskohtade nimekiri.

`LocationDto.java`:

```json
[
  {
    "locationId": 1,
    "locationName": "BCS Koolitus"
  },
  {
    "locationId": 2,
    "locationName": "Veebiõpe"
  },
  {
    "locationId": 3,
    "locationName": "Hübriidõpe"
  }
]
```

Väljade selgitused:

- `locationId` — `location.id`.
- `locationName` — `location.name`. See väli **ei ole tõlgitud** (erinevalt nt `category`/`funding_type` nimedest), seega teenusel pole `contentLang` parameetrit.

Tulemus on sorteeritud `location.id` järgi kasvavalt (lühikeste valikunimekirjade ühtne reegel).

## Eesmärk

`TrainingFormView` vaates (roll: Admin) täidab teenus "Toimumiskoht" rippmenüü, kust valitakse koolituse toimumiskoht koolituse loomisel/muutmisel. Vaade kutsub teenust vormi avamisel (nt olekus "new-training") ja laadib rippmenüü täies mahus, ilma filtreerimise või tõlkimiseta.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

### location

Toimumiskoha kirje — nimi, aadress ja veebis toimumise lipp.

```sql
CREATE TABLE location
(
    id         serial       NOT NULL,
    name       varchar(255) NOT NULL,
    address    text         NOT NULL,
    is_online  boolean      NOT NULL,
    created_at timestamp    NOT NULL,
    updated_at timestamp    NOT NULL,
    created_by int          NOT NULL,
    CONSTRAINT location_pk PRIMARY KEY (id)
);
```

Näidisandmed (`docs/database/3_import.sql`):

| id | name | address | is_online |
|---|---|---|---|
| 1 | BCS Koolitus | Aia tn 7, Tallinn | false |
| 2 | Veebiõpe | Veebipõhine koolitus (nt. Zoom, Teams) | true |
| 3 | Hübriidõpe | Koha peal + veebiõpe | true |

Veerud `address` ja `is_online` on tabelis olemas, kuid vastusesse neid ei lisata — vormi rippmenüü vajab ainult nime (otsustatud).

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Ootamatu serveri viga | 500 Internal Server Error | Standardne `ApiError` vastus |

Teenusel pole sisendeid ega mockupi veateateid (märkmes `Veateated: —`), seega muid veaolukordi pole.

## Vastuvõtu kriteeriumid

- [ ] Endpoint `GET /api/locations` on olemas
- [ ] Vastus (200 OK) tagastab kõik `location` tabeli kirjed `LocationDto` struktuuriga (`locationId`, `locationName`)
- [ ] Näidisandmete põhjal tagastatakse kolm toimumiskohta: "BCS Koolitus", "Veebiõpe", "Hübriidõpe"
- [ ] Tulemus on sorteeritud `location.id` järgi kasvavalt
- [ ] `address` ja `is_online` vastusesse ei jõua
- [ ] `locationName` väli vastab otse `location.name` veerule, ilma tõlketa (`contentLang` parameetrit pole)
- [ ] Ootamatu serveri viga (500) tagastab standardse `ApiError` vastuse
- [ ] Teenusel on automaattestid

## Avatud küsimused

1. **`LocationDto` väljad — otsustatud:** ainult `locationId` ja `locationName` (koodis klassi veel pole, luuakse uuena). `address` ja `isOnline` lisatakse alles siis, kui mõni vaade neid vajab.
2. **Tagastuse järjestus — otsustatud:** `location.id` järgi kasvavalt (lühikeste valikunimekirjade ühtne reegel).
