# Admini koolitusruumide nimekiri

**Teenus:** `GET /api/admin-rooms`

**Kasutav vaade:** `AdminRoomsView.vue` (`/admin-rooms`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-rooms-view/admin-rooms-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/admin-rooms-view-markmed.md`.

Eeldab taski `room-db-changes.md` (view `admin_room_summary`).

## Sisend

**Query parameetrid:**

| Nimi | Tüüp | Kirjeldus |
|---|---|---|
| `includeDeleted` | Boolean | `true` = ka kustutatud ruumid (valikuline, vaikimisi `false`) |

## Väljund

**Response (200 OK):** `AdminRoomSummaryDto` list

```json
[
  {
    "roomId": 1,
    "roomName": "Assauwe",
    "status": "A",
    "upcomingCourseCount": 2,
    "courseCount": 3,
    "updatedAt": "2026-07-15T06:00:00Z"
  },
  ...
]
```

## Eesmärk

Tabel admini koolitusruumide lehel. Otsing ja sorteerimine toimuvad frontendis, backend sorteerib nime järgi (võrdsete nimede korral `roomId` järgi).

## Seotud andmebaasi tabelid

### admin_room_summary (view, uus)

Vt `room-db-changes.md`. `upcomingCourseCount` = toimumiskorrad selles ruumis, `end_date >= täna`, `status NOT IN ('D', 'X')` — kui > 0, on prügikast keelatud. `courseCount` = kõik toimumiskorrad selles ruumis, `status <> 'D'`.

Näidisandmetes (ilma `includeDeleted`-ta) 5 ruumi; `includeDeleted=true` → 6 (Bremeni `status = "D"`).

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body |

## Vastuvõtu kriteeriumid

- [ ] Endpoint `GET /api/admin-rooms` on olemas, `includeDeleted` vaikimisi `false`
- [ ] Vaikimisi ainult aktiivsed, `includeDeleted=true` → ka kustutatud
- [ ] Sorteeritud nime järgi
- [ ] Arvud vastavad view-le
- [ ] Teenusel on automaattestid
