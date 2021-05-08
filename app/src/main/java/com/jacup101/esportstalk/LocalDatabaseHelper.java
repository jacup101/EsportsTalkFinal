package com.jacup101.esportstalk;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;


public class LocalDatabaseHelper extends SQLiteOpenHelper {

    public LocalDatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlStatement = "CREATE TABLE " + LocalDatabaseContract.PostEntry.TABLE_NAME + " (" + LocalDatabaseContract.PostEntry.COLUMN_ID + " INTEGER PRIMARY KEY, " +
                LocalDatabaseContract.PostEntry.COLUMN_COMMENTS + " TEXT, " + LocalDatabaseContract.PostEntry.COLUMN_COMMUNITY + " TEXT, " +
                LocalDatabaseContract.PostEntry.COLUMN_CONTENT + " TEXT, " + LocalDatabaseContract.PostEntry.COLUMN_DATE + " TEXT, " +
                LocalDatabaseContract.PostEntry.COLUMN_TITLE + " TEXT, " + LocalDatabaseContract.PostEntry.COLUMN_TYPE + " TEXT, " +
                LocalDatabaseContract.PostEntry.COLUMN_USER + " TEXT)";
        SQLiteStatement statement = db.compileStatement(sqlStatement);

        statement.execute();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void deleteAll() {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(LocalDatabaseContract.PostEntry.TABLE_NAME,null, null);
        database.close();
    }

    public List<Post> getAllPosts() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        List<Post> posts = new ArrayList<Post>();

        String sqlQuery = "SELECT * FROM " + LocalDatabaseContract.PostEntry.TABLE_NAME;
        Cursor resultSet = sqLiteDatabase.rawQuery(sqlQuery,null);

        if(resultSet.moveToFirst()) {
            do {
                long id = resultSet.getLong(0);
                String comments = resultSet.getString(1);
                String community = resultSet.getString(2);
                String content = resultSet.getString(3);
                String date = resultSet.getString(4);
                String title = resultSet.getString(5);
                String type = resultSet.getString(6);
                String user = resultSet.getString(7);

                Post post =  new Post(title, content, user, id, type, community, date);
                post.setCommentString(comments);
                posts.add(post);


            } while(resultSet.moveToNext());
        }
        resultSet.close();
        sqLiteDatabase.close();
        return posts;
    }



    public long addUser(Post post) {
        SQLiteDatabase database = this.getWritableDatabase();

        String sqlQuery = "INSERT INTO " + LocalDatabaseContract.PostEntry.TABLE_NAME + " (" + LocalDatabaseContract.PostEntry.COLUMN_ID + "," +
                LocalDatabaseContract.PostEntry.COLUMN_COMMENTS + "," + LocalDatabaseContract.PostEntry.COLUMN_COMMUNITY + "," +
                LocalDatabaseContract.PostEntry.COLUMN_CONTENT + "," + LocalDatabaseContract.PostEntry.COLUMN_DATE + "," +
                LocalDatabaseContract.PostEntry.COLUMN_TITLE + "," + LocalDatabaseContract.PostEntry.COLUMN_TYPE + "," +
                LocalDatabaseContract.PostEntry.COLUMN_USER + ") VALUES (?, ?, ?, ?, ?, ? ,? ,?)";

        SQLiteStatement statement = database.compileStatement(sqlQuery);

        long id = post.getId();
        String comments = post.getCommentString();
        String community = post.getCommunity();
        String content = post.getContent();
        String date = post.getDate();
        String title = post.getTitle();
        String type = post.getType();
        String user = post.getUser();

        statement.bindLong(1, id);
        statement.bindString(2, comments);
        statement.bindString(3, community);
        statement.bindString(4, content);
        statement.bindString(5, date);
        statement.bindString(6, title);
        statement.bindString(7, type);
        statement.bindString(8, user);

        long rowId = statement.executeInsert();
        database.close();
        return rowId;
    }
}
