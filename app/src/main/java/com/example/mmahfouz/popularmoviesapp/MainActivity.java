package com.example.mmahfouz.popularmoviesapp;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mmahfouz.popularmoviesapp.database.MoviesDBManager;
import com.example.mmahfouz.popularmoviesapp.model.Movie;
import com.example.mmahfouz.popularmoviesapp.utils.Constants;
import com.example.mmahfouz.popularmoviesapp.utils.ResponseParser;
import com.example.mmahfouz.popularmoviesapp.utils.Utility;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

public class MainActivity extends ActionBarActivity {

    /* course link              https://www.udacity.com/course/progress#!/c-ud853
        project description     https://docs.google.com/document/d/1ZlN1fUsCSKuInLECcJkslIqvpKlP7jWL2TP9m6UiA6I/pub?embedded=true#h.7sxo8jefdfll
        movies API              http://docs.themoviedb.apiary.io/
    * */
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private ImageAdapter mImageAdapter;

    private Movie[] mMovies;
    private Movie[] mPopMovies;
    private Movie[] mRatedMovies;
    private Movie[] mFavMovies;
//    private Map<String,Movie> mAllMovies;

    private GridView gridView;
    private TextView noMoviesView;
    private TextView listTitle;

    private int showOperation;

    private MoviesDBManager dbManager;
//    private String moviesJSONString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setTitle("Popular Movies");
            actionBar.setLogo(R.drawable.popcorn);
            actionBar.setDisplayUseLogoEnabled(true);
//            actionBar.setDisplayShowHomeEnabled(true);
        }

        dbManager = MoviesDBManager.getInstance(this);

        gridView = (GridView) findViewById(R.id.gridview);
        mImageAdapter = new ImageAdapter(this);
        gridView.setAdapter(mImageAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(MainActivity.this, "image " + position + " clicked!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, MovieDetailActivity.class);
                intent.putExtra("selectedMovieDetails", mMovies[position]);
                startActivity(intent);
            }
        });

        noMoviesView = (TextView) findViewById(R.id.no_movies_text_view);
        listTitle = (TextView) findViewById(R.id.list_title);
        gridView.setVisibility(View.VISIBLE);
        noMoviesView.setVisibility(View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initData();
    }

    public void initData(){
        fetchShowOperationFromPreferences();
        new FetchMoviesTask(this).execute();
    }

    private void loadFavouriteMovies(){

        Cursor cursor = dbManager.queryAllFavourites();
        if (cursor.getCount() <= 0) {
            mFavMovies = null;
        } else {
            mFavMovies = new Movie[cursor.getCount()];
            int i = 0;
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                Movie movie = new Movie();
                movie.setId(cursor.getString(cursor.getColumnIndex(MoviesDBManager.COLUMN_MOVIE_ID)));
                movie.setTitle(cursor.getString(cursor.getColumnIndex(MoviesDBManager.COLUMN_MOVIE_TITLE)));
                movie.setBackdrop_path(cursor.getString(cursor.getColumnIndex(MoviesDBManager.COLUMN_MOVIE_BACKDROP_PATH)));
                movie.setPoster_path(cursor.getString(cursor.getColumnIndex(MoviesDBManager.COLUMN_MOVIE_POSTER_PATH)));
                movie.setRelease_date(cursor.getString(cursor.getColumnIndex(MoviesDBManager.COLUMN_MOVIE_RELEASE_DATE)));
                movie.setVote_average(cursor.getDouble(cursor.getColumnIndex(MoviesDBManager.COLUMN_MOVIE_VOTE_AVERAGE)));
                movie.setOverview(cursor.getString(cursor.getColumnIndex(MoviesDBManager.COLUMN_MOVIE_OVERVIEW)));
                mFavMovies[i++] = movie;
            }
        }

    }

    private void showMovies(){

        if (showOperation == Constants.MOST_POPULAR_OP) {
            listTitle.setText(getString(R.string.label_popular_movies));
            mMovies = mPopMovies;
        } else if (showOperation == Constants.TOP_RATED_OP) {
            listTitle.setText(getString(R.string.label_top_rated_movies));
            mMovies = mRatedMovies;
        }else if (showOperation == Constants.FAVOURITE_OP) {
            listTitle.setText(getString(R.string.label_favourite_movies));
            mMovies = mFavMovies;
        }

        if (mMovies == null || mMovies.length == 0) {
            if (showOperation == Constants.MOST_POPULAR_OP)
                noMoviesView.setText(getString(R.string.label_no_popular_movies));
            else if (showOperation == Constants.TOP_RATED_OP)
                noMoviesView.setText(getString(R.string.label_no_top_rated_movies));
            else { // FAV
                noMoviesView.setText(getString(R.string.label_no_favourite_movies));
            }
            gridView.setVisibility(View.GONE);
            noMoviesView.setVisibility(View.VISIBLE);
        } else {
            gridView.setVisibility(View.VISIBLE);
            noMoviesView.setVisibility(View.GONE);
        }
        mImageAdapter.notifyDataSetChanged();
    }

    public void parseData(String popMoviesJsonStr, String ratedMoviesJsonStr){
        try {
            mPopMovies = ResponseParser.parseToMoviesArray(popMoviesJsonStr);
            mRatedMovies = ResponseParser.parseToMoviesArray(ratedMoviesJsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void fetchShowOperationFromPreferences(){
        String sortingType = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.sort_by_key), getString(R.string.sort_by_default));
        showOperation = Constants.MOST_POPULAR_OP;;
        if(sortingType.equals(getString(R.string.sort_by_popular))){
            showOperation = Constants.MOST_POPULAR_OP;
        }else if(sortingType.equals(getString(R.string.sort_by_rating))){
            showOperation = Constants.TOP_RATED_OP;
        }else if(sortingType.equals(getString(R.string.sort_by_favourite))){
            showOperation = Constants.FAVOURITE_OP;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }else if (id == R.id.action_load) {
            Log.v(LOG_TAG, "onOptionsItemSelected");
//            initData();
//            parseData(moviesJSONString);
        }

        return super.onOptionsItemSelected(item);
    }

    public class ImageAdapter extends BaseAdapter{

        private Context mContext;
        private Point screenDimension;

        public ImageAdapter(Context context){
            this.mContext = context;
            screenDimension = getScreenDimensions();
            Log.v(LOG_TAG,"dimensions : "+screenDimension.x+","+screenDimension.y);
        }

        @Override
        public int getCount() {
            if(mMovies == null) return  0;
            return mMovies.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            int padding = 30;
            int columns = 3;
            int imageWidth = (screenDimension.x - 4*padding)/columns;

            ImageView imageView;
            if(convertView == null){
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(imageWidth,imageWidth));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8,8,8,8);

            }else{
                imageView = (ImageView) convertView;
            }


            // adding favourite from the 2 types of list and when loading the favouries while only one type is downloaded from server leaves some movies not present
            // should store the all data of the favourite movies locally/independentely
            if(checkInternetConenction()){
                String imgUrl = Constants.API_BASE_URL + Constants.POSTER_SIZE + "/" + mMovies[position].getBackdrop_path(); //"http://image.tmdb.org/t/p/w185/razvUuLkF7CX4XsLyj02ksC0ayy.jpg"
                Picasso.with(mContext).load(imgUrl).into(imageView);
            }else{
                //Toast.makeText(mContext, "Sorry, couldn't load image, no connection to internet!", Toast.LENGTH_SHORT).show();
                imageView.setImageResource(R.drawable.no_image);
            }

            return imageView;
        }
        public Point getScreenDimensions(){
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point p = new Point();
            display.getSize(p);
            return p;
        }
    }


    private boolean checkInternetConenction() {
        // get Connectivity Manager object to check connection
        ConnectivityManager connec =(ConnectivityManager)getSystemService(getBaseContext().CONNECTIVITY_SERVICE);

        // Check for network connections
        if ( connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||

                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED ) {
            //Toast.makeText(this, " Connected ", Toast.LENGTH_LONG).show();
            return true;
        }else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED  ) {
            //Toast.makeText(this, " Not Connected ", Toast.LENGTH_LONG).show();
            return false;
        }
        return false;
    }

    public class FetchMoviesTask extends AsyncTask<Void,Void,Void> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        private ProgressDialog dialog;
        private Context mContext;

        private String popMoviesJsonStr;
        private String ratedMoviesJsonStr;

        public FetchMoviesTask(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(mContext, "", "Loading...");
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            loadPopMov();
            loadRatedMov();
            return null;
        }

        protected Void loadPopMov(){
            Uri uri = Uri.parse(Constants.MOST_POPULAR_URL)
                    .buildUpon()
                    .appendQueryParameter(Constants.API_KEY_PARAM, Constants.API_KEY).build();

            popMoviesJsonStr = Utility.makeHTTPRequest(uri);

            Log.v(LOG_TAG, popMoviesJsonStr.substring(popMoviesJsonStr.length() - 50));
            return null;
        }

        protected Void loadRatedMov(){
            Uri uri = Uri.parse(Constants.TOP_RATED_URL)
                    .buildUpon()
                    .appendQueryParameter(Constants.API_KEY_PARAM, Constants.API_KEY).build();

            ratedMoviesJsonStr = Utility.makeHTTPRequest(uri);

            Log.v(LOG_TAG, ratedMoviesJsonStr.substring(ratedMoviesJsonStr.length() - 50));
            return null;
        }



        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
//            moviesJSONString = moviesJsonStr;
            Log.v(LOG_TAG, "onPostExecute");
            parseData(popMoviesJsonStr, ratedMoviesJsonStr);
            loadFavouriteMovies();
            showMovies();
        }
    }
}
