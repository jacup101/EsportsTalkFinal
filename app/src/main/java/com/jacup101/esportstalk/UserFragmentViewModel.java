package com.jacup101.esportstalk;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class UserFragmentViewModel extends ViewModel {
    //PostSharedViewModel provides a unified location to store the posts that are to be displayed on screen
    private MutableLiveData<String> name = new MutableLiveData<>();

    public String getStringName() {
        return name.getValue();
    }

    public MutableLiveData<String> getName() {
        return name;
    }

    public void setName(String name) {
        this.name.setValue(name);
    }

    private MutableLiveData<List<Post>> postList = new MutableLiveData<>();

    public void setPostList(List<Post> posts) {
        this.postList.setValue(posts);
    }
    public MutableLiveData<List<Post>> getPostList() {
        return postList;
    }


}
