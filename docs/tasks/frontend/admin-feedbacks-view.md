# Koolituste tagasisidede haldus

**Seis:** implementeeritud 02.10.2026. Serveri autentimine/rollikontroll jääb tööde järjekorras märgitud kasutuselevõtu sõltuvuseks.

**Vaade:** `AdminFeedbacksView.vue`, route `/admin-feedbacks`

**Roll:** Admin

**Vaste mockupis:** kinnitatud interaktiivne AdminFeedbacksView HTML-mock; PDF-lehekülg puudub.

> Mockupi pilt lisatakse hiljem. Aluseks on kinnitatud [HTML-mock](../../mock-wireframe/loo-mock-vaade/admin-feedbacks-view/admin-feedbacks-view-labimang.html), [skeemid](../../mock-wireframe/loo-mock-vaade/admin-feedbacks-view/admin-feedbacks-view-skeemid.md) ja [märkmed](../../mock-wireframe/markmed/admin-feedbacks-view-markmed.md).

## Kasutajavoog

Admin avab „Tagasisided”, näeb toimunud koolituste osalejate vastuseid tabelis ning kogu filtreeritud hulga kokkuvõtteid. Ta otsib, filtreerib ja avab ühe rea vastused ning märgib loetud vastused üle vaadatuks. Koolituse ja registreerumise link viivad olemasolevatele admini detailvaadetele koos tagasiteega. Eksport, vastuste muutmine, tagasiside küsimine ja koolitajate personaalne hindamine ei kuulu sellesse vaatesse.

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Tagasisided | Admin-menüü link ja AdminTabs | Registreerumiste järel samas esimeses grupis; aktiivne vaheleht |
| Tagasisidede arv / vajab ülevaatust / madal hinne | Kokkuvõttekaardid | Kogu filtreeritud hulga arvud, madal hinne ≤ 5 |
| Üldkeskmine / Vastanud-registreerunud | Kokkuvõttekaart | Course valimata keskmine, valitud korral suhtarv ja protsent |
| Otsing ja Otsi | Sisend ja nupp | Koolituse/osaleja nime sõnad; Enter samuti rakendab |
| Ava/peida otsingu filtrid | Nupp | Vaikimisi peidetud; sulgemine ei tühjenda |
| Toimumiskord | Select | Kõik + API toimunud koolitused koos kuupäevadega |
| Staatus | Select | Kõik, vajab ülevaatust, uus, muudetud, üle vaadatud |
| Kommentaarid | Select | Kõik, sisaldab kommentaari, puuduvad |
| Toimumine alates/kuni | Kaks date-sisendit | Valikulised kaasavad vahemikupiirid |
| Vähemalt üks hinne ≤ | Select | Kõik, 3, 5, 7 |
| Filtreeri / Tühjenda filtrid | Nupud | Rakenda; tühjendamine eemaldab ka otsingu |
| Kriteeriumide keskmised | Koondtabel/kaardid | Pealkiri, 1 komakohaga keskmine, vastuste arv |
| Tagasisidede tabel | Tabel | Esitatud, Vastuseid muudetud, Koolitus/toimumine, Osaleja, Keskmine, Madalaim, Kommentaarid, Staatus, Tegevused |
| Veerupealkirjad | Sortimisnupud | Kõik eelmainitud peale kommentaaride/tegevuste; visuaalne suund |
| Koolituse/osaleja nimi | Router link | Admini detail koos returnTo=/admin-feedbacks |
| Uus / Muudetud / Üle vaadatud | Staatuse märk | U selgitab muutmist pärast ülevaatust |
| Ava/peida vastused | Rea nupp | Korraga üks avatud detail |
| Vastuste detail | Rea all tabel | Kriteeriumi pealkiri (kitsas veerg), hinne /10, kommentaar (ülejäänud laius); tühi kommentaar — |
| Märgi üle vaadatuks | N/U rea nupp | Lubatud alles eduka detaililaadimise järel; H real puudub |
| Leheküljestus | PaginationNav | 5 rida lehel, backend page algab 0-st |
| Laadimine / edu / viga / tühi tulemus | Inline teated | Kõigil päringutel; vana tabeli andmeid ei esitata uue filtri tulemusena |

## Käitumine ja valideerimine

