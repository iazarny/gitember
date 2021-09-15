package com.az.gitember.service;

import com.az.gitember.data.ScmItemDocument;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class SearchService implements  AutoCloseable {

    private final String indexStorageFolder;

    Directory index;
    StandardAnalyzer analyzer;
    IndexWriterConfig indexWriterConfig;
    IndexWriter writter;
    IndexReader reader;
    IndexSearcher searcher;

    public SearchService(String projectFolder)  {
        this.indexStorageFolder = getIndexStorageFolder(projectFolder);
        this.analyzer = new StandardAnalyzer();
        this.indexWriterConfig = new IndexWriterConfig(analyzer);

        try {
            this.index = new NIOFSDirectory(Path.of(this.indexStorageFolder));
            this.writter = new IndexWriter(index, indexWriterConfig);
            this.reader = DirectoryReader.open(index);
            this.searcher = new IndexSearcher(reader);

        } catch (IOException e) {
            this.index = null;
            this.writter = null;
        }

    }


    public Map<String, Set<String>> search(String searchTerm) {
        Map<String, Set<String>> rez = new HashMap<>();
        try {
            Query query = new QueryParser("body", analyzer).parse(searchTerm);

            TopDocs docs = searcher.search(query, 1024);
            for (ScoreDoc scireDoc : docs.scoreDocs) {
                Document doc = searcher.doc(scireDoc.doc);
                Set<String> items = rez.computeIfAbsent(doc.get("revision") , s-> new HashSet<>());
                items.add(doc.get("name"));
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return rez;

    }

    public void submitItemToReindex(ScmItemDocument scmItemDocument)  {
        Document document = new Document();
        document.add(new org.apache.lucene.document.TextField("revision", scmItemDocument.getRevision(), Field.Store.YES));
        document.add(new org.apache.lucene.document.TextField("name", scmItemDocument.getName(), Field.Store.YES));
        document.add(new org.apache.lucene.document.TextField("body", scmItemDocument.getBody(), Field.Store.YES));
        if (writter != null) {
            try {
                writter.addDocument(document);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    private static String getIndexStorageFolder(String projectFolder) {
        return System.getProperty("java.io.tmpdir") + File.separator
                + "gitemberidx" + File.separator + CipherService.crypt(projectFolder,"");
    }


    @Override
    public void close() throws Exception {
        if (this.writter != null) {
            writter.close();
        }
    }
}
