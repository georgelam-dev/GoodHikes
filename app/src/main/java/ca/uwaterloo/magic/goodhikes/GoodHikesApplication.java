package ca.uwaterloo.magic.goodhikes;

import android.app.Application;

import ca.uwaterloo.magic.goodhikes.data.User;
import ca.uwaterloo.magic.goodhikes.data.UserManager;

public class GoodHikesApplication extends Application {
    public User currentUser;
    private UserManager userManager;

    public void onCreate() {
        super.onCreate();
        userManager = new UserManager(getApplicationContext());
        currentUser = userManager.getUser();
    }
    public void setUser(User user) {
        this.currentUser = user;
    }
}
