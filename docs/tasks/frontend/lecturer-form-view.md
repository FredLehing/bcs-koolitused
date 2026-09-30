# Koolitaja vorm: uus koolitaja, muutmine ja tõlked

**Vaade:** `LecturerFormView.vue`, route `/lecturer-form` (nimi nt `lecturerFormRoute`)

**Roll:** Admin

**Vaste mockupis:** interaktiivne läbimäng `docs/mock-wireframe/loo-mock-vaade/admin-lecturers-view/admin-lecturers-view-labimang.html` (artifact https://claude.ai/artifact/HNuz21ynvnWCWtYfB1SfzX)

> Mockupi pilt lisatakse hiljem (Balsamiq AI käsk: `admin-lecturers-view-skeemid.md`, jaotis 10).

Taustaks: märkmed `docs/mock-wireframe/markmed/lecturer-form-view-state-new-lecturer-markmed.md`, `…-state-update-markmed.md`, `…-state-new-translation-markmed.md`; otsused ja skeemid `docs/mock-wireframe/loo-mock-vaade/admin-lecturers-view/admin-lecturers-view-skeemid.md` (jaotised "Koolitaja vorm", 2–5). **Olekud ja tõlkeloogika on samad nagu `TrainingFormView`-l** (`training-form-view.md`) — kasuta seda eeskujuna.

## Kasutajavoog

Admin avab "Lisa uus koolitaja" (navbar või nimekiri), sisestab nime, soovi korral pildi ning põhikeele ametinimetuse, lühikirjelduse ja kirjelduse ja vajutab "Lisa". Vorm läheb muutmise olekusse, kus lipukestega saab avada olemasolevaid tõlkeid või lisada puuduva (halli lipu kaudu; väljad eeltäidetakse põhikeele tekstiga, soovi korral "Tee AI tõlge"). Muutmise olekus salvestab "Salvesta" nime, pildi ja avatud tõlke. Kiirnupp "Koolitajad" viib nimekirja.

## Olekud

Olek tuleneb URL-i query parameetritest; pärast iga `router.replace`-i laaditakse andmed uuesti (`$route.query` jälgija).

| Olek | `state` | URL | Pealkiri | Nupud | Lipukesed |
|---|---|---|---|---|---|
| A. Uus koolitaja | `"new-lecturer"` | `/lecturer-form` | "Lisa uus koolitaja" | "Lisa" | — |
| B. Muutmine | `"update"` | `?lecturerId=1&lecturerTranslationId=2` | "Muuda koolitajat" (+ nimi) | "Salvesta"; "Tee AI tõlge" mitte-põhikeelel | jah |
| C. Uus tõlge | `"new-translation"` | `?lecturerId=3&languageId=2` | "Lisa koolitaja tõlge" (+ nimi) | "Lisa tõlge", "Tee AI tõlge" | jah |

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| "Koolitajad" | kiirnupp pealkirja real | → `/admin-lecturers` (kõigis olekutes) |
| Lipukesed | `TranslationFlags.vue` (olemas, muudetakse) | Tõlkekeeled (`requiresTranslation = true`); olemas → värviline (klikk → `router.replace` selle tõlkega), puudub → hall (klikk → olek C); avatud keel esile tõstetud |
| Kaart "Koolitaja andmed" | fieldset | Ei ole tõlgitav |
| Täisnimi * | tekstiväli, max 255 | Olekus C kirjutuskaitstud |
| Pilt | `PhotoUpload.vue` | Eelvaade (`LecturerAvatar`, 80 px), "Vali pilt" (`accept="image/png,image/jpeg,image/webp"`), "Eemalda", vihje "PNG, JPEG või WebP, kuni 2 MB". Olekus C ainult eelvaade |
| Kaart "Tõlge ({keel})" | fieldset, lipp pealkirjas | Olekus C vihje "Väljad on eeltäidetud põhikeele (et) tekstiga — tõlgi need või kasuta AI tõlget." |
| Ametinimetus * | tekstiväli, max 255 + **"?" ikoon tooltip'iga** | Tooltip: "Ametinimetus kuvatakse koolitaja kaardil nime all. Kirjuta lühidalt, millega koolitaja tegeleb, nt „Tarkvaraarendaja ja Java koolitaja“ või „UX-disainer“. Iga keele jaoks eraldi tõlge." Placeholder "nt Tarkvaraarendaja ja Java koolitaja" |
| Lühikirjeldus * | tekstiväli, max 255 | Vihje all: "Kuvatakse koolitaja kaardil koolituse lehel ja toimumiskorra juures." |
| Kirjeldus * | `RichTextEditor.vue` (olemas) | HTML |
| "Lisa" / "Salvesta" / "Lisa tõlge" | nupp (primary) | Olekute järgi (vt tabel ülal) |
| "Tee AI tõlge" | nupp + tooltip | "Tõlgib salvestatud põhikeele tekstid; täidab ainult vormi" |
| Vead | `AlertDanger.vue` | Valideerimis-, pildi- ja backendi vead |
| Eduteated | `AlertSuccess.vue` | "Koolitaja lisatud", "Salvestatud", "Tõlge lisatud" |

## Käitumine ja valideerimine

1. **Laadimine** (skeem: `admin-lecturers-view-skeemid.md`, jaotis 3):
   - kõik olekud: `GET /api/languages` (põhikeel, tõlkekeeled);
   - B: `GET /api/lecturer/{lecturerId}`, `GET /api/lecturer-translation/{lecturerTranslationId}`, `GET /api/lecturer/{lecturerId}/lecturer-translations`;
   - C: `GET /api/lecturer/{lecturerId}`, `GET …/lecturer-translations`, `GET /api/lecturer-translation/{põhikeele tõlke id}` → tõlkeväljad eeltäidetakse.
   - Keele vahetus navbaris vormi uuesti ei lae (rippmenüüsid pole).
2. **Pilt** (`PhotoUpload`): faili valikul kontroll tüüp (`image/png`, `image/jpeg`, `image/webp`) ja suurus (≤ 2 MB); vea korral "Lubatud on PNG, JPEG või WebP pilt kuni 2 MB" ja pilti ei muudeta. OK → `FileReader.readAsDataURL`, Base64 osa (pärast koma) + `file.type` → emit `event-photo-changed`. "Eemalda" → `null`. Kärpimist ega vähendamist ei tehta.
3. **Valideerimine enne saatmist:** täisnimi, ametinimetus, lühikirjeldus ja kirjeldus (HTML ilma tekstita = tühi) täidetud → muidu "Täida kõik kohustuslikud väljad".
4. **"Lisa"** (A): `POST /api/lecturer` (`userId` localStorage'ist) → `router.replace({ lecturerId, lecturerTranslationId })` → olek B, eduteade.
5. **"Salvesta"** (B): `PUT /api/lecturer/{lecturerId}` — pilt saadetakse **alati praegusel kujul** (laaditud Base64, uus pilt või `null`). Eduteade "Salvestatud".
6. **"Lisa tõlge"** (C): `POST /api/lecturer/{lecturerId}/lecturer-translation` → `router.replace({ lecturerId, lecturerTranslationId })` → olek B.
7. **"Tee AI tõlge"** (C ja B mitte-põhikeelel): kui vormis on salvestamata muudatusi → `ConfirmModal` "Kirjuta tõlge üle?"; seejärel `GET …/ai-translation?languageId={id}` → täidab ametinimetuse, lühikirjelduse ja kirjelduse (ei salvesta). Nupp päringu ajal `disabled`.
8. **Vead:** 403 (`PHOTO_TYPE_NOT_ALLOWED`, `PHOTO_TOO_LARGE`, `TRANSLATION_EXISTS`) ja 400 → backendi `message` `AlertDanger`-is; 503 `AI_SERVICE_UNAVAILABLE` → `message`; 404 ja 500 → `NavigationService.navigateToErrorView()`.
9. Nupud on päringu ajal `disabled` (topeltklõps).

## API kutsed

Kõik kontraktid: backend taskid ja märkmete failid.

| Kutse | Backend task | Olek |
|---|---|---|
| `GET /api/languages` | `GET-api-languages.md` (olemas) | A, B, C |
| `GET /api/lecturer/{lecturerId}` | `GET-api-lecturer-lecturerId.md` | B, C |
| `GET /api/lecturer/{lecturerId}/lecturer-translations` | `GET-api-lecturer-lecturerId-lecturer-translations.md` | B, C |
| `GET /api/lecturer-translation/{lecturerTranslationId}` | `GET-api-lecturer-translation-lecturerTranslationId.md` | B, C |
| `POST /api/lecturer` | `POST-api-lecturer.md` | A |
| `PUT /api/lecturer/{lecturerId}` | `PUT-api-lecturer-lecturerId.md` | B |
| `POST /api/lecturer/{lecturerId}/lecturer-translation` | `POST-api-lecturer-lecturerId-lecturer-translation.md` | C |
| `GET /api/lecturer/{lecturerId}/ai-translation?languageId=` | `GET-api-lecturer-lecturerId-ai-translation.md` | B (mitte-põhikeel), C |

**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 403 | `PHOTO_TYPE_NOT_ALLOWED` | "Lubatud on ainult PNG, JPEG või WebP pilt" | `AlertDanger` |
| 403 | `PHOTO_TOO_LARGE` | "Pilt on liiga suur, lubatud kuni 2 MB" | `AlertDanger` |
| 403 | `TRANSLATION_EXISTS` | "Selles keeles tõlge on juba olemas" | `AlertDanger` |
| 403 | `MAIN_LANGUAGE_NOT_TRANSLATABLE` | "Põhikeelde ei saa AI tõlget teha" | `AlertDanger` (tavaliselt ei esine, nupp on peidus) |
| 503 | `AI_SERVICE_UNAVAILABLE` | "AI tõlketeenus ei ole hetkel kättesaadav" | `AlertDanger` |
| 400 | `INCORRECT_INPUT` | valideerimise teade | `AlertDanger` |
| 404 | `PRIMARY_KEY_NOT_FOUND` | "Ei leidnud primary keyd 'lecturerId' väärtusega: 123" | üldine veavaade (nt kustutatud koolitaja) |

## Mock-vastused

Kuni backend valmib: sama muster nagu `TrainingFormView` ("Mock-vastused"). `MockDatabase.js`: koolitajad, tõlked ja pildid `lecturer-db-changes.md` seed'i järgi; meetodid lugemiseks, lisamiseks, muutmiseks, tõlke lisamiseks ja AI tõlkeks (mock: nt `[AI] ` + põhikeele tekst). Eeskuju: läbimängu skript.

## Komponendid ja failistruktuur

| Fail | Uus / muudetakse | Sisu |
|---|---|---|
| `views/LecturerFormView.vue` | uus | Olekud A/B/C, laadimine, salvestamine |
| `components/forms/PhotoUpload.vue` | uus | Propsid `photo`, `contentType`, `readonly`; failivalik, kontroll, eelvaade, "Eemalda"; emits `event-photo-changed`, `event-photo-error` |
| `components/common/LecturerAvatar.vue` | uus (vt `lecturer-card.md`) | Pilt või kohatäide, prop `size` |
| `components/common/TranslationFlags.vue` | muudetakse | Prop `trainingTranslations` → `existingTranslations`; i18n `trainingForm.flags.*` → `translationFlags.*`; **`TrainingFormView` uuendatakse sama muudatusega, käitumine ei muutu** |
| `components/forms/RichTextEditor.vue` | olemas | Kirjeldus |
| tõlkeväljade kaart | arendaja otsustada | nt `components/forms/LecturerTranslationForm.vue` (eeskuju `TrainingTranslationForm.vue`) |
| `api-services/LecturerService.js` | muudetakse | uued kutsed; AI tõlge esialgu mock |
| `router/index.js` | muudetakse | + `/lecturer-form` |
| `locales/et.json`, `locales/en.json` | muudetakse | `lecturerForm.*`, `translationFlags.*` |

## Vastuvõtu kriteeriumid

- [ ] Kolm olekut tulenevad URL-ist ja iga `router.replace` laadib andmed uuesti
- [ ] Olek A: tühi vorm, "Lisa" loob koolitaja ja viib olekusse B eduteatega
- [ ] Olek B: väljad täidetud, lipukesed õiged, "Salvesta" salvestab nime, pildi ja avatud tõlke
- [ ] Olek C: nimi ja pilt lukus, tõlkeväljad eeltäidetud põhikeelest, "Lisa tõlge" viib olekusse B
- [ ] Ametinimetuse kõrval on "?" tooltip; lühikirjelduse all vihje
- [ ] Pildi valik: vale tüüp või > 2 MB → veateade, pilti ei muudeta; eelvaade ja "Eemalda" töötavad; eemaldatud pilt saadetakse `null`-ina
- [ ] "Tee AI tõlge" ainult mitte-põhikeelel; salvestamata muudatuste korral küsib kinnitust; täidab kolm välja
- [ ] Kohustuslike väljade kontroll enne saatmist; backendi vead kuvatakse `message`-iga; 404 → veavaade
- [ ] `TranslationFlags` töötab nii `TrainingFormView`-s kui ka siin
