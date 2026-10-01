# LecturerView.vue — märkmed

Avalik koolitaja detailvaade. Otsused, pildilahendus ja skeemid: `docs/mock-wireframe/loo-mock-vaade/lecturers-view/lecturers-view-skeemid.md`. Pilditeenuse `GET /api/lecturer/{lecturerId}/photo` märge on failis `lecturers-view-markmed.md`. Uute teenuste DTO-d on ettepanek.

## Vaate märkmed

```text
Roll: Kõik rollid (ka sisse logimata külastaja)
Failinimi: LecturerView.vue
Frontend rada: /lecturer?lecturerId={id}

Vaatega seotud lisainfo:
Avaneb "Meie koolitajad" kaardilt. Üleval link "← Kõik koolitajad" → /lecturers; kui URL-is on returnTo (nt koolitaja kaardilt /course või /training lehel), siis selle asemel "← Tagasi" → returnTo (ainult sisemine rada, algab "/"-ga, mitte "//"; muu väärtus jäetakse tähelepanuta). Vasakul suur pilt (kuni 240 px; pilditeenusest, photoVersion = null → kohatäide), paremal nimi (h1), amet, lühikirjeldus sissejuhatusena ja kirjeldus rich text'ina (RichTextContent.vue).
Plokk "Koolitused": publitseeritud koolitused, mille koolitajate hulgas ta on (training_lecturer) — nimi lingina → /training?trainingId={id}&trainingTranslationId={id}; koolitusteta plokki ei kuvata. Tekstid kasutajaliidese keeles (puudumisel põhikeeles), keele vahetusel laaditakse uuesti. Kustutatud või olematu koolitaja (404) → üldine veavaade.
```

## API märkmed — GET /api/lecturer-profile/{lecturerId}

```text
API: GET /api/lecturer-profile/{lecturerId}

Query parameetrid:
contentLang: String — tekstide ja koolituste nimede keel ("et"/"en")

Response (200):
LecturerProfileDto.java
{
  "lecturerId": 1,
  "fullName": "Rain Tüür",
  "title": "Lektor/konsultant",
  "shortDescription": "Tarkvaraarendus, Java, HTML, CSS, JavaScript, SQL, Git, Spring Boot, REST API, PostgreSQL, JPA (Hibernate), MapStruct, JUnit, Swagger, Gradle, Confluence, Jira. Vali Tarkvaraarendus! programm.",
  "description": "<p>Tarkvaraarendus, Java, HTML, CSS, JavaScript, SQL, Git, Spring Boot, REST API, PostgreSQL, JPA (Hibernate), MapStruct, JUnit, Swagger, Gradle, Confluence, Jira. Vali Tarkvaraarendus! programm.</p>",
  "photoVersion": 1784095200,
  "trainings": [
    {
      "trainingId": 1,
      "trainingTranslationId": 1,
      "title": "Java algkursus"
    },
    ...
  ]
}

API teenuse lisainfo:
Ühe aktiivse koolitaja avalik profiil. title, shortDescription ja description (HTML, HtmlSanitizer-iga puhastatud) contentLang keeles, puudumisel põhikeeles. photoVersion = lecturer_photo.updated_at epoch-sekundites või null. trainings = publitseeritud koolitused (training.status = "P"), mille koolitajate hulgas ta on (training_lecturer); title ja trainingTranslationId contentLang keeles, puudumisel põhikeeles (sama reegel nagu admin_training_summary); sorteeritud title järgi; tühi list, kui koolitusi pole.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'lecturerId' väärtusega: 123"
```


## Tagasitee — täiendatud navigatsioon

Vaade võtab vastu valikulise `returnTo` query parameetri ja kuvab lingi „← Tagasi“ (`BackLink.vue`). Avamislingid annavad kaasa lähtevaate täieliku URL-i. Tagasilingi puuduv, väline, tundmatu või iseendale osutav siht asendatakse vaate varusihtkohaga. Oleku- ja tõlkevahetus ei kaota tagasiteed. Eraldi nimega nimekirja-/kalendrinupud säilitavad oma sihtkoha. Täpne [kaardistus ja varusihtkohad](../../tasks/frontend/return-to-navigation.md) ning [skeemid](../loo-mock-vaade/return-to-navigation-skeemid.md).
