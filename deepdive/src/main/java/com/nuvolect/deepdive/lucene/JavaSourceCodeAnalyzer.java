package com.nuvolect.deepdive.lucene;
//
//TODO create class description
//

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.StopFilter;

import java.util.Set;

/**
 * Source Code Search Engine
 *
 * Now that we understand the basics of a search engine, let us look at what a search engine
 * that searches source code should support.
 * In the context of searching relevant example code in Java, a developer is typically interested
 * identifying Java classes that:
 *
 ** Extend a particular class or implement an interface.
 ** Call specific method(s).
 ** Use specific Java class(es).
 *
 * A combination of the above would satisfy the developer in identifying the relevant code
 * he or she is looking for. So the search engine should allow a developer to query one or
 * a combination of these aspects. Here is one other limitation of IDEs: most of the
 * available tools support searching source code based on just one of the above criteria.
 * They do not give the developer the flexibility of combining these criteria in their search.
 *
 *
 * Writing a Source Code Analyzer
 * The first step is to write an analyzer to extract or discard source code elements and ensure
 * that the indexes created are optimal, containing only the relevant aspects of code.
 * In Java, the language keywords--public, null, for, if, etc.--need to be discarded,
 * since they appear in every .java file. They are similar to the common words of
 * English language (the, a, an, of). An analyzer needs to discard these keywords in the indexes.
 *
 * A Java source code analyzer is built by extending Lucene's abstract Analyzer class.
 * The following listing shows the JavaSourceCodeAnalyzer, which implements the
 * tokenStream(String,Reader) method. The JavaSourceCodeAnalyzer defines a set of stop words
 * that can be discarded in the process of indexing, using a StopFilter provided by Lucene.
 * The tokenStream method checks the field that is being indexed.
 * If the field is a comment, it first tokenizes and lower-cases input using the
 * LowerCaseTokenizer, eliminates stop words of English (a limited set of English stop words)
 * using the StopFilter, and uses the PorterStemFilter to remove common morphological
 * and inflexional endings. If the content to be indexed is not a comment, the analyzer tokenizes
 * and lower-cases input using LowerCaseTokenizer and eliminates the Java keywords using
 * the StopFilter.
 *
 * http://www.onjava.com/pub/a/onjava/2006/01/18/using-lucene-to-search-java-source.html?page=2
 */
public class JavaSourceCodeAnalyzer extends Analyzer{

    private Set javaStopSet;
    private Set englishStopSet;

    private static final String[] JAVA_STOP_WORDS = {
            "public","private","protected","interface",
            "abstract","implements","extends","null","new",
            "switch","case", "default" ,"synchronized" ,
            "do", "if", "else", "break","continue","this",
            "assert" ,"for","instanceof", "transient",
            "final", "static" ,"void","catch","try",
            "throws","throw","class", "finally","return",
            "const" , "native", "super","while", "import",
            "package" ,"true", "false" };

    private static final String[] ENGLISH_STOP_WORDS ={
            "a", "an", "and", "are","as","at","be", "but",
            "by", "for", "if", "in", "into", "is", "it",
            "no", "not", "of", "on", "or", "s", "such",
            "that", "the", "their", "then", "there","these",
            "they", "this", "to", "was", "will", "with" };

    public JavaSourceCodeAnalyzer(){
        super();
        javaStopSet = StopFilter.makeStopSet(JAVA_STOP_WORDS);
        englishStopSet = StopFilter.makeStopSet(ENGLISH_STOP_WORDS);
    }

    @Override
    protected TokenStreamComponents createComponents(String s) {
        return null;
    }

//    public TokenStream tokenStream(String fieldName, java.io.Reader reader) {
//
//        if (fieldName.equals("comment"))
//            return   new PorterStemFilter(
//                    new StopFilter(
//                            new LowerCaseTokenizer(reader),englishStopSet));
//        else
//            return   new StopFilter(
//                    new LowerCaseTokenizer(reader),javaStopSet);
//    }
}
