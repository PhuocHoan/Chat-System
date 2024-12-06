package com.haichutieu.chatsystem.client.bus;

import com.haichutieu.chatsystem.client.SocketClient;
import com.haichutieu.chatsystem.client.util.SessionManager;

public class ChatAppController {
    public static void getChatList() {
        SocketClient.getInstance().sendMessages("CHAT_LIST " + SessionManager.getInstance().getCurrentUser().getId());
    }
}
