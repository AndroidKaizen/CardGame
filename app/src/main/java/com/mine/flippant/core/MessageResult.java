package com.mine.flippant.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alphacom on 4/20/2016.
 */
public class MessageResult {
    public static int messageType;
    public static String userId;
    public boolean isGameMessage = false;
    public List<GameMessage> sendList = new ArrayList<>();
    public static String questionCard = "";
    public static String toast_msg = "";
    public static String point;
}
