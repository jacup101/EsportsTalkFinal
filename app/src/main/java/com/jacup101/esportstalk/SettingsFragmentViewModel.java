package com.jacup101.esportstalk;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SettingsFragmentViewModel extends ViewModel {
    //PostSharedViewModel provides a unified location to store the posts that are to be displayed on screen
    private MutableLiveData<String> user = new MutableLiveData<>();
    private MutableLiveData<String> pass = new MutableLiveData<>();

    public void setUser(String user) {
        this.user.setValue(user);
    }
    public MutableLiveData<String> getUser() {
        return user;
    }

    public String getStringUser() {return user.getValue();}

    public void setPass(String pass) {
        this.pass.setValue(pass);
    }
    public MutableLiveData<String> getPass() {
        return pass;
    }

    public String getStringPass() {return pass.getValue();}
}
