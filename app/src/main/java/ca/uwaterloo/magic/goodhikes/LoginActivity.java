package ca.uwaterloo.magic.goodhikes;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    EditText user_email = (EditText)findViewById(R.id.user_email);
    EditText user_password = (EditText)findViewById(R.id.user_password);
    Button btn_login = (Button) findViewById(R.id.btn_login);
    TextView link_register = (TextView) findViewById(R.id.link_register);
    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        pDialog = new ProgressDialog(this);


        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = user_email.getText().toString();
                String password = user_password.getText().toString();
                if (!email.isEmpty() && !password.isEmpty()) {
                    checkLogin(email, password);
                } else {
                    Toast.makeText(getApplicationContext(), "Please fill in all credentials!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        link_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                finish();
            }
        });

    }

    /** Verify user login details **/
    private void checkLogin(final String email, final String password) {

    }

}
