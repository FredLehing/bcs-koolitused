# CourseView.vue — märkmed

Avalik toimumiskorra leht koos "Küsi lisainfot" modaliga ja registreerumise nupuga. Otsused, andmebaasi ettepanek ja skeemid: `docs/mock-wireframe/loo-mock-vaade/courses-view/courses-view-skeemid.md`. Interaktiivne läbimäng: `courses-view-labimang.html`. Uute teenuste DTO-d on ettepanek; olemas: `GET /api/lecturer-summary/{lecturerId}`.

## Vaate märkmed

```text
Roll: Kõik rollid (sh külastajad, sisselogimist ei nõuta)
Failinimi: CourseView.vue
Frontend rada: /course?courseId={id}

Vaatega seotud lisainfo:
Paigutus nagu /training. Vasakul kaart "Koolitus": pealkiri, toimumisaeg, lühikirjeldus, kirjeldus (RichTextContent) ja link "Kõik selle koolituse toimumiskorrad" → /training. Puuduva tõlke korral põhikeele tekst ja märkus.
Paremal kaart "Toimumiskord": Toimumisaeg, Päevi, Akad. tunde, Hind, Toimumisviis, Koolituse keel, Kategooria, Rahastus, märgis "Täis"; nupp "Registreeru" (sisse logimata → /login?redirect=/course-registration?courseId={id}; kasutaja → /course-registration?courseId={id}; juba registreerunud kasutajale nupu asemel märge "✓ Oled sellele toimumiskorrale registreerunud" — GET /api/course/{courseId}/participant-status?userId=; täis → keelatud "Kohad on täis") ja nupp ja "Küsi lisainfot" (avab päringu modali; möödunud toimumiskorral keelatud). Adminile neid kahte nuppu ei kuvata. Kaart "Koolitajad" (LecturerCard, GET /api/lecturer-summary/{lecturerId}) — ilma koolitajateta ei kuvata; iga koolitaja kaart on link → /lecturer?lecturerId={id}&returnTo=/course?courseId={id} (samas tabis).
Vasakus veerus kohe kaardi "Koolitus" all kaart "Toimumiskorrad": sama koolituse kõik avalikud tulevased toimumiskorrad üksteise all linkidena kujul "12/10/2026 – 15/10/2026 · Kohapeal" (täis korral lisaks märgis "Täis"), alguse järgi. Praegune toimumiskord on paksus kirjas (▸) ega ole link; teise lingi vajutus avab sama vaate: /course?courseId={id} (query muutus laadib vaate uuesti). Kui peale praeguse teisi toimumiskordi pole, sektsiooni ei kuvata.
Modal "Küsi lisainfot" (EnquiryModal.vue): all pealkirja koolitus ja toimumisaeg. Väljad: Eesnimi*, Perekonnanimi*, E-post*, Telefon*, Ettevõte, Sõnum* (kuni 255 märki, loendur); all väike tekst "Kasutame sinu andmeid ainult päringule vastamiseks." "Saada" → frontendi kontroll (kohustuslikud väljad, e-posti kuju) → POST /api/enquiry → modal sulgub ja lehel eduteade "Aitäh! Sinu päring on saadetud, võtame peagi ühendust." "Tühista", × või Esc sulgeb ilma saatmata.
Möödunud toimumiskord avaneb märgisega "Toimunud" ja keelatud nuppudega. Mustand, tühistatud, kustutatud või olematu toimumiskord → üldine veavaade. Adminile pliiats → /course-form?courseId={id}. Keele vahetusel laaditakse uuesti.
```

## API märkmed — GET /api/course-summary/{courseId}

```text
API: GET /api/course-summary/{courseId}

Query parameetrid:
contentLang: String — koolituse teksti keel (puudumisel põhikeel)

Response (200):
CoursePageDto.java
{
  "courseId": 9,
  "trainingId": 3,
  "trainingTranslationId": 5,
  "isMainLanguageFallback": false,
  "title": "Spring Boot veebiarendus",
  "shortDescription": "REST API-de loomine Spring Booti abil.",
  "description": "<p>…</p>",
  "categoryName": "Programmeerimine",
  "trainingLanguageFlagIconCode": "fi-ee",
  "fundingTypes": [ ... ],
  "startDate": "2026-10-12",
  "endDate": "2026-10-15",
  "isPast": false,
  "numberOfDays": 4,
  "numberOfAcademicHours": 32,
  "price": 560.0,
  "status": "O",
  "isOnSite": true,
  "isOnline": false,
  "lecturers": [ { "lecturerId": 1, "lecturerName": "Rain Tüür" } ],
  "upcomingCourses": [
    { "courseId": 1, "startDate": "2026-10-05", "endDate": "2026-10-09", "status": "O", "isOnSite": true, "isOnline": false },
    { "courseId": 5, "startDate": "2026-11-16", "endDate": "2026-11-20", "status": "F", "isOnSite": false, "isOnline": true }
  ]
}

API teenuse lisainfo:
Leiab ainult toimumiskorra, mille status on "O" või "F" ja koolitus publitseeritud ("P"); muidu 404 (sama message nagu olematu ID korral). Möödunud toimumiskord leitakse (isPast = true). Tekst contentLang keeles, puudumisel põhikeeles (isMainLanguageFallback = true). lecturers course_lecturer sort_order järjekorras (võib olla tühi). upcomingCourses = sama koolituse kõik public_course_summary read (ka päritud toimumiskord ise, kui see on tulevane), alguse järgi — frontend tõstab praeguse esile.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'courseId' väärtusega: 123"
```

