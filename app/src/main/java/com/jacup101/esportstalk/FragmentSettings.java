package com.jacup101.esportstalk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class FragmentSettings extends Fragment {

    private View view;

    User user;

    EditText userText;
    EditText passText;
    Button signUp;
    Button logIn;
    Button logOut;
    RadioGroup modeGroup;
    RadioButton light;
    RadioButton dark;
    RadioButton auto;

    int orientation;

    SettingsFragmentViewModel viewModel;
    DatabaseHelper database;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);

        database = new DatabaseHelper(getContext());

        user = ((EsportsTalkApplication) getActivity().getApplication()).getGlobalUser();
        viewModel = (SettingsFragmentViewModel) new ViewModelProvider(requireActivity()).get(SettingsFragmentViewModel.class);


        orientation = getContext().getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_PORTRAIT) {
            userText = view.findViewById(R.id.editText_username);
            passText = view.findViewById(R.id.editText_password);
            signUp = view.findViewById(R.id.button_signUp);
            logIn = view.findViewById(R.id.button_logIn);
            logOut = view.findViewById(R.id.button_logOut);
            modeGroup = view.findViewById(R.id.radioGroup);
            light = view.findViewById(R.id.radioButton_light);
            dark = view.findViewById(R.id.radioButton_dark);
            auto = view.findViewById(R.id.radioButton_auto);
        } else if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
            userText = view.findViewById(R.id.editText_usernameLand);
            passText = view.findViewById(R.id.editText_passwordLand);
            signUp = view.findViewById(R.id.button_signUpLand);
            logIn = view.findViewById(R.id.button_logInLand);
            logOut = view.findViewById(R.id.button_logOutLand);
            modeGroup = view.findViewById(R.id.radioGroup);
            light = view.findViewById(R.id.radioButton_lightLand);
            dark = view.findViewById(R.id.radioButton_darkLand);
            auto = view.findViewById(R.id.radioButton_autoLand);
        }

        light.setOnClickListener(v -> lightClicked());
        dark.setOnClickListener(v -> darkClicked());
        auto.setOnClickListener(v -> autoClicked());

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("com.jacup101.esportstalk.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        String viewMode = sharedPreferences.getString("viewmode","light");
        String auto = sharedPreferences.getString("auto","false");
        if(auto.equals("true")) {
            if(orientation==Configuration.ORIENTATION_PORTRAIT) {
                modeGroup.check(R.id.radioButton_auto);
            } else {
                modeGroup.check(R.id.radioButton_autoLand);
            }
        } else {
            if(viewMode.equals("light")) {
                if(orientation==Configuration.ORIENTATION_PORTRAIT) {
                    modeGroup.check(R.id.radioButton_light);
                } else {
                    modeGroup.check(R.id.radioButton_lightLand);
                }
            }
            if(viewMode.equals("dark")) {
                if(orientation==Configuration.ORIENTATION_PORTRAIT) {
                    modeGroup.check(R.id.radioButton_dark);
                } else {
                    modeGroup.check(R.id.radioButton_darkLand);
                }
            }
        }
        logIn.setOnClickListener(v -> logIn());
        logOut.setOnClickListener(v -> logOut());

        signUp.setOnClickListener(v->signUp());

        if(!user.getUsername().equals("USER_L0GGED_0UT")) {
            userText.setVisibility(View.GONE);
            passText.setVisibility(View.GONE);
            signUp.setVisibility(View.GONE);
            logIn.setVisibility(View.GONE);

        } else {
            logOut.setVisibility(View.GONE);
        }

        if(viewModel.getStringUser() != null) {
            userText.setText(viewModel.getStringUser());
        }
        if(viewModel.getStringPass() != null) {
            passText.setText(viewModel.getStringPass());
        }

        return view;

    }
    public void signUp()  {
        if(!checkEmpty()) {
            database.addUser(userText.getText().toString(), passText.getText().toString(), user);
        }
    }

    public void logIn() {
        if(!checkEmpty()) {
            database.logIn(userText.getText().toString(), passText.getText().toString(), user);

        }
    }
    public void logOut() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("com.jacup101.esportstalk.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString("username", "USER_L0GGED_0UT");
        edit.apply();
        user.setUsername("USER_L0GGED_0UT");
        user.setFollowed("");

        viewModel.setUser(null);
        viewModel.setPass(null);

        userText.setVisibility(View.VISIBLE);
        passText.setVisibility(View.VISIBLE);
        signUp.setVisibility(View.VISIBLE);
        logIn.setVisibility(View.VISIBLE);

        logOut.setVisibility(View.GONE);

    }
    private boolean checkEmpty() {
        boolean isEmpty = TextUtils.isEmpty(userText.getText()) || TextUtils.isEmpty(passText.getText());
        if(isEmpty) {
            Toast.makeText(getContext(), "Missing fields", Toast.LENGTH_SHORT).show();
        }
        return isEmpty;
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("stopped","stopped");
        viewModel.setPass(passText.getText().toString());
        viewModel.setUser(userText.getText().toString());
    }

    public void lightClicked() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("com.jacup101.esportstalk.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString("viewmode", "light");
        edit.putString("auto", "false");
        edit.apply();
        Toast.makeText(getContext(),"Mode set to light.",Toast.LENGTH_SHORT).show();
        getActivity().finish();
        getContext().startActivity(getActivity().getIntent());

    }
    public void darkClicked() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("com.jacup101.esportstalk.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString("viewmode", "dark");
        edit.putString("auto", "false");
        edit.apply();
        Toast.makeText(getContext(),"Mode set to dark.",Toast.LENGTH_SHORT).show();
        getActivity().finish();
        getContext().startActivity(getActivity().getIntent());
    }
    public void autoClicked() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("com.jacup101.esportstalk.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString("auto", "true");
        edit.apply();
        Toast.makeText(getContext(),"Mode set to auto. While on the home screen, the app will adjust system preferences based on surrounding light.",Toast.LENGTH_SHORT).show();

    }
}
