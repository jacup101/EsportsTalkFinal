package com.jacup101.esportstalk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CommunityActivity extends AppCompatActivity {

    private String communityID;

    private ImageView coverPhoto;
    private ImageView logo;
    private FloatingActionButton addPostButton;
    private Button followButton;
    private ImageButton homeButton;
    private TextView nameText;

    private RecyclerView recyclerView;
    int orientation;
    private User user;

    private DatabaseHelper databaseHelper;

    private long followNum;

    List<Post> posts;
    PostAdapter adapter;

    CommunityViewModel viewModel;


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
        setContentView(R.layout.activity_community);

        orientation = this.getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_PORTRAIT) {
            nameText = findViewById(R.id.textView_communityName);
            coverPhoto = findViewById(R.id.imageView_communityCover);
            logo = findViewById(R.id.imageView_communityLogo);
            recyclerView = findViewById(R.id.recyclerView_community);
            addPostButton = findViewById(R.id.button_communityAddPost);
            followButton = findViewById(R.id.button_communityFollow);
            homeButton = findViewById(R.id.imageButton_communityHome);

        } else if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
            nameText = findViewById(R.id.textView_communityNameLand);
            coverPhoto = findViewById(R.id.imageView_communityCoverLand);
            logo = findViewById(R.id.imageView_communityLogoLand);
            recyclerView = findViewById(R.id.recyclerView_communityLand);
            addPostButton = findViewById(R.id.button_communityAddPostLand);
            followButton = findViewById(R.id.button_communityFollowLand);
            homeButton = findViewById(R.id.imageButton_communityHomeLand);

        }


        user = ((EsportsTalkApplication) this.getApplication()).getGlobalUser();
        Log.d("user_community",user.getUsername() + ", " + user.getFollowed());

        //can assume that a community has been passed in intent
        communityID = getIntent().getStringExtra("community");


        homeButton.setOnClickListener(v -> goHome());



        viewModel = (CommunityViewModel) new ViewModelProvider(this).get(CommunityViewModel.class);


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

        ArrayList<String > arr = new ArrayList<>();
        arr.add(communityID);
        databaseHelper.loadSpecifiedToView(arr,viewModel.getPostList());

        if(viewModel.getCommunityInfo().getValue()!=null) {
            String[] strings = viewModel.getCommunityInfo().getValue();
            Picasso.get().load(strings[0]).into(logo);
            Picasso.get().load(strings[1]).into(coverPhoto);

            nameText.setText(strings[2]);
            String follow = "Follow";
            if(user.getFollowed() != null && user.getFollowed().contains(communityID)) {
                follow = "Followed";
            }
            followNum = Long.parseLong(strings[3]);

            followButton.setText(follow + " (" + strings[3] + ")");
        }




        viewModel.getCommunityInfo().observe(this, new Observer<String[]>() {
            @Override
            public void onChanged(String[] strings) {
                if(strings != null) {
                    Picasso.get().load(strings[0]).into(logo);
                    Picasso.get().load(strings[1]).into(coverPhoto);

                    nameText.setText(strings[2]);
                    String follow = "Follow";
                    if(user.getFollowed() != null && user.getFollowed().contains(communityID)) {
                        follow = "Followed";
                    }
                    followNum = Long.parseLong(strings[3]);

                    followButton.setText(follow + " (" + strings[3] + ")");

                }
            }
        });
        databaseHelper.getCommunityProperties(viewModel.getCommunityInfo(),communityID);
        addPostButton.setOnClickListener( v-> startNewPost());

        if(user.getFollowed()!=null) followButton.setOnClickListener(v -> follow());


    }



    private void startNewPost() {
        if(!user.getUsername().equals("USER_L0GGED_0UT")) {
            Intent intent = new Intent(this, NewPostActivity.class);
            intent.putExtra("community", communityID);
            startActivity(intent);
        } else {
            Toast.makeText(this,"Please log in to perform that action",Toast.LENGTH_SHORT).show();
        }
    }
    private void goHome() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    private void follow() {
        if(!user.getUsername().equals("USER_L0GGED_0UT")) {
            if (user.getFollowed() != null) {
                if (user.getFollowed().contains(communityID)) {
                    databaseHelper.followCommunity(communityID, user.getUsername(), user, -1);
                    followNum--;
                    followButton.setText("Follow" + " (" + followNum + ")");
                } else {
                    databaseHelper.followCommunity(communityID, user.getUsername(), user, 1);
                    followNum++;
                    followButton.setText("Followed" + " (" + followNum + ")");
                }
            }
        } else {
            Toast.makeText(this,"Please log in to perform that action",Toast.LENGTH_SHORT).show();
        }
    }


}
