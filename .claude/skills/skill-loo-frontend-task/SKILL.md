---
name: skill-loo-frontend-task
description: Loo frontend vaate taski MD fail mockupi (wireframe) PDF-i konkreetse lehekülje põhjal — vaade ja kasutajaliides tulevad mockupilt, kuid vaate API kutsete kontrakt tuletatakse eelistatult olemasolevast backend koodist või backend taskist, mitte ainult mockupi API märkmetest. Küsi kasutajalt PDF failinimi, lehekülje number ja (kui asjakohane) backend taski viide. Kasuta, kui kasutaja tahab luua frontend taski, vaate taski, mockup lehekülje põhjal FE taski, või mainib "loo frontend task" vms.
---

# Loo frontend vaate task mockupi ja olemasoleva backend'i põhjal

Loe mockupi PDF-i konkreetne lehekülg, tuvasta sellel kirjeldatud vaade (Vaate märkmed) ja selle tehtavad API kutsed (API märkmed), ning koosta selle kohta täielik taski MD fail koos vastava lehekülje pildiga. Salvesta `docs/tasks/frontend` kausta. Vaate ja kasutajaliidese kirjeldus tuleb alati mockupilt, aga iga API kutse kontrakt tuletatakse mockupi asemel eelistatult olemasolevast backend koodist või backend taskist — mockupi "API märkmeid" kasutatakse ainult siis, kui kumbagi pole (vt täpne prioriteetsuse järjekord sammus 5).

See skill on `skill-loo-backend-task` vaste frontendi jaoks — erinevus on selles, et frontend task kirjeldab kasutajaliidest ja kasutajavoogu, mitte teenuse äriloogikat.

## Steps

### 1. Küsi vajalikud andmepunktid

Kui kasutaja pole neid juba andnud, küsi korraga:

