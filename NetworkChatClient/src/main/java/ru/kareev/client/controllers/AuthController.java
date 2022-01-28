package ru.kareev.client.controllers;

import javafx.application.Platform;
import ru.kareev.client.ClientChat;
import ru.kareev.client.Network;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.function.Consumer;

public class AuthController {
    public static final String AUTH_COMMAND = "/auth";
    public static final String AUTH_OK_COMMAND = "/authOk";

    @FXML
 private TextField loginField;
 @FXML
 private PasswordField passwordField;
 @FXML
 private Button authButton;

 private ClientChat clientChat;

 @FXML
 public void executeAuth(ActionEvent actionEvent) {
     String login = loginField.getText();
     String password = passwordField.getText();

     if(login == null || password == null  || password.isBlank() || login.isBlank()){
         clientChat.showErrorDialog("Логин и пароль должны быть указанны");
         return;
     }

     String authCommandMessage = String.format("%s %s %s", AUTH_COMMAND, login, password);

     try {
         Network.getInstance().sendMessage(authCommandMessage);
     } catch (IOException e) {
         clientChat.showErrorDialog("Ошибка передачи данных по сети");
         e.printStackTrace();
     }
 }

    public void setClientChat(ClientChat clientChat) {
        this.clientChat = clientChat;
    }

    public void initializeMessageHandler() {
        Network.getInstance().waitMessage(new Consumer<String>() {
            @Override
            public void accept(String message) {
                if (message.startsWith(AUTH_OK_COMMAND)) {
                    String[] parts = message.split(" ");
                    String userName = parts[1];
                    Thread.currentThread().interrupt();
                    Platform.runLater(() -> {
                        clientChat.getChatStage().setTitle(userName);
                        clientChat.getAuthStage().close();
                    });
                } else {
                    Platform.runLater(() -> {
                        clientChat.showErrorDialog("Неправильная связка логин/пароль! ");
                    });
                }
            }
        });
    }
}