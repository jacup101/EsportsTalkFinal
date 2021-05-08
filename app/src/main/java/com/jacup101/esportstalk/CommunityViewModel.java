package com.jacup101.esportstalk;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class CommunityViewModel extends ViewModel {
    //PostSharedViewModel provides a unified location to store the posts that are to be displayed on screen
    private MutableLiveData<List<Post>> postList = new MutableLiveData<>();

    private MutableLiveData<String[]> communityInfo = new MutableLiveData<>();

    public void setCommunityInfo(String[] info) {this.communityInfo.setValue(info);}

    public MutableLiveData<String[]> getCommunityInfo() {return communityInfo;}

    public void setPostList(List<Post> posts) {
        this.postList.setValue(posts);
    }
    public MutableLiveData<List<Post>> getPostList() {
        return postList;
    }


}
