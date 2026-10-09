# Juhend: POST /api/ai-training/pdf ja POST /api/ai-training/pdf/{trainingTranslationId} — Gemini teostus

**Taski fail:** `ai-training-pdf-gemini.md`
**Taustadokument:** `ai-training-pdf-teostusplaan.md`
**Kontroller:** `AiTrainingController.java` (olemas, tagastab praegu placeholder-väärtused)
**Implementeerimise voog:** RestController → Service → (Gemini AI) → Service → RestController
`/pdf/{trainingTranslationId}` puhul faili puudumisel lisaks: Service → Repository → Service

---

## Sissejuhatus

Selle taski eesmärk on asendada kahe AI endpointi placeholder-vastused päris Gemini kutsega: backend saadab PDF-i Geminile, saab vastuseks JSON-i, kontrollib ja puhastab selle ning tagastab olemasoleva `AiTrainingContentDto`. Erinevalt tavalisest POST-ist **ei salvestata midagi andmebaasi** — AI annab ainult ettepaneku vormi jaoks.

Selle harjutuse käigus õpid: kuidas service kutsub välist teenust (AI API) Spring AI `ChatClient`-iga, kuidas välise teenuse vastust **mitte usaldada** (kontroll, kärpimine, puhastus), kuidas välise teenuse vead muuta selgeteks HTTP vigadeks ja kuidas testida koodi, mis sõltub välisest teenusest, ilma seda päriselt kutsumata.

Mapperit selles taskis vaja ei ole — AI vastus läbib enne DTO-ks saamist töötluse, mida MapStruct teha ei oska.

**Eeskuju:** mock `docs/mock-wireframe/loo-mock-vaade/form-view/training-form-view-labimang.html`, skripti jaotis „Päris Gemini AI: PDF → vormi väljad“. Seal on sama loogika JavaScriptis — loe seda kui **kirjeldust**, mitte kopeeritavat koodi.

---

## Samm 0 — ettevalmistus

### Mida teha?

Enne koodi tuleb paika panna kaks asja, ilma milleta ei saa päris PDF-iga katsetada.

**1. Multipart faili suurus.** Springi vaikimisi lubatud faili suurus on 1 MB. Frontend lubab aga kuni 10 MB.

> **Mõtle:** Millised kaks `spring.servlet.multipart.*` omadust tuleb `application.properties` failis seada? Miks peaks päringu kogusuurus olema veidi suurem kui faili suurus?

**2. Mudel.** Globaalne mudel (`gemini-3.1-flash-lite`) jääb chatbotile. Selle päringu jaoks on otsus **Gemini Flash**. Täpse mudeli ID leiad mocki „Päris Gemini AI“ kaardilt nupuga „Laadi mudelid“.

**3. Katseta mockis.** Sisesta mocki oma API võti ja proovi 2–3 päris õppekava PDF-iga ning ühe mitte-õppekava PDF-iga (nt arve). Nii tead, milline hea tulemus välja näeb, enne kui backendis midagi kirjutad.

> **Kontrolli:** Kas keskkonnamuutuja `GEMINI_API_KEY` on IntelliJ käivituskonfiguratsioonis olemas? Chatbot kasutab sama võtit.

---

## Samm 1 — RestController

### Mida teha?

Kontroller on juba olemas: `backend/src/main/java/ee/bcskoolitus/controller/aitraining/AiTrainingController.java`. Mõlemad PDF meetodid tagastavad praegu `getAiTrainingContentPlaceholder()` tulemuse. Selles sammus ühendad **esimese** meetodi (`/pdf`) service klassiga — teine meetod tuleb Samm 5-s.

Kontrolleri ülesanne on ainult päring vastu võtta ja edasi anda. Kogu loogika läheb service'isse.

### Service klassi ettevalmistus

Kontrolli, kas sobiv service klass juba eksisteerib:
- Kaust: `backend/src/main/java/ee/bcskoolitus/service/`
- Taski järgi on nimi `AiTrainingService` — kui puudub, loo see IntelliJ'ga (File → New → Java Class)

