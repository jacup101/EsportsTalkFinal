package com.jacup101.esportstalk;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper {
    FirebaseFirestore db;
    FirebaseStorage storage;
    Context context;
    LocalDatabaseHelper localDB;


    public DatabaseHelper(Context context) {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        this.context = context;
        localDB = new LocalDatabaseHelper(context, "posts.db", null, 1);

    }
    //logIn will allow a user to log in to the service
    //Param: userID and password for sign in purposes, and a User, which should be the global user for the app, so that it can be updated
    public void logIn(String userID, String password, User user) {
        String finalUserID = userID.toLowerCase();
        DocumentReference df = db.collection("users").document(finalUserID);
        df.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                //If the global postID variable is found, proceed
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String dbUser = (String) document.get("username");
                        String dbPass = (String) document.get("password");

                        if(dbUser.equals(finalUserID) && dbPass.equals(password)) {
                            Log.d("log_in","Log in was successful");
                            SharedPreferences pref = context.getSharedPreferences("com.jacup101.esportstalk.PREFERENCE_FILE_KEY",Context.MODE_PRIVATE);
                            SharedPreferences.Editor edit = pref.edit();
                            edit.putString("username", finalUserID);
                            edit.apply();
                            String followed = (String) document.get("followed");
                            if(followed == null) followed = "";
                            user.setFollowed(followed);
                            user.setUsername(finalUserID);

                            Toast.makeText(context,"Logged in as " + userID,Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d("log_in","Login failed");
                            Toast.makeText(context,"Login failed",Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "No such user, please sign up",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }



    //loadUser will load the current logged in user locally (from shared preferences) and actually load the data from the online database
    //Param: userID for reference, and the global User for the app to update data
    public void loadUser(String userID, User user) {
        DocumentReference df = db.collection("users").document(userID);
        df.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                //If the global postID variable is found, proceed
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String followed = (String) document.get("followed");
                        if(followed == null) followed = "";
                        user.setUsername(userID);
                        user.setFollowed(followed);
                        //Toast.makeText(context, "User successfully loaded",Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

    }


    //followCommunity allows for different users to follow a specific community
    //Param: communityID and userID, to update appropriate fields, as well as the global user so that changes are reflected locally
    //Also takes val, which determines whether to follow (1) or unfollow (-1)
    public void followCommunity(String communityID, String userID, User user, long val) {
        DocumentReference df = db.collection("communities").document(communityID);
        DocumentReference userDoc = db.collection("users").document(userID);

        df.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        long followers = (long) document.get("followers");
                        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> taskUser) {
                                if(taskUser.isSuccessful()) {
                                    DocumentSnapshot userDoc = taskUser.getResult();
                                    String followed = (String) userDoc.get("followed");
                                    if(followed == null) followed = "";
                                    if(val == 1) followed += communityID + ",";
                                    if(val == -1 && followed.contains(communityID)) followed = followed.replace(communityID + ",", "");
                                    if(followed != null) user.setFollowed(followed);
                                    Map<String, Object> newUserFollow = new HashMap<>();
                                    newUserFollow.put("followed", followed);
                                    db.collection("users").document(userID)
                                            .set(newUserFollow, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            String text = "Followed " + communityID;
                                            if(val == -1 ) {
                                                text = "Unfollowed " + communityID;
                                            }
                                            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                                            Map<String,Object> newCommunityFollowers = new HashMap<>();
                                            newCommunityFollowers.put("followers",followers + val);
                                            db.collection("communities").document(communityID)
                                                    .set(newCommunityFollowers, SetOptions.merge());
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    //searchPostsList will search the most recent 1000 posts, both by title and content, for the search query
    //Param: data, to store the results in a list, and the search query
    public void searchPostsList(MutableLiveData<List<SearchResult>> data, String search) {
        List<SearchResult> postList = new ArrayList<SearchResult>();
        Query query = db.collection("posts").limitToLast(1000).orderBy("id", Query.Direction.DESCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    for(QueryDocumentSnapshot document : task.getResult()) {
                        String title = (String) document.get("title");
                        String content = (String) document.get("content");
                        if(title.toLowerCase().contains(search.toLowerCase()) || content.toLowerCase().contains(search.toLowerCase())) {
                            Log.d("searchTest", document.getId() + " => " + document.getData());
                            String user = (String) document.getData().get("user");
                            String type = (String) document.getData().get("type");
                            String community = (String) document.getData().get("community");
                            Timestamp timestamp = (Timestamp) document.getData().get("timestamp");
                            Date dateClass = timestamp.toDate();
                            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                            String date = formatter.format(dateClass);

                            long id = (long) document.getData().get("id");
                            Post post = new Post(title, content, user, id, type, community,date);
                            String imageUrl = (String) document.getData().get("imageURL");
                            String videoID = (String) document.getData().get("videoID");
                            String comments = (String) document.getData().get("comments");
                            if(comments!=null ) {
                                post.setCommentString(comments);
                            }
                            if(post.getType().equals("image")) {
                                String imageId = (String) document.getData().get("image");
                                if(imageUrl != null) {
                                    post.setImageUri(Uri.parse(imageUrl));
                                }
                            }
                            if(post.getType().equals("video")) {
                                if(videoID!=null) {
                                    post.setVideoID(videoID);
                                }
                            }
                            postList.add(new SearchResult(id,"post",title,post));
                        }
                    }
                    data.setValue(postList);
                }
            }
        });

    }
    //searchCommunityList will search the most recent 1000 communities for the search query
    //Param: data, to store the results in a list, and the search query
    public void searchCommunityList(MutableLiveData<List<SearchResult>> data,String search) {
        List<SearchResult> communityList = new ArrayList<SearchResult>();
        Query query = db.collection("communities").limitToLast(1000).orderBy("id", Query.Direction.DESCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    for(QueryDocumentSnapshot document : task.getResult()) {
                        String id = (String) document.get("id");
                        String name = (String) document.get("name");
                        if(id.toLowerCase().contains(search.toLowerCase()) || name.toLowerCase().contains(search.toLowerCase())) {
                            communityList.add(new SearchResult(id,"community",name));
                            Log.d("searchTest", document.getId() + " => " + document.getData());
                        }
                    }
                    data.setValue(communityList);
                }
            }
        });

    }
    //searchUsersList will search the most recent 1000 users for the search query
    //Param: data, to store the results in a list, and the search query
    public void searchUsersList(MutableLiveData<List<SearchResult>> data,String search) {
        List<SearchResult> userList = new ArrayList<SearchResult>();
        Query query = db.collection("users").limitToLast(1000).orderBy("id", Query.Direction.DESCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    for(QueryDocumentSnapshot document : task.getResult()) {
                        String username = (String) document.get("username");
                        long id = (long) document.get("id");
                        if(username.toLowerCase().contains(search.toLowerCase()) ) {
                            userList.add(new SearchResult(id, "user", username));
                            Log.d("searchTest", document.getId() + " => " + document.getData());
                        }
                    }
                    data.setValue(userList);
                }
            }
        });
    }

    public void getUserProperties(MutableLiveData<String[]> liveData, String user_id) {
        String[] properties = new String[] {"Username","FollowedString"};
        DocumentReference df = db.collection("users").document(user_id);
        df.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        properties[0] = (String) doc.get("username");
                        String followed = (String) doc.get("followed");
                        if(followed == null) followed = "";
                        properties[1] = followed;
                        liveData.setValue(properties);
                    }
                }
            }
        });

    }


    public void getCommunityProperties(MutableLiveData<String[]> liveData, String community_id) {
            String[] properties = new String[] {"Logo", "Cover", "Name", "0"};
    //      0 - logo, 1 - cover, 2 - name, 3 - follower count
            DocumentReference df = db.collection("communities").document(community_id);
            df.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.exists()) {
                            properties[0] = (String) doc.get("logo");
                            properties[1] = (String) doc.get("cover");
                            properties[2] = (String) doc.get("name");
                            properties[3] = "" + (Long) (doc.get("followers"));
                            liveData.setValue(properties);

                        }
                    }
                }
            });

    }

    public void loadUserPosts(String username, MutableLiveData<List<Post>> liveData) {
        List<Post> data = new ArrayList<Post>();
        Query query = db.collection("posts").limitToLast(100).orderBy("timestamp").whereEqualTo("user",username);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    for(QueryDocumentSnapshot document : task.getResult()) {
                        Log.d("firebaseTest", document.getId() + " => " + document.getData());
                        String title = (String) document.getData().get("title");
                        String content = (String) document.getData().get("content");
                        String user = (String) document.getData().get("user");
                        String type = (String) document.getData().get("type");
                        String community = (String) document.getData().get("community");
                        long id = (long) document.getData().get("id");
                        Timestamp timestamp = (Timestamp) document.getData().get("timestamp");
                        Date dateClass = timestamp.toDate();
                        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                        String date = formatter.format(dateClass);
                        Post post = new Post(title, content, user, id, type, community,date);
                        String imageUrl = (String) document.getData().get("imageURL");
                        String videoID = (String) document.getData().get("videoID");
                        String comments = (String) document.getData().get("comments");
                        if(comments!=null ) {
                            //post.parseCommentsFromString(comments);
                            post.setCommentString(comments);
                        }
                        if(post.getType().equals("image")) {
                            String imageId = (String) document.getData().get("image");
                            if(imageUrl != null) {
                                //Toast.makeText(context,"Found " + title, Toast.LENGTH_SHORT).show();
                                post.setImageUri(Uri.parse(imageUrl));
                            }

                        }
                        if(post.getType().equals("video")) {
                            if(videoID!=null) {
                                post.setVideoID(videoID);
                            }
                        }
                        data.add(post);
                    }
                }
                Collections.sort(data);
                liveData.setValue(data);
            }
        });

    }

    public void loadSpecifiedToView(List<String> communities, MutableLiveData<List<Post>> liveData) {
        List<Post> data = new ArrayList<Post>();
        for(String community : communities) {
            Query query = db.collection("posts").limitToLast(100).orderBy("timestamp").whereEqualTo("community",community);
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()) {
                        for(QueryDocumentSnapshot document : task.getResult()) {
                            Log.d("firebaseTest", document.getId() + " => " + document.getData());
                            String title = (String) document.getData().get("title");
                            String content = (String) document.getData().get("content");
                            String user = (String) document.getData().get("user");
                            String type = (String) document.getData().get("type");
                            String community = (String) document.getData().get("community");
                            long id = (long) document.getData().get("id");
                            Timestamp timestamp = (Timestamp) document.getData().get("timestamp");
                            Date dateClass = timestamp.toDate();
                            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                            String date = formatter.format(dateClass);
                            Post post = new Post(title, content, user, id, type, community,date);
                            String imageUrl = (String) document.getData().get("imageURL");
                            String videoID = (String) document.getData().get("videoID");
                            String comments = (String) document.getData().get("comments");
                            if(comments!=null ) {
                                //post.parseCommentsFromString(comments);
                                post.setCommentString(comments);
                            }
                            if(post.getType().equals("image")) {
                                String imageId = (String) document.getData().get("image");
                                if(imageUrl != null) {
                                    //Toast.makeText(context,"Found " + title, Toast.LENGTH_SHORT).show();
                                    post.setImageUri(Uri.parse(imageUrl));
                                }

                            }
                            if(post.getType().equals("video")) {
                                if(videoID!=null) {
                                    post.setVideoID(videoID);
                                }
                            }
                            data.add(post);
                        }
                    }
                    Collections.sort(data);
                    liveData.setValue(data);
                }
            });
        }
    }
    public void loadAllFromLocalDB(MutableLiveData<List<Post>> liveData) {
        List<Post> data = localDB.getAllPosts();
        Collections.sort(data);
        liveData.setValue(data);

    }
    public void loadAllToLocalDB() {
        Query query = db.collection("posts").limitToLast(1000).orderBy("timestamp");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    localDB.deleteAll();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d("firebaseTest", document.getId() + " => " + document.getData());
                        String title = (String) document.getData().get("title");
                        String content = (String) document.getData().get("content");
                        String user = (String) document.getData().get("user");
                        String type = (String) document.getData().get("type");
                        type = "text";
                        String community = (String) document.getData().get("community");
                        Timestamp timestamp = (Timestamp) document.getData().get("timestamp");
                        Date dateClass = timestamp.toDate();
                        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                        String date = formatter.format(dateClass);
                        Log.d("timestamp_test",date);
                        long id = (long) document.getData().get("id");
                        Post post = new Post(title, content, user, id, type, community,date);
                        String comments = (String) document.getData().get("comments");
                        if(comments!=null ) {
                            //post.parseCommentsFromString(comments);
                            post.setCommentString(comments);
                        }
                        localDB.addUser(post);
                    }
                }
            }
        });
    }


    //TODO: Use LiveData instead of manually loading into the postadapter and relying on copy by reference
    public void loadAllToView(MutableLiveData<List<Post>> liveData) {
        //Load posts
        //Add them to the data list
        //notify adapter of the change
        List<Post> data = new ArrayList<Post>();


        Query query = db.collection("posts").limitToLast(1000).orderBy("timestamp");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d("firebaseTest", document.getId() + " => " + document.getData());
                        String title = (String) document.getData().get("title");
                        String content = (String) document.getData().get("content");
                        String user = (String) document.getData().get("user");
                        String type = (String) document.getData().get("type");
                        String community = (String) document.getData().get("community");
                        Timestamp timestamp = (Timestamp) document.getData().get("timestamp");
                        Date dateClass = timestamp.toDate();
                        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                        String date = formatter.format(dateClass);
                        Log.d("timestamp_test",date);
                        long id = (long) document.getData().get("id");
                        Post post = new Post(title, content, user, id, type, community,date);
                        String imageUrl = (String) document.getData().get("imageURL");
                        String videoID = (String) document.getData().get("videoID");
                        String comments = (String) document.getData().get("comments");


                        if(comments!=null ) {
                            //post.parseCommentsFromString(comments);
                            post.setCommentString(comments);
                        }
                        if(post.getType().equals("image")) {
                            String imageId = (String) document.getData().get("image");
                            if(imageUrl != null) {
                                //Toast.makeText(context,"Found " + title, Toast.LENGTH_SHORT).show();
                                post.setImageUri(Uri.parse(imageUrl));
                            }

                        }
                        if(post.getType().equals("video")) {
                            if(videoID!=null) {
                                post.setVideoID(videoID);
                            }
                        }

                        data.add(post);



                        Log.d("post_load", post.toString());
                    }
                    Collections.sort(data);
                    liveData.setValue(data);
                }
            }
        });
    }

    public void addComment(String postID, String comment, String user) {
        DocumentReference df = db.collection("posts").document(postID);

        df.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    Log.d("comment_add","Post found!");
                    DocumentSnapshot docPost = task.getResult();
                    // Comments are stored in a single string, with the following format: $CM$U:user$P:comment$E
                    //if someone attempts to comment with the keywords $CM, $U, $P, or $E, it will be disallowed and they will be notified
                    String comments = (String) docPost.get("comments");
                    if(comments == null) comments = "";

                    comments += "$CM$U:" + user + "$P:" + comment + "$E";
                    Map<String, Object> newCommentString = new HashMap<>();
                    newCommentString.put("comments", comments);
                    db.collection("posts").document(postID)
                            .set(newCommentString, SetOptions.merge());


                }
            }
        });
    }





    //Add user will take a username and password, and attempt to add a new user to the database
    //TODO: Have addUser somehow notify mainactivity that the user was successfully created, and perform a login function (currently done through toast)
    //TODO: Standardize usernames here.. i.e. all lower case, etc (currently done through toLowerCase())
    public void addUser(String userName, String password, User user)  {
        final String finalUserName = userName.toLowerCase();

        DocumentReference df = db.collection("id").document("idVals");
        DocumentReference userDoc = db.collection("users").document(finalUserName);
        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task1) {
                if(task1.isSuccessful()) {
                    DocumentSnapshot docUser = task1.getResult();
                    if (!docUser.exists()) {
                        //If the username does not presently exist, begin the process of adding a new user
                        //Grab the global, static userId variable
                        df.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                //If the global userId variable is found, proceed
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        Log.d("userId_call", "DocumentSnapshot data: " + document.getData());
                                        long userId = (long) document.get("userId");
                                        //Create the new user's data
                                        Map<String, Object> newUser = new HashMap<>();
                                        newUser.put("username", finalUserName);
                                        newUser.put("password", password);
                                        newUser.put("id", userId);
                                        newUser.put("postCount",0);

                                        db.collection("users").document(finalUserName).set(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //If the user has been successfully added to the database, update the global userId value
                                                //Notify the user that the user was created through a toast
                                                Toast.makeText(context,"User successfully created!", Toast.LENGTH_SHORT).show();
                                                SharedPreferences pref = context.getSharedPreferences("com.jacup101.esportstalk.PREFERENCE_FILE_KEY",Context.MODE_PRIVATE);
                                                SharedPreferences.Editor edit = pref.edit();
                                                edit.putString("username", finalUserName);
                                                edit.apply();
                                                user.setFollowed("");
                                                user.setUsername(finalUserName);
                                                Map<String, Object> updatedData = new HashMap<>();
                                                updatedData.put("userId", userId + 1);

                                                db.collection("id").document("idVals")
                                                        .set(updatedData, SetOptions.merge());
                                            }
                                        });


                                    } else {
                                        Log.d("userId_call", "No such document");
                                    }
                                } else {
                                    Log.d("userId_call", "get failed with ", task.getException());
                                }
                            }
                        });
                    } else {
                        //If the user already existed, notify the user with a toast
                        Toast.makeText(context,"User already existed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });



    }

    public void addPost(String title, String user, String type, String content, String community, @Nullable Uri imageUri, @Nullable Bitmap imageBitmap, @Nullable String youtubeId)  {


        final String finalUserName = user.toLowerCase();

        DocumentReference df = db.collection("id").document("idVals");
        DocumentReference userDoc = db.collection("users").document(finalUserName);

        df.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                //If the global postID variable is found, proceed
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("postId_call", "DocumentSnapshot data: " + document.getData());
                        long postId = (long) document.get("postId");
                        //Create the new user's data
                        Map<String, Object> newPost = new HashMap<>();
                        newPost.put("user", finalUserName);
                        newPost.put("title", title);
                        newPost.put("type",type);
                        newPost.put("content", content);
                        newPost.put("id", postId);
                        newPost.put("community",community);

                        Timestamp timestamp = Timestamp.now();

                        newPost.put("timestamp",timestamp);

                        db.collection("posts").document("" + postId).set(newPost).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Map<String, Object> updatedData = new HashMap<>();
                                updatedData.put("postId", postId + 1);
                                db.collection("id").document("idVals")
                                        .set(updatedData, SetOptions.merge());
                                //merge the data to user
                                userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task2) {
                                        if (task2.isSuccessful()) {
                                            DocumentSnapshot userDocument = task2.getResult();
                                            if (userDocument.exists()) {
                                                Map<String, Object> updatedUserData = new HashMap<>();
                                                long postNum = (long) userDocument.get("postCount");
                                                updatedUserData.put("postCount", postNum + 1);
                                                db.collection("users").document(finalUserName).set(updatedUserData, SetOptions.merge());
                                                db.collection("users").document(finalUserName).collection("userposts").add(newPost);
                                                Toast.makeText(context,"Post successfully created!", Toast.LENGTH_SHORT).show();

                                                if(type.equals("image") && imageUri != null) {
                                                    //UPLAOD IMAGE
                                                    String imgTitle = "images/" + title + user + postId + ".jpg";
                                                    StorageReference storageRef = storage.getReference().child(imgTitle);
                                                    UploadTask uploadTask = storageRef.putFile(imageUri);
                                                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                        @Override
                                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                            Log.d("file_upload","success " + storageRef.getDownloadUrl().toString());
                                                            Map<String, Object> imageData = new HashMap<>();
                                                            imageData.put("image", imgTitle);
                                                            db.collection("posts").document("" + postId)
                                                                    .set(imageData, SetOptions.merge());
                                                            Toast.makeText(context,"Image Upload Complete",Toast.LENGTH_SHORT).show();
                                                            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                @Override
                                                                public void onSuccess(Uri uri) {
                                                                    Map<String, Object> urlData = new HashMap<>();
                                                                    urlData.put("imageURL", uri.toString());
                                                                    db.collection("posts").document("" + postId)
                                                                            .set(urlData, SetOptions.merge());
                                                                }
                                                            });
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.d("file_upload","failure: " + e);
                                                            Toast.makeText(context,"Image Upload Failed",Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                                if(type.equals("image") && imageBitmap != null) {
                                                    //UPLAOD IMAGE
                                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                                    byte[] data = baos.toByteArray();


                                                    String imgTitle = "images/" + title + user + postId + ".jpg";
                                                    StorageReference storageRef = storage.getReference().child(imgTitle);
                                                    UploadTask uploadTask = storageRef.putBytes(data);
                                                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                        @Override
                                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                            Log.d("file_upload","success " + storageRef.getDownloadUrl().toString());
                                                            Map<String, Object> imageData = new HashMap<>();
                                                            imageData.put("image", imgTitle);
                                                            db.collection("posts").document("" + postId)
                                                                    .set(imageData, SetOptions.merge());
                                                            Toast.makeText(context,"Image Upload Complete",Toast.LENGTH_SHORT).show();
                                                            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                @Override
                                                                public void onSuccess(Uri uri) {
                                                                    Map<String, Object> urlData = new HashMap<>();
                                                                    urlData.put("imageURL", uri.toString());
                                                                    db.collection("posts").document("" + postId)
                                                                            .set(urlData, SetOptions.merge());
                                                                }
                                                            });
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.d("file_upload","failure: " + e);
                                                            Toast.makeText(context,"Image Upload Failed",Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                                if(type.equals("video") && youtubeId != null ) {
                                                    Map<String, Object> videoData = new HashMap<>();
                                                    videoData.put("videoID", youtubeId);
                                                    db.collection("posts").document("" + postId)
                                                            .set(videoData, SetOptions.merge());
                                                    Log.d("video_found",youtubeId);
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        });
                    } else {
                        Log.d("postId_call", "No such document");
                        Toast.makeText(context,"Post failed to upload", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("postId_call", "get failed with ", task.getException());
                    Toast.makeText(context,"Post failed to upload", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
