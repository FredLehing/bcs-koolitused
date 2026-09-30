# LecturersView.vue — märkmed

Avalik koolitajate nimekiri ("Meie koolitajad"). Otsused, pildilahendus ja skeemid: `docs/mock-wireframe/loo-mock-vaade/lecturers-view/lecturers-view-skeemid.md`. Uute teenuste DTO-d on ettepanek.

## Vaate märkmed

```text
Roll: Kõik rollid (ka sisse logimata külastaja)
Failinimi: LecturersView.vue
Frontend rada: /lecturers

Vaatega seotud lisainfo:
Avaneb navbari põhimenüü lingist "Meie koolitajad" (menüü "Ettevõttest" all olev "Lektorid" eemaldatakse). Pealkiri "Meie koolitajad" ja lühike sissejuhatus. Kaardid ruudustikus (4 / 3 / 2 / 1 veergu ekraani laiuse järgi): pilt, nimi, amet (title), lühikirjeldus (shortDescription); kogu kaart on link → /lecturer?lecturerId={id}.
Pilt tuleb pilditeenusest <img src="{API}/lecturer/{lecturerId}/photo?v={photoVersion}">; photoVersion = null → hall siluett (kohatäide), kaardid on ühesuguse kõrgusega. Ainult aktiivsed koolitajad nime järgi; otsingut ja leheküljestust pole. Tekstid kasutajaliidese keeles (puudumisel põhikeeles), keele vahetusel laaditakse uuesti. Tühi nimekiri: "Koolitajaid pole veel lisatud."
```

## API märkmed — GET /api/lecturer-summaries

```text
API: GET /api/lecturer-summaries

Query parameetrid:
contentLang: String — ameti ja lühikirjelduse keel ("et"/"en")

Response (200):
LecturerSummaryDto.java
[
  {
    "lecturerId": 7,
    "fullName": "Andres Liitmaa",
    "title": "Spetsialisti valdkonna lektor/konsultant",
    "shortDescription": "Microsofti ametlikud sertifitseerimiskoolitused (MOC), serverid, pilvetehnoloogiad, Microsoft Certified Trainer.",
    "photoVersion": null
  },
  {
    "lecturerId": 1,
    "fullName": "Rain Tüür",
    "title": "Lektor/konsultant",
    "shortDescription": "Tarkvaraarendus, Java, HTML, CSS, JavaScript, SQL, Git, Spring Boot, REST API, PostgreSQL, JPA (Hibernate), MapStruct, JUnit, Swagger, Gradle, Confluence, Jira. Vali Tarkvaraarendus! programm.",
    "photoVersion": 1784095200
  },
  ...
]

API teenuse lisainfo:
Ainult aktiivsed koolitajad (lecturer.status = "A"), sorteeritud fullName järgi. title ja shortDescription contentLang keeles, puudumisel põhikeeles. Sama DTO nagu GET /api/lecturer-summary/{lecturerId} (LecturerCard). Pilti ei tagastata — photoVersion = lecturer_photo.updated_at epoch-sekundites või null, kui pilti pole; frontend koostab pildi URL-i /api/lecturer/{lecturerId}/photo?v={photoVersion}. description (pikk kirjeldus) ei kuulu vastusesse. Tühi list, kui koolitajaid pole.

Veateated: —
```

## API märkmed — GET /api/lecturer/{lecturerId}/photo

```text
API: GET /api/lecturer/{lecturerId}/photo

Query parameetrid:
v: Long — photoVersion (ainult vahemälu jaoks; backend seda ei kontrolli)

Response (200): pildi baidid (image/jpeg), mitte JSON

API teenuse lisainfo:
Avalik teenus, mida kasutatakse otse <img src>-is (LecturersView, LecturerView, LecturerCard, LecturerFormView eelvaade). Content-Type = lecturer_photo.content_type (normaliseeritud pildil alati image/jpeg). Päis Cache-Control: public, max-age=31536000, immutable — URL sisaldab versiooni (v), mis muutub pildi vahetamisel. Pilt normaliseeritakse üleslaadimisel (400×400 JPEG, EXIF eemaldatud), seega on see alati väike.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'lecturerId' väärtusega: 123"
```
