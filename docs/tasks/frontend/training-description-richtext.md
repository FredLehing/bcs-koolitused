# Koolituse kirjelduse richtext editor (TipTap)

**Vaade:** `TrainingFormView.vue`, route `/training-form` — kõik kolm olekut (`new-training`, `update`, `new-translation`)

**Roll:** Admin

**Eelnev task:** `docs/tasks/frontend/training-form-view.md` (valmis). See task **asendab** tõlke sektsiooni kirjelduse `textarea` WYSIWYG editoriga; ülejäänud vaade ei muutu.

**Seotud taskid:**
- `docs/tasks/backend/training-description-html-sanitize.md` — backend puhastab ja valideerib sama HTML-i
- Kliendivaate kuvamine (`TrainingView.vue`, DOMPurify + `v-html`) — **eraldi hilisem task**, vt "Leiud kuvamise taski jaoks" allpool

## Eesmärk

Admin kirjutab koolituse kirjelduse (`training_translation.description`) vormindatud tekstina: pealkirjad, paks/kaldkiri/allajoonitud, loendid ja lingid. Sisu peab saama **kopeerida** mujalt (ChatGPT, Word, Google Docs, veebileht) nii, et struktuur säilib, aga võõrad stiilid (fondid, värvid, klassid) kaovad. Tulemus salvestatakse HTML-ina olemasolevasse `text` veergu — andmebaasi ega API kujusid ei muudeta.

## Prototüübi tulemused (2026-09-30)

