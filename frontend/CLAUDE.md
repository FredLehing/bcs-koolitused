# CLAUDE.md

See fail annab juhiseid Claude Code'ile (claude.ai/code) selle repositooriumi koodiga töötamisel.

## Käsud

```sh
npm install        # Installi sõltuvused
npm run dev        # Arendusserver aadressil http://localhost:8081
npm run build      # Tootmisbuild
npm run lint       # Käivita oxlint ja eslint (mõlemad --fix lipuga)
npm run format     # Prettieri formaatimine src/ kaustas
```

**Claude Code WSL-is:** `node_modules` on Windowsi paigaldus, seega `npm run build` siin kaustas feilib — ehita scratchpadis koopiana (vt juurkausta CLAUDE.md jaotist "Frontendi build WSL-ist").

## Arhitektuur

See on Vue 3 + Vite frontend (Vali-IT grupiprojekt).

**Stack:** Vue 3 (Composition API), Vue Router 5, Pinia, Tailwind CSS 4, Axios, Phosphor Icons

**Kujundus (haru `alternative-frontend-design`):** Bootstrap on asendatud Tailwindiga. Värvid, põhiklassid ja mustrid on kirjas `docs/structure/frontend-tailwind-stiilijuhend.md` — loe see enne vaate kujundamist. Prototüübis on uues stiilis ainult osa vaateid (vt sama faili jaotist „Prototüübi seis“).

**Sisenemispunkt:** `index.html` laeb Vue rakenduse (`src/main.js` → `src/App.vue`).

**API proksi:** Vite suunab `/api` päringud kohalikule backendile (`http://localhost:8080`), vt `vite.config.js`.

**Globaalne axios:** Axios on registreeritud `app.config.globalProperties.$axios`-na — komponentides kasuta `this.$axios` (Options API) või inject via `getCurrentInstance` (Composition API).

**Olekuhaldus:** Pinia on registreeritud (`app.use(createPinia())`), store'id asuvad kaustas `src/stores/`:

- `languageStore.js` — `contentLang` (kasutajaliidese keel ja ühtlasi API parameeter `contentLang`; muuda ainult `setContentLang()` kaudu, mis hoiab store'i, vue-i18n ja localStorage'i sünkis) ja `uiLanguages` (navbari keelevalik, pärineb `i18n.js` konstandist `UI_LANGUAGES`).

**Keeled:** kasutajaliidese keeled (`UI_LANGUAGES`) on frontendis kõvasti kirjas, sest iga keele jaoks peab olemas olema tõlkefail `src/locales/<keelekood>.json`. Andmebaasi keeled — tõlkekeeled (`requiresTranslation`), põhikeel (`isMainLanguage`) ja lipud (`flagIconCode`) — tulevad backendist (`GET /api/languages`) ja vaade laadib need ise; store'i neid ei dubleerita. Lipu kuvamiseks kasuta alati komponenti `components/common/FlagIcon.vue` (prop `flagIconCode`, raamiga; suurus teksti suuruse klassiga, nt `class="text-2xl"`).

**Marsruutimine:** Marsruudid on defineeritud `src/router/index.js`-is.

**Tee alias:** `@` viitab `src/` kaustale.

## Koodistiil

Prettieri seadistus: ilma semikooloniteta, ülakomad, 100-märgiline reavaheline laius. ESLint käivitab esmalt oxlinti, seejärel eslint-plugin-vue (olulised reeglid), Prettieri formaatimine on ESLintist välja jäetud.

## Keel

Selle faili (`CLAUDE.md`) sisu peab alati olema eestikeelne.

## Dokumentatsioon

Kogu dokumentatsioon asub `docs/` kausta alakaustades. Kõigi dokumentatsioonifailide sisu peab alati olema eestikeelne.