```java
@Service
@RequiredArgsConstructor
public class TeenusKlass {
    // ...
}
```

Lisa service muutuja kontrollerisse:

```java
private final TeenusKlass teenuseMuutuja;
```

> **Mõtle:** Kontrolleril pole praegu `@RequiredArgsConstructor` annotatsiooni. Mis juhtub `final` väljaga ilma selleta?

Kutsu service meetod välja. Mõtle, mida service tegelikult vajab — kas kogu `MultipartFile` objekti või ainult faili sisu baitidena?

```java
public TagastatavTüüp meetodiNimi(@RequestPart("osa") MultipartFile fail) {
    return teenuseMuutuja.teenuseMeetod(...);
}
```

> **Vihje:** `MultipartFile`-il on meetod, mis tagastab faili sisu baitidena — vaata **Ctrl+Space** abil, mis meetodeid tal on. Pane tähele, et see meetod võib visata checked exception'i — **Alt+Enter** pakub lahendusi; mõtle, kumb on siin parem.

> **IntelliJ vihje:** Kui service meetod on punasega alla joonitud, vajuta **Alt+Enter** → **"Create method in TeenusKlass"**.

> **Taski nõue:** service meetodi signatuur peab võtma sisendiks **ainult PDF-i baidid** — nii saavad mõlemad endpointid ja hilisem vahemälu sama meetodit kasutada.

### Testid

`AiTrainingControllerTest` loob kontrolleri praegu `new AiTrainingController()` abil. Kui kontrolleril on nüüd konstruktori parameeter, ei kompileeru test enam.

> **Mõtle:** Kuidas anda testis kontrollerile service, ilma et Spring konteksti või Geminit vaja oleks? (Vihje: Mockito `mock(...)`.)

Esialgu võib service meetod tagastada veel placeholder'i — nii jääb kõik roheliseks.

**Kontrolli:** `./gradlew test`

---

## Samm 2 — Service ja prompt

### Mida teha?

Nüüd liigud service klassi. Enne Gemini kutset on vaja **juhist (system prompt)** — teksti, mis ütleb AI-le, mida PDF-ist teha.

### Prompt eraldi failina

Taski otsus: prompt ei ole Java string, vaid eraldi fail `backend/src/main/resources/prompts/training-pdf-system.st`. Sisu võta mocki konstandist `AI_PDF_PROMPT` (see on inglise keeles — mudelid järgivad nii kõige täpsemalt).

> **Vaata projektist:** Kuidas `ChatbotModelService` loeb oma juhiseid failist? Otsi meetodit, mis kasutab `ClassPathResource`-i. Sama muster sobib ka siia.

> **Mõtle:** Kas prompti peaks lugema iga päringu ajal uuesti või üks kord? Mõlemad töötavad — mis on kummagi pluss?

Lisa klassi konstant prompti versiooni jaoks (`PROMPT_VERSION = 1`) — see läheb hiljem logisse.

### (Valikuline) few-shot näide

Kui aega on, lisa promptile näidiskirjeldus failist `docs/JSON/training-description-sample.html`. Vaata `ai-gemini-promptid-ja-skillid.md`, miks see aitab.

---

## Samm 3 — Gemini kutse

### Mida teha?

See on taski peamine töö. Service meetod saadab PDF-i Geminile ja saab vastuseks JSON-i.

### Vastuse record

Gemini vastusel on neli välja — vaata taskifailist JSON näidist. Üks väli (`isCurriculum`) on **ainult backendi sisemine** ja frontendile ei lähe. Seega vajad eraldi sisemist tüüpi, mitte `AiTrainingContentDto`-d.

> **Mõtle:** Kus selline sisemine tüüp peaks elama — eraldi failis või service klassi sees? Vaata, kuidas `ChatbotModelService` oma `ModelAnswer` tüübiga teeb.

### ChatClient

`ChatClient.Builder` süstitakse service klassi nagu `ChatbotModelService`-is. Päringu üldine kuju on ahel:

