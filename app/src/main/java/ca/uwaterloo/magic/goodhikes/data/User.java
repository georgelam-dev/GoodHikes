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

public class User {
    private String name;
    private String email;
    private String ID;
    private String image;

    public User() {}

    public User(String ID, String name, String email, String image) {
        this.name = name;
        this.email = email;
        this.ID = ID;
        this.image = image;
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
    public void setImage(String image) { this.image = image; }
    public String getImage() { return image; }

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
