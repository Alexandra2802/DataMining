package com.ubb;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;

public class IndexCreator {
    public IndexCreator() {
    }

    static int nrDocs = 0;
    public void createIndex() throws IOException {

        // Get the project directory as the base path
        String projectPath = System.getProperty("user.dir");

        // Specify a subdirectory for the Lucene index
        String indexPath = projectPath + "/lucene-index";

        // Create a directory for the index using FSDirectory
        Directory directory = FSDirectory.open(Paths.get(indexPath));

        // Create an analyzer
        Analyzer analyzer = new StandardAnalyzer();

        // Create an index writer configuration with the chosen analyzer
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        // Create an index writer with the configuration
        IndexWriter indexWriter = new IndexWriter(directory, config);

        // Calea către directorul "wikidata"
        String caleDirectorWikidata = "wikidata";

        try {
            // Parcurge toate fișierele din directorul specificat
            Files.walkFileTree(Paths.get(caleDirectorWikidata), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // Verifică dacă fișierul are extensia .txt
                    if (file.toString().toLowerCase().endsWith(".txt")) {
                        // Procesează fișierul
                        parsareFisier(file, indexWriter);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            indexWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Închide IndexWriter la final
            indexWriter.close();
        }
    }
    public String removeStopWords(String text) throws IOException {

        CharArraySet stopWords = StopAnalyzer.ENGLISH_STOP_WORDS_SET;
        StandardTokenizer tokenizer = new StandardTokenizer();
        tokenizer.setReader(new StringReader(text));

        TokenStream tokenStream = new StopFilter(tokenizer, stopWords);
        StringBuilder sb = new StringBuilder();
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);

        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            String term = charTermAttribute.toString();
            sb.append(term).append(" ");
        }

        return sb.toString().trim();
    }
    private String lemmatizeText(String text) {
        // set up pipeline properties
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,pos,lemma");
        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // create a document object
        CoreDocument document = pipeline.processToCoreDocument(text);
        // display tokens
        StringBuilder lemmatizedText = new StringBuilder();
        for (CoreLabel tok : document.tokens()) {
            lemmatizedText.append(tok.lemma());
            //System.out.println(String.format("%s\t%s", tok.word(), tok.lemma()));
        }
        return lemmatizedText.toString();
    }
    private void parsareFisier(Path filePath, IndexWriter indexWriter) {
        // Folosește BufferedReader pentru a citi conținutul fiecărui fișier linie cu linie
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            // Variabile pentru a reține titlul și conținutul paginii
            String titlu = null;
            StringBuilder continutPagina = new StringBuilder();

            String linie;
            while ((linie = reader.readLine()) != null) {
                if (linie.startsWith("[[") && linie.endsWith("]]")) {
                    // Verifică dacă suntem într-o linie cu [[titlu]]
                    if (titlu != null) {
                        // Dacă suntem deja într-un titlu anterior, adaugă documentul
                        addDoc(indexWriter, titlu, lemmatizeText(removeStopWords(continutPagina.toString())));
                        nrDocs++;
                        System.out.println(nrDocs);

                        // Resetăm variabilele pentru a procesa noul titlu
                        titlu = null;
                        continutPagina.setLength(0);
                    }

                    // Extrage conținutul dintre paranteze pentru noul titlu
                    int indexStart = 2; // [[ are lungimea 2
                    int indexEnd = linie.length() - 2; // ]] are lungimea 2
                    titlu = linie.substring(indexStart, indexEnd);
                } else {
                    // Adaugă linia în variabila continutPagina
                    continutPagina.append(linie).append("\n");
                }
            }

            // Verificare finală și adăugare document
            if (titlu != null) {
                addDoc(indexWriter, titlu, lemmatizeText(removeStopWords(continutPagina.toString())));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addDoc(IndexWriter w, String title, String content) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("title", title, Field.Store.YES));
        doc.add(new TextField("content", content, Field.Store.YES));
        w.addDocument(doc);
    }
}
