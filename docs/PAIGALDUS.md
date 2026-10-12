# BCS koolituste paigaldusjuhend (Docker)

Rakendus koosneb kolmest Docker-teenusest: Postgres (andmebaas), Spring Boot (backend) ja
Caddy (frontend + HTTPS). Kogu paigaldus käib ühe `docker compose` käsuga.

Andmebaasi skeem ja algandmed tulevad kaustast `docs/database` (`1_reset_database.sql`,
`2_create.sql`, `3_import.sql`). Postgres laeb need automaatselt **ainult esimesel käivitusel**,
kui andmebaasi volume (`pgdata`) on tühi — skript `deploy/db-init/00_init.sh`.
Hilisem andmebaasi lähtestamine: vt [UUENDAMINE.md](UUENDAMINE.md#andmebaasi-lähtestamine).

## Praegune paigaldus (oktoober 2026)

| Mis | Väärtus |
|---|---|
| Server | Hetzner Cloud CX23, Ubuntu 24.04 |
| IP | `91.98.82.87` |
| Rakenduse aadress | https://91-98-82-87.sslip.io |
| SSH ligipääs | `ssh root@91.98.82.87` (SSH võti `rain-wsl`) |
| Koodi asukoht serveris | `/root/bcs-koolitused` |
| Saladused | `/root/bcs-koolitused/.env` (vt allpool) |

**NB!** Samas serveris oli varem pdfscanner (`/root/pdfscanner`). See on peatatud
(`docker compose down` ilma `-v`-ta), selle andmed on alles Dockeri volume'is. Mõlemad rakendused
ei saa korraga porte 80/443 kasutada — pdfscanneri uuesti käivitamiseks pane enne bcs kinni:

```bash
ssh root@91.98.82.87 'cd bcs-koolitused && docker compose down && cd ../pdfscanner && docker compose up -d'
```

### Saladuste (.env) vaatamine

Saladused elavad AINULT serveris failis `/root/bcs-koolitused/.env` (git'i see ei lähe):

```bash
ssh root@91.98.82.87 'cat bcs-koolitused/.env'
```

- `POSTGRES_PASSWORD` — andmebaasi parool (backend kasutab seda ise)
- `GOOGLE_AI_API_KEY` — Gemini API võti (AI funktsioonid; ilma selleta backend ei käivitu)
- `SITE_ADDRESS` — avalik aadress (pole saladus)

### Andmebaasile ligipääs

Postgres EI ole internetist kättesaadav — ligipääs käib serveri seest, parooli pole vaja:

```bash
ssh root@91.98.82.87
cd bcs-koolitused
docker compose exec db psql -U postgres vali_it
# psql sees:  SET search_path TO bcs_koolitused;  \dt  jne
```

---

## Eeldused

- Server (nt Hetzner Cloud, Ubuntu 24.04) või ükskõik milline masin, kus on Docker.
- SSH ligipääs serverile.

## 1. Serveri ettevalmistus

(Selles serveris juba tehtud.)

```bash
curl -fsSL https://get.docker.com | sh
ufw allow 22/tcp && ufw allow 80/tcp && ufw allow 443/tcp && ufw --force enable
```

## 2. Kood serverisse

Lokaalsest repo kaustast (WSL):

```bash
rsync -az --delete --exclude node_modules --exclude .git --exclude .env \
  --exclude backend/build --exclude backend/.gradle \
  --exclude frontend/dist --exclude .idea --exclude .claude \
  ./ root@91.98.82.87:bcs-koolitused/
```

## 3. Keskkonnamuutujad (serveris)

```bash
cd bcs-koolitused
cp .env.example .env
nano .env
```

| Muutuja | Väärtus |
|---|---|
| `POSTGRES_PASSWORD` | genereeri: `openssl rand -base64 24` |
| `GOOGLE_AI_API_KEY` | Google AI Studio API võti |
| `SITE_ADDRESS` | `91-98-82-87.sslip.io` (serveri IP sidekriipsudega + `.sslip.io`) |
| `HTTP_PORT` / `HTTPS_PORT` | **jäta serveris kommenteerituks** (vaikimisi 80/443, HTTPS-sertifikaat eeldab neid) |

## 4. Käivitamine

```bash
docker compose up -d --build
```

Esimene build võtab ~5–10 minutit. Kontroll:

```bash
docker compose ps                                  # kõik kolm teenust "running"
docker compose logs db | grep -E "Laen|ERROR"      # andmebaasi skriptid laeti
docker compose logs backend | grep -E "Started|ERROR"
```

Kui HTTPS ei tööta, vaata `docker compose logs web` — sagedasim põhjus on vale `SITE_ADDRESS`
või hõivatud port 80/443 (nt pdfscanner jookseb veel).

## 5. Igapäevased käsud (serveris)

```bash
docker compose logs -f backend        # backend'i logid
docker compose restart backend        # backend'i restart
docker compose down                   # peata kõik (andmed säilivad volume'is)
docker compose up -d                  # käivita uuesti

# Varukoopia
docker compose exec db pg_dump -U postgres vali_it > backup_$(date +%F).sql
# Taastamine varukoopiast (tühja baasi peale)
cat backup_YYYY-MM-DD.sql | docker compose exec -T db psql -U postgres vali_it
```

Uue versiooni paigaldus: [UUENDAMINE.md](UUENDAMINE.md).
