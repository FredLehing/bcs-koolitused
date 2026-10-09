# AI: koolituse vormi täitmine õppekava PDF-ist — Gemini teostus

**Teenused:** `POST /api/ai-training/pdf` ja `POST /api/ai-training/pdf/{trainingTranslationId}`

**Kasutav vaade:** `TrainingFormView.vue`, `/training-form`, nupp „Täida vorm PDF + AI abiga“ (olekud `new-training`, `new-translation`, `update`)

**Vaste mockupis:** [interaktiivne läbimäng](../../mock-wireframe/loo-mock-vaade/form-view/training-form-view-labimang.html) — skripti jaotis **„Päris Gemini AI: PDF → vormi väljad“** on selle taski eeskuju (juhis, JSON skeem, `isCurriculum` kontroll, kärpimine, HTML puhastus).

**Alusdokumendid:**

- [`ai-training-pdf-teostusplaan.md`](ai-training-pdf-teostusplaan.md) — otsused, andmevoog, veaolukorrad, riskid
- [`ai-gemini-promptid-ja-skillid.md`](ai-gemini-promptid-ja-skillid.md) — prompt eraldi failina, few-shot näide
- Placeholder-taskid (valmis): [`POST-api-ai-training-pdf.md`](POST-api-ai-training-pdf.md), [`POST-api-ai-training-pdf-trainingTranslationId.md`](POST-api-ai-training-pdf-trainingTranslationId.md)

## Eesmärk

Asendada `AiTrainingController`-i placeholder-vastused päris Gemini kutsega. Backend saadab PDF-i Geminile, saab vastuseks `{ isCurriculum, title, shortDescription, description }`, kontrollib ja puhastab selle ning tagastab olemasoleva `AiTrainingContentDto`.

**Frontend ei muutu** — nupp, laadimise olek ja veateade on juba olemas. **Andmebaasi ei kirjutata midagi**: AI annab ainult ettepaneku, admin salvestab tavalise „Lisa“ / „Salvesta“ nupuga.

`POST /api/ai-training/translation/{trainingId}` **ei kuulu** selle taski skoopi.

## Sisend ja väljund

Sisend ja väljund on samad mis placeholder-taskides (multipart väli `curriculum`, vastus `AiTrainingContentDto`). Muutub ainult vastuse sisu: fikseeritud tekstide asemel tuleb AI tulemus.

Gemini vastuse kuju (backendi sisemine record, `isCurriculum` frontendile ei lähe):

```json
{
  "isCurriculum": true,
  "title": "Java arendaja algkursus",
  "shortDescription": "Praktiline sissejuhatus Java ja Spring Booti veebiarendusse algajatele.",
  "description": "<p>Koolitus annab ...</p><h3>Sihtrühm</h3><p>...</p><h3>Moodulid</h3><ul><li><p>Java põhitõed</p></li></ul>"
}
```

## Veaolukorrad

| Olukord | Status | Veakood |
|---|---|---|
| Fail puudub / ei ole PDF (`%PDF-` signatuur) | 400 \* | `CURRICULUM_TYPE_NOT_ALLOWED` (olemas) |
| Fail > 10 MB | 413 \* | `CURRICULUM_TOO_LARGE` (olemas) |
| `/pdf/{id}`: faili pole päringus ja tõlkel pole salvestatud õppekava | 404 | — (`PrimaryKeyNotFoundException`) |
| AI: PDF ei ole õppekava (`isCurriculum: false`) | 422 \* | `PDF_NOT_CURRICULUM` (uus) |
| Liiga palju päringuid | 429 | `AI_RATE_LIMITED` (uus, valikuline samm 6) |
| Gemini ei vasta / ajapiirang (~60 s) / vigane JSON / tühi pealkiri / vastus lõigati katki | 503 | `AI_SERVICE_UNAVAILABLE` (uus) |

\* **Lahtine küsimus lektorile** (teostusplaani küsimus 1): projekt viskab ärivead praegu `ForbiddenException`-iga (403). Kuni vastuseni kasuta olemasolevaid erindeid ja märgi koodi `// TODO staatuskood`; staatuskoodi vahetus on hiljem ühe rea muudatus. Eeskuju erindist, millel on oma HTTP staatus: `ChatbotException` + `ChatbotModelService.unavailable()`.

## Teostus sammhaaval

