package ca.uwaterloo.magic.goodhikes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

    EditText register_name = (EditText) findViewById(R.id.register_name);
    EditText register_email = (EditText) findViewById(R.id.register_email);
    EditText register_password = (EditText) findViewById(R.id.register_password);
    Button register = (Button) findViewById(R.id.btn_register);
    TextView link_login = (TextView) findViewById(R.id.link_login);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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
                    UserRegister(name, email, password);
                } else {
                    Toast.makeText(RegisterActivity.this, "Please fill in all credentials!", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }


    private void UserRegister (final String name, final String email, final String password) {

    }

}
