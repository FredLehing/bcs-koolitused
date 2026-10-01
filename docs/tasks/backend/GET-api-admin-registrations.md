# Admini registreerumiste nimekiri

**Teenus:** `GET /api/admin-registrations`

**Kasutav vaade:** `AdminRegistrationsView.vue` (`/admin-registrations`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-registrations-view/admin-registrations-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/admin-registrations-view-markmed.md` (API märge on sama sisuga).

Eeldab taski `registration-db-changes.md`.

## Sisend

**Query parameetrid:**

| Nimi | Tüüp | Kirjeldus |
|---|---|---|
| `contentLang` | String | koolituse nime keel (`et` / `en`) |
| `includeCancelled` | Boolean | `true` = ka loobunud (`C`) (valikuline, vaikimisi `false`) |
| `includePast` | Boolean | `true` = ka toimunud toimumiskorrad (valikuline, vaikimisi `false`) |

## Väljund

**Response (200 OK):** `AdminRegistrationSummaryDto` list

```json
[
  {
    "courseParticipantId": 9,
    "registeredAt": "2026-09-28T06:00:00Z",
    "participantName": "Toomas Rebane",
    "email": "toomas.rebane@example.com",
    "courseId": 9,
    "trainingTitle": "Spring Boot veebiarendus",
    "courseStartDate": "2026-10-12",
    "courseEndDate": "2026-10-15",
    "isPast": false,
    "hasPaid": true,
    "requiresLaptop": true,
    "status": "R"
  },
  ...
]
```

## Eesmärk

Admin näeb kõigi toimumiskordade registreerumisi ühes tabelis, uusimad eespool. Vaikimisi ainult registreerunud (`R`) ja toimumiskorrad, mis pole veel lõppenud (`end_date >= täna`). Otsing ja sorteerimine on frontendis, leheküljestust pole.

## Seotud andmebaasi tabelid

View `admin_registration_summary` (`content_language_code = contentLang`). Alati välistatakse `course_status = "D"` ja `training_status = "D"`. `includeCancelled=false` → `status = "R"`; `includePast=false` → `is_past = false`. Järjestus `created_at DESC, course_participant_id DESC`. Tundmatu `contentLang` → tühi list.

Näidisandmed (täna = 01/10/2026): vaikimisi 7 rida (9, 8, 7, 6, 4, 3, 1); `includeCancelled=true` → + rida 5; `includePast=true` → + rida 2.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Ootamatu serveripoolne viga | 500 | Standardne vea response body |

## Vastuvõtu kriteeriumid

- [ ] Endpoint olemas, `includeCancelled` ja `includePast` vaikimisi `false`
- [ ] Kustutatud toimumiskordade ja koolituste registreerumisi ei tagastata
- [ ] Uusimad eespool; koolituse nimi `contentLang` keeles, puudumisel põhikeeles
- [ ] Teenusel on automaattestid
