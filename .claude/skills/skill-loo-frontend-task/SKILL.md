---
name: skill-loo-frontend-task
description: Loo frontend vaate taski MD fail balsamic mockup PDF-i konkreetse lehekülje põhjal — vaade ja kasutajaliides tulevad mockupilt, kuid vaate API kutsete kontrakt tuletatakse eelistatult olemasolevast backend koodist või backend taskist, mitte ainult mockupi API märkmetest. Küsi kasutajalt PDF failinimi, lehekülje number ja (kui asjakohane) backend taski viide. Kasuta, kui kasutaja tahab luua frontend taski, vaate taski, mockup lehekülje põhjal FE taski, või mainib "loo frontend task" vms.
---

# Loo frontend vaate task mockupi ja olemasoleva backend'i põhjal

Loe balsamic mockup PDF-i konkreetne lehekülg, tuvasta sellel kirjeldatud vaade ("Vaate märkmed" post-it) ja selle tehtavad API kutsed ("API märkmed" postid), ning koosta selle kohta täielik taski MD fail koos vastava lehekülje pildiga. Salvesta `docs/tasks/frontend` kausta. Vaate ja kasutajaliidese kirjeldus tuleb alati mockupilt, aga iga API kutse kontrakt tuletatakse mockupi asemel eelistatult olemasolevast backend koodist või backend taskist — mockupi "API märkmeid" kasutatakse ainult siis, kui kumbagi pole (vt täpne prioriteetsuse järjekord sammus 5).

See skill on `skill-loo-backend-task` vaste frontendi jaoks — erinevus on selles, et frontend task kirjeldab kasutajaliidest ja kasutajavoogu, mitte teenuse äriloogikat.

## Steps

### 1. Küsi vajalikud andmepunktid

Kui kasutaja pole neid juba andnud, küsi korraga:

1. **PDF failinimi** — nt `docs/balsamic/Minu Projekt - Avakuva koos sisselogimisega.pdf`
2. **Lehekülje number** — mille pealt task luua
3. **Kas mõne sellel lehel oleva API kutse kohta on juba backend task olemas?** Kui kasutaja teab faili(d), küsi need kohe (nt `docs/tasks/backend/Kasutaja-sisselogimine.md`). Kui ta pole kindel, mainib, et otsid ise `docs/tasks/backend` kaustast sobivaid vasteid ja näitad need talle kinnitamiseks.

Kui leheküljel on mitu "Vaate märkmed" postit (mitu erinevat vaadet samal lehel), küsi kasutajalt, millise vaate kohta konkreetselt task luua.

Oota vastust enne kui jätkad.

### 2. Loe PDF-i vastav lehekülg

Kasuta Read tööriista `pages` parameetriga, et lugeda ainult see üks lehekülg PDF-ist.

Leia leheküljelt kollane **"Vaate märkmed"** post-it (struktuur ja väljade tähendus on kirjeldatud failis `docs/balsamic/notes/balsamiq-markmete-struktuur.md`, jaotis 1) ja loe sealt:

- `Roll` — kes vaadet näeb
- `Failinimi` — `.vue` komponendi nimi
- `Frontend rada` — Vue router path
- `Vaatega seotud lisainfo` — käitumisreeglid, tingimuslik sisu, suunamised jms

Leia leheküljelt ka kõik **"API märkmed"** postid (sama fail, jaotis 2) — need kirjeldavad API kutseid, mida see vaade teeb.

Loe läbi ka wireframe ise — see annab visuaalse konteksti (väljad, nupud, paigutus), mida "Vaate märkmed" tekstina ei pruugi täielikult katta.

Kui kaustas `docs/balsamic/notes/` on olemas vastav `<Failinimi ilma .vue-ta>-markmed.md` fail (nt `HomeView-markmed.md`), loe ka see läbi — see sisaldab sama infot puhtama, kergemini loetava tekstina ja aitab kontrollida, et miski PDF-i lugemisel valesti ei tõlgendatud.

### 3. Leia pildifail

Pildid asuvad `docs/balsamic/pdf-images/` kaustas. Failinimi ei pruugi olla lehekülje number — praktikas on kasutatud ka vaate/PDF-i kirjeldavat nime (nt `Registreeri kasutajaks.png`). Otsi kaustast sobivat faili (lehekülje numbri, vaate nime või PDF-i failinime järgi).

Kui sobivat pilti ei leidu, **peata ja teavita kasutajat** — küsi, kas ta tahab pildi ise genereerida/lisada, või kas jätkata taski loomist ilma pildita (lisades taski algusesse märkuse, et pilt lisatakse hiljem). Ära ise PDF-ist pilte genereerima hakka.

### 4. Uuri frontendi konventsioone

Loe läbi:

- `docs/frontend/projekti-struktuur.md` — kuhu millised failid kuuluvad (`src/views/`, `src/components/common|forms|modals|tables/`, `src/api-services/`, `src/navigation/`, `src/router/`)
- `docs/frontend/vue-komponendi-struktuur.md` — Options API stiil (`data`/`computed`/`methods` järjekord ja kuju, `event-` eesliitega emits, `.then()/.catch()/.finally()` API päringu muster, `beforeMount` andmete laadimiseks)

