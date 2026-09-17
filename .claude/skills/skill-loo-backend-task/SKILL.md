---
name: skill-loo-backend-task
description: Loo backend teenuse taski MD fail balsamic mockup PDF-i konkreetse lehekülje põhjal. Küsi kasutajalt PDF failinimi ja lehekülje number. Kasuta, kui kasutaja tahab luua backend taski, mockup lehekülje põhjal taski, või mainib "loo task", "backend task" vms.
---

# Loo backend teenuse task balsamic mockupist

Loe balsamic mockup PDF-i konkreetne lehekülg, tuvasta sellel kirjeldatud backend teenus (märgitud sõnaga "API:") ja koosta selle kohta täielik taski MD fail koos vastava lehekülje pildiga. Salvesta `docs/tasks/backend` kausta.

## Steps

### 1. Küsi kaks andmepunkti

Kui kasutaja pole neid juba andnud, küsi korraga:

1. **PDF failinimi** — nt `docs/balsamic/minu_projekt_rest.pdf`
2. **Lehekülje number** — mille pealt task luua

Kui leheküljel on mitu "API:" märgistusega backend teenust, küsi kasutajalt, millise teenuse kohta konkreetselt task luua (näita nimekirja leitud teenustest).

Oota vastust enne kui jätkad.

### 2. Loe PDF-i vastav lehekülg

Kasuta Read tööriista `pages` parameetriga, et lugeda ainult see üks lehekülg PDF-ist.

Leia leheküljelt kollane "API:" märgega postit note. Sealt loe välja:

- Teenuse HTTP meetod ja URL (nt `GET /api/atm/transaction-types`)
- DTO klassi nimi, kui mainitud (nt `TransactionTypeInfoDto.java`)
- Response (ja/või request) JSON näidis
- "API teenuse lisainfo" tekst — mida teenus teeb ja kus kasutatakse
- "Veateated" tekst — kui seal on midagi peale "—", kajasta see taskis veaolukordadena

Loe leheküljelt läbi ka ülejäänud kontekst (must-valge wireframe ja kollased selgitusplokid) — need annavad tausta, mis views/komponendid teenust kasutavad ja mis on kasutaja voog.

### 3. Leiba failinime asukoht pildi jaoks

Pildid asuvad `docs/balsamic/pdf-images/<lehekülje number>.png`. Kui see fail puudub, teavita kasutajat ja küsi, kas pildid tuleb enne genereerida (ära ise PDF-ist pilte genereerima hakka, kui vastavat protsessi pole kokku lepitud).

### 4. Uuri andmebaasi skeemi

Tuvasta teenusega seotud tabel(id) faili `docs/database/2_create.sql` põhjal:

