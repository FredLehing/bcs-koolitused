# AdminFeedbacksView.vue — märkmed

Kinnitatud lahendus: üks rida osaleja tagasiside kohta, vastused avanevad rea all. [Mock](../loo-mock-vaade/admin-feedbacks-view/admin-feedbacks-view-labimang.html) ja [skeemid](../loo-mock-vaade/admin-feedbacks-view/admin-feedbacks-view-skeemid.md). Näited pärinevad `docs/database/3_import.sql` failist, seis 01.10.2026.

**Implementatsiooni seis (02.10.2026):** neli endpointi on olemas `AdminFeedbackController` klassis; äriloogika `AdminFeedbackService`, leheküljestus/sort/koondid `AdminFeedbackRepository` JPQL päringutes. `AdminFeedbacksView.vue`, router, navbar, AdminTabs ja ET/EN tekstid on lisatud. 400/404/409 lepingud on implementeeritud. Osaleja PUT ning admini detail ja review võtavad sama feedback rea andmebaasiluku. Serveripoolne autentimise/rollikontrolli leping puudub endiselt; kliendi adminikontroll ei ole serveri õiguste tõend ning päris kasutuselevõtt sõltub selle lahendamisest.

## Vaate märkmed

```text
Roll: Admin
Failinimi: AdminFeedbacksView.vue
Frontend rada: /admin-feedbacks

Vaatega seotud lisainfo:
Admin-menüüs ja AdminTabs.vue vahelehtedel „Tagasisided” kohe „Registreerumised” järel, enne esimest menüü eraldusjoont; aktiivne vaheleht „Tagasisided”. Üks tabelirida = üks feedback; veerud Esitatud, Vastuseid muudetud, Koolitus / toimumine, Osaleja, Keskmine, Madalaim, Kommentaarid, Staatus ja Tegevused. Ülal kogu filtreeritud hulga kokkuvõtted ja kriteeriumide keskmised; valitud toimumiskorral näidatakse „Vastanud / registreerunud”, loobunuid nimetajasse ei loeta.
Otsing koolituse/osaleja nimest „Otsi” või Enteriga. Peidetav filtrikaart: toimumiskord, staatus, kommentaarid, toimumise vahemik, vähemalt üks hinne ≤ 3/5/7. „Filtreeri” rakendab filtrid, peitmine säilitab need; „Tühjenda filtrid” eemaldab ka otsingu. Vaikimisi N/U enne H, grupis uuemad enne; veeru klõps sorteerib backendis. Otsing/filter/sort/tühjendamine alustab lehelt 0, lehe suurus 5. Tühi tabel: „Tagasisidesid ei leitud. Muuda otsingut või tühjenda filtrid.”
„Ava vastused” avab rea alla kriteeriumi, hinde ja kommentaari, korraga üks rida; avamine staatust ei muuda. N/U real saab pärast vastuste edukat laadimist vajutada „Märgi üle vaadatuks”; H real nuppu ei näidata. Edu → „Tagasiside märgitud üle vaadatuks”, sama päringu tabel/kokkuvõtted uuesti. Vahepeal muudetud vastused → 409, laadi/ava viimased vastused ja lase adminil uuesti üle vaadata; automaatselt uuesti H-ks ei märgita.
Koolituse link → /admin-course?courseId={courseId}&returnTo=/admin-feedbacks; osaleja link → /admin-registration?courseParticipantId={courseParticipantId}&returnTo=/admin-feedbacks. Menüü/vahelehed returnTo ei lisa. UI keele vahetus laadib tabeli, koondandmed, filtrivalikud ja avatud vastused uuesti; filtrid/sort/leht säilivad. Kõik laadimised näitavad laadimisolekut; ülevaatamise nupp on päringu ajal keelatud. Ebaõnnestunud detaililaadimine ei võimalda ülevaatamist, 404 suunab üldisele veavaatele.
```

## API märkmed — GET /api/admin-feedback-courses

