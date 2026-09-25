---
name: skill-uus-mockup-markmed
description: Koosta mockup'i (wireframe'i, nt Balsamiq) vaate juurde käivad märkmed — Vaate märkmed või API märkmed — ja salvesta need vaate märkmete faili docs/mock-wireframe/markmed/ kaustas. Kasuta, kui kasutaja tahab luua või uuendada vaate märkmeid, API märkmeid, mockupi märkmeid (ka kui ta ütleb "mockupi silt" või "balsamic silt").
---

# Loo mockupi märkmed

**Taust:** Seda skilli kasutavad õpilased oma tiimiprojekti **planeerimisfaasis** — pärast seda, kui nad on toorikprojektist loonud oma projekti (vt `skill-uus-projekt`), aga **enne** kui domeeni `controller`/`service` klasse on kirjutatud. Sel hetkel on olemas ainult toorikprojekti baasstruktuur (sh `infrastructure/` kaust) ja andmebaasi skeem/näidisandmed — äriloogika kood tuleb alles hiljem. Nende märkmete täpsus kandub otse edasi: need on üks kolmest omavahel sünkis peetavast allikast (mockup, backend task, backend kood — vt struktuuridokumendi kokkuvõtet) ning on hiljem sisendiks task-failide (`skill-loo-backend-task`) ja koodi kirjutamise (`skill-rain-ai-backend`) faasile.

Eesmärk: panna olemasolevatest andmetest (andmebaasi skeem, näidisandmed, kasutaja antud info) kokku mockupi vaate juurde käivad märkmed — kas **Vaate märkmed** või **API märkmed** — täpselt struktuuris, mis on kokku lepitud failis `docs/mock-wireframe/kokkulepped/mock-wireframe-markmete-struktuur.md`.

**Terminid** (kasuta neid järjekindlalt ka kasutajaga suheldes):
- **Vaate märkmed** ja **API märkmed** — kaks märkmete tüüpi (nagu struktuuridokumendis).
- **märge** — üks konkreetne märkmete plokk (nt "üks API märge" = ühe backend kutse API märkmed).
- **kast** — mockupi kollane/valge kast, kuhu märkme tekst copy-pastitakse.
- **vaate märkmete fail** — `docs/mock-wireframe/markmed/<vaate-nimi>-markmed.md`, kus on kõik ühe vaate märkmed.

## 1. Uuri kõigepealt struktuuridokumenti

Loe alati **enne** sisu koostamist läbi `docs/mock-wireframe/kokkulepped/mock-wireframe-markmete-struktuur.md`. See defineerib mõlema märkmete tüübi täpse struktuuri, väljad ja reeglid. Ära tugine mällu jäänud struktuurile ega varasemale näitele — loe fail iga kord uuesti, sest struktuur võib olla vahepeal muutunud.

## 2. Küsi kasutajalt, mida ta soovib

Kui pole juba selge, küsi kasutajalt:

1. Kas soovitakse koostada **uut** märget või **uuendada/täiendada** olemasolevat.
2. Kumb märkmete tüüp — Vaate märkmed (üks vaate kohta) või API märkmed (üks backend kutse kohta). Kui vaade teeb mitu API kutset, tuleta kasutajale meelde, et iga kutse kohta tuleb eraldi API märge (ja mockupis eraldi kast).
3. **Millise vaate juurde märge käib** (nt `HomeView.vue`) — kõik ühe vaate märkmed hoitakse vaate märkmete failis (vt samm 10), seega ka üksiku API märkme puhul peab vaade olema teada. Kui sama API kutset kasutab mitu vaadet, lisatakse API märge iga sellise vaate märkmete faili.

Kui tegemist on uuendamisega, küsi kasutajalt (kui pole juba antud), mille põhjal olemasolevat märget leida/täiendada — kasutaja võib anda:
- olemasoleva `.md` faili path'i `docs/mock-wireframe/markmed/` kaustas,
- vabatekstina kirjelduse/muudatuse, mida tuleb arvesse võtta,
- screenshoti/pildi konkreetsest mockupi vaatest.

Kõiki neid sisendvorme tuleb aktsepteerida — kui kasutaja annab screenshoti, loe sellelt olemasolev tekst välja samamoodi nagu uue märkme koostamisel lähtematerjali loetakse.

## 3. Küsi sisu kohta — peata ja oota vastust

Pärast sammu 2 **peata alati** ja küsi kasutajalt märkmete sisu kohta, vastavalt valitud tüübile. Kasutaja võib vastata vabas vormis, failide path'idena (nt DTO, controller, task) või mockupi screenshotina — kõik vormid sobivad. Ütle kasutajale, et kui ta mõnda asja ei tea, pakud selle ise andmebaasi ja koodi põhjal välja (samm 4).