1. `beforeMount` kontrollib olemasoleva frontendi sessioonimustri admini rolli ning laadib filtrivalikud ja lehe 0. Menüüd ei kuvata teistele rollidele; otsene mitteadmini avamine järgib olemasoleva adminivaate suunamismustrit. Kliendikontroll ei asenda serveri õiguste kontrolli, mille puudujääk on backend taskides märgitud.
2. Vaikeväärtused: kõik filtrid tühjad, sortBy=default, sortDirection=desc, page=0, limit=5, filtrikaart peidus. Valikulised tühjad parameetrid jäetakse requestist välja. Hoida mustandi ja rakendatud filtri/otsingu olek eraldi.
3. Otsi/Enter rakendab otsingu olemasolevate rakendatud filtritega; Filtreeri rakendab filtrid olemasoleva rakendatud otsinguga. From > until korral kuvada välja juures viga ja päringut mitte teha. Peitmine säilitab mustandi/rakendatud väärtused.
4. Tühjenda filtrid eemaldab otsingu ja filtrid, säilitab sortimise. Nupp kuvatakse aktiivse rakendatud filtri või otsingu puhul. Otsing, filter, tühjendamine ja sort alustavad page=0 ning sulgevad vastused; lehevahetus säilitab rakendatud tingimused ja sulgeb detaili.
5. Uue veeru esimene klõps asc, järgmine vahetab suunda. Backend teeb sortimise ja koondarvutused; frontend ei arvuta koonde praegusest lehest. Keskmised 1 komakohaga UI lokaadi järgi, null → —. Madalaim ≤5 eristub visuaalselt ja tekstiga.
6. Ava vastused teeb detailipäringu ja ei muuda staatust. Korraga üks detail. Päringu ebaõnnestumisel ülevaatus keelatud. Kasutada detaili hetke staatust, mitte vaid vana tabelirea oma. Kommentaarid renderdada tekstina, mitte v-html-iga.
7. Ülevaatus saadab detaili answersVersion muutmata headeris. Päringu ajal nupp keelatud; topeltklõps ei tekita paralleelseid PUT-e. Edu: „Tagasiside märgitud üle vaadatuks”, sulgeda detail ja laadida sama leht/sort/filtrid koos koondidega uuesti. Kui viimane leht muutus tühjaks, laadida viimane olemasolev leht üks kord; tühja hulga korral page=0.
8. 409 korral kuvada backend message, laadida ja avada viimased vastused ning värskendada nimekiri. Automaatset uut PUT-i ei tehta; admin peab viimaseid vastuseid teadlikult uuesti üle vaatama. Kui uus staatus H, nuppu ei kuvata.
9. UI keele vahetus laadib valikud, tabeli/koondid ja avatud detaili uuesti, säilitades rakendatud filtrid/sordi/lehe. Eirata aegunud vastuseid (päringujärjekorra tunnus või tühistamine), ka kiirete filtri-, lehe- või detailimuudatuste korral.
10. Koolitus → NavigationService kaudu `/admin-course` query {courseId, returnTo:'/admin-feedbacks'}; osaleja → `/admin-registration` query {courseParticipantId, returnTo:'/admin-feedbacks'}. Menüü/vahelehed returnTo ei lisa. Tagasipöördumisel filtreid taastavat uut URL/store mehhanismi selles taskis ei lisata; kinnitatud mock naaseb vaikeseisu.
11. Tühi tabel: „Tagasisidesid ei leitud. Muuda otsingut või tühjenda filtrid.” Laadimis- ja veaolekud eristada tühjast edukast tulemusest. Kõik nupud/fookus/aria-expanded/sortimise suund ligipääsetavad, tabel mobiilis horisontaalselt keritav. UI tekstid ET/EN i18n võtmetes; contentLang tuleb languageStore-st.

## API kutsed

Kontraktid vastavad AdminFeedbackController/DTO klassidele ja allpool viidatud backend taskidele. GET-idel body puudub, PUT-il samuti; query ja header on kirjeldatud iga kutse juures.

### `GET /api/admin-feedback-courses`

**Backend task:** [GET-api-admin-feedback-courses.md](../backend/GET-api-admin-feedback-courses.md).

`contentLang`: valikuline String, puudumisel või tundmatu koodi puhul põhikeel. Request body puudub. Näide: `GET /api/admin-feedback-courses?contentLang=et`.

**Response (200 OK):** `List<AdminFeedbackCourseDto>`.

