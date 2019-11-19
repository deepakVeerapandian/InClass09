package com.example.inclass09;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SourceAdapter extends ArrayAdapter<Email> {

    public SourceAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Email> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Email source = getItem(position);
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_email,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.tv_subject = (TextView) convertView.findViewById(R.id.txtSubject);
            viewHolder.tv_date = (TextView) convertView.findViewById(R.id.txtDate);
            viewHolder.img_delete = (ImageView) convertView.findViewById(R.id.imgDelete);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tv_subject.setText(source.subject);
        viewHolder.tv_date.setText(source.date);

        return  convertView;
    }

    private static class ViewHolder{
        TextView tv_subject;
        TextView tv_date;
        ImageView img_delete;
    }
}

