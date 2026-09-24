# CLAUDE.md

See fail annab juhiseid Claude Code'ile (claude.ai/code) selles repositooriumis töötamiseks.

**Keel:** Kõik uued kanded sellesse faili kirjutatakse eesti keeles.

## Käsud

```bash
# Ehitamine
./gradlew build

# Käivitamine (nõuab lokaalset PostgreSQL-i)
./gradlew bootRun

# Testide käivitamine
./gradlew test

# Ühe testiklassi käivitamine
./gradlew test --tests "ee.minuprojekt.MinuprojektApplicationTests"

# Ainult kompileerimine (käivitab ka MapStructi annotatsiooni töötluse)
./gradlew compileJava
```

## Andmebaasi seadistamine

PostgreSQL peab töötama `localhost`-is järgmiste seadetega:
- Andmebaas: `vali_it`
- Kasutajanimi: `postgres`
- Parool: `student123`
- Mugav DB url: `jdbc:postgresql://localhost:5432/vali_it`

Käivita skriptid järjekorras kaustast `docs/database`:
1. `1_reset_database.sql` — kustutab ja loob uuesti `minu_projekt` skeema
2. `2_create.sql` — loob kõik tabelid
3. `3_import.sql` — lisab algandmed

Kõik tabelid asuvad `minu_projekt` skeemas.

## Arhitektuur

Tegemist on Spring Boot 4.x / Java 21 REST backendiga. Frontend on eraldi Vue 3 SPA (ei ole selles repos).

### Kihtide struktuur

```
controller/       REST endpointid + päringu/vastuse DTOd
service/          Äriloogika
persistence/      JPA entiteedid, repositooriumid, MapStructi mapperid
infrastructure/   Läbivad komponendid: erindi tüübid, veakoodid, globaalne erindite käsitleja
```

Igal domeenialal on oma alampakk `controller/`-is koos DTOdega, teenusklass ja persistence pakk.

### Olulised konventsioonid

**DTOd vs entiteedid** — Kontrollerid näevad ainult DTOsid. MapStructi mapperid (liidesed annotatsiooniga `@Mapper`) teisendavad DTOd JPA entiteetideks ja vastupidi. Genereeritud mapperi implementatsioonid tekivad kausta `src/main/generated/`.

**Veakäsitlus** — Teenustest visatakse kohandatud erindeid (`DataNotFoundException`, `ForbiddenException`, `PrimaryKeyNotFoundException`), mille püüab kinni `RestExceptionHandler` (`@ControllerAdvice`). Kõik äriveateated ja numbrilised veakoodid on koondatud `ErrorResponse` enumi.

**Muutujate nimetamine** — Muutuja nimi peab peegeldama täistüüpi: `EntityDetailDto entityDetailDto`, mitte `EntityDetailDto dto`.

**Meetodi nimetamine** — `getX()` lubab kindlat tagastust. Kui meetod sisaldab tingimislikku loogikat ja muteerib DTO-d, kasuta `handle`-prefiksit ja anna DTO parameeter sisse: `handleAddImageData(EntityDetailDto entityDetailDto, Integer entityId)`.

**Entiteedi otsing ID järgi** — `repository.findById()` kasutamine `orElseThrow`-ga peab olema `public getValid<Entiteet>By(Integer <entiteet>Id)` meetodis vastava service klassi all (nt `getValidEntityBy(Integer entityId)` `EntityService`-s).

**SQL päringud** — Kohandatud päringud on JPQL, kirjutatud otse Spring Data repositooriumi liidesele `@Query` annotatsiooniga. Vajadusel kasutab repositoorium konstruktori avaldist otse DTOsse projekteerimiseks.

**Repositooriumi meetodi nimetamine** — Meetodi nimi peab mainima, mida ta tagastab (subjekti), mitte jääma geneeriliseks: `findFilteredEntitiesBy(...)` tagastab `List<Entity>`, `findUserBy(...)` tagastab `User`. Väldi kujundeid nagu `findFilteredBy(...)`, kust pole näha, mida meetod tagastab.

**Jagatud DTO-d** — DTO klass, mida kasutab rohkem kui üks ressursipakett (nt mitme kontrolleri/mapperi vahel jagatud), ei kuulu ühegi üksiku ressursi `dto/` paketti, vaid paketti `controller/common/dto/` (nt `SharedEntityDto`).

**SQL logimine** — P6Spy on seadistatud (`spy.properties`), nii et täielik parameetritega SQL kuvatakse konsoolis. Selle saab keelata, lülitades `application.properties`-is tagasi kommenteeritud tavaliste PostgreSQL seadetele.

**DTO väljade järjekord** — kui vastuse DTO-l on nii lihtväljad (nt `page`, `totalPages`, `totalElements`) kui massiiv/kollektsioon-väli (nt `content`), paigutatakse lihtväljad massiivi/kollektsioon-väljast (deklaratsioonis ja JSON näidetes) ettepoole. See kehtib nii DTO klassides kui `docs/tasks/backend/` ja `docs/balsamic/notes/` failide JSON näidetes.

### Mitmekeelsus (i18n)

Tõlgitav sisu (nt kategooria nimi, koolituse pealkiri/kirjeldus, lektori CV) hoitakse eraldi `*_translation` tabelites (`category_translation`, `training_translation`, `lecturer_translation`, `option_translation`), kus iga rida on ühe kirje tõlge ühte keelde (`language_id` viide `language` tabelile, unikaalsuspiirang `(<entiteet>_id, language_id)` kohta). Baastabelis endas (nt `category`, `training`) tõlgitavat teksti ei hoita.

`training` tabelil on lisaks eraldi `training_language_id` veerg (viide `language` tabelile) — see väljendab koolituse **õppekeelt** (millises keeles koolitust läbi viiakse), mitte tõlget. See on äriatribuut, mitte tõlke valik, ja ei tohi segi minna tõlketabelite `language_id`-ga.

**Keele valik API-s** — keel tuleb sisse alati **request query parameetrina**, mitte `Accept-Language` HTTP headerina ega path muutujana (valik tehtud teadlikult, kuna osa teenuseid keelt üldse ei puuduta — parameeter lisatakse ainult neisse endpointidesse, mis seda vajavad). Kaks eraldi, mitte omavahel segatavat parameetrit:
- `contentLang` (nt `?translationLang=et`) — määrab, millises keeles tõlgitud tekstiväljad (nt `title`, `shortDescription`, `categoryName`) vastuses tagastatakse (loetakse vastavast `*_translation` tabelist).
- `trainingLang` (nt `?trainingLang=et`) — filtreerib koolitusi nende õppekeele järgi (`training.training_language_id`).

Näide selle mustri rakendusest: vt `docs/balsamic/notes/HomeView-markmed.md` (`GET /api/trainings`, `GET /api/categories`).

### REST API

Baastee: `/api`. Swagger UI on saadaval aadressil `/swagger-ui.html`.

Üksikute teenuste kirjeldused (URL, sisend, väljund, veaolukorrad, vastuvõtu kriteeriumid) ei ole selles failis — need dokumenteeritakse taski failidena kaustas `docs/tasks/backend/`.
