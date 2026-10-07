# BCS Koolitused

BCS Koolituse uue veebilehe prototüüp, valminud Vali-IT grupiprojektina.: Spring Boot backend + Vue 3 frontend.

## Funktsionaalsus
- Külastaja: koolituste ja kursuste sirvimine, koolitajad, päringu saatmine, registreerumine kursusele, konto loomine
- Osaleja: minu kursused, tunnistused, tagasiside andmine, profiil ja parooli muutmine
- Admin: koolituste, kursuste, koolitajate, ruumide, kasutajate, registreerimiste, päringute ja tagasiside haldus; tõlked

## Struktuur

```
backend/    Spring Boot 4.x / Java 21 REST API
frontend/   Vue 3 + Vite SPA
docs/       Dokumentatsioon ja andmebaasiskriptid
```

Backend ja frontend on eraldi arendatavad ja käivitatavad rakendused. Ehitus-, käivitus- ja testikäskude ning arhitektuuri kohta vaata `backend/CLAUDE.md` ja `frontend/CLAUDE.md`.
