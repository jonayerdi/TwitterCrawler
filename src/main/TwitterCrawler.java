package main;

import filter.PorterStemmer;
import filter.ScoredTweet;
import filter.TfidfFilter;
import misc.Console;
import twitter.Tweet;
import twitter.TweetNavigator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

/**
 * Created by Jon Ayerdi on 11/10/2016.
 */
public class TwitterCrawler {

    public static final String CREDENTIALS_FILE = "credentials.ini";

    public static final int MIN_RETWEETS = 1;
    public static final int FETCH_TWEETS = 10000;

    public static final double MAX_SCORE = 1.0;
    public static final double MIN_SCORE = 0.7;
    public static final int MIN_TERMS = 5;
    public static final int MIN_TERM_SIZE = 2;

    private TweetNavigator twitter;

    public static void main(String[] args) {
        Console.captureOutput();
        TwitterCrawler crawler = new TwitterCrawler();
        crawler.start3();
    }

    public void start() {
        try {
            twitter = new TweetNavigator(CREDENTIALS_FILE);
            Scanner in = new Scanner(System.in);
            Console.out.print("TweetID: ");
            long tweetID = Long.valueOf(in.nextLine());
            List<Tweet> result = crawl(tweetID);
            FileOutputStream out = new FileOutputStream(tweetID + ".csv");
            Tweet.writeToCSV(result,out);
            out.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void start2() {
        try {
            FileOutputStream out = new FileOutputStream("786237498036858880_filtered.csv");
            twitter = new TweetNavigator(CREDENTIALS_FILE);
            List<ScoredTweet> filtered = getBestTweets(new Tweet(twitter.getTweetByTweetID(786237498036858880L))
                    ,Tweet.readFromCSV("786237498036858880.csv"));
            ScoredTweet.writeToCSV(filtered, out);
            out.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void start3() {
        try {
            twitter = new TweetNavigator(CREDENTIALS_FILE);
            Scanner in = new Scanner(System.in);
            Console.out.print("TweetID: ");
            long tweetID = Long.valueOf(in.nextLine());
            List<ScoredTweet> result = crawlBestTweets(tweetID);
            FileOutputStream out = new FileOutputStream(tweetID + ".csv");
            ScoredTweet.writeToCSV(result,out);
            out.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a list with the extracted tweets that match best with the original tweet
     *
     * @param tweetID TweetID of the tweet to crawl
     * @return A list with the extracted tweets that match best with the original tweet
     */
    public List<ScoredTweet> crawlBestTweets(long tweetID) {
        return crawlBestTweets(new Tweet(twitter.getTweetByTweetID(tweetID)));
    }

    /**
     * Returns a list with the extracted tweets that match best with the original tweet
     *
     * @param tweet Tweet to crawl
     * @return A list with the extracted tweets that match best with the original tweet
     */
    public List<ScoredTweet> crawlBestTweets(Tweet tweet) {
        List<Tweet> crawled = crawl(tweet);
        List<ScoredTweet> filtered = getBestTweets(tweet, crawled);
        return filtered;
    }

    /**
     * Extracts tweets related to the provided tweet
     *
     * @param tweetID ID of the tweet to crawl
     * @return A list with the extracted tweets
     */
    public List<Tweet> crawl(long tweetID) {
        return crawl(new Tweet(twitter.getTweetByTweetID(tweetID)));
    }

    /**
     * Extracts tweets related to the provided tweet
     *
     * @param tweet The tweet to crawl
     * @return A list with the extracted tweets
     */
    public List<Tweet> crawl(Tweet tweet) {
        List<Tweet> resultList;
        int lastResultSize = 0;
        //Do not repeat users
        Set<String> crawledUsers = new HashSet<String>();
        //Do not repeat mentions
        Set<String> crawledMentions = new HashSet<String>();
        //We use Maps with textHashes as keys so that there are no duplicates
        Map<Integer,Tweet> toCrawlResponse = new HashMap<Integer,Tweet>();
        Map<Integer,Tweet> toCrawlUser = new HashMap<Integer,Tweet>();
        Map<Integer,Tweet> result = new HashMap<Integer,Tweet>(FETCH_TWEETS);
        //Start crawling from the initial tweet
        toCrawlResponse.put(tweet.getTextHash(),tweet);
        toCrawlUser.put(tweet.getTextHash(),tweet);
        while(result.size() + toCrawlResponse.size() + toCrawlUser.size() - 1 < FETCH_TWEETS) {
            Console.out.println("ResultSize: " + lastResultSize);
            //Fetch tweets from the same user
            Console.out.println("toCrawlUser: " + toCrawlUser.size());
            for (Integer key : toCrawlUser.keySet()) {
                //Early exit
                if(result.size() + toCrawlResponse.size() + toCrawlUser.size() - 1 >= FETCH_TWEETS) break;
                Tweet crawl = toCrawlUser.get(key);
                List<Tweet> fetchList = Tweet.createList(twitter.getUserTimelineTweets(crawl.getStatus(), MIN_RETWEETS, 100));
                for(Tweet fetchTweet : fetchList)
                    toCrawlResponse.put(fetchTweet.getTextHash(),fetchTweet);
            }
            //Add already crawled tweets to result
            result.putAll(toCrawlUser);
            toCrawlUser.clear();
            //Fetch in-reply-to and response tweets
            Console.out.println("toCrawlResponse: " + toCrawlResponse.size());
            for (Integer key : toCrawlResponse.keySet()) {
                //Early exit
                if(result.size() + toCrawlResponse.size() + toCrawlUser.size() - 1 >= FETCH_TWEETS) break;
                Tweet crawl = toCrawlResponse.get(key);
                List<Tweet> fetchList = new ArrayList<>();//Tweet.createList(twitter.getResponseTweets(crawl.getStatus(), MIN_RETWEETS, 100));
                if(!crawledMentions.contains(crawl.getStatus().getUser().getScreenName())) {
                    fetchList = Tweet.createList(twitter.getMentionTweets(crawl.getStatus(), MIN_RETWEETS, 100));
                    crawledMentions.add(crawl.getStatus().getUser().getScreenName());
                }
                //fetchList.addAll(Tweet.createList(twitter.getInReplyToTweets(crawl.getStatus(), 100)));
                for(Tweet fetchTweet : fetchList)
                    if(!crawledUsers.contains(fetchTweet.getStatus().getUser().getScreenName())) {
                        toCrawlUser.put(fetchTweet.getTextHash(),fetchTweet);
                        crawledUsers.add(fetchTweet.getStatus().getUser().getScreenName());
                    }
            }
            //Add already crawled tweets to result
            result.putAll(toCrawlResponse);
            toCrawlResponse.clear();
            //If we have not fetched more tweets, we terminate
            if(result.size() > lastResultSize)
                lastResultSize = result.size();
            else
                break;
        }
        //Add already crawled tweets to result
        result.putAll(toCrawlUser);
        result.putAll(toCrawlResponse);
        //Remove original tweet
        result.remove(tweet.getTextHash());
        //Filter tweets
        resultList = Arrays.asList(result.values().toArray(new Tweet[0]));
        return resultList;
    }

    /**
     * Returns the tweets that best match the query with their corresponding TFIDF score
     *
     * @param query The query tweet
     * @param tweets The documents to score
     * @param minScore Minimum score needed for a document to show in result
     * @param maxScore Maximum score needed for a document to show in result
     * @param minTerms Minimum number of terms a document needs in order to show in result
     * @param minTermSize Minimum number of characters a term must have to count in the score
     * @return The tweets that best match the query with their corresponding TFIDF score
     */
    public List<ScoredTweet> getBestTweets(Tweet query, List<Tweet> tweets
            , double minScore, double maxScore, int minTerms, int minTermSize) {
        TfidfFilter filter = new TfidfFilter(TfidfFilter.ScoringMode.TFIDF
                , new PorterStemmer(), minTerms, minTermSize);
        List<ScoredTweet> scored = filter.getScores(query, tweets);
        List<ScoredTweet> filtered = new ArrayList<>();
        for(ScoredTweet scoredTweet : scored)
            if(scoredTweet.score >= minScore && scoredTweet.score <= maxScore)
                filtered.add(scoredTweet);
        return filtered;
    }

    /**
     * Returns the tweets that best match the query with their corresponding TFIDF score
     *
     * @param query The query tweet
     * @param tweets The documents to score
     * @return The tweets that best match the query with their corresponding TFIDF score
     */
    public List<ScoredTweet> getBestTweets(Tweet query, List<Tweet> tweets) {
        return getBestTweets(query, tweets, MIN_SCORE, MAX_SCORE, MIN_TERMS, MIN_TERM_SIZE);
    }

}