```json
[
  {
    "courseId": 15,
    "trainingId": 3,
    "trainingTitle": "Spring Boot veebiarendus",
    "startDate": "2026-09-28",
    "endDate": "2026-09-30"
  },
  {
    "courseId": 16,
    "trainingId": 15,
    "trainingTitle": "Vali-IT Noorem AI arendaja",
    "startDate": "2026-08-10",
    "endDate": "2026-09-25"
  },
  {
    "courseId": 14,
    "trainingId": 8,
    "trainingTitle": "SQL ja andmebaasid",
    "startDate": "2026-09-21",
    "endDate": "2026-09-23"
  },
  {
    "courseId": 12,
    "trainingId": 7,
    "trainingTitle": "Agiilne meeskonnajuhtimine",
    "startDate": "2026-09-14",
    "endDate": "2026-09-15"
  },
  {
    "courseId": 3,
    "trainingId": 1,
    "trainingTitle": "Java algkursus",
    "startDate": "2026-09-07",
    "endDate": "2026-09-11"
  },
  {
    "courseId": 7,
    "trainingId": 1,
    "trainingTitle": "Java algkursus",
    "startDate": "2026-06-08",
    "endDate": "2026-06-12"
  }
]
```

Valik sisaldab `course.end_date < LocalDate.now(Clock)` ja O/F staatusega toimumiskordi, ka ilma tagasisideta. Järjestus endDate DESC, courseId DESC. Koolituse soft delete ei eemalda ajaloolist toimumiskorda. Tühi valik → `[]`.

**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 500 või võrguviga | Standardne serveriviga / puudub | Serveri standardvastus ei taga message-välja | Näidata lokaliseeritud üldviga; detail/ülevaatus keelatud kuni edukalt laaditud |

### `GET /api/admin-feedbacks`

**Backend task:** [GET-api-admin-feedbacks.md](../backend/GET-api-admin-feedbacks.md).

Kõik parameetrid on query's, request body puudub. Näide: `GET /api/admin-feedbacks?contentLang=et&page=0&limit=5`.

| Parameeter | Tüüp / vaikeväärtus | Reegel |
|---|---|---|
| contentLang | String / põhikeel | Puudumisel/tundmatu koodi korral põhikeel |
| searchText | String / tühi | Iga tühikuga eraldatud sõna koolituse pealkirja ja osaleja nime liittekstis, tõstutundetu; SQL LIKE metamärgid on sõnasõnaline tekst |
| courseId | Integer / puudub | Positiivne olemasolev ID, puuduv kirje 404 |
| status | String / puudub | N, U, H, pending (N/U koos) |
| comments | String / puudub | yes = sisuline kommentaar olemas; no = puudub |
| from, until | LocalDate / puudub | ISO yyyy-MM-dd; from ≤ until; endDate ≥ from, startDate ≤ until |
| low | Integer / puudub | Ainult 3, 5 või 7; tagasiside MIN(score) ≤ low |
| sortBy | String / default | default, createdAt, answersUpdatedAt, trainingTitle, participantName, averageScore, minimumScore, status |
| sortDirection | String / desc | asc või desc; default-järjestuses suunda ei rakendata |
| page | Integer / 0 | Vähemalt 0 |
| limit | Integer / 5 | 1–100 |

Uus query/header valideerimine tagastab `400 INCORRECT_INPUT`, message `väljanimi: vigane väärtus`. Esimese vea korral piisab ühest väljast. Tühjad valikulised tekstfiltrid tähendavad filtri puudumist; tühja kohustuslikku headerit see erand ei puuduta. Numbrite, kuupäevade ja tüübiteisenduse vead peavad samuti jõudma sama 400 lepinguni. See on implementeeritud admini API sisendikontrolli ja RestExceptionHandler-i IncorrectInputException handleriga; olemasolev request DTO valideerimine säilib.

**Response (200 OK):** `AdminFeedbackPageDto`; read `AdminFeedbackSummaryDto`, koondkriteeriumid `FeedbackCriteriaSummaryDto`, vastamismäär `CourseFeedbackResponseRateDto`. Vastus näitab kogu esimest lehte, mitte vaid üht näidisrida.

