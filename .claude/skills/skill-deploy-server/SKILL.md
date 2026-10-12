---
name: skill-deploy-server
description: Vii lokaalsed muudatused serverisse (https://91-98-82-87.sslip.io) — kontrollib koodi, kopeerib rsync'iga, ehitab ja käivitab uuesti Dockeriga. Andmebaasi andmed säilivad. Kasuta, kui kasutaja ütleb "vii uus versioon serverisse", "deploy'i server", "uuenda serverit" vms.
---

# Uue versiooni viimine serverisse

Järgib juhendit [`docs/UUENDAMINE.md`](../../../docs/UUENDAMINE.md). Sammud käivitatakse järjest ilma vahepealsete kinnitusküsimusteta.

Enne käivitamist teavita kasutajat lühidalt, mida tehakse (lokaalne kontroll → rsync serverisse → Docker rebuild → tervisekontroll), ja alusta kohe.

## Samm 0 — Andmebaasi muudatuste kontroll

Projektil pole migratsioone. Kontrolli, kas `docs/database/2_create.sql` või `3_import.sql` on muutunud võrreldes eelmise deploy'ga (nt `git log --oneline -5 -- docs/database`, `git diff` kasutajaga arutades). Kui skeem muutus, ütle kasutajale, et tavaline deploy ei too muudatust serveri andmebaasi, ja paku valikut: `skill-deploy-server-drop-db` (andmed lähtestatakse) või käsitsi `ALTER` lause. Ära jätka enne kasutaja otsust.

## Samm 1 — Lokaalne kontroll

```bash
cd backend && ./gradlew build
cd frontend && npm run lint
```

Frontendi buildi kontrolli scratchpadi koopias (juur-`CLAUDE.md` "Frontendi build WSL-ist") — **mitte** otse `frontend/` kaustas. Kui midagi ebaõnnestub, peata ja näita viga. Ära vii katkist koodi serverisse.

## Samm 2 — Kopeeri kood serverisse

Repo juurkaustast:

```bash
rsync -az --delete --exclude node_modules --exclude .git --exclude .env \
  --exclude backend/build --exclude backend/.gradle \
  --exclude frontend/dist --exclude .idea --exclude .claude \
  ./ root@91.98.82.87:bcs-koolitused/
```

`--exclude .env` on kohustuslik — serveril on oma `.env` päris saladustega. `--delete` eemaldab serverist failid, mis lokaalselt kustutati (nt galerii pildid); `--exclude`-itud teid (`.env`, `node_modules` jm) see ei puuduta.

## Samm 3 — Ehita ja käivita serveris uuesti

```bash
ssh root@91.98.82.87 'cd bcs-koolitused && docker compose up -d --build'
```

## Samm 4 — Kontrolli tulemust

```bash
ssh root@91.98.82.87 'cd bcs-koolitused && docker compose ps && docker compose logs backend --since 3m | grep -E "Started|ERROR"'
curl -s -o /dev/null -w "%{http_code}\n" https://91-98-82-87.sslip.io/
```

`curl` peab tagastama `200` ja logis peab olema `Started BcskoolitusApplication`. Kui logis on `ERROR` või konteiner pole `running`, peata ja raporteeri kasutajale koos logiväljavõttega — ära paranda ise ilma arutamata.

Kokkuvõttes ütle, kas deploy õnnestus, ja tuleta meelde rakendust brauseris üle kontrollida.

## Tagasipööramine

Ainult kasutaja selgel palvel: `git checkout <eelmine commit>` ja korda Samme 2–3.

## Andmete turvalisus

Selle skilli käigus ei tohi kunagi jooksutada `docker compose down -v` ega andmebaasi lähtestamist — selleks on `skill-deploy-server-drop-db`.
