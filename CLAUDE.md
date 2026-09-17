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

Claude Code terminal jookseb WSL2 Ubuntu sees, mitte Windowsi peal — Windowsi tööriistad (nt IntelliJ, Docker Desktop) ei ole siit kättesaadavad. Vali-IT õpilase masinal on WSL2 sees kindlalt olemas:

- `rg` (ripgrep), `jq`, `tree`, `curl`, `unzip` — otsingu-/abitööriistad
- `java` (OpenJDK 21) — backendi jaoks
- `node`, `npm` (NVM kaudu paigaldatud, eraldi versioon Windowsi Node'ist)
- `gh` (GitHub CLI)
- `psql` — **ainult klient**, andmebaasi server ise jookseb Windowsis (port 5432, `localhost:5432` kaudu kättesaadav WSL2-st)
- Docker **puudub** WSL-i seest natiivselt (ainult Windows Docker Desktopi kaudu, kui õpilane on selle käsitsi sisse lülitanud)

## docs/ kausta struktuur

- `docs/theory-materials/` — algajasõbralikud õppematerjalid (Java, Spring, Vue, HTML/CSS teemadel), iga teema kohta nii `.md` kui vastav `.html`
- `docs/transcript-materials/` — õppevideote transkriptidest genereeritud õppematerjalid (`.md`, kuupäeva-video numbriga nimetatud)
- `docs/transcripts/` — õppevideote toored transkriptid (`.vtt`), millest `transcript-materials/` genereeritakse
- `docs/database/` — PostgreSQL skeemi skriptid (`1_reset_database.sql`, `2_create.sql`, `3_import.sql`), mida käivitatakse backendi lokaalseks seadistamiseks (vt backend/CLAUDE.md andmebaasi jaotist)
- `docs/frontend/` — frontendi arhitektuuri dokumendid (nt projekti struktuur, Vue komponendi struktuur)
- `docs/claude-code/` — Claude Code töötoa materjalid ja checklist

Kogu dokumentatsiooni sisu (sh uued failid) peab olema eestikeelne.
