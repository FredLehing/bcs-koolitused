---
name: skill-deploy-server-drop-db
description: Vii lokaalsed muudatused serverisse (https://91-98-82-87.sslip.io) ja lähtesta serveri andmebaas — bcs_koolitused skeem kustutatakse ja docs/database skriptid 1–3 laetakse uuesti. Kasuta, kui kasutaja ütleb "deploy'i server ja lähtesta andmebaas", "kustuta db ja vii uus versioon", "drop db deploy" vms — st kui 2_create.sql või 3_import.sql on muutunud.
---

# Uue versiooni viimine serverisse koos andmebaasi lähtestamisega

Sama protsess mis [`skill-deploy-server`](../skill-deploy-server/SKILL.md), aga enne rebuild'i lähtestatakse andmebaas skriptiga `deploy/db-init/00_init.sh`, mis jooksutab `docs/database/1_reset_database.sql`, `2_create.sql`, `3_import.sql`.

**See kustutab kogu serveri andmebaasi sisu** — alles jäävad ainult `3_import.sql` algandmed.

Enne käivitamist teavita kasutajat lühidalt (varukoopia küsimus → lokaalne kontroll → rsync → **andmebaasi lähtestamine** → rebuild → tervisekontroll).

## Samm 0 — Küsi varukoopia kohta

**Küsi kasutajalt otse, kas ta soovib enne `pg_dump` varukoopiat.** Ära eelda vastust. Kui soovib (repo juurkaustast):

```bash
ssh root@91.98.82.87 'cd bcs-koolitused && docker compose exec -T db pg_dump -U postgres vali_it' > backup_$(date +%F_%H%M).sql
```

Kinnita, kus varukoopia asub, enne kui jätkad.

## Samm 1 — Lokaalne kontroll

```bash
cd backend && ./gradlew build
cd frontend && npm run lint
```

Frontendi build scratchpadi koopias (juur-`CLAUDE.md`). Kui midagi ebaõnnestub, peata.

## Samm 2 — Kopeeri kood serverisse

```bash
rsync -az --delete --exclude node_modules --exclude .git --exclude .env \
  --exclude backend/build --exclude backend/.gradle \
  --exclude frontend/dist --exclude .idea --exclude .claude \
  ./ root@91.98.82.87:bcs-koolitused/
```

## Samm 3 — Peata backend ja lähtesta andmebaas

```bash
ssh root@91.98.82.87 'cd bcs-koolitused && docker compose stop backend && docker compose exec -T db bash /docker-entrypoint-initdb.d/00_init.sh'
```

Väljundis peavad olema read `Laen 1_reset_database.sql`, `Laen 2_create.sql`, `Laen 3_import.sql` ilma `ERROR`-ita. Kui skript feilib, peata ja näita viga (backend on peatatud — ütle seda kasutajale).

## Samm 4 — Ehita ja käivita uuesti

```bash
ssh root@91.98.82.87 'cd bcs-koolitused && docker compose up -d --build'
```

## Samm 5 — Kontrolli tulemust

```bash
ssh root@91.98.82.87 'cd bcs-koolitused && docker compose ps && docker compose logs backend --since 3m | grep -E "Started|ERROR"'
curl -s -o /dev/null -w "%{http_code}\n" https://91-98-82-87.sslip.io/
```

`curl` peab tagastama `200`. Probleemi korral raporteeri koos logidega, ära paranda ise.

Kokkuvõttes ütle, kas deploy ja lähtestamine õnnestusid, ja tuleta meelde, et serveris on nüüd ainult `3_import.sql` algandmed.

## Andmete turvalisus

- Ära käivita lähtestamist ilma Sammus 0 küsimata.
- Ära lähtesta enne, kui Samm 1 on läbinud.
- `docker compose down -v` pole vaja ega tohi kasutada — lähtestamine käib skeemi tasemel.
