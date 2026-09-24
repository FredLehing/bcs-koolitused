---
name: skill-loo-backend-task
description: Loo backend teenuse taski MD fail mockupi (wireframe) PDF-i konkreetse lehekülje põhjal. Küsi kasutajalt PDF failinimi ja lehekülje number. Kasuta, kui kasutaja tahab luua backend taski, mockup lehekülje põhjal taski, või mainib "loo task", "backend task" vms.
---

# Loo backend teenuse task mockupist

Loe mockupi PDF-i konkreetne lehekülg, tuvasta sellel kirjeldatud backend teenus (API märge, mis algab reaga `API:`) ja koosta selle kohta täielik taski MD fail koos vastava lehekülje pildiga. Salvesta `docs/tasks/backend` kausta.

**Väljund:**

- `docs/tasks/backend/<Failinimi>.md` — taski sisu; failinimi tuleb teenuse meetodist ja path'ist (nt `GET /api/roles` → `GET-api-roles.md`, vt samm 7)
- `docs/mock-wireframe/pdf-images/<Vaade>.png` — mockupi lehekülje pilt (nt `EntityView.png`), luuakse PDF-ist, kui seda veel pole; kui on ja see erineb, küsitakse kasutajalt (samm 3). Pilti ei kopeerita taski kausta — task viitab ühisele pildile.

Skill võib peatuda ja kasutajalt täpsustust küsida järgmistes kohtades: mitu teenust samal lehel või PDF ja vaate märkmete fail lahknevad (samm 2), vaate pilt on juba olemas ja erineb või pildi loomine ebaõnnestub (samm 3), sama teenuse task on juba olemas (samm 4), mockup ja olemasolev kood on vastuolus (samm 5), andmebaasist ei leia sobivat tabelit (samm 6).

## Steps

### 1. Küsi sisendid

Kui kasutaja pole neid juba andnud, küsi korraga:

1. **PDF failinimi** — nt `docs/mock-wireframe/pdf/<projekti mockup>.pdf` (vaata `docs/mock-wireframe/pdf/` kaustast, mis PDF-id seal on, ja paku neid valikuna)
2. **Lehekülje number** — mille pealt task luua

Oota vastust enne kui jätkad.

### 2. Loe PDF-i vastav lehekülg

Kasuta Read tööriista `pages` parameetriga, et lugeda ainult see üks lehekülg PDF-ist.

Leia leheküljelt **Vaate märkmete** kast (struktuur: `docs/mock-wireframe/kokkulepped/mock-wireframe-markmete-struktuur.md`, jaotis 1) ja loe sealt:

- `Failinimi` — frontend vaate komponent (nt `EntityView.vue`); see läheb taski reale `**Kasutav vaade:**`
- `Frontend rada` — vaate route (nt `/entity`)
- `Roll` — kes vaadet kasutab (läheb taski "Eesmärk" jaotisesse)

Kui leheküljel "Vaate märkmeid" pole või `Failinimi` puudub, küsi vaate nimi kasutajalt — ära tuleta seda ise wireframe'i pealkirjast.

Leia leheküljelt kõik **API märkmete** kastid (read algavad `API:`). Nende struktuur ja väljade tähendus on kirjeldatud failis `docs/mock-wireframe/kokkulepped/mock-wireframe-markmete-struktuur.md`, jaotis 2 — loe see fail läbi enne märkmete tõlgendamist. Iga API märkme kohta loe välja:

- `API:` rida — HTTP meetod ja path muster (nt `PUT /api/entity/{entityId}`); `{...}` tähistab path variable'it
- **Request DTO** nimi ja `Request body:` JSON — ainult siis, kui teenus võtab body sisse
- **Response DTO** nimi ja `Response (200):` JSON — või `Response (200): NONE`, kui teenus tagastab tühja vastuse
- `API teenuse lisainfo:` — teenuse eripärad (filtrid, valikulised väljad, soft delete jms)
- `Veateated:` — iga veajuhtum kujul `HTTP` / `errorCode` / `message`; `—` tähendab, et mockup vigu ei kirjelda

