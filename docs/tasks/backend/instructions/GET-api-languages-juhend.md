# Juhend: GET /api/languages

**Taski fail:** `GET-api-languages.md`
**Kontroller:** `LanguageController.java` (uus)
**Implementeerimise voog:** RestController → Service → Repository → Service → Mapper → RestController

---

## Sissejuhatus

See endpoint tagastab kõik süsteemis olevad keeled. TrainingsView kasutab neid filtri "Koolituse keel" valikutena. Päring käib läbi kõigi kihtide: kontroller võtab päringu vastu, service küsib repositooriumilt keeled õiges järjekorras ja mapper teisendab entiteedid DTO-deks.

Suur osa vajalikust on projektis juba olemas: entiteet, repositoorium, DTO ja mapper. Selle harjutuse käigus õpid, kuidas **uus endpoint olemasolevate klasside peale ehitada**, ja teed JPA Buddy abil repositooriumi päringu, mis tulemuse **järjestab**.

> **Tähelepanu:** selles projektis on persisteerimise pakett kirjutatud kujul `persistance` (mitte `persistence`). Kasuta seda, mis projektis juba olemas on.

---

## Samm 1 — RestController

### Mida teha?

Keelte jaoks kontrollerit veel pole. Olemasolevad kontrollerid on `LoginController` ja `TrainingController`, aga keeled ei kuulu kumbagi. Seega tuleb luua **uus kontroller** keelte jaoks, oma alampaketti `controller` kausta all (vaata, kuidas `training` alampakett on üles ehitatud).

Kontrolli esmalt, kas vastav kontrolleri klass juba eksisteerib:
- Kaust: `backend/src/main/java/ee/bcskoolitus/controller/`
- Kui **puudub**, loo uus klass IntelliJ-ga (File → New → Java Class)
- Kui **on olemas**, ava see klass ja lisa sinna uus meetod

> **Kontrolli kohe:** kas uus klass tekkis õigesse alampaketti? Levinud viga on see, et klass satub otse `controller` paketti või topeltpaketti.

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

Alusta meetodist **ilma mappingannotatsioonideta**. Nii saad kõigepealt loogika paika:

```java
public void meetodiNimi() {
    // tühi meetod esialgu
}
```

> **Mõtle:** Mis on selle meetodi hea nimi? Nimi peaks kirjeldama, mida meetod teeb.
> Vaata taskifailist HTTP meetodit ja API teed, need annavad vihje.
>
> **Mõtle ka:** Kas sellel endpointil on üldse sisendparameetreid? Vaata taskifaili jaotist "Sisend".

Seejärel lisa:
1. **Mappingannotatsioon** `@GetMapping` koos õige rajaga
2. **Parameetrite annotatsioonid**, kui neid on vaja (`@PathVariable` / `@RequestParam`)
3. **Swagger annotatsioonid** `@Operation` ja `@ApiResponses`. Vaata näidet `TrainingController`-ist ja mõtle, millised vastuskoodid on taskifaili "Veaolukorrad" jaotise järgi selle endpointi puhul asjakohased.

> **Mõtle:** Kas `@Operation` summary kirjeldab täpselt, mida endpoint teeb? Loe see valmis kujul veel korra üle.

### Service klassi ettevalmistus

Enne kui kutsud kontrollerist välja service meetodi, kontrolli, kas sobiv service klass juba eksisteerib:
- Kaust: `backend/src/main/java/ee/bcskoolitus/service/`
- `LoginService` kasutab küll keeli, aga see on sisselogimise loogika jaoks. Uus endpoint vajab oma service klassi.
- Kui sobiv klass **puudub**, loo uus klass IntelliJ-ga (File → New → Java Class)

```java
@Service
@RequiredArgsConstructor
public class TeenusKlass {
    // ...
}
```

Kui service klass on olemas või just loodud, lisa kontrolleri klassi service muutuja:

```java
private final TeenusKlass teenuseMuutuja;
```

Kutsu kontrolleri meetodist välja service meetod. Esialgu on see tühi väljakutse:

```java
public void meetodiNimi() {
    teenuseMuutuja.meetodiNimi();
}
```

> **IntelliJ vihje:** Kui `teenuseMuutuja.meetodiNimi()` on punasega alla joonitud,
> vajuta punasel joonel **Alt+Enter** ja vali **"Create method in TeenusKlass"**.
> IntelliJ loob service klassi automaatselt vastava meetodi.

---

## Samm 2 — Service ja repository päring

