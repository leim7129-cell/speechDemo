package com.iflytek.voicedemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class LogActivity extends Activity {
    private ListView mListView;
    private LogSQLiteHelper mDbHelper;
    private List<Map<String, String>> mLogList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_log);

        mListView = findViewById(R.id.log_list_view);
        mDbHelper = new LogSQLiteHelper(this);
        mLogList = mDbHelper.getAllLogs();

        mListView.setAdapter(new LogAdapter());
    }

    private class LogAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mLogList.size();
        }

        @Override
        public Object getItem(int position) {
            return mLogList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(LogActivity.this).inflate(R.layout.item_log, parent, false);
            }

            Map<String, String> log = mLogList.get(position);
            TextView tvTime = convertView.findViewById(R.id.log_time);
            TextView tvScore = convertView.findViewById(R.id.log_score);
            TextView tvError = convertView.findViewById(R.id.log_error);

            tvTime.setText("时间: " + log.get("time"));
            tvScore.setText("得分: " + log.get("score"));
            tvError.setText("报错: " + log.get("error"));

            return convertView;
        }
    }
}
