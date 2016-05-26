package com.mine.flippant.ui.adapters;

import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mine.flippant.R;
import com.mine.flippant.core.Game;
import com.mine.flippant.core.MessageResult;
import com.mine.flippant.ui.activities.GameActivity;
import com.mine.flippant.utils.TimeUtils;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.users.model.QBUser;

import java.util.List;

//import vc908.stickerfactory.StickersManager;

public class ChatAdapter extends BaseAdapter {
    private static final String TAG = ChatAdapter.class.getSimpleName();

    private final List<QBChatMessage> chatMessages;
    private AppCompatActivity context;
    private enum ChatItemType {
        Message,
        Sticker
    }

    public ChatAdapter(AppCompatActivity context, List<QBChatMessage> chatMessages) {
        this.context = context;
        this.chatMessages = chatMessages;
    }

    @Override
    public int getCount() {
        if (chatMessages != null) {
            return chatMessages.size();
        } else {
            return 0;
        }
    }

    @Override
    public QBChatMessage getItem(int position) {
        if (chatMessages != null) {
            return chatMessages.get(position);
        } else {
            return null;
        }
    }

    @Override
    public int getViewTypeCount() {
        return ChatItemType.values().length;
    }

//    @Override
//    public int getItemViewType(int position) {
//        return StickersManager.isSticker(getItem(position).getBody())
//                ? ChatItemType.Sticker.ordinal()
//                : ChatItemType.Message.ordinal();
//    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG, "getView: " + position);
        //save current message count
        if (position > Game.msg_count) Game.msg_count = position + 1;
        if (!Game.chat_opened) {
            Log.d(TAG, "chat_closed");
            String str = String.valueOf(Game.msg_count - Game.msg_read);
            GameActivity.unread_count_text.setText(str);
        }
        else {
            Log.d(TAG, "chat_opened");
            GameActivity.unread_count_text.setText("0");
        }

        ViewHolder holder;
        QBChatMessage chatMessage = getItem(position);
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            if (getItemViewType(position) == ChatItemType.Sticker.ordinal()) {
                //convertView = vi.inflate(R.layout.list_item_sticker, parent, false);
            } else {
                convertView = vi.inflate(R.layout.list_item_message, parent, false);
            }
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        QBUser currentUser = Game.currentQBUser;
        boolean isOutgoing = chatMessage.getSenderId() == null || chatMessage.getSenderId().equals(currentUser.getId());
        setAlignment(holder, isOutgoing);
//        if (StickersManager.isSticker(chatMessage.getBody())) {
//            StickersManager.with(convertView.getContext())
//                    .loadSticker(chatMessage.getBody())
//                    .setPlaceholderColorFilterRes(android.R.color.darker_gray)
//                    .into(holder.stickerView);
//        } else

        if (holder.txtMessage != null) {
            holder.txtMessage.setText(chatMessage.getBody());
        }
        if (chatMessage.getSenderId() != null) {
            String loginStr = "";
            String infoMsg = "";
            for(int i = 0; i < Game.gameUsers.size(); i++){
                if (chatMessage.getSenderId().equals(Game.gameUsers.get(i).getId())) {
                    loginStr = Game.gameUsers.get(i).getLogin();
                    break;
                }
            }
            infoMsg = loginStr + ": " + getTimeText(chatMessage);
            holder.txtInfo.setText(infoMsg);
        } else {
            holder.txtInfo.setText(getTimeText(chatMessage));
        }
        return convertView;
    }

    /**
     * module for adding messages
     * 1. detect whether the message have to be added or not using Game.onReadyMessage
     * 2. also     if yes, set type, body, user_id
     * @param message
     */
    public void add(QBChatMessage message) {
        Log.d(TAG, "add: " + message.getBody());
        MessageResult result = Game.onReadyMessage(message.getBody());
//        if (!result.isGameMessage) {
//            // Add messages to show in chat ???
//            chatMessages.add(message);
//        }
        chatMessages.add(message);
        GameActivity activity = (GameActivity)context;
        if (result.sendList.size() > 0) {
            activity.sendPendingMessages(result);
        }
        activity.updateUI();
    }

    public void add(List<QBChatMessage> messages) {
        chatMessages.addAll(messages);
    }

    private void setAlignment(ViewHolder holder, boolean isOutgoing) {
        if (!isOutgoing) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            holder.content.setLayoutParams(lp);

            layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.txtInfo.setLayoutParams(layoutParams);
            Log.i(TAG, holder.txtInfo.getText().toString());
            if (holder.txtMessage != null) {
                holder.contentWithBG.setBackgroundResource(R.drawable.incoming_message_bg);
                layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
                layoutParams.gravity = Gravity.RIGHT;
                holder.txtMessage.setLayoutParams(layoutParams);
            } else {
                holder.contentWithBG.setBackgroundResource(android.R.color.transparent);
            }
        } else {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            holder.content.setLayoutParams(lp);

            layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.txtInfo.setLayoutParams(layoutParams);

            if (holder.txtMessage != null) {
                holder.contentWithBG.setBackgroundResource(R.drawable.outgoing_message_bg);
                layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
                layoutParams.gravity = Gravity.LEFT;
                holder.txtMessage.setLayoutParams(layoutParams);
            } else {
                holder.contentWithBG.setBackgroundResource(android.R.color.transparent);
            }
        }
//        TextView infoText = holder.txtInfo;
//        if (!infoText.getText().toString().equals("")) {
//            String idStr = infoText.getText().toString().substring(0, 8);
//            String otherStr = infoText.getText().toString().substring(9);
//            for(int i = 0; i < Game.gameUsers.size(); i++){
//                if (idStr.equals(Game.gameUsers.get(i).getId().toString())) {
//                    idStr = Game.gameUsers.get(i).getLogin();
//                    Log.i(TAG, "id:  " + idStr + "  rest:  " + otherStr);
//                    break;
//                }
//            }
//            holder.txtInfo.setText(idStr.concat(otherStr));
//        }
    }

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.txtMessage = (TextView) v.findViewById(R.id.txtMessage);
        holder.content = (LinearLayout) v.findViewById(R.id.content);
        holder.contentWithBG = (LinearLayout) v.findViewById(R.id.contentWithBackground);
        holder.txtInfo = (TextView) v.findViewById(R.id.txtInfo);
        return holder;
    }

    private String getTimeText(QBChatMessage message) {
        return TimeUtils.millisToLongDHMS(message.getDateSent() * 1000);
    }

    private static class ViewHolder {
        public TextView txtMessage;
        public TextView txtInfo;
        public LinearLayout content;
        public LinearLayout contentWithBG;
        public ImageView stickerView;
    }
}
