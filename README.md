# BCS Koolitused
- Mis rakendus see on: BCS Koolituste koolituste ja kursuste haldamise ning nendele registreerumise veebirakenduse prototüüp.
- Kelle jaoks see on: külastaja, osaleja ja administraator.
- Valmis Vali-IT! raames grupiprojektina.

## Funktsionaalsus 
- Külastaja: koolituste ja kursuste sirvimine, lektorid, päringu saatmine, registreerumine.
- Osaleja: oma kursused, tagasiside vorm, tunnistused, parooli muutmine.
- Administraator: koolituste, kursuste, lektorite, ruumide, kasutajate, päringute, registreerimiste ja tagasiside haldus.
- AI funktsionaalsus - õppekava PDF'st info eraldamine ning vastavate väljade täitmine.
- Mitmekeelsus: kasutajaliides on eesti ja inglise keeles, sisu tõlgitakse andmebaasi keelte põhjal.

## Struktuur

```
backend/    Spring Boot 4.x / Java 21 REST API
frontend/   Vue 3 + Vite SPA
docs/       Dokumentatsioon ja andmebaasiskriptid
```

Backend ja frontend on eraldi arendatavad ja käivitatavad rakendused. Ehitus-, käivitus- ja testikäskude ning arhitektuuri kohta vaata `backend/CLAUDE.md` ja `frontend/CLAUDE.md`.
