---
name: skill-grill-my-task
description: Grilli olemasolevat backend või frontend taski — leia taski failist lüngad, ebaselgused ja vastuolud, küsitle kasutajat ükshaaval koos soovitusliku vastusega ja kirjuta kokkulepitud otsused lõpuks taski faili sisse. Kasuta, kui kasutaja tahab taski läbi grillida, taski proovile panna, taski lünki leida või mainib "grill my task".
---

# Grilli taski

Sama idee nagu `skill-grill-me`, aga fookuses on **üks konkreetne taski fail**. Eesmärk on, et task oleks enne implementeerimist nii selge ja täielik, et selle järgi saab koodi kirjutada ilma oletamata. Grillitakse **taski sisu**, mitte õpilase teadmisi.

## 1. Küsi taski faili

Kui kasutaja pole seda juba andnud, küsi taski faili teed — nt `docs/tasks/backend/POST-api-location.md` või `docs/tasks/frontend/location-form-view.md`. Kui fail puudub, näita kaustast sobivaid faile.

Oota vastust enne kui jätkad.

## 2. Loe task ja kogu kontekst

Loe task läbi ja tuvasta, kas see on backend või frontend task (kaust). Seejärel loe kõik, mis aitab taski kontrollida — **enne küsimist**, et mitte küsida seda, mida saab ise välja uurida:

- `docs/mock-wireframe/kokkulepped/mock-wireframe-markmete-struktuur.md` — märkmete struktuur ja URL-ide kokkulepe (jaotis 3)
- vaate märkmete fail `docs/mock-wireframe/markmed/<vaate-nimi>-markmed.md`, kui olemas, ja taskis viidatud mockupi pilt
- `docs/database/2_create.sql` ja `3_import.sql` — tabelid, piirangud (NOT NULL, UNIQUE, FK), näidisandmed
- olemasolev kood: backend controllerid/DTO-d/`infrastructure/` (veakäsitlus), frontend vaated/router
- seotud taskid: backend taski puhul seda teenust kasutavad frontend taskid; frontend taski puhul tema API kutsete backend taskid (failinimi tuletatakse API realt, nt `PUT /api/location/{locationId}` → `docs/tasks/backend/PUT-api-location-locationId.md`)
- `docs/structure/` — projekti struktuuri dokumendid

## 3. Koosta küsimuste puu

Käi task läbi allolevate teemade kaupa ja pane kirja kõik kohad, mis on **puudu, ebaselged, omavahel vastuolus või ei klapi** andmebaasi, koodi, kokkulepete või seotud taskidega. Järjesta need nii, et otsused, millest teised sõltuvad, tulevad enne (nt "kas teenus tagastab ühe objekti või nimekirja" enne "mis juhtub tühja nimekirja korral").

**Backend task:**
- **Teenuse kuju** — kas HTTP meetod ja path vastavad URL-ide kokkuleppele (ainsus/mitmus, `/api/<ressurss>`)?
- **Sisend** — millised väljad on kohustuslikud, millised valikulised; valideerimisreeglid (pikkus, vahemik, formaat); mis siis, kui path variable'i ID-d pole olemas?
- **Äriloogika** — mida täpselt kontrollitakse (nt unikaalne nimi), mis järjekorras; mis juhtub seotud andmetega (nt liitetabeli read, pilt)?
- **Andmebaasi mõju** — millised tabelid muutuvad; kas kustutamine on päris või soft delete; kas DB piirangud (UNIQUE, NOT NULL) on taskis kajastatud?
- **Väljund** — mida täpselt tagastatakse; järjestus; tühja tulemuse käitumine; `null` väljad?
- **Veaolukorrad** — kas iga äriloogika reegli ja sisendi rikkumise kohta on veaolukord (HTTP kood, `errorCode`, `message`)? Kas need klapivad projekti veakäsitlusega (`infrastructure/`)?
- **Ligipääs** — kes teenust kasutab (roll)?
- **Vastuvõtu kriteeriumid** — kas iga ülaltoodud otsus on kontrollitav kriteeriumina kirjas?