1. **PDF failinimi** — nt `docs/mock-wireframe/pdf/<projekti mockup>.pdf` (vaata `docs/mock-wireframe/pdf/` kaustast, mis PDF-id seal on, ja paku neid valikuna)
2. **Lehekülje number** — mille pealt task luua
3. **Kas mõne sellel lehel oleva API kutse kohta on juba backend task olemas?** Kui kasutaja teab faili(d), küsi need kohe (nt `docs/tasks/backend/POST-api-login.md`). Kui ta pole kindel, mainib, et otsid ise `docs/tasks/backend` kaustast sobivaid vasteid (backend taski failinimi tuletatakse teenuse meetodist ja path'ist, nt `GET /api/roles` → `GET-api-roles.md`) ja näitad need talle kinnitamiseks.

Kui leheküljel on mitu Vaate märkmete kasti (mitu erinevat vaadet samal lehel), küsi kasutajalt, millise vaate kohta konkreetselt task luua.

Oota vastust enne kui jätkad.

### 2. Loe PDF-i vastav lehekülg

Kasuta Read tööriista `pages` parameetriga, et lugeda ainult see üks lehekülg PDF-ist.

Leia leheküljelt **Vaate märkmete** kast (struktuur ja väljade tähendus on kirjeldatud failis `docs/mock-wireframe/kokkulepped/mock-wireframe-markmete-struktuur.md`, jaotis 1) ja loe sealt:

- `Roll` — kes vaadet näeb
- `Failinimi` — `.vue` komponendi nimi
- `Frontend rada` — Vue router path
- `Vaatega seotud lisainfo` — käitumisreeglid, tingimuslik sisu, suunamised jms

Leia leheküljelt ka kõik **API märkmete** kastid (sama fail, jaotis 2) — need kirjeldavad API kutseid, mida see vaade teeb.

Loe läbi ka wireframe ise — see annab visuaalse konteksti (väljad, nupud, paigutus), mida "Vaate märkmed" tekstina ei pruugi täielikult katta.

Kui kaustas `docs/mock-wireframe/markmed/` on olemas selle vaate märkmete fail `<vaate-nimi>-markmed.md` (vaate nimi kebab-case'is, nt `HomeView.vue` → `home-view-markmed.md`), loe ka see läbi — selles on ühes kohas kõik selle vaate märkmed (Vaate märkmed ja kõik API märkmed) puhtama, kergemini loetava tekstina ja see aitab kontrollida, et miski PDF-i lugemisel valesti ei tõlgendatud. Kui PDF-i ja vaate märkmete faili sisu lahknevad (nt erinev path, DTO väli, veateade või vaate käitumine), **peata**, näita kasutajale erinevust ja küsi, kumba kasutada.

### 3. Leia või loo pildifail

Kõik mockupi pildid hoitakse ühes kaustas `docs/mock-wireframe/pdf-images/` — backend ja frontend taskid viitavad samale failile, pilte ei dubleerita.

Pildi nimi on vaate nimi: "Vaate märkmete" `Failinimi` ilma `.vue` laiendita (nt `EntityView.vue` → `EntityView.png`). Sama vaate teiste lehtede pildid on vajadusel kujul `<Vaade>-<lehekülg>.png` (nt `EntityView-7.png`).

**3.1. Loo kandidaatpilt ajutisse kausta `/tmp/mockup-pildid/`** `pdftoppm`-iga (poppler-utils, 150 dpi; `.png` laiendi lisab `pdftoppm` ise):

```bash
mkdir -p /tmp/mockup-pildid
pdftoppm -f <lehekülg> -l <lehekülg> -singlefile -r 150 -png "docs/mock-wireframe/pdf/<pdf fail>" "/tmp/mockup-pildid/<Vaade>-<lehekülg>-uus"
```

Kui pildi loomine ebaõnnestub (nt `pdftoppm` puudub), **peata ja teavita kasutajat** — küsi, kas ta lisab pildi ise, või jätkatakse ilma pildita (sel juhul jäta taskist pildi rida `![Mockup](...)` välja ja lisa selle asemele märkus `> Mockupi pilt lisatakse hiljem.`).

**3.2. Kui selle vaate pilti veel pole** (`docs/mock-wireframe/pdf-images/<Vaade>*.png` ei leia midagi), liiguta kandidaat kohale nimega `<Vaade>.png` ja jätka ilma küsimata.

**3.3. Kui selle vaate pilte on juba olemas**, võrdle kandidaati baithaaval kõigi `<Vaade>*.png` failidega (`pdftoppm` annab sama lehe ja resolutsiooni korral alati identse faili):

```bash
for f in docs/mock-wireframe/pdf-images/<Vaade>*.png; do cmp -s "/tmp/mockup-pildid/<Vaade>-<lehekülg>-uus.png" "$f" && echo "IDENTNE: $f"; done
```

Kui mõni fail on identne, kasuta seda ilma küsimata ja maini kasutajale lühidalt, et sama pilt oli juba olemas.

**3.4. Kui ükski olemasolev pilt pole identne, peata ja lase kasutajal otsustada:**

1. Ava kasutajale Windowsi pildivaaturis olemasolev `<Vaade>.png` ja kandidaat (`explorer.exe` väljumiskood 1 on tavaline, see ei tähenda viga):

   ```bash
   explorer.exe "$(wslpath -w docs/mock-wireframe/pdf-images/<Vaade>.png)"
   explorer.exe "$(wslpath -w "/tmp/mockup-pildid/<Vaade>-<lehekülg>-uus.png")"
   ```

   Kui vaatel on ka teisi `<Vaade>-<lehekülg>.png` pilte, nimeta need kasutajale (ava ainult siis, kui ta soovib).
2. Loe mõlemad pildid Read tööriistaga ise läbi ja näita kasutajale lühikest võrdlustabelit: lehe pealkiri ja number, vormi olek ja näidisandmed, API märkmed (millised teenused), vaate märkmed (mis on erinev). Lisa ühe lausega soovitus (nt "sama olek, ainult teised näidisandmed ja API märkmeid pole → viita olemasolevale" või "teine olek ja teised API kutsed → lisa uus pilt").
3. Kui kaalutakse ülekirjutamist, otsi üles taskid, mis olemasolevale pildile viitavad (`rg -l "pdf-images/<Vaade>.png" docs/tasks`), ja näita neid.
4. Küsi kolm varianti:
   1. **Viita olemasolevale pildile** — uut pilti ei salvestata; task viitab `<Vaade>.png` failile (või kasutaja valitud `<Vaade>-<lehekülg>.png` failile).
   2. **Kirjuta pilt üle (uuenda)** — kandidaat kirjutatakse `<Vaade>.png` asemele; uus pilt kuvatakse ka kõigis punktis 3 leitud taskides.
   3. **Lisa uus pilt lehekülje numbriga** — kandidaat salvestatakse nimega `<Vaade>-<lehekülg>.png`.

Oota vastust enne kui jätkad.

**3.5.** Kustuta ajutine kaust (`rm -r /tmp/mockup-pildid`). Edaspidi nimetatakse taskis kasutatavat faili `<pildi fail>`.

### 4. Uuri frontendi konventsioone

Loe läbi:

- `docs/structure/frontend-projekti-struktuur.md` — kuhu millised failid kuuluvad (`src/views/`, `src/components/common|forms|modals|tables/`, `src/api-services/`, `src/navigation/`, `src/router/`)
- `docs/structure/frontend-vue-komponendi-struktuur.md` — Options API stiil (`data`/`computed`/`methods` järjekord ja kuju, `event-` eesliitega emits, `.then()/.catch()/.finally()` API päringu muster, `beforeMount` andmete laadimiseks)

Vaata olemasolevat koodibaasi (`frontend/src/`) — kas vaate fail (nt `HomeView.vue`) juba eksisteerib (tavaliselt platsihoidjana) ja kas router (`frontend/src/router/index.js`) sisaldab juba vastavat rada. Kui "Vaatega seotud lisainfo" viitab suunamisele rajale, mida router'is veel pole, too see taskis selgelt välja tähelepanekuna (ära ise routerit muuda).

### 5. Tuvasta iga API kutse kontrakt

Iga leitud API märkme kohta tuvasta kontrakti allikas järgmises **prioriteetsuse järjekorras** (suurima kaaluga allikas võidab, kui mitu on olemas):

1. **Suurim kaal — olemasolev backend realisatsioon koodibaasis.** Otsi `backend/src/main/java/` alt (nt `find backend/src/main/java -name "*Controller.java"` või `grep -rl` sobiva URL-i järgi) kõiki `*Controller.java` klasse ja tuvasta, kas mõni neist vastab API märkme HTTP meetodile+URL-ile — Java package'i nimi on projektiti erinev, seega ära eelda konkreetset teed. Kui sobiv Controller (ja selle request/response DTO-d) on juba päriselt implementeeritud, kasuta request/response struktuuri, väljade nimesid, tüüpe ja valideerimisreegleid otse sellest koodist (Controller + DTO klassid, sh nt `@NotNull`/`@Size` jms annotatsioonid ja teenuse/exception handleri veakäitumine), mitte taski dokumendist ega mockupist — reaalne kood on kõige ajakohasem tõde ja võib olla taski dokumendist ka lahknenud.
2. **Kui backend realisatsiooni ei ole veel (kontroller puudub või on ainult platsihoidja)** — kasuta backend taski. Backend taski failinimi on API märkme `API:` realt üheselt tuletatav (sama reegel nagu `skill-loo-backend-task` sammus 7: meetod + path, `/` → `-`, path variable'i `{}` sulud ära, nt `GET /api/roles` → `GET-api-roles.md`, `PUT /api/user/{userId}` → `PUT-api-user-userId.md`). Kontrolli, kas `docs/tasks/backend/<failinimi>` on olemas (või kasuta kasutaja sammus 1 antud viidet). Kui on, ava see ja kasuta sealt request/response DTO struktuuri, JSON näidiseid ja veaolukordade tabelit.
3. **Kui backend realisatsiooni ega backend taski ei leidu** — tuleta kontrakt otse mockupi "API märkmed" postist (samamoodi nagu `skill-loo-backend-task` seda teeks) ja lisa taski API kutse juurde selge märkus, et vastavat backend taski veel pole loodud (soovita see luua enne või paralleelselt `skill-loo-backend-task` abil).
4. **Kui "Vaatega seotud lisainfo" viitab funktsionaalsusele/API kutsele, mille kohta sellel lehel eraldi API märget ei ole** (nt staatiline sisu, mida hallatakse mujal) — jäta see selle taski skoobist välja ja maini seda lühidalt "Kasutajavoog" jaotises, selle asemel et oletada API kontrakti.

Kui backend realisatsioon (punkt 1) ja backend task/mockup (punktid 2-3) omavahel lahknevad (nt task dokument kirjeldab välja, mida koodis enam pole, või vastupidi), kasuta koodi ja too see lahknevus API kutse juures taskis selgelt märkusena välja — see on väike lahtine ots (vt samm 8), mitte põhjus peatuda.

Kopeeri iga kasutatava API kutse request/response JSON näidised ja veateated täies mahus frontend taski sisse (mitte ainult viide) — frontend task peab olema iseseisvalt loetav ilma backend taski avamata, ka juhul kui viide sellele lisatakse. Kui kontrakt tuletati koodist (samm 1), lisa API kutse juurde "**Backend allikas:**" märge viitega konkreetsele Controller/DTO klassile (nt `<Ressurss>Controller.java`, `<Ressurss>Dto.java`), mitte ainult "Backend task" viitele.

### 6. Tuleta taski pealkiri ja failinimi

**Pealkiri** (faili sees `# ...` real) peab kokkuvõtvalt kirjeldama, mida vaade/interaktsioon teeb kasutaja vaatenurgast (nt "Sisselogimine avalehel", "Uue kasutaja registreerimisvorm").

**Failinimi** tuletatakse "Vaate märkmete" `Failinimi` väljast (vaate `.vue` failist), mitte pealkirjast — PascalCase nimi teisendatakse kebab-case'iks:
- Iga suurtähe ette (v.a esimese) pane sidekriips ja muuda kõik tähed väiketähtedeks
- Formaat: `<vaate-nimi>.md`
- Näited: `HomeView.vue` → `home-view.md`, `LoginView.vue` → `login-view.md`, `EntityView.vue` → `entity-view.md`

Kui `docs/tasks/frontend/<vaate-nimi>.md` on juba olemas (sama vaade esineb mockupis sageli mitmel lehel eri olekutes), **peata ja küsi kasutajalt**:
- kas täiendada olemasolevat taski selle lehe olekuga (nt lisamise vormile lisandub muutmise olek),
- kas luua eraldi task lehekülje numbriga (`<vaate-nimi>-<lehekülg>.md`, nt `entity-view-27.md`),
- või katkestada.

Ära kirjuta olemasolevat faili üle ilma kasutaja kinnituseta.

### 7. Koosta taski MD fail

Struktuur (järgi täpselt):

```markdown
# <Pealkiri>

**Vaade:** `<ViewName>.vue`, route `<path>`

**Roll:** <Vaate märkmete Roll väli>

**Vaste mockupis:** "<PDF/vaate nimi>", lehekülg <NR>/<KOKKU> (vt pilt `<pildi fail>`)

![Mockup](../../mock-wireframe/pdf-images/<pildi fail>)

<Kui pilti veel pole, lisa siia selle asemel märkus, et pilt lisatakse hiljem.>

## Kasutajavoog

<2-5 lauset — mida kasutaja vaates algusest lõpuni teeb, tuginedes "Vaatega seotud lisainfo" väljale ja wireframe'ile. Kui osa mockupi sisust jääb selle taski skoobist välja (nt eraldi API kutseta staatiline sisu), maini seda siin lühidalt.>

## Kasutajaliidese elemendid

Tabel veergudega: Element | Tüüp | Kirjeldus/käitumine

<Iga vormiväli, nupp, rippmenüü, teade jms, mis mockupil nähtav, koos kohustuslikkuse ja rolliga.>

## Käitumine ja valideerimine

<Nummerdatud sammud: mis juhtub millise kasutajategevuse peale, mis valideeritakse frontendis enne API kutset (kui midagi), tingimuslikud olekud (loading/error/empty), suunamised pärast õnnestumist/ebaõnnestumist.>

## API kutsed

<Iga API kutse jaoks alapealkiri "### `<METOOD> <path>`">

**Backend task:** vt `docs/tasks/backend/<fail>.md` <või "Backend task puudub — kontraht tuletatud otse mockupist, vt märkust allpool.">

<RequestDtoName.java, kui on> — request body:
```json
...
```

<ResponseDtoName.java, kui on> — response (200):
```json
...
```

**Veateated:**

Tabel: Status code | errorCode | message | Frontend käitumine

## Komponendid ja failistruktuur

<Ettepanek failide/komponentide kohta vastavalt docs/structure/frontend-projekti-struktuur.md konventsioonile: view fail, alamkomponendid (forms/modals/tables/common), api-services fail. Maini, kas view fail juba eksisteerib platsihoidjana ja kas router sisaldab vastavat rada.>

## Vastuvõtu kriteeriumid

<Checkbox nimekiri (- [ ]), mis katab:>
- <Vaates on kõik mockupil nähtavad väljad/nupud olemas>
- <Õnnestunud API kutse(te) käitumine (andmete kuvamine/salvestamine/suunamine)>
- <Frontendi valideerimisreeglid, kui neid on>
- <Kirjeldatud veaolukordade kuvamine kasutajale>
- <Servaolukorrad (tühi nimekiri, puuduv query parameeter jms), kui asjakohane>
```

Ole taski sisu koostades sama põhjalik ja konkreetne nagu olemasolevad taskid — kui `docs/tasks/frontend/` kaustas on juba taske, kasuta neid stiilieeskujuna; muidu võta põhjalikkuse eeskujuks `docs/tasks/backend/` taskid.

### 8. Eristada suuri ja väikeseid lahtiseid otsi

Taski koostamisel tuleb ette kahte tüüpi ebaselgust — need tuleb käsitleda erinevalt:

**Suured lahtised otsad — peata ja küsi kasutajalt enne faili salvestamist:**
- API kutse jaoks pole ei backend koodi ega backend taski ning mockupi API märge on nii puudulik, et kontrakti ei saa sellest usaldusväärselt tuletada,
- mockupi "lisainfo" on iseenesest vastuoluline või API kontrakt on täiesti tuletamatu (PDF-i ja vaate märkmete faili lahknevus lahendatakse juba sammus 2),
- vaade viitab funktsionaalsusele/käitumisele, mida ei saa API märgetest, backend taskidest ega olemasolevast koodist üheselt tuletada, ja vale oletus muudaks suure osa taskist valeks.

Sellisel juhul näita kasutajale lühikokkuvõte tuvastatud vaatest, API kutsetest ja probleemist ning oota vastust enne jätkamist.

**Väikesed lahtised otsad — ära peata, märgi need taski sisusse ja jätka:**
- kosmeetilised/vormistuslikud detailid, mis ei mõjuta ülejäänud taski õigsust (nt lingi sihtkoht, mida mockup ei täpsusta; täpne veateate sõnastus, mida backend ise ei anna),
- olemasoleva koodibaasi tähelepanekud, mis on iseenesest selged (nt vaate fail või router'i rada veel puudub).

Sellised leiud lisa taski vastavasse sektsiooni selge märkusena (nt "täpsusta enne implementeerimist") ja jätka taski loomist katkestamata.

Ära oleta väljamõeldud käitumist kummalgi juhul — väike lahtine ots jääb taskis nähtavale märkusena, suur lahtine ots lahendatakse kasutajaga läbi rääkides enne salvestamist.

### 9. Loo `docs/tasks/frontend` kaust, kui puudub

### 10. Salvesta failid

`docs/tasks/frontend/<vaate-nimi>.md` — taski sisu. Pilt on juba sammus 3 ühisesse kausta loodud — ära kopeeri seda taski kausta.

### 11. Teavita kasutajat

Näita:
- Loodud failide asukohad
- Lühike kokkuvõte, mis vaatest/interaktsioonist task räägib
- Kõik lahtised otsad (puuduv pilt, puuduv route, puuduv backend task jms)
- Küsi, kas midagi jäi puudu, on ebatäpne, või vajab täiendamist

Kui leheküljel oli mitu Vaate märkmete kasti ja loodi ainult üks task, tuleta kasutajale meelde, et ülejäänud vaadete kohta saab soovi korral samamoodi eraldi taskid luua.

## Üldised reeglid

- Suhtle kasutajaga eesti keeles.
- Ära leiuta andmeid ega käitumist — kasuta alati mockupi, `docs/mock-wireframe/markmed/` failide, backend taskide ja olemasoleva koodibaasi (`frontend/src/`, `docs/database/`) reaalset sisu.
- API kontrakti allika prioriteetsus: olemasolev backend Controller/DTO realisatsioon koodis > backend task dokument > mockupi "API märkmed" (viimast kasuta ainult siis, kui kumbagi eelnevat pole). Kontrolli koodibaasi realisatsiooni ka siis, kui backend task juba olemas on — kood võib olla ajakohasem.
- Mockupi pilte hoitakse ainult kaustas `docs/mock-wireframe/pdf-images/` — ära kopeeri neid taski kausta ega loo olemasolevat vaate pilti ilma kasutaja nõusolekuta üle.
- Ära muuda olemasolevat koodi (router, view failid) — task kirjeldab soovitud lõpptulemust, mitte ei implementeeri seda.
- Kui vaate käitumine, API kontrakt või failistruktuur jääb ebaselgeks, küsi kasutajalt täpsustust selle asemel, et oletada — nii andmete kogumise ajal kui enne lõpliku faili salvestamist.