**Vaate märkmete puhul küsi:**
1. **Roll** — kes vaadet näeb (kõik rollid / admin / customer / külastaja)? Kas eri rollid näevad erinevat sisu?
2. **Sisu** — mis vaates on (väljad, nupud, tabelid, rippmenüüd)? Mockupi screenshot on siin kõige parem.
3. **Käitumine** — mis juhtub nuppude peale, mis on tingimuslikult nähtav või peidetud, kuhu kasutaja pärast tegevust suunatakse?
4. **API kutsed** — milliseid backend kutseid vaade teeb? (Nende kohta saab järgmisena teha API märkmed.)

Kontrolli ka vaate nime vastavust URL-ide kokkuleppe vaate nimetamise reeglile (struktuuridokumendi jaotis 3): kui vaates on vorm, millega saab uusi andmeid lisada ja/või olemasolevaid muuta, peab nimi olema kujul `<Ressurss>FormView.vue` (nt `LocationFormView.vue`); nimekirja vaade `<Ressurss mitmuses>View.vue` (`LocationsView.vue`); ühe objekti vaatamise vaade `<Ressurss>View.vue` (`LocationView.vue`). Kui kasutaja antud nimi reeglile ei vasta, paku reeglipärane nimi välja ja küsi kinnitust.

`Frontend rada` tuletad vaate failinimest (samm 4) ja palud kinnitada.

