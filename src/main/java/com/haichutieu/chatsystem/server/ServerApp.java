package com.haichutieu.chatsystem.server;

import com.haichutieu.chatsystem.dto.Customer;
import com.haichutieu.chatsystem.server.dal.FriendsService;

import java.util.List;

public class ServerApp {
    public static void main(String[] args) throws InterruptedException {
        SocketServer.getInstance();
    }
}
