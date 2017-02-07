package com.nuvolect.deepdive.lucene;

import com.nuvolect.deepdive.util.CConst;
import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.OmniFile;
import com.nuvolect.deepdive.util.OmniUtil;

import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.JSONException;
import org.json.JSONObject;
import org.lukhnos.portmobile.file.Paths;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;


/**
 * Index a Lucene database given an path.
 * index_state:{nil, filetree, indexing, complete, interrupted}
 * filetree : gen recursive list of files, { total_docs == 0, indexed_docs==0}
 * indexing : index files, populate { total_docs, indexed_docs }
 * complete : index is complete, are the same { total_docs, indexed_do
 * interrupted: index process was interrupted and is not complete
 */
public class Index {

    public static enum INDEX_STATE {nil, filetree, indexing, interrupted, complete};
    private static INDEX_STATE m_index_state = INDEX_STATE.nil;

    private static final Object m_lock = new Object();
    private static Thread m_indexThread = null;
    private static ThreadGroup m_threadGroup = null;
    private static String INDEX_THREAD_GROUP = "Index thread group";
    private static String INDEX_THREAD = "Index thread";
    private static int STACK_SIZE = 20 * 1024 * 1024;
    private static boolean m_fileTreeActive = false;
    private static final boolean[] m_interrupt = {false};
    private static final int[] m_totalDocs = {0};
    private static final int[] m_indexedDocs = {0};
    private static final String[] m_error = {""};

