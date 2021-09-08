package com.az.gitember.service;

import com.az.gitember.data.ScmItem;
import com.az.gitember.data.ScmItemDocument;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class SearchService implements  AutoCloseable {

    private final String indexStorageFolder;

    Directory memoryIndex;
    StandardAnalyzer analyzer;
    IndexWriterConfig indexWriterConfig;
    IndexWriter writter;

    public SearchService(String projectFolder)  {
        this.indexStorageFolder = getIndexStorageFolder(projectFolder);
        this.analyzer = new StandardAnalyzer();
        this.indexWriterConfig = new IndexWriterConfig(analyzer);

        try {
            this.memoryIndex = new NIOFSDirectory(Path.of(this.indexStorageFolder));
            this.writter = new IndexWriter(memoryIndex, indexWriterConfig);
        } catch (IOException e) {
            this.memoryIndex = null;
            this.writter = null;
        }

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
