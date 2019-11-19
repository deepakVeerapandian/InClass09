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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HomeScreenActivity extends AppCompatActivity {
    private ListView lv_inbox;
    private ImageView img_addEmail;
    private ImageView img_logout;
    private TextView tv_title;

    SharedPreferences mPrefs;
    String token = "";
    String status = "";
    ArrayList<Email> emailList = new ArrayList<>();
    OkHttpClient client;
    SourceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        setTitle("Inbox");

        lv_inbox = findViewById(R.id.listViewInbox);
        img_addEmail = findViewById(R.id.imgCreateEmail);
        img_logout = findViewById(R.id.imgLogout);
        tv_title = findViewById(R.id.txtUserTitle);

        adapter = new SourceAdapter(getBaseContext(),R.layout.layout_email, emailList);
        lv_inbox.setAdapter(adapter);

        tv_title.setText(MainActivity.userName);

        mPrefs = getSharedPreferences("mySharedPref", Context.MODE_PRIVATE);
        token = mPrefs.getString("token","");
        try {
            if(isConnected()) {
                run();
                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        img_addEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeScreenActivity.this,CreateNewEmailActivity.class);
                startActivity(intent);
            }
        });

        img_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                prefsEditor.clear();
                prefsEditor.commit();

                Intent intent = new Intent(HomeScreenActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        lv_inbox.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(HomeScreenActivity.this,DisplayActivity.class);
                intent.putExtra("email_details", emailList.get(position));
                startActivity(intent);
            }
        });
    }

    public void run() throws Exception {

        Request request = new Request.Builder()
                .url("http://ec2-18-234-222-229.compute-1.amazonaws.com/api/inbox")
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
                            JSONArray message = root.getJSONArray("messages");
                            for(int i = 0; i < message.length(); i++){
                                JSONObject msg = message.getJSONObject(i);
                                Email email = new Email();
                                email.subject = (msg.getString("subject")) != null? msg.getString("subject"):"";
                                email.date = (msg.getString("created_at")) != null? msg.getString("created_at"):"";
                                email.emailMsg = (msg.getString("message")) != null? msg.getString("message"):"";
                                email.senderFirstName = (msg.getString("sender_fname")) != null? msg.getString("sender_fname"):"";
                                email.senderLastName = (msg.getString("sender_lname")) != null? msg.getString("sender_lname"):"";
                                email.email_msg_id = Integer.parseInt(msg.getString("id"));

                                emailList.add(email);
                            }

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
}
