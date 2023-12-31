import java.io.BufferedReader;
import java.io.IOException;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class ParseDocs {

    // Directory where the search index will be saved
    private static String INDEX_DIRECTORY = "../index";

    public static void main(String[] args) throws IOException, ParseException {
        File file = new File("src/main/cran/cran.all.1400");

        // Analyzer analyzer = new EnglishAnalyzer();
        Analyzer analyzer = new StandardAnalyzer();

        ArrayList<Document> documents = new ArrayList<Document>();

        BufferedReader br = new BufferedReader(new FileReader(file));

        // Open the directory that contains the search index
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));

        // Set up an index writer to add process and save documents to the index
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter iwriter = new IndexWriter(directory, config);

        //read in the file, look fro .something and then add to appropriate thing in doc.

        //query from slides (how to read in whole query sentence)
        String nextline = br.readLine();
//        String line = br.readLine();

        while(nextline != null){
            if(nextline.charAt(0) == '.'){
                Document doc = new Document();

                if(nextline.charAt(1) == 'I'){
                    int docId = Integer.parseInt(nextline.substring(3).trim());
                    doc.add(new TextField("docId", Integer.toString(docId), Field.Store.YES));
                    nextline = br.readLine();
                }
                if(nextline.charAt(1) == 'T'){
                    StringBuilder title = new StringBuilder();
                    while((nextline = br.readLine()).charAt(0) != '.'){
                        title.append(nextline);
                    }
                    doc.add(new TextField("title", title.toString(), Field.Store.YES));
                }
                if(nextline.charAt(1) == 'A'){
                    StringBuilder author = new StringBuilder();
                    while((nextline = br.readLine()).charAt(0) != '.'){
                        author.append(nextline);
                    }
                    doc.add(new TextField("author", author.toString(), Field.Store.YES));
                }
                if(nextline.charAt(1) == 'B'){
                    StringBuilder bib = new StringBuilder();
                    while((nextline = br.readLine()).charAt(0) != '.'){
                        bib.append(nextline);
                    }
                    doc.add(new TextField("bib", bib.toString(), Field.Store.YES));
                }
                if(nextline.charAt(1) == 'W'){
                    StringBuilder content = new StringBuilder();
                    while((nextline = br.readLine()) != null && nextline.charAt(0) != '.'){
                        content.append(nextline);
                    }
                    doc.add(new TextField("content", content.toString(), Field.Store.YES));
                }
//                System.out.println(doc);
                documents.add(doc);
            }
        }
        // Write all the documents in the linked list to the search index
        iwriter.addDocuments(documents);

        // Commit everything and close
        iwriter.close();
        directory.close();

        queryIndexing();
    }

    public static void queryIndexing() throws IOException, ParseException {
        // Open the folder that contains our search index
        // the location of the search index
        String INDEX_DIRECTORY = "../index";

        // Limit the number of search results we get
        int MAX_RESULTS = 10;
        Directory directory2 = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        File file = new File("src/main/cran/cran.qry");

        File results = new File("results.txt");

        Analyzer analyzer = new StandardAnalyzer();

        ArrayList<String> strings = new ArrayList<>();

        // create objects to read and search across the index
        DirectoryReader ireader = DirectoryReader.open(directory2);
        IndexSearcher isearcher = new IndexSearcher(ireader);
        isearcher.setSimilarity(new BM25Similarity());

        BufferedWriter writer = new BufferedWriter(new FileWriter(results, true));

        QueryParser parser = new QueryParser("content", analyzer);

        BufferedReader br = new BufferedReader(new FileReader(file));

        String nextLine = br.readLine();
        int queryID = 1;
        //read in the whole file containing queries
        while(nextLine != null){
            if(nextLine.charAt(0) == '.'){
                //we ignore the id of the query
                nextLine = br.readLine();
                if(nextLine.charAt(1) == 'W'){
                    //read in the whole query
                    StringBuilder content = new StringBuilder();
                    while((nextLine = br.readLine()) != null && nextLine.charAt(0) != '.'){
                        content.append(nextLine);
                    }
//                    System.out.println(content.toString());
                    String queryString = content.toString().trim().replace("*", "").replace("?", "");
                    Query query = parser.parse(queryString);

                    ScoreDoc[] hits = isearcher.search(query, 10000).scoreDocs;
                    // Print the results
                    System.out.println("Documents: " + hits.length);
                    for (int i = 0; i < hits.length; i++)
                    {
                        Document hitDoc = isearcher.doc(hits[i].doc);
                        writer.append(queryID + " Q0 " + hitDoc.get("docId") + " " + i + " " + hits[i].score + " STANDARD\n");
                    }
                    queryID++;
                }
            }
        }
        // close everything and quit
        writer.close();
        ireader.close();
        directory2.close();
    }




}
