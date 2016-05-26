package com.mine.flippant.models;

import com.mine.flippant.utils.Consts;
import com.quickblox.customobjects.model.QBCustomObject;

public class Question {

    private String id;
    private String questionCard;
    private String date;
//  private List<String> commentsList;

    public Question(QBCustomObject customObject) {
        id = customObject.getCustomObjectId();
        questionCard = parseField(Consts.QUESTION, customObject);
        date = customObject.getUpdatedAt().toString();
    /*    commentsList = new ArrayList<String>();
        String commentString = parseField(Consts.COMMENTS, customObject);
        if (commentString != null) {
            String[] comments = commentString.split("/");
            Collections.addAll(this.commentsList, comments);
        }*/
    }

    private String parseField(String field, QBCustomObject customObject) {
        Object object = customObject.getFields().get(field);
        if (object != null) {
            return object.toString();
        }
        return null;
    }

    public String getQuestionCard() {
        return questionCard;
    }

    public String getDate() {
        return date;
    }

    /*public List<String> getCommentsList() {
        return commentsList;
    }*/

    /*public String getComments() {
        String comments = "";
        for (String comment : commentsList) {
            comments += comment + "/";
        }
        return comments;
    }

    public void addNewComment(String comment) {
        commentsList.add(comment);
    }*/

    public String getId() {
        return id;
    }
}