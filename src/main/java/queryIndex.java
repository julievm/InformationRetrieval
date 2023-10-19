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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class queryIndex {
    // the location of the search index
    private static String INDEX_DIRECTORY = "../index";

    // Limit the number of search results we get
    private static int MAX_RESULTS = 10;

    public static void main(String[] args) throws IOException, ParseException {
        // Open the folder that contains our search index
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        File file = new File("src/main/cran/cran.qry");

        Analyzer analyzer = new StandardAnalyzer();

        // create objects to read and search across the index
        DirectoryReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);

        QueryParser parser = new QueryParser("content", analyzer);

        BufferedReader br = new BufferedReader(new FileReader(file));

        String nextLine = br.readLine();

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

                    ScoreDoc[] hits = isearcher.search(query, MAX_RESULTS).scoreDocs;
                    // Print the results
                    System.out.println("Documents: " + hits.length);
                    for (int i = 0; i < hits.length; i++)
                    {
                        Document hitDoc = isearcher.doc(hits[i].doc);
                        System.out.println(i + ") " + hitDoc.get("title") + " " + hits[i].score);
                    }
                }
            }
        }
        // close everything and quit
        ireader.close();
        directory.close();
    }
}
