package com.example.inclass09;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SignUpActivity extends AppCompatActivity {

    EditText et_firstName;
    EditText et_lastName;
    EditText et_email;
    EditText et_password;
    EditText et_repeatPassword;
    private Button btn_cancel;
    private Button btn_signUp;

    public OkHttpClient client;
    SharedPreferences mPrefs;
    String token = "";
    String status = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        setTitle("Sign Up");

        et_email = findViewById(R.id.et_email_signup);
        et_password = findViewById(R.id.et_password_signup);
        et_firstName = findViewById(R.id.et_firstName);
        et_lastName = findViewById(R.id.et_lastName);
        et_repeatPassword = findViewById(R.id.et_repeatPassord_signup);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_signUp = findViewById(R.id.btn_sign_signup);

        mPrefs = getSharedPreferences("mySharedPref", Context.MODE_PRIVATE);

        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = et_firstName.getText().toString();
                String lastName = et_lastName.getText().toString();
                String email = et_email.getText().toString();
                String password = et_password.getText().toString();
                String repeatPassword = et_repeatPassword.getText().toString();
                int errorFlag=0;
                if(firstName.equals(""))
                {
                    et_firstName.setError("Enter a valid FirstName");
                    errorFlag=1;
                }
                if (lastName.equals(""))
                {
                    et_lastName.setError("Enter a valid LastName ");
                    errorFlag=1;
                }
                if(email.equals(""))
                {
                    et_email.setError("Enter a valid Email");
                    errorFlag=1;
                }
                if(password.equals("")||password.length()<6)
                {
                    et_password.setError("Enter a valid password");
                    errorFlag=1;
                }
                if(errorFlag==0)
                {
                    if(password.equals(repeatPassword)){
                        client = new OkHttpClient();
                        try {
                            RequestBody formBody = new FormBody.Builder()
                                    .add("email", email)
                                    .add("password", password)
                                    .add("fname", firstName)
                                    .add("lname", lastName)
                                    .build();

                            if(isConnected())
                                run(formBody);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        Toast.makeText(SignUpActivity.this, "Password and repeat password should match", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected() ||
                (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                        && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
            Toast.makeText(getApplicationContext(), "No Internet Connected", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    public void run(RequestBody formBody) throws Exception {

        Request request = new Request.Builder()
                .url("http://ec2-18-234-222-229.compute-1.amazonaws.com/api/signup")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    JSONObject root = new JSONObject(responseBody.string());
                    if (!response.isSuccessful()){
                        status = root.getString("status");
                        String msg = root.getString("message");
                        MainActivity.showToastMessage(getBaseContext(), msg);
                    }

                    if(responseBody != null){
                        token = root.getString("token");
                        status = root.getString("status");

                        SharedPreferences.Editor prefsEditor = mPrefs.edit();
                        prefsEditor.putString("token", token);
                        prefsEditor.commit();
                        if(status.equals("ok")){
                            Intent intent = new Intent(getBaseContext(), HomeScreenActivity.class);
                            String fullName = root.getString("user_fname") + " " + root.getString("user_lname");
                            intent.putExtra("userName", fullName);
                            startActivity(intent);
                            finish();
                            MainActivity.showToastMessage(getBaseContext(),"Sign up successful");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
