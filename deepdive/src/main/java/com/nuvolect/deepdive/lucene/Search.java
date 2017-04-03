package com.nuvolect.deepdive.lucene;

import android.content.Context;

import com.nuvolect.deepdive.main.CConst;
import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.OmniFile;
import com.nuvolect.deepdive.util.OmniHash;
import com.nuvolect.deepdive.main.App;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lukhnos.portmobile.file.Paths;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * Methods to search the Omni file structures
 */
public class Search {

    private static Analyzer m_analyzer;
    private static Directory m_directory = null;
    private static final int MAX_HITS = 50;

    private static void preSearch( String volumeId, String searchPath) {

        /**
         * The WhitespaceAnalyzer is case sensitive.
         */
        m_analyzer = new org.apache.lucene.analysis.core.SimpleAnalyzer();

        OmniFile luceneDir = IndexUtil.getCacheDir( volumeId, searchPath);
        boolean cacheDirExists = ! luceneDir.mkdirs();

        try {
            m_directory = FSDirectory.open( Paths.get( luceneDir.getCanonicalPath()));
        } catch (IOException e) {
            LogUtil.logException( Search.class, e);
        }

        if( ! cacheDirExists)
            Index.index( volumeId, searchPath, true);// true == force re-index
    }


    /**
     * Return results for a search along a specific path.  If the path is changed or new
     * create an index.
     * @param searchQuery
     * @param searchPath
     * @return
     */
    public static JSONObject search(String searchQuery, String volumeId, String searchPath) {

        JSONObject result = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        Context ctx = App.getContext();

        DirectoryReader ireader = null;
        ScoreDoc[] scoreDocs = null;
        String error = "";

        preSearch(volumeId, searchPath);
        try {
            ireader = DirectoryReader.open(m_directory);
        } catch (IOException e) {
            LogUtil.logException( Search.class, e);
            error += e.toString();
        }
        IndexSearcher isearcher = new IndexSearcher(ireader);

        try {

            // Parse a simple query that searches for "text":
            QueryParser parser = new QueryParser( CConst.FIELD_CONTENT, m_analyzer);
            Query query = null;
            query = parser.parse(searchQuery);
            TopScoreDocCollector collector = TopScoreDocCollector.create( MAX_HITS);
            isearcher.search( query, collector);
            scoreDocs = collector.topDocs().scoreDocs;

        } catch ( ParseException | IOException e) {
            LogUtil.logException( Search.class, e);
            error += e.toString();
        }
        // Iterate through the results creating an object for each file
        HashMap<String, Integer> hitCounts = new HashMap<>();
        HashMap<String, Integer> hitIndexes = new HashMap<>();

        /**
         * First iterate the hit list and count duplicates based on file path.
         */
        // TODO Find a Lucene query that does not produce duplicates on same file
        // This will save computation too
        for (int ii = 0; scoreDocs != null && ii < scoreDocs.length; ++ii) {

            Document hitDoc = null;
            try {
                hitDoc = isearcher.doc(scoreDocs[ii].doc);
            } catch (IOException e) {
                LogUtil.logException( Search.class, e);
                error += e.toString();
            }
            String filePath = hitDoc.get(( CConst.FIELD_PATH));

            if( hitCounts.containsKey(filePath)){

                hitCounts.put( filePath, hitCounts.get( filePath) + 1);
            }
            else{
                hitCounts.put( filePath, 1);
                hitIndexes.put( filePath, ii);
            }
        }

        /**
         * Iterate over each unique hit and save the results
         */
        for(Map.Entry<String, Integer> uniqueHit : hitIndexes.entrySet()){

            Document hitDoc = null;
            try {
                hitDoc = isearcher.doc(scoreDocs[ uniqueHit.getValue() ].doc);
            } catch (IOException e) {
                LogUtil.logException( Search.class, e);
                error += e.toString();
            }
            String file_name = hitDoc.get(( CConst.FIELD_FILENAME));
            String file_path = hitDoc.get(( CConst.FIELD_PATH));
            File parentFolder = new File( file_path).getParentFile();
            try {
                String folder_url = OmniHash.getHashedServerUrl( ctx, volumeId, file_path);

                JSONObject hitObj = new JSONObject();
                hitObj.put("volume_id", volumeId);
                hitObj.put("file_path", file_path);
                hitObj.put("file_name", file_name);
                hitObj.put("folder_url", folder_url);
                hitObj.put("num_hits", hitCounts.get(file_path));
                hitObj.put("error", error);
                jsonArray.put(hitObj);

            } catch (Exception e) {
                LogUtil.logException( Search.class, e);
            }
        }

        try {
            result.put("hits", jsonArray!=null?jsonArray:new JSONArray());
            result.put("num_hits", scoreDocs!=null?scoreDocs.length:0);
            result.put("error", error);

            ireader.close();
            m_directory.close();

        } catch (JSONException | IOException e) {
            LogUtil.logException( Search.class, e);
        }

        return result;
    }
}
