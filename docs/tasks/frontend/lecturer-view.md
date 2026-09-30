# Koolitaja detailvaade (avalik)

**Vaade:** `LecturerView.vue`, route `/lecturer?lecturerId={id}` (nimi `lecturerRoute`)

**Roll:** Kõik rollid (ka sisse logimata)

**Vaste mockupis:** läbimäng `docs/mock-wireframe/loo-mock-vaade/lecturers-view/lecturers-view-labimang.html` (klõps kaardil). Kõiki vaateid saab koos läbi mängida prototüübi kestas `docs/mock-wireframe/loo-mock-vaade/index.html` (artifact https://claude.ai/artifact/SqxcgWP1BoxUGZqFfSZCPY).

> Mockupi pilt lisatakse hiljem (Balsamiq AI käsk: `lecturers-view-skeemid.md`, jaotis 7).

Taustaks: märkmed `docs/mock-wireframe/markmed/lecturer-view-markmed.md`.

## Kasutajavoog

Külastaja näeb koolitaja suurt pilti, nime, ametit, lühikirjeldust ja kogu kirjeldust ning tema koolitusi; koolituse nimi viib koolituse lehele. "← Kõik koolitajad" viib nimekirja.

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| "← Kõik koolitajad" | link | → `/lecturers` |
| Pilt | `LecturerAvatar` (kuni 240 px, `shape="rounded"`) | kohatäide, kui `photoVersion = null` |
| Nimi (h1), amet, lühikirjeldus | tekst | lühikirjeldus sissejuhatusena |
| Kirjeldus | `RichTextContent.vue` | HTML |
| "Koolitused" | loend | publitseeritud koolitused (`trainings`) lingina → `/training?trainingId={id}&trainingTranslationId={id}`; tühja listi korral plokki pole |

## Käitumine ja valideerimine

1. Avamisel ja keele vahetusel: `GET /api/lecturer-profile/{lecturerId}?contentLang=`.
2. 404 (kustutatud / olematu koolitaja) ja 500 → üldine veavaade. Puuduv `lecturerId` → veavaade.
3. Tulevasi toimumiskordi ei kuvata (otsus).

## API kutsed

| Kutse | Backend task |
|---|---|
| `GET /api/lecturer-profile/{lecturerId}` | `GET-api-lecturer-profile-lecturerId.md` |
| `GET /api/lecturer/{lecturerId}/photo` | `GET-api-lecturer-lecturerId-photo.md` |

## Komponendid ja failistruktuur

| Fail | Uus / muudetakse | Sisu |
|---|---|---|
| `views/LecturerView.vue` | uus | |
| `components/common/LecturerAvatar.vue`, `RichTextContent.vue` | olemas / uus | |
| `api-services/LecturerService.js`, `locales/*.json` | muudetakse | `sendGetLecturerProfileRequest` |

## Vastuvõtu kriteeriumid

- [ ] Pilt või kohatäide, nimi, amet, lühikirjeldus, kirjeldus
- [ ] "Koolitused" lingid koolituse lehele; koolitusteta plokki pole
- [ ] 404 → veavaade; keele vahetus laadib uuesti
