# Koolituse tõlke tegemine AI abil

**Teenus:** `GET /api/training/{trainingId}/ai-translation?languageId={id}`

**Kasutav vaade:** `TrainingFormView.vue` (`/training-form?trainingId={id}&languageId={id}`, `state: "new-translation"`; `/training-form?trainingId={id}&trainingTranslationId={id}`, `state: "update"`, kui avatud tõlge ei ole põhikeeles) — nupp "Tee AI tõlge" koos tooltip'iga

![Mockup](../../mock-wireframe/pdf-images/TrainingFormView-state-new-translation.png)

## Sisend

**Path variable:**

| Nimi | Tüüp | Kirjeldus |
|---|---|---|
| `trainingId` | Integer | Koolitus, mille põhikeele tõlge tõlgitakse (`training.id`) |

**Query parameeter:**

| Nimi | Tüüp | Kohustuslik | Kirjeldus |
|---|---|---|---|
| `languageId` | Integer | jah | **Sihtkeel**, kuhu tõlgitakse (`language.id`). Ei tohi olla põhikeel. |

Request body puudub. Vormi sisu (admini sisestatud pealkiri, lühikirjeldus, kirjeldus) backendile **ei saadeta** ja seda ei kasutata — lähtetekst võetakse alati andmebaasist.

Näide:

```
GET /api/training/1/ai-translation?languageId=2
```

Koolitus 1 = "Java algkursus", keel 2 = "English" (`3_import.sql`).

## Väljund

**Response (200 OK):** `AiTranslationDto.java` — põhikeele tõlke tekstid sihtkeelde tõlgituna.

| Väli | Tüüp | Kirjeldus |
|---|---|---|
| `title` | String | Tõlgitud pealkiri (mahub `varchar(255)` piiresse) |
| `shortDescription` | String | Tõlgitud lühikirjeldus (mahub `varchar(255)` piiresse) |
| `description` | String | Tõlgitud kirjeldus (richtext / HTML) — HTML-märgendid säilivad muutmata, tõlgitakse ainult tekst |

Näide — koolitus 1 põhikeele (`et`) tõlkest (`training_translation.id = 1`) inglise keelde. AI täpne sõnastus võib erineda:

```json
{
  "title": "Java Beginner Course",
  "shortDescription": "Fundamentals of Java programming for beginners.",
  "description": "The course teaches Java syntax, object-oriented programming and basic data structures."
}
```

Mockupi näidis (`"Power BI for Advanced Users"`) on mockupi väljamõeldud koolitus, mida `3_import.sql`-is pole — ülal on kasutatud reaalset koolitust 1. Mockupis on näitena ka vene keel (`ru`), kuid praeguses andmebaasis on ainult keeled `et` (põhikeel) ja `en`, seega `ru` näidet praeguste andmetega teha ei saa (vt "Avatud küsimused").

**Teenuse eripärad ("API teenuse lisainfo"):**

- Tõlgitakse **alati andmebaasi salvestatud põhikeele** (`language.is_main_language = true`, praegu `et`) tõlge. Põhikeele ID-d (`1`) ega koodi (`"et"`) ei tohi koodis kõvasti kirjas olla — põhikeel leitakse andmebaasist.
- **Andmebaasi midagi ei salvestata** — teenus on puhtalt lugev. Tulemus kuvatakse ainult vormis; andmebaasi salvestab admin selle hiljem nupuga "Lisa tõlge" (`POST /api/training/{trainingId}/training-translation`) või "Salvesta".
- Teenust võib kutsuda ka siis, kui sihtkeele tõlge on juba olemas (`state: "update"`) — olemasolevat tõlget ei loeta ega muudeta.
- `description` HTML-märgendid (`<p>`, `<strong>`, `<ul>`, `<li>`, `<a href="...">` jms) ja nende atribuudid peavad säilima; tõlgitakse ainult märgendite vaheline tekst. Kui lähtetekstis HTML-i pole, ei tohi seda ka vastusesse tekkida.
- Vastusel on HTTP päis **`Cache-Control: no-store`** (iga kutse annab uue AI tulemuse, brauser ega vahepuhver ei tohi vastust hoida).
- Päring võib kesta **mitu sekundit** (AI väljakutse). Frontend näitab selle aja laadimisolekut ("Tõlgin…" spinner).

## AI teenus (Claude / Anthropic API)

Tõlke teeb **Claude** Anthropic API kaudu (Messages API, `POST /v1/messages`).

**Nõuded:**

