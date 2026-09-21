# Balsamiq märkmete struktuur (kollased/valged kastid)

See fail defineerib kaks korduvat märkme-tüüpi, mida kasutada Balsamiq mockup'i täiendamisel: **Vaate märkmed** (vaate üldinfo) ja **API märkmed** (üks backend teenuse kutse). Struktuur on teadlikult võtme-väärtus kujul (mitte vabas vormis proosa), et see oleks nii inimesele kui AI-le ühtviisi lihtsalt loetav ja masinaga töödeldav.

Iga vaate juurde tuleb täpselt **üks Vaate märkmete kast** ja **üks API märkmete kast iga backend kutse kohta**, mida see vaade teeb (nt kui vaade teeb 3 eri API kutset, tuleb 3 eraldi API märkmete kasti).

Mõlema tüübi juures kehtib sama loogika: kõigepealt struktuur ja reeglid, kohe seejärel päris näide sama taski (`LoginView.vue`, BEB-5/FEB-7) põhjal.

---

## 1. Vaate märkmed — struktuur

```text
Roll: <kes vaadet näeb — Kõik rollid / Admin / Customer / Külastaja (pole sisse logitud)>
Failinimi: <ComponentName.vue>
Frontend rada: <route path, nt /atms>

Vaatega seotud lisainfo:
<lühike, 1-4 rida — olulised käitumisreeglid/olukorrad, mida vaate juures on hea teada>
```

