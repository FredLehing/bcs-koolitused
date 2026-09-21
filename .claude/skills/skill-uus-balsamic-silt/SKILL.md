---
name: skill-uus-balsamic-silt
description: Pane olemasolevatest andmetest kokku Balsamiq mockup'i selgitav märkme-silt (Vaate märkmed või API märkmed) ja salvesta see docs/balsamic/notes/ kausta. Kasuta, kui kasutaja tahab luua uue balsamic sildi, balsamic märkmed, vaate märkmed, API märkmed, või soovib olemasolevat mockup silti/märget uuendada või täiendada.
---

# Loo uus Balsamiq märkme-silt

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

## 4. Uuri andmebaasi, et saada õiged väärtused

Kasuta järgmisi allikaid, et JSON näidised, ID-d ja väljad oleks kooskõlas päris andmebaasi seisuga (mitte väljamõeldud):

- `docs/database/2_create.sql` — tabelistruktuur, veerud, tüübid, PK/FK seosed. Sellest tuletad ka väljanimed ja seosed DTO-de vahel.
- `docs/database/3_import.sql` — reaalsed näidisandmed (INSERT read). Kasuta neid JSON näidiste väärtusteks (id-d, nimed), mitte väljamõeldud andmeid.

Kui kasutaja on juba andnud konkreetse JSON näidise (nt tekstina või pildilt), eelista seda, aga kontrolli väärtuste (id-de, nimede) kooskõla `3_import.sql`-ga — kui ei klapi, teavita kasutajat lahknevusest ja küsi, kumba kasutada.

## 5. ID-väljade nimetamise reegel

Kõik JSON näidistes esinevad primary key / foreign key väljad peavad sisaldama subjekti nime, mitte olema anonüümne `id`.

- Halb näide: `"id": 2`
- Hea näide: `"cityId": 2`, `"locationId": 5`, `"transactionTypeId": 1`

See kehtib nii Vaate märkmete sees mainitud andmete kui API märkmete request/response JSON näidiste kohta.

## 6. Koosta sisu struktuuridokumendi järgi

Järgi täpselt `docs/balsamic/notes/balsamiq-markmete-struktuur.md` struktuuri ja reegleid vastava märkme tüübi jaoks (Vaate märkmed või API märkmed) — väljade järjekord, tühjade ridade paigutus, `—` kasutamine kui lisainfot pole, DTO nime paiknemine vahetult body ploki kohal, veateadete kolmerealine formaat jne.

Kui koostad mitut API märget sama vaate jaoks, koosta iga API kutse kohta eraldi plokk.

## 7. Vorminda copy-paste jaoks

Kogu lõplik märkme tekst (Vaate märkmed plokk ja/või iga API märkmete plokk) peab olema esitatud eraldi Markdown koodiblokina (` ```text ` piiritlejatega), täpselt nagu struktuuridokumendi näidetes — nii saab kasutaja sisu otse Balsamiq kollasesse/valgesse kasti copy-pastida.

## 8. Salvesta fail

Salvesta tulemus `.md` failina kausta `docs/balsamic/notes/`.

**Failinimi:**
- Tuleta failinimi automaatselt vaate/teenuse nimest, ilma kasutajalt küsimata (v.a kui tuletus jääb ebaselgeks):
  - Vaate märkmete puhul: `<ComponentName>-markmed.md` (nt `LocationView-markmed.md`)
  - API märkmete puhul: `<METOOD>-<path-sidekriipsudega>-markmed.md` (nt `POST-atm-locations-markmed.md`)
  - Eestikeelsed täpitähed translitereeri (õ→o, ä→a, ö→o, ü→u, š→s, ž→z)
- Uuendamise puhul: kui kasutaja andis olemasoleva faili path'i, kirjuta samasse faili. Kui kasutaja andis ainult vaate/teenuse nime või pildi, otsi kõigepealt `docs/balsamic/notes/` kaustast, kas sobiva nimega fail juba eksisteerib, ja kui jah, küsi kasutajalt kinnitust, kas seda täiendada, enne kui üle kirjutad.

Kui samasse faili tuleb nii vaate märkmed kui mitu API märget (nt kui kasutaja soovib kogu vaate kohta korraga kõik sildid), pane need üksteise järele samas failis, iga plokk oma pealkirja all (nt `## Vaate märkmed`, `## API märkmed — POST /api/atm/locations`).

## 9. Teavita kasutajat

Näita:
- Loodud/uuendatud faili path
- Lühike kokkuvõte, mis sisu loodi
- Küsi, kas midagi jäi puudu, on ebatäpne, või vajab täiendamist

## Üldised reeglid

- Suhtle kasutajaga eesti keeles.
- Ära leiuta andmeid — kasuta alati kasutaja antud materjali ja `docs/database/` failide reaalset sisu.
- ID-väljad JSON näidistes olgu alati subjektiga (nt `locationId`, mitte `id`).
- Kui struktuuridokumendi mõni reegel ja kasutaja soov lähevad vastuollu, järgi struktuuridokumenti ja too see kasutajale välja.
