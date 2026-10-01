# AdminRoomsView.vue — märkmed

Admini koolitusruumide nimekiri. Otsused, andmebaasi ettepanek ja skeemid: `docs/mock-wireframe/loo-mock-vaade/admin-rooms-view/admin-rooms-view-skeemid.md`. Interaktiivne läbimäng: `admin-rooms-view-labimang.html`. Uute teenuste DTO-d on ettepanek.

## Vaate märkmed

```text
Roll: Admin
Failinimi: AdminRoomsView.vue
Frontend rada: /admin-rooms

Vaatega seotud lisainfo:
Ülal vahelehed (AdminTabs.vue): Koolituste päringud | Registreerumised | Koolitused | Koolituste kalender | Koolitajad | Koolitusruumid — samad lingid ja järjekord mis menüüs "Admin", selle vaate vaheleht on aktiivne. Kitsal ekraanil on vahelehed ühel keritaval real, aktiivne keritakse keskele.
Avaneb navbari menüüst "Admin" → "Koolitusruumid". Pealkirja real nupp "+ Lisa uus ruum" → /room-form. Tabelis Nimi | Tulevasi toimumiskordi | Toimumiskordi kokku | Uuendatud | Tegevused. Järjestus nime järgi; veerupäistel sorteerimine frontendis (1. klõps kasvav, 2. kahanev, 3. vaikimisi). Otsinguväli "Otsi nime järgi…" filtreerib frontendis (API kutset ei tehta); all "Kokku N ruumi". Vormist tulles näidatakse eduteadet ("Ruum lisatud" / "Salvestatud").
"Muuda" → /room-form?roomId={id}. Prügikast (RoomDeleteButton.vue) küsib kinnitust ja teeb DELETE (soft delete); kui ruumis on tulevasi toimumiskordi, on prügikast keelatud ja tooltip selgitab põhjust (backend: 403 ROOM_HAS_UPCOMING_COURSES → backendi message, nimekiri uuesti).
Lüliti "Näita kustutatud" (vaikimisi väljas) → includeDeleted=true: kustutatud read on tuhmimad, märgisega "Kustutatud" ja ainult nupuga "Taasta" (kinnitus → PUT /api/room/{roomId}/restore).
```

## API märkmed — GET /api/admin-rooms

```text
API: GET /api/admin-rooms

Query parameetrid:
includeDeleted: Boolean — true = ka kustutatud ruumid (valikuline, vaikimisi false)

Response (200):
AdminRoomSummaryDto.java
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

API teenuse lisainfo:
Andmed tulevad view'st admin_room_summary, sorteeritud roomName järgi. status: "A" = aktiivne, "D" = kustutatud; ilma includeDeleted=true tagastatakse ainult aktiivsed. upcomingCourseCount = toimumiskorrad selles ruumis, end_date ≥ täna ja status ei ole "D" ega "X" — kui > 0, on prügikast keelatud. courseCount = kõik toimumiskorrad selles ruumis, status ≠ "D". Otsing ja sorteerimine toimuvad frontendis.

Veateated: —
```

## API märkmed — DELETE /api/room/{roomId}

```text
API: DELETE /api/room/{roomId}

Response (200): NONE

API teenuse lisainfo:
Soft delete: määrab room.status = "D" (RoomStatus.DELETED) ja uuendab updated_at. Toimumiskordade seosed jäävad (kalendris kuvatakse nime edasi). Keeldub, kui ruumis on tulevasi toimumiskordi (end_date ≥ täna, status ei ole "D" ega "X"). Juba kustutatud ruumi korral midagi ei muutu. Kustutatud ruumi ei pakuta toimumiskorra vormi rippmenüüs (GET /api/rooms). Frontendis teeb kutse RoomDeleteButton.vue.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'roomId' väärtusega: 123"

HTTP: 403
errorCode: ROOM_HAS_UPCOMING_COURSES
message: "Ruumis on tulevasi toimumiskordi, vali neile enne teine ruum"
```

## API märkmed — PUT /api/room/{roomId}/restore

```text
API: PUT /api/room/{roomId}/restore

Response (200): NONE

API teenuse lisainfo:
Tegevusteenus: määrab room.status = "A" (RoomStatus.ACTIVE) ja uuendab updated_at. Aktiivse ruumi korral midagi ei muutu. Frontendis teeb kutse RoomRestoreButton.vue.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'roomId' väärtusega: 123"
```
