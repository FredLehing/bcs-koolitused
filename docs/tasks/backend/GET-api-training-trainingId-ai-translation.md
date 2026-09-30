# Koolituse tõlke tegemine AI abil

**Teenus:** `GET /api/training/{trainingId}/ai-translation?languageId={id}`

**Kasutav vaade:** `TrainingFormView.vue` (`/training-form?trainingId={id}&languageId={id}`, `state: "new-translation"`; `/training-form?trainingId={id}&trainingTranslationId={id}`, `state: "update"`, kui avatud tõlge ei ole põhikeeles) — nupp "Tee AI tõlge" koos tooltip'iga

![Mockup](../../mock-wireframe/pdf-images/TrainingFormView-state-new-translation.png)

**Staatus:**

| Osa | Seis |
|---|---|
| API leping (sisend, väljund, veaolukorrad) | **Valmis** — frontend kasutab seda juba (mock-vastusega), muuta ei tohi |
| Tõlgitava sisu kirjeldus (mida AI sisendiks saab) | **Valmis** — tuleneb kirjelduse richtext lahendusest |
| AI tehniline lahendus | **WIP** — suund on otsustatud (**Spring AI + Google Gemini**, Google AI Studio API võti), detailid täpsustuvad (vt "Avatud küsimused") |

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
| `title` | String | Tõlgitud pealkiri (lihttekst, mahub `varchar(255)` piiresse) |
| `shortDescription` | String | Tõlgitud lühikirjeldus (lihttekst, mahub `varchar(255)` piiresse) |
| `description` | String | Tõlgitud kirjeldus (HTML) — märgendid ja atribuudid säilivad, tõlgitakse ainult tekst; läbib enne tagastamist `HtmlSanitizer`-i |

Näide — koolitus 1 põhikeele (`et`) tõlkest (`training_translation.id = 1`) inglise keelde. AI täpne sõnastus võib erineda:

```json
{
  "title": "Java Beginner Course",
  "shortDescription": "Fundamentals of Java programming for beginners.",
  "description": "The course teaches Java syntax, object-oriented programming and basic data structures."
}
```

Vormindatud kirjelduse puhul (nt `docs/JSON/training-description-sample.html`) on `description` sama struktuuriga HTML:

```json
{
  "title": "Junior AI Developer",
  "shortDescription": "Full-stack development and AI in one programme.",
  "description": "<p>The Junior AI Developer retraining programme is a <strong>seven-week intensive in-person course</strong> …</p><h4>What will you learn?</h4><ul><li><p><strong>Front-end development:</strong> HTML, CSS, JavaScript and Vue.js.</p>…</li></ul>…"
}
```

Mockupi näidis (`"Power BI for Advanced Users"`) on mockupi väljamõeldud koolitus, mida `3_import.sql`-is pole. Mockupis on näitena ka vene keel (`ru`), kuid andmebaasis on ainult `et` (põhikeel) ja `en` (vt "Avatud küsimused").

**Teenuse eripärad:**

- Tõlgitakse **alati andmebaasi salvestatud põhikeele** (`language.is_main_language = true`, praegu `et`) tõlge. Põhikeele ID-d (`1`) ega koodi (`"et"`) ei tohi koodis kõvasti kirjas olla.
- **Andmebaasi midagi ei salvestata** — teenus on puhtalt lugev. Tulemus kuvatakse ainult vormis; andmebaasi salvestab admin selle hiljem nupuga "Lisa tõlge" (`POST /api/training/{trainingId}/training-translation`) või "Salvesta".
- Teenust võib kutsuda ka siis, kui sihtkeele tõlge on juba olemas (`state: "update"`) — olemasolevat tõlget ei loeta ega muudeta.
- Vastusel on HTTP päis **`Cache-Control: no-store`** (iga kutse annab uue AI tulemuse).
- Päring võib kesta **mitu sekundit**. Frontend näitab selle aja laadimisolekut ("Tõlgin…" spinner).

## Tõlgitav sisu (AI sisend)

See osa kirjeldab, **mida täpselt AI-le tõlkimiseks antakse** — sõltumata sellest, milline mudel või teek tõlke teeb. Allikas: kirjelduse richtext lahendus (`docs/tasks/frontend/training-description-richtext.md`, `docs/tasks/backend/training-description-html-sanitize.md`) ja prototüübi tulemused.

