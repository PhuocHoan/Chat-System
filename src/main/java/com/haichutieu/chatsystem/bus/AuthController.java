package com.haichutieu.chatsystem.bus;

import com.haichutieu.chatsystem.dal.CustomerService;
import com.haichutieu.chatsystem.dto.Customer;
import javafx.scene.control.TextField;

import java.sql.Timestamp;
import java.util.List;

public class AuthController {
    public static String handleRegister(List<TextField> fields) {
        if (CustomerService.getCustomerByUsername(fields.get(1).getText()) != null) {
            return "Username already exists";
        }
        if (CustomerService.getCustomerByEmail(fields.get(2).getText()) != null) {
            return "Email already exists";
        }
        Customer customer = new Customer();
        customer.setName(fields.get(0).getText());
        customer.setUsername(fields.get(1).getText());
        customer.setEmail(fields.get(2).getText());
        customer.setPassword(fields.get(3).getText());
        customer.setCreate_date(new Timestamp(System.currentTimeMillis()));
        customer.setIs_lock(false);
        CustomerService.addCustomer(customer);
        return null;
    }
}
