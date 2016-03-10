package ca.uwaterloo.magic.goodhikes.data;

import android.content.ContentValues;
import android.database.Cursor;

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

    public void setUsername(String username){
        this.username = username;
    }

    public ContentValues toContentValues(){
        ContentValues values = new ContentValues();
        values.put(UserEntry.COLUMN_USERNAME, username);
        return values;
    }

    public static User fromDBCursor(Cursor cursor){
        User user = new User();
        user.setId(cursor.getLong(cursor.getColumnIndex(UserEntry._ID)));
        user.setUsername(cursor.getString(cursor.getColumnIndex(UserEntry.COLUMN_USERNAME)));
        return user;
    }
}
