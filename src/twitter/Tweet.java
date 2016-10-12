package twitter;

import twitter4j.Status;

import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 11/10/2016.
 */
public class Tweet {

    private Status status;
    private int textHash;

    public Tweet(Status status) {
        this.status = status;
        this.textHash = status.getText().hashCode();
    }

    //Utility

    public boolean isDuplicateOf(Tweet tweet) {
        return this.textHash == tweet.getTextHash();
    }

    public String toString() {
        return "ID: " + status.getId() + "\nDate: " + status.getCreatedAt()
                + "\nUser: " + status.getUser().getScreenName() + "\nText: " + status.getText();
    }

    public static List<Tweet> createList(List<Status> statuses) {
        List<Tweet> tweets = new ArrayList<Tweet>();
        try {
            for(Status status : statuses)
                tweets.add(new Tweet(status));
        } catch (Exception e) {}
        return tweets;
    }

    public static void writeToCSV(List<Tweet> tweets, OutputStream out) {
        PrintStream print = new PrintStream(out);
        print.println("id;userId;userName;text;retweetCount;creationDate;favoriteCount;textHash");
        for(Tweet tweet : tweets) {
            Status status = tweet.getStatus();
            print.println(status.getId()+";"+status.getUser().getId()+";"
                    +toCSVString(status.getUser().getScreenName())+";"+toCSVString(status.getText())
                    +";"+status.getRetweetCount()+";"+new Timestamp(status.getCreatedAt().getTime())
                    +";"+status.getFavoriteCount()+";"+status.getText().hashCode());
        }
    }

    public static String toCSVString(String s) {
        return "\"" + s.replaceAll("\"","\"\"") + "\"";
    }

    //Getters and Setters

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getTextHash() {
        return textHash;
    }

}
