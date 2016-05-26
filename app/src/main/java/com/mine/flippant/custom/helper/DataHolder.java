package com.mine.flippant.custom.helper;

import com.mine.flippant.models.Question;
import com.quickblox.customobjects.model.QBCustomObject;

import java.util.ArrayList;
import java.util.List;

public class DataHolder {

    private static DataHolder dataHolder;
    private int signInUserId;
    private List<Question> questionList;

    public static synchronized DataHolder getDataHolder() {
        if (dataHolder == null) {
            dataHolder = new DataHolder();
        }
        return dataHolder;
    }

    public DataHolder() {
        if (questionList == null) {
            questionList = new ArrayList<>();
        }
    }

    public int getSignInUserId() {
        return signInUserId;
    }

    public void setSignInUserId(int signInUserId) {
        this.signInUserId = signInUserId;
    }

    public int getQuestionListSize() {
        return questionList.size();
    }

    public String getQuestionCard(int position) {
        if (questionList.size() == 0)
            return "";
        else if (questionList.size() <= position)
            return "";
        else
            return questionList.get(position).getQuestionCard();
    }

    public String getQuestionDate(int position) {
        return questionList.get(position).getDate();
    }

    /*public List<String> getQuestionComments(int position) {
        return questionList.get(position).getCommentsList();
    }

    public String getQuestionStatus(int position) {
        return questionList.get(position).getStatus();
    }*/

    public String getQuestionId(int position) {
        return questionList.get(position).getId();
    }

    public void setQuestionToQuestionList(int position, Question note) {
        questionList.set(position, note);
    }

    /*public void addNewComment(int questionPosition, String comment) {
        questionList.get(questionPosition).addNewComment(comment);
    }

    public String getComments(int questionPosition) {
        return questionList.get(questionPosition).getComments();
    }*/

    public void removeQuestionFromList(int position) {
        questionList.remove(position);
    }

    public void clear() {
        questionList.clear();
    }

    public int size() {
        return questionList.size();
    }

    public void addQuestionToList(QBCustomObject customObject) {
        questionList.add(new Question(customObject));
    }
}