# Juhend: GET /api/funding-types

**Taski fail:** `GET-api-funding-types.md`
**Kontroller:** `FundingTypeController.java` (uus)
**Implementeerimise voog:** RestController → Service → Repository → Service → Mapper → RestController

---

## Sissejuhatus

See endpoint tagastab kõik rahastustüübid valitud keeles (`contentLang`). TrainingsView kasutab neid filtri "Rahastus" valikutena ja TrainingFormView vormi checkboxides.

DTO, repositoorium ja mapper on projektis juba olemas, sest `GET /api/trainings` kasutab neid iga koolituse rahastustüüpide kuvamiseks. Selle harjutuse **uus asi on query parameeter** (`@RequestParam`): see tuleb kontrollerisse sisse ja antakse edasi service'i ning sealt repositooriumi päringusse.

> **Tähelepanu:** selles projektis on persisteerimise pakett kirjutatud kujul `persistance` (mitte `persistence`).

---

## Samm 1 — RestController

### Mida teha?

Rahastustüüpide jaoks kontrollerit veel pole. Loo **uus kontroller** oma alampaketti `controller` kausta all, samamoodi nagu tegid keelte puhul.

- Kaust: `backend/src/main/java/ee/bcskoolitus/controller/`
- Kui kontroller **puudub**, loo uus klass IntelliJ-ga (paremklõps `controller` kaustal → New → Java Class, nimeks `alampakett.KlassiNimi`)

> **Mõtle:** Mis nime saab alampakett? Vaata, kuidas on nimetatud olemasolev pakett `persistance/fundingtype/`.
>
> **Kontrolli kohe:** kas klass tekkis õigesse alampaketti? Vaata faili esimest rida (`package ...`).

Uuel kontrolleril on vaja järgmisi klassiannotatsioone:

```java
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class KontrolleriKlass {
    // ...
}
```

### Meetodi loomine

Alusta meetodist **ilma mappingannotatsioonideta**. Seekord on meetodil **üks parameeter**:

```java
public void meetodiNimi(SisendTüüp parameetriNimi) {
    // tühi meetod esialgu
}
```

> **Mõtle:**
> - Mis on meetodi hea nimi? Vaata, kuidas nimetasid keelte meetodi.
> - Mis on parameetri **nimi** ja **tüüp**? Vaata taskifaili jaotist "Sisend".
> - Kust parameeter tuleb: URL-i **rajast** või **küsimärgi järelt**? Vaata taskifaili rida "Teenus". Selle põhjal vali annotatsioon `@PathVariable` või `@RequestParam`.
>   Eeskuju leiad `TrainingController`-ist.

Seejärel lisa:
1. **Mappingannotatsioon** `@GetMapping` koos õige rajaga (vaata taskifaili rida "Teenus", aga ilma `?...` osata)
2. **Parameetri annotatsioon** parameetri ette
3. **Swagger annotatsioonid** `@Operation` ja `@ApiResponses`. Mõtle taskifaili "Veaolukorrad" jaotise põhjal, milliseid vastuskoode dokumenteerida.

### Service klassi ettevalmistus

Kontrolli, kas sobiv service klass juba eksisteerib:
- Kaust: `backend/src/main/java/ee/bcskoolitus/service/`
- Kui **puudub**, loo uus klass otse `service` kausta

```java
@Service
@RequiredArgsConstructor
public class TeenusKlass {
    // ...
}
```

Lisa kontrolleri klassi service muutuja:

```java
private final TeenusKlass teenuseMuutuja;
```

Kutsu service meetod välja ja **anna parameeter edasi**:

```java
public void meetodiNimi(SisendTüüp parameetriNimi) {
    teenuseMuutuja.meetodiNimi(parameetriNimi);
}
```

> **Meeldetuletus:** pöördu **muutuja** poole (väike algustäht), mitte klassi poole. Muidu lisab IntelliJ sõna `static`.
>
> **IntelliJ vihje:** Vajuta punasel meetodinimel **Alt+Enter** → **"Create method ... in TeenusKlass"**.
> IntelliJ loob meetodi koos sama parameetriga.

---

## Samm 2 — Service ja repository

### Mida teha?

Service meetodisse tuleb sisse keelekood. Meetodi ülesanne on küsida andmebaasist kõigi rahastustüüpide nimed selles keeles.

### Millisest tabelist?

Vaata taskifaili jaotist "Seotud andmebaasi tabelid":