```java
SisemineTüüp tulemus = chatClientiEhitaja.build()
        .prompt()
        .system(...)        // juhis
        .user(...)          // kasutaja sõnum + PDF
        .options(...)       // ainult selle päringu seaded
        .call()
        .entity(SisemineTüüp.class);
```

Iga rea kohta mõtle ise läbi:

1. **`.user(...)`** — siia läheb lühike tekst **ja** PDF. Sellel meetodil on variant, mis võtab lambda (`u -> u.text(...).media(...)`). `media` vajab MIME tüüpi ja `Resource`-it.
   > **Vihje:** Millise `Resource` implementatsiooniga saab baidimassiivist ressursi teha? Proovi **Ctrl+Space** `new ...Resource(`.
2. **`.options(...)`** — mudel, `temperature`, `maxOutputTokens` seatakse **ainult siin**, mitte `application.properties`-s. Väärtused leiad teostusplaani tabelist „Tehtud otsused“.
   > **Vihje:** Klass asub Spring AI Google GenAI moodulis. Kirjuta `GoogleGenAi` ja vajuta **Ctrl+Space** — vaata, mis klassid pakutakse. Paljudel options-klassidel on `.builder()`.
3. **`.entity(...)`** — Spring AI teisendab JSON-i sinu tüübiks.

> **Mõtle:** Globaalses konfiguratsioonis on `max-output-tokens=1024`. Mis juhtuks täismahus HTML kirjeldusega, kui sa seda üle ei kirjutaks?

### Logimine

Logi mudeli nimi, `PROMPT_VERSION` ja kestus. **Mitte kunagi** PDF-i sisu.

**Valmis, kui:** Swagger UI-st päris PDF-iga saad päris pealkirja, lühikirjelduse ja kirjelduse. (Selles sammus võid ajutiselt tagastada AI vastuse otse DTO-na — kontrollid tulevad järgmises sammus.)

---

## Samm 4 — kontrollid ja järeltöötlus

### Mida teha?

AI vastust ei tohi pimesi usaldada. Selles sammus lisad kontrollid **enne** ja **pärast** Gemini kutset. Eeskuju mockis: `createContentFromPdf`, `geminiJsonData`, `toAiTrainingContent`, `truncateAtWord`.

### Enne Gemini kutset — faili kontroll

Faili suuruse (≤ 10 MB) ja PDF-i tüübi (`%PDF-` algus) kontroll on projektis **juba olemas**: `TrainingTranslationCurriculumService`.

> **Mõtle:** Kuidas seda loogikat taaskasutada ilma koodi kopeerimata? Praegu on kontroll seal `private`. Mis oleks kõige väiksem muudatus, et ka sinu service saaks seda kasutada?

> **Mõtle:** Miks peab see kontroll toimuma **enne** Gemini kutset?

### Pärast Gemini kutset

Mõtle läbi iga juhtum ja otsusta, mida teha. Vaata taskifailist tabelit „Veaolukorrad“:

| Juhtum | Mida teha? |
|---|---|
| `isCurriculum` puudub (`null`) | ? |
| `isCurriculum` on `false` | ? |
| pealkiri või lühikirjeldus on pikem kui 255 märki | ? |
| kirjeldus sisaldab keelatud HTML-i | ? |
| pealkiri on pärast töötlust tühi | ? |
| Gemini viskab erindi / vastus pole korrektne JSON | ? |

> **Vihje kärpimise kohta:** Kärpida tuleb **sõna piirilt**, mitte keset sõna. Vaata mocki `truncateAtWord` — mis on selle loogika sammud? Kirjuta need enne koodi kommentaaridena lahti.

> **Vihje HTML-i kohta:** Projektis on juba klass, mis teeb täpselt seda. Otsi `infrastructure/util/` kaustast.

### Vead

Lisa `Error` enumi uued veakoodid (`PDF_NOT_CURRICULUM`, `AI_SERVICE_UNAVAILABLE`) eestikeelse sõnumiga — vaata olemasolevate ridade mustrit.

