---
name: skill-uus-balsamic-silt
description: Pane olemasolevatest andmetest kokku Balsamiq mockup'i selgitav märkme-silt (Vaate märkmed või API märkmed) ja salvesta see docs/balsamic/notes/ kausta. Kasuta, kui kasutaja tahab luua uue balsamic sildi, balsamic märkmed, vaate märkmed, API märkmed, või soovib olemasolevat mockup silti/märget uuendada või täiendada.
---

# Loo uus Balsamiq märkme-silt

**Taust:** Seda skilli kasutavad õpilased oma tiimiprojekti **planeerimisfaasis** — pärast seda, kui nad on toorikprojektist loonud oma projekti (vt `skill-uus-projekt`), aga **enne** kui domeeni `controller`/`service` klasse on kirjutatud. Sel hetkel on olemas ainult toorikprojekti baasstruktuur (sh `infrastructure/` kaust) ja andmebaasi skeem/näidisandmed — äriloogika kood tuleb alles hiljem. Nende märkmete täpsus kandub otse edasi: need on üks kolmest omavahel süncis peetavast allikast (Balsamiq mockup, Jira task, OpenAPI spec — vt struktuuridokumendi kokkuvõtet) ning on hiljem sisendiks task-failide (`skill-loo-backend-task`) ja koodi kirjutamise (`skill-rain-ai-backend`) faasile.

Eesmärk: panna olemasolevatest andmetest (andmebaasi skeem, näidisandmed, kasutaja antud info) kokku mockup vaate juurde käiv selgitav silt — kas **Vaate märkmed** või **API märkmed** — täpselt struktuuris, mis on kokku lepitud failis `docs/balsamic/notes/balsamiq-markmete-struktuur.md`.

## 1. Uuri kõigepealt struktuuridokumenti

Loe alati **enne** sisu koostamist läbi `docs/balsamic/notes/balsamiq-markmete-struktuur.md`. See defineerib mõlema märkme-tüübi täpse struktuuri, väljad ja reeglid. Ära tugine mällu jäänud struktuurile ega varasemale näitele — loe fail iga kord uuesti, sest struktuur võib olla vahepeal muutunud.

## 2. Küsi kasutajalt, mida ta soovib

Kui pole juba selge, küsi kasutajalt:

1. Kas soovitakse koostada **uut** märget (Vaate märkmed või API märkmed), või **uuendada/täiendada** olemasolevat.
2. Kumb märkme tüüp — Vaate märkmed (üks vaate kohta) või API märkmed (üks backend kutse kohta). Kui vaade teeb mitu API kutset, tuleta kasutajale meelde, et iga kutse kohta tuleb eraldi API märkmete kast.

Kui tegemist on uuendamisega, küsi kasutajalt (kui pole juba antud), mille põhjal olemasolevat silti leida/täiendada — kasutaja võib anda:
- olemasoleva `.md` faili path'i `docs/balsamic/notes/` kaustas,
- vabatekstina kirjelduse/muudatuse, mida tuleb arvesse võtta,
- screenshoti/pildi konkreetsest balsamic mocki vaatest.

Kõiki neid sisendvorme tuleb aktsepteerida — kui kasutaja annab screenshoti, loe sellelt olemasolev tekst välja samamoodi nagu uue märkme koostamisel lähtematerjali loetakse.

## 3. Kogu kasutajalt sisendmaterjal

Kasutaja võib anda suunised/info mistahes kujul:
- vabatekstina jutus,
- failide path'idena (nt konkreetne DTO fail, controller, teine task),
- screenshotina/pildina konkreetsest Balsamiq vaatest.

Loe kõik viidatud failid/pildid üle enne sisu koostamist. Kui info on ebapiisav mõne kohustusliku välja täitmiseks (struktuuridokumendi järgi), küsi kasutajalt täpsustust selle asemel, et oletada.

## 4. Uuri andmebaasi ja olemasolevat koodi, et saada õiged väärtused

Kasuta järgmisi allikaid, et JSON näidised, ID-d ja väljad oleks kooskõlas päris andmebaasi seisuga (mitte väljamõeldud):

- `docs/database/2_create.sql` — tabelistruktuur, veerud, tüübid, PK/FK seosed. Sellest tuletad ka väljanimed ja seosed DTO-de vahel.
- `docs/database/3_import.sql` — reaalsed näidisandmed (INSERT read). Kasuta neid JSON näidiste väärtusteks (id-d, nimed), mitte väljamõeldud andmeid.

Kui kasutaja on juba andnud konkreetse JSON näidise (nt tekstina või pildilt), eelista seda, aga kontrolli väärtuste (id-de, nimede) kooskõla `3_import.sql`-ga — kui ei klapi, teavita kasutajat lahknevusest ja küsi, kumba kasutada.

Kui planeeritava API kutsega seotud DTO/entity `.java` failid **juba eksisteerivad** koodibaasis (nt varasema taski käigus loodud, või mockup-faasis juba ette kirjutatud) — otsi need üles (nt `backend/src/main/java/**/dto/`, `backend/src/main/java/**/persistence/`) ja kasuta neist täpseid väljanimesid, tüüpe ja DTO klassinime, mitte ära tuleta neid ainult SQL-tabelinimedest. Kui vastavaid faile veel pole (tavaline olukord planeerimisfaasis, vt taustalõik üleval), tuleta väljanimed ja DTO klassinimi `2_create.sql` struktuurist ja kasutaja antud infost, järgides projekti olemasolevat DTO nimetamise stiili (nt `<Subjekt>Dto.java`, `<Subjekt>CreateRequestDto.java` — vaata mõnda olemasolevat DTO-t mujalt koodibaasist eeskujuks, kui vähegi mõni on).

