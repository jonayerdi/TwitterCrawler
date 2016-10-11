package twitter;

import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 05/10/2016.
 */
public class TweetNavigator {

    private final Twitter twitter;

    public TweetNavigator() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        //builder.setOAuthConsumerKey(consumerKey);
        //builder.setOAuthConsumerSecret(consumerSecret);
        builder.setDebugEnabled(false);
        Configuration configuration = builder.build();
        TwitterFactory factory = new TwitterFactory(configuration);
        twitter = factory.getInstance();
        ////twitter.setOAuthConsumer(consumerKey, consumerSecret);
        //AccessToken accessToken = new AccessToken(twitterAccessToken, twitterAccessTokenSecret);
        //twitter.setOAuthAccessToken(accessToken);
    }

    /**
     * Fetch a tweet from its TweetID
     *
     * @param tweetID
     */
    public Status getTweetByTweetID(long tweetID) {
        try {
            Status status = twitter.showStatus(tweetID);
            return status;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Fetch a tweet from its TweetID
     *
     * @param tweetID
     */
    public List<Status> searchTweets(String searchTerms, int maxResults) {
        try {
            Query query = new Query(searchTerms);
            query.setCount(maxResults);
            query.setLang("en");
            QueryResult result = twitter.search(query);
            return result.getTweets();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets the last tweets posted by tweet.getUser()
     *
     * @param tweet Base tweet from which to search for more tweets
     * @param maxResults Maximum number of tweets fetched in total
     * @return
     */
    public List<Status> getUserTimelineTweets(Status tweet, int maxResults) {
        List<Status> userTimelineTweets = new ArrayList<Status>();
        try {
            for(int i = 1 ; i <= maxResults/50 ; i++) {
                userTimelineTweets.addAll(twitter.getUserTimeline(tweet.getUser().getId(), new Paging(i,50)));
            }
            userTimelineTweets.addAll(twitter.getUserTimeline(tweet.getUser().getId(), new Paging((maxResults/50) + 1,maxResults%50)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userTimelineTweets;
    }

    /**
     * Gets the list of tweets to which the base tweet replied
     *
     * @param tweet Base tweet from which to search for more tweets
     * @param maxResults Maximum number of tweets fetched in total
     * @return
     */
    public List<Status> getInReplyToTweets(Status tweet, int maxResults) {
        List<Status> replyTweets = new ArrayList<Status>();
        Status currentTweet = tweet;
        int fetched = 0;
        try {
            while(fetched < maxResults && currentTweet.getInReplyToStatusId() > 0) {
                currentTweet = twitter.showStatus(currentTweet.getInReplyToStatusId());
                replyTweets.add(currentTweet);
                fetched++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  replyTweets;
    }

    /**
     * Gets the list of tweets that reply to the provided tweet
     *
     * @param tweet Base tweet from which to search for more tweets
     * @param maxResults Maximum number of tweets fetched in total
     * @return
     */
    public List<Status> getResponseTweets(Status tweet, int maxResults) {
        List<Status> replyTweets = new ArrayList<Status>();
        try {
            //Search for tweets with @tweet.getUser() since tweet was posted
            Query query = new Query("@" + tweet.getUser().getScreenName() + " since_id:" + tweet.getId());
            query.setCount(50);
            QueryResult result = twitter.search(query);
            while(query != null) {
                List<Status> resultTweets = result.getTweets();

                for (Status response : resultTweets)
                    //Make sure the response is indeed a reply to the original tweet
                    if (response.getInReplyToStatusId() == tweet.getId())
                        replyTweets.add(response);

                //Next page of results
                query = result.nextQuery();

                if (query != null) {
                    if(replyTweets.size()<maxResults-50)
                        result = twitter.search(query);
                    //Break if we can get more than maxResults in the next cycle
                    else break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  replyTweets;
    }

}
