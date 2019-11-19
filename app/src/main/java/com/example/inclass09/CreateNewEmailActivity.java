package com.example.inclass09;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class CreateNewEmailActivity extends AppCompatActivity {

    private EditText et_subject;
    private Spinner spinner_sender;
    private EditText et_email;
    private Button btn_send;
    private Button btn_cancel;

    SharedPreferences mPrefs;
    String token = "";
    String status = "";
    OkHttpClient client;
    ArrayAdapter<String> adapter;
    ArrayList<User> userList = new ArrayList<>();
    ArrayList<String> userNames = new ArrayList<>();
    String idd = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_email);
        setTitle("Create New Email");

        et_subject = findViewById(R.id.editTextSubjectEMAIL);
        spinner_sender = findViewById(R.id.spinnerSenderEMAIL);
        et_email = findViewById(R.id.editTextMultilineEMAIL);
        btn_cancel = findViewById(R.id.btnCancelEMAIL);
        btn_send = findViewById(R.id.btnSendEMAIL);

        mPrefs = getSharedPreferences("mySharedPref", Context.MODE_PRIVATE);
        token = mPrefs.getString("token","");

        try {
            if(isConnected()) {
                String x = "";
                new GetAPIAsync().execute(x);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String selectedSender = spinner_sender.getSelectedItem().toString();
                    int i = 0;
                    for(User x : userList){
                        String fullName = x.fname + " " + x.lname;
                        if(fullName.equals(selectedSender)){
                            idd = x.id+"";
                        }
                        i++;
                    }
                    RequestBody formBody = new FormBody.Builder()
                            .add("receiver_id", idd)
                            .add("subject", et_subject.getText().toString())
                            .add("message", et_email.getText().toString())
                            .build();

                    if(isConnected()) {
                        run(formBody);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                closeCreateEmail();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCreateEmail();
            }
        });

    }

    public void closeCreateEmail(){
        Intent intent = new Intent(CreateNewEmailActivity.this, HomeScreenActivity.class);
//        intent.putExtra("userName", userName);
//        startActivity(intent);
        finish();
    }

    public void run(RequestBody formBody) throws Exception {

        Request request = new Request.Builder()
                .url("http://ec2-18-234-222-229.compute-1.amazonaws.com/api/inbox/add")
                .header("Authorization","BEARER "+token)
                .post(formBody)
                .build();
        client = new OkHttpClient();

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
                        closeCreateEmail();
                    }

                    if(responseBody != null){
                        closeCreateEmail();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void runUser() throws Exception {

        Request request = new Request.Builder()
                .url("http://ec2-18-234-222-229.compute-1.amazonaws.com/api/users")
                .header("Authorization","BEARER "+token)
                .build();
        client = new OkHttpClient();

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
                        MainActivity.showToastMessage(getBaseContext(), msg);
                    }

                    if(responseBody != null){
                        status = root.getString("status");
                        if(status.equals("ok")){
                            JSONArray message = root.getJSONArray("users");
                            for(int i = 0; i < message.length(); i++){
                                JSONObject msg = message.getJSONObject(i);
                                User user = new User();
                                user.fname = (msg.getString("fname")) != null? msg.getString("fname"):"";
                                user.lname = (msg.getString("lname")) != null? msg.getString("lname"):"";
                                user.id = Integer.parseInt(msg.getString("id"));

                                userList.add(user);
                                userNames.add(user.fname + " "+ user.lname);
                            }

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private class GetAPIAsync extends AsyncTask<String, Void, String > {

        @Override
        protected void onPostExecute(final String songs) {
            try  {
                JSONObject root = new JSONObject(songs);
                status = root.getString("status");
                if(status.equals("ok")){
                    JSONArray message = root.getJSONArray("users");
                    for(int i = 0; i < message.length(); i++){
                        JSONObject msg = message.getJSONObject(i);
                        User user = new User();
                        user.fname = (msg.getString("fname")) != null? msg.getString("fname"):"";
                        user.lname = (msg.getString("lname")) != null? msg.getString("lname"):"";
                        user.id = Integer.parseInt(msg.getString("id"));

                        userList.add(user);
                        userNames.add(user.fname + " "+ user.lname);
                    }
                }

                adapter = new ArrayAdapter<String>(CreateNewEmailActivity.this, android.R.layout.simple_spinner_item, userNames);
                spinner_sender.setAdapter(adapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            String result = null;
            Request request = new Request.Builder()
                    .url("http://ec2-18-234-222-229.compute-1.amazonaws.com/api/users")
                    .header("Authorization","BEARER "+token)
                    .build();
            client = new OkHttpClient();
            Response response = null;
            try {
                response = client.newCall(request).execute();
                result = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }
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
}
