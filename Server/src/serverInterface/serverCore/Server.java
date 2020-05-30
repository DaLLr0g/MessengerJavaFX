package serverInterface.serverCore;

import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread{

    private final int port;
    private final TextArea messages;
    private final VBox userList;
    private final List<HandleClient> clients = new ArrayList<>();

    public TextArea getMessages() {
        return messages;
    }
    public VBox getUserList() {
        return userList;
    }
    public List<HandleClient> getClients() {
        return clients;
    }

    public Server(int port, VBox userList, TextArea messages) {
        this.port = port;
        this.userList = userList;
        this.messages = messages;
    }

    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(port);
            System.out.println("Server start");
            //handler for all connection
            while (true){
                Socket clientSocket = server.accept();
                messages.appendText("Connected: " + clientSocket.getInetAddress() + " " + clientSocket.getPort() + "\n");
                HandleClient client = new HandleClient(clientSocket, this);
                clients.add(client);
                client.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
