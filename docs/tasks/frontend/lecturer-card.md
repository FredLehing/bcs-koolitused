# Jagatud koolitaja kaart (pilt, nimi, ametinimetus, lühikirjeldus)

**Komponent:** `components/common/LecturerCard.vue` (uus) ja `components/common/LecturerAvatar.vue` (uus)

**Kasutavad vaated:** `TrainingView.vue` (`/training`, parem veerg "Koolitaja") — ainus kasutaja (admini kalender näitab koolitajaid ainult nimedena)

**Roll:** Kõik rollid (`/training` on avalik), admin (kalender)

**Vaste mockupis:** `/training` läbimängu veel pole; kaardi välimus Balsamiq AI käsus (`admin-lecturers-view-skeemid.md`, jaotis 10 "LecturerCard") ja avaliku koolitajate lehe läbimängus (`lecturers-view-labimang.html`, sama pilt / kohatäide)

> Mockupi pilt lisatakse hiljem (Balsamiq AI käsk: `admin-lecturers-view-skeemid.md`, jaotis 10 "LecturerCard").

Otsus (`admin-lecturers-view-skeemid.md`, "Koolitaja kaart"): **komponent laeb andmed ise** `lecturerId` järgi — nii ei dubleeri kolm vaadet sama päringut.

> **Uuendus (2026-09-30):** pilt tuleb pilditeenusest: `LecturerAvatar` propsid on `lecturerId`, `photoVersion`, `size`, `shape` (`<img src="{API}/lecturer/{lecturerId}/photo?v={photoVersion}">`, `null` → kohatäide) — mitte Base64. Koolitusel võib olla **mitu koolitajat** (`training-lecturers-multiple.md`): `/training` paremas veerus on jaotis "Koolitajad" ja iga `TrainingDto.lecturers` elemendi kohta üks `LecturerCard`. Admini kalender näitab koolitajaid ainult nimedena.

## Kasutajavoog

Külastaja avab koolituse lehe `/training`. Parema veeru kohatäite "Koolitaja" asemel näeb ta koolituse vaikimisi koolitaja kaarti: ümar pilt, nimi, ametinimetus ja lühikirjeldus kasutajaliidese keeles. Kui koolitusel pole vaikimisi koolitajat või koolitaja on kustutatud, kaarti (ja jaotist "Koolitaja") ei kuvata.

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Pilt | `LecturerAvatar.vue` | `<img src="data:{photoContentType};base64,{photo}">`, ümar; pildi puudumisel kohatäide `PhUserCircle`. Prop `size` (kaardil nt 56 px, vormis 80 px) |
| Nimi | tekst (bold) | `fullName` |
| Ametinimetus | tekst (väiksem, hall) | `title` |
| Lühikirjeldus | tekst | `shortDescription` |

## Käitumine ja valideerimine

1. Prop `lecturerId` (Number või `null`). `null` → komponent ei renderda midagi ega tee päringut.
2. `lecturerId` olemas → `GET /api/lecturer-summary/{lecturerId}?contentLang={UI keel}`; päring uuesti, kui `lecturerId` või `contentLang` (Pinia `languageStore`) muutub.
3. 404 (kustutatud või olematu koolitaja) → kaarti ei kuvata, **üldisele veavaatele ei suunata**; emit `event-lecturer-not-found` (vaade võib jaotise pealkirja peita). Muu viga → samuti ei kuvata (kaart on lisainfo, mitte vaate põhiosa).
4. `TrainingView.vue`: parema veeru `sidebarSections`-ist "lecturer" asendub kohatäide `LecturerCard`-iga (`lecturerId` = koolituse `defaultLecturerId` — `GET /api/training/{trainingId}` vastusest või olemasolevast koolituse andmete päringust; täpsusta, kust `TrainingView` selle praegu saab). Ilma vaikimisi koolitajata jaotis peidetakse.

## API kutsed

### `GET /api/lecturer-summary/{lecturerId}`

**Backend task:** `docs/tasks/backend/GET-api-lecturer-summary-lecturerId.md`

`LecturerSummaryDto.java` — response (200):

```json
{
  "lecturerId": 1,
  "fullName": "Rain Tüür",
  "title": "Lektor/konsultant",
  "shortDescription": "Tarkvaraarendus, Java, HTML, CSS, JavaScript, SQL, Git, Spring Boot, REST API, PostgreSQL, JPA (Hibernate), MapStruct, JUnit, Swagger, Gradle, Confluence, Jira. Vali Tarkvaraarendus! programm.",
  "photo": "iVBORw0KGgoAAAANSUhEUgAAACAAAAAg...",
  "photoContentType": "image/png"
}
```

**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 404 | `PRIMARY_KEY_NOT_FOUND` | "Ei leidnud primary keyd 'lecturerId' väärtusega: 123" | kaarti ei kuvata, emit `event-lecturer-not-found` |

## Mock-vastused

Kuni backend valmib: `MockDatabase.getLecturerSummary(lecturerId, contentLang)` koolitajate seed'i põhjal (`lecturer-db-changes.md`); Rain Tüüri pilt seed'i Base64-st.

## Komponendid ja failistruktuur

| Fail | Uus / muudetakse | Sisu |
|---|---|---|
| `components/common/LecturerCard.vue` | uus | Prop `lecturerId`; päring, olek, kuvamine; emit `event-lecturer-not-found` |
| `components/common/LecturerAvatar.vue` | uus | Propsid `photo`, `contentType`, `size`; kasutab ka `LecturerFormView` (`PhotoUpload`) |
| `api-services/LecturerService.js` | muudetakse | + `sendGetLecturerSummaryRequest(lecturerId, contentLang)` |
| `views/TrainingView.vue` | muudetakse | Parema veeru "Koolitaja" kohatäide → `LecturerCard` |
| `locales/*.json` | vajadusel | kohatäite tekstid |

## Vastuvõtu kriteeriumid

- [ ] `LecturerCard` kuvab pildi, nime, ametinimetuse ja lühikirjelduse kasutajaliidese keeles
- [ ] Pildita koolitajal kohatäite ikoon
- [ ] `lecturerId = null` → midagi ei kuvata ega päringut ei tehta
- [ ] Keele või `lecturerId` muutumisel laaditakse kaart uuesti
- [ ] 404 → kaarti ei kuvata, veavaatele ei suunata
- [ ] `/training` paremas veerus on vaikimisi koolitaja kaart; ilma koolitajata jaotis peidus
