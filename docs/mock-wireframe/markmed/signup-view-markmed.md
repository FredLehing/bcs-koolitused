# SignupView.vue — märkmed

Konto loomine (kasutaja + profiil + osaleja), pärast mida logitakse kohe sisse. Otsused, andmebaasi ettepanek ja skeemid: `docs/mock-wireframe/loo-mock-vaade/courses-view/courses-view-skeemid.md`. Interaktiivne läbimäng: `course-registration-view-labimang.html`. Uute teenuste DTO-d on ettepanek.

## Vaate märkmed

```text
Roll: Kõik rollid (sisse logimata)
Failinimi: SignupView.vue
Frontend rada: /signup?redirect={rada}

Vaatega seotud lisainfo:
Avaneb navbari nupust "Loo konto" ja login-lehe lingist "Loo konto" (redirect antakse edasi). Pealkiri "Loo konto".
Väljad: Eesnimi*, Perekonnanimi*, E-post*, Telefon*, Parool* (vähemalt 8 märki), Parool uuesti*. Frontendi kontroll: kohustuslikud väljad, e-posti kuju, parooli pikkus, paroolid ühtivad.
"Loo konto" → POST /api/user → vastus nagu sisselogimisel (userId, roleName) → sessionStorage → suunatakse redirect-rajale (nt /course-registration?courseId=9) või avalehele. E-post juba kasutusel → "Selle e-posti aadressiga konto on juba olemas" + link "Logi sisse".
All link "Mul on juba konto — logi sisse" → /login?redirect={rada}.
```

## API märkmed — POST /api/user

```text
API: POST /api/user

Request body:
SignupRequest.java
{
  "firstName": "Kati",
  "lastName": "Karu",
  "email": "kati.karu@example.com",
  "phone": "+37255512300",
  "password": "salasona1"
}

Response (200):
LoginResponse.java (sama mis POST /api/login)
{
  "userId": 8,
  "roleName": "participant"
}

API teenuse lisainfo:
Avalik teenus. Loob "user" rea (role = participant, status = "A"), profile rea ja participant rea (user_id, name = eesnimi + perekonnanimi, profile_id) — kasutajal on kohe oma osaleja. Vastus on sama mis sisselogimisel, frontend logib kasutaja kohe sisse.
E-post on unikaalne (tõstutundetu). Parool salvestatakse praegu nagu olemasolevatel kasutajatel (projekti turvalisuse lahendus on eraldi teema).

Veateated:
HTTP: 400 — valideerimise viga (@NotBlank, @Email, @Size(min = 8) parool)
HTTP: 403
errorCode: EMAIL_TAKEN
message: "Selle e-posti aadressiga konto on juba olemas"
```