> **Mõtle:** Millise exception'iga need visata? Projekti ärivead kasutavad praegu `ForbiddenException`-it (403), kuid lektori vastus staatuskoodide kohta on veel lahtine. Vaata `ChatbotException`-it — see kannab endas HTTP staatust. Kumb sobib 503 jaoks paremini? Märgi lahtine koht koodis `// TODO staatuskood`.

> **Meetodi palve:** kui kutsud välja meetodi, mis midagi tagastab, ja tahad selle tulemusega midagi edasi teha — pane see kohe muutujasse.

### Ajapiirang

Gemini võib vastata 10–40 s. Pärast ~60 s peab tulemuseks olema 503.

> **Mõtle:** Kus seda piirangut seada saab — kas Spring AI / Google GenAI seadetes või Java poolel? Uuri enne, kui otsustad; kordust (retry) ei tehta.

**Valmis, kui:** arve-PDF annab vea ja liiga suur või mitte-PDF fail annab vea **enne** Gemini kutset.

---

## Samm 5 — teine endpoint ja Repository

### Mida teha?

Nüüd `POST /api/ai-training/pdf/{trainingTranslationId}`. Sellel on kaks haru:

- päringus **on** fail → kasuta seda;
- päringus **pole** faili → loe tõlke salvestatud õppekava andmebaasist.

Mõlemad harud peavad lõpuks kutsuma **sama** service meetodit, mille tegid Samm 1–4 käigus.

### Repository

Salvestatud õppekava lugemiseks pole uut repository meetodit vaja.

> **Vaata projektist:** `TrainingTranslationCurriculumService`-is on juba avalik meetod, mis leiab tõlke õppekava ID järgi ja viskab puudumisel 404. Mis see on ja mida see tagastab? Kust saad sealt faili baidid?

> **Mõtle:** Kus peaks see harude valik elama — kontrolleris või service'is? Pea meeles: kontroller ainult delegeerib.

> **Mõtle:** `MultipartFile` on valikuline (`required = false`). Mis kaks olukorda võivad tähendada „faili pole“? (Vihje: `null` ja ... ?)

---

## Samm 6 — tagasi RestController'isse ja Swagger

### Mida teha?

Mõlemad meetodid kutsuvad nüüd service'it. Placeholder-meetodit `getAiTrainingContentPlaceholder()` pole PDF endpointidel enam vaja.

> **Mõtle:** Kas `/translation` endpoint kasutab seda placeholder-meetodit? Kas tohid selle kustutada? (`/translation` ei kuulu selle taski skoopi.)

Uuenda Swaggeri annotatsioone kahel PDF meetodil:
- eemalda `summary`-st `TO BE IMPLEMENTED:` eesliide;
- eemalda `description`-ist laused placeholder-väärtuste kohta;
- lisa `@ApiResponses`-isse uued vead (422 / 503, staatuskoodid vastavalt Samm 4 otsusele).

> **Tähelepanu:** Klassi `@Tag` kirjeldus mainib ka `/translation` endpointi, mis on endiselt TO BE IMPLEMENTED — muuda seda ettevaatlikult.

---

## Samm 7 — testid

### Mida teha?

**Testid ei tohi kutsuda päris AI API-t.** Seetõttu testitakse service'it nii, et `ChatClient` on mockitud.

Kirjuta unit testid vähemalt nendele juhtudele (taski Samm 6 nimekiri):

- vigane JSON / erind → 503;
- `isCurriculum: false` → `PDF_NOT_CURRICULUM`;
- liiga pikk pealkiri kärbitakse sõna piirilt ≤ 255;
- `<script>` ja keelatud märgendid eemaldatakse;
- `/pdf/{id}` ilma failita loeb salvestatud õppekava; selle puudumisel 404.