```json
{
  "page": 0,
  "totalPages": 6,
  "totalElements": 30,
  "needsReviewCount": 14,
  "lowScoreFeedbackCount": 4,
  "overallAverageScore": 8.653333333333334,
  "courseResponseRate": null,
  "criteriaAverages": [
    {
      "feedbackCriteriaId": 1,
      "title": "Koolitus vastas ootustele",
      "averageScore": 8.566666666666666,
      "answerCount": 30
    },
    {
      "feedbackCriteriaId": 2,
      "title": "Koolitaja oli pädev",
      "averageScore": 9.0,
      "answerCount": 30
    },
    {
      "feedbackCriteriaId": 3,
      "title": "Õppematerjalid olid asjakohased",
      "averageScore": 8.566666666666666,
      "answerCount": 30
    },
    {
      "feedbackCriteriaId": 4,
      "title": "Õpikeskkond ja korraldus olid sobivad",
      "averageScore": 8.4,
      "answerCount": 30
    },
    {
      "feedbackCriteriaId": 5,
      "title": "Soovitaksin koolitust kolleegidele",
      "averageScore": 8.733333333333333,
      "answerCount": 30
    }
  ],
  "content": [
    {
      "feedbackId": 18,
      "courseParticipantId": 29,
      "courseId": 16,
      "trainingId": 15,
      "trainingTitle": "Vali-IT Noorem AI arendaja",
      "participantName": "Kristjan Oja",
      "startDate": "2026-08-10",
      "endDate": "2026-09-25",
      "createdAt": "2026-09-28T08:00:00Z",
      "answersUpdatedAt": "2026-09-30T12:00:00Z",
      "averageScore": 9.0,
      "minimumScore": 8,
      "commentCount": 1,
      "status": "U"
    },
    {
      "feedbackId": 12,
      "courseParticipantId": 23,
      "courseId": 16,
      "trainingId": 15,
      "trainingTitle": "Vali-IT Noorem AI arendaja",
      "participantName": "Jaan Org",
      "startDate": "2026-08-10",
      "endDate": "2026-09-25",
      "createdAt": "2026-09-28T06:00:00Z",
      "answersUpdatedAt": "2026-09-30T12:00:00Z",
      "averageScore": 9.0,
      "minimumScore": 8,
      "commentCount": 1,
      "status": "U"
    },
    {
      "feedbackId": 23,
      "courseParticipantId": 34,
      "courseId": 16,
      "trainingId": 15,
      "trainingTitle": "Vali-IT Noorem AI arendaja",
      "participantName": "Merle Ilves",
      "startDate": "2026-08-10",
      "endDate": "2026-09-25",
      "createdAt": "2026-09-27T10:00:00Z",
      "answersUpdatedAt": "2026-09-27T10:00:00Z",
      "averageScore": 9.2,
      "minimumScore": 8,
      "commentCount": 0,
      "status": "N"
    },
    {
      "feedbackId": 20,
      "courseParticipantId": 31,
      "courseId": 16,
      "trainingId": 15,
      "trainingTitle": "Vali-IT Noorem AI arendaja",
      "participantName": "Markus Lill",
      "startDate": "2026-08-10",
      "endDate": "2026-09-25",
      "createdAt": "2026-09-27T09:00:00Z",
      "answersUpdatedAt": "2026-09-27T09:00:00Z",
      "averageScore": 9.2,
      "minimumScore": 8,
      "commentCount": 0,
      "status": "N"
    },
    {
      "feedbackId": 11,
      "courseParticipantId": 22,
      "courseId": 16,
      "trainingId": 15,
      "trainingTitle": "Vali-IT Noorem AI arendaja",
      "participantName": "Liis Kuusk",
      "startDate": "2026-08-10",
      "endDate": "2026-09-25",
      "createdAt": "2026-09-27T06:00:00Z",
      "answersUpdatedAt": "2026-09-27T06:00:00Z",
      "averageScore": 9.2,
      "minimumScore": 8,
      "commentCount": 0,
      "status": "N"
    }
  ]
}
```

Üks rida = üks feedback. Kaasatakse mineviku toimumiskorra tagasiside; ajaloolist kirjet ei peideta hilisema training/course soft delete või registreerumise C tõttu. Olemasolev tuleviku courseId annab tühja tagasisidehulga, mitte 404; vastamismäär arvutatakse siiski valitud olemasolevast toimumiskorrast.

Kõik koondandmed, kriteeriumide keskmised ja totalElements arvutatakse kogu sama filtreeritud hulga pealt. Leht ja kokkuvõtted loetakse kooskõlalise hetktõmmisena. Liitumised ei tohi korrutada feedback'e ega vastuseid. SQL päringute arv ei tohi kasvada lehel olevate ridade arvuga; enne rea avamist lisadetailipäringuid ei tehta.

