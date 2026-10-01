# Osaleja tagasiside vorm

**Vaade:** `ParticipantFeedbackFormView.vue`, route `/participant-feedback-form?courseParticipantId={id}` (nimi `participantFeedbackFormRoute`)

**Roll:** Kasutaja (osaleja); admin → `/not-authorized`

**Vaste mockupis:** interaktiivne läbimäng `docs/mock-wireframe/loo-mock-vaade/participant-feedback-form-view/participant-feedback-form-view-labimang.html`

> Mockupi pilt lisatakse hiljem (Balsamiq AI käsud: `participant-feedback-form-view-skeemid.md`, jaotis 9).

Taustaks: märkmed `docs/mock-wireframe/markmed/participant-feedback-form-view-markmed.md`, skeemid `participant-feedback-form-view-skeemid.md`. Eeskuju: `ParticipantDetailsView.vue` (lugemisrežiim → "Muuda" → "Salvesta"/"Tühista", `ProfileMenu`, `InlineAlerts`). Eeldab backend taske `GET-…-feedback.md`, `POST-…-feedback.md`, `PUT-…-feedback.md` ja frontend taski `participant-courses-feedback-button.md`.

## Kasutajavoog

Osaleja vajutab "Minu koolitused" lehel "Anna tagasisidet" (uus) või "Vaata tagasisidet" (olemasolev). Uuel tagasisidel hindab iga kriteeriumi 1–10, soovi korral lisab kommentaari ja vajutab "Lisa tagasiside". Olemasolev tagasiside avaneb lugemisrežiimis; "Muuda" → muudab → "Salvesta" (või "Tühista").

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| `ProfileMenu` | vasak menüü | aktiivne "Minu koolitused" ka sellel rajal |
| "← Tagasi minu koolituste juurde" | link | → `/participant-courses` |
| Pealkiri | h2 | "Tagasiside" |
| Päis | tekst | koolituse nimi · `dd/MM/yyyy – dd/MM/yyyy`; olemasoleval "Esitatud dd/MM/yyyy" (`createdAt`), muudetul ka "Muudetud dd/MM/yyyy" (kui `updatedAt` kuupäev erineb `createdAt` kuupäevast). Staatust (N/U/H) ei näidata |
| Skaala vihje | tekst | "Hinda iga väidet skaalal 1–10 (1 = ei nõustu üldse, 10 = nõustun täielikult)" |
| Kriteeriumi nimi + (?) | tekst + tooltip | `title`; (?) hover/fookus → `description` |
| Hinne | 10 raadionuppu (1–10) | kohustuslik; lugemisrežiimis *disabled* |
| "+ Lisa kommentaar" / "− Peida kommentaar" | link | avab/peidab kommentaari kasti; peitmine teksti ei kustuta; lugemisrežiimis kommentaarita kriteeriumil linki pole |
| Kommentaar | textarea, 3 rida, `maxlength="255"` | placeholder "Täpsusta soovi korral oma hinnangut"; all loendur "0 / 255"; olemasoleva kommentaariga kohe lahti; lugemisrežiimis *disabled* |
| "Lisa tagasiside" | nupp (primary) | uus olek → `POST` |
| "Muuda" | nupp (primary) | muutmise olek, lugemisrežiim → väljad muudetavaks |
| "Salvesta" / "Tühista" | nupud | pärast "Muuda": `PUT` / laaditud väärtused tagasi ja lugemisrežiim |
| Teated | `InlineAlerts` | nuppude kõrval: edu ja vead |

## Käitumine ja valideerimine

- `courseParticipantId` puudub → `ErrorView`. Sisselogimata → `/login?redirect={rada koos parameetriga}`; admin → `/not-authorized` (router guard nagu `participantCoursesRoute`).
- Vaate avamisel ja keele vahetusel `GET …/feedback?contentLang={UI keel}`. Olek tuleb vastusest: `hasFeedback = false` → väljad kohe täidetavad; `true` → lugemisrežiim.
- GET 403 `FEEDBACK_NOT_ALLOWED` / 404 `REGISTRATION_NOT_FOUND` / 404 `PRIMARY_KEY_NOT_FOUND` → kaardil punane teade backendi tekstiga, vormi pole, link tagasi.
- Enne saatmist: kui mõni hinne puudub → "Hinda kõiki kriteeriume", hindamata kriteeriumid punase äärisega; päringut ei tehta.
- Body: kõik vormi kriteeriumid (`feedbackCriteriaId`, `score`, `feedbackText`); tühi või ainult tühikutest kommentaar → `null`.
- Edu → "Tagasiside salvestatud", `GET` uuesti, lugemisrežiim (POST-i järel on vorm muutmise olekus).
- 403 `FEEDBACK_ALREADY_EXISTS`, 403 `FEEDBACK_CRITERIA_CHANGED`, 404 `FEEDBACK_NOT_FOUND` → backendi teade ja `GET` uuesti.
- Navbari "👤 ▾" menüüsse uut punkti ei tule.

## API kutsed

- `GET /api/user/{userId}/registration/{courseParticipantId}/feedback?contentLang=` → `ParticipantFeedbackDto` `{ courseParticipantId, trainingTitle, startDate, endDate, hasFeedback, createdAt, updatedAt, criteria[{ feedbackCriteriaId, title, description, score, feedbackText }] }`
- `POST /api/user/{userId}/registration/{courseParticipantId}/feedback` `{ answers[{ feedbackCriteriaId, score, feedbackText }] }` → 200
- `PUT /api/user/{userId}/registration/{courseParticipantId}/feedback` (sama body) → 200

`userId` tuleb `SessionStorageService.getUserId()`-st.

## Komponendid ja failistruktuur

- `views/ParticipantFeedbackFormView.vue` (uus)
- `components/profile/FeedbackCriteriaItem.vue` (uus) — ühe kriteeriumi rida
- `components/profile/ProfileMenu.vue` (muudetakse — "Minu koolitused" aktiivne ka sellel rajal)
- `api-services/UserService.js`, `router/index.js`, `locales/et.json`, `locales/en.json` (muudetakse)

## Vastuvõtu kriteeriumid

- [ ] Registreerumine 2: uus vorm, hinnete puudumisel teade, "Lisa tagasiside" salvestab ja vorm läheb lugemisrežiimi
- [ ] Registreerumine 10: lugemisrežiim (väljad *disabled*, "Esitatud 12/06/2026"), "Muuda" → "Salvesta" salvestab, "Tühista" taastab
- [ ] Tooltip, kommentaari avamine/peitmine ja loendur töötavad
- [ ] Keelatud ja võõras registreerumine → teade, vormi pole
- [ ] Admin → `/not-authorized`, sisselogimata → login koos redirectiga
- [ ] Tekstid et/en
