Modalità:
- [x] durata relativa: fra due secondi, in mezz'ora, settimana scorsa, quest'anno (?)
- [x] giorno/i relativo: giovedì prossimo, fra due lunedì
- [x] mese relativo: settembre scorso, ad ottobre (non due settembri fa)
- [x] giorno speciale: domani, l'altro ieri, ieri l'altro, dopo domani pomeriggio, oggi
- [x] ora: alle 18, le sedici in punto, alle tre (solo num interi 0<=.<=24)
- [x] ora speciale: mezzanotte, mezzogiorno
- [x] momento giornata: mattino, di pomeriggio, alla sera, questa notte (anche "prossima notte"?)
- [x] minuto: cinquantanove, e diciotto minuti
- [x] minuto speciale: e mezza, un quarto
- [x] secondo
- [x] p.m. a.m.
- [x] giorno settimana: sabato
- [ ] giorno: 6, il sette (solo num interi 0<=.<=31)
- [x] nome mese: gennaio, a novembre ...
- [ ] mese: 12
- [ ] anno: millenovecento, duemilasedici
- [x] bc: avanti cristo, dopo cristo, ...

GIORNO? MESE|(mese prossimo|scorso)|(prossimo|scorso mese)? ANNO|(anno prossimo|scorso)|(prossimo|scorso anno)? <ora>? -- nel millenovecentosettantaquattro, il due maggio, a giugno 2021, a novembre dell'anno prossimo


ora: alle|all? ORE :? MINUTI? (in punto)? (sera|mattina|am|pm)? (GIORNO_DELLA_SETTIMANA prossimo|scorso?)|(dopo? domani)|(altro? ieri)|(ieri altro) sera|mattina? -- alle diciassette e quarantacinque, alle sei di sera, 6:30 di domani mattina
ora: mezzanotte|mezzogiorno|... --



Componenti:
- anno -> nel 1976, fra due anni, anno prossimo
- mese -> ad ottobre, in tre mesi, mese scorso
- settimana -> settimana prossima, due settimane fa
- giorno -> mercoledì, fra due giorni, ieri
- momento della giornata -> di mattina, al pomeriggio, sera, notte
- ora -> alle 18, a mezzogiorno, alle tre in punto, fra due ore
- minuto -> :45 (dopo ora), e ventisei (dopo ora), due minuti fa, un quarto d'ora prima di
- secondo -> in un paio di secondi, :19 (dopo minuto), e quattordici (dopo minuto)
