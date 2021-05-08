package com.jacup101.esportstalk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    EditText searchBar;
    Button buttonSearch;

    TabLayout tabLayoutSearch;
    RecyclerView recyclerView;

    String tabSelect = "Posts";

    DatabaseHelper databaseHelper;

    ImageButton homeButton;

    int orientation;
    SearchActivityViewModel viewModel;
    List<SearchResult> results;
    SearchResultAdapter adapter;

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

        setContentView(R.layout.activity_search);
        viewModel = (SearchActivityViewModel) new ViewModelProvider(this).get(SearchActivityViewModel.class);

        orientation = this.getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_PORTRAIT) {
            searchBar = findViewById(R.id.editText_searchBar);
            buttonSearch = findViewById(R.id.button_searchGo);
            tabLayoutSearch = findViewById(R.id.tabLayout_search);
            recyclerView = findViewById(R.id.recyclerView_search);
            homeButton = findViewById(R.id.imageButton_searchHome);

        } else if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
            searchBar = findViewById(R.id.editText_searchBarLand);
            buttonSearch = findViewById(R.id.button_searchGoLand);
            tabLayoutSearch = findViewById(R.id.tabLayout_searchLand);
            recyclerView = findViewById(R.id.recyclerView_searchLand);
            homeButton = findViewById(R.id.imageButton_searchHomeLand);
        }





        tabLayoutSearch.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // called when tab selected
                tabSelect = tab.getText().toString();
                parseClick(tabSelect);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // called when tab unselected
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                tabSelect = tab.getText().toString();
                parseClick(tabSelect);
                // called when a tab is reselected
            }
        });

        buttonSearch.setOnClickListener(v -> parseClick(tabSelect));

        databaseHelper = new DatabaseHelper(this);

        homeButton.setOnClickListener(v -> goHome());
        results = new ArrayList<SearchResult>();
        viewModel.setShareList(results);
        adapter = new SearchResultAdapter(results, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        viewModel.getShareList().observe(this, new Observer<List<SearchResult>>() {

            @Override
            public void onChanged(List<SearchResult> resultList) {
                if(resultList != null) {
                    results = resultList;
                    adapter.updateAdapter(results);
                }
            }
        });
        if(viewModel.getSearchText().getValue()!=null) {
            searchBar.setText(viewModel.getSearchText().getValue());
        }
        if(viewModel.getType().getValue() == null) {
            parseClick("Posts");

        } else {
            parseTab(viewModel.getType().getValue());
            parseClick(viewModel.getType().getValue());
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        parseClick(tabSelect);
    }

    private void goHome() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    private void parseClick(String str) {
        if(str.equals("Posts")) {
            tabSelect = "Posts";
            databaseHelper.searchPostsList(viewModel.getShareList(),searchBar.getText().toString());
        }
        if(str.equals("Communities")) {
            tabSelect = "Communities";
            databaseHelper.searchCommunityList(viewModel.getShareList(),searchBar.getText().toString());
        }
        if(str.equals("Users")) {
            tabSelect = "Users";
            databaseHelper.searchUsersList(viewModel.getShareList(),searchBar.getText().toString());
        }

    }
    public void parseTab(String selection) {
        if(selection.equals("Posts")) {
            tabLayoutSearch.selectTab(tabLayoutSearch.getTabAt(0));
        }        if(selection.equals("Communities")) {
            tabLayoutSearch.selectTab(tabLayoutSearch.getTabAt(1));
        }        if(selection.equals("Users")) {
            tabLayoutSearch.selectTab(tabLayoutSearch.getTabAt(2));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        viewModel.setSearchText(searchBar.getText().toString());
        viewModel.setType(tabSelect);
    }
}
