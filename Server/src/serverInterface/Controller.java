package serverInterface;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import serverInterface.serverCore.Server;

public class Controller {
    @FXML
    public VBox userList;
    @FXML
    public TextArea messages;

    @FXML
    void initialize() {
        Server server = new Server(9999, userList, messages);
        server.start();
    }
}