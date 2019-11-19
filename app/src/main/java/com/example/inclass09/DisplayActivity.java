package com.example.inclass09;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DisplayActivity extends AppCompatActivity {
    private TextView tv_senderValue;
    private TextView tv_subjectValue;
    private TextView tv_messageValue;
    private TextView tv_createdAtValue;
    private Button btn_close;
    //private String sowmya;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        setTitle("Display email");

        tv_senderValue=findViewById(R.id.tv_senderValue);
        tv_subjectValue=findViewById(R.id.tv_subjectValue);
        tv_messageValue=findViewById(R.id.tv_messageVlaue);
        tv_createdAtValue=findViewById(R.id.tv_createdAtValue);
        btn_close=findViewById(R.id.btn_close);
        if(getIntent().getExtras()!=null)
        {
            Email e= (Email) getIntent().getExtras().getSerializable("email_details");
            tv_senderValue.setText(e.senderFirstName+" "+e.senderLastName);
            tv_subjectValue.setText(e.subject);
            tv_createdAtValue.setText(e.date);
            tv_messageValue.setText(e.emailMsg);
        }
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(DisplayActivity.this,HomeScreenActivity.class);
//                startActivity(i);
                finish();
            }
        });


    }
}
