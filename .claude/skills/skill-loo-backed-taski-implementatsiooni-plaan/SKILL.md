---
name: skill-loo-backed-taski-implementatsiooni-plaan
description: Loo backend taski MD failile implementatsiooni plaan — uuri taski, olemasolevat koodibaasi (controller/service/persistence), backend/CLAUDE.md konventsioone ja docs/structure/backend-projekti-struktuur.md. Kasuta, kui kasutaja tahab implementatsiooniplaani, tehnilist plaani, taski lahenduskäiku backend taskile, või mainib "implementatsiooni plaan", "kuidas seda taski lahendada", "tee plaan backend taskile".
---

# Loo backend taski implementatsiooni plaan

Koosta olemasoleva backend taski MD faili põhjal detailne tehniline implementatsiooniplaan: milliseid faile luua/muuta, mis kihti (controller/service/persistence/infrastructure) need kuuluvad, ja mis juba koodibaasis olemas on.

## Steps

### 1. Küsi taski faili path

Kui kasutaja pole seda juba andnud, küsi, millise taski faili (`docs/tasks/backend/*.md`) jaoks implementatsiooniplaan koostada.

Oota vastust enne kui jätkad.

### 2. Tutvu taskiga

Loe taski MD fail täielikult läbi. Pane tähele:

- Teenuse HTTP meetod ja URL
- Sisend (path variable, query parameeter, request body)
- Väljund (response body struktuur, DTO väljad)
- Seotud andmebaasi tabelid
- Veaolukorrad
- Vastuvõtu kriteeriumid

### 3. Tutvu backend/CLAUDE.md juhistega

Loe `backend/CLAUDE.md` läbi (kihtide struktuur, nimetamiskonventsioonid, veakäsitlus, DTO/entiteedi reeglid, repositooriumi meetodite nimetamine jne). Implementatsiooniplaan peab neid konventsioone järgima.

### 4. Jälgi projekti struktuuri juhist

Loe `docs/structure/backend-projekti-struktuur.md` läbi. Uute failide asukoht ja nimetamine peab järgima seal kirjeldatud struktuuri (nt `controller/<ressurss>/`, `controller/<ressurss>/dto/`, `controller/common/dto/`, `persistence/<entiteet>/`, `service/`).

**Jagatud DTO-d** — kui plaanitav response/request DTO on (või hakkab olema) kasutusel rohkem kui ühe ressursi kontrolleris/mapperis/service'is, ei kuulu see ühegi üksiku ressursi `dto/` paketti, vaid paketti `controller/common/dto/` (vt backend/CLAUDE.md reeglit "Jagatud DTO-d"). Kontrolli olemasoleva DTO puhul alati, kas seda kasutab juba mõni teine ressurss (Grep DTO nime järgi) — kui jah, ja see asub veel ressursipõhises paketis, tuleks plaan sisaldada selle ümbertõstmist paketti `controller/common/dto/`.

### 5. Uuri olemasolevat koodibaasi

Enne kui eeldad, et midagi tuleb nullist luua, kontrolli, mis on juba olemas:

- **Otsi entiteet** — kas taskiga seotud andmebaasi tabeli(te) jaoks on juba JPA entiteet `persistence/` all?
- **Otsi repository** — kas Spring Data repository liides on juba olemas?
- **Otsi mapper** — kas MapStruct mapper entiteedi ja DTO vahel on juba olemas, ja kas see toodab taski poolt nõutud väljad?
- **Otsi DTO** — kas response/request DTO klass on juba olemas (võib-olla mõne teise ressursi `dto/` paketis, kui seda kasutatakse mujal ka)?
- **Otsi service meetod** — kas service klassis on juba meetod, mis vajaliku äriloogika katab, või on olemas ainult osa sellest (nt `getValidXBy(id)` üksiku kirje jaoks, aga puudub `findAll`/nimekirja tagastav meetod)?
- **Otsi controller/endpoint** — kas REST endpoint juba eksisteerib mõnes kontrolleris, või on kontroller täiesti loomata?
- **Otsi olemasolevad testid** — kas mõni test juba katab sarnast funktsionaalsust, mida saab eeskujuks võtta?

Kasuta Grep/Glob tööriistu ja otsi nii taski URL-i (nt `api/roles`), DTO nimesid, entiteedi nime kui tabeli nime järgi. Ära eelda kohe, et kõik tuleb kirjutada nullist — sageli on osa keti lülidest (entiteet, repository, mapper) juba olemas mõne muu funktsionaalsuse käigus ja puudub ainult üks kiht (tavaliselt controller ja/või vastav service meetod).

