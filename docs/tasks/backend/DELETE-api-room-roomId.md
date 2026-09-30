# Koolitusruumi kustutamine (soft delete)

**Teenus:** `DELETE /api/room/{roomId}`

**Kasutav vaade:** `AdminRoomsView.vue` (`/admin-rooms`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-rooms-view/admin-rooms-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/admin-rooms-view-markmed.md`.

Eeldab taski `room-db-changes.md`. Eeskuju: `DELETE-api-lecturer-lecturerId.md`.

## Sisend

**Path variable:** `roomId` (Integer). Query parameetreid ja body't pole.

## Väljund

**Response (200 OK):** tühi vastus.

## Eesmärk

Admin kustutab ruumi prügikasti ikooniga (`RoomDeleteButton.vue`, kinnitus "Kas soovid ruumi „Megede“ kustutada?"). Soft delete: `room.status = "D"`. Toimumiskordade seosed jäävad (kalendris kuvatakse nime edasi). Kustutatud ruumi ei pakuta toimumiskorra vormis (`room-deleted-status.md`). Ruumi, kus on **tulevasi toimumiskordi**, kustutada ei saa.

## Seotud andmebaasi tabelid

`room` (`status`, `updated_at`), `course` (ainult lugemine): `course.room_id = :roomId AND end_date >= täna AND status NOT IN ('D', 'X')` — nt `CourseRepository.countUpcomingCoursesBy(roomId, today, ignoredStatuses)`.

Näidisandmed: Assauwe (1) — 2 tulevast → 403; Megede (6) — kustutamine õnnestub.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `roomId` ei leidu | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'roomId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Ruumis on tulevasi toimumiskordi | 403 Forbidden | `{ "message": "Ruumis on tulevasi toimumiskordi, vali neile enne teine ruum", "errorCode": "ROOM_HAS_UPCOMING_COURSES" }` |

Juba kustutatud ruumi kustutamine → 200, midagi ei muutu. Uus väärtus `Error` enumisse: `ROOM_HAS_UPCOMING_COURSES`.

## Vastuvõtu kriteeriumid

- [ ] `DELETE /api/room/{roomId}` määrab `status = "D"`, `updated_at` uueneb
- [ ] Tulevaste toimumiskordadega ruum → 403; möödunud, tühistatud ja kustutatud toimumiskorrad ei takista
- [ ] Juba kustutatud → 200; olematu → 404
- [ ] Teenusel on automaattestid
