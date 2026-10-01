# Koolituse detailvaade /training

**Seis:** implementeeritud harus `RAIN-training-view`.
**Vaade:** `TrainingView.vue`, kõigile rollidele; muutmise ikoon ainult adminile.

## Paigutus ja käitumine

Kahe veeru paigutus nagu `/course`: vasakul 8/12, paremal 4/12; kitsal ekraanil
kaardid üksteise all. Ülal ühine „← Tagasi“ kasutab `returnTo`, varusiht `/trainings`.

| Asukoht | Kaart | Sisu ja reeglid |
|---|---|---|
| Vasak, esimene | Koolituse info | Pealkiri, admini muutmise ikoon kohe selle järel, esiletõstmise täht, lühikirjeldus, rich text kirjeldus, vajadusel põhikeele varuteksti märkus |
| Vasak, info all | Õppekava (PDF) | Valitud tõlke failinimi allalaadimislingina ja suurus MB-des; ilma selle keele failita kogu kaart peidetud |
| Parem, esimene | Koolituse andmed | Õppekeele lipp, kategooria, rahastus, asukoht (veebiasukohal veebimärgis), tellitavus |
| Parem | Koolitajad | `LecturerCard` saab koondvastuse kirje prop-iga `lecturerSummary`; pole aktiivseid koolitajaid → kaart peidetud |
| Parem | Järgmised koolitused | Sama koolituse avalikud tulevased toimumiskorrad, kuupäevad ja Kohapeal/Veebis/Kohapeal + Veebis; F korral Täis märgis; tühjal nimekirjal selgitav tekst |
| Parem, viimane | Kalender | Link üldkalendrisse `/courses` |

Koolituse üldandmetesse ei kopeerita ühe suvalise toimumiskorra hinda, kuupäevi
või mahtu. Need kuuluvad konkreetsesse `/course` vaatesse.

## Andmed ja navigeerimine

Üks `GET /api/training-summary/{trainingId}?contentLang=...` päring;
valikulise URL-i `trainingTranslationId` korral lisatakse see päringusse.
Tõlke valik ja PDF-i kuvamine: [backendi task](../backend/GET-api-training-summary-trainingId.md).
PDF laaditakse alla olemasoleva tõlke õppekava teenusega; `download` atribuut
ja serveri `Content-Disposition: attachment` annavad failile õige nime.

Keelevahetus eemaldab kindla tõlke ID, säilitab `returnTo` ja laadib uue keele
andmed. Route'i muutudes laaditakse kogu vaade uuesti; hilinenud päring ei tohi
kirjutada üle uuema päringu vastust. Laadimisel on tekst, vea või vigase
koolituse ID korral veateade; eelmise koolituse/PDF-i andmed eemaldatakse kohe.

Koolitaja ja admini muutmise lingid annavad kaasa lähte-URL-i `returnTo`.
„Järgmised koolitused“ kaardi lingid avavad `/course?courseId={id}` ilma
`returnTo` parameetrita; sealt „Tagasi“ viib varusihtkohta `/courses`.
Üldkalendri link valib iseseisva jaotise. Vormist avatud tõlke
PDF tähendab ainult salvestatud faili, mitte vormis valitud salvestamata faili.

## Mock ja skeemid

[Interaktiivne läbimäng](../../mock-wireframe/loo-mock-vaade/training-view/training-view-labimang.html),
[skeemid](../../mock-wireframe/loo-mock-vaade/training-view/training-view-skeemid.md),
[märkmed](../../mock-wireframe/markmed/training-view-markmed.md).
Mock on ühendatud ühise `index.html` kestaga ja kasutab sama tagasitee lahendust.
Eraldi avamisel saab muuta keelt, rolli, PDF-i olemasolu, tulevaste koolituste
olemasolu, avada inglise tõlke eelvaate ja katsetada veaolekut.

## Kontrollid

Backendi kogu testikomplekt: 301 testi, 0 viga (sh 8 koolituse detaili testi
ja koolitajate hulgi laadimise kontrollid). Frontendi muudetud JS/Vue failide ESLint ja
Linuxi koopias tehtud tootmisbuild läbivad. Vue renderduse kontroll kinnitab
kaartide sisu ja järjekorra, PDF-i lingi/peitmise, tühjad/laadimise/veaolekud,
returnTo lingid ja hilinenud vastuste tõrjumise. Mocki keele/PDF-i/vea
läbimängud ning näidis-PDF-i failivorming läbivad kontrolli.
Visuaalset brauserikontrolli ei tehtud.

## Koolitajakaartide päringud

Koolitajate kaartide tekstid ja foto versioonid tulevad vaate koondvastuse
`lecturers` massiivist. LecturerCard ei tee JSON-päringuid ega pea oma
laadimisolekut. Keelevahetusel laadib andmed uuesti vaade. Kursuse
registreerumisstaatus (ainult sisseloginud kasutajale) ja fotode failipäringud
on endiselt eraldi. `/lecturers` ja `/lecturer` töötavad juba sama põhimõttega.
