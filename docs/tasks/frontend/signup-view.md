# Konto loomine ja sisselogimise redirect

**Vaated:** `SignupView.vue` (`/signup?redirect=`, `signupRoute`); muutub `LoginView.vue` (`/login?redirect=`)

**Roll:** Kõik rollid (sisse logimata)

**Vaste mockupis:** interaktiivne läbimäng `docs/mock-wireframe/loo-mock-vaade/courses-view/course-registration-view-labimang.html` (prototüübi kest https://claude.ai/artifact/SqxcgWP1BoxUGZqFfSZCPY)

> Mockupi pilt lisatakse hiljem (Balsamiq AI käsk: `docs/mock-wireframe/loo-mock-vaade/courses-view/courses-view-skeemid.md`, jaotis 8 "SignupView").

Taustaks: märkmed `docs/mock-wireframe/markmed/signup-view-markmed.md`, skeemid `docs/mock-wireframe/loo-mock-vaade/courses-view/courses-view-skeemid.md`. Olemasolev task `login-view.md` kirjeldab praegust sisselogimist.

Eeldab backend taski `POST-api-user.md`.

## Kasutajavoog

Uus inimene vajutab navbaris või login-lehel "Loo konto", täidab vormi ja on kohe sisse logitud; kui tuli registreerumisest, jätkab `redirect` rajal.

## Kasutajaliidese elemendid

### SignupView

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Pealkiri "Loo konto" | h1 | kitsas keskel vorm |
| Info (kui `redirect`) | `alert-info` | "Pärast konto loomist jätkad registreerumisega." |
| Väljad | vorm | Eesnimi*, Perekonnanimi*, E-post*, Telefon*, Parool* (≥ 8), Parool uuesti* |
| "Loo konto" | nupp | kontroll: kohustuslikud, e-post, parooli pikkus, paroolid ühtivad → `POST /api/user` → `sessionStorage` (`userId`, `roleName`) → `redirect` või avaleht |
| `EMAIL_TAKEN` | `AlertDanger` | "Selle e-posti aadressiga konto on juba olemas" + link "Logi sisse" |
| "Mul on juba konto — logi sisse" | link | → `/login?redirect=` |

### LoginView muudatus

- `?redirect=` olemasolul info "Registreerumiseks logi sisse või loo konto." ja pärast edukat sisselogimist `router.push(redirect)` (ainult sisemine rada, algab `/`-ga); muidu senine käitumine.
- Link **"Loo konto"** → `/signup?redirect=…`.

### Navbar

- Sisse logimata: "Logi sisse" ja **"Loo konto"** → `/signup`.

## API kutsed

- `POST /api/user` (uus), `POST /api/login` (olemas)

## Komponendid ja failistruktuur

- `views/SignupView.vue` (uus); `views/LoginView.vue`, `App.vue`, `router/index.js`, `NavigationService.js`, `api-services/UserService.js`, `locales/*` (muudetakse)

## Vastuvõtu kriteeriumid

- [ ] Konto loomine logib sisse ja suunab `redirect`-ile
- [ ] Kõik kontrollid ja `EMAIL_TAKEN`
- [ ] Login `redirect` ja "Loo konto" link
- [ ] Tekstid et/en, lint ja build puhtad
