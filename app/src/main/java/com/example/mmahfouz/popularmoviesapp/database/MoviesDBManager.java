package com.example.mmahfouz.popularmoviesapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.mmahfouz.popularmoviesapp.model.Movie;

/**
 * Created by mmahfouz on 2/5/2016.
 */
public class MoviesDBManager {

    private DBHelper dbHelper;
    private static MoviesDBManager instance;

    private MoviesDBManager (Context context){
        this.dbHelper = new DBHelper(context);
    }


    public static synchronized MoviesDBManager getInstance(Context context){
        if (instance == null) {
            instance = new MoviesDBManager(context);
        }
        return instance;
    }

    private SQLiteDatabase getWritableDB(){
        return this.dbHelper.getWritableDatabase();
    }

    private SQLiteDatabase getReadableDB(){
        return this.dbHelper.getReadableDatabase();
    }

    public Cursor queryAllFavourites (){
        // check the DB if this movie is a favourite one
        Cursor cursor = getReadableDB().query(MoviesDBManager.FAVOURITES_TABLE_NAME, null,
                null,
                null,
                null,null,null);
        return cursor;
    }

    public boolean queryIsFavourite (String movieID){
        // check the DB if this movie is a favourite one
        Cursor cursor = getReadableDB().query(MoviesDBManager.FAVOURITES_TABLE_NAME, null,
                MoviesDBManager.COLUMN_MOVIE_ID + "=?",
                new String[]{movieID},
                null,null,null);
        return cursor.getCount() > 0;
    }

    /*
    private String id;
    private String title;
    private String backdrop_path;
    private String release_date;
    private double vote_average;
    private String overview;
    private String poster_path;
        private boolean isFav;
        private boolean adult;
        private String genre_ids; // array not string
        private String original_language;
        private String original_title;
        private double popularity;
        private boolean video;
        private int vote_count;
    * */



    public boolean addToFavourite (Movie movie){
        ContentValues values = new ContentValues();
        values.put(COLUMN_MOVIE_ID,movie.getId());
        values.put(COLUMN_MOVIE_TITLE,movie.getTitle());
        values.put(COLUMN_MOVIE_BACKDROP_PATH,movie.getBackdrop_path());
        values.put(COLUMN_MOVIE_POSTER_PATH,movie.getPoster_path());
        values.put(COLUMN_MOVIE_RELEASE_DATE,movie.getRelease_date());
        values.put(COLUMN_MOVIE_VOTE_AVERAGE,movie.getVote_average());
        values.put(COLUMN_MOVIE_OVERVIEW,movie.getOverview());

        long row_id = getWritableDB().insert(MoviesDBManager.FAVOURITES_TABLE_NAME, null, values);

        if(row_id > 0)
            return true;
        return false;
    }

    public boolean removeFromFavourite (String movieID){
        long count = getWritableDB().delete(MoviesDBManager.FAVOURITES_TABLE_NAME, COLUMN_MOVIE_ID +"=?", new String[]{movieID});

        if(count > 0)
            return true;
        return false;
    }

    public static final String DB_NAME = "movies.db";
    public static final int DB_VERSION = 2;

    public static final String FAVOURITES_TABLE_NAME = "favourites";

    public static final String COLUMN_MOVIE_ID = "movie_id";
    public static final String COLUMN_MOVIE_TITLE = "title";
    public static final String COLUMN_MOVIE_BACKDROP_PATH = "backdrop_path";
    public static final String COLUMN_MOVIE_POSTER_PATH = "poster_path";
    public static final String COLUMN_MOVIE_RELEASE_DATE = "release_date";
    public static final String COLUMN_MOVIE_VOTE_AVERAGE = "vote_average";
    public static final String COLUMN_MOVIE_OVERVIEW = "overview";


    public static final String CREATE_TABLE =
            "CREATE TABLE "+ FAVOURITES_TABLE_NAME + "(" + COLUMN_MOVIE_ID + " TEXT NOT NULL UNIQUE," +
                    COLUMN_MOVIE_TITLE +" TEXT, " +
                    COLUMN_MOVIE_BACKDROP_PATH +" TEXT, " +
                    COLUMN_MOVIE_POSTER_PATH +" TEXT, " +
                    COLUMN_MOVIE_RELEASE_DATE + " TEXT, " +
                    COLUMN_MOVIE_VOTE_AVERAGE + " REAL, " +
                    COLUMN_MOVIE_OVERVIEW + " TEXT);";


    public class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS "+ FAVOURITES_TABLE_NAME);
            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS "+ FAVOURITES_TABLE_NAME);
            onCreate(db);
        }
    }
}
