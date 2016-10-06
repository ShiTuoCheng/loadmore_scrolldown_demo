package com.pratap.endlessrecyclerview;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private TextView tvEmptyView;
    private RecyclerView mRecyclerView;
    private DataAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private ExecutorService pool = Executors.newCachedThreadPool();

    private List<Student> studentList;


    protected Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tvEmptyView = (TextView) findViewById(R.id.empty_view);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        studentList = new ArrayList<Student>();
        handler = new Handler();
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Android Students");

        }

        pool.execute(loadData());

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView



    }


    // load initial data
    private Runnable loadData() {

        /*
        for (int i = 1; i <= 20; i++) {
            studentList.add(new Student("Student " + i, "androidstudent" + i + "@gmail.com"));

        }
        */

        return new Runnable() {
            @Override
            public void run() {
                final HttpURLConnection[] connection = new HttpURLConnection[1];
                final InputStream[] inputStream = new InputStream[1];
                final String api = "https://api.dribbble.com/v1/shots/2995418/likes?access_token=aef92385e190422a5f27496da51e9e95f47a18391b002bf6b1473e9b601e6216";

                try {
                    connection[0] = (HttpURLConnection)new URL(api).openConnection();
                    connection[0].setRequestMethod("GET");
                    connection[0].connect();

                    inputStream[0] = connection[0].getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream[0]));
                    String line;
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((line = bufferedReader.readLine())!= null){
                        stringBuilder.append(line);
                    }

                    JSONArray jsonArray = new JSONArray(stringBuilder.toString());

                    for (int i=0; i<jsonArray.length();i++){
                        Student student = new Student();
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        JSONObject userJson = jsonObject.getJSONObject("user");
                        String user_name = userJson.getString("name");
                        student.setName(user_name);
                        studentList.add(student);
                        Log.d("size", String.valueOf(studentList.size()));
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            mLayoutManager = new LinearLayoutManager(getBaseContext());


                            // use a linear layout manager
                            mRecyclerView.setLayoutManager(mLayoutManager);

                            // create an Object for Adapter
                            mAdapter = new DataAdapter(studentList, mRecyclerView);

                            // set the adapter object to the Recyclerview
                            mRecyclerView.setAdapter(mAdapter);

                            mAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                                @Override
                                public void onLoadMore() {
                                    //add null , so the adapter will check view_type and show progress bar at bottom
                                    studentList.add(null);
                                    mAdapter.notifyItemInserted(studentList.size() - 1);

                                    pool.execute(new Runnable() {
                                        @Override
                                        public void run() {

                                            String more_api = "https://api.dribbble.com/v1/shots/2995418/likes?page=2&access_token=aef92385e190422a5f27496da51e9e95f47a18391b002bf6b1473e9b601e6216";

                                            try {
                                                connection[0] = (HttpURLConnection)new URL(more_api).openConnection();

                                                connection[0].setRequestMethod("GET");
                                                connection[0].connect();

                                                inputStream[0] = connection[0].getInputStream();
                                                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream[0]));
                                                String line;
                                                StringBuilder stringBuilder = new StringBuilder();

                                                while ((line = bufferedReader.readLine())!= null){
                                                    stringBuilder.append(line);
                                                }

                                                final JSONArray more_jsonArray = new JSONArray(stringBuilder.toString());

                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        studentList.remove(studentList.size() - 1);
                                                        mAdapter.notifyItemRemoved(studentList.size());
                                                        //add items one by one
                                                        int start = studentList.size();
                                                        int end = start + more_jsonArray.length();

                                                        for (int i =0; i < more_jsonArray.length(); i++) {


                                                            Student student = new Student();
                                                            JSONObject jsonObject ;
                                                            try {
                                                                jsonObject = more_jsonArray.getJSONObject(i);
                                                                JSONObject userJson = jsonObject.getJSONObject("user");
                                                                String user_name = userJson.getString("name");
                                                                student.setName(user_name);
                                                                studentList.add(student);
                                                                Log.d("size", String.valueOf(studentList.size()));
                                                                //studentList.add(new Student("Student " + i, "AndroidStudent" + i + "@gmail.com"));
                                                                mAdapter.notifyItemInserted(studentList.size());
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                        mAdapter.setLoaded();
                                                    }
                                                });
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }


                                        }
                                    });
                /*
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //   remove progress item
                        studentList.remove(studentList.size() - 1);
                        mAdapter.notifyItemRemoved(studentList.size());
                        //add items one by one
                        int start = studentList.size();
                        int end = start + 20;

                        for (int i = start + 1; i <= end; i++) {
                            studentList.add(new Student("Student " + i, "AndroidStudent" + i + "@gmail.com"));
                            mAdapter.notifyItemInserted(studentList.size());
                        }
                        mAdapter.setLoaded();
                       //or you can add all at once but do not forget to call mAdapter.notifyDataSetChanged();
                    }
                }, 2000);

                */
                                }
                            });
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            };
        }

    }
