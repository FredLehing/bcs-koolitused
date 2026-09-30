# Admini koolituste tabel: otsing, filtrid, sorteerimine ja leheküljestus

**Vaade:** `AdminTrainingsView.vue`, route `/admin-trainings` (nimi nt `adminTrainingsRoute`)

**Roll:** Admin

**Vaste mockupis:** interaktiivne läbimäng `docs/mock-wireframe/loo-mock-vaade/admin-trainings-view/admin-trainings-view-labimang.html` (artifact https://claude.ai/artifact/DudKxB6fPxFGzpQg55hhDX)

> Mockupi pilt lisatakse hiljem (Balsamiq AI käsk: `admin-trainings-view-skeemid.md`, jaotis 9).

Taustaks: märkmed `docs/mock-wireframe/markmed/admin-trainings-view-markmed.md`, otsused ja skeemid `docs/mock-wireframe/loo-mock-vaade/admin-trainings-view/admin-trainings-view-skeemid.md`, tööde järjekord `admin-trainings-view-toode-jarjekord.md` (samas kaustas). Staatuse nupp on eraldi taskis `training-status-button.md`.

## Kasutajavoog

Admin avab navbari menüüst "Admin" → "Koolituste haldus" tabeli, kus on vaikimisi kõik aktiivsed koolitused (uuemad üleval). Nime järgi otsides pakub otsinguväli olemasolevaid nimesid. Kaardiga "Otsingu filtrid" (vaikimisi peidus) saab tabelit kitsendada, sh näha kustutatud koolitusi. Veeru pealkirjale klõpsates sorteeritakse backendis, lehtede vahel liigutakse tabeli all. Iga rea ikoonidega saab koolitust vaadata, muuta või kustutada, staatuse nupuga publitseerida, mustandisse liigutada või (kustutatud real) taastada. Pealkirja real on nupp "Lisa uus koolitus".

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Menüülink "Koolituste haldus" | Navbar link | `App.vue`, menüüs "Admin" (ainult adminile) "Lisa uus koolitus" all; uus i18n võti (nt `navbar.manageTrainings`) |
| Pealkiri "Koolituste haldus" | Tekst | Päise real vasakul |
| "Lisa uus koolitus" | Nupp (primary) | Päise real paremal → `/training-form`; kitsal ekraanil pealkirja all |
| Otsinguväli | Tekstisisend + `<datalist>` | Placeholder "Otsi koolituse nime järgi…"; ettepanekud `GET /api/training-titles`; ettepaneku valik ainult täidab välja |
| "Otsi" | Nupp | Käivitab otsingu (ka Enter) |
| Küsimärgi ikoon | Ikoon + tooltip | Nupu "Otsi" kõrval, sama muster nagu `TrainingsView`-s. Tooltip: "Otsitakse koolituse nimest. Iga sõna peab nimes esinema, käändeid ei kohandata. Nimede valikus on ainult aktiivsed koolitused — kustutatud koolituse leidmiseks vali filtrites Staatus „Kustutatud“." |
| "▾ Ava otsingu filtrid" / "▴ Peida otsingu filtrid" | Link-nupp | Avab/peidab filtrikaardi; `aria-expanded` |
| "N filtrit aktiivne" + "Tühjenda filtrid" | Märgis + link | Nähtav, kui rakendatud filtrid erinevad vaikimisi väärtustest (ka peidetud kaardiga) |
| Kaart "Otsingu filtrid" | Kaart | Sama stiil nagu TrainingFormView "Koolituse andmed"; vaikimisi peidus |
| Kategooria | Dropdown | "Kõik" (`0`) + `GET /api/categories` (`CategoriesDropdown.vue`) |
| Koolituse keel | Dropdown | "Kõik" (`0`) + `GET /api/languages` (`LanguagesDropdown.vue`) |
| Rahastus | Dropdown | "Kõik" (`0`) + `GET /api/funding-types` |
| Staatus | Dropdown | Aktiivsed (vaikimisi, `null`) / Mustand (`U`) / Publitseeritud (`P`) / Kustutatud (`D`) |
| Tellitav, Esile tõstetud | Dropdown | Kõik (`null`) / Jah (`true`) / Ei (`false`) |
| Tõlked | Dropdown | Kõik (`null`) / Olemas (`true`) / Puuduvad (`false`) |
| "Filtreeri", "Tühjenda filtrid" | Nupud | Kaardi all |
| Tabel | Tabel | Veerud vt allpool |
| Leheküljestus | `PaginationNav.vue` | "Eelmine", leheküljenumbrid, "Järgmine" |
| "Kokku N koolitust" | Tekst | `totalElements` |
| "Tulemusi ei leitud" | Tabeli rida | Tühja tulemuse korral |

**Tabeli veerud:**

| Veerg | Sisu | Sorteeritav (`sortBy`) |
|---|---|---|
| Lisatud | `createdAt` kujul `30/09/2026` (tooltip: täielik ajatempel) | `createdAt` |
| Uuendatud | `updatedAt` kujul `30/09/2026` | `updatedAt` |
| Koolituse nimi | `title` | `title` |
| Kategooria | `categoryName` | `categoryName` |
| Keel | õppekeele lipp `FlagIcon.vue` (`trainingLanguageFlagIconCode`) | `trainingLanguageCode` |
| Staatus | märgis Mustand / Publitseeritud / Kustutatud | `status` |
| Tõlked | roheline linnuke (`hasAllTranslations`) või punane rist; risti tooltip "Puudub: en" (`missingTranslationLanguageCodes`) | `hasAllTranslations` |
| Sätted | sinise täpiga read: `fundingTypes[].fundingTypeName`, "Tellitav" (`isOrderable`), "Esile tõstetud" (`isPromoted`); tühjal "—" | ei |
| Tegevused | ikoonid Vaata, Muuda (`EditTrainingLink.vue`), Kustuta (`TrainingDeleteButton.vue`); kustutatud real peidus | ei |
| (nupp) | `TrainingStatusButton.vue` — Publitseeri / Liiguta mustandisse / Taasta | ei |

Kustutatud rida on tuhmim (nt `text-body-secondary`).

## Käitumine ja valideerimine

0. **Rollikontroll.** Mitte-admin (`SessionStorageService.userIsAdmin()`) suunatakse `NotAuthorizedView`-le ja andmeid ei laadita (nagu `TrainingFormView`).
1. **Avamine (`beforeMount`).** Päringud `GET /api/languages`, `GET /api/categories`, `GET /api/funding-types`, `GET /api/training-titles` (kolm viimast `contentLang`-iga) ja `GET /api/admin-trainings` vaikimisi väärtustega: `searchText=""`, `categoryId=0`, `trainingLanguageId=0`, `fundingTypeId=0`, valikulised filtrid puuduvad, `sortBy=createdAt`, `sortDirection=desc`, `page=0`, `limit=10`.
2. **Olek.** Otsingusõnal ja filtritel on kaks koopiat: sisestatav (`searchText`, `filters`) ja rakendatud (`appliedSearchText`, `appliedFilters`). Päringusse lähevad ainult rakendatud väärtused; dropdowni muutmine päringut ei tee. `null` väärtusega parameetreid axios ei saada.
3. **Otsing.** "Otsi" või Enter → `appliedSearchText = searchText`, `page = 0`, päring (koos rakendatud filtritega). Frontendis sisendit ei valideerita; tühi otsing on lubatud.
4. **Filtrid.** "Filtreeri" → `appliedFilters = filters` koopia, `page = 0`, päring (koos rakendatud otsingusõnaga). "Tühjenda filtrid" → mõlemad vaikimisi, `page = 0`, päring. Kaardi peitmine filtreid ei muuda.
5. **Sorteerimine.** Klõps sorteeritava veeru pealkirjal: uus veerg → `sortBy = veerg`, `sortDirection = asc`; sama veerg → suund vahetub. Seejärel `page = 0` ja päring. Nool (▲/▼) on ainult aktiivsel veerul; pealkirjal `aria-sort`.
6. **Leheküljestus.** `PaginationNav` sündmus → `page = uus`, päring; kõik muu jääb samaks. Kui `totalPages = 0`, leheküljestust ei kuvata.
7. **Keele vahetus navbaris** (`watch: contentLang`) → uuesti kategooriad, rahastustüübid, nimede ettepanekud ja tabel; otsing, filtrid, sorteerimine ja leht jäävad alles.
8. **Vaata** → `/training?trainingId={trainingId}&trainingTranslationId={trainingTranslationId}` (`NavigationService.navigateToTrainingView`). **Muuda** → `/training-form?trainingId=…&trainingTranslationId=…` (`EditTrainingLink`). **Lisa uus koolitus** → `/training-form`.
9. **Kustuta.** `TrainingDeleteButton` (prügikasti ikoon) avab `ConfirmModal`-i ("Kas soovid koolituse „{title}“ kustutada? Koolitus kaob admini tabelist ja avalikust nimekirjast."), kinnitusel kutsub ise `DELETE /api/training/{trainingId}` ja emit'ib `event-training-deleted`. Vaade: eduteade "Koolitus kustutatud", tabel ja nimede ettepanekud uuesti; kui leht jäi tühjaks ja `page > 0`, siis `page - 1` ja uus päring.
10. **Staatuse nupp** (`TrainingStatusButton`, vt `training-status-button.md`) emit'ib `event-status-changed` → vaade näitab eduteadet, laadib tabeli uuesti; taastamise (`D` → `U`) järel ka nimede ettepanekud.
11. **Vead.** Laadimise ootamatu viga (400/500) → `NavigationService.navigateToErrorView()`. Nuppude komponendid käsitlevad oma vigu ise.

## API kutsed

### `GET /api/admin-trainings`

**Backend task:** `docs/tasks/backend/GET-api-admin-trainings.md` (uus).

Query parameetrid: `contentLang`, `searchText`, `categoryId`, `trainingLanguageId`, `fundingTypeId`, valikulised `status`, `isOrderable`, `isPromoted`, `hasAllTranslations`, `sortBy`, `sortDirection`, `page`, `limit`.

`AdminTrainingSummaryDto.java` — response (200):

```json
{
  "totalPages": 2,
  "totalElements": 13,
  "adminTrainingSummaries": [
    {
      "trainingId": 13,
      "trainingTranslationId": 23,
      "title": "Tehisaru töövahendid arendajale",
      "categoryId": 1,
      "categoryName": "Programmeerimine",
      "trainingLanguageCode": "et",
      "trainingLanguageFlagIconCode": "fi-ee",
      "status": "P",
      "isOrderable": true,
      "isPromoted": true,
      "createdAt": "2026-09-25T12:10:00Z",
      "updatedAt": "2026-09-26T06:00:00Z",
      "hasAllTranslations": false,
      "missingTranslationLanguageCodes": [
        "en"
      ],
      "fundingTypes": [
        {
          "fundingTypeId": 1,
          "fundingTypeName": "Töötukassa"
        },
        {
          "fundingTypeId": 2,
          "fundingTypeName": "EL rahastus"
        }
      ]
    }
  ]
}
```

**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 400 / 500 | — | — | `NavigationService.navigateToErrorView()` |

### `GET /api/training-titles`

**Backend task:** `docs/tasks/backend/GET-api-training-titles.md` (uus).

`TrainingTitleDto.java` — response (200): `[{ "trainingId": 1, "title": "Java algkursus" }, ...]`

**Veateated:** 500 → üldine veavaade.

### `DELETE /api/training/{trainingId}`

**Backend task:** `docs/tasks/backend/DELETE-api-training-trainingId.md` (uus). Kutsub `TrainingDeleteButton.vue`.

Response (200): NONE

**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 404 | `PRIMARY_KEY_NOT_FOUND` | "Ei leidnud primary keyd 'trainingId' väärtusega: 123" | `NavigationService.navigateToErrorView()` |

### `GET /api/categories`, `GET /api/funding-types`, `GET /api/languages`

**Backend taskid:** olemasolevad (`GET-api-categories.md`, `GET-api-funding-types.md`, `GET-api-languages.md`), kood on olemas. Kasutatakse filtrite valikutena.

### `PUT /api/training/{trainingId}/publish`, `/unpublish`, `/restore`

Kutsub `TrainingStatusButton.vue` — vt `training-status-button.md`.

## Mock-vastused

Kuni backend teenused valmivad, kasutatakse sama mustrit nagu `TrainingFormView` (vt `training-form-view.md`, "Mock-vastused"): päris axios-kutse on kommentaaris ja tagastatakse `mockResponse(MockDatabase.…)`.

- `MockDatabase.js`-i lisada koolitustele `createdAt`, `updatedAt` ja `status` (`3_import.sql` väärtused), meetodid `getAdminTrainings(params)` (filtreerimine, sorteerimine, leheküljestus nagu backend taskis), `getTrainingTitles(contentLang)`, `deleteTraining(trainingId)`, `restoreTraining(trainingId)`.
- Läbimängu (`admin-trainings-view-labimang.html`) skripti funktsioon `findAdminTrainings` on sobiv eeskuju mock-loogikale.

## Komponendid ja failistruktuur

| Fail | Uus / muudetakse | Sisu |
|---|---|---|
| `views/AdminTrainingsView.vue` | uus | Vaade, olek, laadimise päringud |
| `components/common/PaginationNav.vue` | uus, jagatud | Propsid `page` (0-põhine), `totalPages`; emit `event-page-changed` (uus leht). Bootstrap `pagination`. **Asendab ka `TrainingsView.vue` praeguse leheküljestuse markup'i** |
| `components/common/TrainingDeleteButton.vue` | uus | Propsid `trainingId`, `title`; prügikasti ikoon (Phosphor `PhTrash`), `ConfirmModal`, `DELETE` kutse; emit `event-training-deleted`; 404 → veavaade |
| `components/common/TrainingStatusButton.vue` | uus | Vt `training-status-button.md` |
| filtrikaart, sorteeritav veeru pealkiri | arendaja otsustada | nt `components/forms/AdminTrainingFilters.vue`, `components/common/SortableColumnHeader.vue` |
| `components/common/EditTrainingLink.vue`, `FlagIcon.vue`, `modals/ConfirmModal.vue`, `forms/CategoriesDropdown.vue`, `forms/LanguagesDropdown.vue` | olemas | Taaskasutus; dropdownidele on vaja valikut "Kõik" (`0`) — kontrolli, kas komponent seda juba toetab |
| `api-services/TrainingService.js` | muudetakse | + `sendGetAdminTrainingsRequest(params)`, `sendGetTrainingTitlesRequest(contentLang)`, `sendDeleteTrainingRequest(trainingId)` |
| `router/index.js` | muudetakse | + rada `/admin-trainings` |
| `App.vue` | muudetakse | Menüüsse "Admin" link "Koolituste haldus" ("Lisa uus koolitus" alla) |
| `locales/et.json`, `locales/en.json` | muudetakse | Vaate tekstid, menüülink |

Kuupäeva vorming `dd/MM/yyyy` tehakse väikese abifunktsiooniga (nt `services/DateService.js` või vaate meetod) — eraldi teeki pole vaja.

## Vastuvõtu kriteeriumid

- [ ] Menüüs "Admin" on "Lisa uus koolitus" all link "Koolituste haldus", mis avab `/admin-trainings`
- [ ] Mitte-admin suunatakse `NotAuthorizedView`-le
- [ ] Päise real paremal on nupp "Lisa uus koolitus" → `/training-form`
- [ ] Avamisel kuvatakse aktiivsed koolitused Lisatud järgi kahanevalt, 10 rida lehel, "Kokku N koolitust"
- [ ] Tabelis on kõik 10 veergu õiges järjekorras; kuupäevad kujul `30/09/2026`; lipp, staatuse märgis, linnuke/rist koos tooltip'iga, sätted täppidega
- [ ] Otsingunupu kõrval on küsimärgi ikoon, mille tooltip selgitab otsingut ja seda, et kustutatud koolitusi nimede valikus pole
- [ ] Otsing (nupp ja Enter) otsib ainult nimest, alustab lehelt 0 ja arvestab rakendatud filtreid; datalist pakub nimesid
- [ ] Filtrikaart on vaikimisi peidus; link vahetab teksti ja noole; filtrid rakenduvad ainult "Filtreeri" vajutamisel ja jäävad kehtima peidetud kaardiga; aktiivsete filtrite märk ja "Tühjenda filtrid" töötavad
- [ ] Staatuse filter "Kustutatud" näitab kustutatud koolitusi; nende real on ainult nupp "Taasta"
- [ ] Sorteerimine: 7 veergu, uus veerg kasvavalt, sama veerg vahetab suunda, nool ainult aktiivsel veerul, leht 0
- [ ] `PaginationNav` töötab siin ja `TrainingsView`-s (vana markup eemaldatud)
- [ ] Kustutamine: kinnitus → rida kaob, eduteade, tühjaks jäänud lehelt liigutakse eelmisele
- [ ] Keele vahetus laadib andmed uuesti, otsing/filtrid/sorteerimine/leht jäävad
- [ ] Tühja tulemuse korral "Tulemusi ei leitud"; laadimise viga → üldine veavaade
