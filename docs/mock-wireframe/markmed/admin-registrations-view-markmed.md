# AdminRegistrationsView.vue — märkmed

Kõigi registreerumiste (course_participant) nimekiri adminile. Otsused, andmebaasi ettepanek ja skeemid: `docs/mock-wireframe/loo-mock-vaade/admin-registrations-view/admin-registrations-view-skeemid.md`. Interaktiivne läbimäng: `admin-registrations-view-labimang.html`. Uute teenuste DTO-d on ettepanek.

## Vaate märkmed

```text
Roll: Admin
Failinimi: AdminRegistrationsView.vue
Frontend rada: /admin-registrations

Vaatega seotud lisainfo:
Ülal vahelehed (AdminTabs.vue): Koolituste päringud | Registreerumised | Koolitused | Koolituste kalender | Koolitajad | Koolitusruumid — samad lingid ja järjekord mis menüüs "Admin", selle vaate vaheleht on aktiivne. Kitsal ekraanil on vahelehed ühel keritaval real, aktiivne keritakse keskele.
Avaneb navbari menüüst "Admin" → "Registreerumised" (kohe "Koolituste päringud" järel). Pealkiri "Registreerumised". Tabelis Registreerus | Osaleja | E-post | Koolitus | Toimumisaeg | Tasunud ✓/✗ | Vajab sülearvutit ✓/✗ | Staatus (Registreerunud / Loobunud) | Tegevused. Koolituse nimi on kasutajaliidese keeles (puudumisel põhikeeles), keele vahetusel laaditakse nimekiri uuesti. Toimumisaeg on link → /admin-course?courseId={id}; toimunud toimumiskorral märgis "Toimunud". Loobunu rida on tuhmim.
Lüliti "Näita ka loobunud" (vaikimisi väljas → ainult registreerunud) → includeCancelled=true; lüliti "Näita ka toimunud" (vaikimisi väljas → ainult toimumiskorrad, mille lõpp on täna või hiljem) → includePast=true; lüliti muutmisel laaditakse nimekiri uuesti. Otsinguväli filtreerib frontendis osaleja nime, e-posti ja koolituse nime järgi. Veerud Registreerus, Osaleja, Koolitus, Toimumisaeg, Tasunud, Vajab sülearvutit ja Staatus on sorteeritavad frontendis (1. klõps kasvav, 2. kahanev, 3. vaikimisi = registreerumise aeg, uusimad eespool). Leheküljestust pole. All "Kokku N registreerumist".
Silma ikoon "Vaata" → /admin-registration?courseParticipantId={id}.
```

## API märkmed — GET /api/admin-registrations

```text
API: GET /api/admin-registrations

Query parameetrid:
contentLang: String — koolituse nime keel ("et"/"en")
includeCancelled: Boolean — true = ka loobunud (status "C") (valikuline, vaikimisi false)
includePast: Boolean — true = ka toimunud toimumiskorrad (valikuline, vaikimisi false)

Response (200):
AdminRegistrationSummaryDto.java
[
  {
    "courseParticipantId": 9,
    "registeredAt": "2026-09-28T06:00:00Z",
    "participantName": "Toomas Rebane",
    "email": "toomas.rebane@example.com",
    "courseId": 9,
    "trainingTitle": "Spring Boot veebiarendus",
    "courseStartDate": "2026-10-12",
    "courseEndDate": "2026-10-15",
    "isPast": false,
    "hasPaid": true,
    "requiresLaptop": true,
    "status": "R"
  },
  ...
]

API teenuse lisainfo:
course_participant + participant (name) + profile (email) + course (kuupäevad) + koolituse nimi contentLang keeles, puudumisel põhikeeles (sama reegel nagu admin_training_summary). Kustutatud toimumiskordade (course.status = "D") ja kustutatud koolituste registreerumisi ei tagastata. includeCancelled=false → ainult status = "R". includePast=false → ainult course.end_date >= täna; isPast = course.end_date < täna. Järjestus created_at kahanevalt (uusimad eespool). Otsing ja sorteerimine toimuvad frontendis; leheküljestust pole. Kui päring läheb keerukaks, võib lisada view admin_registration_summary (nagu admin_enquiry_summary).

Veateated: —
```
