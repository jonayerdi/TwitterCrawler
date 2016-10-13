package twitter;

import misc.Console;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.internal.http.HttpResponseCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 05/10/2016.
 */
public class TweetNavigator {

    private Twitter twitter;
    private final TwitterCredential[] credentials;
    private TwitterCredential credential;
    private int credentialIndex;

    public TweetNavigator() throws Exception{
        credentials = TwitterCredential.loadCredentialsFromFile("credentials.ini");
        if(credentials.length < 1)
            throw new Exception("No credentials loaded");
        credentialIndex = 0;
        credential = credentials[credentialIndex];
        buildConfiguration();
    }

    public void buildConfiguration() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(credential.getOAuthConsumerKey());
        builder.setOAuthConsumerSecret(credential.getOAuthConsumerSecret());
        builder.setDebugEnabled(false);
        Configuration configuration = builder.build();
        TwitterFactory factory = new TwitterFactory(configuration);
        twitter = factory.getInstance();
        //twitter.setOAuthConsumer(consumerKey, consumerSecret);
        AccessToken accessToken = new AccessToken(credential.getOAuthAccessToken(), credential.getOAuthAccessTokenSecret());
        twitter.setOAuthAccessToken(accessToken);
    }

    public void switchCredentials(int secondsUntilReset) {
        credential.setResetTimestamp(System.currentTimeMillis());
        credential.setSecondsUntilReset(secondsUntilReset);
        while(credential.remainingSeconds()>0) {
            credential = credentials[(++credentialIndex)%credentials.length];
            Console.out.println("Switching to credential " + credentialIndex%credentials.length);
            if(credentialIndex%credentials.length == 0) {
                Console.out.println("First credential : Sleeping for " + credential.remainingSeconds() + " seconds");
                try {
                    Thread.sleep(credential.remainingSeconds()*1200);
                } catch (Exception e) {}
            }
        }
        buildConfiguration();
        Console.out.println("Using credential " + credentialIndex%credentials.length);
    }

    /**
     * Fetch a tweet from its TweetID
     *
     * @param tweetID
     */
    public Status getTweetByTweetID(long tweetID) {
        boolean loop = true;
        while(loop) {
            loop = false;
            try {
                Status status = twitter.showStatus(tweetID);
                return status;
            } catch (TwitterException e) {
                if(e.getStatusCode() == HttpResponseCode.TOO_MANY_REQUESTS) {
                    RateLimitStatus rateLimitStatus = e.getRateLimitStatus();
                    if (rateLimitStatus != null)
                        switchCredentials(rateLimitStatus.getSecondsUntilReset());
                    else
                        switchCredentials(900);
                    loop = true;
                }
                else e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Fetch a tweet from its TweetID
     *
     * @param tweetID
     */
    public List<Status> searchTweets(String searchTerms, int maxResults) {
        boolean loop = true;
        while(loop) {
            loop = false;
            try {
                Query query = new Query(searchTerms);
                query.setCount(maxResults);
                query.setLang("en");
                QueryResult result = twitter.search(query);
                return result.getTweets();
            } catch (TwitterException e) {
                if(e.getStatusCode() == HttpResponseCode.TOO_MANY_REQUESTS) {
                    RateLimitStatus rateLimitStatus = e.getRateLimitStatus();
                    if (rateLimitStatus != null)
                        switchCredentials(rateLimitStatus.getSecondsUntilReset());
                    else
                        switchCredentials(900);
                    loop = true;
                }
                else e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        boolean loop = true;
        while(loop) {
            loop = false;
            try {
                for (int i = 1; i <= maxResults / 100; i++) {
                    userTimelineTweets.addAll(twitter.getUserTimeline(tweet.getUser().getId(), new Paging(i, 100)));
                }
            } catch (TwitterException e) {
                if(e.getStatusCode() == HttpResponseCode.TOO_MANY_REQUESTS) {
                    RateLimitStatus rateLimitStatus = e.getRateLimitStatus();
                    if (rateLimitStatus != null)
                        switchCredentials(rateLimitStatus.getSecondsUntilReset());
                    else
                        switchCredentials(900);
                    loop = true;
                    userTimelineTweets.clear();
                }
                else e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        boolean loop = true;
        while(loop) {
            loop = false;
            try {
                while (fetched < maxResults && currentTweet.getInReplyToStatusId() > 0) {
                    currentTweet = twitter.showStatus(currentTweet.getInReplyToStatusId());
                    replyTweets.add(currentTweet);
                    fetched++;
                }
            } catch (TwitterException e) {
                if(e.getStatusCode() == HttpResponseCode.TOO_MANY_REQUESTS) {
                    RateLimitStatus rateLimitStatus = e.getRateLimitStatus();
                    if (rateLimitStatus != null)
                        switchCredentials(rateLimitStatus.getSecondsUntilReset());
                    else
                        switchCredentials(900);
                    loop = true;
                }
                else e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        boolean loop = true;
        while(loop) {
            loop = false;
            try {
                //Search for tweets with @tweet.getUser() since tweet was posted
                Query query = new Query("@" + tweet.getUser().getScreenName() + " since_id:" + tweet.getId());
                query.setCount(100);
                QueryResult result = twitter.search(query);
                while (query != null) {
                    List<Status> resultTweets = result.getTweets();

                    for (Status response : resultTweets)
                        //Make sure the response is indeed a reply to the original tweet
                        if (response.getInReplyToStatusId() == tweet.getId())
                            replyTweets.add(response);

                    //Next page of results
                    query = result.nextQuery();

                    if (query != null) {
                        if (replyTweets.size() < maxResults - 100)
                            result = twitter.search(query);
                            //Break if we can get more than maxResults in the next cycle
                        else break;
                    }
                }
            } catch (TwitterException e) {
                if(e.getStatusCode() == HttpResponseCode.TOO_MANY_REQUESTS) {
                    RateLimitStatus rateLimitStatus = e.getRateLimitStatus();
                    if (rateLimitStatus != null)
                        switchCredentials(rateLimitStatus.getSecondsUntilReset());
                    else
                        switchCredentials(900);
                    loop = true;
                    replyTweets.clear();
                }
                else e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return  replyTweets;
    }

}
