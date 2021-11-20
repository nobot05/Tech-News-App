package com.nhk.technewsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<String> articleTitles, articleUrls;
    TextView txtJson;
    ProgressDialog pd;
    Button btnHit;
    ArrayList<Integer> idList;

    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        listView = (ListView) findViewById(R.id.listV);
        txtJson = (TextView) findViewById(R.id.txt_json);
        idList = new ArrayList<>();
        articleTitles = new ArrayList();
        articleUrls = new ArrayList();
        JsonTask task = new JsonTask();

//        String buffer = task.doInBackground(new String[]{"https://hacker-news.firebaseio.com/v0/topstories.json"});
//        try {
//            getArticlesFromString(buffer);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        ArrayList<Integer> invalidIds = new ArrayList<>();
//        for(int id: idList){
//            String url = "https://hacker-news.firebaseio.com/v0/item/"+id+".json?print=pretty";
//            buffer = task.doInBackground(new String[]{url});
//            try {
//                if(!getArticleDataFromString(buffer))
//                    invalidIds.add(id);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        for(int id: invalidIds){
//            idList.remove(idList.indexOf(id));
//        }


        //local database
        SQLiteDatabase db = this.openOrCreateDatabase("newsdb", MODE_PRIVATE, null);
//        db.execSQL("CREATE TABLE IF NOT EXISTS news (news_ID INT(8), news_title VARCHAR, news_url VARCHAR)");
//        Log.d("Articles sizes:",idList.size()+" "+articleTitles.size()+" "+articleUrls.size());
//        for(int i = 0; i < idList.size(); i++){
//            int ids = idList.get(i);
//            String titles = articleTitles.get(i);
//            String urls = articleUrls.get(i);
//            db.execSQL("INSERT INTO news(news_ID, news_title, news_url) VALUES(\""+ids+"\",\""+titles+"\",\""+urls+"\")");
//            Log.i("ids", "the id is " + ids);
//            Log.i("titles", "the title is " + titles);
//            Log.i("urls", "the url is " + urls);
//        }



//        db.execSQL("delete from "+ news);


        Cursor c = db.rawQuery("Select * from news", null);
        int newsIDIndex = c.getColumnIndex("news_ID");
        int newsTitleIndex = c.getColumnIndex("news_title");
        int newsUrlIndex = c.getColumnIndex("news_url");
        int index = 0;
        Log.i("Database size",c.getCount()+"");
        c.moveToFirst();
        while(c!= null && index<c.getCount()){
            Log.i("index of news",index+"");
            index++;
            idList.add(c.getInt(newsIDIndex));
            articleTitles.add(c.getString(newsTitleIndex));
            articleUrls.add(c.getString(newsUrlIndex));
            c.moveToNext();
        }









        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, articleTitles);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener( new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i("Person Selected", articleUrls.get(i));
//                Toast.makeText(getApplicationContext(), "Test " + articleUrls.get(i), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this, ArticleActivity.class);
                intent.putExtra("URL",articleUrls.get(i));
                startActivity(intent);
            }
        });


//        txtJson.setText(buffer);
    }


    void getArticlesFromString(String str) throws JSONException {
        Random r = new Random();
        ArrayList<Integer> used = new ArrayList<>();
        JSONArray idArray = (JSONArray) new JSONObject(new JSONTokener("{data:"+str+"}")).get("data");
        while(used.size()<20) {
            int index = r.nextInt(idArray.length());
            if (used.size() > 0 && used.contains(index)) {
                continue;
            }
            used.add(index);
            idList.add(idArray.getInt(index));
        }
//        txtJson.setText(idList.toString());
    }

    boolean getArticleDataFromString(String str) throws JSONException {
       JSONObject article = new JSONObject(str);
        if(!article.has("url"))
            return false;
        articleTitles.add(article.getString("title"));
        articleUrls.add(article.getString("url"));
        Log.e("FetchURL","fetch");
        return true;
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);
                }
                return buffer.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }
            txtJson.setText(result);
        }
    }


}