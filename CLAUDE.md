# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

**Keel:** Kõik uued kanded projekti CLAUDE.md failidesse kirjutatakse eesti keeles.

## Ülevaade

See on Vali-IT pangaäpi monorepo: Spring Boot backend + Vue 3 frontend.

```
backend/    Spring Boot 4.x / Java 21 REST API — vt backend/CLAUDE.md
frontend/   Vue 3 + Vite SPA — vt frontend/CLAUDE.md
docs/       Dokumentatsioon ja andmebaasiskriptid
```

**Backend ja frontend on eraldi arendatavad ja käivitatavad rakendused** — igaühel on oma CLAUDE.md alamkaustas koos täpsete ehitus-/käivitus-/testikäskudega, arhitektuuri ja koodikonventsioonidega. Enne kummaski kaustas töötamist loe vastav CLAUDE.md.

## Claude Code'i keskkond (WSL2/Ubuntu)

Claude Code terminal jookseb WSL2 Ubuntu sees, mitte Windowsi peal — Windowsi tööriistad (nt IntelliJ, Docker Desktop) ei ole siit kättesaadavad. Vali-IT installer paigaldab õpilase masinal WSL2 sisse kindlalt järgmised tööriistad.

**apt-paketid:**

- `rg` (ripgrep), `jq`, `tree`, `curl`, `unzip` — otsingu-/abitööriistad
- `git` — versioonihaldus
- `python3`, `pip3` — Python 3 ja selle paketihaldur
- `java` (OpenJDK 21) — backendi jaoks
- `pdfinfo`, `pdftoppm`, `pdftotext` (poppler-utils) — PDF-tööriistad, nt mockupi PDF-i lehekülgede piltideks või tekstiks teisendamiseks
- `psql` — **ainult klient**, andmebaasi server ise jookseb Windowsis (port 5432, `localhost:5432` kaudu kättesaadav WSL2-st)

**Eraldi paigaldusloogikaga tööriistad:**

- `gh` — GitHub CLI
- `nvm` — Node'i versioonihaldur
- `node`, `npm` — Node.js LTS (NVM kaudu paigaldatud, eraldi versioon Windowsi Node'ist)
- `claude` — Claude Code

Docker **puudub** WSL-i seest natiivselt (projekti kokkulepe) — ainult Windows Docker Desktopi kaudu, kui õpilane on selle käsitsi sisse lülitanud.

## Sub-agentide kasutamine

Kui ülesanne on suur ja jaguneb sõltumatuteks osadeks (nt mitu eraldi backend taski, laiaulatuslik koodiotsing), kaalu tavaliste sub-agentide kasutamist (mitte fork — fork kasutab alati sama mudelit). Väikesed, omavahel tihedalt seotud või ainult vestluse kontekstist sõltuvad tööd tee ise.

**Enne sub-agentide käivitamist peatu ja küsi kasutajalt kinnitust.** Näita:
1. millisteks alamülesanneteks töö jagatakse ja mis järjekorras (mis käib paralleelselt, mis järjestikku);
2. iga agendi ülesande lühikokkuvõtet ja milliseid faile ta aluseks võtab — agent vestlust ei näe, seega otsused peavad olema failides või ülesande tekstis;
3. soovitatud mudelit koos põhjendusega:
   - **Haiku** — failide otsimine, koodi uurimine, lihtsad mehaanilised muudatused;
   - **Sonnet** — tavaline kood olemasoleva mustri järgi (nt lihtne nimekirjateenus);
   - **Opus** — keerukas loogika (transaktsioonid, mitu tabelit, veakäsitlus, välised teenused, arhitektuur).

Käivita agendid alles pärast kasutaja kinnitust. Paralleelsed agendid ei tohi muuta samu faile. Enne kasutajale raporteerimist vaata agentide tulemus üle (kompileerimine, testid) ja anna kasutajale lühike kokkuvõte.

## docs/ kausta struktuur

- `docs/theory-materials/` — algajasõbralikud õppematerjalid (Java, Spring, Vue, HTML/CSS teemadel), iga teema kohta nii `.md` kui vastav `.html`
- `docs/transcript-materials/` — õppevideote transkriptidest genereeritud õppematerjalid (`.md`, kuupäeva-video numbriga nimetatud)
- `docs/transcripts/` — õppevideote toored transkriptid (`.vtt`), millest `transcript-materials/` genereeritakse
- `docs/database/` — PostgreSQL skeemi skriptid (`1_reset_database.sql`, `2_create.sql`, `3_import.sql`), mida käivitatakse backendi lokaalseks seadistamiseks (vt backend/CLAUDE.md andmebaasi jaotist)
- `docs/structure/` — projekti struktuuri dokumendid (`backend-projekti-struktuur.md`, `frontend-projekti-struktuur.md`, `frontend-vue-komponendi-struktuur.md`); kirjeldavad soovituslikku, üldist struktuuri, mitte tingimata hetke koodi seisu
- `docs/mock-wireframe/` — rakenduse mockup/wireframe (tööriistast sõltumatu):
  - `pdf/` — mockupist eksporditud PDF
  - `pdf-images/` — mockupi lehekülgede pildid, failinimi `<Vaade>.png` (nt `TrainingsView.png`); sama vaate teise lehe pilt vajadusel `<Vaade>-<lehekülg>.png` (nt `TrainingsView-2.png`). Pildid luuakse PDF-ist taski loomise käigus (`skill-loo-backend-task`, `skill-loo-frontend-task`) ja neid hoitakse ainult siin — taskid viitavad neile, koopiaid ei tehta
  - `kokkulepped/mock-wireframe-markmete-struktuur.md` — mockupi "Vaate märkmete" ja "API märkmete" kokkulepitud struktuur
  - `loo-mock-vaade/` — uute (veel mockupis puuduvate) vaadete planeerimisfailid: vaate plaan, skeemid (Mermaid) ja Balsamiq AI käsk, mille põhjal vaade mockupisse genereeritakse (nt `training-form-view-skeemid.md`)
  - `markmed/` — selle struktuuri järgi koostatud märkmed, üks fail vaate kohta (`<vaate-nimi>-markmed.md`, nt `home-view-markmed.md`) koos vaate kõigi märkmetega — Vaate märkmed ja API märkmed (`skill-uus-mockup-markmed` väljund). Erand: mitme olekuga vaatel võib olla eraldi fail iga oleku kohta (`<vaate-nimi>-state-<olek>-markmed.md`, nt `training-form-view-state-update-markmed.md`), mis sisaldab selle oleku Vaate märkmeid ja kõiki oleku API märkmeid
- `docs/tasks/backend/` — backend teenuste taskid (`.md`, failinimi teenuse URL-ist, nt `GET-api-trainings.md`; mockupi pilt viitega `pdf-images/` kausta), loodud `skill-loo-backend-task` abil
  - `instructions/` — taskide samm-sammulised lahendusjuhendid (`<taskifailinimi>-juhend.md`, `skill-rain-ai-backend` väljund)
- `docs/tasks/frontend/` — frontend vaadete taskid (`.md`, failinimi vaatest, nt `TrainingsView.vue` → `trainings-view.md`; mockupi pilt viitega `pdf-images/` kausta), loodud `skill-loo-frontend-task` abil
- `docs/JSON/` — API päringute/vastuste JSON näidised
- `docs/claude-code/` — Claude Code töötoa materjalid ja checklist

Kogu dokumentatsiooni sisu (sh uued failid) peab olema eestikeelne.
