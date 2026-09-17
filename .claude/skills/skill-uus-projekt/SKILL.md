---
name: skill-uus-projekt
description: Aita kasutajal olemasolevast toorikprojektist kiirelt luua uus projekt (uus kausta nimi, backend package, frontend package name), et see saaks GitHubi lisada. Kasuta, kui kasutaja tahab toorikust uut projekti teha, mainib "uus projekt", "projekti nime vahetus" vms.
---

Eesmärk on see, et kasutaja saab olemasolevast toorikprojektist kiirelt teha ühe uue projekti, mille saab siis GitHubi lisada.

## 1. Build-artefaktide ja sõltuvuste kustutamine

Kohe töö alguses kustuta järgmised kaustad (kui need on olemas), et neid poleks vaja hiljem ümber nimetada/kaasa vedada ega üleliigsete failidena ringi tassida:

- `backend/out`
- `backend/build`
- `frontend/node_modules`

Lisaks puhasta `backend/src/main/generated` kaust — eemalda selle KÕIK alamkaustad ja failid (nt `rm -rf backend/src/main/generated/*`), aga jäta `generated` kaust ise alles (see on annotation processor'i, nt QueryDSL, väljundikaust ja täitub uuesti järgmisel buildil).

Kustuta ka olemasolevad testiklassid, kui neid leidub `backend/src/test/java` all praeguse backend package'i juures (vt punkt 4 package tee tuvastamise kohta) — nt `rm -rf backend/src/test/java/<package-tee>`. Toorikprojekti testid viitavad sageli main klassidele, mis vahepeal on poolikult ümber tõstetud või puuduvad — nende kaasavedamine tekitab package-vahetuse järel eksitavaid `compileTestJava` vigu, mis ei ole seotud tegeliku package-vahetusega. Uus projekt saab need testid kirjutada ise nullist.

## 2. Näidis-domeenikoodi puhastamine (backend)

Enne selle sammu tegemist tuvasta backend'i praegune package tee: `find backend/src/main/java -maxdepth 6 -type d` ja vaata, mis on `*Application.java` faili nimi (nt `find backend/src/main/java -name "*Application.java"`). Toorikprojekt võib eri hetkedel olla eri seisus — mõnikord sisaldab see veel varasema näidisprojekti domeeni lähtekoodi (nt näidis-controllerid, -persistence, -service kaustad koos päris äriloogikaga), mõnikord on domeenikood juba eemaldatud ja alles on ainult `*Application.java` ja `infrastructure/`. Package nimi ise (nt `ee.minuprojekt` / `MinuprojektApplication`) on tooriku enda vaikimisi nimi ega ole märk sellest, et keegi on projekti juba ümber nimetanud — package name vahetus on alles samm 4, seega ära tõlgenda vaikenime nägemist kahtlase leiuna, mis vajaks eraldi uurimist.

Kui package'i all leidub main klasside kõrval veel muid kaustu või faile peale `infrastructure/` (nt `controller/`, `persistence/`, `service/`, või root-tasandi abifailid), siis kustuta need — uus rühm ehitab oma äriloogika ise nullist. Jäta alles ainult:
- `*Application.java` (Spring Booti main klass)
- `infrastructure/` (kogu kaust koos sisuga)

Kontrolli enne kustutamist alati `ls backend/src/main/java/<package-tee>`, kuna toorikprojekti sisu võib aja jooksul muutuda — kustuta täpselt see, mis pole main-klass ega `infrastructure/`. Kui package alt leiadki juba ainult need kaks, pole selles sammus midagi teha.

## 3. Projekti nimi

Küsi kasutajalt uue projekti nimi tavalise vestlussõnumina (mitte AskUserQuestion tööriistaga — see on vabas vormis tekstisisend, millel pole fikseeritud valikuid pakkuda). Suuna kasutajat, et see võiks olla kas inglisekeelne sõna/väljend või brändi/meeskonna nimi.

See nimi läheb root kausta nimeks. Kausta nimeks kasuta sisendit, teisendades selle lower kebab-case nimetamise konventsiooni järgi (nt "Meie uus projekt" → `meie-uus-projekt`).

**Root kausta ümbernimetamist ennast ÄRA tee siin ega üheski teises sammus** — praegune sessioon töötab selle kausta seest ja ei saa seda enda alt ümber nimetada ega kustutada. Ära loo ka koopiat teise kausta all. Kausta ümbernimetamine on samm, mille kasutaja teeb ise käsitsi pärast kõikide muudatuste valmimist, **enne** GitHubi lisamist — vt punkt 7 ja `edasised-sammud.md` samm 1.

Pärast kasutajalt nime saamist uuenda ka `README.md` esimene rida (pealkiri `# ...`) uue projekti nimega, kuna see peab kausta nimega vastavuses olema.

## 4. Backend package name

Loe kõigepealt praegune package tee otse failidest (nt `grep -rn "^package " backend/src/main/java | head -1` või `find backend/src/main/java -name "*Application.java"`) — ära eelda kindlat nime, kuna see sõltub sellest, mis nimega toorik varem seadistati.

Paku kasutajale mõni backend package name variant, hoides seda lühikesena (samas stiilis nagu praegune, tuletatuna uue projekti nimest). Küsi kasutajalt kinnitust valiku kohta (siin sobib AskUserQuestion, kuna pakud konkreetseid valikuid).

Pärast kinnitust vaheta package nimi läbivalt ära:
- praegune package kaustastruktuur (ja kõik selle alamkaustad) tuleb ümber tõsta uude package'i vastavasse kaustastruktuuri
- kõikides `src/main` Java failides vana `package ...` deklaratsioonid ja vastavad `import ...` read (punktist 1 tulenevalt on `src/test/java` alt vana package kaust selleks hetkeks juba kustutatud, seega testifaile ümber tõsta ei ole vaja)
- `backend/build.gradle` — `group = '...'` väärtus (loe praegune väärtus otse failist, ära eelda konkreetset stringi)

**Application-klass tuleb ka ise ümber nimetada**, mitte ainult ümber tõsta — Spring Booti konventsiooni järgi peab klassi nimi vastama uue package'i viimasele osale, Capitalized + "Application" (nt "Meie uus projekt" → package `ee.meieuusprojekt` → klass `MeieuusprojektApplication`, fail `MeieuusprojektApplication.java`). Selleks:
- nimeta fail ise ümber (nt `git mv` puudumisel tavaline `mv`)
- muuda faili sees klassi deklaratsioon (`public class ...`) ja `SpringApplication.run(...)` sees olev klassiviide uuele nimele
- kontrolli, kas mõni teine fail (nt testid, kui neid veel on) viitab vana Application-klassi nimele importide või muude viidete kaudu, ja uuenda need samuti

Kontrolli pärast muudatust, et backend jätkuvalt kompileerub: `./gradlew clean compileJava` backend kaustas. Kasuta kindlasti `clean` käsku (mitte pelgalt `compileJava`), sest Gradle võib muidu tagastada vana vahemällu jäänud (UP-TO-DATE) tulemuse, mis ei kajasta tegelikku package-vahetuse õnnestumist ega genereeri `generated` kausta sisu uuesti.

## 5. Frontend package name

Paku sarnaselt uus `name` väärtus faili `frontend/package.json` jaoks (loe hetke väärtus otse failist — see ei pruugi kattuda root-kausta nimega), tuletatuna uue projekti nimest. Nimi peab olema lower-kebab-case, sufiksiga `-front` (nt "Meie uus projekt" → `meie-uus-projekt-front`). Küsi kinnitust ja seejärel uuenda `frontend/package.json` väli `name`.

## 6. GitHubi lisamine

Enne juhendamist kontrolli, kas projekti root kaustas on `.git` kaust (nt `ls -la` või `git rev-parse --is-inside-work-tree`). Toorikprojekt ei tohi kanda kaasa vana git ajalugu — kui `.git` kaust on olemas, kustuta see ära (`rm -rf .git`), et uus repositoorium saaks alata puhta ajalooga ilma initial commitita. Kui `.git` kaust juba puudub, jäta see samm vahele.

Kui `.git` on eemaldatud (või puudus algusest peale), pole projektil veel git repositooriumi ega ühtegi commiti. Kui eelnevad sammud on tehtud, juhenda kasutajat, kuidas ta saab projekti IntelliJ Ultimate abil GitHubi lisada:

- Projekti tuleb lisada GitHubi **avaliku (public)** repositooriumina.
- Seda teeb ainult **üks** õpilane rühmast (mitte igaüks eraldi).
- Menüüst **Git → GitHub → Share Project on GitHub**. See samm teeb kõik korraga ära: initsialiseerib kohaliku git repositooriumi (eraldi `git init` pole vaja teha), avab commit-akna esimese (initial) commiti jaoks ning pärast kinnitamist loob GitHubis uue repo ja pushib commiti sinna — seega eraldi initial commit käsku ette teha ei ole vaja.
- Pärast repositooriumi loomist tuleb ülejäänud rühmaliikmed lisada **collaborators** alla, et neil oleks õigus repositooriumisse kirjutada.

Selgita mõlemat sammu (GitHubile lisamine IntelliJ Ultimate kaudu ja collaboratorite lisamine GitHubi repo seadetes) konkreetsete klikkide/menüükäikude tasemel, kuna tegu on õpilastega, kes ei pruugi seda varem teinud olla.

## 7. Edasised sammud (edasised-sammud.md)

Projekti root kaustas on juba olemas fail `edasised-sammud.md` (osa toorikust) — see sisaldab beginner-friendly samm-sammult juhendit kõigi käsitsi tehtavate sammude kohta, selles järjekorras: root kausta ümbernimetamine + `.idea` kausta kustutamine + IntelliJ taasavamine (ainult see rühmaliige, kes järgmisena GitHubi lisab), GitHubi lisamine, repo avalikuks kontroll, collaboratorite lisamine, kloonimine (ülejäänud rühmaliikmetele), backendi käivitamine IntelliJ's (Gradle refresh + Run nupp, igale rühmaliikmele), frontendi sõltuvuste paigaldamine (`npm install`, kuna `frontend/node_modules` kustutati sammus 1, igale rühmaliikmele) ning viimasena frontendi käivitamine IntelliJ's (npm Run konfiguratsioon `run dev`, igale rühmaliikmele). `npm install` tuleb käivitada **Windowsi keskkonnast** (mitte WSL2 Ubuntu terminalist), kuna frontendi arendatakse ja käivitatakse Windowsi node/npm'iga.

Ära loo seda faili uuesti nullist. Kui projekti nimi (samm 3) on juba teada, kontrolli faili üle ja vajadusel täpsusta/täienda seda konkreetse projekti nimega (nt näidiskäsu `mv vana-kausta-nimi uus-kausta-nimi` sees).

Vestluse kokkuvõttes chatis ära dubleeri kogu edasiste sammude teksti (kõik klõpsu-tasemel detailid jäävad ainult faili) — anna selle asemel lühike, mõne-realine kokkuvõte faili põhipunktidest (nt loeteluna: kausta ümbernimetamine + `.idea` kustutamine, GitHub, avalikkuse kontroll, collaboratorid, kloonimine, backendi käivitamine IntelliJ's, `npm install` ja frontendi käivitamine IntelliJ's) ja lõpeta viitega, et täpsed sammud on failis `edasised-sammud.md`.

**Ära ise käivita git/GitHub käske (nt `git remote add`, `gh repo create`, push) ega tee ise faili- või kaustamuudatusi enne, kui kasutaja on kõik nimed (projekti nimi, backend package, frontend package name) kinnitanud.** Kausta- ja failimuudatused (sammud 3–5) tohib teha alles pärast vastavat kinnitust — sammud 1–2 (build-artefaktide, testide ja näidiskoodi puhastamine) tohib teha kohe, kuna need ei sõltu kasutaja valitud nimedest. Root kausta ümbernimetamine ja GitHubi lisamine (samm 6) on kasutaja enda käsitsi tehtavad toimingud, mida ainult juhendad/dokumenteerid (vt `edasised-sammud.md` sammud 1–2 nende täpse järjekorra ja `.idea` kausta kustutamise kohta).

**`.git` kausta kustutamine (samm 6) on hävitav ja pöördumatu toiming.** Enne `rm -rf .git` käivitamist teavita kasutajat, mida see teeb (kogu kohalik git ajalugu kaob) ja küsi selleks eraldi kinnitust — ära kustuta seda vaikimisi koos teiste sammudega.

Suhtle kasutajaga eesti keeles.