Rea keskmine = AVG olemasolevatest hinnetest; minimumScore = MIN, vastusteta kirjel mõlemad null. Kommentaaride arv loeb trimmitud mittetühja teksti; comments=yes nõuab arvu > 0, no = 0. Üldkeskmine on kõigi vastuste AVG, mitte feedback keskmiste AVG. Puuduv vastus ei ole 0. lowScoreFeedbackCount kasutab alati piiri 5, sõltumata low filtrist. Arvutused ja sort kasutavad ümardamata väärtusi. Vastuseid muudetud = MAX(course_participant_feedback.updated_at), vastusteta null; päise ülevaatamise aeg seda ei mõjuta.

Vaikimisi N/U koos enne H, grupis createdAt DESC, feedbackId DESC. Muude sortide võrdsed väärtused → feedbackId DESC; nullväärtused mõlemas suunas lõpus. Staatus asc N → U → H. Pealkirjad järjestatakse kuvatud keeles, nimed tõstutundetult (andmebaasi kokkulepitud kollatsioon); sorteerimisväljad on lubatud loend, kliendi teksti ei ühendata SQL-i.

criteriaAverages sisaldab aktiivseid kriteeriume ning filtreeritud hulgas vastatud D-kriteeriume. Järjestus sequence ASC, ID ASC, title UI keeles/fallback. Vastuseta kriteeriumil averageScore=null ja answerCount=0. Tühi tulemus → content=[], totalPages=0, totalElements/needsReviewCount/lowScoreFeedbackCount=0, overallAverageScore=null, aktiivsed kriteeriumid null-keskmisega. Vahemikust väljas leht annab content=[], säilitades kogu hulga koondid ja küsitud page.

courseResponseRate=null, kui courseId puudub. Muidu R-registreerumiste arv ning neist tagasiside esitanute arv; C jääb mõlemast välja. Muud filtrid ega page seda ei mõjuta. Nimetaja 0 → responsePercentage=null. Näide courseId=12:

```json
{
  "courseId": 12,
  "registeredCount": 5,
  "respondedCount": 4,
  "responsePercentage": 80.0
}
```

**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 400 | INCORRECT_INPUT | väljanimi: vigane väärtus | Kuvada inline viga; ebaõnnestunud andmeid mitte rakendada |
| 404 | PRIMARY_KEY_NOT_FOUND | Ei leidnud primary keyd 'courseId' väärtusega: 123 | Suunata olemasolevale üldisele veavaatele |
| 500 või võrguviga | Standardne serveriviga / puudub | Serveri standardvastus ei taga message-välja | Näidata lokaliseeritud üldviga; detail/ülevaatus keelatud kuni edukalt laaditud |

### `GET /api/admin-feedback/{feedbackId}`

**Backend task:** [GET-api-admin-feedback-feedbackId.md](../backend/GET-api-admin-feedback-feedbackId.md).

`feedbackId`: kohustuslik positiivne Integer path variable. `contentLang`: valikuline String query, puudumisel/tundmatu koodi puhul põhikeel. Body puudub. Näide: `GET /api/admin-feedback/3?contentLang=et`.

Uus query/header valideerimine tagastab `400 INCORRECT_INPUT`, message `väljanimi: vigane väärtus`. Esimese vea korral piisab ühest väljast. Tühjad valikulised tekstfiltrid tähendavad filtri puudumist; tühja kohustuslikku headerit see erand ei puuduta. Numbrite, kuupäevade ja tüübiteisenduse vead peavad samuti jõudma sama 400 lepinguni. See on implementeeritud admini API sisendikontrolli ja RestExceptionHandler-i IncorrectInputException handleriga; olemasolev request DTO valideerimine säilib.

**Response (200 OK):** `AdminFeedbackDto`.

