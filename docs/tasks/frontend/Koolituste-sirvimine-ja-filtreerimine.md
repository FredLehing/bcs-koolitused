# Koolituste sirvimine ja filtreerimine

**Vaade:** `TrainingsView.vue`, route `/trainings`

**Roll:** Kõik rollid (sh külastajad, sisselogimist ei nõuta)

**Vaste balsamic mockupis:** "TrainingsView", lehekülg 1/1 (vt lisatud pilt `Koolituste-sirvimine-ja-filtreerimine.png`)

![Mockup](./Koolituste-sirvimine-ja-filtreerimine.png)

## Kasutajavoog

Kasutaja avab avalikult ligipääsetava koolituste nimekirja vaate (sisselogimist ei nõuta). Vaates kuvatakse leheküljestatud koolituste kaardid koos otsingu, kategooria- ja keelefiltriga. Otsingusõna sisestamisel ja "Otsi" nupule vajutamisel, samuti kategooria või keele filtri muutmisel, tehakse uus `GET /api/trainings` päring vastavate query parameetritega ning tulemus värskendatakse. Iga koolituse kaardi "Vaata lähemalt" nupule vajutades suunatakse kasutaja `CourseView` vaatele valitud koolituse kohta. Lehekülgede vahel liigutakse "Eelmine"/"Järgmine" nuppude ja otseste leheküljenumbrite abil.

Mockupi "Vaatega seotud lisainfo" mainis lisaks ka sortimist ja kalendris kuupäeva muutmist, kuid kuna kumbki pole wireframe'il tegelikult nähtav ega backend kontraktis olemas (vt `docs/tasks/backend/Koolituste-nimekirja-paring.md` "Avatud küsimused"), on need sellest taskist teadlikult välja jäetud.

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Otsinguväli | Tekstisisend | Vabateksti otsing (nt "AI-Arendaja"), täidetakse enne "Otsi" nupule vajutamist |
| "Otsi" nupp | Nupp | Käivitab uue `GET /api/trainings` päringu kehtiva otsingusõna ja filtritega |
| Kategooria valik | Dropdown (select) | Väärtused vastavad `category` kirjetele (nt AI, Tarkvaraarendus, Andmebaasid, Graafika); valikul tehakse uus päring `categoryId`-ga |
| Keel valik | Dropdown (select) | Väärtused Eesti/Inglise; valikul tehakse uus päring `trainingLang`-iga |
| Koolituse kaart (`TrainingCard.vue`) | Korduv element | Kuvab koolituse pealkirja, keele märgist (nt "Eesti"), lühikirjeldust, kategooria/rahastustüübi silte ja "Vaata lähemalt" nuppu; kui `isPromoted` on `true`, kuvatakse lisaks täht-ikoon |
| "Vaata lähemalt" nupp | Nupp | Suunab kasutaja `CourseView` vaatele, kandes kaasa valitud koolituse `trainingId` |
| Lehekülje navigatsioon (Eelmine / leheküljenumbrid / Järgmine) | Navigatsioon | Liigub `trainingSummaries` lehtede vahel (`page` parameeter) |

Wireframe'il nähtav ülemine peamenüü (Koolitused, Teenused, Ettevõttest ▾ Lektorid, Blogi, Kontakt, Tagasiside, "Võta ühendust") kuulub tõenäoliselt jagatud lehepäise/navigatsiooni komponendi (`frontend/src/navigation/`) alla, mitte `TrainingsView.vue` enda skoopi — see task ei kata peamenüü implementeerimist.

## Käitumine ja valideerimine

1. Vaate avanemisel (`beforeMount`) tehakse esialgne `GET /api/trainings` päring ilma filtriteta (va vaikimisi leheküljestus), et kuvada esimene koolituste lehekülg.
2. Kasutaja sisestab otsingusõna ja vajutab "Otsi" nupule, või muudab kategooria/keele filtrit → tehakse uus `GET /api/trainings` päring, mis asendab senise koolituste nimekirja ja lähtestab lehekülje (`page=0`).
3. Kui tulemuseks on tühi `trainingSummaries` list, kuvatakse kasutajale sõbralik "Tulemusi ei leitud" teade (mitte viga — tühi tulemus on backend kontrakti järgi normaalne, mitte veaolukord).
4. Lehekülje navigatsiooni nuppudel (Eelmine/Järgmine/leheküljenumber) tehakse uus `GET /api/trainings` päring sama otsingu/filtritega, aga muudetud `page` väärtusega.
5. "Vaata lähemalt" nupule vajutades suunatakse kasutaja `CourseView` vaatele valitud koolituse `trainingId`-ga (täpne route muster sõltub `CourseView` taskist, mida veel pole loodud).
6. Ootamatu vea korral (backend tagastab 400 või 500, vt "API kutsed" allpool) suunatakse kasutaja üldisele veavaatele, kuna teenusel pole hetkel ühtegi kasutajale kuvatavat spetsiifilist `errorCode`-i defineeritud.

