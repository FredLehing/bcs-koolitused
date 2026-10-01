# Kasutaja registreeringu olek

**Teenus:** `GET /api/course/{courseId}/participant-status`

**Kasutavad vaated:** `CourseView.vue` ("Registreeru" vs märge), `CourseRegistrationView.vue`

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/courses-view/courses-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/course-view-markmed.md` (API märge on sama sisuga).

Eeldab taski `courses-calendar-db-changes.md`.

## Sisend

**Path variable:** `courseId`. **Query:** `userId` (sisseloginud kasutaja, `sessionStorage`).

## Väljund

**Response (200 OK):** `CourseParticipantStatusDto` — `{ "status": "R" }`; `"C"` = loobunud; `null` = pole registreerunud (ka siis, kui kasutajal osalejat pole).

## Eesmärk

Registreerunud kasutaja näeb nupu asemel märget "✓ Oled sellele toimumiskorrale registreerunud".

## Seotud andmebaasi tabelid

`participant` (`user_id`) → `course_participant` (`course_id`, `participant_id`).

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Olematu courseId | 404 | `PRIMARY_KEY_NOT_FOUND` — "Ei leidnud primary keyd 'courseId' väärtusega: 123" |
| Olematu userId | 404 | `PRIMARY_KEY_NOT_FOUND` — "Ei leidnud primary keyd 'userId' väärtusega: 123" |

## Vastuvõtu kriteeriumid

- [ ] Näidisandmed: kasutaja 2 + toimumiskord 1 → `R`, toimumiskord 9 → `null`
- [ ] Teenusel on automaattestid