```text
API: GET /api/admin-feedback-courses

Query parameetrid:
contentLang: String — koolituse pealkirja keel, puudumisel põhikeel (valikuline)

Response (200):
AdminFeedbackCourseDto.java
[
  {
    "courseId": 15,
    "trainingId": 3,
    "trainingTitle": "Spring Boot veebiarendus",
    "startDate": "2026-09-28",
    "endDate": "2026-09-30"
  },
  ...
]

API teenuse lisainfo:
Ainult adminile. Toimunud toimumiskorrad: course.end_date < serveri tänane kuupäev, status O/F; järjestus end_date DESC, courseId DESC. Ka tagasisideta toimumiskorrad kuuluvad valikusse (näide courseId 15, vastamismäär 0/2). Koolituse pealkiri contentLang keeles, puudumisel põhikeeles. Ajaloolise toimumiskorra koolituse hilisem soft delete ei eemalda seda filtrivalikust. Tühi valik → [].

Veateated: —
```

## API märkmed — GET /api/admin-feedbacks

```text
API: GET /api/admin-feedbacks

Query parameetrid:
contentLang: String — pealkirjade/kriteeriumide keel, puudumisel põhikeel (valikuline)
searchText: String — iga tühikuga eraldatud sõna peab leiduma koolituse ja osaleja nime liittekstis, tõstutundetu (valikuline)
courseId: Integer — konkreetne toimumiskord, puudumisel kõik (valikuline)
status: String — N/U/H või pending (N/U koos), puudumisel kõik (valikuline)
comments: String — yes/no, puudumisel kõik (valikuline)
from: LocalDate — kaasav toimumise alguspiir; course.endDate >= from (valikuline)
until: LocalDate — kaasav toimumise lõpppiir; course.startDate <= until (valikuline)
low: Integer — vähemalt üks score <= low; valikud 3/5/7, puudumisel kõik (valikuline)
sortBy: String — default/createdAt/answersUpdatedAt/trainingTitle/participantName/averageScore/minimumScore/status; vaikimisi default (valikuline)
sortDirection: String — asc/desc, vaikimisi desc; sortBy=default kasutab fikseeritud tööjärjekorda (valikuline)
page: Integer — nullist algav leht, vaikimisi 0 (valikuline)
limit: Integer — lehe suurus, vaikimisi 5, lubatud 1–100 (valikuline)

Response (200):
AdminFeedbackPageDto.java
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

API teenuse lisainfo:
Ainult adminile. Üks rida feedback kohta; tingimus course.end_date < täna. Juba antud ajaloolist tagasisidet ei peideta koolituse/toimumiskorra hilisema soft delete või registreerumise hilisema C-staatuse tõttu. Esitatud = feedback.created_at; Vastuseid muudetud = MAX(vastuste.updated_at), mitte päise updated_at. Vaikimisi N/U enne H, grupis createdAt DESC ja feedbackId DESC; sort veeru järgi, võrdsete puhul feedbackId DESC. Staatus asc: N → U → H. Liitumised ei tohi korrutada tagasisideridu; koondandmed pärinevad kogu samade filtritega hulgast, vastused ei vaja enne avamist eraldi reahaaval päringuid.
Keskmised arvutatakse olemasolevatest hinnetest ümardamata; frontend kuvab ühe komakohaga. Puuduv hinne ei ole 0. lowScoreFeedbackCount = tagasisidede arv, mille madalaim score <= 5. Tühja hulga puhul content=[], totalPages=0, arvud 0, overallAverageScore=null; criteriaAverages sisaldab aktiivseid ja filtreeritud hulgas vastatud ajaloolisi kriteeriume, puuduv keskmine null ja answerCount 0. Kriteeriumide järjestus sequence, seejärel feedbackCriteriaId; tõlke fallback põhikeelde. Kommentaare ei tõlgita.
courseResponseRate on null ilma courseId-ta; valitud toimumiskorral objekt {"courseId":12,"registeredCount":5,"respondedCount":4,"responsePercentage":80.0}. Arvutus loeb ainult R-registreerumisi; muud filtrid ja lehekülg ei mõjuta seda objekti. Registreerunuid 0 → responsePercentage=null. H-staatuse muutus ei muuda vastamismäära. Puuduv courseId kirje → 404; vale filter, from > until või leheküljestus/sort väljaspool lubatud väärtusi → 400.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'courseId' väärtusega: 123"
```

## API märkmed — GET /api/admin-feedback/{feedbackId}

