# Tagasisidede haldus — tööde järjekord

`AdminFeedbacksView.vue`, rada `/admin-feedbacks`. Kõik kuus taski on implementeeritud (02.10.2026): neli uut API teenust, vastuste versioon/lukustus ja Vue vaade koos menüü ning tõlgetega.

Järjekord: **näidisandmete kontroll → vastuste versioon ja samaaegsed muudatused → lugemisteenused → ülevaatamine → frontend**.

Allikad: [mock](admin-feedbacks-view-labimang.html), [skeemid](admin-feedbacks-view-skeemid.md), [vaate/API märkmed](../../markmed/admin-feedbacks-view-markmed.md). Taskid koostatud backend- ja frontend-taski skillide järgi kinnitatud HTML-mocki põhjal; PDF-i pole.

## 0. Eeltöö

| # | Töö | Taskifail / allikas | Märkus |
|---|---|---|---|
| 0.1 | Olemasolev tagasiside skeem ja kooskõlalised DML-i näited | [DDL](../../../database/2_create.sql), [DML](../../../database/3_import.sql) | Näited lisatud ja eraldatud mälubaasis kontrollitud; kommentaariveerg laiendatud text-tüübiks (olemasolevale baasile eraldi 4_feedback_long_comments.sql). PostgreSQL-is skripti ei käivitatud. |
| 0.2 | Tagasiside versioon, ühine lukk ja osaleja PUT-i täiendamine | [feedback-review-concurrency.md](../../../tasks/backend/feedback-review-concurrency.md) | Valmis: ühine DB lukk, kanooniline versioon; H → U loogika säilib. |

## 1. Etapp — backend

| # | Teenus | Taskifail | Keerukus / sõltuvus |
|---|---|---|---|
| 1.1 | `GET /api/admin-feedback-courses` | [GET-api-admin-feedback-courses.md](../../../tasks/backend/GET-api-admin-feedback-courses.md) | valmis; lihtne |
| 1.2 | `GET /api/admin-feedbacks` | [GET-api-admin-feedbacks.md](../../../tasks/backend/GET-api-admin-feedbacks.md) | valmis; keskmine |
| 1.3 | `GET /api/admin-feedback/{feedbackId}` | [GET-api-admin-feedback-feedbackId.md](../../../tasks/backend/GET-api-admin-feedback-feedbackId.md) | valmis; keskmine; 0.2 |
| 1.4 | `PUT /api/admin-feedback/{feedbackId}/review` | [PUT-api-admin-feedback-feedbackId-review.md](../../../tasks/backend/PUT-api-admin-feedback-feedbackId-review.md) | valmis; keskmine; 0.2 |

Kõigi lugemisteenuste koondid arvestavad kogu filtrihulka. Detail ja review kasutavad sama versioonilepingut. 400/409 handlerid ja Swagger kuuluvad taskidesse, mitte eraldi puuduva taski taha.

## 2. Etapp — frontend

| # | Töö | Taskifail | Sõltuvus |
|---|---|---|---|
| 2.1 | AdminFeedbacksView, router, navbar, AdminTabs, API teenus, ET/EN, laadimised ja vead | [admin-feedbacks-view.md](../../../tasks/frontend/admin-feedbacks-view.md) | valmis; kõik neli API taski ja 0.2 |

## 3. Kontroll ja dokumentatsioon

Taskide vastuvõtukriteeriumid katavad DML-i arvutused, filtreerimise/leheküljestuse, tõlked, ajaloolise kriteeriumi, idempotentsuse ja samaaegse osaleja muudatuse. Backendil automaattestid, konkurentsil kahe transaktsiooniga integratsioonitest, frontendil mocki kasutajavoo kontroll. Märkmed ja skeemid on teostusega sünkroniseeritud. Frontendi käitumistestid käivitab `npm run test:feedbacks`; backend-testid `./gradlew test`. H2 integratsioonitest ei kasuta arenduse PostgreSQL-i.

Kontrolli tulemus: 314 backend-testi ja 7 frontendi käitumistesti läbisid; muudetud frontendi failide ESLint ning eraldatud tootmisbuild korras. Andmebaasi päringute arv ei kasva lehel olevate ridade arvuga (integratsioonitestis 5 päringut nii 1 kui 100 rea limiidiga).

## Lahtised küsimused ja kasutuselevõtu sõltuvused

- Olemasolev backend ei paku usaldusväärset serveripoolset autentimise/rollikontrolli lepingut. Admini teenuste päris kasutuselevõtt sõltub selle lahendamisest; kliendi sessionStorage ei tõenda admini õigusi. Autentimissüsteemi projekteerimine ei kuulu selle vaate taskidesse.
- Detaililt naastes on kokkulepitud mocki vaikeseis; filtrite taastamine URL-i/oleku kaudu on hilisem täiendus.
- PDF-pilti pole: taskid viitavad kinnitatud interaktiivsele HTML-mockile ja sisaldavad märget pildi hilisema lisamise kohta.

DML-i ühisnäited: 30 tagasisidet, 150 vastust, 14 vajab ülevaatust, 4 madala hindega; üldkeskmine 8,7. Toimumiskorrad 12 / 14 / 15: 4/5, 6/6, 0/2. Mocki „Taasta näidisandmed” on brauseri mälus toimuv tegevus, päris API endpointi selleks ei looda.

02.10.2026 täiendus: Vali-IT Noorem AI arendaja (trainingId 15, courseId 16) lisatud 18 registreerunu ja 16 tagasisidega. Koguhulk 30 tagasisidet, 150 vastust, 13 uut kontot. Kommentaarid: 80 Vali-IT vastusest 7 kommentaariga; neli pikka mitmelõigulist teksti. Admini vastuste kriteeriumiveerus ainult title, laius 12rem; hinne 5rem, ülejäänu kommentaarile. Kommentaaride piir tõstetud 10000 märgini; DDL text ja olemasoleva baasi muutmisskript `docs/database/4_feedback_long_comments.sql`. Skripti arendaja baasis ei käivitatud.

Täienduse kontroll: 25 backendi tagasiside integratsiooni-/teenuse-/valideerimistesti ning 7 frontendi testi läbivad. ESLint ja Linuxi scratch-build läbivad; muudetud komponentide Oxlint samuti. Kogu frontendi Oxlint peatub varasemal NavigationService.js no-control-regex veal. DML-i 30 tagasisidet ja 150 vastust kontrollitud koos 44 välisvõtme ja kronoloogiaga.
