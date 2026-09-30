# Meie koolitajad (avalik nimekiri)

**Vaade:** `LecturersView.vue`, route `/lecturers` (nimi `lecturersRoute`)

**Roll:** Kõik rollid (ka sisse logimata)

**Vaste mockupis:** läbimäng `docs/mock-wireframe/loo-mock-vaade/lecturers-view/lecturers-view-labimang.html` (artifact https://claude.ai/artifact/34hJJ4KrPRCt3zKxn4W1ad). Kõiki vaateid saab koos läbi mängida prototüübi kestas `docs/mock-wireframe/loo-mock-vaade/index.html` (artifact https://claude.ai/artifact/SqxcgWP1BoxUGZqFfSZCPY).

> Mockupi pilt lisatakse hiljem (Balsamiq AI käsk: `lecturers-view-skeemid.md`, jaotis 7).

Taustaks: otsused `docs/mock-wireframe/loo-mock-vaade/lecturers-view/lecturers-view-skeemid.md`, märkmed `docs/mock-wireframe/markmed/lecturers-view-markmed.md`.

## Kasutajavoog

Külastaja avab navbari põhimenüüst "Meie koolitajad" kaardiruudustiku (pilt, nimi, amet, lühikirjeldus). Klõps kaardil avab koolitaja detailvaate.

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Navbari link "Meie koolitajad" | `App.vue` | põhimenüüs "Koolitused" järel (i18n `navbar.ourLecturers`, en "Our trainers"); **"Ettevõttest" → "Lektorid" eemaldatakse** ja "Ettevõttest" jääb ilma alammenüüta |
| Pealkiri "Meie koolitajad" + sissejuhatus | | |
| Kaardid (`LecturerTile.vue`) | `row-cols-1 row-cols-sm-2 row-cols-lg-3 row-cols-xl-4` | ruudukujuline pilt (`LecturerAvatar`, `shape="rounded"`), nimi, amet, lühikirjeldus (kuni 3 rida); kogu kaart on `RouterLink` → `/lecturer?lecturerId={id}` |
| Kohatäide | siluett | kui `photoVersion = null` |
| Tühi nimekiri | tekst | "Koolitajaid pole veel lisatud." |

## Käitumine ja valideerimine

1. Avamisel ja keele vahetusel: `GET /api/lecturer-summaries?contentLang=`.
2. Pilt: `<img src="{API}/lecturer/{lecturerId}/photo?v={photoVersion}">` (abifunktsioon `LecturerService.getLecturerPhotoUrl`), `loading="lazy"`.
3. Viga → üldine veavaade.

## API kutsed

| Kutse | Backend task |
|---|---|
| `GET /api/lecturer-summaries` | `GET-api-lecturer-summaries.md` |
| `GET /api/lecturer/{lecturerId}/photo` | `GET-api-lecturer-lecturerId-photo.md` (`<img src>`) |

## Komponendid ja failistruktuur

| Fail | Uus / muudetakse | Sisu |
|---|---|---|
| `views/LecturersView.vue` | uus | |
| `components/common/LecturerTile.vue` | uus | |
| `components/common/LecturerAvatar.vue` | uus / muudetakse (vt `lecturer-card.md`) | propsid `lecturerId`, `photoVersion`, `size`, `shape` |
| `App.vue`, `router/index.js`, `NavigationService.js` | muudetakse | navbar, rada `/lecturers`, `/lecturer` |
| `api-services/LecturerService.js`, `locales/*.json` | muudetakse | |

## Vastuvõtu kriteeriumid

- [ ] Navbaris "Meie koolitajad"; "Ettevõttest" all "Lektorid" eemaldatud
- [ ] Kaardid ruudustikus, pildita koolitajal kohatäide, kogu kaart on link
- [ ] Keele vahetus laadib uuesti; tühja nimekirja tekst
