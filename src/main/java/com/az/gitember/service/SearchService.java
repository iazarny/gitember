package com.az.gitember.service;

import com.az.gitember.data.ScmItemDocument;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiBits;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Bits;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SearchService implements AutoCloseable {

    /** Default index-folder prefix for the per-project history index. */
    public static final String HISTORY_INDEX_PREFIX = "luceneidx-";

    /** Field names used by the working-copy file documents (see {@link #updateFileDoc}). */
    private static final String FIELD_PATH = "path";
    private static final String FIELD_MTIME = "mtime";
    private static final String FIELD_BODY = "body";

    private final String indexStorageFolder;

    Directory index;
    StandardAnalyzer analyzer;
    IndexWriterConfig indexWriterConfig;
    IndexWriter writter = null;
    IndexReader reader =  null;
    IndexSearcher searcher=  null;

    public SearchService(String projectFolder)  {
        this(projectFolder, HISTORY_INDEX_PREFIX);
    }

    /**
     * @param projectFolder repository whose content is indexed; folded into the index location
     * @param indexPrefix   folder-name prefix that keeps unrelated indexes (e.g. history vs.
     *                      workspace working-copy) apart under the shared index root
     */
    public SearchService(String projectFolder, String indexPrefix)  {
        this.indexStorageFolder = getIndexStorageFolder(projectFolder, indexPrefix);
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
            this.writter = new IndexWriter(index, new IndexWriterConfig(analyzer));
        }
        return this.writter;
    }

    /**
     * Opens the writer so that a subsequent {@link #commitIndex()} materializes an (possibly empty)
     * index on disk. Lets a project with no indexable content still count as "indexed", avoiding a
     * needless full reindex on every open.
     */
    public void ensureCreated() {
        try {
            getWritter();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Flushes and closes the writer, resets reader/searcher so searches see new data. */
    public synchronized void commitIndex() throws IOException {
        if (writter != null) {
            writter.commit();
            writter.close();
            writter = null;
        }
        if (reader != null) {
            reader.close();
            reader = null;
        }
        searcher = null;
    }

    public boolean hasIndex() {
        try {
            return DirectoryReader.indexExists(index);
        } catch (IOException e) {
            return false;
        }
    }

    // ── Working-copy file documents ────────────────────────────────────────────
    //
    // Unlike the history documents (keyed by commit revision), these represent the
    // *current* on-disk version of a file. Each is keyed by its repo-relative path so
    // it can be updated in place on reindex, and carries the file's last-modified time
    // so changed files can be detected without re-reading unchanged content.

    /**
     * Adds or replaces the indexed content of a single working-copy file. Uses the path as the
     * document key, so re-indexing a changed file overwrites the previous version.
     */
    public void updateFileDoc(String path, String body, long mtime) {
        Document document = new Document();
        document.add(new StringField(FIELD_PATH, path, Field.Store.YES));
        document.add(new StoredField(FIELD_MTIME, mtime));
        document.add(new TextField(FIELD_BODY, body, Field.Store.NO));
        try {
            getWritter().updateDocument(new Term(FIELD_PATH, path), document);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Removes a working-copy file document (used when the file no longer exists on disk). */
    public void deleteFileDoc(String path) {
        try {
            getWritter().deleteDocuments(new Term(FIELD_PATH, path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the path→last-modified map of every currently indexed working-copy file, so an
     * incremental reindex can tell which files are new, changed or gone. Empty when no index exists.
     */
    public Map<String, Long> indexedFileMtimes() {
        Map<String, Long> rez = new HashMap<>();
        if (!hasIndex()) {
            return rez;
        }
        try {
            IndexReader r = getReader();
            Bits liveDocs = MultiBits.getLiveDocs(r);
            for (int i = 0; i < r.maxDoc(); i++) {
                if (liveDocs != null && !liveDocs.get(i)) {
                    continue;
                }
                Document doc = r.document(i);
                String path = doc.get(FIELD_PATH);
                org.apache.lucene.index.IndexableField mtimeField = doc.getField(FIELD_MTIME);
                if (path != null && mtimeField != null && mtimeField.numericValue() != null) {
                    rez.put(path, mtimeField.numericValue().longValue());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rez;
    }

    /** Searches working-copy file content and returns the matching repo-relative paths. */
    public Set<String> searchFiles(String searchTerm) {
        Set<String> rez = new HashSet<>();
        try {
            Query query = new QueryParser(FIELD_BODY, analyzer).parse(searchTerm);
            TopDocs docs = getSearcher().search(query, 1024);
            for (ScoreDoc scoreDoc : docs.scoreDocs) {
                Document doc = getSearcher().doc(scoreDoc.doc);
                String path = doc.get(FIELD_PATH);
                if (path != null) {
                    rez.add(path);
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return rez;
    }



    private static String getIndexStorageFolder(String projectFolder, String indexPrefix) {
        return System.getProperty("java.io.tmpdir") + File.separator
                + "gitemberidx" + File.separator + indexPrefix + GitemberUtil.getMd5Hash(projectFolder);
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