```json
{
  "feedbackId": 3,
  "courseParticipantId": 11,
  "courseId": 14,
  "trainingId": 8,
  "trainingTitle": "SQL ja andmebaasid",
  "participantName": "Anna Saar",
  "startDate": "2026-09-21",
  "endDate": "2026-09-23",
  "status": "U",
  "createdAt": "2026-09-24T07:00:00Z",
  "answersUpdatedAt": "2026-09-28T11:00:00Z",
  "answersVersion": "aa50de31307af532a06f996a2a161fab5d0316fd66d3abe7d0bc0f22dac37678",
  "criteria": [
    {
      "feedbackCriteriaId": 1,
      "title": "Koolitus vastas ootustele",
      "description": "Koolituse sisu, tase ja maht vastasid koolituse kirjelduse põhjal tekkinud ootustele.",
      "score": 5,
      "feedbackText": "Tempo oli minu jaoks liiga kiire."
    },
    {
      "feedbackCriteriaId": 2,
      "title": "Koolitaja oli pädev",
      "description": "Koolitaja valdas teemat põhjalikult ning selgitas seda arusaadavalt ja näidetega.",
      "score": 8,
      "feedbackText": null
    },
    {
      "feedbackCriteriaId": 3,
      "title": "Õppematerjalid olid asjakohased",
      "description": "Õppematerjalid ja harjutused toetasid teema omandamist ning on kasutatavad ka pärast koolitust.",
      "score": 4,
      "feedbackText": "Vajaksin rohkem algajatele sobivaid näiteid."
    },
    {
      "feedbackCriteriaId": 4,
      "title": "Õpikeskkond ja korraldus olid sobivad",
      "description": "Koolitusruum või veebikeskkond, ajakava ja info edastamine toetasid õppimist.",
      "score": 7,
      "feedbackText": null
    },
    {
      "feedbackCriteriaId": 5,
      "title": "Soovitaksin koolitust kolleegidele",
      "description": "Julgeksin seda koolitust soovitada kolleegidele või teistele samade vajadustega inimestele.",
      "score": 5,
      "feedbackText": null
    }
  ]
}
```

Vaid tegelikult antud vastused, sh hiljem D-kriteeriumiks muutunud küsimused. Hiljem lisatud vastamata kriteerium ei ole osaleja vastus. Järjestus sequence ASC, ID ASC. Kasutada olemasolevat `FeedbackCriteriaItemDto`, vajadusel tõsta `controller/common/dto` alla ja uuendada ka osaleja API importi; väljakujuga identset DTO-d ei dubleerita. Detail loetakse kooskõlalise hetktõmmisena koos versiooniga, järgides [konkurentsikontrolli taski](../backend/feedback-review-concurrency.md). Puuduvad vastused → criteria=[], answersUpdatedAt=null, kuid ka tühjal hulgal on versioon.

Versioon ei sõltu kuvatud keelest, tõlgetest ega staatusest. Lugemine ei muuda staatust ega auditiveerge. Detail on admini teenus, mitte osaleja omanikukontrolliga teenuse ümberkasutus.

**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 400 | INCORRECT_INPUT | väljanimi: vigane väärtus | Kuvada inline viga; ebaõnnestunud andmeid mitte rakendada |
| 404 | PRIMARY_KEY_NOT_FOUND | Ei leidnud primary keyd 'feedbackId' väärtusega: 123 | Suunata olemasolevale üldisele veavaatele |
| 500 või võrguviga | Standardne serveriviga / puudub | Serveri standardvastus ei taga message-välja | Näidata lokaliseeritud üldviga; detail/ülevaatus keelatud kuni edukalt laaditud |

### `PUT /api/admin-feedback/{feedbackId}/review`

**Backend task:** [PUT-api-admin-feedback-feedbackId-review.md](../backend/PUT-api-admin-feedback-feedbackId-review.md).

`feedbackId`: kohustuslik positiivne Integer path variable. Kohustuslik header `X-Answers-Version`: avatud detaili täpne läbipaistmatu answersVersion. Body ja request DTO puuduvad. Näide: `PUT /api/admin-feedback/3/review`, header `X-Answers-Version: aa50de31307af532a06f996a2a161fab5d0316fd66d3abe7d0bc0f22dac37678`.

Puuduv/tühi või mitte 64 väikese hex-märgi kujul versioon annab 400. Korrektse kujuga, kuid erinev versioon annab 409.

Uus query/header valideerimine tagastab `400 INCORRECT_INPUT`, message `väljanimi: vigane väärtus`. Esimese vea korral piisab ühest väljast. Tühjad valikulised tekstfiltrid tähendavad filtri puudumist; tühja kohustuslikku headerit see erand ei puuduta. Numbrite, kuupäevade ja tüübiteisenduse vead peavad samuti jõudma sama 400 lepinguni. See on implementeeritud admini API sisendikontrolli ja RestExceptionHandler-i IncorrectInputException handleriga; olemasolev request DTO valideerimine säilib.

