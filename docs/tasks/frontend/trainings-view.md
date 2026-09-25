# Koolituste sirvimine ja filtreerimine

**Vaade:** `TrainingsView.vue`, route `/trainings`

**Roll:** Kõik rollid (sh külastajad, sisselogimist ei nõuta)

**Vaste mockupis:** "TrainingsView", lehekülg 1/1 (vt pilt `TrainingsView.png`)

![Mockup](../../mock-wireframe/pdf-images/TrainingsView.png)

## Kasutajavoog

Kasutaja avab avaliku koolituste nimekirja vaate (sisselogimist ei nõuta). Vaates kuvatakse leheküljestatud koolituste kaardid (`TrainingCard.vue`) koos otsinguvälja, kategooria- ja keelefiltriga. Kategooria või keele valiku muutmisel, samuti "Otsi" nupule vajutamisel, tehakse uus `GET /api/trainings` päring kehtivate filtritega ja nimekiri värskendatakse. Lehekülgede vahel liigutakse "Eelmine"/"Järgmine" nuppude ja leheküljenumbritega. Iga kaardi "Vaata lähemalt" nupp suunab kasutaja `CourseView` vaatele valitud koolituse kohta.

Mockupi "Vaatega seotud lisainfo" mainib ka sortimist ja kalendris kuupäeva muutmist, kuid wireframe'il neid pole, mockupi kõrvalmärge ütleb selgelt "Sorteerimist pole" ning backendis pole ei sortimise ega kuupäeva parameetrit — need jäävad selle taski skoobist välja. Kõrvalmärge "Andmete pärimiseks teha eraldi database view" puudutab backendi ja on juba tehtud (`training_summary` vaade failis `docs/database/2_create.sql`).

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Otsinguväli | Tekstisisend | Vabateksti otsing (mockupil platsihoidja "AI-Arendaja"). Valikuline. **NB!** Backendis pole otsingusõna parameetrit — vt "Lahtised otsad" |
| "Otsi" nupp | Nupp | Teeb uue `GET /api/trainings` päringu kehtivate filtritega (kõrvalmärge: "Otsinguga triggerdub ka kategooria keele valik") ja lähtestab lehekülje (`page=0`) |
| Kategooria valik | Dropdown (select) | Valik "Kõik" (`categoryId=0`) + kategooriad. Muutmisel tehakse uus päring. Mockupil näidisväärtused AI, Tarkvaraarendus, Andmebaasid, Graafika; andmebaasis on praegu Programmeerimine (1), Disain (2), Juhtimine (3). Valikute allikas — vt "Lahtised otsad" |
| Keele valik | Dropdown (select) | Väärtused "Eesti" (`et`) ja "Inglise" (`en`) → `trainingLang`. Muutmisel tehakse uus päring. Valikut "Kõik keeled" backend ei toeta (vt "API kutsed") |
| Koolituse kaart (`TrainingCard.vue`) | Korduv komponent | Üks kaart iga `trainingSummaries` elemendi kohta: pealkiri (`title`), õppekeele märk (`trainingLanguageCode`: `et` → "Eesti", `en` → "Inglise"), lühikirjeldus (`shortDescription`), kategooria silt (`categoryName`), rahastustüüpide sildid (`fundingTypes[].fundingTypeName`, kui neid on) ja "Vaata lähemalt" nupp |
| Esiletõstmise täht | Ikoon kaardil | Kuvatakse ainult siis, kui `isPromoted=true` (mockupil esimese kaardi paremas ülanurgas) |
| "Vaata lähemalt" nupp | Nupp kaardil | Suunab `CourseView` vaatele, kaasas valitud koolituse `trainingId` |
| Lehekülje navigatsioon (Eelmine / 1 2 3 / Järgmine) | Navigatsioon | Leheküljenumbrid 1..`totalPages`, aktiivne lehekülg esile tõstetud. "Eelmine" on esimesel lehel ja "Järgmine" viimasel lehel mitteaktiivne |
| "Tulemusi ei leitud" teade | Tekst | Kuvatakse kaartide asemel, kui `trainingSummaries` on tühi list (mockupil pole, kuid tühi tulemus on normaalne olukord) |