| Tabel | Mida hoiab |
|---|---|
| `funding_type` | ainult `id` ja tehniline `code`, **nime siin pole** |
| `funding_type_translation` | rahastustüübi **nimi** ühes keeles |
| `language` | keelekood (`et`/`en`) |

> **Mõtle:** Kumb tabel annab korraga nii rahastustüübi `id` (seose kaudu) kui ka **nime**?

Selle tabeli entiteedil on **repositoorium juba olemas**. Leia see `persistance/fundingtype/` kaustast.

Lisa repositoorium service'isse väljana: kirjuta meetodi sisse **väikese tähega** muutuja nime algus ja vajuta **Tab**.

```java
@Service
@RequiredArgsConstructor
public class TeenusKlass {

    private final EntiteetRepository entiteetRepository;

    public void meetodiNimi(SisendTüüp parameetriNimi) {
        entiteetRepository
    }
}
```

### Kas olemasolev meetod sobib?

Ava repositoorium ja vaata olemasolevat meetodit.

**Küsi endalt:**
- Mis parameetreid see meetod küsib? Kas meil on need kõik olemas?
- Kas see tagastab **kõik** rahastustüübid või ainult **ühe koolituse** omad?

> **Rusikareegel:** Kui olemasolev meetod teeb midagi muud, kui vaja, ära muuda seda, sest keegi teine kasutab seda juba.
> Tee **uus meetod** (vt samm 5).

Kui repository meetod tagastab midagi, **pane tulemus kohe muutujasse**.

---

## Samm 3 — DTO klass

### Mida teha?

Vastus peab olema `FundingTypeDto` objektide list. See DTO on **juba olemas**.

- Leia see kaustast `controller/common/dto/`
- Võrdle selle välju taskifaili JSON näidisega. Kas kõik klapib?

Uut DTO-d luua ei ole vaja.

---

## Samm 4 — Mapper

### Mida teha?

Repositooriumist tulevad **tõlketabeli entiteedid**. Need tuleb teisendada DTO-deks. Mapper on **juba olemas** samas kaustas, kus on repositoorium.

Ava mapper ja vasta endale:
- Mis meetodid seal on? Mis vahe neil on?
- Kas iga DTO target-väli on kaardistatud? Võrdle DTO välju `@Mapping` ridadega.
- Kumba meetodit on sul vaja, kui repositoorium tagastab listi?

> Pane tähele `source = "seotudObjekt.väli"` kuju. Nii jõutakse tõlketabeli entiteedist seotud entiteedi väljani.
> Sama nipp tuleb kasuks ka sammus 5.

### Service meetodi lõpetamine

Lisa mapper service'isse väljana (väike täht + **Tab**), kutsu listi meetod välja ja tagasta tulemus:

```java
public void meetodiNimi(SisendTüüp parameetriNimi) {
    List<EntiteetTüüp> entiteedid = entiteetRepository.meetodiNimi(parameetriNimi);
    return mapperMuutuja.toDtoKlassiNimid(entiteedid);
}
```

> **IntelliJ vihje:** Vajuta `return` peal **Alt+Enter** → **"Change method return type"**.

---

## Samm 5 — Repository (uus päring)

### Mida teha?

Taskifaili järgi:
- tagastatakse **kõik** rahastustüübid, st ilma koolituse tingimuseta,
- nimi on `contentLang` **keeles**,
- tulemus on sorteeritud **rahastustüübi `id`** järgi **kasvavalt**.

> **Mõtle enne kirjutamist:** Olemasolev päring samas failis teeb peaaegu sama asja:
> - milline osa sellest filtreerib **keele** järgi?
> - milline osa seob tulemuse **ühe koolitusega**? Kas seda osa on meil vaja?
> - milline osa **järjestab**? Kas see sobib meie nõudega?

### Uue meetodi loomine

**Variant A: JPA Buddy.** Pane kursor `{ }` vahele → **Alt+Insert** → **Repository Method...**
1. Vali **Query** variant ja **Find collection**
2. **Wrap type:** `List`
3. **Query condition:** keelekood. Parameetri nimi peaks olema sama, mis kontrolleris.
4. **Order By:** rahastustüübi `id`, õige suunaga

> Kui JPA Buddy ei lase seotud entiteedi välju valida, loo meetod lihtsama tingimusega ja paranda `@Query` tekst pärast käsitsi.

**Variant B: olemasoleva päringu eeskujul.** Kirjuta uus `@Query` meetod olemasoleva kõrvale, jättes välja osa, mida meil vaja pole.

