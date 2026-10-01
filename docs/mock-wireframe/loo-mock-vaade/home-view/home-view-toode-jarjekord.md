# Avalehe täiendus — tööde järjekord

`HomeView.vue`. Katusharu `feature/RAIN-home-view` (võetud master'ist).

Allikad: skeemid `home-view-skeemid.md`, märkmed `docs/mock-wireframe/markmed/home-view-markmed.md`, läbimäng `home-view-labimang.html` (prototüübi kestas `../index.html`, vaade "Avaleht").

| # | Töö | Taskifail | Keerukus | Märkus |
|---|---|---|---|---|
| 1.1 | `GET /api/next-courses` | `docs/tasks/backend/GET-api-next-courses.md` | lihtne | andmebaasi muudatusi pole |
| 2.1 | Avaleht: suunavad kaardid, järgmised 5 toimumiskorda, turunduslause | `docs/tasks/frontend/home-view.md` | lihtne | pärast 1.1 |

## Hiljem

- Mockupi pilt (Balsamiq käsk skeemide failis, jaotis 4).

## Seis (2026-10-01)

**Tehtud** (katusharus `feature/RAIN-home-view`, iga task oma harust `--no-ff`): 1.1, 2.1. Backendi testid läbivad (232), frontendi lint ja build läbivad. Andmebaasi muudatusi pole. Brauseris kontrollib kasutaja; seejärel katusharu `--no-ff` master'isse.
