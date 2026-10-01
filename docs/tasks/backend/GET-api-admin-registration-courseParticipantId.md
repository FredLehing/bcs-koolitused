# Ühe registreerumise admini vaade

**Teenus:** `GET /api/admin-registration/{courseParticipantId}`

**Kasutav vaade:** `AdminRegistrationView.vue` (`/admin-registration?courseParticipantId={id}`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-registrations-view/admin-registrations-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/admin-registration-view-markmed.md`.

Eeldab taski `registration-db-changes.md`.

## Sisend

**Path variable:** `courseParticipantId` (Integer).

**Query parameeter:** `contentLang` (String) — koolituse nime keel (`et` / `en`).

## Väljund

**Response (200 OK):** `AdminRegistrationDto`

```json
{
  "courseParticipantId": 1,
  "status": "R",
  "hasPaid": true,
  "requiresLaptop": true,
  "notes": "Registreerus veebilehe kaudu.",
  "adminNotes": null,
  "createdAt": "2026-09-10T09:00:00Z",
  "updatedAt": "2026-09-10T09:00:00Z",
  "participantName": "Anna Saar",
  "email": "anna.saar@example.com",
  "phone": "+37256789012",
  "accountEmail": "kasutaja@vali-it.ee",
  "courseId": 1,
  "trainingTitle": "Java algkursus",
  "courseStartDate": "2026-10-05",
  "courseEndDate": "2026-10-09",
  "courseStatus": "O",
  "isPast": false
}
```

## Eesmärk

Admin näeb ühe registreerumise kõiki andmeid: registreerumise väljad (vormi algväärtused), osaleja kontakt (ainult lugemiseks) ja toimumiskord. `notes` = osaleja enda lisainfo (tühi string, kui puudub), `adminNotes` = admini märkmed (`null`, kui puudub). `accountEmail` = kasutajakonto e-post (frontend näitab seda ainult siis, kui see erineb `email`-ist).

## Seotud andmebaasi tabelid

View `admin_registration_summary` (`course_participant_id` + `contentLang`). Kustutatud toimumiskorra registreerumine on samuti nähtav (nimekirjas seda pole, aga otselink töötab).

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Olematu `courseParticipantId` | 404 | `PRIMARY_KEY_NOT_FOUND` — "Ei leidnud primary keyd 'courseParticipantId' väärtusega: 123" |
| Tundmatu `contentLang` | 404 | sama mis olematu |

## Vastuvõtu kriteeriumid

- [ ] Kõik väljad view'st, koolituse nimi `contentLang` keeles, puudumisel põhikeeles
- [ ] Olematu registreerumine → 404
- [ ] Teenusel on automaattestid