```text
API: GET /api/admin-feedback/{feedbackId}

Query parameetrid:
contentLang: String — kriteeriumide/koolituse teksti keel, puudumisel põhikeel (valikuline)

Response (200):
AdminFeedbackDto.java
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
    ...
  ]
}

API teenuse lisainfo:
Ainult adminile. Tagastab just sellele feedback kirjele antud vastused, ka hiljem kustutatud kriteeriumid; hiljem lisatud vastamata kriteeriume ei esitata osaleja antud vastusena. Järjestus sequence, feedbackCriteriaId; title/description contentLang keeles, puudumisel põhikeeles, kommentaar algkeeles. criteria elemendi väljad vastavad olemasolevale FeedbackCriteriaItemDto.java klassile; mitme ressursipaketi vahel taaskasutamisel liigub see controller/common/dto paketti. Detaili lugemine ei muuda staatust.
answersVersion on backendis arvutatud läbipaistmatu kontrollsumma vastuste tegelikust sisust, identiteetidest ja muutmisaegadest; ei ole DB veerg ega üksnes MAX(updated_at). UI saadab täpselt saadud väärtuse ülevaatamise teenusele. Versioon ei sõltu contentLang-st, tõlgitud pealkirjadest ega N/U/H staatusest. Vastused ja versioon loetakse kooskõlalise hetktõmmisena. Kontrollsumma kanooniline serialiseerimine on fikseeritud taskis feedback-review-concurrency.md; näite väärtus on arvutatud DML-i feedbackId 3 vastuste põhjal, mitte osalejalt sisestatav tekst.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'feedbackId' väärtusega: 123"
```

## API märkmed — PUT /api/admin-feedback/{feedbackId}/review

```text
API: PUT /api/admin-feedback/{feedbackId}/review

Response (200): NONE

API teenuse lisainfo:
Ainult adminile. Kohustuslik request header X-Answers-Version: detaili answersVersion väärtus; see on konkurentsikontroll, mitte uus staatus. Tegevusteenusel request body ega request DTO puudub, backend teab sihtstaatust H. Kontrollib feedback olemasolu, lukustab selle ning võrdleb avatud vastuste versiooni praegusega; sama luku peab võtma ka osaleja PUT-tagasiside teenus enne vastuste muutmist. Kehtiv versioon → N/U muutub H; sama versiooniga korduv kutse H korral tagastab 200 muutmata andmeid. Vastuste hindeid/tekste ega nende updated_at väärtusi ei muudeta, päise updated_at auditeeritakse automaatselt.
Vahepeal muudetud vastused → 409, midagi ei märgita üle vaadatuks; FE laadib viimased vastused ja nõuab uut teadlikku ülevaatamist. Puuduv/tühi versioon → 400. 409 FEEDBACK_ANSWERS_CHANGED tekstiga „Tagasiside vastused on vahepeal muutunud. Vaata vastused uuesti üle.” on ConflictException-i ja RestExceptionHandler-i kaudu implementeeritud. Vastuseid pole võimalik muuta pärast kontrolli enne H salvestamist sama transaktsiooni/luku sees. Osaleja tegelik hilisem muudatus muudab H → U; muutmata vastuste salvestamine staatust ei muuda.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'feedbackId' väärtusega: 123"
```

## DTO-de ja vigade implementatsiooni märkused

- `AdminFeedbackPageDto`, selle rea `AdminFeedbackSummaryDto`, `AdminFeedbackCourseDto`, `AdminFeedbackDto`, kriteeriumi koondrea `FeedbackCriteriaSummaryDto` ja vastamismäära `CourseFeedbackResponseRateDto` on implementeeritud DTO-nimed. Jagatud tüübid paigutatakse `controller/common/dto` alla ainult siis, kui neid kasutab mitu ressursipaketti.
- Vastuse DTO lihtväljad ja lihtobjekt `courseResponseRate` paiknevad deklaratsioonis/JSON-is enne massiive `criteriaAverages` ja `content`. Detaili `criteria` vastab olemasoleva kriteeriumi DTO väljadele.
- Olemasolev `INCORRECT_INPUT` (400) genereerib valideerimise message kujul `väljanimi: valideerimisteade`; uue admini API query/header kontroll annab IncorrectInputException-i ja 400 „väljanimi: vigane väärtus”; kohandatud 409 handler on lisatud. Uute teenuste 401/403 autentimisvigade koode/tekste ei esitata olemasoleva koodina.
- Näidete ajatempleid käsitletakse seniste mockide järgi Europe/Tallinn kohaliku aja teisendusena UTC-sse; kõigi näidete päevadel on vahe +03:00. See teisendus tuleb PostgreSQL timestamp veergude ja JPA Instant väljade vahel ühtselt tagada. Kuupäevad startDate/endDate on LocalDate, neid ajavööndiga ei nihutata.
- Näidisandmed: 30 tagasisidet, 150 vastust, 14 vajab ülevaatust, 4 madala hindega tagasisidet. Kõigi vastuste keskmine 8.653333333333334 → kuval 8,7. Toimumiskord 12: 4/5 (80%); 14: 6/6 (100%); 15: 0/2 (0%).

