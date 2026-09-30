# Jagatud staatuse nupp: publitseeri, liiguta mustandisse, taasta

**Komponent:** `components/common/TrainingStatusButton.vue` (uus)

**Kasutavad vaated:** `AdminTrainingsView.vue` (`/admin-trainings`, iga tabeli rida), `TrainingFormView.vue` (`/training-form`, olekud `update` ja `new-translation`)

**Roll:** Admin

**Vaste mockupis:** läbimängud `docs/mock-wireframe/loo-mock-vaade/admin-trainings-view/admin-trainings-view-labimang.html` (tabeli viimane veerg) ja `docs/mock-wireframe/loo-mock-vaade/form-view/training-form-view-labimang.html` (vormi nupurida)

> Mockupi pilt lisatakse hiljem.

Otsus (`admin-trainings-view-skeemid.md`): **API kutse tehakse komponendi sees.** Komponent on terviklik (nupp + kinnituse modal + päring + veakäsitlus), vaade ainult kuulab sündmust ja laadib andmed uuesti. Nii ei dubleeri tabel ja vorm sama loogikat.

## Kasutajavoog

Admin vajutab koolituse staatuse nupule. Avaneb kinnituse modal; kinnitusel muudetakse staatus backendis ja vaade värskendab andmeid. Nupu tekst ja tegevus sõltuvad koolituse staatusest.

## Kasutajaliidese elemendid

| `status` | Nupu tekst | Modal (pealkiri / tekst) | Kinnitusnupp | Päring | Uus status |
|---|---|---|---|---|---|
| `U` | "Publitseeri" | "Publitseeri koolitus" / "Kas soovid koolituse „{title}“ publitseerida? See muutub avalikult nähtavaks." | "Publitseeri" | `PUT /api/training/{trainingId}/publish` | `P` |
| `P` | "Liiguta mustandisse" | "Liiguta mustandisse" / "Kas soovid koolituse „{title}“ mustandisse liigutada? See kaob avalikust nimekirjast." | "Liiguta mustandisse" | `PUT /api/training/{trainingId}/unpublish` | `U` |
| `D` | "Taasta" | "Taasta koolitus" / "Kas soovid koolituse „{title}“ taastada? See läheb mustandisse, avalikuks muutub alles pärast publitseerimist." | "Taasta" | `PUT /api/training/{trainingId}/restore` | `U` |

Kui `title` propsi pole antud (vormis), kasutatakse teksti ilma nimeta (nt "Kas soovid koolituse publitseerida? …"), nagu praegu `TrainingFormView`-s. Tekstid on i18n võtmetega (`et.json`, `en.json`); olemasolevad `trainingForm.statusModal.*` ja `trainingForm.buttons.publish/unpublish` võib komponenti üle viia.

Nupu välimus: vormis `btn btn-outline-primary` nagu praegu; tabelis väiksem (`btn-sm`) — nt props `size` või klass väljastpoolt.

## Käitumine ja valideerimine

1. Klõps nupul → avaneb `ConfirmModal`. "Katkesta" / sulgemine → midagi ei juhtu.
2. Kinnitus → modal sulgub, komponent kutsub staatusele vastava `TrainingService` meetodi.
3. Õnnestumine → emit `event-status-changed` uue staatusega (`"P"` / `"U"`). Vaade:
   - `TrainingFormView`: eduteade (`trainingForm.messages.published` / `unpublished`) ja `getTraining()` nagu praegu `handleChangeTrainingStatusResponse()`;
   - `AdminTrainingsView`: eduteade ("Publitseeritud" / "Liigutatud mustandisse" / "Koolitus taastatud (mustand)"), tabel uuesti; taastamise järel ka nimede ettepanekud.
4. Viga:
   - 403 `TRAINING_DELETED` (koolitus kustutati vahepeal nt teises aknas) → komponent emit'ib `event-status-error` backendi `message`-iga; vaade näitab seda veateatena ja laadib andmed uuesti (tabelis näeb siis kustutatud olekut, vormis tuleb 404 → veavaade);
   - muu viga (404, 500) → `NavigationService.navigateToErrorView()`.
