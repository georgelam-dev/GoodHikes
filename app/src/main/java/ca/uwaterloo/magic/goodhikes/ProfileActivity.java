package ca.uwaterloo.magic.goodhikes;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import ca.uwaterloo.magic.goodhikes.data.User;
import ca.uwaterloo.magic.goodhikes.data.UserManager;

public class ProfileActivity extends AppCompatActivity {
    private ImageView profile_image;
    private TextView profile_user_name;
    private TextView profile_email;
    private UserManager userManager;
    private User user;
    private Button upload_image;
    private static final int FROM_GALLERY = 1;
    private Bitmap image;
    private String image_str;

    protected static final String LOG_TAG = "ProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /** Define variables **/
        userManager = new UserManager(getApplicationContext());
        user = userManager.getUser();
        profile_image = (ImageView) findViewById(R.id.profile_image);
        profile_user_name = (TextView) findViewById(R.id.profile_user_name);
        profile_email = (TextView) findViewById(R.id.profile_email);
        upload_image = (Button) findViewById(R.id.upload_picture);

        profile_user_name.setText(user.getUsername());
        profile_email.setText(user.getEmail());
        image_str = user.getImage();
        if (image_str != "") {
            /** decode image string and set profile image **/
            byte[] image_arr = Base64.decode(image_str, 0);
            profile_image.setImageBitmap(BitmapFactory.decodeByteArray(image_arr, 0, image_arr.length));

        }

        upload_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadProfileImage();

            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) return;
        if (requestCode == FROM_GALLERY)
            if (resultCode == RESULT_OK) {
                Uri selectedImage = data.getData();
                profile_image.setImageURI(selectedImage);
                try {
                    /* uri -> bitmap -> encoded string
                     * store string in sharedpreference */
                    image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    ByteArrayOutputStream byte_out = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG, 90, byte_out); //image is the bitmap object
                    image = Bitmap.createScaledBitmap(image, 640, 480, false);
                    byte[] byte_arr = byte_out.toByteArray();
                    String encoded = Base64.encodeToString(byte_arr, Base64.DEFAULT);
                    Log.d(LOG_TAG, "encoded string length: " + encoded.length());
                    String input = "uid=";
                    input += URLEncoder.encode(user.getId(), "UTF-8");
                    input += "&image_str=";
                    input += URLEncoder.encode(encoded, "UTF-8");
                    new SyncImage().execute(input);
                    userManager.setImage(encoded);
                    //Log.d(LOG_TAG, "set image str: " + userManager.getImage());
                } catch(Exception e) {
                    Log.d(LOG_TAG, e.getMessage());
                }
            }
    }

    private void uploadProfileImage() {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, FROM_GALLERY);
    }
    //TODO: let user upload picture

    /** sync profile picture to server **/
    private class SyncImage extends AsyncTask<String, Void, Boolean> {

        protected Boolean doInBackground(String... params) {
            // update user sharedpreference
            Boolean r = false;
            try {
                // Open Connection
                URL url = new URL("http://chelseahu.comlu.com/goodhikes_php/Upload_Image.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Send request
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(params[0]);
                writer.flush();
                writer.close();
                os.close();

                conn.connect();
                // Receive response as inputStream
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                // Convert inputstream to string
                String response = reader.readLine();
                Log.d("Response", response);
                JSONObject json = new JSONObject(response);
                String error = json.getString("error");
                if (error == "false") {
                    r = true;
                } else { r = false; }
            } catch (Exception e) {
                Log.d(LOG_TAG, e.getLocalizedMessage());
            }
            return r;
        }

        protected void onPostExecute(Boolean valid) {
            if (valid == true) {
                Toast.makeText(getApplicationContext(), "Successfully uploaded picture!", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getApplicationContext(), "Fail to upload picture!", Toast.LENGTH_SHORT).show();
            }
        }


    };

}
