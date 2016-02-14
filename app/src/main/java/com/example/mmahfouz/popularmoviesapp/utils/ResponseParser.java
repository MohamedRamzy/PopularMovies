package com.example.mmahfouz.popularmoviesapp.utils;

import com.example.mmahfouz.popularmoviesapp.model.Movie;
import com.example.mmahfouz.popularmoviesapp.model.Review;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mmahfouz on 1/11/2016.
 */
public class ResponseParser {

    public static Movie[] parseToMoviesArray(String jsonResponse) throws JSONException {
        if(jsonResponse == null)return null;

        JSONObject root = new JSONObject(jsonResponse);
        JSONArray results = root.getJSONArray("results");

        Movie[]movies = new Movie[results.length()];

        for (int i = 0; i < movies.length; i++) {
            JSONObject obj = results.getJSONObject(i);
            Movie movie= new Movie();
            movie.setAdult(obj.getBoolean("adult"));
            movie.setBackdrop_path(obj.getString("backdrop_path"));
            movie.setGenre_ids(obj.getString("genre_ids"));
            movie.setId(obj.getString("id"));
            movie.setOriginal_language(obj.getString("original_language"));
            movie.setTitle(obj.getString("original_title"));
            movie.setOverview(obj.getString("overview"));
            movie.setRelease_date(obj.getString("release_date"));
            movie.setPoster_path(obj.getString("poster_path"));
            movie.setPopularity(obj.getDouble("popularity"));
            movie.setTitle(obj.getString("title"));
            movie.setVideo(obj.getBoolean("video"));
            movie.setVote_average(obj.getDouble("vote_average"));
            movie.setVote_count(obj.getInt("vote_count"));
            movie.setIsFav(false); // for now, should query db for the favourites and them update it.
            movies[i] = movie;
        }

        return movies;
    }

    public static String[] parseToTrailersArray(String jsonResponse) throws JSONException {
        if(jsonResponse == null)return null;

        JSONObject root = new JSONObject(jsonResponse);
        JSONArray results = root.getJSONArray("results");

        String[]movies = new String[results.length()];

        for (int i = 0; i < movies.length; i++) {
            JSONObject obj = results.getJSONObject(i);
            String site = obj.getString("site");
            if(!site.equalsIgnoreCase("youtube"))
                continue;
            movies[i] = obj.getString("key");
        }

        return movies;
    }

    public static Review[] parseToReviewsArray(String jsonResponse) throws JSONException {
        if(jsonResponse == null)return null;

        JSONObject root = new JSONObject(jsonResponse);
        JSONArray results = root.getJSONArray("results");

        Review[]reviews = new Review[results.length()];

        for (int i = 0; i < reviews.length; i++) {
            JSONObject obj = results.getJSONObject(i);
            String author = obj.getString("author");
            String content = obj.getString("content");
            Review review = new Review();
            review.setAuthor(author);
            review.setContent(content);
            reviews[i] = review;
        }
        return reviews;
    }
}