### `title` ja `shortDescription`

- **Lihttekst**, mitte HTML; max 255 märki (`varchar(255)`).
- Tõlge peab samuti mahtuma 255 märgi sisse — inglise tõlge on sageli eestikeelsest pikem.

### `description`

**HTML, mis on alati läbinud `HtmlSanitizer`-i** (salvestamisel). Seega on sisendis **ainult** need märgendid:

| Märgend | Tähendus | Märkus AI jaoks |
|---|---|---|
| `p` | lõik | |
| `br` | reavahetus lõigu sees | |
| `h3`, `h4` | alapealkirjad | tõlgitakse |
| `strong`, `em`, `u` | paks, kaldkiri, allajoonitud | võivad olla pesastatud (`<strong><em>…</em></strong>`), vormindus peab jääma **samale sõnale/fraasile** ka tõlkes |
| `ul`, `ol`, `li` | loendid | **iga `li` sees on `<p>`** (`<li><p>…</p></li>`) — TipTapi omadus; loendid võivad olla **pesastatud** (`li` sees uus `ul`/`ol`) |
| `a` | link | kujul `<a target="_blank" rel="noopener noreferrer nofollow" href="https://…">tekst</a>`; **`href` ja teised atribuudid jäävad muutmata**, tõlgitakse ainult lingi tekst |

Muud omadused:

- **Escape'itud märgid:** tekstis võivad olla HTML-entiteedid (`&amp;`, `&lt;`, `&nbsp;`) — need peavad jääma kehtivaks HTML-iks.
- **Lihttekst ilma märgenditeta** on samuti võimalik (vanad kirjeldused `3_import.sql`-ist) — siis **ei tohi vastusesse HTML-i tekkida**.
- **Pikkus:** realistlik kirjeldus on ~3,5 k märki (`docs/JSON/training-description-sample.html`). Arvesta sisendi + väljundi tokenite mahtu (väljund on sisendiga sarnase pikkusega, HTML-märgendid kaasa arvatud). Eraldi maksimumpikkust praegu pole (vt `training-description-html-sanitize.md` avatud küsimus 1).
- **Võimalik prügi:** ChatGPT-st kleebitud sisus võib olla allikaviidete jääke (nt tekst "Pasted text" lõigu lõpus), kui admin neid ei eemaldanud. AI ei pea neid eemaldama — tõlgib nagu tavalist teksti (või jätab muutmata); sisu eest vastutab admin.
- **Mittetõlgitav:** URL-id, tehnoloogiate ja toodete nimed (nt "Java", "Spring", "Vue.js", "PostgreSQL", "Scrum"), lühendid (nt "REST", "RAG", "MCP").

### AI väljundi nõuded

- Sama HTML-struktuur mis sisendis: samad märgendid, sama pesastus, sama loendielementide arv. Märgendeid ei lisata ega eemaldata.
- **AI väljund on usaldamatu** — `description` läbib enne tagastamist sama `HtmlSanitizer.sanitizeDescription(...)`.
- Kui AI tagastab katkise HTML-i, parandab jsoup selle puhastamisel süntaktiliselt (sulgeb märgendid) — eraldi viga ei visata.

## AI teenus: Spring AI + Google Gemini — WIP

**Otsustatud:**