**Reeglid:**
- `Roll`, `Failinimi` ja `Frontend rada` käivad koos, ilma tühjade ridadeta nende vahel — need on vaate baasinfo.
- Enne `Vaatega seotud lisainfo:` tuleb üks tühi eraldusrida.
- `Roll` — kui vaate sisu erineb rolliti (nt admin näeb lisavälju), kirjuta see selgelt, nt `Roll: Kõik rollid (admin näeb lisaks edit/delete ikoone)`
- `Failinimi` — täpne `.vue` komponendi nimi, nagu see kavatsetakse koodis luua
- `Frontend rada` — Vue router path; kui rada kasutab query parameetrit, näita seda mustrina, nt `/location?locationId={id}` (vt url-kaardistus.md kokkulepet query vs path variable kohta — see puudutab **backend** API-t, frontend route ise kasutab query stringi, nagu varasemas vestluses kokku lepiti)
- `Vaatega seotud lisainfo` — lühike (1–4 rida) vabas vormis märkus vaate käitumise kohta mingites olukordades, mis pole eelnevatest väljadest ilmne. Näiteks: mis juhtub kui `locationId` query parameeter puudub (uue lisamise vorm vs muutmise vorm samal route'il), millised elemendid on tingimuslikult nähtavad/peidetud (nt "Sisse logimine" link kaob pärast edukat logimist), kuhu kasutaja pärast tegevust suunatakse. Kui vaate juures pole midagi sellist lisada, jäta väärtuseks `—`.

### Näide — LoginView.vue

```text
Roll: Külastaja (pole sisse logitud)
Failinimi: LoginView.vue
Frontend rada: /login

Vaatega seotud lisainfo:
Enne saatmist kontrollitakse, kas kõik väljad (Kasutajanimi, Parool) on täidetud — kui mitte, kuvatakse AlertDanger.vue komponendiga teade "Täida kõik väljad". Kui backend vastab errorCode'ga INCORRECT_CREDENTIALS, kuvatakse samas AlertDanger.vue's backend'i message väli ("Vale kasutajanimi või parool"). Eduka sisselogimise korral salvestatakse userId ja roleName sessionStorage'isse ning kasutaja suunatakse vaatele /atms.
```

### Näide — LocationView.vue

```text
Roll: Admin
Failinimi: LocationView.vue
Frontend rada: /location

Vaatega seotud lisainfo:
Menüü link "Asukoht" on nähtav ainult adminile.

Kui vaade avatakse ilma locationId query parameetrita ($route.query.locationId puudub), käitub see uue asukoha lisamise vormina (pealkiri "Lisa asukoht").

Nupule "Lisa" vajutades kogutakse lehelt kokku vajalikud andmed ning saadetakse backendile POST /api/atm/locations sõnumiga.

Nupule "Tagasi" vajutades suunatakse kasutaja tagasi /atms lehele (ilma API kutseta).
```

---

## 2. API märkmed — struktuur

```text
API: <METOOD> <path>

<RequestDtoClassName.java>
Request body:
{
  ...päris JSON näidis...
}

<ResponseDtoClassName.java>
Response (200):
{
  ...päris JSON näidis...
}

API teenuse lisainfo:
<lühike, 1-3 rida — teenuse eripärad, mida pole väljanimedest endist näha>

Veateated:
HTTP: <status>
errorCode: <ENUM_NIMI>
message: "<backend message väli>"

HTTP: <status>
errorCode: <ENUM_NIMI>
message: "<backend message väli>"
```

**Reeglid:**
- Iga plokk (`API:`, DTO+body paar, `API teenuse lisainfo:`, iga veajuhtum) on eraldatud tühja reaga.
- `API` rida — meetod + path muster, nii nagu spec (`stoplight_io_openAPI.json`) ja Jira taskid juba defineerivad. Path muster peab täpselt vastama JSON-ile (nt path variable `{locationId}`, mitte query param). Konkreetsed väärtused paistavad juba `Request body`/`Response` näidetest, seega `API` rida ei vaja eraldi näidis-URL'i.
- **DTO nimi käib alati vahetult vastava body ploki kohal**, mitte eraldi ühtse `DTO:` reana üleval:
    - Kui operatsioon võtab sisse request body, kirjuta `<RequestDtoClassName.java>` real vahetult enne `Request body:` plokki.
    - Response DTO nimi (`<ResponseDtoClassName.java>`) käib vahetult enne `Response (200):` plokki.
    - Kui operatsioonil pole request body't (nt lihtne GET/DELETE), jäta `Request body` osa täielikult ära ja alusta otse response DTO-st.
    - Kui operatsioonil pole response body't (nt POST/PUT/DELETE, mis tagastab tühja 200), kirjuta `Response (200): NONE` ilma DTO nimeta selle kohal.
- `API teenuse lisainfo` — lühike (1–3 rida) vabas vormis märkus teenuse käitumise kohta, mis pole väljanimedest endist ilmne. Näiteks: filtri erikäitumine (`cityId=0` tagastab kõik), valikulised väljad (`imageData` võib olla tühi string), soft delete, vms. Kui teenusel pole midagi sellist lisada, jäta väärtuseks `—`.
- `Veateated` — iga veajuhtum on eraldi kolmerealine plokk, alati sama kolme võtmega samas järjekorras:
    - `HTTP:` — staatuskood (nt `404`, `403`)
    - `errorCode:` — backend ENUM-nimi (mitte number, vastavalt meie kokkuleppele), nt `PRIMARY_KEY_NOT_FOUND`
    - `message:` — backend `message` välja täpne sisu, nii nagu see JSON response'is tuleb
    - Mitme veajuhtumi vahel jäta üks tühi rida
    - Kui vigu pole, kirjuta `Veateated: —`

### Näide — POST /api/login

```text
API: POST /api/login

LoginRequestDto.java
Request body:
{
  "username": "admin",
  "password": "123"
}

LoginResponseDto.java
Response (200):
{
  "userId": 1,
  "roleName": "admin"
}

API teenuse lisainfo:
Süsteemist otsitakse username ja password abil kasutajat, kelle konto on aktiivne (user tabeli status = 'A'). roleName võib olla nt "admin" või "customer".

Veateated:
HTTP: 403
errorCode: INCORRECT_CREDENTIALS
message: "Vale kasutajanimi või parool"
```

### Näide — POST /api/atm/locations

```text
API: POST /api/atm/locations

AtmLocationCreateRequestDto.java
Request body:
{
  "cityId": 2,
  "locationName": "Mustamäe Prisma",
  "numberOfAtms": 3,
  "imageData": "BASE64-image-data",
  "lng": 123,
  "lat": 123,
  "transactionTypes": [
    {
      "transactionTypeId": 1,
      "transactionTypeName": "raha sisse",
      "isAvailable": true
    },
    {
      "transactionTypeId": 2,
      "transactionTypeName": "raha välja",
      "isAvailable": true
    },
    {
      "transactionTypeId": 3,
      "transactionTypeName": "maksed",
      "isAvailable": true
    }
  ]
}

Response (200): NONE

API teenuse lisainfo:
imageData ja transactionTypes on kohustuslikud väljad. imageData on tühi string (""), kui pilti ei lisata — sel juhul pilti süsteemi ei lisata. transactionTypeName välja infot backend koodis ei kasuta.

Veateated:
HTTP: 403
errorCode: LOCATION_UNAVAILABLE
message: "Sellise nimega pangaautomaadi asukoht on juba süsteemis olemas"

HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'cityId' väärtusega: 123"
```

---

## Kokkuvõte — kuidas ühte vaadet Balsamiqus märgistada

1. Lisa vaate mockup'i kõrvale/juurde üks kollane/valge kast **Vaate märkmed** struktuuriga.
2. Iga backend kutse kohta, mida see vaade teeb, lisa eraldi kast **API märkmed** struktuuriga; kasti pealkirjaks/nimeks võib panna lühidalt API path (nt "GET /api/cities"), et need Balsamiqu vaates kergesti eristuksid.
3. Kõik path/DTO/errorCode väärtused peavad ühtima `docs/stoplight_io_openAPI.json` ja vastava Jira taski (`docs/jira-updates/*.md`) sisuga — need kolm allikat (Balsamiq, Jira, OpenAPI) kirjeldavad sama asja kolmest eri vaatenurgast ja peavad olema omavahel süncis.
