package com.mine.flippant.utils;

import com.mine.flippant.R;

public class OnlineStatusHelper {

    public static int getOnlineStatus(boolean online) {
        if (online) {
            return R.string.frl_online;
        } else {
            return R.string.frl_offline;
        }
    }
}