- Teek: **Spring AI** (`ChatClient`), mitte pakkuja oma SDK ega käsitsi HTTP kutse. Põhjus: ühtne Spring Booti konfiguratsioon, struktureeritud väljund (`entity(...)`) ja pakkujat saab hiljem vahetada.
- Pakkuja: **Google Gemini** Gemini Developer API kaudu, API võti **Google AI Studiost** (<https://aistudio.google.com/app/apikey>). Mitte Vertex AI (vajaks Google Cloudi projekti).

**Sõltuvus** (Spring Boot 4.x → Spring AI 2.x; dokumentatsioon kontrollitud versiooni 2.0.1 järgi):

```gradle
dependencyManagement {
    imports {
        mavenBom "org.springframework.ai:spring-ai-bom:<Spring AI 2.x versioon>"
    }
}

dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-google-genai'
}
```

Viide: <https://docs.spring.io/spring-ai/reference/api/chat/google-genai-chat.html>

**Konfiguratsioon** (`application.properties`):

```properties
spring.ai.google.genai.api-key=${GEMINI_API_KEY}
spring.ai.google.genai.chat.model=gemini-2.5-flash
spring.ai.google.genai.chat.temperature=0.2
spring.ai.google.genai.chat.max-output-tokens=8192
```

- **Mudel tuleb konfiguratsioonist**, mitte koodist. `gemini-2.5-flash` on Spring AI vaikimisi mudel ja tõlkeks piisav; saadaolevad mudelid muutuvad kiiresti — vali implementeerimisel AI Studios kättesaadav Flash-mudel.
- **API võti ei tohi olla koodis ega git'is** (`application.properties` on repos) — ainult keskkonnamuutujast `GEMINI_API_KEY` (IntelliJ Run Configuration → Environment variables).
- **Madal temperatuur** — tõlge peab olema täpne, mitte loominguline.

**Struktureeritud väljund:** vastus küsitakse otse `AiTranslationDto` kujul:

```java
ResponseEntity<ChatResponse, AiTranslationDto> responseEntity = chatClient.prompt()
        .system(...)
        .user(...)
        .call()
        .responseEntity(AiTranslationDto.class);
```

`responseEntity(...)` (mitte `entity(...)`) annab ligipääsu ka `ChatResponse` metaandmetele — vaja lõpetamise põhjuse kontrolliks.

**Lõpetamise põhjus:** kontrollida `chatResponse.getResult().getMetadata().getFinishReason()`. Kasutatav on ainult normaalne lõpp (Gemini `STOP`). `MAX_TOKENS` (vastus pooleli), `SAFETY` / `RECITATION` jms (keeldumine) → 503 `AI_SERVICE_UNAVAILABLE`.

**Prompti põhimõtted:**

- Süsteemi juhis: professionaalne koolituste turundusteksti tõlkija; tõlgi loomulikult, mitte sõna-sõnalt; ära lisa, jäta välja ega kommenteeri sisu.
- Lähte- ja sihtkeel antakse nime ja koodiga `language` tabelist (nt `Eesti (et)` → `English (en)`).
- `description`: reeglid jaotisest "AI väljundi nõuded" (säilita märgendid, atribuudid ja `href`; ära lisa HTML-i lihtteksti; ära tõlgi nimesid ja lühendeid).
- `title` ja `shortDescription`: lihttekst, alla 255 märgi.
- Lähtetekst antakse **andmetena** (eraldi märgendatud plokkides), et selles olevaid lauseid ei tõlgendataks juhistena.

**Arhitektuur:**

- AI kutse on **eraldi komponendis** (nt `service/ai/AiTranslationClient`), mis kasutab `ChatClient`-i. `TrainingService` (või eraldi teenus) teeb kontrollid, loeb andmebaasist, kutsub klienti ja puhastab `description`-i.
- Automaattestid **ei tohi** kutsuda päris Gemini API-t (vajab võtit, kvoot, tulemus pole deterministlik) — `AiTranslationClient` mockitakse.
- Spring AI / Google SDK erindid püütakse AI kliendis kinni ja teisendatakse `AiServiceUnavailableException`-iks — SDK erind ega API võti ei tohi jõuda vastusesse.

**Andmekaitse (Google AI Studio tasuta tase):** tasuta taseme puhul võib Google kasutada saadetud sisu oma toodete arendamiseks. Koolituste kirjeldused on avalik turundustekst, seega on see aktsepteeritav — **isikuandmeid ega konfidentsiaalset sisu sellesse teenusesse saata ei tohi.** Tasuta tasemel on ka päringute piirangud (rate limit) → ületamisel 503.

## Eesmärk

Admin lisab või muudab `TrainingFormView` vaates koolituse tõlget mõnes muus keeles kui põhikeel. Käsitsi tõlkimise kiirendamiseks vajutab ta nuppu "Tee AI tõlge" (tooltip: *"Tõlge tehakse salvestatud põhikeele (et) tekstist, mitte vormi sisust. Tulemus kuvatakse ainult vormis — see salvestub alles siis, kui vajutad „Lisa tõlge“ / „Salvesta“."*). Kui vormis on salvestamata muudatusi, küsib frontend enne kinnitust ("Asenda tekst AI tõlkega?"). Teenus tõlgib salvestatud põhikeele tõlke sihtkeelde ja frontend täidab vastusega pealkirja, lühikirjelduse ja kirjelduse (richtext editor) väljad; admin kontrollib teksti ja salvestab selle eraldi teenusega. Põhikeele vormis (`state: "new-training"` ja põhikeele `state: "update"`) nuppu ei kuvata.

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

`3_import.sql` kirjeldused on lihttekst. Vormindatud HTML-kirjelduse testimiseks lisa koolitus editori kaudu või kasuta näidist `docs/JSON/training-description-sample.html`.

Sihtkeele olemasolevat tõlget (nt `id = 2`, "Java Basics") teenus ei loe ega muuda.

## Veaolukorrad

Kontrollide järjekord: `trainingId` → `languageId` → sihtkeel on põhikeel → põhikeele tõlge puudub → AI kutse.

| Olukord | Status code | Response body |
|---|---|---|
| `trainingId` ei leidu andmebaasist | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'trainingId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| `languageId` ei leidu andmebaasist | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'languageId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| `languageId` on põhikeel (`is_main_language = true`) | 403 Forbidden | `{ "message": "Põhikeelde ei saa AI tõlget teha", "errorCode": "MAIN_LANGUAGE_NOT_TRANSLATABLE" }` |
| Koolitusel puudub põhikeele `training_translation` rida | 404 Not Found | `{ "message": "Koolitusel puudub põhikeele tõlge", "errorCode": "MAIN_TRANSLATION_NOT_FOUND" }` |
| AI teenus ei vasta või annab vea (nt kvoot/rate limit 429, 5xx, timeout, API võti puudub/vigane), keeldub (`SAFETY` jms), vastus on pooleli (`MAX_TOKENS`) või ei vasta `AiTranslationDto` kujule | 503 Service Unavailable | `{ "message": "AI tõlketeenus ei ole hetkel kättesaadav", "errorCode": "AI_SERVICE_UNAVAILABLE" }` |
| `languageId` puudub või ei ole täisarv | 400 Bad Request | Springi standardne vea vastus (projekti globaalne handler seda eraldi ei vorminda) |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

Märkused:

- Kõik 404/403/503 read on mockupi märkmetest (`Veateated`) ja kasutaja kinnitatud. Frontend (`TrainingFormView.handleGetAiTranslationError()`) kuvab need kolm errorCode'i vormis — **nende kuju ei tohi muutuda**.
- `PRIMARY_KEY_NOT_FOUND` read vastavad olemasolevale mustrile `PrimaryKeyNotFoundException` + `getValidTrainingBy(Integer trainingId)` / `getValidLanguageBy(Integer languageId)` (vt `backend/CLAUDE.md`). `PrimaryKeyNotFoundException` paneb `errorCode` ise, seda `Error` enumisse ei lisata.
- 403 → olemasolev `ForbiddenException`; 404 `MAIN_TRANSLATION_NOT_FOUND` → olemasolev `DataNotFoundException`.
- **503 jaoks praegu käsitlus puudub:** luua uus erind `infrastructure/exception/AiServiceUnavailableException` (sama kujuga nagu `ForbiddenException` / `DataNotFoundException`: `message` + `errorCode`) ja lisada `RestExceptionHandler`-isse meetod, mis tagastab `ApiError` staatusega `HttpStatus.SERVICE_UNAVAILABLE`.
- `Error` enumisse (`ee.bcskoolitus.Error`) lisada:
  - `MAIN_LANGUAGE_NOT_TRANSLATABLE("Põhikeelde ei saa AI tõlget teha")`
  - `MAIN_TRANSLATION_NOT_FOUND("Koolitusel puudub põhikeele tõlge")`
  - `AI_SERVICE_UNAVAILABLE("AI tõlketeenus ei ole hetkel kättesaadav")`

## Vastuvõtu kriteeriumid

- [ ] Endpoint `GET /api/training/{trainingId}/ai-translation` on olemas ja võtab kohustusliku query parameetri `languageId`
- [ ] Õnnestunud päring tagastab 200 OK ja `AiTranslationDto` (`title`, `shortDescription`, `description`)
- [ ] Vastusel on päis `Cache-Control: no-store`
- [ ] Lähtetekst on alati andmebaasi salvestatud põhikeele tõlge; põhikeele ID ega kood pole koodis kõvasti kirjas
- [ ] Teenus ei loo ega muuda andmebaasis ühtegi rida
- [ ] `description` HTML-struktuur säilib (märgendid, pesastatud loendid, lingid koos `href`-iga); lihttekstist ei teki HTML-i; `description` läbib `HtmlSanitizer`-i
- [ ] `title` ja `shortDescription` on lihttekst ja alla 255 märgi
- [ ] Kasutatakse Spring AI `ChatClient`-i ja `spring-ai-starter-model-google-genai` starterit; mudel tuleb konfiguratsioonist; vastus küsitakse struktureeritud väljundina
- [ ] API võti tuleb keskkonnamuutujast `GEMINI_API_KEY`, mitte koodist ega repost
- [ ] Olematu `trainingId` / `languageId` → 404 `PRIMARY_KEY_NOT_FOUND` õige väljanimega
- [ ] `languageId` on põhikeel → 403 `MAIN_LANGUAGE_NOT_TRANSLATABLE`
- [ ] Koolitusel puudub põhikeele tõlge → 404 `MAIN_TRANSLATION_NOT_FOUND`
- [ ] AI viga, kvoodi ületamine, timeout, keeldumine, pooleli või vigase kujuga vastus → 503 `AI_SERVICE_UNAVAILABLE`
- [ ] Uued errorCode'id on lisatud `Error` enumisse
- [ ] Teenusel on automaattestid, mis ei kutsu päris Gemini API-t (AI klient on mockitud)
- [ ] Käsitsi test: `docs/JSON/training-description-sample.html` sisuga koolituse tõlge inglise keelde säilitab struktuuri ja lingid

## Avatud küsimused (WIP)

1. **Rakenduse käivitumine ilma API võtmeta.** Kui `spring.ai.google.genai.api-key` puudub, lülitub Spring AI Google GenAI klient Vertex AI režiimi (nõuab `project-id` ja `location`) — tõenäoliselt ei käivitu rakendus siis üldse. Õpilastel ja CI-s võtit pole. Variandid: lokaalne profiil `spring.ai.model.chat=none` + AI klient puuduva `ChatClient`-i korral 503; või võti alati nõutav. **Kontrollida implementeerimisel.**
2. **Mudel.** `gemini-2.5-flash` (vaikimisi) vs uuem Flash/Flash-Lite mudel — valida AI Studios implementeerimise ajal saadaoleva ja tasuta tasemel lubatu hulgast.
3. **Thinking-režiim.** Gemini 2.5+ mudelid "mõtlevad" vaikimisi, mis lisab viivitust ja võib kulutada väljundi tokeneid (`MAX_TOKENS` risk). Tõlkeks pole seda vaja — kontrollida, kas Spring AI kaudu saab thinking-eelarve nulli/madalaks seada.
4. **Timeout ja kordused.** Mis on mõistlik ooteaeg (nt 60 s) ja kas Spring AI vaikimisi kordused (retry) sobivad? Frontendi axios'el timeout'i pole.
5. **`response-mime-type=application/json`.** Kas `responseEntity(AiTranslationDto.class)` vajab seda eraldi või piisab Spring AI väljundi konverteri juhistest? Kontrollida, et pikk HTML `description` JSON-stringis ei lähe katki (jutumärgid, reavahetused).
6. **Vene keele (`ru`) näide.** Mockupis on sihtkeelena vene keel, andmebaasis ainult `et` ja `en`. Kas lisada `language` tabelisse `ru` või piisab inglise keelest?
7. **Autentimine ja kuritarvitus — teadaolev risk.** Backendil pole autentimist — igaüks, kes teab URL-i, saab kulutada AI kvooti. Arenduses aktsepteeritud; **enne avalikku kasutust** on vaja päringute piirangut või admin-rolli kontrolli.
8. **`languageId` puudumise vorming.** Puuduv/vigane query parameeter annab Springi standardse 400 vastuse (mitte `ApiError` kuju). Kas lisada `RestExceptionHandler`-isse käsitlus (`INCORRECT_INPUT`)?
