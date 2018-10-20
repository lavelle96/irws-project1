package com.dl.a_1;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;


import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;

import org.apache.lucene.index.DirectoryReader;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.BM25Similarity;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class run_queries {
    private static String INDEX_DIRECTORY = "index";
    private static String TREC_FILE = "/cran/results";
    private static String CRAN_QUERIES = "/cran/cran.qry";
    public static void main(String[] args) throws IOException, ParseException {

        //--------------- SET UP INDEX ----------------------
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
		
		// create objects to read and search across the index
		DirectoryReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);
       
        isearcher.setSimilarity(new BM25Similarity());




        EnglishAnalyzer analyzer = new EnglishAnalyzer();

        QueryParser parser = new QueryParser("words", analyzer);
        
        //---------------- HANDLE QUERY -------------------------
        //Clear results file
        PrintWriter pw = new PrintWriter(System.getProperty("user.dir") + "/"+ TREC_FILE);
        pw.close();
        
        

        String query_path = System.getProperty("user.dir") + "/"+ CRAN_QUERIES;
        FileInputStream fstream = new FileInputStream(query_path);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
        String strLine, current_entry;
        String current_mode = "I";
        Map<String, String> doc = new HashMap<>();        
        

        int zero_query_counter = 0;
        int counter = 0;
        while ((strLine = br.readLine()) != null) {
            String s[];
            s = strLine.split(" ");
            switch(s[0].strip())
            {
                case(".I"):
                    if (doc.size() > 0){

                        //---- PERFORM QUERY AND ENTER RESULTS - -------- - - -- --- - -- --- ------ - -- -- - -- - -  - - - - --  - -- -- -  -
                        counter++;
                        zero_query_counter += process_query(counter, doc, isearcher, parser) ? 0 : 1;

                        doc.clear();
                    }
                    current_mode = "I";
                    doc.put("I", s[1].strip());
                    break;
                
                case(".W"):
                    current_mode = "W";
                    break;
                default:
                    if ((current_entry = doc.get(current_mode)) != null){
                        current_entry += ("\n" + strLine);
                    }
                    else{
                        if(strLine != null){
                            current_entry = strLine;
                        }
                    }
                    doc.put(current_mode, current_entry);
                    break;
            }
            
        }
        if (doc.size() > 0){
            //Enter last document into index
            //Make last query;
            zero_query_counter += process_query(counter+ 1, doc, isearcher, parser)? 0 : 1;
            doc.clear();

        }
        System.out.print("Num queries processed: " + (counter + 1) + "\nNum zero queries: " + zero_query_counter);
            
        
        //Close the input stream
        br.close();
        
        ireader.close();
        
    }


    private static Boolean process_query(int query_num, Map<String, String> doc, IndexSearcher isearcher, QueryParser parser) throws IOException, ParseException{
        FileWriter fw = new FileWriter(System.getProperty("user.dir") + "/"+ TREC_FILE, true);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter pw = new PrintWriter(bw);

        String querystr = doc.get("W");
        querystr = QueryParserBase.escape(querystr).trim();
        Query query = parser.parse(querystr);
        
        
        ScoreDoc[] hits = isearcher.search(query, 1400).scoreDocs;  
    
        int numTotalHits = hits.length;
        
        int end = Math.min(numTotalHits, 1400);
        if (end == 0){
            System.out.print("Zero hit found");
            pw.close();
            return false;
        }
        for (int i = 0; i < end; i++) {
            Document d = isearcher.doc(hits[i].doc);
            String docno = d.get("id");
            

            write_to_eval(pw, query_num + " Q0 "+docno+" "+( i + 1) +" "+hits[i].score+" "+"0");
        }
        pw.close();
        return true;
        

    }

    public static void write_to_eval(PrintWriter pw, String string_to_add) throws IOException{
       
        pw.println(string_to_add);
    }
}

