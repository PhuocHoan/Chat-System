package com.haichutieu.chatsystem.client.gui.adminPanel;

import com.haichutieu.chatsystem.client.bus.AdminController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Statistics {
    private static Statistics instance;
    Map<Integer, List<Long>> appUsageCount = new HashMap<>();
    Map<Integer, List<Long>> newUsersCount = new HashMap<>();

    @FXML
    private ComboBox<Integer> appUsageYearFilter;

    @FXML
    private ComboBox<Integer> newUserYearFilter;

    @FXML
    private CategoryAxis xMonthAppUsage; // x-axis for app usage

    @FXML
    private CategoryAxis xMonthNewUser; // x-axis for new user

    @FXML
    private BarChart<String, Long> yearlyAppUsage;

    @FXML
    private BarChart<String, Long> yearlyNewUser;

    public Statistics() {
        instance = this;
    }

    public static Statistics getInstance() {
        return instance;
    }

    String monthNumberToFullName(int monthNumber) {
        return Month.of(monthNumber).getDisplayName(
                TextStyle.FULL, Locale.getDefault()
        );
    }

    @FXML
    public void initialize() {
        AdminController.fetchNewUsersMonthly();
        AdminController.fetchAppUsageMonthly();
        // disable animation for preventing labels bad positioning
        xMonthNewUser.setAnimated(false);
        xMonthAppUsage.setAnimated(false);

        newUserYearFilter.setOnAction(event -> {
            int year = newUserYearFilter.getValue();
            List<Long> data = newUsersCount.get(year);

            XYChart.Series<String, Long> series = new XYChart.Series<>();
            Platform.runLater(() -> {
                yearlyNewUser.getData().clear();
                for (int i = 0; i < data.size(); i++) {
                    XYChart.Data<String, Long> dataPoint = new XYChart.Data<>(monthNumberToFullName(i + 1), data.get(i));
                    // Add the data point to the series
                    series.getData().add(dataPoint);
                    // Attach a listener to add the value label once the node is available
                    dataPoint.nodeProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue != null) {
                            displayBarValue(dataPoint);
                        }
                    });
                }
                yearlyNewUser.getData().add(series);
            });
        });

        appUsageYearFilter.setOnAction(event -> {
            int year = appUsageYearFilter.getValue();
            List<Long> data = appUsageCount.get(year);

            XYChart.Series<String, Long> series = new XYChart.Series<>();
            Platform.runLater(() -> {
                yearlyAppUsage.getData().clear();
                for (int i = 0; i < data.size(); i++) {
                    XYChart.Data<String, Long> dataPoint = new XYChart.Data<>(monthNumberToFullName(i + 1), data.get(i));
                    // Add the data point to the series
                    series.getData().add(dataPoint);
                    // Attach a listener to add the value label once the node is available
                    dataPoint.nodeProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue != null) {
                            displayBarValue(dataPoint);
                        }
                    });
                }
                yearlyAppUsage.getData().add(series);
            });
        });
    }

    public void onFetchNewUserMonthly(Map<Integer, List<Long>> newUsers) {
        newUsersCount = newUsers;
        newUserYearFilter.getItems().setAll(newUsersCount.keySet());
        // get the latest year as a default value for the filter
        newUserYearFilter.setValue(newUsersCount.keySet().stream().max(Integer::compareTo).orElseThrow());
        Platform.runLater(() -> newUserYearFilter.getOnAction().handle(null));
    }

    public void onFetchAppUsageMonthly(Map<Integer, List<Long>> appUsage) {
        appUsageCount = appUsage;
        appUsageYearFilter.getItems().setAll(appUsageCount.keySet());
        // get the latest year as a default value for the filter
        appUsageYearFilter.setValue(appUsageCount.keySet().stream().max(Integer::compareTo).orElseThrow());
        Platform.runLater(() -> appUsageYearFilter.getOnAction().handle(null));
    }

    // display the value of the bar
    private void displayBarValue(XYChart.Data<String, Long> data) {
        // Create a label to display the value
        Label valueLabel = new Label(String.valueOf(data.getYValue()));
        valueLabel.getStyleClass().add("bar-value"); // style the label

        // Place the label on middle of the bar
        StackPane node = (StackPane) data.getNode();
        node.getChildren().add(valueLabel);
    }
}