- Kasutada ametlikku Anthropic Java SDK-d (`com.anthropic:anthropic-java`, `build.gradle`-sse `implementation`), mitte käsitsi kirjutatud HTTP kutset ega OpenAI-ühilduvat vahekihti.
- **Mudel: `claude-sonnet-5`** (otsustatud) — lühikeste koolitustekstide tõlkeks piisav kvaliteet oluliselt väiksema kuluga kui `claude-opus-5`. Mudeli ID tuleb **konfiguratsioonist** (nt `application.properties`: `anthropic.model=claude-sonnet-5`), mitte koodist, et seda saaks hiljem odavama mudeli vastu vahetada. Kasutada ainult täpset ID-d ilma kuupäeva-järelliiteta.
- Tõlge on lihtne ülesanne — `effort` madal (`low` või `medium`, `OutputConfig` sees), `max_tokens` piisavalt suur, et pikk `description` ei lõikuks (nt 16000).
- Vastus tuleb küsida **struktureeritud väljundina** (JSON skeem väljadega `title`, `shortDescription`, `description`; Java SDK-s `outputConfig(<klass>)`), mitte vaba tekstina, mida peaks ise parsima.
- Enne vastuse lugemist kontrollida `stop_reason`-it: kui see on `refusal` või `max_tokens` (vastus pooleli), on tulemus kasutuskõlbmatu → 503 `AI_SERVICE_UNAVAILABLE`.
- Kliendil peab olema mõistlik **timeout** (SDK vaikimisi 10 min on liiga pikk — nt 60 s) ja SDK vaikimisi kordused (2 korda 429/5xx/ühenduse vigade korral) võivad jääda.

**Prompti põhimõtted:**

- Süsteemi juhis: professionaalne koolituste turundusteksti tõlkija; tõlgi lähtekeelest sihtkeelde loomulikult, mitte sõna-sõnalt; ära lisa, jäta välja ega kommenteeri sisu.
- Lähte- ja sihtkeel antakse nime ja koodiga `language` tabelist (nt `Eesti (et)` → `English (en)`).
- `description` puhul: säilita kõik HTML-märgendid ja atribuudid täpselt, tõlgi ainult teksti; URL-e, koodinäiteid ja pärisnimesid (nt "Java", "Scrum") ei tõlgita.
- `title` ja `shortDescription` peavad jääma alla 255 märgi.
- Lähtetekst antakse andmetena (nt eraldi märgendatud plokkides), et selles olevaid lauseid ei tõlgendataks juhistena.

**API võti ja konfiguratsioon:**

- API võti **ei tohi olla koodis ega git'i pandud failis** (`application.properties` on repos). Võti loetakse keskkonnamuutujast `ANTHROPIC_API_KEY` (Java SDK `AnthropicOkHttpClient.fromEnv()` loeb selle ise) või `application.properties` kaudu kujul `anthropic.api-key=${ANTHROPIC_API_KEY}`.
- Arendaja seab keskkonnamuutuja lokaalselt (nt IntelliJ Run Configuration → Environment variables). Kui võti puudub, peab rakendus siiski käivituma — AI teenuse kutse annab siis 503 `AI_SERVICE_UNAVAILABLE`.
- Automaattestid **ei tohi** kutsuda päris Anthropic API-t (maksab raha, vajab võtit, tulemus pole deterministlik) — AI kutse peab olema eraldi komponendis, mida testis saab asendada (mock).

## Eesmärk

Admin lisab või muudab `TrainingFormView` vaates koolituse tõlget mõnes muus keeles kui põhikeel. Käsitsi tõlkimise kiirendamiseks vajutab ta nuppu "Tee AI tõlge" (tooltip: *"Tõlge tehakse salvestatud põhikeele (et) tekstist, mitte vormi sisust. Tulemus kuvatakse ainult vormis — see salvestub alles siis, kui vajutad „Lisa tõlge“ / „Salvesta“."*). Kui vormis on salvestamata muudatusi, küsib frontend enne kinnitust ("Asenda tekst AI tõlkega?"). Teenus tõlgib salvestatud põhikeele tõlke sihtkeelde ja frontend täidab vastusega pealkirja, lühikirjelduse ja kirjelduse väljad; admin kontrollib teksti ja salvestab selle eraldi teenusega. Põhikeele vormis (`state: "new-training"` ja põhikeele `state: "update"`) nuppu ei kuvata.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`. Teenus ainult loeb andmeid — ühtegi rida ei looda ega muudeta.

### training

Ainult `trainingId` olemasolu kontrolliks (`PrimaryKeyNotFoundException`). Näidisandmed: koolitused 1 ("Java algkursus") ja 2 ("Projektijuhtimise põhitõed").

### language

Sihtkeele (`languageId`) olemasolu kontroll ja põhikeele leidmine (`is_main_language = true`; unikaalne osaindeks lubab ainult ühte põhikeelt). Keele `name` ja `code` antakse AI-le promptis.

```sql
CREATE TABLE language
(
    id               serial      NOT NULL,
    code             varchar(2)  NOT NULL,
    name             varchar(50) NOT NULL,
    is_main_language boolean     NOT NULL,
    CONSTRAINT language_pk PRIMARY KEY (id),
    CONSTRAINT language_code_uq UNIQUE (code)
);

