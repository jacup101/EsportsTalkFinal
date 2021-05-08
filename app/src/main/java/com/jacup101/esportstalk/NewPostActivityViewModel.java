package com.jacup101.esportstalk;

import android.net.Uri;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class NewPostActivityViewModel extends ViewModel {
    //PostSharedViewModel provides a unified location to store the posts that are to be displayed on screen
    private MutableLiveData<String> textInput = new MutableLiveData<>();

    private MutableLiveData<String> titleInput = new MutableLiveData<>();

    private MutableLiveData<Uri> uri = new MutableLiveData<>();
    private MutableLiveData<String> ytvid = new MutableLiveData<>();

    private MutableLiveData<String> type = new MutableLiveData<>();

    public MutableLiveData<String> getTextInput() {
        return textInput;
    }

    public void setTextInput(String textInput) {
        this.textInput.setValue(textInput);
    }

    public MutableLiveData<String> getTitleInput() {
        return titleInput;
    }

    public void setTitleInput(String titleInput) {
        this.titleInput.setValue(titleInput);
    }

    public MutableLiveData<Uri> getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri.setValue(uri);
    }

    public MutableLiveData<String> getYtvid() {
        return ytvid;
    }

    public void setYtvid(String ytvid) {
        this.ytvid.setValue(ytvid);
    }

    public MutableLiveData<String> getType() {
        return type;
    }

    public void setType(String type) {
        this.type.setValue(type);
    }
}