### Mida teha?

Nüüd liigud service klassi äsja loodud meetodisse. Sisendeid pole. Meetodi ülesanne on küsida andmebaasist kõik keeled ja tagastada need õiges järjekorras.

Ava service klass (lõid selle sammus 1) ja mine äsja loodud meetodisse.

### Repository ühenduse loomine

Mõtle: **millisest tabelist** on vaja andmeid pärida?
Vaata taskifaili jaotist "Seotud andmebaasi tabelid", sealt leiad tabeli.

Alusta service meetodis repositooriumi muutuja nime kirjutamist:

```java
public void meetodiNimi() {
    entiteetRep  // <- kirjuta algus siia
}
```

> **IntelliJ vihje:** Kirjuta muutuja nime algus (nt `entiteetRep`) ja IntelliJ pakub
> vastavat repositooriumi. Vajuta **Tab** ja repositoorium lisatakse klassiväljana.

Tulemus võiks välja näha nii:

```java
@Service
@RequiredArgsConstructor
public class TeenusKlass {

    private final EntiteetRepository entiteetRepository;

    public void meetodiNimi() {
        entiteetRepository
    }
}
```

Selle entiteedi repositoorium on projektis **juba olemas**. Leia see `persistance` paketist üles.

**Küsi endalt:** Kas JPA pakub valmis meetodit, mis annab kõik read? Ja kas see meetod garanteerib ka **järjekorra**, mida taskifail nõuab (vaata Väljundi jaotist "Järjestus")?

> **Rusikareegel:** Kui tulemus peab tulema kindlas järjekorras, ei piisa JPA baasmeetoditest.
> Siis on vaja **uut repository meetodit**, millel on oma järjestus (vt samm 5).

Kui repository meetod tagastab midagi, **pane tulemus kohe muutujasse**.

---

## Samm 3 — DTO klass

### Mida teha?

Tavaliselt lood selles etapis väljundi DTO klassi. Selle taski puhul on DTO **juba olemas**, sest seda kasutab ka sisselogimise vastus.

Mõtle: kus see DTO asub?
- Vaata taskifailist, mis nimega DTO-d vastus kasutab
- Otsi seda kaustast `backend/src/main/java/ee/bcskoolitus/controller/`

> **Mõtle:** Miks asub see DTO kaustas `controller/common/dto/`, mitte mõne üksiku ressursi `dto/` kaustas? Vihje: vaata `backend/CLAUDE.md` jaotist "Jagatud DTO-d".

Ava DTO klass ja võrdle selle välju taskifaili JSON näidisega. Kas kõik väljad klapivad? Uut DTO-d luua ei ole vaja.

---

## Samm 4 — Mapper

### Mida teha?

Repositooriumist saadud entiteetide list tuleb teisendada DTO-de listiks. Ka mapper on juba olemas: sama mapperit kasutab `LoginService`.

### Mapper

Leia mapper interface üles. See asub entiteediga samas `persistance` alampaketis.

Uuri mapperit ja vasta endale:
- Kas mapperis on meetod **üksiku** entiteedi teisendamiseks? Kas mapperis on meetod **listi** teisendamiseks?
- Kas iga DTO target-väli on kaardistatud kas `source`-iga või `ignore = true`-ga? Võrdle DTO välju `@Mapping` ridadega ükshaaval.
- Kumba meetodit sul on vaja, kui repositoorium tagastab listi?

> **Meeldetuletus:** `@Mapping` annotatsioonid käivad alati **üksiku objekti** meetodile (ainsuses).
> Listi meetod (mitmuses) jääb annotatsioonideta. MapStruct genereerib selle ise ja kutsub iga elemendi kohta üksiku objekti meetodit.

Kui mapper vastab kõigile nõuetele, ei ole seal midagi vaja muuta.

### Service meetodi lõpetamine

Kutsu service meetodis välja mapperi meetod ja tagasta tulemus:

```java
public void meetodiNimi() {
    List<EntiteetTüüp> entiteedid = entiteetRepository.meetodiNimi();
    return mapperMuutuja.toDtoKlassiNimid(entiteedid);
}
```

> **IntelliJ vihje:** Meetodi tagastustüüp on praegu `void`, aga meetodis on `return` lause.
> Vajuta punase joone peal **Alt+Enter**, siis parandab IntelliJ tagastustüübi automaatselt.

---

## Samm 5 — Repository (järjestatud päring)

### Mida teha?

