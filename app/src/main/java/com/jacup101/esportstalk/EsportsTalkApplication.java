package com.jacup101.esportstalk;

import android.app.Application;
import android.content.SharedPreferences;

public class EsportsTalkApplication extends Application {

    private User globalUser;

    public User getGlobalUser() {
        if(globalUser == null) {
            //SharedPreferences pref = getSharedPreferences()
            globalUser = new User("USER_L0GGED_0UT",null);
        }
        return globalUser;
    }

    public void setGlobalUser(User globalUser) {
        this.globalUser = globalUser;
    }
}
