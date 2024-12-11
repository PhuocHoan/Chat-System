package com.haichutieu.chatsystem.client.bus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haichutieu.chatsystem.client.gui.adminPanel.AdminLogin;
import com.haichutieu.chatsystem.client.gui.adminPanel.ReportGUI;
import com.haichutieu.chatsystem.client.gui.adminPanel.UserManagement;
import com.haichutieu.chatsystem.dto.Customer;
import com.haichutieu.chatsystem.dto.LoginTime;
import com.haichutieu.chatsystem.dto.UserLoginTime;

import java.util.List;

public class AdminController {
    public static void handleLoginAdmin(String message) {
        AdminLogin.getInstance().onLoginResponse(message);
    }

    public static void fetchAccountList(String message) {
        String[] parts = message.split(" ", 2);
        if (parts[0].equals("OK")) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                List<Customer> accounts = objectMapper.readValue(parts[1], new TypeReference<List<Customer>>() {
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
                List<UserLoginTime> loginHistory = null;
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    System.out.println(parts[2]);
                    loginHistory = objectMapper.readValue(parts[2], new TypeReference<List<UserLoginTime>>() {
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
                List<LoginTime> loginTimes = null;
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    loginTimes = objectMapper.readValue(parts[2], new TypeReference<List<LoginTime>>() {
                    });
                    UserManagement.getInstance().onLoginHistoryResponse(true, parts[0], loginTimes);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
