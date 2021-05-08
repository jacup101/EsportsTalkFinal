package com.jacup101.esportstalk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity implements SensorEventListener {


    private DatabaseHelper databaseHelper;
    private TabLayout tabLayout;


    private SensorManager sensorManager;
    private Sensor lightSensor;
    private MainActivityViewModel viewModel;
    private int orientation;

    private boolean auto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        SharedPreferences sharedPreferences = getSharedPreferences("com.jacup101.esportstalk.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        String viewMode = sharedPreferences.getString("viewmode","light");
        if(viewMode.equals("light")) {
            setTheme(R.style.Theme_EsportsTalk);
        } else if(viewMode.equals("dark")) {
            setTheme(R.style.DarkMode);
        }


        orientation = this.getResources().getConfiguration().orientation;
        setContentView(R.layout.activity_main);
        if(orientation == Configuration.ORIENTATION_PORTRAIT) {

            tabLayout = findViewById(R.id.tabLayout_main);
        } else if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
            tabLayout = findViewById(R.id.tabLayoutLand_main);
        }



        User user = ((EsportsTalkApplication) this.getApplication()).getGlobalUser();
        //load view model
        viewModel = (MainActivityViewModel) new ViewModelProvider(this).get(MainActivityViewModel.class);
        if(viewModel.getStringFragmentSelect() == null) {
            parseClick("Home");

        } else {
            parseTab(viewModel.getStringFragmentSelect());
            parseClick(viewModel.getStringFragmentSelect());
        }
        //

        databaseHelper = new DatabaseHelper(this);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if(lightSensor == null) {
            Toast.makeText(this, "Light sensor not found. Auto may not work as intended", Toast.LENGTH_SHORT).show();
        }



        if(!sharedPreferences.contains("username")) {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putString("username", "USER_L0GGED_0UT");
            edit.apply();
        }
        String storedUserString = sharedPreferences.getString("username","USER_L0GGED_0UT");
        if(!storedUserString.equals("USER_L0GGED_0UT")) {
            databaseHelper.loadUser(storedUserString,user);
        } else {
            user.setUsername("USER_L0GGED_0UT");
            user.setFollowed("");
        }


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // called when tab selected
                String tabSelect = tab.getText().toString();
                parseClick(tabSelect);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // called when tab unselected
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                String tabSelect = tab.getText().toString();
                parseClick(tabSelect);
                // called when a tab is reselected
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences sharedPreferences = getSharedPreferences("com.jacup101.esportstalk.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        String autoStr = sharedPreferences.getString("auto","false");
        Log.d("auto_test","ON start with val " + autoStr);
        if(autoStr.equals("false")) {
            //setTheme(R.style.Theme_EsportsTalk);
            this.auto = false;
            Log.d("auto_test","false");
        } else if(autoStr.equals("true")) {
            //TODO IMPLEMENT AUTO
            this.auto = true;
            Log.d("auto_test","true");
        }
        if(lightSensor !=null) {
            sensorManager.registerListener(this, lightSensor,SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
    }

    public void parseClick(String selection) {
        if(selection.equals("Home")) {
            if(orientation == Configuration.ORIENTATION_PORTRAIT) loadFragment(new FragmentHome(),R.id.fragmentContainerView);
            if(orientation == Configuration.ORIENTATION_LANDSCAPE) loadFragment(new FragmentHome(),R.id.fragmentContainerView_mainLand);

            viewModel.setFragmentSelect("Home");
        } if(selection.equals("User")) {
            if(orientation == Configuration.ORIENTATION_PORTRAIT) loadFragment(new FragmentUser(),R.id.fragmentContainerView);
            if(orientation == Configuration.ORIENTATION_LANDSCAPE) loadFragment(new FragmentUser(),R.id.fragmentContainerView_mainLand);


            viewModel.setFragmentSelect("User");
        } if(selection.equals("Settings")) {
            if(orientation == Configuration.ORIENTATION_PORTRAIT) loadFragment(new FragmentSettings(),R.id.fragmentContainerView);
            if(orientation == Configuration.ORIENTATION_LANDSCAPE) loadFragment(new FragmentSettings(),R.id.fragmentContainerView_mainLand);

            viewModel.setFragmentSelect("Settings");
        }

    }
    public void parseTab(String selection) {
        if(selection.equals("Home")) {
            tabLayout.selectTab(tabLayout.getTabAt(0));
        }        if(selection.equals("User")) {
            tabLayout.selectTab(tabLayout.getTabAt(1));
        }        if(selection.equals("Settings")) {
            tabLayout.selectTab(tabLayout.getTabAt(2));
        }
    }


    public void loadFragment(Fragment fragment, int id) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(id,fragment);
        fragmentTransaction.commit();

    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        SharedPreferences sharedPreferences = getSharedPreferences("com.jacup101.esportstalk.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        String type = sharedPreferences.getString("viewmode","light");
        SharedPreferences.Editor edit = sharedPreferences.edit();
        int sensorType = event.sensor.getType();
        if(sensorType == Sensor.TYPE_LIGHT) {
            float currentVal = event.values[0];
            Log.d("sensor_change", "" + currentVal + " " + auto);
            if(this.auto) {
                Log.d("sensor_change", "" + currentVal);
                if(currentVal <= 20 && type.equals("light")) {
                    Toast.makeText(this, "Auto set mode to dark, reload to take effect.", Toast.LENGTH_SHORT).show();
                    edit.putString("viewmode", "dark");
                    edit.apply();
                } else if(currentVal > 20 && type.equals("dark")) {
                    Toast.makeText(this, "Auto set mode to light, reload to take effect.", Toast.LENGTH_SHORT).show();
                    edit.putString("viewmode", "light");
                    edit.apply();
                }
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }
}