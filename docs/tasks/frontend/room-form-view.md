# Koolitusruumi vorm: lisamine ja muutmine

**Vaade:** `RoomFormView.vue`, route `/room-form` (nimi `roomFormRoute`)

**Roll:** Admin

**Vaste mockupis:** interaktiivne läbimäng `docs/mock-wireframe/loo-mock-vaade/admin-rooms-view/admin-rooms-view-labimang.html` (artifact https://claude.ai/artifact/AvYy4K8zEhuG4JvhbV6uv6)

> Mockupi pilt lisatakse hiljem (Balsamiq AI käsk: `admin-rooms-view-skeemid.md`, jaotis 8).

Taustaks: märkmed `docs/mock-wireframe/markmed/room-form-view-markmed.md`, skeemid `admin-rooms-view-skeemid.md`. Eeskuju: `CourseFormView.vue` (kaks olekut URL-ist, suunamine eduteatega).

## Kasutajavoog

Admin avab vormi nimekirja nupust "+ Lisa uus ruum" (olek A) või rea pliiatsiga (olek B). Sisestab nime ja vajutab "Lisa" / "Salvesta" → suunatakse `/admin-rooms` lehele eduteatega.

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Pealkiri | h1 | A: "Lisa uus ruum", B: "Muuda ruumi" |
| "Koolitusruumid" | nupp (secondary), pealkirja real | → `/admin-rooms` ilma salvestamata |
| Kaart "Ruumi andmed" | fieldset | |
| "Nimi *" | tekstiväli, `maxlength="255"` | B-s eeltäidetud |
| Veateade | `AlertDanger` | frontendi kontroll või backendi teade |
| "Lisa" / "Salvesta" | nupp | A: `POST /api/room`, B: `PUT /api/room/{roomId}`; saatmise ajal keelatud |

## Käitumine ja valideerimine

- Olek URL-ist: `roomId` puudub → A, olemas → B (`GET /api/room/{roomId}`; 404 → veavaade).
- Enne saatmist: nimi täidetud (`trim`) → muidu "Täida kõik kohustuslikud väljad".
- 403 `ROOM_NAME_EXISTS` ja 400 → backendi `message` `AlertDanger`-is; 404 (vahepeal kustutatud) → veavaade.
- Õnnestumisel `NavigationService.navigateToAdminRoomsView(successMessage)` ("Ruum lisatud" / "Salvestatud").
- Ainult adminile.

## API kutsed

- `GET /api/room/{roomId}` → `{ roomId, roomName }`
- `POST /api/room` `{ userId, roomName }` → 200
- `PUT /api/room/{roomId}` `{ roomName }` → 200

## Komponendid ja failistruktuur

- `views/RoomFormView.vue` (uus); `RoomService.js`, `NavigationService.js`, `router/index.js`, locale'id (muudetakse)

## Vastuvõtu kriteeriumid

- [ ] Uue ruumi lisamine ja muutmine töötavad, suunatakse nimekirja eduteatega
- [ ] Tühja nime ja korduva nime teated
- [ ] Olematu / kustutatud `roomId` → veavaade
- [ ] Tekstid et/en