5. Topeltklõpsu vältimiseks on nupp päringu ajaks `disabled`.

## API kutsed

### `PUT /api/training/{trainingId}/publish`

**Backend task:** `docs/tasks/backend/PUT-api-training-trainingId-publish.md`. Request ja response body puuduvad.

### `PUT /api/training/{trainingId}/unpublish`

**Backend task:** `docs/tasks/backend/PUT-api-training-trainingId-unpublish.md`. Request ja response body puuduvad.

### `PUT /api/training/{trainingId}/restore`

**Backend task:** `docs/tasks/backend/PUT-api-training-trainingId-restore.md` (uus). Request ja response body puuduvad.

**Veateated (kõik kolm):**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 403 | `TRAINING_DELETED` | "Kustutatud koolituse staatust ei saa muuta, taasta see enne" (ainult publish/unpublish) | emit `event-status-error` → vaade näitab `message`-i ja laadib andmed uuesti |
| 404 | `PRIMARY_KEY_NOT_FOUND` | "Ei leidnud primary keyd 'trainingId' väärtusega: 123" | `NavigationService.navigateToErrorView()` |
| 500 | — | — | `NavigationService.navigateToErrorView()` |

## Mock-vastused

`TrainingService.sendPutTrainingPublishRequest` ja `sendPutTrainingUnpublishRequest` on **praegu mock-vastusega** (päris axios-kutse kommentaaris). Kui backendi teenused on valmis, vaheta need päris kutseks (vt `training-form-view.md`, "Mock-vastused" → "Päris teenusele üleminek"). Uus `sendPutTrainingRestoreRequest(trainingId)` tehakse samuti esialgu mockiga (`MockDatabase.restoreTraining`), kui backend pole veel valmis.

## Komponendid ja failistruktuur

| Fail | Uus / muudetakse | Sisu |
|---|---|---|
| `components/common/TrainingStatusButton.vue` | uus | Propsid `trainingId` (Number), `status` (String `U`/`P`/`D`), `title` (String, valikuline); `ConfirmModal`; emits `event-status-changed`, `event-status-error` |
| `views/TrainingFormView.vue` | muudetakse | Praegune staatuse nupp, `isStatusModalOpen`, `statusModalTitle`/`statusModalMessage` ja `changeTrainingStatus()` asendatakse komponendiga; `handleChangeTrainingStatusResponse()` jääb sündmuse käsitlejaks |
| `views/AdminTrainingsView.vue` | kasutab | Iga rea viimane veerg (vt `admin-trainings-view.md`) |
| `api-services/TrainingService.js` | muudetakse | + `sendPutTrainingRestoreRequest(trainingId)`; publish/unpublish mockist päris kutseks, kui backend on valmis |
| `locales/et.json`, `locales/en.json` | muudetakse | Taastamise tekstid; staatuse tekstid komponendi võtmete alla |

## Vastuvõtu kriteeriumid

- [ ] `TrainingStatusButton` kuvab staatuse järgi õige teksti (Publitseeri / Liiguta mustandisse / Taasta)
- [ ] Iga tegevuse ees on kinnituse modal; katkestamisel päringut ei tehta
- [ ] Kinnitusel teeb komponent ise õige päringu ja emit'ib `event-status-changed` uue staatusega
- [ ] `TrainingFormView` kasutab komponenti; vormi käitumine (eduteade, andmete uuendamine, nupu tekst) on sama mis enne
- [ ] `AdminTrainingsView` tabelis on komponent igal real; pärast tegevust tabel uueneb
- [ ] Kustutatud koolituse real näidatakse ainult "Taasta"; taastatud koolitus on mustandis
- [ ] 403 `TRAINING_DELETED` näidatakse kasutajale backendi tekstiga; 404/500 → üldine veavaade
- [ ] Päringu ajal on nupp `disabled`
