package com.dl.a_1;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileInputStream;


import org.apache.lucene.analysis.en.EnglishAnalyzer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class create_index {
    private static String INDEX_DIRECTORY = "index";
    private static String CRAN_DOCS = "/cran/cran_docs";
    public static void main(String[] args) throws IOException, ParseException {

        //--------------- SET UP INDEX ----------------------
    
        EnglishAnalyzer analyzer = new EnglishAnalyzer();
        Directory index = FSDirectory.open(Paths.get(INDEX_DIRECTORY));

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter w = new IndexWriter(index, config);

        read_cran_docs(w);


        w.close();

        
       
    }

    private static void addDoc(IndexWriter w, Map <String, String> doc) throws IOException {
        //Enter hashmap as document
        Document d = new Document();
        d.add(new StringField(("id"), doc.get("I"), Field.Store.YES));
       

        try{
            d.add(new StringField(("author"), doc.get("A"), Field.Store.YES));
        } catch(IllegalArgumentException e){
            d.add(new StringField(("author"), "", Field.Store.YES));
        }

        try{
            d.add(new TextField(("bibliography"), doc.get("B"), Field.Store.YES));
        } catch(IllegalArgumentException e){
            d.add(new TextField(("bibliography"), "", Field.Store.YES));
        }

        try{
            d.add(new StringField(("title"), doc.get("T"), Field.Store.YES));
        } catch(IllegalArgumentException e){
            d.add(new StringField(("title"), "", Field.Store.YES));
        }

        try {
            d.add(new TextField(("words"), doc.get("W"), Field.Store.YES));
        } catch (IllegalArgumentException e) {
            d.add(new TextField(("words"), "", Field.Store.YES));
        }
        w.addDocument(d);
        
        
    }

    private static void read_cran_docs(IndexWriter w) throws IOException {
        //Open file
        String cran_direc = System.getProperty("user.dir") + "/"+ CRAN_DOCS;
        FileInputStream fstream = new FileInputStream(cran_direc);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
        String strLine, current_mode, current_entry;
        current_mode = "I";
        Map<String, String> doc = new HashMap<String, String>();

        //Read File Line By Line
        while ((strLine = br.readLine()) != null) {
            // Print the content on the console\=
            String s[];
            s = strLine.split(" ");
            switch(s[0].strip())
            {
                case(".I"):
                    //Save last doc and create a new one
                    if (doc.size() > 0){
                        //Enter last document into index
                        addDoc(w, doc);
                        doc.clear();
                    }
                    current_mode = "I";
                    doc.put("I", s[1].strip());
                    break;
                case(".T"):
                    //Title found
                    current_mode = "T";
                    break;
                case(".A"):
                    //Author found
                    current_mode = "A";
                    break;
                case(".B"):
                    //Bibliography found
                    current_mode = "B";
                    break;
                case(".W"):
                    //Words found
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
            addDoc(w, doc);
            doc.clear();
        }
        //Close the input stream
        br.close();
    }


}