Otsused põhinevad prototüübil `/prototype-richtext` (`PrototypeRichTextView.vue`, throwaway — branch `prototype/richtext-editor`, ei merge'ita):

| Küsimus | Tulemus |
|---|---|
| ChatGPT-st kopeerimine (hiirega märgistades **ja** "Copy" nupuga) | Lõikelauale tuleb mõlemal juhul `text/plain` + `text/html`; TipTap kasutab HTML-i. Säilivad H3/H4, paks, kaldkiri, pesastatud täpp- ja numberloendid, lingid |
| Markdowni kleepimise tugi | **Pole vaja** — HTML-tee katab ChatGPT. Markdowni tee oleks isegi halvem (ChatGPT `text/plain` sisaldab `:chatgpt-content-reference{...}` jääke) |
| Wordi/ChatGPT prügi (`<span>`, `StartFragment`, klassid, stiilid) | TipTapi skeemi filter eemaldab |
| ChatGPT allikaviited | Kopeeruvad tekstina **"Pasted text"** lõigu/loendi elemendi lõppu — editor ei saa neid eristada (vt "Teadaolevad piirangud") |
| Lingid | TipTap lisab ise `target="_blank" rel="noopener noreferrer nofollow"` |
| Tühi editor | `getHTML()` annab `<p></p>`, `editor.isEmpty` on `true` |
| Maht | Realistlik kirjeldus ~3,5 k märki — `text` veerg sobib |

Realistlik editori väljund: `docs/JSON/training-description-sample.html` (ChatGPT-st kleebitud Vali-IT kirjeldus, "Pasted text" jäägid eemaldatud).

## Toetatud vormingud

| Vorming | HTML | Tööriistariba nupp |
|---|---|---|
| Lõik | `<p>` | — |
| Pealkiri (2 taset) | `<h3>`, `<h4>` | H3, H4 |
| Paks / kaldkiri / allajoonitud | `<strong>`, `<em>`, `<u>` | B, I, U |
| Täpploend / numberloend (ka pesastatud) | `<ul>`, `<ol>`, `<li><p>…</p></li>` | • loend, 1. loend |
| Link | `<a href target rel>` | Link (lisa / eemalda) |
| Reavahetus lõigu sees | `<br>` (Shift+Enter) | — |

**Ei toetata** (teadlik otsus): pildid, tabelid, koodiplokid ja `code`, tsitaadid, horisontaaljoon, läbikriipsutus, fondid/värvid, H1/H2/H5/H6 (vt kleepimine), Markdown.

Sama märgendite nimekiri on backendi Safelist'is — **muutmisel tuleb muuta mõlemat** (vt backend task).

## Sõltuvused

`frontend/package.json` (prototüübi käigus juba paigaldatud):

- `@tiptap/vue-3`, `@tiptap/pm`, `@tiptap/starter-kit` — editor (TipTap v3; StarterKit sisaldab ka `Link` ja `Underline` laiendusi)
- `dompurify` — ei kasutata selles taskis, jääb kuvamise taskile
- `@tiptap/markdown` — kasutati prototüübis ainult võrdluseks, on juba eemaldatud

## Komponendid ja failid

| Fail | Muudatus |
|---|---|
| `components/forms/RichTextEditor.vue` | **Uus**, korduvkasutatav editor (tööriistariba + `EditorContent`) |
| `components/forms/TrainingTranslationForm.vue` | `textarea` → `RichTextEditor`; eemaldada `TODO: richtext editor` kommentaar |
| `api-services/mock/MockDatabase.js` | Näidis-HTML ja AI mocki parandus (vt "Mock-andmed") |
| `locales/et.json`, `locales/en.json` | Tööriistariba nuppude `title` tekstid ja lingi küsimuse tekst |
| `views/TrainingFormView.vue` | Muudatusi ei tohiks vaja minna (vt "Andmevoog") |

### `RichTextEditor.vue`

Options API ja projekti props/emits muster (`event-` eesliide), nagu `TrainingTranslationForm.vue`-s:

- **prop** `html` (String) — sisu
- **emit** `event-new-html-input` — uus sisu; **tühja editori korral `''`**, mitte `<p></p>` (`editor.isEmpty`)
- `mounted()` loob `new Editor({...})` (`@tiptap/vue-3`), `beforeUnmount()` kutsub `editor.destroy()`

Editori seadistus:

```js
StarterKit.configure({
  heading: { levels: [3, 4] },
  code: false,
  codeBlock: false,
  blockquote: false,
  horizontalRule: false,
  strike: false,
  link: {
    openOnClick: false,
    autolink: true,
    defaultProtocol: 'https',
    // lubatud ainult http(s) ja mailto — NB: `protocols` option lisab protokolle, ei piira
    isAllowedUri: (url, ctx) => ctx.defaultValidate(url) && /^(https?:\/\/|mailto:)/i.test(url),
  },
})
```

Lingi dialoogis sisestatud aadress ilma protokollita (nt `vali-it.ee`) tuleb enne `setLink`-i täiendada `https://` eesliitega.

`editorProps.transformPastedHTML` — kleebitud pealkirjad teisendatakse lubatud tasemele, muidu muutuksid need tavaliseks lõiguks:

- `<h1>`, `<h2>` → `<h3>`
- `<h5>`, `<h6>` → `<h4>`

Tööriistariba:

- Bootstrapi nupud (`btn btn-sm`), aktiivne vorming esile tõstetud (`editor.isActive(...)`) — prototüübi muster
- Ikoonid `@phosphor-icons/vue`-st (nt `PhTextB`, `PhTextItalic`, `PhTextUnderline`, `PhListBullets`, `PhListNumbers`, `PhLink`), pealkirjad tekstina "H3", "H4"
- Igal nupul `title` (i18n) ja `type="button"` (vorm ei tohi submit'ida)
- Link: kui kursor on lingil → eemalda (`unsetLink`), muidu küsi URL (`window.prompt` on esimeses versioonis piisav) ja `setLink({ href })`

CSS (scoped + `:deep()`, sest ProseMirrori sisu ei saa scoped stiile):

- editori ala näeb välja nagu `form-control` (raam, padding), `min-height` ~200px, fookusel `form-control` fookuse stiil
- `li > p { margin-bottom: 0 }` — TipTap paneb iga loendi elemendi sisse `<p>`, Bootstrapi `p` margin teeks loendisse suured vahed (prototüübis nähtud)
- `h3`, `h4` väiksemaks kui Bootstrapi vaikimisi (nt `fs-5` / `fs-6` suurus) — need on kirjelduse alapealkirjad, mitte lehe pealkirjad

Ligipääsetavus: `label` ei saa viidata `for`-iga editorile — kasuta `aria-labelledby` (label'i `id`).

## Andmevoog

`TrainingTranslationForm.vue`:

```html
<RichTextEditor
  :html="translation.description"
  @event-new-html-input="$emit('event-new-description-input', $event)"
/>
```

`TrainingFormView.vue` kuulab juba `event-new-description-input` sündmust — muutust ei ole vaja.

**Väline sisu muutus** (andmete laadimine, oleku vahetus `router.replace`-iga, AI tõlke tulemus, `resetTranslation`): `RichTextEditor` jälgib `html` prop'i (`watch`) ja kui see erineb editori praegusest sisust, kutsub `editor.commands.setContent(html, { emitUpdate: false })`. `emitUpdate: false` väldib lõputut tsüklit (prop → editor → emit → prop).

**Valideerimine:** kuna tühi editor emitib `''`, töötab olemasolev kontroll `this.translation.description.trim() === ''` → "Lisa kirjeldus" muutmata kujul.

**Salvestamata muudatuste kontroll** (`translationHasUnsavedChanges`, AI tõlke kinnitus): võrdleb stringe. Andmebaasis olev lihttekst (`3_import.sql`) laaditakse editorisse ilma emitita, seega ei teki valepositiivset muudatust enne, kui admin ise midagi muudab. Kui admin muudab ja siis tagasi, võib editori normaliseeritud HTML (`<p>…</p>`) erineda algsest lihttekstist → kinnituse modal kuvatakse ka siis. See on aktsepteeritav.

**Olemasolevad lihttekstiga kirjeldused** (`3_import.sql`) avanevad editoris ühe lõiguna ja salvestuvad järgmisel salvestusel `<p>…</p>` kujul — andmete migratsiooni pole vaja.

## Mock-andmed

`MockDatabase.js`:

1. Ühe koolituse põhikeele tõlke `description` (nt koolitus 1, `et`) asendada vormindatud HTML-iga, et editori laadimist saaks testida ilma kleepimata — võib kasutada lühendatud osa failist `docs/JSON/training-description-sample.html`.
2. `getAiTranslation` lisab praegu eesliite `'[AI en] '` ka `description` ette — HTML-i puhul jääks see tekstiks väljaspool `<p>`-d. Lisada eesliide ainult `title` ja `shortDescription` ette, `description` tagastada muutmata (päris AI säilitab märgendid).

## Teadaolevad piirangud (kirjutada ka adminile mõeldud juhendisse, kui see tekib)

- **ChatGPT allikaviited** ("Pasted text", ka muud viitesildid) kopeeruvad tavalise tekstina. Neid ei filtreerita automaatselt (ChatGPT kasutajaliides muutub, filter oleks habras). Soovitus: ChatGPT promptis keelata allikaviited ja admin vaatab teksti enne salvestamist üle.
- **Kirjeldus ei alga koolituse pealkirjaga** — pealkiri (`title`) kuvatakse detailvaates eraldi. ChatGPT paneb selle sageli H3-na algusesse.
- **Markdown** (nt `.md` failist või lihttekstina) jääb editorisse toore tekstina (`**paks**`, `### …`).
- **Pildid ja tabelid** kleepimisel kaovad; tabeli tekst jääb lõikudena alles.

## Väljaspool skoopi

- Kliendivaate kuvamine (`TrainingView.vue`) — eraldi task
- Backendi HTML puhastus ja valideerimine — `training-description-html-sanitize.md`
- `title` ja `shortDescription` jäävad tavalisteks tekstiväljadeks (`shortDescription` kuvatakse nimekirja kaartidel)

## Leiud kuvamise taski jaoks

Kui kuvamise task (`TrainingView.vue`) koostatakse, arvesta:

- `DOMPurify.sanitize(description, { ALLOWED_TAGS: ['p','br','strong','em','u','h3','h4','ul','ol','li','a'], ALLOWED_ATTR: ['href','target','rel'] })` + `v-html` — prototüübis eemaldas `<script>`, `<img onerror>`, `javascript:` lingi, `style` ja `onclick`
- Kuvamise CSS vajab samu reegleid mis editor (`li > p` margin, väiksemad `h3`/`h4`) — mõistlik on teha korduvkasutatav komponent (nt `RichTextContent.vue`) ja jagada stiile editoriga
- `v-html` sisu ei saa scoped stiile → `:deep()`
- Näidisandmed: `docs/JSON/training-description-sample.html` (vajadusel `3_import.sql`-i)

## Vastuvõtu kriteeriumid

- [ ] Kirjelduse `textarea` on asendatud `RichTextEditor` komponendiga kõigis kolmes olekus
- [ ] Tööriistariba: B, I, U, H3, H4, täpploend, numberloend, link (lisa/eemalda); aktiivne vorming on esile tõstetud; nupud ei submit'i vormi
- [ ] Editor ei luba ega jäta kleepimisel alles pilte, tabeleid, koodiplokke, tsitaate, horisontaaljoont, läbikriipsutust ega stiile/klasse
- [ ] ChatGPT vastuse kleepimine (hiirega ja "Copy" nupuga) säilitab pealkirjad, paksu/kaldkirja, pesastatud loendid ja lingid
- [ ] Kleebitud `<h1>`/`<h2>` muutuvad H3-ks, `<h5>`/`<h6>` H4-ks
- [ ] Lingid lubavad ainult `http`, `https`, `mailto` protokolli
- [ ] Olemasolev tõlge ja AI tõlke tulemus ilmuvad editorisse; oleku vahetus (`router.replace`) laadib editori sisu uuesti
- [ ] Tühi editor → "Lisa kirjeldus" valideerimisviga, API kutset ei tehta
- [ ] Salvestatud `description` on editori HTML (`<p>…</p>` jne), mitte lihttekst
- [ ] Loendi elementide vahel pole suuri vahesid; H3/H4 on alapealkirja suuruses
- [ ] Mock: vähemalt ühel koolitusel on vormindatud kirjeldus; AI mock ei riku `description` HTML-i
- [ ] `npm run build` ja `npm run lint` lähevad läbi