Vaata olemasolevat koodibaasi (`frontend/src/`) — kas vaate fail (nt `HomeView.vue`) juba eksisteerib (tavaliselt platsihoidjana) ja kas router (`frontend/src/router/index.js`) sisaldab juba vastavat rada. Kui "Vaatega seotud lisainfo" viitab suunamisele rajale, mida router'is veel pole, too see taskis selgelt välja tähelepanekuna (ära ise routerit muuda).

### 5. Tuvasta iga API kutse kontrakt

Iga leitud "API märkmed" posti kohta tuvasta kontrakti allikas järgmises **prioriteetsuse järjekorras** (suurima kaaluga allikas võidab, kui mitu on olemas):

1. **Suurim kaal — olemasolev backend realisatsioon koodibaasis.** Otsi `backend/src/main/java/` alt (nt `find backend/src/main/java -name "*Controller.java"` või `grep -rl` sobiva URL-i järgi) kõiki `*Controller.java` klasse ja tuvasta, kas mõni neist vastab API märkme HTTP meetodile+URL-ile — Java package'i nimi on projektiti erinev, seega ära eelda konkreetset teed. Kui sobiv Controller (ja selle request/response DTO-d) on juba päriselt implementeeritud, kasuta request/response struktuuri, väljade nimesid, tüüpe ja valideerimisreegleid otse sellest koodist (Controller + DTO klassid, sh nt `@NotNull`/`@Size` jms annotatsioonid ja teenuse/exception handleri veakäitumine), mitte taski dokumendist ega mockupist — reaalne kood on kõige ajakohasem tõde ja võib olla taski dokumendist ka lahknenud.
2. **Kui backend realisatsiooni ei ole veel (kontroller puudub või on ainult platsihoidja)** — kasuta kasutaja antud backend taski viidet (samm 1) või `docs/tasks/backend` kaustast HTTP meetodi+URL-i järgi leitud sobivat faili. Ava see ja kasuta sealt request/response DTO struktuuri, JSON näidiseid ja veaolukordade tabelit.
3. Kui mitu backend taski faili näivad sobivat või nimi pole ühene, näita kasutajale leitud kandidaadid ja küsi, milline neist on õige.
4. **Kui backend realisatsiooni ega backend taski ei leidu** — tuleta kontrakt otse mockupi "API märkmed" postist (samamoodi nagu `skill-loo-backend-task` seda teeks) ja lisa taski API kutse juurde selge märkus, et vastavat backend taski veel pole loodud (soovita see luua enne või paralleelselt `skill-loo-backend-task` abil).
5. **Kui "Vaatega seotud lisainfo" viitab funktsionaalsusele/API kutsele, mille kohta sellel lehel eraldi "API märkmed" postit ei ole** (nt staatiline sisu, mida hallatakse mujal) — jäta see selle taski skoobist välja ja maini seda lühidalt "Kasutajavoog" jaotises, selle asemel et oletada API kontrakti.

Kui backend realisatsioon (samm 1) ja backend task/mockup (sammud 2-4) omavahel lahknevad (nt task dokument kirjeldab välja, mida koodis enam pole, või vastupidi), kasuta koodi ja too see lahknevus API kutse juures taskis selgelt märkusena välja — see on väike lahtine ots (vt samm 8), mitte põhjus peatuda.

Kopeeri iga kasutatava API kutse request/response JSON näidised ja veateated täies mahus frontend taski sisse (mitte ainult viide) — frontend task peab olema iseseisvalt loetav ilma backend taski avamata, ka juhul kui viide sellele lisatakse. Kui kontrakt tuletati koodist (samm 1), lisa API kutse juurde "**Backend allikas:**" märge viitega konkreetsele Controller/DTO klassile (nt `<Ressurss>Controller.java`, `<Ressurss>Dto.java`), mitte ainult "Backend task" viitele.

### 6. Tuleta taski pealkiri ja failinimi

Pealkiri peab kokkuvõtvalt kirjeldama, mida vaade/interaktsioon teeb kasutaja vaatenurgast (nt "Sisselogimine avalehel", "Uue kasutaja registreerimisvorm").

Failinimi tuletatakse pealkirjast samamoodi nagu backend taskides:
- Eestikeelsed täpitähed translitereeri (õ→o, ä→a, ö→o, ü→u, š→s, ž→z)
- Tühikud asenda sidekriipsuga
- Formaat: `<Pealkiri-Sidekriipsudega>.md`

### 7. Koosta taski MD fail

Struktuur (järgi täpselt):

