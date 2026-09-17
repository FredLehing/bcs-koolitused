# Edasised sammud pärast "uus projekt" skripti

See juhend on mõeldud sulle, kui oled just kasutanud `skill-uus-projekt` abi, et teha toorikprojektist oma rühma projekt (uus projekti nimi, backend package, frontend package name on juba vahetatud). Nüüd on jäänud veel mõned sammud, mida pead tegema **ise käsitsi**.

Käi need sammud läbi täpselt selles järjekorras.

## 1. Projekti kausta nime muutmine (ainult see, kes GitHubi repo loob)

**See samm kehtib ainult sellele rühmaliikmele, kes järgmisena (samm 2) projekti GitHubi lisab** — tema kohalik kaust kannab hetkel veel vana (toorikust päritud) nime. Ülejäänud rühmaliikmed saavad kausta juba õige nimega, kui nad hiljem (samm 5) GitHubist kloonivad, seega neile see samm ei kehti.

Kausta ei saa ümber nimetada ajal, kui see on IntelliJ's või Claude Code sessioonis lahti — kaust on sel ajal "kasutuses" ja operatsioonisüsteem ei luba seda ümber nimetada.

1. **Sulge IntelliJ IDEA** täielikult (File → Exit, või sule aken).
2. **Sulge ka Claude Code sessioon** (ja mis tahes terminaliaken), mis selles kaustas parasjagu töötab.
3. Alles nüüd, kui miski enam kausta lahti ei hoia:
   - **Windows Exploreris**: leia kaust (nt `C:\Projects\grupp\...`), tee paremklõps → **Rename** → sisesta uus nimi (sama, mis skilli abil valisite projekti nimeks, kebab-case) → Enter.
   - **Või uuest terminaliaknast** (nt WSL-is): `mv vana-kausta-nimi uus-kausta-nimi`
4. Kustuta uue nimega kausta seest `.idea` alamkaust (kui see on olemas) — see sisaldab IntelliJ projektiseadeid, mis viitavad veel vanale kausta nimele/asukohale, ja tekitatakse IntelliJ poolt uuesti automaatselt, kui projekt uuesti avatakse.
5. Ava IntelliJ uuesti ja ava projekt uuest asukohast (uue kausta nimega). IntelliJ loob `.idea` kausta uuena, õige kausta nime järgi.

## 2. GitHubi lisamine (teeb ainult ÜKS rühmaliige)

See samm tehakse ainult korra kogu rühma peale — kokku leppige, kes rühmast selle teeb. Kõik teised rühmaliikmed liituvad hiljem punktis 4 kirjeldatud viisil.

1. Ava projekti kaust (uue nimega, sammust 1) IntelliJ IDEA Ultimate'is (kui see pole juba avatud).
2. Ülemisest menüüst vali **Git → GitHub → Share Project on GitHub**.
3. Kui pole varem GitHubiga sisse logitud, palub IntelliJ sul autentida oma GitHubi kontoga — järgi ekraanil olevaid juhiseid (tavaliselt avaneb brauseriaken, kus kinnitad ligipääsu).
4. Avanevas aknas:
   - Sisesta repositooriumi nimi (soovitatavalt sama, mis su projekti kausta nimi).
   - Veendu, et **"Private"** märkeruut on **VÄLJA lülitatud** — repo peab olema **avalik (public)**, mitte privaatne.
   - Vajuta **Share**.
5. Avaneb commit-aken, kus on näha kõik failid, mis lähevad esimesse commiti. Kirjuta commit-sõnum, näiteks `Initial commit`.
6. Vajuta **Commit and Push**.
7. IntelliJ loob nüüd GitHubis uue repositooriumi ja pushib kogu koodi sinna. Eraldi `git init` ega esimest commit-käsku pole vaja ise teha — IntelliJ tegi selle sammu 5–6 käigus juba ära.

Kui kõik õnnestus, näed oma projekti GitHubi veebilehel (github.com) enda konto all uue repona.

## 3. Kontrolli, et repo on avalik

