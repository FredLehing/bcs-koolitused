# Toimumiskorrale registreerumine

**Teenus:** `POST /api/course/{courseId}/participant`

**Kasutav vaade:** `CourseRegistrationView.vue` (`/course-registration?courseId={id}`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/courses-view/course-registration-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/course-registration-view-markmed.md` (API märge on sama sisuga).

Eeldab taske `courses-calendar-db-changes.md`, `GET-api-user-userId-participant.md`. Keerukas: mitu tabelit ühes transaktsioonis (`@Transactional`).

## Sisend

**Path variable:** `courseId`. **Request body:** `CourseRegistrationRequest`

```json
{
  "userId": 2, "firstName": "Anna", "lastName": "Saar", "email": "anna.saar@example.com", "phone": "+37256789012",
  "requiresLaptop": true, "notes": "Arve ettevõttele: OÜ Näidis"
}
```

Kohustuslikud: `userId`, `firstName`, `lastName`, `email` (`@Email`), `phone`. `requiresLaptop` vaikimisi `false`, `notes` valikuline (tühi → `""`, veerg on `NOT NULL`).

## Väljund

**Response (200 OK):** NONE

## Eesmärk

Kasutaja registreerib **iseennast** avatud toimumiskorrale.

## Seotud andmebaasi tabelid

1. Kasutaja osaleja (`participant.user_id = userId`); puudumisel luuakse `profile` + `participant` (`name` = eesnimi + perekonnanimi). Olemasoleva osaleja `profile` ja `participant.name` uuendatakse vormi andmetega.
2. `course_participant`: uus rida (`status = 'R'`, `has_paid = false`, `requires_laptop`, `notes`) või olemasolev `C` rida muudetakse `R`-iks (andmed uuendatakse).

Uued `Error` väärtused: `COURSE_FULL("Toimumiskord on täis")`, `ALREADY_REGISTERED("Oled sellele toimumiskorrale juba registreerunud")`, `REGISTRATION_CLOSED("Registreerumine on lõppenud")`.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Kohustuslik väli puudu / vigane | 400 | valideerimise viga |
| Toimumiskord `F` | 403 | `COURSE_FULL` |
| Kasutajal juba `R` rida | 403 | `ALREADY_REGISTERED` |
| `start_date < täna` | 403 | `REGISTRATION_CLOSED` |
| Olematu courseId | 404 | `PRIMARY_KEY_NOT_FOUND` — "Ei leidnud primary keyd 'courseId' väärtusega: 123" |
| Toimumiskord pole `O`/`F` või koolitus pole `P` | 404 | sama mis olematu |
| Olematu userId | 404 | `PRIMARY_KEY_NOT_FOUND` — "Ei leidnud primary keyd 'userId' väärtusega: 123" |

## Vastuvõtu kriteeriumid

- [ ] Esimene registreerumine loob vajadusel osaleja; korduv → 403 `ALREADY_REGISTERED`
- [ ] Loobunu (`C`) saab uuesti registreeruda
- [ ] Kõik veaolukorrad testitud, transaktsioon (vea korral midagi ei salvestu)
