package com.jacup101.esportstalk;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class PostActivityViewModel extends ViewModel {
    //PostSharedViewModel provides a unified location to store the posts that are to be displayed on screen
    private MutableLiveData<String[]> postInfo = new MutableLiveData<>();

    public String[] getPostInfoArray() {return postInfo.getValue();}

    public MutableLiveData<String[]> getPostInfo() {
        return postInfo;
    }

    public void setPostInfo(String[] userInfo) {
        this.postInfo.setValue(userInfo);
    }

    private MutableLiveData<List<Post>> postList = new MutableLiveData<>();

    public void setPostList(List<Post> posts) {
        this.postList.setValue(posts);
    }
    public MutableLiveData<List<Post>> getPostList() {
        return postList;
    }


}
