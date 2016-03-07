package ca.uwaterloo.magic.goodhikes;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class RegisterActivity extends AppCompatActivity {

    EditText register_name;
    EditText register_email;
    EditText register_password;
    Button register;
    TextView link_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        /** define variables **/
        register_name = (EditText) findViewById(R.id.register_name);
        register_email = (EditText) findViewById(R.id.register_email);
        register_password = (EditText) findViewById(R.id.register_password);
        register = (Button) findViewById(R.id.btn_register);
        link_login = (TextView) findViewById(R.id.link_login);

        link_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = register_name.getText().toString();
                String email = register_email.getText().toString();
                String password = register_password.getText().toString();

                if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                    try {
                        String input = "name=";
                        input += URLEncoder.encode(name, "UTF-8");
                        input += "&email=";
                        input += URLEncoder.encode(email, "UTF-8");
                        input += "&password=";
                        input += URLEncoder.encode(password, "UTF-8");
                        Log.d("Input for AsyncTask", input);
                        new UserRegister().execute(input);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Please fill in all credentials!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    /** Verify user login details **/
    private class UserRegister extends AsyncTask<String, Void, Boolean> {

        protected Boolean doInBackground(String... params) {
            Boolean r = false;
            try {
                // Open Connection
                URL url = new URL("http://chelseahu.comlu.com/goodhikes_php/Register.php");
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
                Log.d("InputStream", e.getLocalizedMessage());
            }
            return r;
        }

        protected void onPostExecute(Boolean valid) {
            if (valid == true) {
                Toast.makeText(getApplicationContext(), "Successfully registered!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Email is already registered!", Toast.LENGTH_SHORT).show();
            }
        }


    };

}