Kui kasutaja ei ole täpset API path'i/HTTP meetodit andnud, kontrolli seda kõigepealt olemasolevast controller-klassist (kui vastav endpoint juba eksisteerib). Kui controllerit veel pole, otsi vastavus `docs/stoplight_io_openAPI.json` (OpenAPI spec) või Jira taski failist (`docs/jira-updates/*.md`, kui olemas) — struktuuridokumendi kokkuvõte defineerib need kolm allikat (Balsamiq, Jira, OpenAPI) kui omavahel süncis olevad kirjeldused samast asjast. Kui ka need puuduvad, tugine kasutaja kirjeldusele ja pane path/meetod paika koos temaga, järgides projekti olemasolevat REST konventsiooni (nt `/api/...` baastee, ressursinimed mitmuses).

## 5. API märgete veateated — tuleta reaalsest error-infrastruktuurist

API märgete `Veateated:` plokki ära kunagi väljamõeldud/oletusliku sisuga täida — tuleta iga veajuhtum otse koodist, mitte ainult controlleri `@ApiResponse` kommentaaridest (need võivad olla aegunud või mittetäielikud).

Backendi error-infrastruktuur (`infrastructure/` kaust koos sisuga) eksisteerib projektis juba algusest peale, ka enne kui ühtegi domeeni controller/service klassi on kirjutatud — see on osa toorikprojektist, mille pealt uued meeskonnaprojektid luuakse. Package tee ise (nt `ee.bcs.bank` vs `ee.mingiprojekt`) erineb projektiti, seega leia see esmalt dünaamiliselt üles, mitte ära eelda konkreetset teed:

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
- Hea näide: `"cityId": 2`, `"locationId": 5`, `"transactionTypeId": 1`

See kehtib nii Vaate märkmete sees mainitud andmete kui API märkmete request/response JSON näidiste kohta.

## 7. Koosta sisu struktuuridokumendi järgi

Järgi täpselt `docs/balsamic/notes/balsamiq-markmete-struktuur.md` struktuuri ja reegleid vastava märkme tüübi jaoks (Vaate märkmed või API märkmed) — väljade järjekord, tühjade ridade paigutus, `—` kasutamine kui lisainfot pole, DTO nime paiknemine vahetult body ploki kohal, veateadete kolmerealine formaat jne.

Kui koostad mitut API märget sama vaate jaoks, koosta iga API kutse kohta eraldi plokk.

## 8. Vorminda copy-paste jaoks

Kogu lõplik märkme tekst (Vaate märkmed plokk ja/või iga API märkmete plokk) peab olema esitatud eraldi Markdown koodiblokina (` ```text ` piiritlejatega), täpselt nagu struktuuridokumendi näidetes — nii saab kasutaja sisu otse Balsamiq kollasesse/valgesse kasti copy-pastida.

## 9. Salvesta fail

Salvesta tulemus `.md` failina kausta `docs/balsamic/notes/`.

**Failinimi:**
- Tuleta failinimi automaatselt vaate/teenuse nimest, ilma kasutajalt küsimata (v.a kui tuletus jääb ebaselgeks):
  - Vaate märkmete puhul: `<ComponentName>-markmed.md` (nt `LocationView-markmed.md`)
  - API märkmete puhul: `<METOOD>-<path-sidekriipsudega>-markmed.md` (nt `POST-atm-locations-markmed.md`)
  - Eestikeelsed täpitähed translitereeri (õ→o, ä→a, ö→o, ü→u, š→s, ž→z)
- Uuendamise puhul: kui kasutaja andis olemasoleva faili path'i, kirjuta samasse faili. Kui kasutaja andis ainult vaate/teenuse nime või pildi, otsi kõigepealt `docs/balsamic/notes/` kaustast, kas sobiva nimega fail juba eksisteerib, ja kui jah, küsi kasutajalt kinnitust, kas seda täiendada, enne kui üle kirjutad.

Kui samasse faili tuleb nii vaate märkmed kui mitu API märget (nt kui kasutaja soovib kogu vaate kohta korraga kõik sildid), pane need üksteise järele samas failis, iga plokk oma pealkirja all (nt `## Vaate märkmed`, `## API märkmed — POST /api/atm/locations`).

## 10. Teavita kasutajat

Näita:
- Loodud/uuendatud faili path
- Lühike kokkuvõte, mis sisu loodi
- Küsi, kas midagi jäi puudu, on ebatäpne, või vajab täiendamist

## Üldised reeglid

- Suhtle kasutajaga eesti keeles.
- Ära leiuta andmeid — kasuta alati kasutaja antud materjali, `docs/database/` failide reaalset sisu, olemasolevaid DTO/entity `.java` faile (kui eksisteerivad) ja backendi error-infrastruktuuri (`infrastructure/` kaust).
- ID-väljad JSON näidistes olgu alati subjektiga (nt `locationId`, mitte `id`).
- Kui struktuuridokumendi mõni reegel ja kasutaja soov lähevad vastuollu, järgi struktuuridokumenti ja too see kasutajale välja.
- Kui vajalik controller/service kood veel ei eksisteeri (tavaline planeerimisfaasis, vt taustalõik üleval), ära oleta ega väljamõtle — otsi analoogiat mujalt koodibaasist või küsi kasutajalt täpsustust.