Iga samm lõpeb töötava seisuga — võid peatuda ja commit'ida pärast iga sammu.

### Samm 1 — ettevalmistus (≈ 30 min)

- [x] `application.properties`: `spring.servlet.multipart.max-file-size=10MB` ja `spring.servlet.multipart.max-request-size=11MB` (vaikimisi 1 MB → üle 1 MB PDF annaks 413).
- [x] API võti on keskkonnamuutujas `GOOGLE_API_KEY` (IntelliJ käivituskonfiguratsioon; `application.properties` viitab sellele, varem `GEMINI_API_KEY`).
- [x] Vali mudel: globaalne mudel muudetud → `gemini-3.5-flash-lite` (kehtib ka chatbotile; branch masterisse ei lähe). Kas PDF päring saab eraldi mudeli, otsustatakse Samm 3-s.
- [x] Katseta mockis oma API võtmega 2–3 päris õppekava PDF-i ja ühe mitte-õppekava PDF-iga (nt arve). *(katsetatud: õppekava PDF täidab vormi, mitte-õppekava PDF → `PDF_NOT_CURRICULUM`)* Kui vaja, häälesta mockis `AI_PDF_PROMPT`-i.

**Valmis, kui:** tead mudeli ID-d ja prompt annab mockis hea tulemuse.

### Samm 2 — prompt ja teenuse skelett (≈ 1 h)

- [x] Kopeeri mocki `AI_PDF_PROMPT` faili `backend/src/main/resources/prompts/training-pdf-system.st` (inglise keeles, nagu mockis).
- [ ] *(Edasi lükatud — tagasi pärast Samm 3 käsitsi katsetamist, kui kirjeldused vajavad parandamist)* (Valikuline) lisa promptile few-shot näide `docs/JSON/training-description-sample.html` põhjal (vt `ai-gemini-promptid-ja-skillid.md`).
- [x] Loo `service/AiTrainingService.java` meetodiga `AiTrainingContentDto createContentFromPdf(byte[] pdf)` — esialgu tagastab veel placeholder'i.
- [x] Konstant `PROMPT_VERSION = 1`.
- [x] Ühenda controller teenusega (`curriculum.getBytes()` → teenus). Controlleris ainult delegeerimine, loogika teenuses.

**Valmis, kui:** `./gradlew test` on roheline (`AiTrainingControllerTest` võib vajada teenuse mocki).

### Samm 3 — Gemini kutse, `/pdf` otsast lõpuni (≈ 3–4 h, peamine töö)

- [ ] `ChatClient` (`ChatClient.Builder` süstitakse nagu `ChatbotModelService`-is):
  - `.system(...)` — prompt failist (`ClassPathResource`, vt `ChatbotModelService.readResource`);
  - `.user(u -> u.text("Create the training form fields from this PDF.").media(<application/pdf>, new ByteArrayResource(pdf)))` — Gemini loeb PDF-i natiivselt, PDFBox-i pole vaja;
  - **ainult selle päringu** valikud: mudel (Flash), `temperature` 0.2, `maxOutputTokens` 8192, vastus JSON-ina. Klass on Spring AI Google GenAI moodulis (`GoogleGenAiChatOptions` vms — kontrolli `build.gradle` sõltuvuse järgi täpset nime);
  - `.call().entity(AiPdfResult.class)` — sisemine record `AiPdfResult(Boolean isCurriculum, String title, String shortDescription, String description)`.
- [ ] Logi mudeli nimi, `PROMPT_VERSION`, kestus ja tokenid (**mitte** PDF-i sisu).

**Valmis, kui:** Swaggerist / vormist päris PDF-iga saad vormi päris pealkirja, lühikirjelduse ja vormindatud kirjelduse, mis näeb TipTap editoris hea välja.

### Samm 4 — kontrollid ja järeltöötlus (≈ 2–3 h)

Eeskuju: mocki funktsioonid `createContentFromPdf`, `geminiJsonData`, `toAiTrainingContent`, `truncateAtWord`.

