# Admini koolitusruumide nimekiri: otsing, sorteerimine, kustutamine ja taastamine

**Vaade:** `AdminRoomsView.vue`, route `/admin-rooms` (nimi `adminRoomsRoute`)

**Roll:** Admin

**Vaste mockupis:** interaktiivne läbimäng `docs/mock-wireframe/loo-mock-vaade/admin-rooms-view/admin-rooms-view-labimang.html` (artifact https://claude.ai/artifact/AvYy4K8zEhuG4JvhbV6uv6)

> Mockupi pilt lisatakse hiljem (Balsamiq AI käsk: `admin-rooms-view-skeemid.md`, jaotis 8).

Taustaks: märkmed `docs/mock-wireframe/markmed/admin-rooms-view-markmed.md`, otsused ja skeemid `docs/mock-wireframe/loo-mock-vaade/admin-rooms-view/admin-rooms-view-skeemid.md`, tööde järjekord `admin-rooms-view-toode-jarjekord.md` (samas kaustas). Vorm on eraldi taskis `room-form-view.md`. Eeskuju: `AdminLecturersView.vue`.

## Kasutajavoog

Admin avab navbari menüüst "Admin" → "Koolitusruumid" tabeli aktiivsete ruumidega nime järgi. Otsinguväli filtreerib trükkimise ajal, veerupäised sorteerivad. Pliiatsiga avaneb ruumi vorm, prügikastiga saab ruumi kustutada (kui seal pole tulevasi toimumiskordi). Lüliti "Näita kustutatud" toob nähtavale kustutatud ruumid, mida saab taastada. Pealkirja real on nupp "+ Lisa uus ruum".

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Menüülink "Koolitusruumid" | navbar, menüü "Admin" | Koolitajate linkide järel eraldaja, siis see link → `/admin-rooms` (i18n `navbar.manageRooms`, en "Training rooms") |
| Pealkiri "Koolitusruumid" | h1 | |
| "+ Lisa uus ruum" | nupp (primary) | → `/room-form` |
| "Otsi nime järgi…" | otsinguväli | Filtreerib **frontendis** (`roomName` sisaldab, tõstutundetu) |
| "Näita kustutatud" | lüliti, vaikimisi väljas | Muutmisel uus päring `includeDeleted` |
| Tabel | Bootstrap tabel | Nimi \| Tulevasi toimumiskordi \| Toimumiskordi kokku \| Uuendatud \| Tegevused |
| Veerupäised | `SortableColumnHeader` | Kõik peale "Tegevused": 1. klõps kasvav, 2. kahanev, 3. vaikimisi (nimi) |
| Uuendatud | tekst | `updatedAt` → `30/09/2026` |
| Muuda | pliiatsi ikoon | → `/room-form?roomId={id}` |
| Kustuta | `RoomDeleteButton.vue` | Kinnitus "Kas soovid ruumi „Megede“ kustutada?"; `upcomingCourseCount > 0` → keelatud, tooltip "Ruumis on tulevasi toimumiskordi — vali neile enne teine ruum" |
| Kustutatud rida | tuhm rida + märgis "Kustutatud" | Ainult `RoomRestoreButton.vue` ("Taasta", kinnitusega) |
| Teated | `InlineAlerts` | "Ruum kustutatud", "Ruum taastatud", backendi 403 teade; vormist tulles `history.state.successMessage` |
| "Kokku N ruumi" | tekst tabeli all | filtreeritud ridade arv |

## Käitumine ja valideerimine

- Ainult adminile (`SessionStorageService.userIsAdmin()`), muidu `NotAuthorizedView`.
- Nimekiri ei sõltu keelest.
- 403 `ROOM_HAS_UPCOMING_COURSES` kustutamisel → teade + nimekiri uuesti; muu viga → veavaade.

## API kutsed

- `GET /api/admin-rooms?includeDeleted=` → `[{ roomId, roomName, status, upcomingCourseCount, courseCount, updatedAt }]`
- `DELETE /api/room/{roomId}` → 200 / 403 `ROOM_HAS_UPCOMING_COURSES`
- `PUT /api/room/{roomId}/restore` → 200

Vt märkmed `admin-rooms-view-markmed.md`.

## Komponendid ja failistruktuur

- `views/AdminRoomsView.vue` (uus)
- `components/common/RoomDeleteButton.vue`, `RoomRestoreButton.vue` (uued)
- `api-services/RoomService.js`, `services/NavigationService.js`, `router/index.js`, `App.vue`, `locales/et.json`, `en.json` (muudetakse)

## Vastuvõtu kriteeriumid

- [ ] Menüüs "Admin" on eraldaja ja link "Koolitusruumid"
- [ ] Tabel, otsing, sorteerimine ja "Kokku N ruumi" töötavad
- [ ] Kustutamine kinnitusega; tulevaste toimumiskordadega ruumil prügikast keelatud
- [ ] "Näita kustutatud" + taastamine
- [ ] Vormist tulles eduteade
- [ ] Tekstid et/en
