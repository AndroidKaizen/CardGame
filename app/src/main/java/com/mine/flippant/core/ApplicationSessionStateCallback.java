package com.mine.flippant.core;

/**
 * Created by igorkhomenko on 4/29/15.
 */
public interface ApplicationSessionStateCallback {
    void onStartSessionRecreation();
    void onFinishSessionRecreation(boolean success);
}
