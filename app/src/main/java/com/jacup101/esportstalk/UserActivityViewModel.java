package com.jacup101.esportstalk;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class UserActivityViewModel extends ViewModel {
    //PostSharedViewModel provides a unified location to store the posts that are to be displayed on screen
    private MutableLiveData<String[]> userInfo = new MutableLiveData<>();

    public String[] getUserInfoArray() {return userInfo.getValue();}

    public MutableLiveData<String[]> getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String[] userInfo) {
        this.userInfo.setValue(userInfo);
    }

    private MutableLiveData<List<Post>> postList = new MutableLiveData<>();

    public void setPostList(List<Post> posts) {
        this.postList.setValue(posts);
    }
    public MutableLiveData<List<Post>> getPostList() {
        return postList;
    }


}
