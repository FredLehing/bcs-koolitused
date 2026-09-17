---
name: skill-loo-uus-oppematerjal
description: Loo uus beginner-friendly õppematerjal eesti keeles ja salvesta docs/theory-materials kausta.
---

# Loo uus õppematerjal

Loo algajasõbralik õppematerjal eesti keeles ja salvesta see `docs/theory-materials` kausta.

## Steps

### 1. Küsi teema

Küsi kasutajalt: **"Millisel teemal soovid uut materjali luua? (nt "CSS struktuur ja kasutamine")"**

Oota vastust enne kui jätkad.

### 2. Kontrolli olemasolevaid materjale

- Vaata `docs/theory-materials` kausta (`Glob: docs/theory-materials/**/*`)
- Kui sama teema kohta on juba fail olemas, teavita kasutajat ja küsi, kas uuendada olemasolevat või luua eraldi fail.
- Vaata ka eelnevaid materjale (nt `html-sissejuhatus.md`) et hoida stiil ja raskusaste ühtne.

### 3. Arvesta vestluse konteksti

- Vaata, mida kasutaja on selles vestluses varem õppinud või küsinud.
- Kui materjal seondub eelnevalt käsitletud teemaga (nt HTML → CSS → JS), viita sellele loogilisele järjekorrale.
- Hoia materjali raskusaste kooskõlas sellega, mida kasutaja juba teab.

### 4. Koosta materjal

Materjal peab sisaldama:

- **Selge pealkiri** eesti keeles (`# Teema – Algajatele` või sarnane)
- **Lühike sissejuhatus** — mis see asi on, miks see oluline on, päriselust analoogia
- **Visuaalsed skeemid** ASCII-ga, kus sobiv (struktuur, suhted, voog)
- **Koodinäited** — lihtsad, kommenteeritud, reaalsed
- **Päriselu analoogiad** — võrdle tuntud asjadega (maja, retsept, liiklus vms)
- **Kokkuvõte tabelina** — võtmemõisted lühidalt
- **Järgmised sammud** — mida edasi õppida

Stiilireeglid:
- Eesti keeles (tehnilised terminid võivad jääda inglise keeles, aga selgita neid)
- Lihtsad laused, ei eelda eelteadmisi
- Iga uus mõiste selgitatakse enne kasutamist
- Kasutada konkreetseid näiteid, mitte abstraktseid kirjeldusi

### 5. Failinime valik

Failinime formaat: `teema-alateema.md` (väiketähed, sidekriipsud, eesti keeles)

Näited:
- `css-sissejuhatus.md`
- `css-flexbox.md`
- `javascript-muutujad.md`
- `git-põhitõed.md`

### 6. Salvesta fail

Salvesta fail `docs/theory-materials/<failinime>.md`.

### 7. Teavita kasutajat

Näita kasutajale:
- Faili asukoht
- Lühike kokkuvõte (2-3 lauset), mida materjal katab
- Küsi, kas midagi jäi puudu või vajab täiendamist
