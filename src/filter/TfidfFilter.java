package filter;

import twitter.Tweet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jon Ayerdi on 14/10/2016.
 */
public class TfidfFilter {

    public enum ScoringMode {
        TF,
        TFIDF
    }

    private ScoringMode mode;
    private Stemmer stemmer;

    public TfidfFilter(ScoringMode mode, Stemmer stemmer) {
        this.mode = mode;
        this.stemmer = stemmer;
    }

    public double[] getScores(Tweet query, List<Tweet> documents) {
        List<String> queryTerms;
        List<List<String>> documentTerms = new ArrayList<List<String>>();

        List<Map<String,Double>> TF = new ArrayList<Map<String,Double>>();
        Map<String,Double> IDF;
        double[] score = new double[documents.size()];

        //Extract terms from query
        queryTerms = TermExtractor.extractTerms(query, stemmer);
        for(int i = 0 ; i < documents.size() ; i++) {
            //Extract terms from documents
            documentTerms.add(TermExtractor.extractTerms(documents.get(i)));
            //TF
            TF.add(TfIdf.tf(documentTerms.get(i)));
        }

        switch (mode) {
            case TF:
                for(int i = 0 ; i < documents.size() ; i++) {
                    Map<String,Double> tf = TF.get(i);
                    score[i] = 0;
                    for(String term : tf.keySet())
                        if(queryTerms.contains(term))
                            score[i] += tf.get(term);
                }
                break;
            case TFIDF:
                IDF = TfIdf.idf(documentTerms);
                for(int i = 0 ; i < documents.size() ; i++) {
                    Map<String,Double> tfidf = TfIdf.tfIdf(TF.get(i), IDF);
                    score[i] = 0;
                    for(String term : tfidf.keySet())
                        if(queryTerms.contains(term))
                            score[i] += tfidf.get(term);
                }
                break;
        }
        return score;
    }

}
