# Toimumiskorra "Esile tõstetud" väli

**Teenused (olemas, muutuvad):** `GET /api/course/{courseId}`, `POST /api/training/{trainingId}/course`, `PUT /api/course/{courseId}`

**Kasutav vaade:** `CourseFormView.vue` (`/course-form`) — lüliti "Esile tõstetud" (frontend task `docs/tasks/frontend/course-form-is-promoted.md`)

> Otsus: `docs/mock-wireframe/loo-mock-vaade/courses-view/courses-view-skeemid.md`, "Üldine" (esile tõstetud toimumiskord on avalikus kalendris eespool).

Eeldab taski `courses-calendar-db-changes.md`.

## Sisend

`CourseCreateRequestDto` ja `CourseUpdateRequestDto`: uus väli `isPromoted` (Boolean, kohustuslik, `@NotNull`).

## Väljund

`GET /api/course/{courseId}` vastuses (`CourseDto`) uus väli `"isPromoted": true`.

## Eesmärk

Admin märgib toimumiskorra esile tõstetuks; `/courses` lehel on need eespool ja esile tõstetud kaardiga.

## Seotud andmebaasi tabelid

`course.is_promoted`.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `isPromoted` puudub | 400 | valideerimise viga |

## Vastuvõtu kriteeriumid

- [ ] `isPromoted` salvestub `POST`/`PUT` kaudu ja tagastatakse `GET`-is
- [ ] `MockDatabase.js` / frontendi mock (kui kasutusel) saab välja juurde
- [ ] Olemasolevad testid uuendatud
