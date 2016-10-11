package twitter;

import twitter4j.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 11/10/2016.
 */
public class Tweet {

    private Status status;

    public Tweet(Status status) {
        this.status = status;
    }

    //Utility

    public static List<Tweet> createList(List<Status> statuses) {
        List<Tweet> tweets = new ArrayList<Tweet>();
        try {
            for(Status status : statuses)
                tweets.add(new Tweet(status));
        } catch (Exception e) {}
        return tweets;
    }

    public String toString() {
        return "ID: " + status.getId() + "\nDate: " + status.getCreatedAt() + "\nUser: " + status.getUser().getName() + "\nTweet: " + status.getText();
    }

    //Getters and Setters

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
