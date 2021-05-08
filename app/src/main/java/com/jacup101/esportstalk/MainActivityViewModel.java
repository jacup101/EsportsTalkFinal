package com.jacup101.esportstalk;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class MainActivityViewModel extends ViewModel {
    //PostSharedViewModel provides a unified location to store the posts that are to be displayed on screen
    private MutableLiveData<String> fragmentSelect = new MutableLiveData<>();

    public void setFragmentSelect(String fragmentSelect) {
        this.fragmentSelect.setValue(fragmentSelect);
    }
    public MutableLiveData<String> getFragmentSelect() {
        return fragmentSelect;
    }

    public String getStringFragmentSelect() {return fragmentSelect.getValue();}
}
