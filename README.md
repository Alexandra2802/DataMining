# DataMining

Pași pentru rularea codului:
1. Din clasa IndexCreator se rulează pentru a putea fi creat indexul, care folosește fișierele din folderul wikidata. Procel a durat în cazul nostru, aproximativ 3 ore.
2. Indexul se va crea într-un folderul numit ”lucene-index”.
3. Apoi, din clasa Main, se rulează funcția main() pentru a căuta rezultatele în documentele generate de index, la întrebările din fișierul questions.txt, atașat proiectului.
4. Rezultatele obținute se pot observa în fișierele Rezultate
