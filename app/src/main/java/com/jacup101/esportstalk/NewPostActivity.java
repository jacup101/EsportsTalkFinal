package com.jacup101.esportstalk;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewPostActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    DatabaseHelper databaseHelper;

    private Spinner typeSpinner;
    private Button post;
    private Button addImage;
    private Button cancel;

    private ImageView imageView;

    private User user;

    private NewPostActivityViewModel viewModel;

    private EditText content;
    private EditText title;

    private EditText videoInput;

    private Uri imageUri = null;
    private Bitmap imageBitmap = null;

    private String community = "null";
    private String type = "null";

    private boolean saveData = true;
    private int orientation;
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        if(selectedImage == null) {
                            Bundle extras = result.getData().getExtras();
                            imageBitmap = (Bitmap) extras.get("data");
                            imageView.setImageBitmap(imageBitmap);

                            Log.d("camera_upload","Camera uri failed");
                        } else {
                            imageView.setImageURI(selectedImage);
                            imageUri = selectedImage;
                        }



                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseHelper = new DatabaseHelper(this);
        viewModel = (NewPostActivityViewModel) new ViewModelProvider(this).get(NewPostActivityViewModel.class);

        SharedPreferences sharedPreferences = getSharedPreferences("com.jacup101.esportstalk.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        String viewMode = sharedPreferences.getString("viewmode","light");
        if(viewMode.equals("light")) {
            //do nothing
        } else if(viewMode.equals("dark")) {
            setTheme(R.style.DarkMode);
        } else if(viewMode.equals("auto")) {
            //TODO IMPLEMENT AUTO
        }
        setContentView(R.layout.activity_new_post);


        orientation = this.getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_PORTRAIT) {
            typeSpinner = findViewById(R.id.spinner_type);
            cancel = findViewById(R.id.button_nPCancel);
            addImage = findViewById(R.id.button_nPAddImage);
            videoInput = findViewById(R.id.editText_newPVideo);
            imageView = findViewById(R.id.imageView_nPUploaded);
            content = findViewById(R.id.editText_nPContent);
            title = findViewById(R.id.editText_nPTitle);
            post = findViewById(R.id.button_nPPost);
        } else if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
            typeSpinner = findViewById(R.id.spinner_typeLand);
            cancel = findViewById(R.id.button_nPCancelLand);
            addImage = findViewById(R.id.button_nPAddImageLand);
            videoInput = findViewById(R.id.editText_newPVideoLand);
            imageView = findViewById(R.id.imageView_nPUploadedLand);
            content = findViewById(R.id.editText_nPContentLand);
            title = findViewById(R.id.editText_nPTitleLand);
            post = findViewById(R.id.button_nPPostLand);
        }



        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.types_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);
        typeSpinner.setOnItemSelectedListener(this);


        cancel.setOnClickListener(v -> cancel(v));


        addImage.setOnClickListener(v -> addImage(v));


        post.setOnClickListener(v -> post(v));


        if(viewModel.getUri().getValue()!=null) {
            imageUri = viewModel.getUri().getValue();
            imageView.setImageURI(imageUri);
        }

        if(viewModel.getType().getValue()!= null) {
            type = viewModel.getType().getValue();
            if(type.equals("image")) typeSpinner.setSelection(1);
            if(type.equals("video")) typeSpinner.setSelection(2);
        }
        if(viewModel.getTitleInput().getValue()!=null) title.setText(viewModel.getTitleInput().getValue());
        if(viewModel.getTextInput().getValue()!=null) content.setText(viewModel.getTextInput().getValue());

        if(!type.equals("video")) videoInput.setVisibility(View.GONE);
        if(viewModel.getYtvid().getValue()!=null) {
            videoInput.setText(viewModel.getYtvid().getValue());
        }

        user = ((EsportsTalkApplication) getApplication()).getGlobalUser();



        community = getIntent().getStringExtra("community");
    }
    public void addImage(View v) {
        if(!type.contains("image")) {
            Toast.makeText(this,"Please select image type",Toast.LENGTH_SHORT).show();
        } else {
            promptImageSelection();

        }

    }
    private boolean checkEmpty() {
        boolean isEmpty = TextUtils.isEmpty(title.getText()) || TextUtils.isEmpty(content.getText());
        if(isEmpty) {
            Toast.makeText(this, "Missing fields", Toast.LENGTH_SHORT).show();
        }
        return isEmpty;
    }


    public String parseYoutubeLink(String youtubeUrl) {
        String pattern = "https?://(?:[0-9A-Z-]+\\.)?(?:youtu\\.be/|youtube\\.com\\S*[^\\w\\-\\s])([\\w\\-]{11})(?=[^\\w\\-]|$)(?![?=&+%\\w]*(?:['\"][^<>]*>|</a>))[?=&+%\\w]*";
        Log.d("youtube_parse","parsing");
        Pattern compiledPattern = Pattern.compile(pattern,
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(youtubeUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        Log.d("youtube_parse","failed to parse");

        return "null";
    }
    public void promptImageSelection() {

        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Upload Photo");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    someActivityResultLauncher.launch(takePicture);

                } else if (options[item].equals("Choose from Gallery")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    someActivityResultLauncher.launch(pickPhoto);

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        imageUri = selectedImage;
                        imageView.setImageURI(selectedImage);
                    }
                    break;
            }
        }
    }

    public void cancel(View v) {
        saveData = false;
        Intent intent = new Intent(this, CommunityActivity.class);
        intent.putExtra("community",community);
        startActivity(intent);
    }
    public void post(View v) {
        if(checkEmpty()) {
            return;
        }
        saveData = false;
        if(type.equals("text")) {
            databaseHelper.addPost(title.getText().toString(),user.getUsername(),type,content.getText().toString(),community, null, null, null);
        } if(type.equals("image")) {
            if(imageUri != null || imageBitmap != null) {
                if(imageBitmap != null) {
                    databaseHelper.addPost(title.getText().toString(),user.getUsername(),type,content.getText().toString(),community, null, imageBitmap, null);
                } else if (imageUri != null) {
                    databaseHelper.addPost(title.getText().toString(),user.getUsername(),type,content.getText().toString(),community, imageUri, null,null);
                }

            } else {
                type = "text";
                databaseHelper.addPost(title.getText().toString(),user.getUsername(),type,content.getText().toString(),community, null, null, null);
            }
        } if(type.equals("video")) {
            String parsed = parseYoutubeLink(videoInput.getText().toString());
            Log.d("youtube_parse","Parsed: " + parsed);
            if(!parsed.equals("null")) {
                databaseHelper.addPost(title.getText().toString(),user.getUsername(),type,content.getText().toString(), community,null, null, parsed);
            } else {
                type = "text";
                databaseHelper.addPost(title.getText().toString(),user.getUsername(),type,content.getText().toString(),community, null, null, null);
            }
        }
        Intent intent = new Intent(this, CommunityActivity.class);
        intent.putExtra("community",community);
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        type = (String) parent.getItemAtPosition(position);
        if(type.equals("video")) {
            videoInput.setVisibility(View.VISIBLE);
        } else {
            videoInput.setVisibility(View.GONE);
        } if(!type.equals("image")) {
            imageView.setImageBitmap(null);
        } else {
            if(imageUri != null) imageView.setImageURI(imageUri);
        }

        //Log.d("type_select",type);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //Do nothing
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(saveData) {
            viewModel.setTextInput(content.getText().toString());
            viewModel.setTitleInput(content.getText().toString());
            viewModel.setType(type);
            viewModel.setUri(imageUri);
            viewModel.setYtvid(videoInput.getText().toString());
        } else {
            viewModel.setTextInput(null);
            viewModel.setTitleInput(null);
            viewModel.setType(null);
            viewModel.setUri(null);
            viewModel.setYtvid(null);
        }
        Log.d("newpoststop","stopped with val " + saveData);
    }


}
