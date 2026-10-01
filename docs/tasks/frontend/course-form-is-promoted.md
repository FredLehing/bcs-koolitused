# Toimumiskorra vorm: lüliti "Esile tõstetud"

**Vaade:** `CourseFormView.vue` (`/course-form`)

**Roll:** Admin

**Vaste mockupis:** otsus `docs/mock-wireframe/loo-mock-vaade/courses-view/courses-view-skeemid.md` ("Üldine": esile tõstetud toimumiskord).

Eeldab backend taski `course-is-promoted.md`.

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| "Esile tõstetud" | lüliti (`form-switch`) | staatuse välja juures; uuel toimumiskorral vaikimisi väljas; muutmisel `GET /api/course/{courseId}` väärtus |

## Käitumine

- `isPromoted` saadetakse `POST /api/training/{trainingId}/course` ja `PUT /api/course/{courseId}` päringus.
- Vihje: "Esile tõstetud toimumiskorrad on koolituste kalendris eespool."

## Vastuvõtu kriteeriumid

- [ ] Lüliti salvestub ja laaditakse muutmisel
- [ ] Tekstid et/en
