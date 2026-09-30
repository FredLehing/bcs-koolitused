# Toimumiskorra vorm: kustutatud ruum rippmenüüs

**Vaade:** `CourseFormView.vue`, route `/course-form`

**Roll:** Admin

Taustaks: `docs/mock-wireframe/loo-mock-vaade/admin-rooms-view/admin-rooms-view-skeemid.md`, jaotis "Kustutatud ruum teistes teenustes"; backend `docs/tasks/backend/room-deleted-status.md`.

## Kasutajavoog

`GET /api/rooms` tagastab edaspidi ainult aktiivsed ruumid. Kui toimumiskorra praegune ruum on kustutatud, peab see rippmenüüs alles jääma, et vormi saaks muutmata ruumiga salvestada.

## Käitumine

- `CourseDto` sisaldab `roomName`. Kui `course.roomId` ei ole `rooms` nimekirjas, lisatakse rippmenüüsse valik "{roomName} (kustutatud)" (i18n `courseForm.roomDeleted`).
- Kui admin valib teise ruumi, jääb kustutatud ruumi valik nimekirja (saab tagasi valida kuni lehelt lahkumiseni).
- Salvestamisel 404 `'roomId'` (ruum kustutati vahepeal) → backendi teade `AlertDanger`-is (nagu `'lecturerId'`).

## Vastuvõtu kriteeriumid

- [ ] Kustutatud praegune ruum on rippmenüüs märgisega "(kustutatud)" ja valitud
- [ ] Uue toimumiskorra vormis kustutatud ruume pole
- [ ] 404 `'roomId'` → teade vormis