    public static JSONObject index(final String volumeId, final String searchPath, final boolean forceIndex) {

        if( m_interrupt[0]){

            LogUtil.log(Index.class, "Index canceled post interrupt");

            m_interrupt[0] = false;
            return responseInterruptIndexing();
        }

        OmniFile cacheDir = IndexUtil.getCacheDir( volumeId, searchPath);
        boolean cacheDirCreated = false;
        try {
            cacheDirCreated = OmniUtil.forceMkdir(cacheDir);
        } catch (IOException e) {
            return responseFolderCreateError( searchPath);
        }

        final String luceneDirPath = cacheDir.getAbsolutePath();

        boolean cacheDirExists = ! cacheDirCreated;
        boolean indexingOngoing = m_indexThread != null && m_indexThread.isAlive();
        boolean indexingRequired = ! cacheDirExists || forceIndex;

        synchronized (m_lock) {

            if( indexingOngoing){

                if( m_fileTreeActive)
                    m_index_state = INDEX_STATE.filetree;
                else
                    m_index_state = INDEX_STATE.indexing;
            }
            else{
                if(indexingRequired)
                    m_index_state = INDEX_STATE.indexing;
                else
                    m_index_state = INDEX_STATE.complete;
            }
        }

        if(indexingRequired || indexingOngoing) {

            if( indexingOngoing) {

                // Nothing to do, let the background process run. Monitor m_indexedDocs for progress.
            }
            else{

                synchronized (m_lock) {
                    m_index_state = INDEX_STATE.filetree;
                    m_totalDocs[0] = 0;
                    m_indexedDocs[0] = 0;
                    m_error[0] = "";
                }
                m_threadGroup = new ThreadGroup(INDEX_THREAD_GROUP);
                m_indexThread = new Thread(m_threadGroup, new Runnable() {
                    @Override
                    public void run() {

//                        Analyzer analyzer = new org.apache.lucene.analysis.core.WhitespaceAnalyzer();//standard.StandardAnalyzer();
                        Analyzer analyzer = new org.apache.lucene.analysis.core.SimpleAnalyzer();//WhitespaceAnalyzer();//standard.StandardAnalyzer();
                        IndexWriterConfig config = new IndexWriterConfig(analyzer);
                        IndexWriter iwriter = null;

                        try {
                            Directory m_directory = FSDirectory.open( Paths.get( luceneDirPath));
                            iwriter = new IndexWriter( m_directory, config);
                            iwriter.deleteAll();
                            iwriter.commit();
                        } catch (IOException e) {
                            LogUtil.logException( Index.class, e);
                            m_error[0] = "IndexWriter constructor exception";
                        }

                        synchronized (m_lock) {
                            m_fileTreeActive = true;
                            m_index_state = INDEX_STATE.filetree;
                        }
                        Collection<OmniFile> files = IndexUtil.getFilePaths( volumeId, searchPath);

                        synchronized (m_lock){
                            m_index_state = INDEX_STATE.indexing;
                            m_fileTreeActive = false;
                            m_totalDocs[0] = files.size();
                            m_indexedDocs[0] = 0;
                        }

                        try {

                            for (OmniFile file : files) {

                                if ( m_interrupt[0]) {
                                    LogUtil.log(Index.class, "Iterator loop canceled");
                                    break;
                                }

                                String path = file.getPath();

                                LogUtil.log(Index.class, "indexing: " + path);
                                iwriter.addDocument(makeDoc( volumeId, path));
                                synchronized (m_lock) {
                                    ++m_indexedDocs[0];
                                }
                            }

                            iwriter.commit();
                            iwriter.close();
                            synchronized (m_lock){
                                m_index_state = m_interrupt[0]?INDEX_STATE.interrupted :INDEX_STATE.complete;
                                m_totalDocs[0] = m_indexedDocs[0];
                            }

                        } catch (Exception e) {
                            LogUtil.logException( Index.class, e);
                            m_error[0] = "IndexWriter addDocument exception";
                        }
                    }
                }, INDEX_THREAD, STACK_SIZE);

                m_indexThread.setPriority(Thread.MAX_PRIORITY);
                m_indexThread.start();
            }
        }else{

            // Indexing is complete
            // Get number of documents indexed
            try {
                Directory directory = FSDirectory.open( Paths.get( luceneDirPath));
                DirectoryReader ireader = DirectoryReader.open( directory);
                synchronized (m_lock) {
                    m_indexedDocs[0] = ireader.numDocs();
                    m_totalDocs[0] = m_indexedDocs[0];
                    m_index_state = INDEX_STATE.complete;
                }
                ireader.close();
                directory.close();
            } catch (IOException e) {
                LogUtil.logException( Index.class, e);
            }
        }

        JSONObject result =new JSONObject();
        try {
            synchronized (m_lock) {
                result.put("index_state", m_index_state.toString());
                result.put("error", m_error[0]);
                result.put("indexed_docs", m_indexedDocs[0]);
                result.put("total_docs", m_totalDocs[0]);
//                result.put("full_path", cacheDir.getAbsolutePath());
                result.put("search_path", searchPath);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static JSONObject interrupt(){

        synchronized (m_lock) {
            m_interrupt[0] = true;
        }
        LogUtil.log( Index.class, "interrupting---------------================================================");
        return responseInterruptIndexing();
    }

    private static JSONObject responseInterruptIndexing() {

        JSONObject result =new JSONObject();
        try {
            result.put("index_state", INDEX_STATE.interrupted.toString());
            result.put("error", "");
            result.put("indexed_docs", m_indexedDocs[0]);
            result.put("total_docs", m_totalDocs[0]);
            result.put("search_path", "");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static JSONObject responseFolderCreateError(String searchPath) {

        JSONObject result =new JSONObject();
        try {
                result.put("index_state", INDEX_STATE.complete.toString());
                result.put("error", "Folder error: "+searchPath);
                result.put("indexed_docs", 0);
                result.put("total_docs", 0);
                result.put("search_path", searchPath);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Build a single document to be indexed with additional data to be returned with search results.
     * @param volumeId // Volume of the file
     * @param path // Path to the file
     * @return
     * @throws IllegalArgumentException
     * @throws FileNotFoundException
     */
    private static Document makeDoc( String volumeId, String path) throws IllegalArgumentException, FileNotFoundException {

        Document doc = new Document();

        String fileName = FilenameUtils.getName(path);
        // Tokenize, index and store
        doc.add(new TextField(CConst.FIELD_FILENAME, fileName, Field.Store.YES));

        // Only stored, not indexed
        doc.add( new StoredField(CConst.FIELD_VOLUME, volumeId));

        // Only stored, not indexed
        doc.add( new StoredField(CConst.FIELD_PATH, path));

        // Index only, do not store
        OmniFile file = new OmniFile( volumeId, path);
        java.io.Reader reader = new java.io.FileReader( file.getStdFile() );
        doc.add(new Field( CConst.FIELD_CONTENT, reader, TextField.TYPE_NOT_STORED));

        return doc;
    }
}