**API märkmete puhul küsi:**
1. **Eesmärk** — mida teenus teeb (1–2 lauset)?
2. **Kuju** — kas teenus tagastab/muudab **ühe** objekti või **nimekirja**? Kas see loob, muudab või kustutab? (Sellest tuleb HTTP meetod ja path'i ainsus/mitmus, vt samm 4.)
3. **Sisend** — mida teenus saab (ID, filtrid, vormi väljad)?
4. **Väljund** — mida teenus tagastab?
5. **Äriloogika reeglid** — mida peab kontrollima (nt "sama nimega ei tohi olla")? Neist tulevad veaolukorrad.
6. **Seotud tabelid** — millised andmebaasi tabelid on seotud (kui kasutaja teab)?

Oota vastust. Loe kõik viidatud failid/pildid üle enne sisu koostamist. Kui info on pärast vastust ikka ebapiisav mõne kohustusliku välja täitmiseks (struktuuridokumendi järgi), küsi täpsustust selle asemel, et oletada.

## 4. Uuri andmebaasi ja olemasolevat koodi, et saada õiged väärtused

Kasuta järgmisi allikaid, et JSON näidised, ID-d ja väljad oleks kooskõlas päris andmebaasi seisuga (mitte väljamõeldud):

- `docs/database/2_create.sql` — tabelistruktuur, veerud, tüübid, PK/FK seosed. Sellest tuletad ka väljanimed ja seosed DTO-de vahel.
- `docs/database/3_import.sql` — reaalsed näidisandmed (INSERT read). Kasuta neid JSON näidiste väärtusteks (id-d, nimed), mitte väljamõeldud andmeid.

Kui kasutaja on juba andnud konkreetse JSON näidise (nt tekstina või pildilt), eelista seda, aga kontrolli väärtuste (id-de, nimede) kooskõla `3_import.sql`-ga — kui ei klapi, teavita kasutajat lahknevusest ja küsi, kumba kasutada.

Kui planeeritava API kutsega seotud DTO/entity `.java` failid **juba eksisteerivad** koodibaasis (nt varasema taski käigus loodud, või mockup-faasis juba ette kirjutatud) — otsi need üles (nt `backend/src/main/java/**/dto/`, `backend/src/main/java/**/persistence/`) ja kasuta neist täpseid väljanimesid, tüüpe ja DTO klassinime, mitte ära tuleta neid ainult SQL-tabelinimedest. Kui vastavaid faile veel pole (tavaline olukord planeerimisfaasis, vt taustalõik üleval), tuleta väljanimed ja DTO klassinimi `2_create.sql` struktuurist ja kasutaja antud infost, järgides projekti olemasolevat DTO nimetamise stiili (nt `<Subjekt>Dto.java`, `<Subjekt>CreateRequestDto.java` — vaata mõnda olemasolevat DTO-t mujalt koodibaasist eeskujuks, kui vähegi mõni on).

Kui kasutaja ei ole täpset API path'i/HTTP meetodit andnud, kontrolli seda kõigepealt olemasolevast controller-klassist (kui vastav endpoint juba eksisteerib). Kui controllerit veel pole, otsi vastavust backend taski failidest (`docs/tasks/backend/*.md`, kui olemas) — struktuuridokumendi kokkuvõte defineerib mockupi, backend taski ja koodi kui omavahel sünkis olevad kirjeldused samast asjast. Kui ka need puuduvad (tavaline planeerimisfaasis), tuleta path ja meetod **URL-ide kokkuleppe** järgi (`docs/mock-wireframe/kokkulepped/mock-wireframe-markmete-struktuur.md`, jaotis 3): alati `/api/<ressurss>` ilma valdkonna eesliiteta, ressursi nimi andmebaasi tabelist, ühe objekti teenus ainsuses (`GET /api/location/{locationId}`, `POST /api/location`) ja nimekirja teenus mitmuses (`GET /api/locations`). **Paku tuletatud path kasutajale koos lühikese põhjendusega välja** (nt "tagastab ühe objekti → ainsus") **ja küsi kinnitust** enne, kui selle märkmesse kirjutad.

Vaate märkmete `Frontend rada` tuleta samuti sama kokkuleppe järgi vaate failinimest (`LocationView.vue` → `/location`, `LocationsView.vue` → `/locations`, `LocationFormView.vue` → `/location-form`, `HomeView.vue` → `/`), kui kasutaja pole muud öelnud.

## 5. API märgete veateated — tuleta reaalsest error-infrastruktuurist

API märgete `Veateated:` plokki ära kunagi väljamõeldud/oletusliku sisuga täida — tuleta iga veajuhtum otse koodist, mitte ainult controlleri `@ApiResponse` kommentaaridest (need võivad olla aegunud või mittetäielikud).

Backendi error-infrastruktuur (`infrastructure/` kaust koos sisuga) eksisteerib projektis juba algusest peale, ka enne kui ühtegi domeeni controller/service klassi on kirjutatud — see on osa toorikprojektist, mille pealt uued meeskonnaprojektid luuakse. Package tee ise (nt `ee.minuprojekt` vs `ee.mingiprojekt`) erineb projektiti, seega leia see esmalt dünaamiliselt üles, mitte ära eelda konkreetset teed:

```bash
find backend/src/main/java -maxdepth 6 -type d -path "*/infrastructure"
```

Selle kausta seest loe läbi:
- error response'i DTO klass (tavaliselt `infrastructure/error/` all, nt `ApiError.java`) — response body kuju (nt `message`, `errorCode` väljad).
- tsentraalne veakäsitleja (tavaliselt `@ControllerAdvice`/`@ExceptionHandler` annotatsiooniga klass, nt `RestExceptionHandler.java`) — kaardistab iga erindi tüübi HTTP staatuskoodiks (nt `ForbiddenException` → 403, `DataNotFoundException` → 404, `PrimaryKeyNotFoundException` → 404, valideerimisviga → 400 `errorCode: INCORRECT_INPUT`).
- kohandatud erindite klassid (tavaliselt `infrastructure/exception/` all) — igaüks kannab `message` ja `errorCode` väljad; mõni erind (nt `PrimaryKeyNotFoundException`) genereerib `message` fikseeritud mustri järgi konstruktori argumentidest automaatselt, mõni kannab fikseeritud teksti.

Konkreetne `controller`/`service` klass (nt `LocationService`), mida päritav API endpoint kutsub, ei pruugi taski koostamise hetkel veel eksisteerida (uue projekti/taski algfaasis). Kui see juba on olemas, kontrolli sealt, milliseid erindeid (ja millise `message`/`errorCode` sisuga) just see konkreetne meetod tegelikult viskab. Kaks operatsiooni, mis tunduvad sarnased (nt POST vs PUT samale ressursile), ei pruugi visata samu vigu — kontrolli iga meetodit eraldi, ära kopeeri veateateid ühelt operatsioonilt teisele eeldades sarnasust.

Kui vastavat service meetodit veel pole (planeerimisfaas, vt taustalõik üleval), ei tohi veajuhtumeid välja mõelda vabalt — vaata esmalt, kas mõnel muul juba olemasoleval sarnasel endpointil (nt sama tüüpi ressursi otsing/loomine mujal koodibaasis) on juba analoogne veajuhtum, ning kasuta seda mustrit (nt `PRIMARY_KEY_NOT_FOUND` foreign key puudumisel on korduv muster kogu koodibaasis, vt struktuuridokumendi näited). Kui analoogiat pole ja info jääb ikka ebapiisavaks, küsi kasutajalt täpsustust, milliseid veajuhtumeid see endpoint peaks käsitlema — ära oleta.

Kui meetod ei viska ühtegi kohandatud erindit (nt valideerimist pole implementeeritud), kirjuta `Veateated: —`, isegi kui controlleri dokumentatsioon/kommentaarid (nt `@ApiResponse`-tüüpi annotatsioonid) väidavad teisiti — kood on tõde, dokumentatsioon (sh kommentaarid) võib olla aegunud.

## 6. ID-väljade nimetamise reegel

Kõik JSON näidistes esinevad primary key / foreign key väljad peavad sisaldama subjekti nime, mitte olema anonüümne `id`.

- Halb näide: `"id": 2`
- Hea näide: `"userId": 2`, `"entityId": 5`, `"roleId": 1`

See kehtib nii Vaate märkmete sees mainitud andmete kui API märkmete request/response JSON näidiste kohta.

## 7. JSON massiivide (array) reegel näidistes

Kui JSON näidises on massiiv (array / `List`), pane näidisesse **vaid üks array element**, mille järel on koma ja uuel real `...` (kolm punkti), mis viitab sellele, et massiivis võib olla veel elemente.

Näide response listi puhul:
```json
[
  {
    "categoryId": 1,
    "categoryName": "Aiatööd"
  },
  ...
]
```

Näide objekti sees oleva listi puhul:
```json
{
  "entityId": 2,
  "relatedTypes": [
    {
      "relatedTypeId": 1,
      "relatedTypeName": "tüüp 1",
      "isSelected": true
    },
    ...
  ]
}
```

## 8. Koosta sisu struktuuridokumendi järgi

Järgi täpselt `docs/mock-wireframe/kokkulepped/mock-wireframe-markmete-struktuur.md` struktuuri ja reegleid vastava märkmete tüübi jaoks (Vaate märkmed või API märkmed) — väljade järjekord, tühjade ridade paigutus, `—` kasutamine kui lisainfot pole, DTO nime paiknemine vahetult body ploki kohal, veateadete kolmerealine formaat jne.

Kui koostad mitut API märget sama vaate jaoks, koosta iga API kutse kohta eraldi plokk.

## 9. Vorminda copy-paste jaoks

Kogu lõplik märkmete tekst (Vaate märge ja/või iga API märge) peab olema esitatud eraldi Markdown koodiblokina (` ```text ` piiritlejatega), täpselt nagu struktuuridokumendi näidetes — nii saab kasutaja sisu otse mockupi (nt Balsamiq) kollasesse/valgesse kasti copy-pastida.

## 10. Salvesta fail

Salvesta tulemus `.md` failina kausta `docs/mock-wireframe/markmed/`.

**Üks vaade = üks vaate märkmete fail.** Kõik ühe vaate märkmed — Vaate märkmed ja kõik selle vaate API märkmed — hoitakse alati ühes ja samas failis. Eraldi faile üksikute API märgete jaoks ei looda.

**Failinimi:** vaate nimi kebab-case'is + `-markmed.md` — iga suurtähe ette (v.a esimese) pane sidekriips ja muuda kõik tähed väiketähtedeks (nt `HomeView.vue` → `home-view-markmed.md`, `EntityView.vue` → `entity-view-markmed.md`). Sama nimekuju kasutab ka frontend task (`home-view.md`, vt `skill-loo-frontend-task`). Tuleta failinimi automaatselt, ilma kasutajalt küsimata.

**Faili struktuur** — iga plokk oma pealkirja all, Vaate märkmed alati esimesena:

````markdown
# HomeView.vue — märkmed

## Vaate märkmed

```text
...
```

## API märkmed — GET /api/roles

```text
...
```

## API märkmed — POST /api/login

```text
...
```
````

**Uue märkme lisamine või olemasoleva uuendamine:**
- Kui vaate märkmete fail on juba olemas, lisa uus API märge sinna uue `## API märkmed — <METOOD> <path>` jaotisena (API märkmed samas järjekorras nagu vaade neid kutsub, kui see on teada).
- Kui sama pealkirjaga jaotis on juba olemas, uuenda seda — küsi enne kasutajalt kinnitust, et olemasolevat sisu üle kirjutada.
- Kui faili veel pole, loo see; kui luuakse ainult API märge ja Vaate märkmeid veel pole, jäta `## Vaate märkmed` jaotisse märkus `Vaate märkmed lisatakse hiljem.`

## 11. Teavita kasutajat

Näita:
- Loodud/uuendatud faili path
- Lühike kokkuvõte, mis sisu loodi
- Küsi, kas midagi jäi puudu, on ebatäpne, või vajab täiendamist

## Üldised reeglid

- Suhtle kasutajaga eesti keeles.
- Ära leiuta andmeid — kasuta alati kasutaja antud materjali, `docs/database/` failide reaalset sisu, olemasolevaid DTO/entity `.java` faile (kui eksisteerivad) ja backendi error-infrastruktuuri (`infrastructure/` kaust).
- ID-väljad JSON näidistes olgu alati subjektiga (nt `entityId`, mitte `id`).
- JSON massiivides esita vaid 1 element, koma ja `...` uuel real.
- Kui struktuuridokumendi mõni reegel ja kasutaja soov lähevad vastuollu, järgi struktuuridokumenti ja too see kasutajale välja.
- Kui vajalik controller/service kood veel ei eksisteeri (tavaline planeerimisfaasis, vt taustalõik üleval), ära oleta ega väljamõtle — otsi analoogiat mujalt koodibaasist või küsi kasutajalt täpsustust.
