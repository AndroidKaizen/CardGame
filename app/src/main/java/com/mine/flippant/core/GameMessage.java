package com.mine.flippant.core;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by alphacom on 4/11/2016.
 * message format:
 *
 * 0.1. send hello
 *    [game:]<hello>player's id</hello>
 *
 * 1. provide question card
 *    [game:]<card>"sample question card"</card>
 *
 * 2. reply own answer
 *
 * 3. vote other's answer
 *    [game:]<vote id=[selected user's id]/>
 *
 * 4. win point
 *    [game:]<win id=[winner's id]>[point]</win>
 *
 * 5. game result
 *    [game:]<result id=[winner's id]></result>
 *
 *
 */
public class GameMessage {

    // For message type
    public static final int MESSAGE_NULL = 0;
    public static final int MESSAGE_HELLO = 10;
    public static final int MESSAGE_CARD = 1;
    public static final int MESSAGE_ANSWER = 2;
    public static final int MESSAGE_VOTE = 3;
    public static final int MESSAGE_WIN = 4;
    public static final int MESSAGE_RESULT = 5;

    // For parsing tags in raw game message
    public static final String TAG_NULL = "";
    public static final String TAG_HELLO = "hello";
    public static final String TAG_CARD = "card";
    public static final String TAG_ANSWER = "answer";
    public static final String TAG_VOTE = "vote";
    public static final String TAG_WIN = "win";
    public static final String TAG_RESULT = "result";

    private int type;
    private String user_id;
    private String body;
    private String round_id = "";

    public String getRound_id(){
        return round_id;
    }
    public String getUser_id() {
        return user_id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getType() {
        return type;
    }

    // Constructor
    public GameMessage() {

    }

    // For use in parsing general game message
    public GameMessage(String body) {
        xmlMessage(body.substring(7));
    }

    // For use in setting type and body of game message
    public GameMessage(int type, String body) {
        this.type = type;
        this.body = body;
    }

    // For use in setting type, body and userId of game message
    public GameMessage(int type, String body, String userId) {
        this.type = type;
        this.body = body;
        this.user_id = userId;
    }

    /**
     * @param body
     * @return GameMessage
     */
    // Decide whether input message is for game module or for chat module
    // return GameMessage with type, body, userId
    public static GameMessage parseMessage(String body) {
        if (!body.startsWith("[game:]"))
            return null;

        Log.println(Log.DEBUG, "game", "receive game message");

        return new GameMessage(body);
    }

    // Get and set type, body and userId of GameMessage from raw game message
    public boolean xmlMessage(String xmlBody) {
        //Get the DOM Builder Factory
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document = null;

        //Get the DOM Builder
        DocumentBuilder builder = null;

        try {
            builder = factory.newDocumentBuilder();
            //Load and Parse the XML document
            //document contains the complete XML as a Tree.
            document = builder.parse(new InputSource(new StringReader(xmlBody)));
            Node node = document.getDocumentElement();
            String nodeName = node.getNodeName();

            //TODO check location of setting round_id
            //round_id = node.getAttributes().getNamedItem("roundId").getNodeValue();
            if (nodeName.equals(TAG_HELLO)) {
                type = MESSAGE_HELLO;
                body = node.getTextContent().trim();
            } else if (nodeName.equals(TAG_CARD)) {
                type = MESSAGE_CARD;
                body = node.getTextContent().trim();
                round_id = node.getAttributes().getNamedItem("roundId").getNodeValue();
            } else if (nodeName.equals(TAG_ANSWER)) {
                type = MESSAGE_ANSWER;
                user_id = node.getAttributes().getNamedItem("id").getNodeValue();
                round_id = node.getAttributes().getNamedItem("roundId").getNodeValue();
                body = node.getTextContent().trim();
            } else if (nodeName.equals(TAG_VOTE)) {
                type = MESSAGE_VOTE;
                body = node.getTextContent().trim();
                user_id = node.getAttributes().getNamedItem("id").getNodeValue();
                round_id = node.getAttributes().getNamedItem("roundId").getNodeValue();
            } else if (nodeName.equals(TAG_WIN)) {
                type = MESSAGE_WIN;
                user_id = node.getAttributes().getNamedItem("id").getNodeValue();
                body = node.getTextContent().trim();
                round_id = node.getAttributes().getNamedItem("roundId").getNodeValue();
            } else if (nodeName.equals(TAG_RESULT)) {
                type = MESSAGE_RESULT;
                user_id = node.getAttributes().getNamedItem("id").getNodeValue();
//                round_id = node.getAttributes().getNamedItem("roundId").getNodeValue();
            } else {
                type = MESSAGE_NULL;

            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e){
            e.printStackTrace();
            return false;
        } catch (IOException e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    // Get composition of type, body, userId in order to use for later detecting
    // You can use it to make game message to send
    public static String compose(int type, String body, String userId, String roundId) {
        switch (type) {
            case MESSAGE_NULL:
                return "";

            case MESSAGE_HELLO:
                return "<hello>" + body + "</hello>";

            case MESSAGE_CARD:
                return "<card roundId='" + roundId + "'>" + body + "</card>";

            case MESSAGE_ANSWER:
                return "<answer roundId='" + roundId + "' id='" + userId + "'>" + body + "</answer>";

            case MESSAGE_VOTE:
                return "<vote roundId='" + roundId + "' id='" + userId + "'/>";

            case MESSAGE_WIN:
                return "<win roundId='" + roundId + "' id='" + userId + "'>" + body + "</win>";

            case MESSAGE_RESULT:
                return "<result id='" + userId + "'/>";

            default:
                return "";
        }
    }

    // Make final game message
    public String toString() {
        return "[game:]".concat(compose(type, body, user_id, Game.roundId));
    }
}

