package com.example.mmahfouz.popularmoviesapp;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mmahfouz.popularmoviesapp.database.MoviesDBManager;
import com.example.mmahfouz.popularmoviesapp.model.Movie;
import com.example.mmahfouz.popularmoviesapp.model.Review;
import com.example.mmahfouz.popularmoviesapp.utils.Constants;
import com.example.mmahfouz.popularmoviesapp.utils.DownloadImageTask;
import com.example.mmahfouz.popularmoviesapp.utils.ResponseParser;
import com.example.mmahfouz.popularmoviesapp.utils.Utility;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

public class MovieDetailActivity extends ActionBarActivity implements View.OnClickListener {

    private final String LOG_TAG = MovieDetailActivity.class.getSimpleName();

    private ImageView imageView;
    private ImageView favIcon;
    private TextView title;
    private TextView overview;
    private TextView year;
    private TextView vote_average;
    private TextView popularity;
    private Movie mMovie;

    private String []trailersKeys;
    private LinearLayout trailersListView;

    private boolean reviewsLoadedAlready;
    private boolean trailersLoadedAlready;

//    private TrailersListAdapter trailersListAdapter;

    private Review []reviewsList;
    private LinearLayout reviewsListView;
//    private ReviewsListAdapter reviewsListAdapter;

    private MoviesDBManager dbManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        Intent intent = getIntent();
        if(intent != null) {
            mMovie = intent.getParcelableExtra("selectedMovieDetails");
        }
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setLogo(R.drawable.popcorn);
            actionBar.setDisplayUseLogoEnabled(true);
//            actionBar.setDisplayShowHomeEnabled(true);
            if(mMovie != null)
                actionBar.setTitle(mMovie.getTitle() + " ("+mMovie.getRelease_date().substring(0,4)+")");
        }

        dbManager = MoviesDBManager.getInstance(this);

        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setMinimumWidth(720);
        imageView.setMinimumHeight(1024);

        favIcon = (ImageView) findViewById(R.id.fav_icon);
        favIcon.setOnClickListener(this);

        // check internet connection, if not available, show no_image icon
        String imgUrl = Constants.API_BASE_URL + Constants.DETAILS_SIZE + "/" + mMovie.getPoster_path(); //"http://image.tmdb.org/t/p/w185/razvUuLkF7CX4XsLyj02ksC0ayy.jpg"

        if(checkInternetConenction()){
            loadImageWithNativeAsyncTask(imageView, imgUrl);
            //loadImageWithPicassa(imageView, imgUrl);
            Log.v(LOG_TAG,imgUrl);
        }else{
            Toast.makeText(this, "Sorry, couldn't load image, no connection to internet!", Toast.LENGTH_LONG).show();
            imageView.setImageResource(R.drawable.no_image);
        }


        title = (TextView) findViewById(R.id.original_title);
        overview = (TextView) findViewById(R.id.overview);
        year = (TextView) findViewById(R.id.year);
        vote_average = (TextView) findViewById(R.id.vote_average);
        popularity = (TextView) findViewById(R.id.popularity);

        title.setText(mMovie.getTitle());
        overview.setText(" " + mMovie.getOverview());
        year.setText(mMovie.getRelease_date().substring(0, 4));
        vote_average.setText(String.format("%.1f/10", mMovie.getVote_average()));
        popularity.setText("Popularity : " + String.valueOf(mMovie.getPopularity()));


        if(dbManager.queryIsFavourite(mMovie.getId())) {
            mMovie.setIsFav(true);
            favIcon.setImageResource(R.drawable.fav_on);
        }else{
            mMovie.setIsFav(false);
            favIcon.setImageResource(R.drawable.fav_off);
        }


        trailersListView = (LinearLayout) findViewById(R.id.trailersListView);

        reviewsListView = (LinearLayout) findViewById(R.id.reviewsListView);

        reviewsLoadedAlready = false;
        trailersLoadedAlready = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        new FetchTrailersReviewsTask(this).execute();
    }

    private void initReviewsList() {
        if(reviewsLoadedAlready)
            return;

        for (Review review : reviewsList) {
            View vi = LayoutInflater.from(this).inflate(R.layout.reviews_list_item, null);

            TextView authorTV = (TextView) vi.findViewById(R.id.author_text_view);
            TextView contentTV = (TextView) vi.findViewById(R.id.content_text_view);
            authorTV.setText(review.getAuthor());
            if(review.getContent().length() > 50)
                contentTV.setText(review.getContent().substring(0,50)+" .. (Click to full review)");
            else
                contentTV.setText(review.getContent());

            final String content = review.getContent();
            vi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //open a dialog with the full content of the review
                    Toast.makeText(getBaseContext(), content.substring(0,content.length()>200?200:content.length())+" ...", Toast.LENGTH_LONG).show();
                }
            });
            reviewsListView.addView(vi);
        }
        reviewsLoadedAlready = true;
    }

    private void initTrailersList() {
        if(trailersLoadedAlready)
            return;
        int i = 1;
        for (final String trailerKey : trailersKeys) {
            View vi = LayoutInflater.from(this).inflate(R.layout.trailers_list_item, null);

            TextView trailerTV = (TextView) vi.findViewById(R.id.trailerTextView);
            trailerTV.setText("Trailer "+i);
            vi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //open a dialog with the full content of the review
                    watchYoutubeVideo(trailerKey);
                }
            });
            trailersListView.addView(vi);
            i++;
        }
        trailersLoadedAlready = true;
    }


    void loadImageWithPicassa(ImageView imgView,String imgUrl){
        Picasso.with(this).load(imgUrl).into(imgView);
    }

    void loadImageWithNativeAsyncTask(ImageView imgView,String imgUrl){
        ProgressDialog dialog = ProgressDialog.show(MovieDetailActivity.this, "", "Loading...", true);
        dialog.show();
        new DownloadImageTask(imgView,this).execute(imgUrl);
        dialog.dismiss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fav_icon:
                if(mMovie.getIsFav() == true) {
                    favIcon.setImageResource(R.drawable.fav_off);
//                    mMovie.setIsFav(false); // meaningless, because its a local change, no update to the original object. DELETE THEM AFTER DB operation
                    //update the database to remove this movie from favourite
                    dbManager.removeFromFavourite(mMovie.getId());
                    Toast.makeText(this, "Movie removed from favorites", Toast.LENGTH_SHORT).show();
                    mMovie.setIsFav(false);
                }else{
                    favIcon.setImageResource(R.drawable.fav_on);
//                    mMovie.setIsFav(true); // meaningless, because its a local change, no update to the original object. DELETE THEM AFTER DB operation
                    //update the database to add this movie to favourite
                    dbManager.addToFavourite(mMovie);
                    Toast.makeText(this, "Movie added from favorites", Toast.LENGTH_SHORT).show();
                    mMovie.setIsFav(true);
                }
                break;
            default:break;
        }
    }

    public void watchYoutubeVideo(String id){
        try{
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
            startActivity(intent);
        }catch (ActivityNotFoundException ex){
            Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v="+id));
            startActivity(intent);
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

    private void parseData(String trailersJsonStr, String reviewsJsonStr) {
        try {

            trailersKeys = ResponseParser.parseToTrailersArray(trailersJsonStr);
            reviewsList = ResponseParser.parseToReviewsArray(reviewsJsonStr);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public class FetchTrailersReviewsTask extends AsyncTask<Void,Void,Void> {

        private final String LOG_TAG = FetchTrailersReviewsTask.class.getSimpleName();

        private ProgressDialog dialog;
        private Context mContext;

        private String trailersJsonStr;
        private String reviewsJsonStr;

        public FetchTrailersReviewsTask(Context mContext) {
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
            loadTrailers();
            loadReviews();
            return null;
        }

        protected Void loadTrailers(){
            Uri uri = Uri.parse(Constants.TRAILERS_URL.replace("?",mMovie.getId()))
                    .buildUpon()
                    .appendQueryParameter(Constants.API_KEY_PARAM, Constants.API_KEY).build();

            trailersJsonStr = Utility.makeHTTPRequest(uri);

            Log.v(LOG_TAG, trailersJsonStr.substring(trailersJsonStr.length() - 50));
            return null;
        }

        protected Void loadReviews(){
            Uri uri = Uri.parse(Constants.REVIEWS_URL.replace("?",mMovie.getId()))
                    .buildUpon()
                    .appendQueryParameter(Constants.API_KEY_PARAM, Constants.API_KEY).build();

            reviewsJsonStr = Utility.makeHTTPRequest(uri);

            Log.v(LOG_TAG, reviewsJsonStr.substring(reviewsJsonStr.length() - 50));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            Log.v(LOG_TAG, "onPostExecute");
            parseData(trailersJsonStr, reviewsJsonStr);
            initReviewsList();
            initTrailersList();
        }
    }



}