```markdown
# <Pealkiri>

**Vaade:** `<ViewName>.vue`, route `<path>`

**Roll:** <Vaate märkmete Roll väli>

**Vaste balsamic mockupis:** "<PDF/vaate nimi>", lehekülg <NR>/<KOKKU> (vt lisatud pilt `<failinimi>.png`)

![Mockup](./<failinimi>.png)

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

<Ettepanek failide/komponentide kohta vastavalt docs/frontend/projekti-struktuur.md konventsioonile: view fail, alamkomponendid (forms/modals/tables/common), api-services fail. Maini, kas view fail juba eksisteerib platsihoidjana ja kas router sisaldab vastavat rada.>

## Vastuvõtu kriteeriumid

<Checkbox nimekiri (- [ ]), mis katab:>
- <Vaates on kõik mockupil nähtavad väljad/nupud olemas>
- <Õnnestunud API kutse(te) käitumine (andmete kuvamine/salvestamine/suunamine)>
- <Frontendi valideerimisreeglid, kui neid on>
- <Kirjeldatud veaolukordade kuvamine kasutajale>
- <Servaolukorrad (tühi nimekiri, puuduv query parameeter jms), kui asjakohane>
```

Ole taski sisu koostades sama põhjalik ja konkreetne nagu varasemas näidises (`docs/tasks/frontend/Sisselogimine-avalehel.md`, kui see fail on olemas) — kasuta seda stiilieeskujuna.

### 8. Eristada suuri ja väikeseid lahtiseid otsi

Taski koostamisel tuleb ette kahte tüüpi ebaselgust — need tuleb käsitleda erinevalt:

**Suured lahtised otsad — peata ja küsi kasutajalt enne faili salvestamist:**
- backend taski ei leitud või oli mitu sobivat kandidaati, mistõttu API kontrakti allikas on ebakindel,
- mockupi "lisainfo" on vastuoluline (nt PDF ja `docs/balsamic/notes/` fail lahknevad) või API kontrakt on täiesti tuletamatu,
- vaade viitab funktsionaalsusele/käitumisele, mida ei saa API märgetest, backend taskidest ega olemasolevast koodist üheselt tuletada, ja vale oletus muudaks suure osa taskist valeks.

Sellisel juhul näita kasutajale lühikokkuvõte tuvastatud vaatest, API kutsetest ja probleemist ning oota vastust enne jätkamist.

**Väikesed lahtised otsad — ära peata, märgi need taski sisusse ja jätka:**
- kosmeetilised/vormistuslikud detailid, mis ei mõjuta ülejäänud taski õigsust (nt lingi sihtkoht, mida mockup ei täpsusta; täpne veateate sõnastus, mida backend ise ei anna),
- olemasoleva koodibaasi tähelepanekud, mis on iseenesest selged (nt vaate fail või router'i rada veel puudub).

Sellised leiud lisa taski vastavasse sektsiooni selge märkusena (nt "täpsusta enne implementeerimist") ja jätka taski loomist katkestamata.

Ära oleta väljamõeldud käitumist kummalgi juhul — väike lahtine ots jääb taskis nähtavale märkusena, suur lahtine ots lahendatakse kasutajaga läbi rääkides enne salvestamist.

### 9. Loo `docs/tasks/frontend` kaust, kui puudub

### 10. Salvesta failid

1. `docs/tasks/frontend/<failinimi>.md` — taski sisu
2. `docs/tasks/frontend/<failinimi>.png` — koopia leitud pildifailist (kui see samm 3 põhjal olemas oli)

### 11. Teavita kasutajat

Näita:
- Loodud failide asukohad
- Lühike kokkuvõte, mis vaatest/interaktsioonist task räägib
- Kõik lahtised otsad (puuduv pilt, puuduv route, puuduv backend task jms)
- Küsi, kas midagi jäi puudu, on ebatäpne, või vajab täiendamist

Kui leheküljel oli mitu "Vaate märkmed" posti ja loodi ainult üks task, tuleta kasutajale meelde, et ülejäänud vaadete kohta saab soovi korral samamoodi eraldi taskid luua.

## Üldised reeglid

- Suhtle kasutajaga eesti keeles.
- Ära leiuta andmeid ega käitumist — kasuta alati mockupi, `docs/balsamic/notes/` failide, backend taskide ja olemasoleva koodibaasi (`frontend/src/`, `docs/database/`) reaalset sisu.
- API kontrakti allika prioriteetsus: olemasolev backend Controller/DTO realisatsioon koodis > backend task dokument > mockupi "API märkmed" (viimast kasuta ainult siis, kui kumbagi eelnevat pole). Kontrolli koodibaasi realisatsiooni ka siis, kui backend task juba olemas on — kood võib olla ajakohasem.
- Ära loo skripti/automatiseeringut PDF-ist piltide genereerimiseks — eelda, et `docs/balsamic/pdf-images/` sisu on juba olemas; kui pilti pole, küsi kasutajalt, ära genereeri ise.
- Ära muuda olemasolevat koodi (router, view failid) — task kirjeldab soovitud lõpptulemust, mitte ei implementeeri seda.
- Kui vaate käitumine, API kontrakt või failistruktuur jääb ebaselgeks, küsi kasutajalt täpsustust selle asemel, et oletada — nii andmete kogumise ajal kui enne lõpliku faili salvestamist.
