# Koolitusruumi muutmine

**Teenus:** `PUT /api/room/{roomId}`

**Kasutav vaade:** `RoomFormView.vue` (`/room-form?roomId={id}`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-rooms-view/admin-rooms-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/room-form-view-markmed.md`.

Eeldab taske `room-db-changes.md`, `room-deleted-status.md` ja `POST-api-room.md` (`ROOM_NAME_EXISTS`).

## Sisend

**Path variable:** `roomId` (Integer).

**Request body:** `RoomUpdateRequestDto`

```json
{
  "roomName": "Hellemanni saal"
}
```

`roomName` — kohustuslik (`@NotBlank`), kuni 255 märki.

## Väljund

**Response (200 OK):** tühi vastus.

## Eesmärk

Admin muudab ruumi nime (`trim`). `updated_at` uueneb auditeerimisega. Toimumiskordadel kuvatakse edaspidi uus nimi.

## Seotud andmebaasi tabelid

`room`. Unikaalsus: `existsByNameIgnoreCaseAndIdNot(name, roomId)` — sama ruumi nime tõstu muutmine (nt "hellemanni" → "Hellemanni") on lubatud.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `roomId` ei leidu või ruum on kustutatud | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'roomId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Teisel ruumil on sama nimi | 403 Forbidden | `{ "message": "Sellise nimega ruum on juba olemas", "errorCode": "ROOM_NAME_EXISTS" }` |
| Nimi puudub või on liiga pikk | 400 Bad Request | `INCORRECT_INPUT` |

## Vastuvõtu kriteeriumid

- [ ] Endpoint `PUT /api/room/{roomId}` muudab nime
- [ ] Kustutatud / olematu → 404; teise ruumi nimi → 403; oma nimi sobib
- [ ] Teenusel on automaattestid
