package com.mine.flippant.utils;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.mine.flippant.R;

public class Consts {

    // Consts for Authentification
    public static final String APP_ID = "38164";
    public static final String AUTH_KEY = "YYeR2UsEC4tAGs9";
    public static final String AUTH_SECRET = "O6YQNOaQsfq7gwM";
    public static final String ACCOUNT_KEY = "jkpLypfZcLQ4yR35zrnY";
    public static final String POSITION = "position";
    public static final String EMPTY_STRING = "";

    // Consts for Using Custom Object
    public static final String CLASS_SCORE = "FlipScore";
    public static final String STATUS_NEW = "New";
    public static final String STATUS_IN_PROCESS = "In Process";
    public static final String STATUS_DONE = "Done";
    public static final String CLASS_NAME = "FlipCard";
    public static final String QUESTION = "question";
    public static final String STATUS = "status";
    public static final String COMMENTS = "comments";


//    public static final String QB_APP_ID = "38183";
//    public static final String QB_AUTH_KEY = "TufQDrX9jSpt66x";
//    public static final String QB_AUTH_SECRET = "n-nR7E3pJGNR33y";
//
//    public static final String QB_DOMAIN = "api.stage.quickblox.com";

    // Universal Image Loader
    public static final DisplayImageOptions UIL_DEFAULT_DISPLAY_OPTIONS = new DisplayImageOptions.Builder()
            .cacheInMemory(true).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).cacheOnDisc(true).
                    considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565).build();


    public static final DisplayImageOptions UIL_USER_AVATAR_DISPLAY_OPTIONS = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.placeholder_user).showImageForEmptyUri(R.drawable.placeholder_user)
            .showImageOnFail(R.drawable.placeholder_user).cacheOnDisc(true).cacheInMemory(true).considerExifParams(true).build();

    public static final DisplayImageOptions UIL_GROUP_AVATAR_DISPLAY_OPTIONS = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.placeholder_group).showImageForEmptyUri(
                    R.drawable.placeholder_group).showImageOnFail(R.drawable.placeholder_group).cacheOnDisc(
                    true).cacheInMemory(true).considerExifParams(true).build();
}