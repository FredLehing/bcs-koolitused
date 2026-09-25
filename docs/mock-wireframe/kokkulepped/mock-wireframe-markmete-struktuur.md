# Mockupi märkmete struktuur (kollased/valged kastid)

See fail defineerib kaks korduvat märkme-tüüpi, mida kasutada mockup'i/wireframe'i täiendamisel (tööriistast sõltumata — nt Balsamiq, Figma): **Vaate märkmed** (vaate üldinfo) ja **API märkmed** (üks backend teenuse kutse). Struktuur on teadlikult võtme-väärtus kujul (mitte vabas vormis proosa), et see oleks nii inimesele kui AI-le ühtviisi lihtsalt loetav ja masinaga töödeldav.

Iga vaate juurde tuleb täpselt **üks Vaate märkmete kast** ja **üks API märkmete kast iga backend kutse kohta**, mida see vaade teeb (nt kui vaade teeb 3 eri API kutset, tuleb 3 eraldi API märkmete kasti).

Mõlema tüübi juures kehtib sama loogika: kõigepealt struktuur ja reeglid, kohe seejärel näited (sisselogimine ja üldine olemi lisamise vaade).

---

## 1. Vaate märkmed — struktuur

```text
Roll: <kes vaadet näeb — Kõik rollid / Admin / Customer / Külastaja (pole sisse logitud)>
Failinimi: <ComponentName.vue>
Frontend rada: <route path, nt /entities>

Vaatega seotud lisainfo:
<lühike, 1-4 rida — olulised käitumisreeglid/olukorrad, mida vaate juures on hea teada>
```

