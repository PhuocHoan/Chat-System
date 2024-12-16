package com.haichutieu.chatsystem.client.gui.adminPanel;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

public class TabLoader {

    public Pane getPane(String fileName) {
        Pane view;
        try {
            URL fileUrl = AdminPanel.class.getResource(fileName + ".fxml");
            if (fileUrl == null) {
                throw new FileNotFoundException(fileName + ".fxml");
            }

            view = FXMLLoader.load(fileUrl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return view;
    }
}
