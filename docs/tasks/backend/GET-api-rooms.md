# Ruumide nimekiri

> **Uuendus (2026-09-30):** `room.status` on nüüd `A` / `D` (soft delete). Teenus tagastab ainult aktiivsed ruumid ja `roomStatus` eemaldatakse — vt `room-deleted-status.md`.

**Teenus:** `GET /api/rooms`

**Kasutav vaade:** `CourseFormView.vue` (ruumi rippmenüü, esimene valik "Ruum puudub")

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-training-courses-view/admin-training-courses-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/course-form-view-markmed.md` (JSON-näited).

## Sisend

Teenusel puuduvad sisendid.

## Väljund

**Response (200 OK):** `List<RoomDto>` — `roomId`, `roomName`, `roomStatus`, sorteeritud nime järgi:

```json
[
  { "roomId": 1, "roomName": "Assauwe", "roomStatus": "VAB" },
  { "roomId": 2, "roomName": "Bremeni", "roomStatus": "KIN" },
  ...
]
```

`roomStatus` (`VAB` / `KIN`) tähendus on lahtine küsimus — praegu kuvatakse kõik ruumid.

## Eesmärk

Toimumiskorra vormis valitakse ruum.

## Seotud andmebaasi tabelid

`room` (`3_import.sql`: Assauwe, Bremeni, Eppingi, Hellemanni, Landskrone, Megede).

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

## Vastuvõtu kriteeriumid

- [ ] Endpoint tagastab kõik ruumid nime järgi
- [ ] Teenusel on automaattestid
