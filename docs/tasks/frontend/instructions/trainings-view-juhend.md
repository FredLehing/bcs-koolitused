# Edenemismärge: TrainingsView (frontend)

**Taski fail:** `docs/tasks/frontend/trainings-view.md`
**Branch:** `Fred-TrainingsViewFront-Dev`
**Seis:** esimene töötav versioon on valmis — koolitused laetakse backendist ja kuvatakse kaartidena. Pooleli on kaardi parema veeru kujundus.

Märge on kirjutatud selleks, et tööd saaks jätkata uues Claude Code'i sessioonis (nt kodusest arvutist). Uue sessiooni alguses anna Claude'ile see fail lugeda ja palu jätkata juhendamist sammust "Järgmine samm".

---

## Mis on tehtud

| Fail | Sisu |
|---|---|
| `frontend/src/router/index.js` | Rajad `/trainings` (`trainingsRoute`) ja `/error` (`errorRoute`) |
| `frontend/src/api-services/TrainingService.js` | `sendGetTrainingsRequest(categoryId, fundingTypeId, limit, page, trainingLanguageId, contentLang)` — `axios.get` koos `params` objektiga |
| `frontend/src/views/TrainingsView.vue` | `data()` (kuus päringu välja + `totalPages`, `trainingSummaries`), `getTrainings()` → `.then` → `handleGetTrainings(response)`, `.catch` → `NavigationService.navigateToErrorView()`, päring `beforeMount` sees, kaardid `v-for`-iga |
| `frontend/src/components/TrainingCard.vue` | Prop `training: Object`, computed `trainingLanguageFlag` (`et` → `fi-ee`, muu → `fi-gb`), Bootstrapi kaart: päises pealkiri, kehas vasakul lühikirjeldus ja paremal veerus lipp + "Vaata lähemalt" |
| `frontend/src/views/ErrorView.vue` | Veateade ja nupp "Tagasi koolituste juurde" |
| `frontend/src/services/NavigationService.js` | `navigateToTrainingsView()`, `navigateToErrorView()` — `router.push({ name: ... })` |
| `frontend/src/main.js` | `flag-icons` CSS import (`flag-icons/css/flag-icons.min.css`) |

## Tehtud otsused (meeskonnaga kokku lepitud)

- **Keelemärk on lipp**, mitte sõna "Eesti"/"Inglise" (`flag-icons` raamatukogu; emoji-lippe ei kasutata, sest Windows neid ei kuva). Mockup on uuendatud; taskifaili tekst räägib veel sõnadest — vajab uuendamist.
- **`NavigationService.js` asub kaustas `src/services/`**, mitte `src/navigation/` (viimane on navigatsiooniriba komponentide jaoks).
- **`TrainingCard.vue` asub otse `src/components/` all** (nagu `FooterComponent.vue`).
- **Kaardi paigutus** (kaardi laius, veerud filtrite kõrval) on **vaate** (`TrainingsView`) otsustada — kaardi sees `container`/`row`/`col` klasse ei kasutata.

## Kontrakt (seisuga pärast master'i merge'i)

Tõeallikas on backendi kood (`TrainingController.java`, `TrainingSummaryItemDto.java`), mitte taskifaili näidis.

- Päring: `GET /api/trainings?categoryId=0&fundingTypeId=0&limit=3&page=0&trainingLanguageId=0&contentLang=et`
  - kõik kuus parameetrit on kohustuslikud; `trainingLanguageId` on **number**, `0` = kõik keeled
- Vastuse element: `trainingId`, `trainingLanguageCode`, `title`, `shortDescription`, `categoryId`, `categoryName`, `isOrderable`, `isPromoted`, `fundingTypes[]`
  - `trainingLanguageCode` on DTO-s olemas, kuigi taskifaili JSON näidisest on see välja jäänud

---

## Järgmine samm: kaardi parem veerg

Soovitud paigutus (ülevalt alla, kõik paremas servas):

```
┌─ parem veerg ──────────┐
│                   🇪🇪  │  ← lipp
│           [Tellitav]   │  ← ainult kui isOrderable = true
│     [Vaata lähemalt]   │  ← alati
└────────────────────────┘
```

1. **"Tellitav" element** lipu ja "Vaata lähemalt" vahele.
   - Täpsusta meeskonnaga: kas see on **nupp** (klikk teeb midagi) või **badge** (ainult info)?
   - Kuvatakse tingimuslikult `v-if` abil `training.isOrderable` põhjal. Mõtle, kas boolean-välja puhul on võrdlust üldse vaja.
2. **Parema veeru `<div>` vertikaalseks flexiks** — Bootstrapi *Utilities → Flex*: `d-flex`, suund üksteise alla (vt `App.vue`), joondus paremasse serva (*Align items*), vahed `gap-*` klassiga.
3. Kontroll: andmebaasis on koolitusel 1 `isOrderable = true` ja koolitusel 2 `false` — üks kaart peab "Tellitav" elementi näitama, teine mitte.

## Edasised sammud (taski järgi)

1. **Esiletõstmise täht** — kuvatakse ainult `isPromoted = true` korral (`v-if`); ikoon Phosphor Icons raamatukogust (vt importi `App.vue`-s; Options API komponendis tuleb ikoon registreerida `components` plokis).
2. **Kategooria silt** (`categoryName`) ja **rahastustüüpide sildid** (`fundingTypes[].fundingTypeName`) — viimane on list, st `v-for` kaardi sees.
3. **"Tulemusi ei leitud"** teade tühja `trainingSummaries` korral (`v-if` / `v-else`) — ilma selleta näeb tühi tulemus välja nagu katkine leht.
4. **Paigutus vaates** — vasakule filtrite veerg, paremale kaardid (Bootstrapi `row` / `col`).
5. **Kategooria ja keele filtrid** (dropdown'id); muutmisel uus päring ja `page = 0`.
   - Keele valikud peavad taski järgi tulema `GET /api/languages` teenusest — **seda endpointi veel pole**.
6. **Otsinguväli ja "Otsi" nupp** — backendis pole veel otsingusõna parameetrit.
7. **Lehekülje navigatsioon** — Eelmine / 1..`totalPages` / Järgmine; muutub ainult `page`.
8. **"Vaata lähemalt"** → `CourseView` — ootab `CourseView` taski (route muster pole teada); suunamine käib `NavigationService` kaudu.

## Lahtised küsimused

- "Tellitav": nupp või badge? Mida klikk teeb?
- Taskifaili uuendamine: lipp sõna asemel, `isOrderable` kuvamine, `trainingLanguageCode` JSON näidisesse tagasi.
- `GET /api/languages` ja kategooriate allikas filtrite jaoks.

## Meelespea

- **Pärast master'i merge'i:** kui `docs/database/` skriptid muutusid → reset DB (1 → 2 → 3); kui `package.json` muutus → `npm install`; taaskäivita backend.
- **Pärast `router/index.js` muutmist** võib Vite'i hot reload tekitada kaks routerit (URL muutub, vaade mitte) → sulge brauseri tab või vajuta F5.
- **Päringu parameetrid vs vastuse väljad** on eri asjad: `trainingLanguageId` läheb päringuga välja, `trainingLanguageCode` tuleb vastusega sisse.
- **Alt+Enter "parandused"** — IntelliJ võib vale nime peale lisada importi või `computed` ploki, mis impordib vaate iseendasse. Loe enne kinnitamist, mida IntelliJ teha kavatseb.
- JavaScript ei hoiata vale väljanime korral (väärtus on lihtsalt `undefined`) — kontrolli väljanimesid backendi DTO-st.
