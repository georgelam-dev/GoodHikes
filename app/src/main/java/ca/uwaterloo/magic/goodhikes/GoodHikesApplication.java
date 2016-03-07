package ca.uwaterloo.magic.goodhikes;

import android.app.Application;

import ca.uwaterloo.magic.goodhikes.data.User;

public class GoodHikesApplication extends Application {
    public User currentUser;
    @Override
    public void onCreate() {
        super.onCreate();
        currentUser = new User("stub");
    }
}