> **Vihje:** `ChatClient`-i sujuv ahel (`.prompt().system().user()...`) on mockimiseks tülikas — iga samm tagastab uue objekti. Mõtle, kas Mockito `RETURNS_DEEP_STUBS` aitab, või kas Gemini kutse tasuks eraldada eraldi väikesesse meetodisse/klassi, mida on lihtne mockida. Vaata, kuidas projekti olemasolevad service testid (`backend/src/test/java/ee/bcskoolitus/service/`) on üles ehitatud.

**Kontrolli:** `./gradlew test`

---

## Samm 8 — kood ilusaks (refactor)

### Make it work → Make it beautiful

Kui kood töötab, on aeg vaadata, kas saab koodi puhtamaks muuta.

**Extract Method IntelliJ'ga:**

Märgi service meetodis koodilõik, mida soovid eraldada helper meetodiks → paremklõps → Refactor → Extract Method.

> **Tähelepanu:** IntelliJ kasutab ekstraktimisel kogu objekti parameetrina.
> Vaata üle, kas helper meetod vajab tegelikult kogu objekti või ainult üht välja — ja tee vajadusel korrektuur.

Enne:
```java
kontrolliMidagiHelper(vastuseObjekt);

private void kontrolliMidagiHelper(VastuseTüüp vastus) {
    if (vastus.getMingiVäli() == null) {
        throw new MingiException(...);
    }
}
```

Pärast (parem — anna edasi ainult vajalik):
```java
kontrolliMidagiHelper(vastuseObjekt.getMingiVäli());

private void kontrolliMidagiHelper(VäljaTüüp väljaNimi) {
    if (väljaNimi == null) {
        throw new MingiException(...);
    }
}
```

**Head kandidaadid eraldamiseks selles taskis:** Gemini kutse ise, vastuse kontroll, teksti kärpimine.

### Meetodite järjekord

Kontrolli meetodite järjekorda vastavalt Java konventsioonile:
1. `public` meetodid enne
2. `private` meetodid pärast
3. Järjesta ka väljakutsumise hierarhia järgi — peameetod üleval, helper meetodid all

---

## Kokkuvõte ja kontrollnimekiri

Enne kui pead koodi valmis, kontrolli läbi:

- [ ] `application.properties`: multipart piirangud tõstetud; globaalne mudel ja `max-output-tokens` **muutmata**
- [ ] `AiTrainingController`-il on `@RequiredArgsConstructor` ja see ainult delegeerib service'isse
- [ ] `AiTrainingService` on olemas `@Service`, `@RequiredArgsConstructor` annotatsiooniga
- [ ] Service'il on meetod, mis võtab sisendiks ainult PDF-i baidid; mõlemad endpointid kasutavad seda
- [ ] Prompt on failis `resources/prompts/training-pdf-system.st`; `PROMPT_VERSION = 1` koodis
- [ ] Mudel, `temperature` ja `maxOutputTokens` on seatud ainult selle päringu jaoks
- [ ] Faili suuruse ja tüübi kontroll taaskasutab olemasolevat loogikat ja toimub enne Gemini kutset
- [ ] Kõik taskifaili „Veaolukorrad“ juhud on kaetud; uued veakoodid on `Error` enumis
- [ ] Pealkiri ja lühikirjeldus ≤ 255 märki (sõna piirilt), kirjeldus läbinud HTML puhastuse
- [ ] Logis on mudel, prompti versioon ja kestus — mitte PDF-i sisu
- [ ] Andmebaasi ei kirjutata midagi
- [ ] Swaggeris pole kahel PDF endpointil enam `TO BE IMPLEMENTED`
- [ ] Meetodite järjekord: `public` enne, `private` pärast — järjesta ka väljakutsumise hierarhia järgi
- [ ] `./gradlew test` on roheline; testid ei kutsu päris AI API-t

---

> **Järgmine samm:** Testi mõlemat endpointi Swagger UI kaudu (`http://localhost:8080/swagger-ui/index.html`)
> ja vormis (`/training-form`, kõik kolm olekut) 2–3 päris õppekava PDF-i ning ühe mitte-õppekava PDF-iga.
> Kontrolli, et kirjelduses pole infot, mida PDF-is ei ole.
