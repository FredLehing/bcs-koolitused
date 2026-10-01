# Koolituste sirvimine ja filtreerimine

**Vaade:** `TrainingsView.vue`, rada `/trainings`. **Roll:** kõik, ka külastaja.

Aluseks on [läbimäng](../../mock-wireframe/loo-mock-vaade/trainings-view/trainings-view-labimang.html), [skeemid](../../mock-wireframe/loo-mock-vaade/trainings-view/trainings-view-skeemid.md) ja [vaate märkmed](../../mock-wireframe/markmed/trainings-view-markmed.md). Varasem PDF on ajalooline; praeguse käitumise võrdluseks kasuta läbimängu ja skeeme.

## Paigutus ja eeltäitmised

Ülal „Meie koolitused | Koolituste kalender“ vahelehed. Vasakul filtrid järgmises järjekorras, paremal otsing, kaardid ja leheküljestus. Kitsal ekraanil on filtrid kaartide kohal.

| Filter | Algvalik | Valikute allikas |
|---|---|---|
| Koolituse keel (select) | „Kõik keeled“, `trainingLanguageId=0` | `GET /api/languages`, kõik õppekeeled |
| Koolituse kategooria (select) | „Kõik kategooriad“, `categoryId=0` | `GET /api/categories?contentLang=...` |
| Rahastus (raadionupud) | Märgitud „Kõik“, `fundingTypeId=0` | `GET /api/funding-types?contentLang=...` |

`contentLang` tuleb `languageStore`-ist (salvestatud kasutajaliidese keel, vaikimisi `et`). Õppekeele filter on sellest sõltumatu; `requiresTranslation=false` keeled jäävad samuti valikutesse. Enne valikute API vastuseid näidatakse ainult „Kõik“ algvalikuid, tühje näidiskirjeid ei kuvata. Select-väljade pealkirjad on seotud väljadega, rahastusel on `fieldset`/`legend`. Tekstid on eesti ja inglise keeles.

Alguses on `page=0`, `limit=5`, otsing tühi (avalehelt tulles eeltäidetud query `searchText` väärtusega). Kasutajale kuvatakse esimene leht numbriga 1.

## Käitumine

1. Avamisel küsitakse koolituste esimene leht ning kõigi kolme filtri valikud.
2. Filtri muutmine rakendub kohe, säilitab teised filtrid ja rakendatud otsingu, seab `page=0`. Sama väärtuse uuesti valimine päringut ei käivita. Kõik filtrid rakenduvad koos.
3. „Otsi“ või Enter seab `appliedSearchText=searchText.trim()`, lähtestab `page=0` ja küsib koolitused. Sisestatud ja rakendatud otsing on eraldi. Tühi otsing on lubatud. Kõik otsingusõnad peavad esinema tõstutundetult pealkirjas või lühikirjelduses; käändeid ei kohandata.
4. Otsingu tühjendamine käsitsi, ×, Esc, „Tühista otsing“ või „Näita kõiki koolitusi“ eemaldab ainult otsingu, filtrid säilivad. Rakendatud otsingu eemaldamisel küsitakse uus esimene leht. × ja Esc järel saab otsinguväli fookuse.
5. Lehekülje muutmine säilitab filtrid ja rakendatud otsingu.
6. Kasutajaliidese keele vahetus laadib uuesti koolitused, kategooriad ja rahastuse ning uuendab otsingu tooltippi. Õppekeelte loendit uuesti ei küsita. Filtrite ID-d, otsing ja lehekülg säilivad; tulemuste piiridest välja jäänud lehe korral küsitakse viimane olemasolev leht (`max(0, totalPages-1)`).
7. Vanem koolituste päring ei tohi asendada viimase valiku tulemust. Varasema kuvamiskeele kategooria- ja rahastusvastus ei asenda uue keele nimesid.
8. Tühi tulemus ei ole viga: otsinguga kuvatakse otsinguteksti sisaldav teade, soovitus ja „Näita kõiki koolitusi“ nupp; ilma otsinguta „Valitud filtritele vastavaid koolitusi ei leitud“. Tühja tulemuse teadet ei kuvata päringu laadimise ajal.
9. Ootamatu API viga suunab üldisele veavaatele (`NavigationService.navigateToErrorView()`).

