# Kasutaja oma osaleja andmed

**Teenus:** `GET /api/user/{userId}/participant`

**Kasutav vaade:** `CourseRegistrationView.vue` (vormi eeltäitmine)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/courses-view/course-registration-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/course-registration-view-markmed.md` (API märge on sama sisuga).

Eeldab taski `courses-calendar-db-changes.md`.

## Sisend

**Path variable:** `userId`.

## Väljund

**Response (200 OK):** `MyParticipantDto`

```json
{ "participantId": 1, "firstName": "Anna", "lastName": "Saar", "email": "anna.saar@example.com", "phone": "+37256789012" }
```

Kui kasutajal osalejat pole (nt admini loodud konto): `participantId = null`, nimed ja telefon `""`, `email = user.email`.

## Eesmärk

Registreerumise vorm on eeltäidetud kasutaja profiiliga.

## Seotud andmebaasi tabelid

`"user"`, `participant` (`user_id`), `profile`.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Olematu userId | 404 | `PRIMARY_KEY_NOT_FOUND` — "Ei leidnud primary keyd 'userId' väärtusega: 123" |

## Vastuvõtu kriteeriumid

- [ ] Osalejaga ja osalejata kasutaja vastus nagu ülal
- [ ] Teenusel on automaattestid