**Frontendipoolset sisendvalideerimist otsinguväljal ei ole** — tühja otsingusõnaga "Otsi" nupule vajutamine on lubatud (tagastab filtreerimata nimekirja).

## API kutsed

### `GET /api/trainings`

**Backend task:** vt `docs/tasks/backend/Koolituste-nimekirja-paring.md` (backend realisatsiooni veel ei ole — `backend/src/main/java` alt ei leitud selle URL-iga kontrollerit, ainuke olemasolev on `LoginController.java`).

Query parameetrid (kõik valikulised):

| Parameeter | Kirjeldus |
|---|---|
| `categoryId` | Filtreerib kategooria järgi |
| `fundingTypeId` | Filtreerib rahastustüübi järgi |
| `limit` | Lehekülje suurus |
| `page` | Lehekülje number, alates 0-st |
| `trainingLang` | Filtreerib õppekeele järgi (`et`/`en`) |
| `contentLang` | Määrab tõlgitud väljade keele (`et`/`en`) |

`TrainingSummaryDto.java` — response (200):

```json
{
  "page": 0,
  "totalPages": 1,
  "totalElements": 2,
  "trainingSummaries": [
    {
      "trainingId": 1,
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
    }
  ]
}
```

**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 400 Bad Request | — (pole hetkel defineeritud) | — | Suunatakse üldisele veavaatele (`NavigationService.navigateToErrorView()`) |
| 500 Internal Server Error | — (pole hetkel defineeritud) | — | Suunatakse üldisele veavaatele (`NavigationService.navigateToErrorView()`) |

**Lahtine ots — otsinguväli:** Wireframe'il on otsinguväli ("Otsi" nupuga) ja "Vaatega seotud lisainfo" tekst viitab, et otsingusõna muutmisel tehakse uus `GET /api/trainings` päring — aga backend taski (`docs/tasks/backend/Koolituste-nimekirja-paring.md`) query parameetrite loetelus **ei ole ühtegi otsingusõna/pealkirja-põhist parameetrit** (nt `search`/`keyword`/`title`). See task kirjeldab otsinguvälja kui UI elementi, aga selle täpne query parameeter tuleb backend taski täiendades kokku leppida (soovitavalt `skill-loo-backend-task` või käsitsi backend taski muutmisega), enne kui otsingufunktsionaalsus reaalselt juhtmestada saab.

## Komponendid ja failistruktuur

- **View:** `frontend/src/views/TrainingsView.vue` — ei eksisteeri veel (praegu on `frontend/src/views/` all ainult `HomeView.vue` ja `TestView.vue`).
- **Router:** `frontend/src/router/index.js` ei sisalda veel `/trainings` rada — tuleb lisada `TrainingsView` importi ja marsruuti (router'it ennast selle taski käigus ei muudeta).
- **Alamkomponendid (ettepanek):**
  - `frontend/src/components/common/TrainingCard.vue` — korduv koolituse kaart (vastavalt mockupi `TrainingCard.vue` sildile)
  - `frontend/src/components/forms/CategoryDropdown.vue` ja `frontend/src/components/forms/LanguageDropdown.vue` — filtri dropdown'id (või üks ühine filtrikomponent, arendaja otsustada)
- **API teenus:** `frontend/src/api-services/TrainingService.js` — meetod nt `sendGetTrainingsRequest(searchParams)`, mis teeb `GET /api/trainings` päringu query parameetritega.

## Vastuvõtu kriteeriumid

- [ ] `/trainings` route on olemas ja avab `TrainingsView.vue`, ilma sisselogimisnõudeta
- [ ] Vaate avanemisel laetakse ja kuvatakse esimene koolituste lehekülg
- [ ] Kategooria ja keele filtri muutmine käivitab uue `GET /api/trainings` päringu ja värskendab nimekirja
- [ ] Otsinguväli + "Otsi" nupp on olemas kasutajaliideses (täpne query parameeter täpsustatakse backend taski täienduses, vt "Lahtine ots")
- [ ] Iga koolituse kaart kuvab pealkirja, lühikirjeldust, keelt, kategooriat, rahastustüüpe (kui olemas) ja "promoted" tähist (kui `isPromoted=true`)
- [ ] "Vaata lähemalt" nupp suunab kasutaja `CourseView` vaatele valitud koolituse kohta
- [ ] Lehekülje navigatsioon (Eelmine/Järgmine/leheküljenumbrid) töötab `page` parameetri põhjal
- [ ] Tühja tulemuse korral kuvatakse kasutajale selge "Tulemusi ei leitud" teade, mitte tühi/katkine vaade
- [ ] Ootamatu API vea korral (400/500) suunatakse kasutaja üldisele veavaatele
