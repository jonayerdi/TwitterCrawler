package main;

import twitter.Tweet;
import twitter.TweetNavigator;

import java.io.FileOutputStream;
import java.util.*;

/**
 * Created by User on 11/10/2016.
 */
public class TwitterCrawler {

    public static final int FETCH_TWEETS = 1000;

    private TweetNavigator twitter;

    public static void main(String[] args) {
        TwitterCrawler crawler = new TwitterCrawler();
        crawler.start();
    }

    public void start() {
        try {
            twitter = new TweetNavigator();
            Scanner in = new Scanner(System.in);
            System.out.print("CSV Filename: ");
            FileOutputStream out = new FileOutputStream(in.nextLine() + ".csv");
            System.out.print("TweetID: ");
            List<Tweet> result = crawl(Long.valueOf(in.nextLine()));
            Tweet.writeToCSV(result,out);
            out.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public List<Tweet> crawl(long tweetID) {
        return crawl(new Tweet(twitter.getTweetByTweetID(tweetID)));
    }

    public List<Tweet> crawl(Tweet tweet) {
        List<Tweet> resultList;
        //We use Maps with textHashes as keys so that there are no duplicates
        Map<Integer,Tweet> fetched = new HashMap<Integer,Tweet>();
        Map<Integer,Tweet> toCrawl = new HashMap<Integer,Tweet>();
        Map<Integer,Tweet> result = new HashMap<Integer,Tweet>(FETCH_TWEETS);
        toCrawl.put(tweet.getTextHash(),tweet);
        while(result.size() + toCrawl.size() - 1 < FETCH_TWEETS) {
            for (Integer key : toCrawl.keySet()) {
                Tweet crawl = toCrawl.get(key);
                List<Tweet> fetchList = fetchTweets(crawl);
                for(Tweet fetchTweet : fetchList)
                    fetched.put(fetchTweet.getTextHash(),fetchTweet);
            }
            //Add already crawled tweets to result
            result.putAll(toCrawl);
            //Add newly fetched tweets to crawl list.
            toCrawl.clear();
            toCrawl.putAll(fetched);
            //Clear newly fetched list
            fetched.clear();
        }
        //Remove original tweet
        result.remove(tweet.getTextHash());
        resultList = Arrays.asList(result.values().toArray(new Tweet[0]));
        resultList = filterTweets(resultList);
        return resultList;
    }

    public List<Tweet> fetchTweets(Tweet tweet) {

    }

    public List<Tweet> filterTweets(List<Tweet> tweets) {
        return tweets;
    }

}
