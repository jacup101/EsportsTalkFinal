package com.jacup101.esportstalk;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class FragmentUser extends Fragment {

    private View view;

    User user;

    List<Post> posts;
    PostAdapter adapter;
    TextView userName;
    RecyclerView recyclerView;

    int orientation;

    RecyclerView followedRecyclerView;

    UserFragmentViewModel viewModel;

    DatabaseHelper database;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user, container, false);

        database = new DatabaseHelper(getContext());

        orientation = getContext().getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView = view.findViewById(R.id.recyclerView_userFPosts);
            userName = view.findViewById(R.id.textView_userFName);
            followedRecyclerView = view.findViewById(R.id.recyclerView_userFFollowed);
        } else if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recyclerView = view.findViewById(R.id.recyclerView_userFPostsLand);
            userName = view.findViewById(R.id.textView_userFNameLand);
            followedRecyclerView = view.findViewById(R.id.recyclerView_userFFollowedLand);
        }

        user = ((EsportsTalkApplication) getActivity().getApplication()).getGlobalUser();

        viewModel = (UserFragmentViewModel) new ViewModelProvider(requireActivity()).get(UserFragmentViewModel.class);



        userName.setText(user.getUsername());

        //Load in posts / viewmodel
        posts = new ArrayList<Post>();
        if(viewModel.getPostList() == null) viewModel.setPostList(posts);
        adapter = new PostAdapter(posts, getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));



        viewModel.getPostList().observe(getViewLifecycleOwner(), new Observer<List<Post>>() {

            @Override
            public void onChanged(List<Post> postList) {
                if(postList != null) {
                    posts = postList;
                    adapter.updateAdapter(posts);
                }
            }
        });
        //Load posts back
        database.loadUserPosts(user.getUsername(),viewModel.getPostList());


        FollowedAdapter fAdapter = new FollowedAdapter(parseFollowedFromUser(),getContext());
        followedRecyclerView.setAdapter(fAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        if(orientation == Configuration.ORIENTATION_PORTRAIT) layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        followedRecyclerView.setLayoutManager(layoutManager);



        return view;

    }


    private List<String> parseFollowedFromUser() {
        ArrayList<String> list = new ArrayList<String>();
        String followed = user.getFollowed();
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
