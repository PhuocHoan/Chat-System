package com.haichutieu.chatsystem.client.bus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.haichutieu.chatsystem.client.SocketClient;
import com.haichutieu.chatsystem.client.gui.adminPanel.*;
import com.haichutieu.chatsystem.dto.*;
import com.haichutieu.chatsystem.util.Util;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

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
                UserAndFriendGUI.getInstance().onFriendCountTable(friendCountList);
            } catch (Exception e) {
                e.printStackTrace();
                UserAndFriendGUI.getInstance().onFriendCountTable(null);
            }
        } else {
            UserAndFriendGUI.getInstance().onFriendCountTable(null);
        }
    }

    // fetch all group, not dual group
    public static void fetchGroupList() {
        SocketClient.getInstance().sendMessages("FETCH_GROUP_LIST ALL");
    }

    // fetch all group, not dual group
    public static void handleGroupList(String message) {
        List<Conversation> groupList = Util.deserializeObject(message, new TypeReference<>() {
        });
        ChatGroup.getInstance().onFetchGroupList(groupList);
    }

    // fetch all member in group, not dual group
    public static void fetchMemberList(Long conversationID) {
        SocketClient.getInstance().sendMessages("FETCH_MEMBER_LIST " + conversationID);
    }

    // fetch all member in group, not dual group
    public static void handleMemberList(String message) {
        List<MemberConversation> memberList = Util.deserializeObject(message, new TypeReference<>() {
        });
        ChatGroup.getInstance().onFetchMemberList(memberList);
    }

    // fetch login user count list
    public static void fetchOnlineUserCountList() {
        SocketClient.getInstance().sendMessages("FETCH_ONLINE_USER_COUNT_LIST ALL");
    }

    // fetch login user count list with time range
    public static void fetchOnlineUserCountList(Timestamp from, Timestamp to) {
        SocketClient.getInstance().sendMessages("FETCH_ONLINE_USER_COUNT_TIME_RANGE_LIST " + from + " END " + to);
    }

    public static void handleOnlineUserCountList(String message) {
        List<OnlineUserCount> onlineUserCount = Util.deserializeObject(message, new TypeReference<>() {
        });
        UserAndFriendGUI.getInstance().onOnlineUserTable(onlineUserCount);
    }

    // fetch number of years have new users
    public static void fetchNewUsersMonthly() {
        SocketClient.getInstance().sendMessages("FETCH_NEW_USERS_MONTHLY ALL");
    }

    public static void handleNewUsersMonthly(String message) {
        Map<Integer, List<Long>> newUsers = Util.deserializeObject(message, new TypeReference<>() {
        });
        Statistics.getInstance().onFetchNewUserMonthly(newUsers);
    }

    // fetch number of years have new users
    public static void fetchAppUsageMonthly() {
        SocketClient.getInstance().sendMessages("FETCH_APP_USAGE_MONTHLY ALL");
    }

    public static void handleAppUsageMonthly(String message) {
        Map<Integer, List<Long>> appUsage = Util.deserializeObject(message, new TypeReference<>() {
        });
        Statistics.getInstance().onFetchAppUsageMonthly(appUsage);
    }
}
