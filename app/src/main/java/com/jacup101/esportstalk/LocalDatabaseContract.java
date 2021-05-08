package com.jacup101.esportstalk;

import android.provider.BaseColumns;

public class LocalDatabaseContract {
    private LocalDatabaseContract() {

    }

    public static class PostEntry implements BaseColumns {
        public static final String TABLE_NAME = "post_table";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_USER = "user";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_COMMUNITY = "community";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_COMMENTS = "comments";

    }

    /*
    Attributes:
    private String title;*
    private String content;*
    private String user;*
    private long id;*
    private String type;*
    private String community;*

    private String commentString;*

    private String date;*


     */
}
