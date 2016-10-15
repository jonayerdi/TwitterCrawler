package filter;

import com.twitter.Extractor;
import twitter.Tweet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jon Ayerdi on 15/10/2016.
 */
public class TermExtractor {

    public static final String[] TO_REMOVE_FROM_TERMS
            = {".",":",",",";","\n","\r","?","!","&","'","\"","(",")"};

    public static List<String> extractTerms(Tweet tweet) {
        return extractTerms(tweet, new PorterStemmer());
    }

    public static List<String> extractTerms(Tweet tweet, Stemmer stemmer) {
        List<String> terms;
        String textWithoutEntities = removeEntities(tweet.getStatus().getText());
        terms = splitInTerms(textWithoutEntities, stemmer);
        return terms;
    }

    public static String removeEntities(String text) {
        Extractor extractor = new Extractor();
        List<Extractor.Entity> entities = extractor.extractEntitiesWithIndices(text);
        for(Extractor.Entity entity : entities) {
            switch (entity.getType()) {
                case HASHTAG:
                    text = text.replaceFirst("#" + entity.getValue(), "");
                    break;
                case MENTION:
                    text = text.replaceFirst("@" + entity.getValue(), "");
                    break;
                case CASHTAG:
                    text = text.replaceFirst("$" + entity.getValue(), "");
                    break;
                case URL:
                    text = text.replaceFirst(entity.getValue(), "");
                    break;
            }
        }
        return text;
    }

    public static List<String> splitInTerms(String text) {
        return splitInTerms(text, new PorterStemmer());
    }

    public static List<String> splitInTerms(String text, Stemmer stemmer) {
        List<String> terms = new ArrayList<String>();
        for(String term : text.split(" ")) {
            for(String remove : TO_REMOVE_FROM_TERMS)
                term = term.replace(remove, "");
            term = term.toLowerCase();
            term = stemmer.stem(term);
            if(!term.isEmpty())
                terms.add(term);
        }
    }

}