**Reeglid:**
- `Roll`, `Failinimi` ja `Frontend rada` käivad koos, ilma tühjade ridadeta nende vahel — need on vaate baasinfo.
- Enne `Vaatega seotud lisainfo:` tuleb üks tühi eraldusrida.
- `Roll` — kui vaate sisu erineb rolliti (nt admin näeb lisavälju), kirjuta see selgelt, nt `Roll: Kõik rollid (admin näeb lisaks edit/delete ikoone)`
- `Failinimi` — täpne `.vue` komponendi nimi, nagu see kavatsetakse koodis luua
- `Frontend rada` — Vue router path, tuletatakse vaate failinimest (vt jaotis 3); kui rada kasutab query parameetrit, näita seda mustrina, nt `/entity-form?entityId={id}` (frontend route kasutab query stringi; backend API path'is kasutatakse sama ID jaoks path variable'it, nt `/api/entity/{entityId}`)
- `Vaatega seotud lisainfo` — lühike (1–4 rida) vabas vormis märkus vaate käitumise kohta mingites olukordades, mis pole eelnevatest väljadest ilmne. Näiteks: mis juhtub kui `entityId` query parameeter puudub (uue lisamise vorm vs muutmise vorm samal route'il), millised elemendid on tingimuslikult nähtavad/peidetud (nt "Sisse logimine" link kaob pärast edukat logimist), kuhu kasutaja pärast tegevust suunatakse. Kui vaate juures pole midagi sellist lisada, jäta väärtuseks `—`.

### Näide — LoginView.vue

```text
Roll: Külastaja (pole sisse logitud)
Failinimi: LoginView.vue
Frontend rada: /login

Vaatega seotud lisainfo:
Enne saatmist kontrollitakse, kas kõik väljad (Kasutajanimi, Parool) on täidetud — kui mitte, kuvatakse AlertDanger.vue komponendiga teade "Täida kõik väljad". Kui backend vastab errorCode'ga INCORRECT_CREDENTIALS, kuvatakse samas AlertDanger.vue's backend'i message väli ("Vale kasutajanimi või parool"). Eduka sisselogimise korral salvestatakse userId ja roleName sessionStorage'isse ning kasutaja suunatakse vaatele /entities.
```

### Näide — EntityFormView.vue

```text
Roll: Admin
Failinimi: EntityFormView.vue
Frontend rada: /entity-form

Vaatega seotud lisainfo:
Menüü link "Lisa uus" on nähtav ainult adminile.

Kui vaade avatakse ilma entityId query parameetrita ($route.query.entityId puudub), käitub see uue olemi lisamise vormina (pealkiri "Lisa uus").

Nupule "Lisa" vajutades kogutakse lehelt kokku vajalikud andmed ning saadetakse backendile POST /api/entity sõnumiga.

Nupule "Tagasi" vajutades suunatakse kasutaja tagasi /entities lehele (ilma API kutseta).
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
- `API` rida — meetod + path muster, nii nagu see on (või saab olema) backend controller'is; uue teenuse path tuletatakse URL-ide kokkuleppe järgi (vt jaotis 3). Path muster peab täpselt vastama backendi mustrile (nt path variable `{entityId}`, mitte query param). Konkreetsed väärtused paistavad juba `Request body`/`Response` näidetest, seega `API` rida ei vaja eraldi näidis-URL'i.
- **DTO nimi käib alati vahetult vastava body ploki kohal**, mitte eraldi ühtse `DTO:` reana üleval:
    - Kui operatsioon võtab sisse request body, kirjuta `<RequestDtoClassName.java>` real vahetult enne `Request body:` plokki.
    - Response DTO nimi (`<ResponseDtoClassName.java>`) käib vahetult enne `Response (200):` plokki.
    - Kui operatsioonil pole request body't (nt lihtne GET/DELETE), jäta `Request body` osa täielikult ära ja alusta otse response DTO-st.
    - Kui operatsioonil pole response body't (nt POST/PUT/DELETE, mis tagastab tühja 200), kirjuta `Response (200): NONE` ilma DTO nimeta selle kohal.
- **JSON massiivide (array) reegel:** Kui JSON näidises on massiiv (juurtasemel või objekti sees), pannakse näidisesse **ainult üks element**, mille järel on koma ja järgmisel real `...` (kolm punkti), mis viitab sellele, et elemente võib olla rohkem.
- `API teenuse lisainfo` — lühike (1–3 rida) vabas vormis märkus teenuse käitumise kohta, mis pole väljanimedest endist ilmne. Näiteks: filtri erikäitumine (`parentId=0` tagastab kõik), valikulised väljad (`imageData` võib olla tühi string), soft delete, vms. Kui teenusel pole midagi sellist lisada, jäta väärtuseks `—`.
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

### Näide — POST /api/entity

```text
API: POST /api/entity

EntityCreateRequestDto.java
Request body:
{
  "parentId": 2,
  "entityName": "Näidisolem",
  "quantity": 3,
  "imageData": "BASE64-image-data",
  "relatedTypes": [
    {
      "relatedTypeId": 1,
      "relatedTypeName": "tüüp 1",
      "isSelected": true
    },
    ...
  ]
}

Response (200): NONE

API teenuse lisainfo:
imageData ja relatedTypes on kohustuslikud väljad. imageData on tühi string (""), kui pilti ei lisata — sel juhul pilti süsteemi ei lisata. relatedTypeName välja infot backend koodis ei kasuta.

Veateated:
HTTP: 403
errorCode: ENTITY_UNAVAILABLE
message: "Sellise nimega olem on juba süsteemis olemas"

HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'parentId' väärtusega: 123"
```

---

## 3. URL-ide kokkulepe

Kehtib uute vaadete ja teenuste kohta. Kui vastav vaade/controller on koodis juba olemas, on tõde kood.

### Vaate nimi (`Failinimi`)

Vaate failinimi näitab, mis tüüpi vaatega on tegu:

| Vaate tüüp | Nimekuju | Näide |
|---|---|---|
| nimekiri | `<Ressurss mitmuses>View.vue` | `EntitiesView.vue` |
| ühe objekti vaatamine | `<Ressurss>View.vue` | `EntityView.vue` |
| vorm, millega saab uusi andmeid **lisada** ja/või olemasolevaid **muuta** | `<Ressurss>FormView.vue` | `EntityFormView.vue` |

Lisamine ja muutmine on sama vorm: ilma ID-ta avatuna on see lisamise vorm, ID-ga (`?entityId={id}`) avatuna muutmise vorm.

### Vaate rada (`Frontend rada`)

Tuletatakse vaate failinimest: eemalda lõpust `View`, ülejäänu teisenda kebab-case'iks ja lisa ette `/`.

| Vaade | Frontend rada |
|---|---|
| `HomeView.vue` | `/` (erand: avaleht) |
| `EntityView.vue` | `/entity` |
| `EntitiesView.vue` | `/entities` |
| `EntityFormView.vue` | `/entity-form` (muutmisel `/entity-form?entityId={id}`) |
| `UserProfileView.vue` | `/user-profile` |

Konkreetse objekti ID antakse vaatele query stringis: `/entity?entityId={id}`.

### API tee (`API:` rida)

- Alati kujul `/api/<ressurss>` — **ilma** valdkonna eesliiteta (mitte `/api/atm/...`).
- Ressursi nimi tuleneb andmebaasi tabeli nimest; mitmesõnaline nimi kebab-case'is (`related_type` → `related-type` / `related-types`).
- **Ainsus või mitmus sõltub sellest, mida teenus tagastab või millega opereerib:** ühe objekti teenus on ainsuses, nimekirja teenus mitmuses.
- Konkreetse objekti ID on path variable'ina (`{entityId}`), nimekirja filtrid query parameetritena.

| Mida teenus teeb | Tee |
|---|---|
| tagastab nimekirja | `GET /api/entities` |
| tagastab filtreeritud nimekirja | `GET /api/entities?parentId=2` |
| tagastab ühe objekti | `GET /api/entity/{entityId}` |
| loob ühe objekti | `POST /api/entity` |
| muudab ühte objekti | `PUT /api/entity/{entityId}` |
| kustutab ühe objekti | `DELETE /api/entity/{entityId}` |
| tagastab ühe objekti alamnimekirja | `GET /api/entity/{entityId}/related-types` |

Kui API tee pole veel koodis ega backend taskis olemas, tuletatakse see selle kokkuleppe järgi ja pakutakse kasutajale koos põhjendusega (nt "tagastab ühe objekti → ainsus") kinnitamiseks välja.

---

## Kokkuvõte — kuidas ühte vaadet mockupis märgistada

1. Lisa vaate mockup'i kõrvale/juurde üks kollane/valge kast **Vaate märkmed** struktuuriga.
2. Iga backend kutse kohta, mida see vaade teeb, lisa eraldi kast **API märkmed** struktuuriga; kasti pealkirjaks/nimeks võib panna lühidalt API path (nt "GET /api/entities"), et need mockupi vaates kergesti eristuksid.
3. Kõik path/DTO/errorCode väärtused peavad ühtima olemasoleva backend koodiga (`backend/src/main/java/`) ja vastava backend taskiga (`docs/tasks/backend/*.md`), kui need on olemas — mockup, task ja kood kirjeldavad sama asja kolmest eri vaatenurgast ja peavad olema omavahel sünkis.