**Frontend task:**
- **Vaade** — kas nimi ja rada vastavad kokkuleppele (`…View`, `…FormView`, `…sView`)? Kas vorm töötab nii lisamise kui muutmise režiimis ja kuidas neid eristatakse (query parameeter)?
- **Rollid** — kes vaadet näeb; mis on rolliti erinev; mis juhtub, kui õiguseta kasutaja avab raja otse?
- **Olekud** — laadimine, tühi nimekiri, viga, puuduv või vigane query parameeter?
- **Valideerimine** — mida kontrollitakse frontendis enne API kutset ja millise teatega?
- **API kutsed** — kas iga kutse kontrakt klapib backend taski/koodiga? Kuidas kuvatakse iga backendi veaolukord?
- **Kasutajavoog** — kuhu suunatakse pärast õnnestumist/ebaõnnestumist/tagasi nuppu; kas edu teade kuvatakse?
- **Vastuvõtu kriteeriumid** — kas iga ülaltoodud otsus on kontrollitav kriteeriumina kirjas?

Kui mõnele küsimusele leiad vastuse ise (kood, andmebaas, kokkulepe, seotud task), **ära küsi** — pane see otsuste nimekirja koos allikaga ja maini kokkuvõttes.

Näita kasutajale enne küsimist lühidalt, mitu küsimust ja millistel teemadel sul on (nt "Leidsin 7 lahtist kohta: sisend (2), veaolukorrad (3), andmebaas (1), vastuvõtu kriteeriumid (1). Alustame.").

## 4. Grilli — üks küsimus korraga

- Küsi **üks küsimus korraga** ja oota vastust.
- Iga küsimuse juures:
  - ütle lühidalt, **miks** see on lahtine (mis taskis puudu on või millega see vastuolus on, viitega allikale),
  - paku **oma soovituslik vastus** koos põhjendusega (lähtu projekti kokkulepetest, olemasolevast koodist ja andmebaasist).
- Kui vastus avab uue haru (uue küsimuse), lisa see puusse õigesse kohta.
- Kui vastus muudab mõnda juba tehtud otsust, ütle seda kohe.
- Pea otsuste nimekirja jooksvalt meeles.

Lõpeta grillimine, kui kõik harud on lahendatud või kasutaja ütleb, et aitab.

## 5. Näita otsuste kokkuvõtet

Näita kõik otsused ühe nimekirjana, grupeerituna teemade kaupa, ja iga otsuse juures, mis taski jaotist see muudab. Märgi eraldi otsused, mille leidsid ise (koos allikaga).

Küsi kinnitust enne taski faili muutmist.

## 6. Uuenda taski faili

- Kirjuta otsused taski **olemasolevatesse jaotistesse** (Sisend, Väljund, Veaolukorrad, Käitumine ja valideerimine, Vastuvõtu kriteeriumid jne) — ära lisa eraldi "grillimise" jaotist.
- Säilita taski struktuur ja jaotiste järjekord täpselt nii, nagu selle lõi `skill-loo-backend-task` / `skill-loo-frontend-task`.
- Ära kustuta olemasolevat sisu, mida otsused ei puuduta.

## 7. Teavita seotud failidest

Ära muuda teisi faile ise, vaid ütle kasutajale, mida võiks järgmisena uuendada:

- kui muutus API kontrakt (väli, veaolukord, path) — seotud frontend/backend task, vaate märkmete fail ja mockup (`skill-uus-mockup-markmed`)
- kui task on juba implementeeritud või sellel on implementatsiooni plaan / rAIn-i juhend (`-IMPLEMENTATSIOON.md`, `instructions/…-juhend.md`) — need võivad vajada uuendamist

## Üldised reeglid

- Suhtle kasutajaga eesti keeles.
- Üks küsimus korraga, alati koos soovitusliku vastusega.
- Kui vastuse saab koodibaasist, andmebaasist või kokkulepetest, uuri ise — ära küsi.
- Ära leiuta nõudeid, mida kasutaja pole kinnitanud; soovitus on alati ettepanek, mitte otsus.
- Ära kirjuta koodi — see skill täpsustab ainult taski.