## API märkmed — GET /api/lecturer-summary/{lecturerId}

```text
API: GET /api/lecturer-summary/{lecturerId}

Query parameetrid:
contentLang: String — ametinimetuse ja lühikirjelduse keel ("et"/"en")

Response (200):
LecturerSummaryDto.java
{
  "lecturerId": 1,
  "fullName": "Rain Tüür",
  "title": "Lektor/konsultant",
  "shortDescription": "Tarkvaraarendus, Java, HTML, CSS, JavaScript, SQL, …",
  "photoVersion": 1759305600
}

API teenuse lisainfo:
Olemasolev teenus — LecturerCard.vue laeb iga koolitaja kaardi ise (sama mis /training lehel), uuesti keele muutumisel. title ja shortDescription contentLang keeles, puudumisel põhikeeles. photoVersion = lecturer_photo.updated_at epoch-sekundites või null (pilti pole); pildi URL /api/lecturer/{lecturerId}/photo?v={photoVersion}. Kustutatud koolitaja → 404 (kaart jäetakse välja).

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'lecturerId' väärtusega: 123"
```

## API märkmed — GET /api/course/{courseId}/participant-status

```text
API: GET /api/course/{courseId}/participant-status

Query parameetrid:
userId: Integer — sisseloginud kasutaja (sessionStorage)

Response (200):
CourseParticipantStatusDto.java
{
  "status": "R"
}

API teenuse lisainfo:
Kasutaja oma osaleja (participant.user_id = userId) course_participant rea staatus: "R" = registreerunud, "C" = loobunud, null = pole registreerunud (ka siis, kui kasutajal osalejat veel pole). Kutsutakse ainult sisseloginud kasutaja korral.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'courseId' väärtusega: 123" (sama 'userId' kohta)
```

## API märkmed — POST /api/enquiry

```text
API: POST /api/enquiry

Request body:
EnquiryCreateRequest.java
{
  "trainingId": 1,
  "courseId": 1,
  "firstName": "Kati",
  "lastName": "Karu",
  "email": "kati.karu@example.com",
  "phone": "+37255512300",
  "companyName": null,
  "message": "Kas kursusele saab tulla ka ilma eelteadmisteta?"
}

Response (200): NONE

API teenuse lisainfo:
Avalik teenus (sisselogimist ei nõuta). Loob uue profile rea (first_name, last_name, email, phone) ja enquiry rea: status = "U" (EnquiryStatus.NEW), training_id, course_id (valikuline — null = üldine päring koolituse kohta), company_name (tühi → null), message. Päring ilmub adminile /admin-enquiries nimekirja uuena.
Kontroll: kohustuslikud trainingId, firstName, lastName, email (@Email), phone, message (max 255); companyName max 255. Koolitus peab olema publitseeritud ja courseId korral toimumiskord selle koolituse avalik ("O"/"F") toimumiskord.

Veateated:
HTTP: 400 — valideerimise viga (kohustuslik väli puudu või vigane)
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'courseId' väärtusega: 123" (sama 'trainingId' kohta)
```


## Tagasitee — täiendatud navigatsioon

Vaade võtab vastu valikulise `returnTo` query parameetri ja kuvab lingi „← Tagasi“ (`BackLink.vue`). Avamislingid annavad kaasa lähtevaate täieliku URL-i. Tagasilingi puuduv, väline, tundmatu või iseendale osutav siht asendatakse vaate varusihtkohaga. Oleku- ja tõlkevahetus ei kaota tagasiteed. Eraldi nimega nimekirja-/kalendrinupud säilitavad oma sihtkoha. Täpne [kaardistus ja varusihtkohad](../../tasks/frontend/return-to-navigation.md) ning [skeemid](../loo-mock-vaade/return-to-navigation-skeemid.md).
