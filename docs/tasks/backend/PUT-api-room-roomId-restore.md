# Koolitusruumi taastamine

**Teenus:** `PUT /api/room/{roomId}/restore`

**Kasutav vaade:** `AdminRoomsView.vue` (`/admin-rooms`, lüliti "Näita kustutatud")

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-rooms-view/admin-rooms-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/admin-rooms-view-markmed.md`.

Eeldab taski `room-db-changes.md`. Sobib teha koos `DELETE-api-room-roomId.md`-ga.

## Sisend

**Path variable:** `roomId` (Integer). Body't pole.

## Väljund

**Response (200 OK):** tühi vastus.

## Eesmärk

Tegevusteenus: kustutatud ruum (`status = "D"`) muutub aktiivseks (`"A"`) ja seda saab jälle toimumiskordadele valida (`RoomRestoreButton.vue`, kinnitusega). Aktiivse ruumi korral midagi ei muutu.

## Seotud andmebaasi tabelid

`room` (`status`, `updated_at`). Näidisandmed: Bremeni (2).

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `roomId` ei leidu | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'roomId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |

## Vastuvõtu kriteeriumid

- [ ] `PUT /api/room/{roomId}/restore` määrab `status = "A"`
- [ ] Aktiivne ruum → 200, ei muutu; olematu → 404
- [ ] Teenusel on automaattestid
