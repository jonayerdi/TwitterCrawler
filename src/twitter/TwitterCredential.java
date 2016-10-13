package twitter;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by User on 13/10/2016.
 */
public class TwitterCredential {

    private String OAuthConsumerKey
            ,OAuthConsumerSecret
            ,OAuthAccessToken
            ,OAuthAccessTokenSecret;

    private int secondsUntilReset = 0;
    private long resetTimestamp = 0;

    public static TwitterCredential[] loadCredentials(InputStream input) {
        List<TwitterCredential> credentials = new ArrayList<TwitterCredential>();
        Scanner in = new Scanner(input);
        while(in.hasNext()) {
            TwitterCredential credential = new TwitterCredential();
            credential.setOAuthConsumerKey(in.nextLine().split("[=]")[1]);
            credential.setOAuthConsumerSecret(in.nextLine().split("[=]")[1]);
            credential.setOAuthAccessToken(in.nextLine().split("[=]")[1]);
            credential.setOAuthAccessTokenSecret(in.nextLine().split("[=]")[1]);
            credentials.add(credential);
        }
        return credentials.toArray(new TwitterCredential[0]);
    }

    public static TwitterCredential[] loadCredentialsFromFile(String filename) {
        TwitterCredential[] credentials = new TwitterCredential[0];
        try {
            FileInputStream file = new FileInputStream(filename);
            credentials = loadCredentials(file);
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return credentials;
    }

    public int remainingSeconds() {
        return (int)((resetTimestamp/1000 + secondsUntilReset) - System.currentTimeMillis()/1000);
    }

    public int getSecondsUntilReset() {
        return secondsUntilReset;
    }

    public void setSecondsUntilReset(int secondsUntilReset) {
        this.secondsUntilReset = secondsUntilReset;
    }

    public long getResetTimestamp() {
        return resetTimestamp;
    }

    public void setResetTimestamp(long resetTimestamp) {
        this.resetTimestamp = resetTimestamp;
    }

    public String getOAuthConsumerKey() {
        return OAuthConsumerKey;
    }

    public void setOAuthConsumerKey(String OAuthConsumerKey) {
        this.OAuthConsumerKey = OAuthConsumerKey;
    }

    public String getOAuthConsumerSecret() {
        return OAuthConsumerSecret;
    }

    public void setOAuthConsumerSecret(String OAuthConsumerSecret) {
        this.OAuthConsumerSecret = OAuthConsumerSecret;
    }

    public String getOAuthAccessToken() {
        return OAuthAccessToken;
    }

    public void setOAuthAccessToken(String OAuthAccessToken) {
        this.OAuthAccessToken = OAuthAccessToken;
    }

    public String getOAuthAccessTokenSecret() {
        return OAuthAccessTokenSecret;
    }

    public void setOAuthAccessTokenSecret(String OAuthAccessTokenSecret) {
        this.OAuthAccessTokenSecret = OAuthAccessTokenSecret;
    }

}
