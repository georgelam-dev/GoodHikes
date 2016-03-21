package ca.uwaterloo.magic.goodhikes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import ca.uwaterloo.magic.goodhikes.data.User;
import ca.uwaterloo.magic.goodhikes.data.UserManager;

public class ProfileActivity extends AppCompatActivity {
    private ImageView profile_image;
    private TextView profile_user_name;
    private TextView profile_email;
    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /** Define variables **/
        userManager = new UserManager(getApplicationContext());
        User user = userManager.getUser();
        profile_image = (ImageView) findViewById(R.id.profile_image);
        profile_user_name = (TextView) findViewById(R.id.profile_user_name);
        profile_email = (TextView) findViewById(R.id.profile_email);

        profile_user_name.setText(String.format("Name: %s", user.getUsername()));
        profile_email.setText(String.format("Email: %s", user.getEmail()));
    }
    //TODO: let user upload picture
}
