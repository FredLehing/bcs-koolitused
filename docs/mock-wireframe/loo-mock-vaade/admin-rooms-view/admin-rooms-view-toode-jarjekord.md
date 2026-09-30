# Koolitusruumide haldus — tööde järjekord

`AdminRoomsView.vue`, `RoomFormView.vue`. Katusharu `feature/RAIN-rooms`.

Järjekord: **andmebaas ja entity'd → kustutatud ruum teistes teenustes → nimekirja teenused → vormi teenused → frontend**.

Allikad: skeemid `admin-rooms-view-skeemid.md`, märkmed `docs/mock-wireframe/markmed/admin-rooms-view-markmed.md` ja `room-form-view-markmed.md`, läbimäng `admin-rooms-view-labimang.html`.

## 0. Eeltöö

| # | Töö | Taskifail | Märkus |
|---|---|---|---|
| 0.1 | DDL + seed: `room.status` A/D, auditiveerud, view `admin_room_summary`; `RoomStatus`, `Room`, `AdminRoomSummary` | `docs/tasks/backend/room-db-changes.md` | kõik järgmised sõltuvad sellest; andmebaasi kontrollib kasutaja |

## 1. Etapp — backend

| # | Teenus / töö | Taskifail | Keerukus |
|---|---|---|---|
| 1.1 | Kustutatud ruum: `GET /api/rooms` ainult aktiivsed, `getValidActiveRoomBy`, toimumiskorra ruumi kontroll, `CourseDto.roomName` | `docs/tasks/backend/room-deleted-status.md` | lihtne |
| 1.2 | `GET /api/admin-rooms` | `docs/tasks/backend/GET-api-admin-rooms.md` | lihtne |
| 1.3 | `DELETE /api/room/{roomId}` | `docs/tasks/backend/DELETE-api-room-roomId.md` | lihtne |
| 1.4 | `PUT /api/room/{roomId}/restore` | `docs/tasks/backend/PUT-api-room-roomId-restore.md` | lihtne |
| 1.5 | `GET /api/room/{roomId}` | `docs/tasks/backend/GET-api-room-roomId.md` | lihtne |
| 1.6 | `POST /api/room` | `docs/tasks/backend/POST-api-room.md` | lihtne |
| 1.7 | `PUT /api/room/{roomId}` | `docs/tasks/backend/PUT-api-room-roomId.md` | lihtne |

## 2. Etapp — frontend

| # | Töö | Taskifail |
|---|---|---|
| 2.1 | `AdminRoomsView.vue` + kustutamise/taastamise nupud + navbar + router | `docs/tasks/frontend/admin-rooms-view.md` |
| 2.2 | `RoomFormView.vue` | `docs/tasks/frontend/room-form-view.md` |
| 2.3 | `CourseFormView`: kustutatud ruum rippmenüüs | `docs/tasks/frontend/course-form-deleted-room.md` |

## Lahtised küsimused

- Mahutavus, asukoht või varustus — hiljem eraldi taskiga, kui vaja.
