package com.jacup101.esportstalk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PostActivity extends AppCompatActivity {

    private long id;

    private TextView title;
    private TextView userText;
    private TextView content;
    private TextView community;
    private TextView date;

    private User user;

    private ImageView imageView;
    private RecyclerView recyclerView;

    private YouTubePlayerView youTubePlayerView;

    private EditText commentInput;
    private Button commentAdd;
    private ImageButton homeButton;

    private int orientation;

    private PostActivityViewModel viewModel;

    private DatabaseHelper databaseHelper;
    private List<Comment> comments;
    private CommentAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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
        setContentView(R.layout.activity_post);

        Intent intent = getIntent();

        id = intent.getLongExtra("id",1);

        user = ((EsportsTalkApplication) this.getApplication()).getGlobalUser();

        orientation = this.getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_PORTRAIT) {
            title = findViewById(R.id.textView_postATitle);
            content = findViewById(R.id.textView_postAContent);
            userText = findViewById(R.id.textView_postAUser);
            community = findViewById(R.id.textView_postACommunity);
            date = findViewById(R.id.textView_postADate);
            imageView = findViewById(R.id.imageView_postAImage);
            youTubePlayerView = findViewById(R.id.youtubePlayer_postA);
            commentInput = findViewById(R.id.editText_commentPostA);
            commentAdd = findViewById(R.id.button_postAComment);
            recyclerView = findViewById(R.id.recyclerView_postA);
            homeButton = findViewById(R.id.imageButton_postAHome);
        } else if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
            title = findViewById(R.id.textView_postATitleLand);
            content = findViewById(R.id.textView_postAContentLand);
            userText = findViewById(R.id.textView_postAUserLand);
            community = findViewById(R.id.textView_postACommunityLand);
            date = findViewById(R.id.textView_postADateLand);
            imageView = findViewById(R.id.imageView_postAImageLand);
            youTubePlayerView = findViewById(R.id.youtubePlayer_postALand);
            commentInput = findViewById(R.id.editText_commentPostALand);
            commentAdd = findViewById(R.id.button_postACommentLand);
            recyclerView = findViewById(R.id.recyclerView_postALand);
            homeButton = findViewById(R.id.imageButton_postAHomeLand);
        }
        homeButton.setOnClickListener(v -> goHome());


        viewModel = (PostActivityViewModel) new ViewModelProvider(this).get(PostActivityViewModel.class);

        String[] postUpload = new String[9];
        //type, title, user, content, community, date, imgurl, vidurl, comment
        if(intent.getStringExtra("type") != null) {
            postUpload[0] = intent.getStringExtra("type");
            postUpload[1] = intent.getStringExtra("title");
            postUpload[2] = intent.getStringExtra("user");
            postUpload[3] = intent.getStringExtra("content");
            postUpload[4] = intent.getStringExtra("community");
            postUpload[5] = intent.getStringExtra("date");
            postUpload[6] = intent.getStringExtra("imgurl");
            postUpload[7] = intent.getStringExtra("vidid");
            postUpload[8] = intent.getStringExtra("commentString");
            viewModel.setPostInfo(postUpload);
        }

        final String[] postInfo = viewModel.getPostInfoArray();
        title.setText(postInfo[1]);

        userText.setText(postInfo[2]);


        content.setText(postInfo[3]);


        community.setText(postInfo[4]);


        date.setText(postInfo[5]);


        if(postInfo[0].equals("image") && postInfo[6] != null) {
            imageView.setAdjustViewBounds(true);
            Picasso.get().load(Uri.parse(postInfo[6])).into(imageView);
        }


        if(postInfo[0].equals("video") && postInfo[7] != null) {
            youTubePlayerView.setVisibility(View.VISIBLE);
            youTubePlayerView.getYouTubePlayerWhenReady(youTubePlayer -> {
                youTubePlayer.cueVideo(postInfo[7],0);
                // do stuff with it
            });
        } else {
            youTubePlayerView.setVisibility(View.GONE);
        }


        community.setOnClickListener(v -> launchCommunity());
        userText.setOnClickListener(v -> launchUser());


        String commentString = postInfo[8];
        parseCommentsFromString(commentString);
        adapter = new CommentAdapter(comments,this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        commentAdd.setOnClickListener(v->addComment(v));


        databaseHelper = new DatabaseHelper(this);

    }
    public void launchCommunity() {
        Intent intent = new Intent(this,CommunityActivity.class);
        intent.putExtra("community", community.getText().toString());
        startActivity(intent);

    }
    public void launchUser() {
        Intent intent = new Intent(this,UserActivity.class);
        intent.putExtra("user",userText.getText().toString());
        startActivity(intent);
    }

    public void parseCommentsFromString(String commentString) {
        this.comments = new ArrayList<Comment>();
        while(commentString.indexOf("$E") != -1) {
            String sub = commentString.substring(commentString.indexOf("$CM"),commentString.indexOf("$E")+2);
            Log.d("substring_attempt", sub);
            String user = sub.substring(sub.indexOf("$U:") + 3,sub.indexOf("$P:"));
            String commentText = sub.substring(sub.indexOf("$P:") + 3,sub.indexOf("$E"));
            Comment comment = new Comment(user, commentText, this.id);
            comments.add(comment);
            commentString = commentString.substring(commentString.indexOf("$E") + 2);
        }


    }
    public void addComment(View v) {
        //TODO: Bar disallowed text
        if(!user.getUsername().equals("USER_L0GGED_0UT")) {
            if(!checkEmpty()) {
                databaseHelper.addComment("" + id,commentInput.getText().toString(), user.getUsername());
                comments.add(new Comment(user.getUsername(),commentInput.getText().toString(), id));
                adapter.notifyDataSetChanged();
                commentInput.setText("");
            }
        } else {
            Toast.makeText(this,"Please log in to perform that action",Toast.LENGTH_SHORT).show();
        }


    }
    private boolean checkEmpty() {
        boolean isEmpty = TextUtils.isEmpty(commentInput.getText());
        if(isEmpty) {
            Toast.makeText(this, "Missing fields", Toast.LENGTH_SHORT).show();
        }
        return isEmpty;
    }
    private void goHome() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