Taskifaili järgi peab eesti keel olema alati esimene. Eesti keel on süsteemi põhikeel, st `language` tabeli veerus `is_main_language` on tal väärtus `true`. Ülejäänud keeled tulevad `id` järgi kasvavas järjekorras. Selleks tee repositooriumi uus meetod, mis tagastab kõik keeled selles järjekorras.

> **Mõtle enne JPA Buddy avamist:** Kui sorteerid boolean-veeru järgi, siis kumb väärtus tuleb vaikimisi (kasvavas järjekorras) ette: `false` või `true`? Kumba suunda sul siis vaja on?
> Ja mis peaks olema **teine** sorteerimise tingimus?

### Uue meetodi loomine JPA Buddy abil

Mine repository interface'i faili. Kasuta **JPA Buddy** funktsionaalsust:

1. Ava JPA Buddy paneel (paremklõps repository klassis → JPA Buddy)
2. Valikutes **Method** ja **Query** vali **Query**
3. Vali meetodi tüüp:
    - **Find instance** üksiku rea leidmiseks
    - **Find collection** mitme rea leidmiseks
    - **Count** loendamiseks
    - **Exists** olemasolu kontrollimiseks
4. Määra **Wrap type**:
    - Üksiku rea puhul kaalu `Optional<EntiteetKlass>`
    - Mitme rea puhul vali `List<EntiteetKlass>`
5. Lisa **query conditionid**, st milliseid veerge filtreeritakse. Mõtle, kas selles taskis on üldse vaja filtreerida.
6. **Advanced** sektsioonis vali alati **Named parameters**
7. **Order By Attributes**: siin on selle taski kõige olulisem osa. Lisa sorteerimise tingimused õiges järjekorras ja õige suunaga.

Peale meetodi loomist:
- Vaata meetodi nimi üle. Nimi peab ütlema, mida meetod tagastab (vt `backend/CLAUDE.md` jaotist "Repositooriumi meetodi nimetamine"). JPA Buddy pakutud pikk automaatne nimi asenda lühema ja selgemaga.
- Kui meetodil on parameetreid, kontrolli nende nimed ja tee vastav muudatus ka `@Query` annotatsioonis.
- Eemalda ebavajalikud `@Param()` annotatsioonid.

Kasuta uut meetodit service meetodis.

---

## Samm 6 — tagasi RestController'isse

### Mida teha?

Service meetod on nüüd valmis ja tagastab DTO-de listi. Mine tagasi kontrolleri meetodisse.

Täienda kontrolleri meetodit, lisa `return` lause:

```java
public void meetodiNimi() {
    teenuseMuutuja.meetodiNimi();  // <- enne: tulemus kasutamata
}
```

> **IntelliJ vihje:** Lisa `return` lause. IntelliJ kurdab, et `void` meetod ei saa midagi tagastada.
> Vajuta **Alt+Enter** ja vali "Change return type".

Tulemus:

```java
public TagastatavTüüp meetodiNimi() {
    return teenuseMuutuja.meetodiNimi();
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

- [ ] Uus RestController klass on õiges alampaketis ja sellel on annotatsioonid `@RestController`, `@RequestMapping`, `@RequiredArgsConstructor`
- [ ] Kontrolleri meetodil on `@GetMapping`, `@Operation` ja `@ApiResponses` annotatsioonid ning `@Operation` summary kirjeldab endpointi täpselt
- [ ] Uus service klass on olemas ja sellel on annotatsioonid `@Service`, `@RequiredArgsConstructor`
- [ ] Repository interface laiendab `JpaRepository`-t ja uuel meetodil on `@Query` annotatsioon
- [ ] Repository meetodi nimi ütleb, mida see tagastab
- [ ] Päringu järjestus: eesti keel (põhikeel) on esimene, ülejäänud tulevad `id` järgi
- [ ] Kasutatakse olemasolevat mapperit ja DTO-d, midagi pole topelt loodud
- [ ] Kõik `@Mapping` annotatsioonid on täidetud ja ükski väli ei ole kaardistamata
- [ ] Meetodite järjekord: `public` enne, `private` pärast, lisaks väljakutsumise hierarhia järgi
- [ ] Kood kompileerub ja endpoint on Swagger UI-s nähtav

---

> **Järgmine samm:** Testi endpointi Swagger UI kaudu (`http://localhost:8080/swagger-ui/index.html`)
> ja kontrolli, et vastus vastab taskifaili näidisandmetele: `et / Eesti` on esimene, `en / English` teine.
