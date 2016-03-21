package ca.uwaterloo.magic.goodhikes.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by Chelsea on 3/20/16.
 */
public class UserManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private static final String PREF_NAME = "UserManager";
    protected static final String LOG_TAG = "UserManager";

    /** Initialize **/
    public UserManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, 0);
        editor = pref.edit();
    }

    /** Set user info after login **/
    public void setUser(User user) {
        editor.putBoolean("login", true);
        editor.putString("ID", user.getId());
        editor.putString("name", user.getUsername());
        editor.putString("email", user.getEmail());

        editor.commit();

        Log.d(LOG_TAG, "User ID = " + pref.getString("ID", "none"));
        Log.d(LOG_TAG, "User name = " + pref.getString("name", "none"));
    }

    /** Clear user info after logout **/
    public void clearUser() {
        editor.clear();
        editor.commit();
    }

    /** Check if user is logged in **/
    public boolean checkLogin() {
        return pref.getBoolean("login", false);
    }

    /** Get user info **/
    public User getUser() {
        String ID = pref.getString("ID", "000");
        String name = pref.getString("name", "AAA");
        String email = pref.getString("email", "");

        User user = new User(ID, name, email);
        return user;
    }

}
