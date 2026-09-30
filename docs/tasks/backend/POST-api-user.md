# Konto loomine

**Teenus:** `POST /api/user`

**Kasutav vaade:** `SignupView.vue` (`/signup?redirect=`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/courses-view/course-registration-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/signup-view-markmed.md` (API märge on sama sisuga).

Eeldab taski `courses-calendar-db-changes.md`. Eeskuju vastusele: `POST /api/login` (`LoginResponse`).

## Sisend

**Request body:** `SignupRequest`

```json
{ "firstName": "Kati", "lastName": "Karu", "email": "kati.karu@example.com", "phone": "+37255512300", "password": "salasona1" }
```

Kõik kohustuslikud; `email` `@Email`; `password` `@Size(min = 8)`.

## Väljund

**Response (200 OK):** `LoginResponse` — `{ "userId": 8, "roleName": "participant" }` (frontend logib kohe sisse).

## Eesmärk

Uus inimene loob konto, et toimumiskorrale registreeruda.

## Seotud andmebaasi tabelid

Ühes transaktsioonis: `"user"` (roll `participant`, `status = 'A'`, `created_at`), `profile`, `participant` (`user_id`, `name`, `profile_id`). E-post unikaalne tõstutundetult (kontroll enne salvestamist + DB piirang). Parool salvestatakse nagu olemasolevatel kasutajatel (räsimine on eraldi teema).

Uus `Error` väärtus: `EMAIL_TAKEN("Selle e-posti aadressiga konto on juba olemas")`.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Kohustuslik väli puudu / vigane | 400 | valideerimise viga |
| E-post juba kasutusel | 403 | `EMAIL_TAKEN` |

## Vastuvõtu kriteeriumid

- [ ] Loob kasutaja + profiili + osaleja; uue kontoga saab `POST /api/login` kaudu sisse logida
- [ ] `EMAIL_TAKEN` ka suur-/väiketähe erinevusel
- [ ] Teenusel on automaattestid
