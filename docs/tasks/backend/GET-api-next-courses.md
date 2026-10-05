# Avalehe järgmised toimumiskorrad

**Teenus:** `GET /api/next-courses?contentLang={contentLang}&limit={limit}`

**Kasutav vaade:** `HomeView.vue` (`/`), plokk "Meie järgmised 5 koolitust"

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/home-view/home-view-labimang.html`, skeeme `home-view-skeemid.md` ja märkmeid `docs/mock-wireframe/markmed/home-view-markmed.md` (JSON-näide).

Andmebaasi muudatusi pole — kasutab olemasolevat view'd `public_course_summary` nagu `GET-api-courses.md`.

## Sisend

| Nimi | Tüüp | Kohustuslik | Kirjeldus |
|---|---|---|---|
| `contentLang` | String | jah | Tõlgitud väljade keel (`"et"` / `"en"`) |
| `limit` | Integer | ei | Mitu toimumiskorda tagastada, 1–20, vaikimisi 5 |

Query parameetrid kogutakse DTO-sse `NextCourseFilterDto` (`@ParameterObject`, nagu `PublicCourseFilterDto`): `contentLang` `@NotNull`, `limit` `@Min(1)` `@Max(20)`, vaikeväärtus 5.

## Väljund

**Response (200 OK):** `List<PublicCourseSummaryItemDto>` — sama DTO nagu `GET /api/courses` vastuse `courseSummaries` (kasutab `CourseCard.vue`), sh `fundingTypes` `contentLang` keeles.

- Ainult **avatud** toimumiskorrad: view `public_course_summary` (publitseeritud koolitus, `O`/`F`, `start_date >= täna`, `contentLang` tõlkega), **täis (`F`) välja jäetud**.
- Järjestus `is_promoted DESC, start_date, course_id` (sama mis kalendris), esimesed `limit` rida.
- Tühi list, kui sobivaid toimumiskordi pole.

Näide: `docs/mock-wireframe/markmed/home-view-markmed.md`, jaotis "API märkmed".

## Eesmärk

Avalehel näidatakse kuni 5 järgmist toimumiskorda, kuhu saab registreeruda, ilma filtrite ja leheküljestuseta. Eraldi teenus (mitte `GET /api/courses?page=0&limit=5&hideFull=true`), sest avaleht ei vaja lehe infot (`totalPages`, `totalElements`) ega filtreid ning vastus on lihtne massiiv.

Nimi `next-courses`, sest `UpcomingCourseDto` on juba kasutusel (toimumiskorra lehe teiste toimumiskordade lingid).

## Lahenduse suund

- `CourseController`: `@GetMapping("/next-courses")`, Swaggeri kirjeldus nagu `findPublicCourses`.
- `CourseService.findNextCourses(NextCourseFilterDto)`: `PageRequest.of(0, limit, Sort ...)` + `Specification.allOf(hasContentLanguageCode, isFullIncluded(true))` → `publicCourseSummaryRepository.findAll(spec, pageable).getContent()` → `publicCourseSummaryMapper.toPublicCourseSummaryItemDtos` → `handleAddFundingTypes` iga rea jaoks.

## Seotud andmebaasi tabelid

View `public_course_summary`; `funding_type_translation` (rahastuse nimed). Näidisandmed (täna = 01/10/2026): Java algkursus 05.10, Spring Boot veebiarendus 12.10, UX disaini alused 09.11 (esile tõstetud), Vue.js esmaspetsialist 26.10, Projektijuhtimise põhitõed 02.11.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `contentLang` puudub või `limit` väljaspool 1–20 | 400 Bad Request | `errorCode: INCORRECT_INPUT` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

## Vastuvõtu kriteeriumid

- [x] Endpoint tagastab kuni `limit` avatud tulevast toimumiskorda (täis välja jäetud), esile tõstetud eespool, edasi alguse järgi
- [x] Vastuse kuju on sama mis `GET /api/courses` `courseSummaries` (sh `fundingTypes`)
- [x] `limit` vaikimisi 5; `limit` 0 või 21 ja puuduv `contentLang` → 400
- [x] Teenusel on automaattestid