-- Põhikeel saab olla ainult üks
CREATE UNIQUE INDEX language_main_language_uq ON language (is_main_language) WHERE is_main_language;
```

Näidisandmed:

| id | code | name | is_main_language |
|---|---|---|---|
| 1 | `et` | Eesti | true |
| 2 | `en` | English | false |

### training_translation

Siit loetakse lähtetekst: rida, kus `training_id = trainingId` ja `language_id` = põhikeele ID (unikaalsuspiirang `(training_id, language_id)` tagab, et rida on max üks).

```sql
CREATE TABLE training_translation
(
    id                serial       NOT NULL,
    training_id       int          NOT NULL,
    language_id       int          NOT NULL,
    title             varchar(255) NOT NULL,
    short_description varchar(255) NOT NULL,
    description       text         NOT NULL,
    created_at        timestamp    NOT NULL,
    updated_at        timestamp    NOT NULL,
    CONSTRAINT training_translation_pk PRIMARY KEY (id),
    CONSTRAINT training_translation_uq UNIQUE (training_id, language_id)
);
```

Näidisandmed (põhikeele read, mida tõlgitakse):

| id | training_id | language_id | title | short_description | description |
|---|---|---|---|---|---|
| 1 | 1 | 1 | Java algkursus | Java programmeerimise alused algajatele. | Kursusel õpitakse Java süntaksit, objektorienteeritud programmeerimist ja põhilisi andmestruktuure. |
| 3 | 2 | 1 | Projektijuhtimise põhitõed | Sissejuhatus IT-projektijuhtimisse. | Kursusel käsitletakse Scrumi, Kanbani ja projekti planeerimise põhimõtteid. |

Sihtkeele olemasolevat tõlget (nt `id = 2`, "Java Basics") teenus ei loe ega muuda.

## Veaolukorrad

Kontrollide järjekord: `trainingId` → `languageId` → sihtkeel on põhikeel → põhikeele tõlge puudub → AI kutse.

| Olukord | Status code | Response body |
|---|---|---|
| `trainingId` ei leidu andmebaasist | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'trainingId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| `languageId` ei leidu andmebaasist | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'languageId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| `languageId` on põhikeel (`is_main_language = true`) | 403 Forbidden | `{ "message": "Põhikeelde ei saa AI tõlget teha", "errorCode": "MAIN_LANGUAGE_NOT_TRANSLATABLE" }` |
| Koolitusel puudub põhikeele `training_translation` rida | 404 Not Found | `{ "message": "Koolitusel puudub põhikeele tõlge", "errorCode": "MAIN_TRANSLATION_NOT_FOUND" }` |
| AI teenus ei vasta, annab vea (nt 429, 5xx, timeout, API võti puudub/vigane), keeldub (`stop_reason: refusal`) või vastus on pooleli/vigane | 503 Service Unavailable | `{ "message": "AI tõlketeenus ei ole hetkel kättesaadav", "errorCode": "AI_SERVICE_UNAVAILABLE" }` |
| `languageId` puudub või ei ole täisarv | 400 Bad Request | Springi standardne vea vastus (projekti globaalne handler seda eraldi ei vorminda) |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

Märkused:

- Kõik 404/403/503 read on mockupi märkmetest (`Veateated`) ja kasutaja kinnitatud.
- `PRIMARY_KEY_NOT_FOUND` read vastavad olemasolevale mustrile `PrimaryKeyNotFoundException` + `getValidTrainingBy(Integer trainingId)` / `getValidLanguageBy(Integer languageId)` (vt `backend/CLAUDE.md`). `PrimaryKeyNotFoundException` paneb `errorCode` ise, seda `Error` enumisse ei lisata.
- 403 → olemasolev `ForbiddenException` (`RestExceptionHandler.handleForbiddenException`); 404 `MAIN_TRANSLATION_NOT_FOUND` → olemasolev `DataNotFoundException` (`handleDataNotFoundException`).
- **503 jaoks praegu käsitlus puudub:** tuleb luua uus erind kausta `infrastructure/exception/` (sama kujuga nagu `ForbiddenException` / `DataNotFoundException`: `message` + `errorCode`) ja lisada `RestExceptionHandler`-isse meetod, mis tagastab `ApiError` staatusega `HttpStatus.SERVICE_UNAVAILABLE`. Anthropic SDK erindid (`com.anthropic.errors.*`) püütakse AI kutse kohas kinni ja teisendatakse selleks erindiks — SDK erind ega API võti ei tohi jõuda vastusesse.
- `Error` enumis (`ee.bcskoolitus.Error`) on praegu ainult `INCORRECT_CREDENTIALS`. Sinna tuleb lisada:
  - `MAIN_LANGUAGE_NOT_TRANSLATABLE("Põhikeelde ei saa AI tõlget teha")`
  - `MAIN_TRANSLATION_NOT_FOUND("Koolitusel puudub põhikeele tõlge")`
  - `AI_SERVICE_UNAVAILABLE("AI tõlketeenus ei ole hetkel kättesaadav")`

## Vastuvõtu kriteeriumid

- [ ] Endpoint `GET /api/training/{trainingId}/ai-translation` on olemas ja võtab kohustusliku query parameetri `languageId`
- [ ] Õnnestunud päring tagastab 200 OK ja `AiTranslationDto` (`title`, `shortDescription`, `description`)
- [ ] Vastusel on päis `Cache-Control: no-store`
- [ ] Lähtetekst on alati andmebaasi salvestatud põhikeele (`is_main_language = true`) tõlge; põhikeele ID ega kood pole koodis kõvasti kirjas
- [ ] Teenus ei loo ega muuda andmebaasis ühtegi rida
- [ ] `description` HTML-märgendid ja atribuudid säilivad; `title` ja `shortDescription` on alla 255 märgi
- [ ] Kasutatakse Anthropic Java SDK-d; mudel (`claude-sonnet-5`) tuleb konfiguratsioonist; vastus küsitakse struktureeritud väljundina
- [ ] API võti ei ole koodis ega repos — see tuleb keskkonnamuutujast `ANTHROPIC_API_KEY`; ilma võtmeta rakendus käivitub ja AI kutse annab 503
- [ ] AI kliendil on mõistlik timeout (nt 60 s)
- [ ] Olematu `trainingId` / `languageId` → 404 `PRIMARY_KEY_NOT_FOUND` õige väljanimega
- [ ] `languageId` on põhikeel → 403 `MAIN_LANGUAGE_NOT_TRANSLATABLE`
- [ ] Koolitusel puudub põhikeele tõlge → 404 `MAIN_TRANSLATION_NOT_FOUND`
- [ ] AI teenuse viga, timeout, keeldumine või pooleli vastus → 503 `AI_SERVICE_UNAVAILABLE` (uus erind + `RestExceptionHandler` käsitlus)
- [ ] Uued errorCode'id on lisatud `Error` enumisse
- [ ] Teenusel on automaattestid, mis ei kutsu päris Anthropic API-t (AI komponent on mockitud)

## Avatud küsimused

1. **Vene keele (`ru`) näide.** Mockupis on sihtkeelena näha vene keel, kuid `3_import.sql`-is on ainult `et` ja `en`. Kas lisada `language` tabelisse `(3, 'ru', 'Русский', false)` (koos keelest sõltuvate tõlketabelite ridadega), või piisab testimiseks inglise keelest?
2. **Mudeli valik ja kulu — otsustatud:** `claude-sonnet-5` (odavam kui `claude-opus-5`, tõlkeks piisav). Mudel on konfiguratsioonis, vajadusel saab vahetada ilma koodimuudatuseta.
3. **Keeldumise automaatne varumudel.** Anthropic soovitab Opus-mudelite puhul lülitada sisse serveripoolse varumudeli (`fallbacks`, beta) juhuks, kui turvafilter päringu tagasi lükkab. Koolituste tekstide puhul on see ebatõenäoline — kas lisada või piisab 503 vastusest?
4. **Autentimine ja kuritarvitus — teadaolev risk (otsustatud).** Backendil pole autentimist — igaüks, kes teab URL-i, saab teha tasulisi AI kutseid. Arenduses aktsepteeritud; **enne avalikku kasutust** on vaja vähemalt päringute piirangut (rate limit) või autentimist/admin-rolli kontrolli.
5. **Frontendi veakäsitlus — lahendatud:** `TrainingFormView.handleGetAiTranslationError()` kuvab `AI_SERVICE_UNAVAILABLE`, `MAIN_LANGUAGE_NOT_TRANSLATABLE` ja `MAIN_TRANSLATION_NOT_FOUND` korral backendi `message` välja vormis (`AlertDanger`), vormi sisu jääb alles; muud vead → `ErrorView`.
6. **Timeout'i väärtus.** Kas 60 sekundit on sobiv? Frontendi axios'el pole timeout'i seatud, seega ootab frontend backendi vastust.
7. **`languageId` puudumise vorming.** Puuduv/vigane query parameeter annab praegu Springi standardse 400 vastuse (mitte `ApiError` kuju). Kas lisada `RestExceptionHandler`-isse käsitlus (`INCORRECT_INPUT`) või jääb see nii?
8. **`backend/CLAUDE.md` ebatäpsus — parandatud:** CLAUDE.md nimetab nüüd enumi õigesti `Error` (`ee.bcskoolitus.Error`).