## Taskides fikseeritud täpsustused

- [Tööde järjekord](../loo-mock-vaade/admin-feedbacks-view/admin-feedbacks-view-toode-jarjekord.md) viitab kõigile neljale endpointi taskile, konkurentsikontrolli taskile ning frontend taskile.
- Uus 400: `INCORRECT_INPUT`, message `väljanimi: vigane väärtus`; review puhul puuduv/tühi või vale kujuga X-Answers-Version annab 400.
- Uus 409: `FEEDBACK_ANSWERS_CHANGED`, message „Tagasiside vastused on vahepeal muutunud. Vaata vastused uuesti üle.” Korrektse kujuga, ent aegunud versioon annab 409 ka H-staatuses.
- Versioon on SHA-256 UTF-8 kanoonilisest JSON-ist `[feedbackId,[[answerId,feedbackCriteriaId,score,feedbackText,updatedAtEpochSecond,updatedAtNano],...]]`, vastused answerId ASC. Täpne leping ja DML-i kontrollnäide on konkurentsikontrolli taskis.
- Sortimisel nullid mõlemas suunas lõppu; vastusteta rea keskmine/madalam/aeg on null. LIKE metamärgid otsingus on sõnasõnaline tekst. Olemasolev tuleviku courseId annab tühja hulga, mitte 404.
- Tühjad valikulised tekstfiltrid tähendavad filtri puudumist. Puuduv või tundmatu contentLang kasutab põhikeelt.
- Detaililt tagasipöördumisel jääb kinnitatud mocki vaikeseis; filtrite taastamine on eraldi edasine täiendus.

## Teostuse kontroll

- Tagasiside ajatemplite teisendaja `FeedbackTimestampConverter` loeb/kirjutab nende kahe tabeli kohalikke timestamp-väärtusi Europe/Tallinn ajana. API ja kontrollsumma kasutavad Instant/UTC väärtust; teiste domeenide ajatempleid see ei muuda.
- Nimekirja teenuse REPEATABLE_READ transaktsioon hoiab lehe, arvud ja keskmised samas hetktõmmises. Dünaamiline JPQL jääb persistence-kihisse: muutuv otsingusõnade arv ja lubatud sortide avaldised; kõik kasutajaväärtused seotakse parameetritena.
- H2 PostgreSQL-režiimis integratsioonitest kasutab DML-i päris näiteid; kontrollib koonde, sorte, keelt, ajaloolisi vastuseid, versiooni, idempotentsust ning kahe transaktsiooni mõlemat muutmisjärjekorda. Arenduse PostgreSQL-i test ei kasuta.
- Frontendi käitumistestide käsk: `npm run test:feedbacks`; testid katavad mustandi/rakendatud filtri eristust, aegunud vastuseid, ülevaatamise lukustust, 409 ja lehe korrigeerimist. Build kontrollitakse eraldatud Linuxi koopias.
- 404 suunamine kasutab selles vaates Routeri `errorRoute` rada: olemasolev üldine NavigationService.navigateToErrorView on projektis välja kommenteeritud.

02.10.2026 täiendus: vastuste detail kuvab ainult kriteeriumi pealkirja (12rem), hinne võtab 5rem ja kommentaar ülejäänud laiuse. Lõiguvahed säilivad ning pikad sõnad murduvad. API description-välja ei kuvata. Vali-IT Noorem AI arendaja: 16 tagasisidet / 18 registreerunut; 80 vastusest 7 kommentaariga, neist neli pikka mitmelõigulist teksti. Kommentaari piir 10000 märki, andmebaasis text; olemasoleva baasi muutmisskript: docs/database/4_feedback_long_comments.sql.
