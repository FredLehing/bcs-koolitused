# RoomFormView.vue — märkmed

Koolitusruumi lisamise ja muutmise vorm (kaks olekut: ilma `roomId`-ta uus ruum, `roomId`-ga muutmine). Otsused, andmebaasi ettepanek ja skeemid: `docs/mock-wireframe/loo-mock-vaade/admin-rooms-view/admin-rooms-view-skeemid.md`. Interaktiivne läbimäng: `admin-rooms-view-labimang.html`. Uute teenuste DTO-d on ettepanek.

## Vaate märkmed

```text
Roll: Admin
Failinimi: RoomFormView.vue
Frontend rada: /room-form?roomId={id}

Vaatega seotud lisainfo:
Ilma roomId query parameetrita on vaade uue ruumi vorm (pealkiri "Lisa uus ruum", nupp "Lisa" → POST /api/room). roomId olemasolul laaditakse ruum (GET /api/room/{roomId}), pealkiri "Muuda ruumi", nupp "Salvesta" → PUT /api/room/{roomId}. Kustutatud või olematu ruum → üldine veavaade.
Enne saatmist kontrollitakse, et "Nimi" on täidetud — muidu AlertDanger "Täida kõik kohustuslikud väljad". Backendi 403 ROOM_NAME_EXISTS → backendi message AlertDanger'is.
Pärast edukat salvestamist suunatakse /admin-rooms lehele eduteatega. Nupp "Koolitusruumid" → /admin-rooms (ilma salvestamata).
```

## API märkmed — GET /api/room/{roomId}

```text
API: GET /api/room/{roomId}

Response (200):
RoomDto.java
{
  "roomId": 4,
  "roomName": "Hellemanni"
}

API teenuse lisainfo:
Ainult aktiivne ruum; kustutatud ruum on nagu olematu (404). Sama RoomDto nagu GET /api/rooms nimekirjas (roomStatus eemaldatakse).

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'roomId' väärtusega: 123"
```

## API märkmed — POST /api/room

```text
API: POST /api/room

Request body:
RoomCreateRequestDto.java
{
  "userId": 1,
  "roomName": "Tallinna saal"
}

Response (200): NONE

API teenuse lisainfo:
Loob ruumi (status = "A", created_by = userId). roomName on kohustuslik, kuni 255 märki (@Valid → 400 INCORRECT_INPUT). Nimi peab olema unikaalne (tõstutundetult, ka kustutatud ruumide hulgas).

Veateated:
HTTP: 403
errorCode: ROOM_NAME_EXISTS
message: "Sellise nimega ruum on juba olemas"
```

## API märkmed — PUT /api/room/{roomId}

```text
API: PUT /api/room/{roomId}

Request body:
RoomUpdateRequestDto.java
{
  "roomName": "Hellemanni saal"
}

Response (200): NONE

API teenuse lisainfo:
Muudab ruumi nime ja uuendab updated_at. Ainult aktiivne ruum (kustutatud → 404). Nimi peab olema unikaalne (tõstutundetult, see ruum välja arvatud).

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'roomId' väärtusega: 123"

HTTP: 403
errorCode: ROOM_NAME_EXISTS
message: "Sellise nimega ruum on juba olemas"
```


## Tagasitee — täiendatud navigatsioon

Vaade võtab vastu valikulise `returnTo` query parameetri ja kuvab lingi „← Tagasi“ (`BackLink.vue`). Avamislingid annavad kaasa lähtevaate täieliku URL-i. Tagasilingi puuduv, väline, tundmatu või iseendale osutav siht asendatakse vaate varusihtkohaga. Oleku- ja tõlkevahetus ei kaota tagasiteed. Eraldi nimega nimekirja-/kalendrinupud säilitavad oma sihtkoha. Täpne [kaardistus ja varusihtkohad](../../tasks/frontend/return-to-navigation.md) ning [skeemid](../loo-mock-vaade/return-to-navigation-skeemid.md).
