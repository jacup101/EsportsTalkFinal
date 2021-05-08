package com.jacup101.esportstalk;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class SearchActivityViewModel extends ViewModel {
    //PostSharedViewModel provides a unified location to store the posts that are to be displayed on screen
    private MutableLiveData<List<SearchResult>> shareList = new MutableLiveData<>();

    private MutableLiveData<String> searchText = new MutableLiveData<>();
    private MutableLiveData<String> type = new MutableLiveData<>();

    public void setShareList(List<SearchResult> searchResults) {
        this.shareList.setValue(searchResults);
    }
    public MutableLiveData<List<SearchResult>> getShareList() {
        return shareList;
    }

    public MutableLiveData<String> getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText.setValue(searchText);
    }

    public MutableLiveData<String> getType() {
        return type;
    }

    public void setType(String type) {
        this.type.setValue(type);
    }
}