1. Ava oma repo leht GitHubi veebis (github.com/sinu-kasutajanimi/sinu-repo-nimi).
2. Kui repo nime kõrval on kirjas **"Public"**, on kõik korras.
3. Kui on kirjas **"Private"**, mine **Settings** → keri alla **"Danger Zone"** juurde → **"Change visibility"** → vali **"Change to public"** ja kinnita.

## 4. Rühmaliikmete lisamine (Collaborators)

Seda teeb see rühmaliige, kes repositooriumi lõi (samm 2).

1. Ava oma repo leht GitHubi veebis.
2. Vajuta ülemises menüüs **Settings**.
3. Vali vasakpoolsest menüüst **Collaborators**.
4. Vajuta nuppu **Add people**.
5. Sisesta iga rühmaliikme GitHubi kasutajanimi (või nendega seotud e-posti aadress) ja vali nimekirjast õige inimene.
6. Vajuta **Add [kasutajanimi] to this repository**.
7. Korda iga rühmaliikme jaoks.

Iga rühmaliige saab GitHubilt (ja/või e-mailile) kutse, mille nad peavad ise kinnitama:

1. Ava saadud kutse (link e-mailis, või GitHubi lehel paremal üleval kellake ikooni all **Notifications**).
2. Vajuta **Accept invitation**.
3. Pärast kinnitamist on sul õigus repositooriumisse kirjutada (push teha).

## 5. Projekti kloonimine (ülejäänud rühmaliikmetele)

Kui sa ei olnud see, kes repo GitHubi lisas (samm 2), pead nüüd projekti enda arvutisse tooma:

1. Ava GitHubis oma rühma repo leht.
2. Vajuta rohelist nuppu **Code** ja kopeeri HTTPS link.
3. Ava IntelliJ IDEA Ultimate ja vali **Get from VCS** (või menüüst **Git → Clone...**).
4. Kleebi link väljale **URL** ja vali, kuhu arvutisse projekt salvestada.
5. Vajuta **Clone**.

Kloonitud kaust kannab juba GitHubi repo nime (uus projekti nimi), seega eraldi kausta ümbernimetamist siin vaja pole.

## 6. Backendi käivitamine IntelliJ's (kehtib igale rühmaliikmele)

Enne käivitamist veendu, et lokaalne PostgreSQL töötab ja andmebaas on seadistatud vastavalt `backend/CLAUDE.md` juhistele (andmebaasi skriptide käivitamine kaustast `docs/database`).

1. Ava IntelliJ IDEA-s projekti kaust (kui see pole juba avatud).
2. Tee Gradle refresh, et IntelliJ tõmbaks sõltuvused alla ja sünkroniseeriks projekti struktuuri (eriti oluline pärast kloonimist, kuna IntelliJ ei pruugi Gradle seadistust automaatselt värskendada): ava paremal servas **Gradle** paneel (kui see pole nähtaval, **View → Tool Windows → Gradle**) ja vajuta ülal olevat **Reload All Gradle Projects** nuppu (kahe ringikujulise noolega ikoon).
3. Oota, kuni sünkroniseerimine lõpeb (näed edenemist alumises staatusribas) — see võib esimesel korral võtta mõne minuti, kuna sõltuvused laetakse alla.
4. Ava fail `backend/src/main/java/.../*Application.java` (Spring Booti main klass).
5. Klassi deklaratsiooni real (`public class ...Application`) või `main`-meetodi kõrval on rohelise kolmnurgaga **Run** nupp (gutter-ikoon rea numbri kõrval) — vajuta seda ja vali **Run '...Application'**.
6. Oota, kuni IntelliJ Gradle'i abil projekti ehitab ja rakenduse käivitab — **Run** paneelis peaks lõpuks ilmuma Spring Booti logi koos teatega, et rakendus on käivitunud (nt "Started ...Application").
7. Kui käivitamine ebaõnnestub andmebaasiühenduse veaga, kontrolli, kas PostgreSQL töötab ja andmebaasi seaded (kasutajanimi, parool, port) vastavad `backend/CLAUDE.md` kirjeldatule.

