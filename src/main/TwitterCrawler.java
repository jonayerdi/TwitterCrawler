package main;

import twitter.Tweet;
import twitter.TweetNavigator;

import java.io.FileOutputStream;
import java.util.*;

/**
 * Created by User on 11/10/2016.
 */
public class TwitterCrawler {

    public static final int FETCH_TWEETS = 100;

    private TweetNavigator twitter;

    public static void main(String[] args) {
        TwitterCrawler crawler = new TwitterCrawler();
        crawler.start();
    }

    public void start() {
        try {
            twitter = new TweetNavigator();
            Scanner in = new Scanner(System.in);
            System.out.print("TweetID: ");
            long tweetID = Long.valueOf(in.nextLine());
            List<Tweet> result = crawl(tweetID);
            FileOutputStream out = new FileOutputStream(tweetID + ".csv");
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
        int lastResultSize = 0;
        //We use Maps with textHashes as keys so that there are no duplicates
        Map<Integer,Tweet> toCrawlResponse = new HashMap<Integer,Tweet>();
        Map<Integer,Tweet> toCrawlUser = new HashMap<Integer,Tweet>();
        Map<Integer,Tweet> result = new HashMap<Integer,Tweet>(FETCH_TWEETS);
        //Start crawling from the initial tweet
        toCrawlResponse.put(tweet.getTextHash(),tweet);
        toCrawlUser.put(tweet.getTextHash(),tweet);
        while(result.size() + toCrawlResponse.size() + toCrawlUser.size() - 1 < FETCH_TWEETS) {
            System.out.println(lastResultSize + " tweets fetched");
            //Fetch tweets from the same user
            for (Integer key : toCrawlUser.keySet()) {
                Tweet crawl = toCrawlUser.get(key);
                List<Tweet> fetchList = Tweet.createList(twitter.getUserTimelineTweets(crawl.getStatus(), 100));
                for(Tweet fetchTweet : fetchList)
                    toCrawlResponse.put(fetchTweet.getTextHash(),fetchTweet);
            }
            //Add already crawled tweets to result
            System.out.println(toCrawlUser.size() + " tweets crawled from same users");
            result.putAll(toCrawlUser);
            toCrawlUser.clear();
            //Fetch in-reply-to and response tweets
            for (Integer key : toCrawlResponse.keySet()) {
                Tweet crawl = toCrawlResponse.get(key);
                List<Tweet> fetchList = Tweet.createList(twitter.getResponseTweets(crawl.getStatus(), FETCH_TWEETS));
                //fetchList.addAll(Tweet.createList(twitter.getInReplyToTweets(crawl.getStatus(), FETCH_TWEETS)));
                for(Tweet fetchTweet : fetchList)
                    toCrawlUser.put(fetchTweet.getTextHash(),fetchTweet);
            }
            //Add already crawled tweets to result
            System.out.println(toCrawlResponse.size() + " tweets crawled from responses");
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
        resultList = filterTweets(resultList);
        return resultList;
    }

    public List<Tweet> filterTweets(List<Tweet> tweets) {
        return tweets;
    }

}
