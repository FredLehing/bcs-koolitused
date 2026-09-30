# Koolituse kirjelduse HTML puhastamine ja valideerimine

**Teenused:** kõik, mis salvestavad või tagastavad `training_translation.description` sisu admini sisendist või AI-st:

| Teenus | Seis | Mida teha |
|---|---|---|
| `POST /api/training` | Implementeeritud | Lisada puhastus ja valideerimine |
| `PUT /api/training/{trainingId}` | Task olemas (`PUT-api-training-trainingId.md`), implementeerimata | Rakendada sama muster implementeerimisel |
| `POST /api/training/{trainingId}/training-translation` | Task olemas (`POST-api-training-trainingId-training-translation.md`), implementeerimata | Rakendada sama muster implementeerimisel |
| `GET /api/training/{trainingId}/ai-translation` | Task olemas, implementeerimata | AI vastuse `description` puhastada enne tagastamist |

**Kasutav vaade:** `TrainingFormView.vue` — kirjelduse richtext editor (`docs/tasks/frontend/training-description-richtext.md`)

## Eesmärk

Frontend saadab `description` väljas nüüd **HTML-i** (TipTap editori väljund). Backendil pole autentimist, seega võib keegi saata API-le otse suvalist HTML-i (nt `<script>`, `onerror=`, `javascript:` lingid). Kirjeldus kuvatakse hiljem kliendivaates `v-html`-iga — seega peab andmebaasi jõudma **ainult lubatud märgenditega puhas HTML**. Frontendi DOMPurify kuvamisel on teine kaitsekiht, mitte ainus.

Lisaks peab valideerimine aru saama, et `<p></p>` on sisuliselt tühi kirjeldus.

Andmebaasi skeem **ei muutu** (`description text NOT NULL`), API request/response kujud ei muutu.

## Lubatud HTML (Safelist)

Sama nimekiri mis frontendi editoris — **muutmisel muuta mõlemat**:

| Märgend | Atribuudid |
|---|---|
| `p`, `br`, `strong`, `em`, `u`, `h3`, `h4`, `ul`, `ol`, `li` | — |
| `a` | `href` (ainult `http`, `https`, `mailto`); `target="_blank"` ja `rel="noopener noreferrer nofollow"` **seatakse backendi poolt alati** |

Kõik muu eemaldatakse: märgendid (`script`, `img`, `table`, `span`, `div`, `h1`…), atribuudid (`style`, `class`, `on*`) ja lubamatud protokollid (`javascript:`). Lubamatu märgendi **tekst jääb alles** (nt `<span>tekst</span>` → `tekst`), `<script>` sisu kaob.

## Tehniline lahendus

### Sõltuvus

`build.gradle`: `implementation 'org.jsoup:jsoup:<viimane stabiilne versioon>'` (Maven Central).

### Puhastaja

Uus klass, nt `infrastructure/util/HtmlSanitizer.java` (läbiv komponent, mitte ühe domeeni teenus):

```java
private static final Safelist DESCRIPTION_SAFELIST = new Safelist()
        .addTags("p", "br", "strong", "em", "u", "h3", "h4", "ul", "ol", "li", "a")
        .addAttributes("a", "href")
        .addProtocols("a", "href", "http", "https", "mailto")
        .addEnforcedAttribute("a", "target", "_blank")
        .addEnforcedAttribute("a", "rel", "noopener noreferrer nofollow");

// prettyPrint(false) — muidu lisab jsoup reavahetusi ja taandeid
private static final Document.OutputSettings OUTPUT_SETTINGS =
        new Document.OutputSettings().prettyPrint(false);
```

Meetodid:

- `sanitizeDescription(String html)` → `Jsoup.clean(html, "", DESCRIPTION_SAFELIST, OUTPUT_SETTINGS)`
- `hasText(String html)` → `!Jsoup.parse(sanitizeDescription(html)).text().isBlank()` — tühjuse kontroll **pärast** puhastust (nt `<p><script>x</script></p>` on tühi)

Tähelepanekud:

- **`Jsoup.clean` ilma `OutputSettings`-ita** vormindab HTML-i ümber (reavahetused, taanded) → iga salvestus muudaks sisu ja frontendi "salvestamata muudatused" kontroll läheks segadusse.
- Kuna `baseUri` on `""`, eemaldatakse **suhtelised lingid** (`href="/koolitused"`) — editor lubab niikuinii ainult täis-URL-e.
- Lihttekst (nt `3_import.sql` kirjeldused) läbib puhastuse muutmata kujul (erimärgid escape'itakse, nt `&` → `&amp;`).

### Valideerimine

`@NotBlank` jääb `description` väljale alles, lisaks uus Bean Validation annotatsioon, nt `@HtmlNotBlank` (`infrastructure/validation/`) + validaator, mis kasutab `HtmlSanitizer.hasText(...)`. Nii tuleb viga olemasoleva `RestExceptionHandler.handleMethodArgumentNotValid` kaudu samal kujul nagu teised valideerimisvead:

```json
{ "message": "description: <valideerimise teade>", "errorCode": "INCORRECT_INPUT" }
```

Annotatsioon lisada kõigi kolme request DTO `description` väljale (`TrainingCreateRequestDto`, `TrainingUpdateRequestDto`, `TrainingTranslationCreateRequestDto` — viimased kaks tekivad oma taskide käigus).

### Kus puhastatakse

**Teenuse kihis enne entiteedi salvestamist**, mitte kontrolleris ega mapperis — nii on näha, kus sisu muutub:

- `TrainingService.createMainLanguageTrainingTranslation(...)` — pärast mapperit `trainingTranslation.setDescription(HtmlSanitizer.sanitizeDescription(...))`
- PUT ja tõlke POST teenustes sama muster
- AI tõlke teenuses: AI vastuse `description` puhastatakse enne `AiTranslationDto` tagastamist (AI väljund on samuti usaldamatu sisend)

`title` ja `shortDescription` **ei ole HTML** — neid ei puhastata (frontend kuvab need `{{ }}`-ga, Vue escape'ib).

## Näidisandmed

Realistlik TipTap editori väljund: `docs/JSON/training-description-sample.html` (~3,5 k märki; märgendid `p`, `h4`, `strong`, `em`, `ul`, `ol`, pesastatud `li`, `a`). Selle puhastamine **ei tohi muuta** teksti ega struktuuri.

Pahatahtlik näide testideks (prototüübist):

```html
<p>Tavaline tekst</p><img src=x onerror="alert('XSS')"><script>alert(1)</script><a href="javascript:alert(1)">pahatahtlik link</a><p style="color:red" onclick="alert(1)">stiiliga lõik</p>
```

Oodatud tulemus: `<p>Tavaline tekst</p>`, lingi tekst "pahatahtlik link" ilma `href`-ita (või ilma `<a>`-ta), `<p>stiiliga lõik</p>` ilma atribuutideta; `img`, `script` ja `alert` kaovad.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `description` on tühi või sisaldab pärast puhastust ainult tühje märgendeid (nt `<p></p>`, `<p> </p>`, `<p><br></p>`) | 400 Bad Request | `{ "message": "description: <valideerimise teade>", "errorCode": "INCORRECT_INPUT" }` |
| `description` sisaldab lubamatuid märgendeid/atribuute | — (**ei ole viga**) | Salvestatakse puhastatud kujul, vastus nagu tavaliselt |

Lubamatu HTML-i eest viga ei visata, sest kleebitud sisus on alati mingit prügi (`span`, `class`) ja admin ei saa sellest aru — puhastamine on vaikne.

## Vastuvõtu kriteeriumid

- [ ] `jsoup` on `build.gradle`-s
- [ ] `HtmlSanitizer` lubab ainult Safelist'i märgendeid; `a` saab alati `target="_blank"` ja `rel="noopener noreferrer nofollow"`; `href` ainult `http`/`https`/`mailto`
- [ ] Puhastus ei lisa reavahetusi ega taandeid (`prettyPrint(false)`)
- [ ] `POST /api/training` salvestab `description` puhastatud kujul
- [ ] `<p></p>` ja muu ainult tühjadest märgenditest koosnev `description` → 400 `INCORRECT_INPUT`
- [ ] PUT, tõlke POST ja AI tõlke teenuse taskides/implementatsioonis kasutatakse sama `HtmlSanitizer`-it (märgitud nende implementeerimisel)
- [ ] Testid:
  - [ ] `docs/JSON/training-description-sample.html` puhastamine säilitab teksti ja märgendid (`Jsoup.parse(...).text()` võrdne; märgendite arv võrdne). Täpset stringivõrdlust ei saa teha, sest jsoup võib järjestada `a` atribuudid teisiti kui TipTap
  - [ ] Puhastus on idempotentne: `sanitize(sanitize(x)).equals(sanitize(x))`
  - [ ] Pahatahtlik näide → `script`, `img`, `on*`, `style`, `javascript:` kaovad
  - [ ] Lihttekst (`3_import.sql` kirjeldus) jääb samaks
  - [ ] `hasText`: `<p></p>`, `<p> </p>`, `<p><br></p>`, `<script>x</script>` → `false`; `<p>a</p>` → `true`

## Avatud küsimused

1. **Maksimaalne pikkus.** `text` veerul piirangut pole. Kas lisada kaitseks `@Size(max = 20000)` (realistlik kirjeldus ~3,5 k märki)?
2. **Olemasolevad andmed.** `3_import.sql` kirjeldused on lihttekst — kas muuta need `<p>…</p>` kujule või jätta (kuvatakse korrektselt ka nii)? Soovitus: jätta, kuni kuvamise task lisab vormindatud näidise.
3. **Puhastaja kuju.** Staatiline util-klass (lihtne, nagu `StringBytesConverter`) vs Spring `@Component` (saab testides asendada). Soovitus: staatiline — puhastus on deterministlik ega vaja mockimist.
