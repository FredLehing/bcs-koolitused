# Koolitusruumi andmed (vorm)

**Teenus:** `GET /api/room/{roomId}`

**Kasutav vaade:** `RoomFormView.vue` (`/room-form?roomId={id}`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-rooms-view/admin-rooms-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/room-form-view-markmed.md`.

Eeldab taske `room-db-changes.md` ja `room-deleted-status.md` (`getValidActiveRoomBy`).

## Sisend

**Path variable:**

| Nimi | Tüüp | Kirjeldus |
|---|---|---|
| `roomId` | Integer | Ruumi ID (`room.id`) |

## Väljund

**Response (200 OK):** `RoomDto` (sama DTO nagu `GET /api/rooms` nimekirjas)

```json
{
  "roomId": 4,
  "roomName": "Hellemanni"
}
```

## Eesmärk

Ruumi vormi muutmise olekus nime eeltäitmine.

## Seotud andmebaasi tabelid

`room`. Kustutatud ruum on nagu olematu.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `roomId` ei leidu või ruum on kustutatud | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'roomId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |

## Vastuvõtu kriteeriumid

- [ ] Endpoint `GET /api/room/{roomId}` tagastab `RoomDto`
- [ ] Kustutatud või olematu ruum → 404
- [ ] Teenusel on automaattestid