- Otsi tabeleid, mille nimi/veerud sobivad teenuse URL-i ja DTO väljadega (nt `/atm/transaction-types` → `transaction_type` tabel)
- Loe välja tabeli täielik struktuur (veerud, tüübid, PK/FK constraint'id, unikaalsuse piirangud)
- Kui teenus puudutab seost mitme tabeli vahel (nt many-to-many liitetabel) või vaadet (`CREATE VIEW`), kajasta ka see

### 5. Leia näidisandmed

Otsi `docs/database/3_import.sql` failist reaalsed `INSERT` read tuvastatud tabeli(te) kohta. Kasuta neid andmeid (ID-sid, nimesid, väärtusi) JSON näidistes — mitte väljamõeldud andmeid — nii et task on kooskõlas päris andmebaasi seisuga.

Kui PDF-i lehel endal on juba konkreetne JSON näidis olemas, eelista seda täpselt sellisel kujul, aga kontrolli, et väärtused (id-d, nimed) klapiksid `3_import.sql` andmetega — kui ei klapi, kasuta `3_import.sql` andmeid ja märgi see taskis.

### 6. Tuleta taski pealkiri ja failinimi

Pealkiri peab kokkuvõtvalt kirjeldama, mida teenus teeb (nt "Tehingutüüpide nimekirja päring").

Failinimi tuletatakse pealkirjast:
- Eestikeelsed täpitähed translitereeri (õ→o, ä→a, ö→o, ü→u, š→s, ž→z)
- Tühikud asenda sidekriipsuga
- Formaat: `<Pealkiri-Sidekriipsudega>.md`
- Näide: pealkiri "Tehingutüüpide nimekirja päring" → fail `Tehingutuupide-nimekirja-paring.md`

### 7. Koosta taski MD fail

Struktuur (järgi täpselt):

```markdown
# <Pealkiri>

**Teenus:** `<HTTP MEETOD> <URL>`

**Vaste balsamic mockupis:** STEP-X, lehekülg <NR>/<KOKKU> (vt lisatud pilt `<failinimi>.png`)

![Mockup](./<failinimi>.png)

## Sisend

<Path variable'id, query parameetrid, request body kirjeldus. Kui puuduvad, kirjuta selgelt "Teenusel puuduvad sisendid.">

<Kui request body on olemas, lisa JSON näidis code block'is.>

## Väljund

**Response (200 OK):** <kirjeldus, mida tagastatakse>

<JSON näidis code block'is, kasutades 3_import.sql / PDF andmeid>

<Vajalikud selgitused väljade tähenduse kohta, kui pole ilmselge.>

## Eesmärk

<2-4 lauset: mis kasutaja voos/view's teenust kasutatakse, mida üritatakse saavutada, miks teenust vaja on.>

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

<Iga seotud tabeli jaoks: tabeli nimi pealkirjana, lühikirjeldus ja CREATE TABLE lõik code block'is.>

<Kui on ka näidisandmed 3_import.sql-st, lisa tabelina või loeteluna.>

<Kui teenus EI puuduta mõnda seonduvat tabelit (nt liitetabel on kasutusel mujal), maini seda selguse mõttes.>

## Veaolukorrad

Tabel veergudega: Olukord | Status code | Response body

<Kui sisendeid pole ja PDF ei viita valideerimis/autoriseerimisnõuetele, piisab reeglina 500 Internal Server Error realistlik veaolukorrast. Kui teenusel on sisendid (path/query/body) või PDF/kontekst viitab rollipõhisele piirangule, lisa ka vastavad 400/401/403/404 read koos konkreetsete tingimustega.>

## Vastuvõtu kriteeriumid

<Checkbox nimekiri (- [ ]), mis katab:>
- <Endpoint on olemas õige URL ja HTTP meetodiga>
- <Õnnestunud vastuse staatuskood ja struktuur>
- <Andmete õigsus/järjekord vastavalt andmebaasile>
- <Servaolukorrad (tühi tulemus, jms), kui asjakohane>
- <Kirjeldatud veaolukorrad>
- <Automaattestide olemasolu nõue>
```

Ole taski sisu koostades sama põhjalik ja konkreetne nagu varasemas näidises (`docs/tasks/backend/Tehingutuupide-nimekirja-paring.md`) — kasuta seda stiilieeskujuna, kui see fail on olemas.

### 8. Loo `docs/tasks/backend` kaust, kui puudub

### 9. Salvesta failid

1. `docs/tasks/backend/<failinimi>.md` — taski sisu
2. `docs/tasks/backend/<failinimi>.png` — koopia failist `docs/balsamic/pdf-images/<lehekülje number>.png`

### 10. Teavita kasutajat

Näita:
- Loodud failide asukohad
- Lühike kokkuvõte, mis teenusest task räägib
- Küsi, kas midagi jäi puudu, on ebatäpne, või vajab täiendamist

Kui leheküljel oli mitu "API:" teenust ja loodi ainult üks task, tuleta kasutajale meelde, et ülejäänud teenuste kohta saab soovi korral samamoodi eraldi taskid luua.

## Üldised reeglid

- Suhtle kasutajaga eesti keeles.
- Ära leiuta andmeid — kasuta alati PDF-i ja `docs/database/` failide reaalset sisu.
- Ära loo skripti/automatiseeringut PDF-ist piltide genereerimiseks — eelda, et `docs/balsamic/pdf-images/` sisu on juba olemas.
- Kui tabelistruktuur või teenuse loogika jääb PDF-i põhjal ebaselgeks, küsi kasutajalt täpsustust selle asemel, et oletada.
