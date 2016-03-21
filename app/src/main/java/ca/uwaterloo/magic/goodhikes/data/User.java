package ca.uwaterloo.magic.goodhikes.data;

public class User {
    private String name;
    private String email;
    private String ID;

    public User() {}

    public User(String ID, String name, String email) {
        this.name = name;
        this.email = email;
        this.ID = ID;
    }

    public String getId() {return ID;}
    public void setId(String id) {this.ID = id;}
    public String getUsername(){
        return name;
    }
    public void setUsername(String name){
        this.name = name;
    }
    public String getEmail() { return email; }

    /*public ContentValues toContentValues(){
        ContentValues values = new ContentValues();
        values.put(UserEntry.COLUMN_USERNAME, name);
        return values;
    }*/

    /*public static User fromDBCursor(Cursor cursor){
        User user = new User();
        user.setId(cursor.getString(cursor.getColumnIndex(UserEntry._ID)));
        user.setUsername(cursor.getString(cursor.getColumnIndex(UserEntry.COLUMN_USERNAME)));
        return user;
    }*/
}
