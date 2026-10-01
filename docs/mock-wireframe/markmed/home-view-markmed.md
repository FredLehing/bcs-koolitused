# HomeView.vue — märkmed

Avaleht: otsing, galerii, kaks suunavat kaarti, järgmised toimumiskorrad, "Kliendid meist" ja turunduslause. Galerii (`HomeGallery.vue`) ja "Kliendid meist" (`HomeTestimonials.vue`) on master'is olemas (tiimikaaslase töö). Otsused ja skeemid: `docs/mock-wireframe/loo-mock-vaade/home-view/home-view-skeemid.md`. Interaktiivne läbimäng: `home-view-labimang.html`. Uue teenuse DTO on ettepanek.

## Vaate märkmed

```text
Roll: Kõik rollid (sh külastajad, sisselogimist ei nõuta; admin näeb kaartidel pliiatsit)
Failinimi: HomeView.vue
Frontend rada: /

Vaatega seotud lisainfo:
Järjestus: pealkiri ja otsing → galerii (HomeGallery.vue) → kaks suunavat kaarti → "Meie järgmised 5 koolitust" → "Kliendid meist" (HomeTestimonials.vue) → turunduslause.
Otsing nagu praegu ("Otsi"/Enter → /trainings?searchText=, × ja Esc tühjendavad). Galerii: Bootstrapi karussell (pildid kaustast src/assets/images/gallery/, nooled ja indikaatorid, automaatset kerimist pole).
Galerii all kaks kõrvuti kaarti (kitsal ekraanil üksteise all), kogu kaart on link: "Meie koolitused" (raamatute ikoon, "Vaata kõiki meie koolitusi ja nende sisu.", "Vaata koolitusi →") → /trainings ja "Koolituste kalender" (kalendri ikoon, "Vali sobiv toimumisaeg ja registreeru.", "Vaata kalendrit →") → /courses.
Pealkiri "Meie järgmised 5 koolitust" ja kuni 5 kaarti CourseCard.vue (sama mis /courses; "Vaata lähemalt" → /course?courseId={id}). Ainult avatud (mitte täis) tulevased toimumiskorrad, esile tõstetud eespool, edasi alguse järgi; filtreid ja leheküljestust pole. Kui toimumiskordi pole (või päring ebaõnnestub), on see plokk peidetud.
Pärast "Kliendid meist" (kolm tagasiside kaarti, tekstid tõlkefailist) lõpus lause "Leia endale sobiv koolitus — uusi toimumiskordi lisandub pidevalt." ja nupp "Vaata kõiki toimuvaid koolitusi →" → /courses. Keele vahetusel laaditakse toimumiskorrad uuesti.
```

## API märkmed — GET /api/next-courses

```text
API: GET /api/next-courses

Query parameetrid:
contentLang: String — tõlgitud väljade keel ("et"/"en"), kohustuslik
limit: Integer — mitu toimumiskorda (1–20, vaikimisi 5)

Response (200):
PublicCourseSummaryItemDto.java (massiiv)
[
  {
    "courseId": 1,
    "trainingId": 1,
    "title": "Java algkursus",
    "shortDescription": "Java programmeerimise alused algajatele.",
    "categoryName": "Programmeerimine",
    "trainingLanguageFlagIconCode": "fi-ee",
    "startDate": "2026-10-05",
    "endDate": "2026-10-09",
    "numberOfDays": 5,
    "numberOfAcademicHours": 40,
    "price": 490,
    "status": "O",
    "isPromoted": true,
    "isOnSite": true,
    "isOnline": false,
    "lecturerNames": "Rain Tüür, Meelis Teern",
    "fundingTypes": [{ "fundingTypeId": 1, "fundingTypeName": "Töötukassa" }]
  }
]

Loogika:
View public_course_summary (publitseeritud koolitus, staatus O/F, algus täna või hiljem, ainult contentLang tõlkega), täis (F) välja jäetud. Järjestus is_promoted DESC, start_date, course_id; esimesed limit rida. Tühi massiiv, kui toimumiskordi pole.

Vead:
400 INCORRECT_INPUT — contentLang puudub või limit väljaspool 1–20
```
