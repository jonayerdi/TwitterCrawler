package main;

import misc.Console;
import twitter.Tweet;
import twitter.TweetNavigator;

import java.io.FileOutputStream;
import java.util.*;

/**
 * Created by Jon Ayerdi on 11/10/2016.
 */
public class TwitterCrawler {

    public static final String CREDENTIALS_FILE = "credentials.ini";

    public static final int MIN_RETWEETS = 1;
    public static final int FETCH_TWEETS = 1000;

    private TweetNavigator twitter;

    public static void main(String[] args) {
        Console.captureOutput();
        TwitterCrawler crawler = new TwitterCrawler();
        crawler.start();
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
                if(toCrawlResponse.size() > 100 || result.size()
                        + toCrawlResponse.size() + toCrawlUser.size() - 1 >= FETCH_TWEETS) break;
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
                if(toCrawlUser.size() > 100 || result.size()
                        + toCrawlResponse.size() + toCrawlUser.size() - 1 >= FETCH_TWEETS) break;
                Tweet crawl = toCrawlResponse.get(key);
                //List<Tweet> fetchList = Tweet.createList(twitter.getResponseTweets(crawl.getStatus(), MIN_RETWEETS, 100));
                List<Tweet> fetchList = Tweet.createList(twitter.getMentionTweets(crawl.getStatus(), MIN_RETWEETS, 100));
                fetchList.addAll(Tweet.createList(twitter.getInReplyToTweets(crawl.getStatus(), 100)));
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

}
