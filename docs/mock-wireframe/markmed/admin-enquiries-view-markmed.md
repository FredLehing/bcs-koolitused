# AdminEnquiriesView.vue — märkmed

Admini huviliste päringute (koolituste päringute) nimekiri. Otsused, andmebaasi ettepanek ja skeemid: `docs/mock-wireframe/loo-mock-vaade/admin-enquiries-view/admin-enquiries-view-skeemid.md`. Interaktiivne läbimäng: `admin-enquiries-view-labimang.html`. Uute teenuste DTO-d on ettepanek.

## Vaate märkmed

```text
Roll: Admin
Failinimi: AdminEnquiriesView.vue
Frontend rada: /admin-enquiries

Vaatega seotud lisainfo:
Ülal vahelehed (AdminTabs.vue): Koolituste päringud | Registreerumised | Koolitused | Koolituste kalender | Koolitajad | Koolitusruumid | Kontod — samad lingid ja järjekord mis menüüs "Admin", selle vaate vaheleht on aktiivne. Kitsal ekraanil on vahelehed ühel keritaval real, aktiivne keritakse keskele.
Avaneb navbari menüüst "Admin" → "Koolituste päringud". Tabelis Saabunud | Nimi | E-post | Ettevõte | Koolitus | Toimumiskord | Staatus | Tegevused; uusimad üleval, uue päringu rida paksus kirjas. Koolituse nimi on kasutajaliidese keeles (puudumisel põhikeeles), keele vahetusel laaditakse nimekiri uuesti. Toimumiskord "—", kui päring on koolituse kohta üldiselt.
Lüliti "Näita ka käsitletud" (vaikimisi väljas) → includeHandled=true. Otsinguväli filtreerib frontendis nime, e-posti, ettevõtte ja koolituse järgi; veerud Saabunud, Nimi, Koolitus ja Staatus on sorteeritavad frontendis (1. klõps kasvav, 2. kahanev, 3. vaikimisi). All "Kokku N päringut".
Silma ikoon "Vaata" → /admin-enquiry?enquiryId={id}.
```

## API märkmed — GET /api/admin-enquiries

```text
API: GET /api/admin-enquiries

Query parameetrid:
contentLang: String — koolituse nime keel ("et"/"en")
includeHandled: Boolean — true = ka käsitletud päringud (valikuline, vaikimisi false)

Response (200):
AdminEnquirySummaryDto.java
[
  {
    "enquiryId": 4,
    "createdAt": "2026-09-28T13:40:00Z",
    "fullName": "Martin Kask",
    "email": "martin.kask@example.com",
    "companyName": null,
    "trainingTitle": "Java algkursus",
    "courseStartDate": "2026-11-16",
    "courseEndDate": "2026-11-20",
    "status": "U"
  },
  ...
]

API teenuse lisainfo:
Andmed tulevad view'st admin_enquiry_summary, uusimad eespool (created_at kahanevalt). status: "U" = uus, "H" = käsitletud; ilma includeHandled=true tagastatakse ainult uued. trainingTitle on contentLang keeles, puudumisel põhikeeles. courseStartDate ja courseEndDate on null, kui päring pole toimumiskorra kohta; companyName võib olla null. Sõnumit ja telefoni nimekirjas ei tagastata. Otsing ja sorteerimine toimuvad frontendis.

Veateated: —
```
