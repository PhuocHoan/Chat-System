package com.haichutieu.chatsystem.client.gui.adminPanel;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.io.FileNotFoundException;
import java.net.URL;

public class TabLoader {
    private Pane view;

    public Pane getPane(String fileName) {
        try {
            URL fileUrl = AdminPanel.class.getResource(fileName + ".fxml");
            if (fileUrl == null) {
                throw new FileNotFoundException(fileName + ".fxml");
            }

            view = FXMLLoader.load(fileUrl);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return view;
    }
}
