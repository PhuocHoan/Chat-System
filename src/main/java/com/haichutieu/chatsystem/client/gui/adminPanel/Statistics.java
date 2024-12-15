package com.haichutieu.chatsystem.client.gui.adminPanel;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.ComboBox;

public class Statistics {

    @FXML
    private ComboBox<Integer> appUsageYearFilter;

    @FXML
    private ComboBox<Integer> newUserYearFilter;

    @FXML
    private CategoryAxis xMonthAppUsage;

    @FXML
    private CategoryAxis xMonthNewUser;

    @FXML
    private NumberAxis yQuantityAppUsage;

    @FXML
    private NumberAxis yQuantityNewUsers;

    @FXML
    private BarChart<String, Number> yearlyAppUsage;

    @FXML
    private BarChart<String, Number> yearlyNewUser;

    @FXML
    public void initialize() {
        appUsageYearFilter.getItems().addAll(2021, 2022, 2023, 2024, 2025);
        newUserYearFilter.getItems().addAll(2021, 2022, 2023, 2024, 2025);
    }
}
