---
name: skill-jarjesta-meetodid
description: Küsi kasutajalt faili path ja järjesta selle faili meetodid ümber vastavalt nende väljakutsumise hierarhiale. Kasuta, kui kasutaja tahab meetodeid järjestada, ütleb "järjesta meetodid", "korrasta meetodite järjekord" või "sorteeri meetodid väljakutsumise järgi".
---

Küsi kasutajalt, millise faili meetodeid soovitakse järjestada, kui kasutaja ei ole faili path'i juba andnud. Kui kasutaja ei ole konkreetset faili maininud, aga eelnevast vestlusest on selge, et jutt käib mingi kindla klassi/failiga seotud tegevusest, ja sul on põhjendatud oletus, millist faili silmas peetakse, siis ära eelda seda vaikimisi — küsi kasutajalt kinnitust (nt "Kas mõtled faili `X`?"), millele kasutaja saab vastata jah või anda ise õige faili path.

Loe fail üle ja tuvasta kõik selle klassi/faili meetodid ning nendevahelised väljakutsumisseosed (milline meetod kutsub välja millist).

Järjesta meetodid ümber vastavalt väljakutsumise hierarhiale: kõrgema taseme meetod (nt avalik API meetod, mis teisi meetodeid välja kutsub) peab failis paiknema enne meetodeid, mida ta välja kutsub. Kui üks meetod kutsub mitut teist meetodit, järjesta need omakorda samas põhimõttes nende väljakutsumisjärjekorra alusel. Meetodid, mida keegi failisiseselt välja ei kutsu (nt konstruktorid, override'itud liidesemeetodid, avalikud sisenemispunktid), jäävad kõrgemasse ossa.

**Java/backend erireegel:** kui järjestatav fail on Java backend fail (nt Service, Controller klass), paiguta esmalt kõik `public` meetodid klassi algusesse, nende omavahelises väljakutsumis-/deklareerimisjärjekorras. Alles seejärel järgnevad kõik `private` (ja muud mitte-public) meetodid, grupeerituna oma "omaniku" `public` meetodi järgi (st iga `public` meetodi abimeetodid koos, väljakutsumishierarhia järgi selle grupi sees), samas järjekorras nagu vastavad `public` meetodid ülal paiknevad.

Kui mõnda meetodit kutsuvad välja mitu erinevat ülemmeetodit (jagatud ehk "shared" abimeetodid), paiguta need kõige faili lõppu, pärast kõiki meetodeid, millel on ainult üks väljakutsuja.

Säilita iga meetodi sisu, kommentaarid ja annotatsioonid muutumatuna — muutub ainult meetodite järjekord failis.

Kui väljakutsumisahelas esineb tsükleid (meetodid kutsuvad teineteist vastastikku), säilita nende omavaheline suhteline järjekord ja lisa lühike kommentaar ainult siis, kui järjekord jääb muidu segaseks.

Pärast ümberjärjestamist näita kasutajale, mis muutus (meetodite uus järjekord), ja veendu, et fail endiselt kompileerub/on süntaktiliselt korrektne.

Suhtle kasutajaga eesti keeles.