Kui backend käivitus edukalt, on see IntelliJ's tööks valmis — edasisi käske pole vaja käsurealt joosta, kuna Run nupp jääb sama seadistusega kättesaadavaks ka järgmistel kordadel.

## 7. Frontendi sõltuvuste paigaldamine (kehtib igale rühmaliikmele)

`frontend/node_modules` kaust kustutati projekti seadistamise käigus (see ei tohi sattuda GitHubi repositooriumisse ega vedada kaasa vanu/valesid sõltuvusi), seega tuleb see igal rühmaliikmel oma masinas uuesti paigaldada — nii sellel, kes projekti GitHubi lisas, kui neil, kes selle punktis 5 kloonisid.

**Tähtis:** see käsk tuleb käivitada **Windowsi keskkonnas** (nt IntelliJ enda terminalis või Windowsi Command Prompt/PowerShell/Git Bash), **mitte** WSL2 Ubuntu terminalist. Frontendi arendab ja käivitab (`npm run dev` jne) igaüks oma Windowsi node/npm'iga, seega peavad ka sõltuvused olema paigaldatud sama keskkonnaga — vastasel juhul võivad tekkida platvormispetsiifiliste pakettide vead.

Ava Windowsi terminal (nt IntelliJ alumises paneelis olev "Terminal", seadistatud Command Prompt/PowerShell peal) uue kausta nimega projekti kaustas ja käivita:

```sh
cd frontend
npm install
```

See loob `frontend/node_modules` kausta koos kõigi vajalike sõltuvustega. Kui käsk lõpeb veateateteta, on frontend arendustööks valmis (vt `frontend/CLAUDE.md` edasiste käskude, nt `npm run dev`, jaoks).

## 8. Frontendi käivitamine IntelliJ's (kehtib igale rühmaliikmele)

Selle sammu eesmärk on luua IntelliJ's `npm run dev` jaoks Run konfiguratsioon, et frontendi saaks edaspidi käivitada sama mugavalt kui backendi (Run nupuga), ilma iga kord käsitsi terminali käske sisestamata.

1. Ülal paremas nurgas Run/Debug konfiguratsioonide rippmenüüst (kus praegu on valitud nt `...Application`) vali **Edit Configurations...**.
2. Avanevas aknas vajuta vasakul üleval **+** (Add New Configuration) ja vali nimekirjast **npm**.
3. Täida seadistus:
   - **Name**: `frontend` (või mõni muu sulle sobiv nimi, mis eristub backendi konfiguratsioonist).
   - **package.json**: vali projekti `frontend/package.json` fail (mitte root kausta oma).
   - **Command**: `run`.
   - **Scripts**: `dev`.
4. Vajuta **OK**, et konfiguratsioon salvestada.
5. Nüüd on Run/Debug rippmenüüs valitav ka `frontend` konfiguratsioon — vali see ja vajuta rohelist **Run** (kolmnurga) nuppu, et frontend käivitada.
6. Konsoolis peaks ilmuma Vite dev-serveri väljund koos aadressiga (tavaliselt `http://localhost:5173`), mille kaudu saab rakendust brauseris avada.

Backendi ja frontendi konfiguratsioonid jäävad IntelliJ's alles ka järgmisteks kordadeks — mõlemat saab edaspidi käivitada otse Run nupuga, ilma käsurida kasutamata.

## Kokkuvõte — kontrolli, et kõik on tehtud

- [ ] Repo looja kohaliku projekti kausta nimi on vahetatud uueks (kebab-case) ja `.idea` kaust kustutatud/uuesti loodud
- [ ] Repo on GitHubis olemas ja **avalik**
- [ ] Kõik rühmaliikmed on **collaborators** all ja on kutse kinnitanud
- [ ] Kõik rühmaliikmed on projekti oma arvutisse kloonud
- [ ] Backend käivitub IntelliJ's veata (Run nupp `*Application` klassil)
- [ ] `cd frontend && npm install` on käivitatud igas rühmaliikme masinas **Windowsi keskkonnast** (mitte WSL2-st)
- [ ] Frontendi jaoks on IntelliJ's loodud npm Run konfiguratsioon (`run dev`) ja see käivitub veata