Massiivide puhul on JSON näidises tavaliselt üks element ja `...` — see tähendab "elemente võib olla rohkem", mitte et massiivis ongi üks element.

Loe läbi ka "Vaate märkmete" `Vaatega seotud lisainfo` ja must-valge wireframe. Need annavad tausta, mis on kasutaja voog ja millal teenust kutsutakse.

Kui kaustas `docs/mock-wireframe/markmed/` on olemas selle vaate märkmete fail `<vaate-nimi>-markmed.md` (vaate nimi kebab-case'is, nt `LocationFormView.vue` → `location-form-view-markmed.md`), loe ka see läbi — selles on ühes kohas kõik selle vaate märkmed (Vaate märkmed ja kõik API märkmed) puhtama, kergemini loetava tekstina ja see aitab kontrollida, et miski PDF-i lugemisel valesti ei tõlgendatud. Kui PDF-i ja vaate märkmete faili sisu lahknevad (nt erinev path, DTO väli või veateade), **peata**, näita kasutajale erinevust ja küsi, kumba kasutada.

**Kui leheküljel on mitu API märget**, näita kasutajale nimekirja (meetod + path) ja küsi, millise teenuse kohta task luua. Oota vastust.

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

### 4. Kontrolli olemasolevaid taske

Sama teenus esineb mockupis sageli mitmel lehel (nt `GET /api/roles` nii "tühja" kui "täidetud" vormi lehel). Kuna taski failinimi tuletatakse üheselt teenuse meetodist ja path'ist (vt samm 7), piisab kontrollist, kas see fail on juba olemas (nt `ls docs/tasks/backend/GET-api-roles.md`).

Kui sama teenuse task on juba olemas, **peata ja küsi kasutajalt**:

- kas olemasolevat taski uuendada — kui teenust kasutab ka uus vaade, lisa see vaade olemasoleva taski `**Kasutav vaade:**` reale (komaga eraldatult) ja täienda "Eesmärk" jaotist,
- või katkestada.

Ära kirjuta olemasolevat faili üle ilma kasutaja kinnituseta.

### 5. Uuri olemasolevat backend koodi ja konventsioone

Loe läbi `backend/CLAUDE.md` (eriti jaotised "Olulised konventsioonid" ja "REST API") ja `docs/structure/backend-projekti-struktuur.md`.

Seejärel kontrolli koodist (`backend/src/main/java/`):

- **Kas endpoint on juba olemas** — otsi controller'itest path'i (nt `rg "roles" backend/src/main/java`). Kui on, too see taskis välja (task kirjeldab siis olemasolevat või muudetavat teenust).
- **DTO-d** — kas mockupis mainitud DTO klass või sama väljadega DTO on juba olemas (vt ka `controller/common/dto/` jagatud DTO-sid). Kui mockupi DTO nimi erineb koodis juba kasutusel olevast sama kujuga DTO-st, maini seda taskis ja küsi kasutajalt, kumba nime kasutada.
- **Veakäsitlus** — vaata `infrastructure/RestExceptionHandler.java` ja `infrastructure/exception/`, et teada, millised exception'id millise HTTP staatuse annavad. Vea response body kuju tuleb klassist `infrastructure/error/ApiError.java` (`message`, `errorCode`). Äriveateadete enum on tavaliselt base-paketis olev `Error.java` (nt `ee.minuprojekt.Error`) — kui see on juba loodud, kontrolli, kas mockupi `errorCode` on seal olemas; kui enumit veel pole, maini taskis, et see tuleb luua.
- **Leiu-meetodid** — ID järgi otsingu jaoks kasutatakse `getValid<Entiteet>By(...)` mustrit, mis viskab `PrimaryKeyNotFoundException` (404, `PRIMARY_KEY_NOT_FOUND`). Kui teenusel on path variable'ina ID, on see veaolukord peaaegu alati asjakohane.

Kasuta seda infot ainult taski täpsemaks kirjeldamiseks (veaolukorrad, olemasolevad klassid). **Ära kirjuta taski implementatsiooni plaani** — selleks on eraldi skill `skill-loo-backed-taski-implementatsiooni-plaan`.

### 6. Uuri andmebaasi skeemi ja näidisandmeid

Tuvasta teenusega seotud tabel(id) failist `docs/database/2_create.sql`:

- Otsi tabeleid, mille nimi/veerud sobivad teenuse path'i ja DTO väljadega (nt `/api/roles` → `role` tabel)
- Loe välja tabeli täielik struktuur (veerud, tüübid, PK/FK constraint'id, unikaalsuse piirangud)
- Kui teenus puudutab mitme tabeli seost (nt many-to-many liitetabel) või vaadet (`CREATE VIEW`), kajasta ka see

Seejärel otsi `docs/database/3_import.sql` failist nende tabelite `INSERT` read. Kasuta neid andmeid (ID-sid, nimesid, väärtusi) JSON näidistes — mitte väljamõeldud andmeid.

Kui PDF-i lehel on JSON näidis olemas, võta see aluseks samal kujul (väljade nimed ja struktuur), aga kontrolli, et väärtused (id-d, nimed) klapiksid `3_import.sql` andmetega. Kui ei klapi, kasuta `3_import.sql` andmeid ja märgi see taskis. Massiivi puhul kirjuta taski näidisesse kõik `3_import.sql` read (kui neid on kuni ~5); kui neid on rohkem, näita paar elementi ja lisa selgitus.

Kui sobivat tabelit ei leidu, küsi kasutajalt täpsustust — ära oleta.

### 7. Tuleta taski pealkiri ja failinimi

**Pealkiri** (faili sees `# ...` real) kirjeldab eesti keeles kokkuvõtvalt, mida teenus teeb (nt "Rollide nimekirja päring", "Kasutaja andmete muutmine"). Vaata stiili olemasolevatest `docs/tasks/backend/*.md` pealkirjadest.

**Failinimi** tuletatakse teenuse `API:` realt (HTTP meetod + path muster), mitte pealkirjast — nii on see ühene ja sama teenuse task on lihtne üles leida:

- Alusta HTTP meetodiga suurtähtedes, seejärel path
- Eemalda path'i algusest `/` ja asenda ülejäänud `/` sidekriipsuga
- Path variable'i puhul eemalda `{` `}` sulud, nimi ise jääb alles
- Query parameetrid (`?...`) failinimesse ei lähe
- Path'i tähtede suurus jääb samaks nagu API real
- Formaat: `<METOOD>-<path-sidekriipsudega>.md`
- Näited:
  - `GET /api/roles` → `GET-api-roles.md`
  - `PUT /api/user/{userId}` → `PUT-api-user-userId.md`
  - `GET /api/user/{userId}/transactions-history` → `GET-api-user-userId-transactions-history.md`

### 8. Koosta taski MD fail

Struktuur (jaotiste pealkirjad ja järjekord täpselt nii; nurksulgudes `<...>` on juhised sisu kohta):

```markdown
# <Pealkiri>

**Teenus:** `<HTTP MEETOD> <path muster>`

**Kasutav vaade:** `<Vaate failinimi.vue>` (`<Frontend rada>`)

![Mockup](../../mock-wireframe/pdf-images/<pildi fail>)

## Sisend

<Path variable'id (nimi, tüüp, tähendus), query parameetrid, request body (DTO nimi + väljade kirjeldus). Kui sisendeid pole, kirjuta "Teenusel puuduvad sisendid.">

<Kui request body on olemas, lisa JSON näidis code block'is.>

## Väljund

**Response (200 OK):** <DTO nimi ja kirjeldus, mida tagastatakse. Kui mockupis on `Response (200): NONE`, kirjuta "Tühi vastus (ainult staatuskood 200 OK).">

<JSON näidis code block'is, kasutades PDF / 3_import.sql andmeid — kui vastus pole tühi.>

<Valikuline: selgitused väljade tähenduse kohta, kui pole ilmselge (nt järjestus, valikulised väljad, "API teenuse lisainfo" sisu).>

## Eesmärk

<2-4 lauset: millises vaates / kasutaja voos teenust kasutatakse, kes on kasutaja (roll), mida üritatakse saavutada, miks teenust vaja on.>

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

<Iga seotud tabeli jaoks: tabeli nimi alampealkirjana, lühikirjeldus ja CREATE TABLE lõik code block'is.>

<Näidisandmed 3_import.sql-st tabelina või loeteluna.>

<Valikuline: kui mõni seonduv tabel (nt liitetabel) EI puutu sellesse teenusesse, maini seda selguse mõttes.>

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| <tingimus> | <kood + nimi, nt 404 Not Found> | `{ "message": "...", "errorCode": "..." }` |

<Valikuline: märkus, millisele olemasolevale exception'i / meetodi mustrile veaolukord vastab (nt `PrimaryKeyNotFoundException` + `getValidLocationBy`).>

## Vastuvõtu kriteeriumid

- [ ] Endpoint `<METOOD> <path>` on olemas
- [ ] <Õnnestunud vastuse staatuskood ja struktuur>
- [ ] <Andmete õigsus/järjekord vastavalt andmebaasile>
- [ ] <Servaolukorrad (tühi tulemus jms), kui asjakohane>
- [ ] <Iga veaolukorra rida eraldi kriteeriumina>
- [ ] Teenusel on automaattestid
```

**Veaolukordade reeglid:**

- Kui mockupi `Veateated` plokis on veajuhtumid, võta need taski **täpselt üle**: sama HTTP kood, sama `errorCode` ja sama `message` tekst.
- Lisa veaolukorrad, mis tulenevad sisenditest ja projekti olemasolevast mustrist, isegi kui mockup neid ei maini (nt path variable ID puudub andmebaasist → 404 `PRIMARY_KEY_NOT_FOUND` sõnumiga `Ei leidnud primary keyd '<väli>' väärtusega: <id>`; `@Valid` request body vigane → 400 `INCORRECT_INPUT`). Märgi taskis, et need tulenevad olemasolevast mustrist, mitte mockupist.
- Lisa alati rida ootamatu serveripoolse vea kohta: `500 Internal Server Error` — "Standardne vea response body (vastavalt projekti globaalsele error handler'ile)".
- Kui teenusel pole sisendeid ega mockupi veateateid, piisab 500 reast ja lausest, miks muid veaolukordi pole.

Kui kaustas `docs/tasks/backend/` on juba taske, kasuta neid stiilieeskujuna (eriti sama tüüpi teenuse puhul, nt sisendita GET või path variable'i + request body'ga PUT). Ole sama põhjalik ja konkreetne.

### 9. Salvesta failid

Kirjuta `docs/tasks/backend/<Failinimi>.md` Write tööriistaga (loob kausta, kui see puudub). Pilt on juba sammus 3 ühisesse kausta loodud — ära kopeeri seda taski kausta.

Ära kirjuta midagi kausta `docs/tasks/backend/instructions/` — see on teiste skillide väljund.

### 10. Teavita kasutajat

Näita:

- Loodud failide asukohad
- Lühike kokkuvõte, mis teenusest task räägib
- Tähelepanekud, mis vajavad kasutaja otsust (nt mockupi ja `3_import.sql` andmete erinevus, DTO nime erinevus koodist, endpoint on juba koodis olemas, mockupis puuduv `errorCode` `Error` enumis)
- Küsi, kas midagi jäi puudu, on ebatäpne või vajab täiendamist

Kui leheküljel oli mitu API märget ja loodi ainult üks task, tuleta kasutajale meelde, et ülejäänud teenuste kohta saab soovi korral samamoodi eraldi taskid luua.

## Üldised reeglid

- Suhtle kasutajaga ja kirjuta task eesti keeles.
- Ära leiuta andmeid — kasuta alati PDF-i, `docs/database/` failide ja olemasoleva koodi reaalset sisu.
- Mockupi pilte hoitakse ainult kaustas `docs/mock-wireframe/pdf-images/` — ära kopeeri neid taski kausta ega loo olemasolevat vaate pilti ilma kasutaja nõusolekuta üle.
- Ära muuda backend koodi — see skill loob ainult taski kirjelduse.
- Kui tabelistruktuur, teenuse loogika või veaolukorrad jäävad PDF-i ja koodi põhjal ebaselgeks, küsi kasutajalt täpsustust selle asemel, et oletada.