Peale meetodi loomist:
- **Meetodi nimi:** lühike ja ütleb, **mida** meetod tagastab (vt `backend/CLAUDE.md` jaotist "Repositooriumi meetodi nimetamine" ja olemasolevat `findTrainingFundingTypeTranslationsBy`). Taskifail pakub ka ise nimeettepaneku.
- **Parameetri nimi** meetodis ja `@Query` sees (`:nimi`) peavad klappima.

Kasuta uut meetodit service'is ja anna talle keelekood edasi.

---

## Samm 6 — tagasi RestController'isse

### Mida teha?

Service meetod tagastab nüüd DTO-de listi. Mine tagasi kontrolleri meetodisse ja lisa `return`:

```java
public void meetodiNimi(SisendTüüp parameetriNimi) {
    teenuseMuutuja.meetodiNimi(parameetriNimi);  // <- enne: tulemus kasutamata
}
```

> **IntelliJ vihje:** Lisa `return` ja vajuta **Alt+Enter** → "Change return type".

Tulemus:

```java
public TagastatavTüüp meetodiNimi(SisendTüüp parameetriNimi) {
    return teenuseMuutuja.meetodiNimi(parameetriNimi);
}
```

---

## Samm 7 — kood ilusaks (refactor)

### Make it work → Make it beautiful

Kui kood töötab, vaata üle, kas koodi saab puhtamaks muuta.

**Extract Method IntelliJ-ga:**

Märgi service meetodis koodilõik, mille tahad eraldada helper meetodiks, ja tee paremklõps → Refactor → Extract Method.

> **Tähelepanu:** Ekstraktimisel annab IntelliJ helper meetodile parameetrina kogu objekti.
> Vaata üle, kas helper meetod vajab tegelikult kogu objekti või ainult üht välja, ja paranda vajadusel.

Enne:
```java
kontrolliMidagiHelper(dtoObjekt);

private void kontrolliMidagiHelper(DtoTüüp dto) {
    boolean onProbleem = repositoorium.kontrollimeetod(dto.getMingiVäli());
    if (onProbleem) {
        throw new MingiException(...);
    }
}
```

Pärast (parem, sest edasi antakse ainult vajalik):
```java
kontrolliMidagiHelper(dtoObjekt.getMingiVäli());

private void kontrolliMidagiHelper(VäljaTüüp väljaNimi) {
    boolean onProbleem = repositoorium.kontrollimeetod(väljaNimi);
    if (onProbleem) {
        throw new MingiException(...);
    }
}
```

> Selle taski service meetod on lühike. Ekstraktimist ei pruugi vaja olla, otsusta ise.

### Meetodite järjekord

Kontrolli meetodite järjekorda Java konventsiooni järgi:
1. `public` meetodid eespool
2. `private` meetodid tagapool
3. Järjesta ka väljakutsumise hierarhia järgi: peameetod üleval, helper meetodid all

---

## Kokkuvõte ja kontrollnimekiri

Enne kui pead koodi valmis, kontrolli läbi:

- [ ] Uus RestController on õiges alampaketis ja sellel on annotatsioonid `@RestController`, `@RequestMapping`, `@RequiredArgsConstructor`
- [ ] Kontrolleri meetodil on `@GetMapping`, `@RequestParam` parameeter, `@Operation` ja `@ApiResponses`
- [ ] Uus service klass on olemas ja sellel on annotatsioonid `@Service`, `@RequiredArgsConstructor`
- [ ] Repositooriumis on **uus** `@Query` meetod ja olemasolev meetod on muutmata
- [ ] Päring filtreerib keelekoodi järgi, ei ole seotud koolitusega ja järjestab rahastustüübi `id` järgi kasvavalt
- [ ] Repository meetodi nimi ütleb, mida see tagastab
- [ ] Kasutatakse olemasolevat `FundingTypeDto`-d ja mapperit, midagi pole topelt loodud
- [ ] Meetodite järjekord: `public` enne, `private` pärast, lisaks väljakutsumise hierarhia järgi
- [ ] Kood kompileerub ja endpoint on Swagger UI-s nähtav

---

> **Järgmine samm:** Testi endpointi Swagger UI kaudu (`http://localhost:8080/swagger-ui.html`):
> - `contentLang=et` → eestikeelsed nimed
> - `contentLang=en` → ingliskeelsed nimed
> - `contentLang=xx` → tühi list `[]`
