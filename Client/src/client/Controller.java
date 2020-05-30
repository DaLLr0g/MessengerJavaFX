package client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Controller implements Initializable {
    @FXML
    private ImageView sendButton;
    @FXML
    private Label receiver;
    @FXML
    private Label userName;

    private final List<String> userMsg = new ArrayList<>();
    private final ObservableList<String> userList = FXCollections.observableArrayList();
    @FXML
    private ListView<String> userListView = new ListView<>(userList);
    @FXML
    private VBox messages;
    @FXML
    private ScrollPane logField;
    @FXML
    private TextArea messageField;

    private Gateway gateway;

    @FXML
    public void initialize(URL url, ResourceBundle rb){
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("IP configuration");
        dialog.setResizable(true);

        Label label1 = new Label("IP: ");
        Label label2 = new Label("Port: ");
        TextField text1 = new TextField();
        TextField text2 = new TextField();

        GridPane grid = new GridPane();
        grid.add(label1, 1, 1);
        grid.add(text1, 2, 1);
        grid.add(label2, 1, 2);
        grid.add(text2, 2, 2);
        dialog.getDialogPane().setContent(grid);

        ButtonType buttonTypeOk = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);

        dialog.setResultConverter(buttonType -> {
            if(buttonType == buttonTypeOk)
                return new Pair<>(text1.getText(), text2.getText());
            return null;
        });

        Optional<Pair<String, String >> result = dialog.showAndWait();

        AtomicReference<String> ip = new AtomicReference<>("");
        AtomicReference<String> port = new AtomicReference<>("");
        result.ifPresent(stringStringPair -> {
            ip.set(stringStringPair.getKey());
            port.set(stringStringPair.getValue());
        });
        //create new gateway with server
        gateway = new Gateway(ip.get(), port.get());
        setVisibleToMsg(false);
        TextInputDialog dialogLogin = new TextInputDialog();
        dialogLogin.setTitle("Log In");
        dialogLogin.setHeaderText(null);
        dialogLogin.setContentText("Enter a Login:");
        Optional<String> resultLogin = dialogLogin.showAndWait();
        resultLogin.ifPresent(name -> gateway.login(name));
        userName.setText(gateway.getLogin());
        userListView.setItems(userList);
        new Thread(new Checker(gateway.getServerIn(), userList)).start();
    }

    private void setVisibleToMsg(boolean x){
        messageField.setVisible(x);
        sendButton.setVisible(x);
    }

    @FXML
    void closeApp() {
        System.exit(0);
        try {
            gateway.closeConnection();
        } catch (IOException e){
            System.err.println("Error: " + e);
        }
    }


    void view(String msg, boolean user){
        SpeechBox speechBox = new SpeechBox(msg, user);
        messages.getChildren().add(speechBox);
        messageField.setText("");
        logField.applyCss();
        logField.layout();
        logField.setVvalue(1);
    }

    //send message from messageField to server
    @FXML
    void sendMessage() {
        String msg = messageField.getText();
        if(!msg.equals("")) {
            gateway.send(receiver.getText(), msg);
            userMsg.add("send " + receiver.getText() + " " + msg);
            view(msg, false);
        }
    }

    @FXML
    void sendMessageFromTextArea(KeyEvent event) {
        if(event.isControlDown() && event.getCode() == KeyCode.ENTER) {
            messageField.appendText("\n");
        }
        else if (event.getCode() == KeyCode.ENTER)
            sendMessage();
    }


    @FXML
    void selectUser(){
        String selectedUser = userListView.getSelectionModel().getSelectedItem();
        if(!selectedUser.equals(receiver.getText())) {
            receiver.setText(selectedUser);
            messages.getChildren().clear();
            setVisibleToMsg(true);
            for(String msg : userMsg){
                String[] cmd = msg.split(" ", 3);
                if(cmd[0].equals("get") && cmd[1].equals(selectedUser)){
                    view(cmd[2], true);
                } else if (cmd[0].equals("send") && cmd[1].equals(selectedUser)) {
                    view(cmd[2], false);
                }
            }
        }
    }


    public class Checker implements Runnable {
        private final Scanner serverIn;
        private final ObservableList<String> userList;


        public Checker(Scanner serverIn, ObservableList<String> userList)
        {
            this.serverIn = serverIn;
            this.userList = userList;
        }

        @Override
        public void run() {
            while (serverIn.hasNextLine())
            {
                String line = serverIn.nextLine();
                switch(line) {
                    case "get" : {
                        String[] msg = serverIn.nextLine().split(" ", 2);
                        if(msg[0].equals(receiver.getText()))
                            Platform.runLater(() -> view(msg[1], true));
                        userMsg.add("get " + msg[0] + " " + msg[1]);
                        break;
                    }
                    case "online" : {
                        String user = serverIn.nextLine();
                        Platform.runLater(()->userList.add(user));
                        break;
                    }
                    case "offline" : {
                        String user = serverIn.nextLine();
                        Platform.runLater(()-> userList.remove(user));
                        if(user.equals(receiver.getText())) {
                            Platform.runLater(() -> {
                                receiver.setText("");
                                messages.getChildren().clear();
                                setVisibleToMsg(false);
                            });
                        }
                        break;
                    }
                }

            }
        }
    }
}