Wireframe'i ülemine peamenüü (Koolitused, Teenused, Ettevõttest ▾ Lektorid, Blogi, Kontakt, Tagasiside, "Võta ühendust") ja logo kuuluvad jagatud navigatsioonikomponendi alla (`frontend/src/navigation/`), mitte `TrainingsView.vue` skoopi — see task peamenüüd ei kata.

`isOrderOnly` väli tuleb vastuses kaasa, kuid mockupil pole selle jaoks nähtavat elementi — seda selles taskis ei kuvata.

## Käitumine ja valideerimine

1. Vaate avanemisel (`beforeMount`) tehakse `GET /api/trainings` päring vaikeväärtustega: `categoryId=0`, `fundingTypeId=0`, `limit=3`, `page=0`, `trainingLang=et`, `contentLang=et` (vaikeväärtused vt "Lahtised otsad").
2. **Kõik kuus query parameetrit tuleb alati kaasa saata** — backend nõuab neid kõiki; kui mõni puudub, tagastab backend 400. Filter "kõik" väljendatakse `categoryId`/`fundingTypeId` puhul väärtusega `0`.
3. Kategooria või keele valiku muutmisel tehakse uus päring muudetud filtriga, `page` lähtestatakse väärtusele `0`.
4. "Otsi" nupule vajutamisel tehakse uus päring kehtivate filtritega, `page` lähtestatakse väärtusele `0`. Tühja otsingusõnaga vajutamine on lubatud — frontendipoolset sisendi valideerimist pole.
5. Leheküljenumbrile, "Eelmine" või "Järgmine" nupule vajutamisel tehakse uus päring samade filtritega, muutub ainult `page` (UI-s kuvatav number = `page + 1`). Vastuses `page` välja pole — frontend hoiab kehtivat lehekülje numbrit ise (`data` sees).
6. Vastuse saabumisel asendatakse kaartide nimekiri `trainingSummaries` sisuga ja lehekülje navigatsioon arvutatakse `totalPages` põhjal. Järjestuse määrab backend (esile tõstetud koolitused ees, seejärel pealkirja järgi) — frontend ei sorteeri ümber.
7. Kui `trainingSummaries` on tühi list, kuvatakse "Tulemusi ei leitud" teade ja lehekülje navigatsiooni ei kuvata. See ei ole veaolukord.
8. "Vaata lähemalt" nupule vajutamisel suunatakse kasutaja `CourseView` vaatele valitud koolituse `trainingId`-ga (täpne route muster selgub `CourseView` taskist — vt "Lahtised otsad").
9. API vea korral (400/500) suunatakse kasutaja üldisele veavaatele — teenusel pole kasutajale kuvatavaid `errorCode`-e.

## API kutsed

### `GET /api/trainings`

**Backend task:** vt `docs/tasks/backend/GET-api-trainings.md`

**Backend allikas:** `TrainingController.java` (meetod `findFilteredTrainings`), `TrainingService.java`, `TrainingSummaryRepository.java`, vastus `TrainingSummaryDto.java` + `TrainingSummaryItemDto.java` + `FundingTypeDto.java`. Kontrakt on võetud koodist, mis on mockupist ja backend taskist kohati erinev (vt märkust allpool).

Näidispäring:

```
GET /api/trainings?categoryId=0&fundingTypeId=0&limit=3&page=0&trainingLang=et&contentLang=et
```

Query parameetrid (**kõik kohustuslikud** — `@RequestParam` ilma `required=false`):

| Parameeter | Tüüp | Kirjeldus |
|---|---|---|
| `categoryId` | Integer | Filtreerib kategooria järgi; `0` = kõik kategooriad |
| `fundingTypeId` | Integer | Filtreerib rahastustüübi järgi; `0` = kõik. Selles vaates rahastustüübi filtrit pole, seega saadetakse alati `0` |
| `limit` | Integer | Lehekülje suurus |
| `page` | Integer | Lehekülje number, alates `0`-st |
| `trainingLang` | String (`et`/`en`) | Õppekeele filter — **täpne vaste**, väärtust "kõik keeled" pole |
| `contentLang` | String (`et`/`en`) | Tõlgitud väljade (`title`, `shortDescription`, `categoryName`, `fundingTypeName`) keel. Samuti täpne vaste — kui koolitusel selles keeles tõlget pole, siis tulemustesse seda ei tule |

