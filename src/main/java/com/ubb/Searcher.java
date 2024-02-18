package com.ubb;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Objects;

public class Searcher {
    public Searcher() {
    }

    public void search(){
        // Specify the path to your Jeopardy questions file
        String filePath = "questions.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Each question has 4 lines (CATEGORY, CLUE, ANSWER, NEWLINE)
                String category = line.trim();
                String clue = br.readLine().trim();
                String answer = br.readLine().trim();
                br.readLine(); // Skip the newline

                //remove stop words from clue
                IndexCreator indexCreator = new IndexCreator();
                String clueWithoutStopWords = indexCreator.removeStopWords(clue);

                // Process the extracted information (you can replace this with your logic)
//                System.out.println("Category: " + category);
//                System.out.println("Clue: " + clueWithoutStopWords);
//                System.out.println("Answer: " + answer);
//                System.out.println();

                //pentru fiecare intrebare construieste query-ul concatenand categoria si intrebarea
                Analyzer analyzer = new StandardAnalyzer(); //trebuie acelasi analyzer ca cel cu care s-a creat indexul


//                String[] fields = {"title", "content"};
//                MultiFieldQueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
//                Query q = queryParser.parse(clueWithoutStopWords);

                Query q = new QueryParser("content", analyzer).parse(category+" "+clueWithoutStopWords);


                int hitsPerPage = 20;
                String projectPath = System.getProperty("user.dir");
                String indexPath = projectPath + "/lucene-index";
                Directory directory = FSDirectory.open(Paths.get(indexPath));
                IndexReader reader = DirectoryReader.open(directory);
                IndexSearcher searcher = new IndexSearcher(reader);
                TopDocs docs = searcher.search(q, hitsPerPage);
                ScoreDoc[] hits = docs.scoreDocs;

                System.out.println("Correct answer: "+answer);
                for(int i=0;i<hits.length;i+=2) {
                    int docId = hits[i].doc;
                    Document d = searcher.doc(docId);
//                    if(Objects.equals(d.get("title"), answer) && i==0 )
//                    if(Objects.equals(d.get("title"), answer))
//                        System.out.println((i + 1)/2+1 + ". " + d.get("title") + "\t" );
                    System.out.println((i + 1)/2+1 + ". " + d.get("title") + "\t" );
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
