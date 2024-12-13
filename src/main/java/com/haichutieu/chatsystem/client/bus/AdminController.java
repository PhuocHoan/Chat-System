package com.haichutieu.chatsystem.client.bus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.haichutieu.chatsystem.client.gui.adminPanel.AdminLogin;
import com.haichutieu.chatsystem.client.gui.adminPanel.FriendGUI;
import com.haichutieu.chatsystem.client.gui.adminPanel.ReportGUI;
import com.haichutieu.chatsystem.client.gui.adminPanel.UserManagement;
import com.haichutieu.chatsystem.dto.*;
import com.haichutieu.chatsystem.util.Util;

import java.util.List;

public class AdminController {
    public static void handleLoginAdmin(String message) {
        AdminLogin.getInstance().onLoginResponse(message);
    }

    public static void fetchAccountList(String message) {
        String[] parts = message.split(" ", 2);
        if (parts[0].equals("OK")) {
            try {
                List<Customer> accounts = Util.deserializeObject(parts[1], new TypeReference<>() {
                });
                UserManagement.getInstance().onFetchAccountList(accounts);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void handleLoginHistory(String message) {
        String[] parts = message.split(" ", 3);
        if (parts[0].equals("ALL")) {
            if (parts[1].equals("ERROR")) {
                ReportGUI.getInstance().onLoginHistoryReceived(false, null);
            } else {
                List<UserLoginTime> loginHistory;
                try {
                    System.out.println(parts[2]);
                    loginHistory = Util.deserializeObject(parts[2], new TypeReference<>() {
                    });
                    ReportGUI.getInstance().onLoginHistoryReceived(true, loginHistory);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } else if (parts[0].equals("USER")) {
            if (parts[1].equals("ERROR")) {
                UserManagement.getInstance().onLoginHistoryResponse(false, null, null);
            } else {
                List<LoginTime> loginTimes;
                try {
                    loginTimes = Util.deserializeObject(parts[2], new TypeReference<>() {
                    });
                    UserManagement.getInstance().onLoginHistoryResponse(true, parts[0], loginTimes);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void handleReport(String message) {
        String[] parts = message.split(" ", 2);
        if (parts[0].equals("OK")) {
            try {
                List<Customer> accounts = Util.deserializeObject(parts[1], new TypeReference<>() {
                });
                UserManagement.getInstance().onFetchAccountList(accounts);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void handleLockStatus(String message) {
        String[] parts = message.split(" ", 2);
        int id = Integer.parseInt(parts[1]);
        UserManagement.getInstance().onToggleLockStatus(parts[0].equals("OK"), id);
    }

    public static void handleChangePassword(String message) {
        String[] parts = message.split(" ", 2);
        int id = Integer.parseInt(parts[1]);
        UserManagement.getInstance().onChangePassword(parts[0].equals("OK"), id);
    }

    public static void handleSpamList(String part) {
        String[] parts = part.split(" ", 2);
        if (parts[0].equals("OK")) {
            try {
                List<SpamList> spamList = Util.deserializeObject(parts[1], new TypeReference<>() {
                });
                ReportGUI.getInstance().onSpamListReceived(true, spamList);
            } catch (Exception e) {
                e.printStackTrace();
                ReportGUI.getInstance().onSpamListReceived(false, null);
            }
        } else {
            ReportGUI.getInstance().onSpamListReceived(false, null);
        }
    }

    public static void handleLockAccount(String part) {
        String[] parts = part.split(" ", 2);
        ReportGUI.getInstance().onLockStatusResponse(parts[0].equals("OK"), Integer.parseInt(parts[1]));
    }

    public static void handleFriendCount(String part) {
        String[] parts = part.split(" ", 2);
        if (parts[0].equals("OK")) {
            try {
                List<FriendCount> friendCountList = Util.deserializeObject(parts[1], new TypeReference<>() {
                });
                FriendGUI.getInstance().onFriendCountTable(friendCountList);
            } catch (Exception e) {
                e.printStackTrace();
                FriendGUI.getInstance().onFriendCountTable(null);
            }
        } else {
            FriendGUI.getInstance().onFriendCountTable(null);
        }
    }
}