`TrainingSummaryDto.java` — response (200):

```json
{
  "totalPages": 1,
  "totalElements": 2,
  "trainingSummaries": [
    {
      "trainingId": 1,
      "trainingLanguageCode": "et",
      "title": "Java algkursus",
      "shortDescription": "Java programmeerimise alused algajatele.",
      "categoryId": 1,
      "categoryName": "Programmeerimine",
      "isOrderOnly": false,
      "isPromoted": true,
      "fundingTypes": [
        {
          "fundingTypeId": 1,
          "fundingTypeName": "Töötukassa"
        }
      ]
    },
    {
      "trainingId": 2,
      "trainingLanguageCode": "et",
      "title": "Projektijuhtimise põhitõed",
      "shortDescription": "Sissejuhatus IT-projektijuhtimisse.",
      "categoryId": 3,
      "categoryName": "Juhtimine",
      "isOrderOnly": false,
      "isPromoted": false,
      "fundingTypes": []
    }
  ]
}
```

Väljade märkused:

- `totalPages`, `totalElements` — kogu filtreeritud tulemushulga kohta (lehitsemise jaoks), mitte ainult tagastatud lehe kohta.
- `trainingLanguageCode` — koolituse õppekeele kood (`et`/`en`), kuvatakse kaardil keele märgina.
- `fundingTypes` — võib olla tühi list (koolitusel pole rahastustüüpi).
- Järjestus: `isPromoted` kahanevalt, seejärel `title` järgi (fikseeritud backendis).

**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 400 Bad Request | — | Spring'i vaikimisi veavastus (nt puuduv parameeter või `page`/`limit`/`categoryId` ei ole number) | Suunatakse üldisele veavaatele (`NavigationService.navigateToErrorView()`) |
| 500 Internal Server Error | — | — | Suunatakse üldisele veavaatele (`NavigationService.navigateToErrorView()`) |

Tühi tulemus (olematu `categoryId`, tõlke puudumine vms) ei ole viga — vastus on 200 tühja `trainingSummaries` listiga.

**Märkus — kood vs mockup/backend task:**

- Mockup ja backend task kirjeldavad parameetreid valikulistena, kuid koodis on kõik kohustuslikud. Frontend peab alati kõik kuus saatma.
- Mockupi parameeter `translationLang` on koodis `contentLang`.
- Mockupi URL-is olevat `sort` parameetrit koodis pole (järjestus on fikseeritud).
- Mockupi response näidises on väli `"page": 0`, koodis seda pole. Koodis on lisaks väli `trainingLanguageCode`, mida mockupis ja backend taskis pole.
- Swaggeri annotatsioon lubab 400 korral `ApiError` kuju, kuid puuduva/vigase query parameetri korral tuleb tegelikult Spring'i vaikimisi veavastus (`RestExceptionHandler` neid eraldi ei töötle) — frontend `errorCode`-ile ei tugine.

## Komponendid ja failistruktuur

- **View:** `frontend/src/views/TrainingsView.vue` — ei eksisteeri veel (praegu on `views/` all `HomeView.vue`, `LoginView.vue`, `TestView.vue`).
- **Router:** `frontend/src/router/index.js` ei sisalda veel `/trainings` rada — tuleb lisada (nt `name: 'trainingsRoute'`, stiilis nagu olemasolevad `homeRoute`/`loginRoute`). Samuti puudub `CourseView` rada, kuhu "Vaata lähemalt" suunab.
- **Alamkomponendid (ettepanek):**
  - `frontend/src/components/common/TrainingCard.vue` — koolituse kaart (mockupi silt `TrainingCard.vue`); saab `props` kaudu ühe `trainingSummaries` elemendi ja emit'ib nt `event-training-selected` `trainingId`-ga (või suunab ise).
  - Kategooria ja keele dropdown'id — kas eraldi komponentidena `frontend/src/components/forms/` all või otse vaates (arendaja otsustada).
  - Lehekülje navigatsioon — soovi korral eraldi komponent `frontend/src/components/common/`, emit'ib valitud lehekülje (`event-page-selected`).
