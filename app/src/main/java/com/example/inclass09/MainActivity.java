package com.example.inclass09;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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

public class MainActivity extends AppCompatActivity {

    private EditText et_email;
    private EditText et_password;
    private Button btn_login;
    private Button btn_signUp;

    public OkHttpClient client;
    SharedPreferences mPrefs;
    String token = "";
    String status = "";
    public static String userName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Mailer");

        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        btn_login = findViewById(R.id.btnLogin);
        btn_signUp = findViewById(R.id.btnSignUp);

        mPrefs = getSharedPreferences("mySharedPref", Context.MODE_PRIVATE);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int errorFlag=0;
                String email = et_email.getText().toString();
                String password = et_password.getText().toString();
                if(email.equals(""))
                {
                    et_email.setError("Enter email");
                    errorFlag=1;

                }
                if(password.equals("")|| password.length()<6)
                {
                    et_password.setError("Enter a valid password");
                    errorFlag=1;

                }
                if(errorFlag==0)
                {
                    client = new OkHttpClient();
                    try {
                        RequestBody formBody = new FormBody.Builder()
                                .add("email", email)
                                .add("password", password)
                                .build();

                        if(isConnected())
                            run(formBody);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
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

    public static void showToastMessage(final Context context,
                                                  final String msg) {
        if (context != null && msg != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void run(RequestBody formBody) throws Exception {

        Request request = new Request.Builder()
                .url("http://ec2-18-234-222-229.compute-1.amazonaws.com/api/login")
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
                    JSONObject root = new JSONObject(responseBody.string());
                    if (!response.isSuccessful()){
                        status = root.getString("status");
                        String msg = root.getString("message");
                        showToastMessage(getApplicationContext(), msg);
                    }

                    if(responseBody != null){
                        token = root.getString("token");
                        status = root.getString("status");

                        SharedPreferences.Editor prefsEditor = mPrefs.edit();
                        prefsEditor.putString("token", token);
                        prefsEditor.commit();
                        if(status.equals("ok")){
                            showToastMessage(getApplicationContext(), "Login Successful");
                            Intent intent = new Intent(MainActivity.this, HomeScreenActivity.class);
                            String fullName = root.getString("user_fname") + " " + root.getString("user_lname");
                            userName = fullName;
                            intent.putExtra("userName", fullName);
                            startActivity(intent);
                            finish();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
