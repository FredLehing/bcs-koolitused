# Uue koolitusruumi lisamine

**Teenus:** `POST /api/room`

**Kasutav vaade:** `RoomFormView.vue` (`/room-form`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-rooms-view/admin-rooms-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/room-form-view-markmed.md`.

Eeldab taski `room-db-changes.md`.

## Sisend

**Request body:** `RoomCreateRequestDto`

```json
{
  "userId": 1,
  "roomName": "Tallinna saal"
}
```

| Väli | Tüüp | Reegel |
|---|---|---|
| `userId` | Integer | kohustuslik (`@NotNull`) — `room.created_by` |
| `roomName` | String | kohustuslik (`@NotBlank`), kuni 255 märki (`@Size`) |

## Väljund

**Response (200 OK):** tühi vastus.

## Eesmärk

Admin lisab uue ruumi (`status = "A"`). Nimi salvestatakse tühikuteta alguses ja lõpus (`trim`). Pärast lisamist suunab frontend `/admin-rooms` lehele eduteatega "Ruum lisatud".

## Seotud andmebaasi tabelid

`room`. Nime unikaalsus: `RoomRepository.existsByNameIgnoreCase(name)` (ka kustutatud ruumide hulgas).

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Sama nimega ruum on olemas (tõstutundetu) | 403 Forbidden | `{ "message": "Sellise nimega ruum on juba olemas", "errorCode": "ROOM_NAME_EXISTS" }` |
| `userId` ei leidu | 404 Not Found | `PRIMARY_KEY_NOT_FOUND ('userId')` |
| Nimi puudub või on liiga pikk | 400 Bad Request | `INCORRECT_INPUT` |

Uus väärtus `Error` enumisse: `ROOM_NAME_EXISTS("Sellise nimega ruum on juba olemas")`.

## Vastuvõtu kriteeriumid

- [ ] Endpoint `POST /api/room` loob ruumi (`status = "A"`, `created_by`, auditiveerud)
- [ ] Korduv nimi (tõstutundetu) → 403 `ROOM_NAME_EXISTS`
- [ ] Valideerimine → 400 `INCORRECT_INPUT`
- [ ] Teenusel on automaattestid
