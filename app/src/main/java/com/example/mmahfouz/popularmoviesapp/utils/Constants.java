package com.example.mmahfouz.popularmoviesapp.utils;

/**
 * Created by mmahfouz on 1/11/2016.
 */
public class Constants {

    // add all these constants of all classes to a Constants class
    public static final String API_BASE_URL = "http://image.tmdb.org/t/p/";
    public static final String[] apiImageSizes = {"w92", "w154", "w185", "w342", "w500", "w780", "original"};
    public static String POSTER_SIZE = apiImageSizes[2]; // "w185"
    public static String DETAILS_SIZE = apiImageSizes[3]; // "w342"

    public static  final String MOST_POPULAR_URL = "http://api.themoviedb.org/3/movie/popular?page=1";
    public static  final String TOP_RATED_URL = "http://api.themoviedb.org/3/movie/top_rated?page=1";
    public static  final String TRAILERS_URL = "http://api.themoviedb.org/3/movie/?/videos";
    public static  final String REVIEWS_URL = "http://api.themoviedb.org/3/movie/?/reviews";



    public static  final String API_KEY_PARAM = "api_key";
    public static  final String API_KEY = "18fa1db94fa55e20469fa648526e4f95";

    public static final int MOST_POPULAR_OP = 0;
    public static final int TOP_RATED_OP = 1;
    public static final int FAVOURITE_OP = 2;
}
