package com.jacup101.esportstalk;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class HomeFragmentViewModel extends ViewModel {
    //PostSharedViewModel provides a unified location to store the posts that are to be displayed on screen
    private MutableLiveData<String> selection = new MutableLiveData<>();

    public String getSelectionString() {
        return selection.getValue();
    }

    public MutableLiveData<String> getSelection() {
        return selection;
    }

    public void setSelection(String name) {
        this.selection.setValue(name);
    }

    private MutableLiveData<List<Post>> postList = new MutableLiveData<>();

    public void setPostList(List<Post> posts) {
        this.postList.setValue(posts);
    }
    public MutableLiveData<List<Post>> getPostList() {
        return postList;
    }


}
