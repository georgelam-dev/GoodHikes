package ca.uwaterloo.magic.goodhikes.data;

import android.content.ContentValues;

import ca.uwaterloo.magic.goodhikes.data.RoutesContract.UserEntry;

public class User {
    private String username;
    private long id;

    public User() {}

    public User(String username) {
        this.username = username;
    }

    public long getId() {return id;}
    public void setId(long id) {this.id = id;}

    public String getUsername(){
        return username;
    }
    public ContentValues getContentValues(){
        ContentValues values = new ContentValues();
        values.put(UserEntry.COLUMN_NAME, username);
        return values;
    }
}
