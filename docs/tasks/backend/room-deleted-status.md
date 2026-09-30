# Kustutatud ruum teistes teenustes

**Teenus:** `GET /api/rooms` (muutub), `GET /api/course/{courseId}` (muutub), `POST /api/training/{trainingId}/course` ja `PUT /api/course/{courseId}` (ruumi kontroll)

**Kasutav vaade:** `CourseFormView.vue` (`/course-form`)

> Otsused: `docs/mock-wireframe/loo-mock-vaade/admin-rooms-view/admin-rooms-view-skeemid.md`, jaotis "Kustutatud ruum teistes teenustes". Sama loogika nagu kustutatud koolitajal (`lecturer-deleted-status.md`).

Eeldab taski `room-db-changes.md`.

## Sisend

Muutuvad olemasolevad teenused; uusi parameetreid pole.

## Väljund

**`GET /api/rooms`** — ainult aktiivsed ruumid nime järgi; `roomStatus` eemaldatakse:

```json
[
  {
    "roomId": 1,
    "roomName": "Assauwe"
  },
  ...
]
```

**`GET /api/course/{courseId}`** — `CourseDto`-sse lisandub `roomName` (`null`, kui ruumi pole), ka kustutatud ruumi korral:

```json
{
  "roomId": 2,
  "roomName": "Bremeni"
}
```

## Eesmärk

Kustutatud ruumi ei saa toimumiskorrale uueks ruumiks valida, aga olemasolev seos jääb ja toimumiskorda saab edasi salvestada (samamoodi nagu kustutatud koolitaja). Frontend näitab kustutatud praegust ruumi rippmenüüs "Bremeni (kustutatud)" (`course-form-deleted-room.md`).

## Seotud andmebaasi tabelid

`room` (`status`), `course.room_id`.

`RoomService`:
- `getValidRoomBy(roomId)` — leiab ka kustutatud ruumi (delete, restore);
- `getValidActiveRoomBy(roomId)` — kustutatud → `404 PRIMARY_KEY_NOT_FOUND ('roomId')`;
- `getValidAssignableRoomBy(roomId, linkedRoomId)` — kui `roomId` on toimumiskorra praegune ruum, sobib ka kustutatud; muidu peab olema aktiivne.

`CourseService`: `addCourse` → `getValidActiveRoomBy`; `updateCourse` → `getValidAssignableRoomBy(roomId, praegune room_id)`.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Uueks ruumiks valitud kustutatud või olematu ruum | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'roomId' väärtusega: 2", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |

## Vastuvõtu kriteeriumid

- [ ] `GET /api/rooms` tagastab ainult aktiivsed ruumid, ilma `roomStatus`-eta
- [ ] `CourseDto` sisaldab `roomName`
- [ ] Uue toimumiskorra loomine kustutatud ruumiga → 404
- [ ] Toimumiskorra muutmine: praegune kustutatud ruum jääb, uus kustutatud ruum → 404
- [ ] Teenustel on automaattestid