**Response (200 OK):** tühi vastus, ainult staatuskood 200.

Transaktsioonis võtta sama feedback rea lukk kui osaleja muutmisel; lugeda vastused, võrrelda versiooni ja alles siis N/U → H. Sama versiooniga H → H on idempotentne ning ei muuda auditiveerge. Erinev versioon annab 409 ka H korral. Puuduv kirje 404; midagi osaliselt ei salvestata. Muutunud päise updatedAt täidab auditing, vastuste hindeid/tekste/aegu ei muudeta. Eeldus: [konkurentsikontrolli task](../backend/feedback-review-concurrency.md) valmis.

**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 400 | INCORRECT_INPUT | väljanimi: vigane väärtus | Kuvada inline viga; ebaõnnestunud andmeid mitte rakendada |
| 404 | PRIMARY_KEY_NOT_FOUND | Ei leidnud primary keyd 'feedbackId' väärtusega: 123 | Suunata olemasolevale üldisele veavaatele |
| 409 | FEEDBACK_ANSWERS_CHANGED | Tagasiside vastused on vahepeal muutunud. Vaata vastused uuesti üle. | Laadida viimased vastused; uus ülevaatus vajab admini klõpsu |
| 500 või võrguviga | Standardne serveriviga / puudub | Serveri standardvastus ei taga message-välja | Näidata lokaliseeritud üldviga; detail/ülevaatus keelatud kuni edukalt laaditud |

## Komponendid ja failistruktuur

- `frontend/src/views/AdminFeedbacksView.vue`: vaade ja adminFeedbacksRoute on lisatud.
- `frontend/src/api-services/AdminFeedbackService.js`: neli axios päringut. Nimi on frontend teenuse nimi, backendis äriloogika eraldi.
- Vajadusel `frontend/src/components/tables/AdminFeedbacksTable.vue` ja `frontend/src/components/forms/AdminFeedbackFilters.vue`; emits `event-` eesliitega. Korduv detail võiks olla tabelikomponendi osa.
- Taaskasutada `components/common/AdminTabs.vue`, `PaginationNav.vue`, `SortableColumnHeader.vue` ja projekti olemasolevaid inline-teateid.
- `frontend/src/router/index.js`: `adminFeedbacksRoute`; `src/services/NavigationService.js`: navigeerimismeetodid tegeliku projekti struktuuri järgi.
- `App.vue` admin-menüü ja `components/common/AdminTabs.vue`: uus link pärast Registreerumisi samas grupis; `src/locales/et.json` / `en.json`, sh `navbar.manageFeedbacks`.
- Vue 3 Options API ja Bootstrap 5; andmed `beforeMount`, API `.then/.catch/.finally`; arvutus- ja kuvamisloogika computed/methods projekti juhiste järgi.

## Vastuvõtu kriteeriumid

- [x] Route, admin-menüü, AdminTabs ja ET/EN tekstid on olemas ning samas järjekorras nagu mockis.
- [x] Kõik mocki tabeliveerud, filtrid, koondid, detail ja tegevused on olemas; limit=5.
- [x] Nimekirja laadimine ei tekita iga tabelirea kohta detailipäringut.
- [x] Otsing, Enter, filtrimustand/rakendatud väärtused, peitmine, tühjendamine, sortimine ja leheküljed järgivad kokkulepet.
- [x] Vigane kuupäevavahemik peatab päringu; backend 400, 404, 409, 500 ja võrguvead käituvad kirjeldatult.
- [x] Ülevaatus on lubatud alles loetud detailile; topeltpäringud ja automaatne PUT pärast 409 puuduvad.
- [x] Kõik koondid on backendist; DML-i näited kuvavad 30 / 14 / 4 / 8,7 ning course 12 korral 4/5.
- [x] Tühjad tulemused, null-keskmised, kommentaarita read, H-detail ja ajaloolised vastused on kontrollitud.
- [x] Keelevahetus säilitab filtrid/sordi/lehe; aegunud päring ei kirjuta uut olekut üle.
- [x] Detaililingid annavad returnTo query-objektina; menüü ja tabs ei lisa returnTo-d.
- [x] Klaviatuur, aria olekud ja mobiili tabel on kontrollitud; kommentaarid ei renderdu HTML-ina.
- [x] Mockid, skeemid ja märkmed ajakohastatakse tegeliku implementatsiooni järel.
