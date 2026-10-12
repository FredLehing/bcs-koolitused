# Uue versiooni viimine serverisse

Juhend olukorraks, kus lähtekood on lokaalselt muutunud ja muudatus peab jõudma serverisse
(https://91-98-82-87.sslip.io). Sammud saab käsitsi läbi teha või paluda Claude'il ära teha
("vii uus versioon serverisse" → `skill-deploy-server`).

**Andmed EI lähe tavalisel uuendamisel kaduma.** Postgresi andmed elavad Dockeri volume'is
(`pgdata`), mida uuendamine ei puuduta. **Aga:** projektil pole migratsioone (Flyway) —
kui `docs/database/2_create.sql` või `3_import.sql` muutus, ei jõua see muudatus olemasolevasse
serveri andmebaasi iseenesest. Siis on kaks võimalust:

- lähtesta andmebaas (kõik serveri andmed asendatakse skriptide algandmetega) —
  [Andmebaasi lähtestamine](#andmebaasi-lähtestamine), Claude'iga `skill-deploy-server-drop-db`;
- või jooksuta muudatus käsitsi `ALTER TABLE ...` lausena (nagu `4_feedback_long_comments.sql`).

---

## Sammud

### 1. Kontrolli lokaalselt

```bash
cd backend && ./gradlew build          # backend kompileerub + testid läbivad
cd frontend && npm run lint            # frontendi kontrollid
```

Frontendi buildi kontroll WSL-ist käib scratchpadi koopias (vt juur-`CLAUDE.md`
"Frontendi build WSL-ist"). Riskantse muudatuse eel tee varukoopia:

```bash
ssh root@91.98.82.87 'cd bcs-koolitused && docker compose exec -T db pg_dump -U postgres vali_it' > backup_$(date +%F).sql
```

### 2. Kopeeri kood serverisse

```bash
rsync -az --delete --exclude node_modules --exclude .git --exclude .env \
  --exclude backend/build --exclude backend/.gradle \
  --exclude frontend/dist --exclude .idea --exclude .claude \
  ./ root@91.98.82.87:bcs-koolitused/
```

`--exclude .env` on kohustuslik — serveril on oma `.env` päris saladustega. `--delete` eemaldab serverist failid, mis lokaalselt kustutati (nt galerii pildid); `--exclude`-itud teid (`.env`, `node_modules` jm) see ei puuduta.

### 3. Ehita ja käivita serveris uuesti

```bash
ssh root@91.98.82.87 'cd bcs-koolitused && docker compose up -d --build'
```

Postgres jääb püsti; backend ja web ehitatakse uuesti (~1–3 min, ~30 s katkestus).

### 4. Kontrolli

```bash
ssh root@91.98.82.87 'cd bcs-koolitused && docker compose ps && docker compose logs backend --since 2m | grep -E "Started|ERROR"'
curl -s -o /dev/null -w "%{http_code}\n" https://91-98-82-87.sslip.io/   # peab olema 200
```

Ja ava rakendus brauseris ning kontrolli muudetud kohta.

## Andmebaasi lähtestamine

Kustutab `bcs_koolitused` skeemi ja laeb `docs/database` skriptid 1–3 uuesti (sama skript, mis
esmakäivitusel). **Kõik serveris tehtud andmemuudatused kaovad.**

```bash
# (soovi korral enne varukoopia — vt samm 1)
ssh root@91.98.82.87 'cd bcs-koolitused && docker compose stop backend \
  && docker compose exec -T db bash /docker-entrypoint-initdb.d/00_init.sh \
  && docker compose up -d --build'
```

Eeldab, et uus kood (sh `docs/database`) on enne rsync'iga serverisse viidud (samm 2) —
skriptid loetakse serveri koodikaustast.

## Tagasipööramine

```bash
git log --oneline -5                 # leia eelmine töötav commit
git checkout <commit>
# korda samme 2–3
```

## Andmete turvalisus

**Ohutu:** `docker compose up -d --build`, `docker compose down` + `up -d`, `restart backend`, serveri reboot.

**OHTLIK (andmed kaovad!):** `docker compose down -v` (kustutab volume'id, sh kogu andmebaasi;
järgmisel käivitusel laetakse algandmed), andmebaasi lähtestamine, serveri kustutamine.
Enne iga ohtlikku käsku `pg_dump` varukoopia.