Vaata ka sarnaste, juba valmis teenuste eeskuju (nt kui on olemas mõni analoogne `GET` nimekirja-teenus, mille kontroller/service/mapper on täielikult valmis) — implementatsiooniplaan peaks järgima sama mustrit.

### 6. Koosta implementatsiooniplaan

Struktuur (järgi täpselt):

```markdown
# <Taski pealkiri> — implementatsiooni plaan

**Seotud task:** `<taski faili path>`

## Hetkeseis (mis on juba olemas)

<Loetelu leitud olemasolevatest failidest koos täieliku path'iga ja lühikirjeldusega, mida iga fail juba teeb. Kui midagi olulist puudub, maini seda ka siin selgelt ("Kontroller endpointi jaoks puudub" vms).>

## Puuduv/muudetav

<Loetelu, mida on vaja luua või muuta, et task valmis saaks.>

## Sammud

<Nummerdatud sammud, igaühe juures:>
1. **<Tegevus>** — fail: `<täielik path uuele/muudetavale failile, vastavalt docs/structure/backend-projekti-struktuur.md struktuurile>`
   - <Mida täpselt sinna kirjutada/muuta, viidates backend/CLAUDE.md konventsioonidele (nimetamine, kihi vastutus, veakäsitlus jne)>
   - <Koodinäide või meetodi signatuur, kui aitab selgust luua>

<Sammud peavad olema kihtide järjekorras: persistence (entiteet → repository → mapper) → service → controller → testid, v.a kui mõni kiht on juba olemas, siis jäta see samm ära või märgi "juba olemas, muudatust ei vaja".>

## Veakäsitlus

<Kuidas taskis kirjeldatud veaolukorrad (nt 404, 500) implementeeritakse — millist erindiklassi visata (`DataNotFoundException`, `ForbiddenException`, `PrimaryKeyNotFoundException` vms) ja kus (service kihis), tuginedes backend/CLAUDE.md veakäsitluse konventsioonile.>

## Testid

<Milliseid teste on vaja lisada/kohandada (ühiktestid service kihile, integratsioonitestid kontrollerile), mis juhtumeid katta (õnnestunud vastus, tühi tulemus, veaolukorrad vastavalt taski vastuvõtu kriteeriumidele).>

## Avatud küsimused

<Kui midagi taskis või koodibaasis jääb ebaselgeks (nt vastuolu taski ja olemasoleva API tee vahel), too need siin selgelt välja kasutajale otsustamiseks. Ära oleta vaikimisi ühte varianti, kui vastuolu on oluline.>
```

### 7. Kontrolli vastuolusid taski ja koodibaasi vahel

Kui taskis kirjeldatud URL, DTO väljad, tabelinimi vms ei klapi täpselt olemasoleva koodiga (nt task nõuab `/api/roles`, aga koodibaasis/CLAUDE.md-s on juba dokumenteeritud teine tee samale funktsionaalsusele), too see selgelt esile plaani "Avatud küsimused" sektsioonis — ära vaikimisi ise otsustada, kumb on õige.

### 8. Salvesta fail

Salvesta implementatsiooniplaan **samasse kausta**, kus asub target taski fail, **sama failinimega**, millele on lisatud postfiks `-IMPLEMENTATSIOON` enne `.md` laiendit.

Näide: task `docs/tasks/backend/GET-api-roles.md` → plaan `docs/tasks/backend/GET-api-roles-IMPLEMENTATSIOON.md`.

### 9. Teavita kasutajat

Näita:
- Loodud faili asukoht
- Lühike kokkuvõte (2-3 lauset): mis juba on olemas ja mis põhiline töö plaani järgi ees seisab
- Kas leidsid mõne vastuolu taski ja koodibaasi vahel, mis vajab kasutaja otsust

## Üldised reeglid

- Suhtle kasutajaga eesti keeles.
- Ära hakka koodi kirjutama ega faile looma/muutma — see skill toodab ainult plaani MD faili.
- Ära oleta koodibaasi seisu — kontrolli alati Grep/Glob/Read tööriistadega, mis päriselt olemas on.
- Järgi rangelt backend/CLAUDE.md konventsioone (nimetamine, kihtide vastutus, veakäsitlus).
- Järgi rangelt docs/structure/backend-projekti-struktuur.md failide paigutust.
