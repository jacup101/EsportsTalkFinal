    package com.jacup101.esportstalk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity {

    private String userID;

    private DatabaseHelper databaseHelper;

    List<String> followed;

    List<Post> posts;
    PostAdapter adapter;

    UserActivityViewModel viewModel;
    int orientation;

    TextView userName;
    RecyclerView recyclerView;
    RecyclerView followedRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        SharedPreferences sharedPreferences = getSharedPreferences("com.jacup101.esportstalk.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        String viewMode = sharedPreferences.getString("viewmode","light");
        if(viewMode.equals("light")) {
            //do nothing
        } else if(viewMode.equals("dark")) {
            setTheme(R.style.DarkMode);
        } else if(viewMode.equals("auto")) {
            //TODO IMPLEMENT AUTO
        }

        setContentView(R.layout.activity_user);
        userID = getIntent().getStringExtra("user");

        orientation = this.getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView = findViewById(R.id.recyclerView_userAPosts);
            followedRecyclerView = findViewById(R.id.recyclerView_userAFollowed);
            userName = findViewById(R.id.textView_userAName);
        } else if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recyclerView = findViewById(R.id.recyclerView_userAPostsLand);
            followedRecyclerView = findViewById(R.id.recyclerView_userAFollowedLand);
            userName = findViewById(R.id.textView_userANameLand);
        }

        viewModel = (UserActivityViewModel) new ViewModelProvider(this).get(UserActivityViewModel.class);




        posts = new ArrayList<Post>();
        viewModel.setPostList(posts);
        adapter = new PostAdapter(posts, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        viewModel.getPostList().observe(this, new Observer<List<Post>>() {

            @Override
            public void onChanged(List<Post> postList) {
                if(postList != null) {
                    posts = postList;
                    adapter.updateAdapter(posts);
                }
            }
        });





        databaseHelper = new DatabaseHelper(this);

        databaseHelper.loadUserPosts(userID,viewModel.getPostList());



        viewModel.getUserInfo().observe(this, new Observer<String[]>() {
            @Override
            public void onChanged(String[] strings) {
                if(strings != null) {


                    userName.setText(strings[0]);
                    //TODO: LOAD FOLLOW INTO RECYCLER VIEW
                    followed = parseFollowedFromUser(strings[1]);
                    FollowedAdapter fAdapter = new FollowedAdapter(followed,UserActivity.this);
                    followedRecyclerView.setAdapter(fAdapter);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(UserActivity.this);
                    if(orientation == Configuration.ORIENTATION_PORTRAIT) layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    followedRecyclerView.setLayoutManager(layoutManager);

                }
            }
        });
        databaseHelper.getUserProperties(viewModel.getUserInfo(),userID);



    }

    private List<String> parseFollowedFromUser(String followed) {
        ArrayList<String> list = new ArrayList<String>();


        if(followed == null) {
            return list;
        }
        while(followed.indexOf(",") != -1) {
            list.add(followed.substring(0,followed.indexOf(",")));
            followed = followed.substring(followed.indexOf(",")+1);
        }
        return list;
    }
}
