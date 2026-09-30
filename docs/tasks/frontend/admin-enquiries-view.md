# Admini päringute nimekiri ja päringu vaade

**Vaated:** `AdminEnquiriesView.vue` (`/admin-enquiries`, `adminEnquiriesRoute`) ja `AdminEnquiryView.vue` (`/admin-enquiry?enquiryId={id}`, `adminEnquiryRoute`)

**Roll:** Admin

**Vaste mockupis:** interaktiivne läbimäng `docs/mock-wireframe/loo-mock-vaade/admin-enquiries-view/admin-enquiries-view-labimang.html` (artifact https://claude.ai/artifact/7ZWj2shTxXAhw5H4hBzs97)

> Mockupi pilt lisatakse hiljem (Balsamiq AI käsud: `admin-enquiries-view-skeemid.md`, jaotis 7).

Taustaks: märkmed `docs/mock-wireframe/markmed/admin-enquiries-view-markmed.md` ja `admin-enquiry-view-markmed.md`, skeemid `admin-enquiries-view-skeemid.md`. Eeskuju: `AdminRoomsView.vue` (otsing, lüliti, sorteerimine).

## Kasutajavoog

Admin avab navbari menüüst "Admin" → "Koolituste päringud" nimekirja (vaikimisi uued päringud, uusimad üleval), otsib ja sorteerib, avab silma ikooniga päringu, loeb sõnumit ja kontakti ning märgib päringu käsitletuks (või tagasi uueks).

## Kasutajaliidese elemendid

### Nimekiri

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Menüülink "Koolituste päringud" | navbar, menüü "Admin" | koolitusruumide järel eraldaja + link (i18n `navbar.manageEnquiries`) |
| Pealkiri "Koolituste päringud" | h1 | |
| Otsinguväli | `type="search"` | frontendis: nimi, e-post, ettevõte, koolitus (sisaldab, tõstutundetu) |
| "Näita ka käsitletud" | lüliti, vaikimisi väljas | uus päring `includeHandled` |
| Tabel | Bootstrap tabel | Saabunud \| Nimi \| E-post \| Ettevõte \| Koolitus \| Toimumiskord \| Vorm \| Staatus \| Tegevused; uue päringu rida `fw-semibold` |
| Sorteeritavad veerud | `SortableColumnHeader` | Saabunud, Nimi, Koolitus, Staatus (uus → käsitletud) |
| Staatus | `EnquiryStatusBadge.vue` | "Uus" (`text-bg-primary`) / "Käsitletud" (`text-bg-secondary`) |
| Vaata | silma ikoon (`PhEye`) | → `/admin-enquiry?enquiryId={id}` |
| "Kokku N päringut" | tekst | filtreeritud ridade arv |

### Päringu vaade

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Pealkiri "Koolituse päring" + nupp "Koolituste päringud" | h1, nupp | nupp → `/admin-enquiries` |
| Kaart "Päring" | `fieldset` + `dl` | Saabunud, Staatus, Koolitus (link `trainingRoute`), Toimumiskord / "Toimumiskorda pole valitud", Vorm, Ettevõte / "—", Sõnum (`white-space: pre-wrap`) |
| Kaart "Kontakt" | `fieldset` + `dl` | Nimi, E-post (`mailto:`), Telefon (`tel:`) |
| "Märgi käsitletuks" / "Märgi uueks" | nupp | olenevalt staatusest; pärast edukat kutset päring uuesti ja eduteade (`AlertSuccess`) |

## Käitumine

- Ainult adminile, muidu `NotAuthorizedView`.
- Kuupäev-kellaaeg: `FormatService.formatDateTime(instant)` → `30/09/2026 14:20` (kasutaja ajavööndis); toimumiskord `formatLocalDate(start) – formatLocalDate(end)`.
- Keele vahetusel mõlemas vaates päring uuesti (`contentLang`).
- Vead: nimekirja päring → veavaade; päringu vaates 404 → veavaade.

## API kutsed

- `GET /api/admin-enquiries?contentLang=&includeHandled=`
- `GET /api/admin-enquiry/{enquiryId}?contentLang=`
- `PUT /api/enquiry/{enquiryId}/handle`, `PUT /api/enquiry/{enquiryId}/reopen`

## Komponendid ja failistruktuur

- `views/AdminEnquiriesView.vue`, `views/AdminEnquiryView.vue`, `components/common/EnquiryStatusBadge.vue`, `api-services/EnquiryService.js` (uued)
- `App.vue`, `router/index.js`, `NavigationService.js`, `FormatService.js`, `locales/et.json`, `en.json` (muudetakse)

## Vastuvõtu kriteeriumid

- [ ] Menüülink ja mõlemad vaated töötavad
- [ ] Otsing, lüliti, sorteerimine, "Kokku N päringut"
- [ ] Staatuse muutmine ja eduteade
- [ ] Tekstid et/en