„Tühjenda filtrid“ link on nähtav ainult siis, kui õppekeele, kategooria või rahastuse filter on aktiivne. Vajutus seab kõik kolm filtri ID-d ja page väärtuse 0-ks ning küsib koolitused ühe päringuga. Sisestatud ja rakendatud otsing säilivad; ainult otsing ei tee filtrite tühjendamise linki nähtavaks.

## Kaardid ja navigeerimine

`TrainingCard.vue` kasutab `/courses` kaartidega kooskõlas kompaktset kujundust: pealkiri, lühikirjeldus, kategooria ja rahastus, õppekeele lipp ning „Vaata lähemalt“. `isOrderable=true` lisab „Tellitav“ märgise, `isPromoted=true` tähe ja kollaka tausta. Admin näeb muutmise ikooni.

- Detailid → `trainingRoute`, `/training?trainingId={id}`.
- Admini muutmine → `trainingFormRoute`, `/training-form?trainingId={id}&trainingTranslationId={id}`.
- Kalender → `coursesRoute`, `/courses`.

## API kutsed

| Teenus | Parameetrid | Vastus |
|---|---|---|
| `GET /api/trainings` | `categoryId`, `fundingTypeId`, `trainingLanguageId` (0 = kõik), `contentLang`, `searchText`, `page`, `limit` | `TrainingSummaryDto`: `totalPages`, `totalElements`, `trainingSummaries` |
| `GET /api/categories` | `contentLang` | `CategoryDto[]`: `categoryId`, `categoryName` |
| `GET /api/funding-types` | `contentLang` | `FundingTypeDto[]`: `fundingTypeId`, `fundingTypeName` |
| `GET /api/languages` | — | `SystemLanguageDto[]`: ID, kood, nimi, lipukood ja keele omadused |

Backend tagastab ainult publitseeritud koolitused, millel on `contentLang` tõlge. Järjestus: esile tõstetud eespool, seejärel pealkiri. Vastusenäited on [vaate märkmetes](../../mock-wireframe/markmed/trainings-view-markmed.md) ning [API taskis](../backend/GET-api-trainings.md). Kohandatud äriveateateid pole.

```http
GET /api/trainings?categoryId=0&fundingTypeId=0&limit=5&page=0&trainingLanguageId=0&contentLang=et&searchText=
```

## Failid

- `frontend/src/views/TrainingsView.vue` — filtrite väärtused, laadimine ja sündmused.
- `frontend/src/components/forms/LanguagesDropdown.vue`, `CategoriesDropdown.vue`, `FundingTypesRadio.vue` — filtrite väljad.
- `frontend/src/components/TrainingCard.vue` — kaart.
- `frontend/src/components/common/TrainingsTabs.vue`, `PaginationNav.vue` — navigatsioon.
- `frontend/src/api-services/TrainingService.js`, `CategoryService.js`, `FundingTypeService.js`, `LanguageService.js` — API kutsed.
- `frontend/src/locales/et.json`, `en.json` — tekstid.

## Vastuvõtu kriteeriumid

- [ ] Filtrite pealkirjad, järjestus ja algvalikud vastavad läbimängule; tühje näidiskirjeid ei kuvata.
- [ ] Rahastuse „Kõik“ on alguses märgitud; õppekeele filter on kasutajaliidese keelest sõltumatu.
- [ ] „Tühjenda filtrid“ ilmub aktiivse filtri korral, lähtestab filtrid ja lehe ning säilitab otsingu.
- [ ] Filter rakendub kohe koos teiste filtrite ja otsinguga ning seab `page=0`.
- [ ] Otsing käivitub „Otsi“/Enteriga; tühjendamine säilitab filtrid.
- [ ] Keele vahetus säilitab valikud ja korrigeerib vajadusel lehekülge.
- [ ] Aegunud päring ei asenda uuema valiku tulemusi.
- [ ] Lehel on kuni viis kaarti, tühja tulemuse teated ja toimiv leheküljestus.
- [ ] Eesti ja inglise tekstid, seotud väljade pealkirjad ning õiged navigeerimislingid.
- [ ] ESLint ja tootmisbuild läbivad kontrolli.