- [ ] Enne Geminit: suuruse (≤ 10 MB) ja `%PDF-` signatuuri kontroll — **taaskasuta** `TrainingTranslationCurriculumService` loogikat (`MAX_CURRICULUM_BYTES`, `PDF_SIGNATURE`); vajadusel tõsta kontroll eraldi avalikuks meetodiks, ära kopeeri.
- [ ] `isCurriculum == null` → 503; `isCurriculum == false` → `PDF_NOT_CURRICULUM`.
- [ ] `title` ja `shortDescription`: tühikud normaliseerida, kärpida 255 märgini **sõna piirilt** (turvavõrk `varchar(255)` jaoks).
- [ ] `description` → `HtmlSanitizer.sanitizeDescription(...)`.
- [ ] Tühi pealkiri pärast töötlust → 503.
- [ ] Iga Gemini viga (erind, vigane JSON, katkine vastus) → 503 `AI_SERVICE_UNAVAILABLE`. **Kordust pole.**
- [ ] Ajapiirang ~60 s → 503.
- [ ] Lisa `Error` enumi `PDF_NOT_CURRICULUM` ja `AI_SERVICE_UNAVAILABLE` eestikeelsete sõnumitega.

**Valmis, kui:** arve-PDF annab vea ja vorm jääb puutumata; liiga suur või mitte-PDF fail annab vea enne Gemini kutset.

### Samm 5 — `/pdf/{trainingTranslationId}` (≈ 1–2 h)

- [ ] Kui päringus on `curriculum` → kasuta seda.
- [ ] Muidu loe salvestatud fail: `TrainingTranslationCurriculumService.getValidTrainingTranslationCurriculumBy(id).getFile()` (puudumisel 404).
- [ ] Mõlemad harud kutsuvad **sama** `createContentFromPdf(byte[])`.

**Valmis, kui:** olekus `update` töötab nupp nii uue valitud failiga kui ilma (salvestatud õppekavaga).

### Samm 6 — viimistlus (≈ 2–3 h)

- [ ] Unit testid mockitud `ChatClient`-iga (**päris AI API-t testides ei kutsuta**):
  - vigane JSON / erind → 503;
  - `isCurriculum: false` → `PDF_NOT_CURRICULUM`;
  - liiga pikk pealkiri kärbitakse sõna piirilt ≤ 255;
  - `<script>` ja keelatud märgendid eemaldatakse kirjeldusest;
  - `/pdf/{id}` ilma failita loeb salvestatud õppekava; selle puudumisel 404.
- [ ] Swagger: eemalda `TO BE IMPLEMENTED` kahelt PDF teenuselt ja kirjeldusest placeholder-laused; lisa uued veakoodid (422/503, vajadusel 429).
- [ ] (Valikuline, jääb esimesena ära) mälupõhine päringute piirang, nt 10 päringut/min IP kohta → 429 `AI_RATE_LIMITED`.
- [ ] Showcase'i läbimäng: A → PDF + AI → „Lisa“ (vt läbimängu sissejuhatust).

## Vastuvõtu kriteeriumid

- [ ] Mõlemad PDF teenused tagastavad päris Gemini tulemuse `AiTrainingContentDto` kujul.
- [ ] AI päring ei loe ega kirjuta andmebaasi, v.a `/pdf/{id}` salvestatud õppekava lugemine.
- [ ] Mudel, `temperature`, `maxOutputTokens` on seatud ainult selle päringu jaoks; globaalne konfiguratsioon ja chatbot ei muutu.
- [ ] Prompt on failis `resources/prompts/training-pdf-system.st`, `PROMPT_VERSION = 1` koodis.
- [ ] Kõik tabeli „Veaolukorrad“ juhud on kaetud; frontendis jääb vea korral vorm ja PDF alles.
- [ ] Pealkiri ja lühikirjeldus ≤ 255 märki, kirjeldus läbinud `HtmlSanitizer`-i.
- [ ] Üle 1 MB PDF (kuni 10 MB) läheb läbi.
- [ ] `./gradlew test` on roheline; testid ei kutsu päris AI API-t.
- [ ] Käsitsi kontrollitud 2–3 päris õppekava + 1 mitte-õppekava PDF-iga; kirjelduses pole PDF-ist puuduvat infot.

## Lahtised küsimused (vt teostusplaani „Küsimused lektorile“)

1. HTTP staatuskoodid: 403 projekti kokkuleppe järgi või 400 / 413 / 422 / 429?
2. Väljundi keel: PDF-i keel (praegune otsus) või vormi keel (`languageId`)?
3. Päringute piirang: kas showcase'iks vajalik?

**Kontroll:** `cd backend && ./gradlew test`
