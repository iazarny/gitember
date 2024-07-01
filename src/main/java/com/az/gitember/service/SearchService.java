package com.az.gitember.service;

import com.az.gitember.data.ScmItemDocument;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SearchService implements AutoCloseable {

    private final String indexStorageFolder;

    Directory index;
    StandardAnalyzer analyzer;
    IndexWriterConfig indexWriterConfig;
    IndexWriter writter = null;
    IndexReader reader =  null;
    IndexSearcher searcher=  null;

    public SearchService(String projectFolder)  {
        this.indexStorageFolder = getIndexStorageFolder(projectFolder);
        this.analyzer = new StandardAnalyzer();
        this.indexWriterConfig = new IndexWriterConfig(analyzer);

        try {
            this.index = new NIOFSDirectory(Path.of(this.indexStorageFolder));
        } catch (IOException e) {
            e.printStackTrace();
            this.index = null;
        }

    }

    public void dropIndex() {
        try {
            FileUtils.deleteDirectory(new File(this.indexStorageFolder));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Map<String, Set<String>> search(String searchTerm) {
        Map<String, Set<String>> rez = new HashMap<>();
        try {
            Query query = new QueryParser("body", analyzer).parse(searchTerm);


            //Term term = new Term("body", searchTerm);
            //Query query = new PrefixQuery(term);

            //Term term = new Term("body", searchTerm);
            //Query query = new FuzzyQuery(term);

            TopDocs docs = getSearcher().search(query, 1024);
            for (ScoreDoc scireDoc : docs.scoreDocs) {
                Document doc = getSearcher().doc(scireDoc.doc);
                Set<String> items = rez.computeIfAbsent(doc.get("revision") , s-> new HashSet<>());
                items.add(doc.get("name"));
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return rez;

    }

    public synchronized IndexSearcher getSearcher() throws IOException {
        if (this.searcher == null) {
            this.searcher = new IndexSearcher(getReader());
        }
        return searcher;
    }

    public synchronized IndexReader getReader() throws IOException  {
        if(this.reader == null) {
            this.reader = DirectoryReader.open(index);
        }
        return reader;
    }

    public void submitItemToReindex(ScmItemDocument scmItemDocument)  {
        Document document = new Document();
        document.add(new org.apache.lucene.document.TextField("revision", scmItemDocument.getRevision(), Field.Store.YES));
        document.add(new org.apache.lucene.document.TextField("name", scmItemDocument.getName(), Field.Store.YES));
        document.add(new org.apache.lucene.document.TextField("body", scmItemDocument.getBody(), Field.Store.YES));
        try {
            getWritter().addDocument(document);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public synchronized IndexWriter getWritter() throws IOException {
        if (this.writter == null) {
            this.writter = new IndexWriter(index, indexWriterConfig);
        }
        return this.writter;
    }



    private static String getIndexStorageFolder(String projectFolder) {
        return System.getProperty("java.io.tmpdir") + File.separator
                + "gitemberidx" + File.separator + CipherService.crypt(projectFolder,"a");
    }

    @Override
    public void close()  {
        try {
            if (this.writter != null) {
                writter.close();
            }

            if (this.reader != null) {
                reader.close();
            }
            index.close();

        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