- **API teenus:** `frontend/src/api-services/TrainingService.js` (uus, stiilis nagu `LoginService.js`) — meetod nt `sendGetTrainingsRequest(categoryId, fundingTypeId, limit, page, trainingLang, contentLang)`, mis teeb `axios.get('/api/trainings', { params: {...} })`.
- **Veavaate suunamine:** `NavigationService.navigateToErrorView()` on konventsiooni muster (`docs/structure/frontend-vue-komponendi-struktuur.md`), kuid `NavigationService` ja veavaade frontendis veel puuduvad (`frontend/src/services/` on tühi).

## Lahtised otsad

- **Otsingusõna:** wireframe'il on otsinguväli, kuid backendis pole ühtegi otsingusõna parameetrit (nt `search`/`keyword`). Otsinguväli tehakse UI-sse, aga sõna päringusse ei lähe, kuni backend seda ei toeta — täpsusta enne implementeerimist (backend taski täiendus).
- **Kategooria valikute allikas:** sellel lehel pole kategooriate nimekirja API märget ja backendis pole kategooriate teenust. Seni võib valikud kirjutada vaatesse käsitsi andmebaasi järgi (0 = Kõik, 1 = Programmeerimine, 2 = Disain, 3 = Juhtimine) — täpsusta, kas luuakse eraldi teenus (nt `GET /api/categories`).
- **Vaikeväärtused:** `limit=3` on võetud mockupil nähtava kaartide arvu järgi, `trainingLang=et` ja `contentLang=et` eeldusel, et vaikimisi on eestikeelne leht — täpsusta enne implementeerimist. Kasutajaliidese keele vahetajat mockupil pole, seega `contentLang` on selles taskis fikseeritud.
- **Keelefilter "kõik":** backend ei võimalda pärida kõigis keeltes koolitusi korraga, seega keele dropdownil peab alati olema valitud "Eesti" või "Inglise". Kui "Kõik keeled" valikut on vaja, tuleb backendi muuta.
- **`CourseView` suunamine:** `CourseView` vaadet, rada ega taski veel pole — suunamise route muster (nt `/course?trainingId=1`) täpsustatakse `CourseView` taskis.

## Vastuvõtu kriteeriumid

- [ ] `/trainings` rada on olemas ja avab `TrainingsView.vue` ilma sisselogimisnõudeta
- [ ] Vaate avanemisel laaditakse ja kuvatakse esimene koolituste lehekülg (`page=0`)
- [ ] Iga `GET /api/trainings` päring saadab kõik kuus query parameetrit (`categoryId`, `fundingTypeId`, `limit`, `page`, `trainingLang`, `contentLang`)
- [ ] Kategooria valiku muutmine teeb uue päringu (`categoryId`, "Kõik" = `0`) ja lähtestab lehekülje
- [ ] Keele valiku muutmine teeb uue päringu (`trainingLang` = `et`/`en`) ja lähtestab lehekülje
- [ ] Otsinguväli ja "Otsi" nupp on olemas; "Otsi" teeb uue päringu kehtivate filtritega (otsingusõna parameeter vt "Lahtised otsad")
- [ ] Iga kaart kuvab pealkirja, õppekeele märki (`trainingLanguageCode` → "Eesti"/"Inglise"), lühikirjeldust, kategooria silti ja rahastustüüpide silte (kui neid on)
- [ ] Täht-ikoon kuvatakse ainult koolitustel, kus `isPromoted=true`
- [ ] Koolitused kuvatakse backendi antud järjekorras
- [ ] Lehekülje navigatsioon kuvab leheküljed 1..`totalPages`, tõstab aktiivse esile ja lehe vahetamine teeb uue päringu uue `page` väärtusega; "Eelmine"/"Järgmine" on äärtel mitteaktiivsed
- [ ] "Vaata lähemalt" suunab `CourseView` vaatele valitud koolituse `trainingId`-ga
- [ ] Tühja `trainingSummaries` korral kuvatakse "Tulemusi ei leitud" teade ja navigatsiooni ei kuvata
- [ ] API vea korral (400/500) suunatakse kasutaja üldisele veavaatele
