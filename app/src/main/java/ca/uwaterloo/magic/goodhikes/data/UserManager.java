/*------------------------------------------------------------------------------
 *   Authors: Slavik, George, Thao, Chelsea
 *   Copyright: (c) 2016 Team Magic
 *
 *   This file is part of GoodHikes.
 *
 *   GoodHikes is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   GoodHikes is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with GoodHikes.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uwaterloo.magic.goodhikes.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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
        editor.putString("image", user.getImage());

        editor.commit();

        Log.d(LOG_TAG, "User ID = " + pref.getString("ID", "none"));
        Log.d(LOG_TAG, "User name = " + pref.getString("name", "none"));
        //Log.d(LOG_TAG, "Image str = " + pref.getString("image", "none"));
    }

    /** Get encoded image string **/
    public void setImage(String image) {
        editor.putString("image", image);
        editor.commit();
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
        String image = pref.getString("image", "");

        User user = new User(ID, name, email, image);
        return user;
    }



}
