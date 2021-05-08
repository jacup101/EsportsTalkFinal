package com.jacup101.esportstalk;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

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

public class FragmentHome extends Fragment {

    private View view;

    List<Post> posts;
    PostAdapter adapter;
    RecyclerView recyclerView;

    HomeFragmentViewModel viewModel;
    int orientation;
    DatabaseHelper database;
    TabLayout tabLayout;
    ImageButton searchButton;

    boolean isConnected;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        database = new DatabaseHelper(getContext());

        checkConnectivity();

        viewModel = (HomeFragmentViewModel) new ViewModelProvider(requireActivity()).get(HomeFragmentViewModel.class);



        orientation = getContext().getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_PORTRAIT) {
            tabLayout = view.findViewById(R.id.tabLayout_postSelect);
            recyclerView = view.findViewById(R.id.recyclerView_postFragment);
            searchButton = view.findViewById(R.id.imageButton_searchButton);
        } else if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
            tabLayout = view.findViewById(R.id.tabLayout_postSelectLand);
            recyclerView = view.findViewById(R.id.recyclerView_postFragmentLand);
            searchButton = view.findViewById(R.id.imageButton_searchButtonLand);
        }



        tabLayout.selectTab(tabLayout.getTabAt(1));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // called when tab selected
                String tabSelect = tab.getText().toString();
                parseClick(tabSelect);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // called when tab unselected
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                String tabSelect = tab.getText().toString();
                parseClick(tabSelect);
                // called when a tab is reselected
            }
        });


        searchButton.setOnClickListener(v -> startSearch());

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

        if(isConnected) {
            database.loadAllToLocalDB();
            if (viewModel.getSelectionString() == null) {
                database.loadAllToView(viewModel.getPostList());
                viewModel.setSelection("All");
            } else {
                parseTab(viewModel.getSelectionString());
            }
        } else {
            Toast.makeText(getContext(),"No internet connection detected",Toast.LENGTH_SHORT).show();
            database.loadAllFromLocalDB(viewModel.getPostList());
        }

        return view;

    }

    private void parseClick(String select) {
        checkConnectivity();
        if(!isConnected) {
            Toast.makeText(getContext(),"No internet connection detected",Toast.LENGTH_SHORT).show();
            viewModel.setSelection("All");
            database.loadAllFromLocalDB(viewModel.getPostList());
            return;
        }
        if(select.equals("All")) {
            database.loadAllToView(viewModel.getPostList());
            viewModel.setSelection("All");
        }
        if(select.equals("Followed")) {
            List<String> parsed = parseFollowedFromUser();
            if(parsed != null) {
                database.loadSpecifiedToView(parseFollowedFromUser(),viewModel.getPostList());
                viewModel.setSelection("Followed");

            } else {
                tabLayout.selectTab(tabLayout.getTabAt(1));
                Toast.makeText(getContext(),"No followed communities",Toast.LENGTH_SHORT).show();
                viewModel.setSelection("All");
                //database.loadAllToView(viewModel.getPostList(),adapter);
            }

        }
    }
    public void parseTab(String selection) {
        if(selection.equals("Followed")) {
            tabLayout.selectTab(tabLayout.getTabAt(0));
        }        if(selection.equals("All")) {
            tabLayout.selectTab(tabLayout.getTabAt(1));
        }
    }
    private void startSearch() {
        Intent intent = new Intent(getContext(),SearchActivity.class);
        startActivity(intent);
    }

    private List<String> parseFollowedFromUser() {
        ArrayList<String> list = new ArrayList<String>();
        User user = ((EsportsTalkApplication) getActivity().getApplication()).getGlobalUser();
        String followed = user.getFollowed();
        if(followed == null || followed.equals("")) {
            return null;
        }
        while(followed.indexOf(",") != -1) {
            list.add(followed.substring(0,followed.indexOf(",")));
            followed = followed.substring(followed.indexOf(",")+1);
        }
        return list;
    }
    private void checkConnectivity() {
        ConnectivityManager cm =  (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

}
