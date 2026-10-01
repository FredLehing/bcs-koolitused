# Registreerumise muutmine (admin)

**Teenus:** `PUT /api/admin-registration/{courseParticipantId}`

**Kasutav vaade:** `AdminRegistrationView.vue` (`/admin-registration?courseParticipantId={id}`), nupp "Salvesta"

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-registrations-view/admin-registrations-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/admin-registration-view-markmed.md`.

Eeldab taski `registration-db-changes.md`.

## Sisend

**Path variable:** `courseParticipantId` (Integer).

**Request body:** `AdminRegistrationUpdateRequestDto`

```json
{
  "status": "C",
  "hasPaid": false,
  "requiresLaptop": true,
  "adminNotes": "Teatas telefoni teel 25.09."
}
```

| Väli | Kohustuslik | Valideerimine |
|---|---|---|
| `status` | jah | `R` või `C` |
| `hasPaid` | jah | |
| `requiresLaptop` | jah | |
| `adminNotes` | ei | trimmitakse, tühi → `null` |

## Väljund

**Response (200 OK):** tühi vastus.

## Eesmärk

Admin muudab ainult registreerumise enda välju (`course_participant.status`, `has_paid`, `requires_laptop`, `admin_notes`); `updated_at` uueneb auditeerimisega. Osaleja lisainfot (`notes`) ja profiili ei muudeta. Kustutamist pole — loobumine on staatus `C`.

- Taastamine (`C` → `R`) on lubatud ka täis toimumiskorrale (`F`) — `COURSE_FULL` kontrolli pole (mahutavust veel ei modelleerita).
- Tasumise väärtus jääb loobumisel alles.
- Staatuse muutus mõjutab `GET /api/admin-courses` arve (`participantCount` ja `paidCount` loevad ainult `R`).
- Kinnituse staatuse muutmisel küsib frontend; backend seda ei tea.

## Seotud andmebaasi tabelid

`course_participant`.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Olematu `courseParticipantId` | 404 | `PRIMARY_KEY_NOT_FOUND` — "Ei leidnud primary keyd 'courseParticipantId' väärtusega: 123" |
| Kohustuslik väli puudub või `status` pole `R`/`C` | 400 | `INCORRECT_INPUT` |

## Vastuvõtu kriteeriumid

- [ ] Muutuvad ainult neli registreerumise välja
- [ ] `adminNotes` trimmitakse, tühi → `null`
- [ ] Taastamine täis toimumiskorrale töötab
- [ ] Olematu registreerumine → 404, vigane sisend → 400
- [ ] Teenusel ja request DTO valideerimisel on automaattestid
