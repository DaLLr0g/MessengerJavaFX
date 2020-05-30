package serverInterface.serverCore;

import javafx.application.Platform;
import javafx.scene.control.Label;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class HandleClient extends Thread{

    private final Socket client;
    private final Server server;

    private String userLogin;
    private Scanner in;
    private PrintWriter out;
    private Label user;

    public HandleClient(Socket client, Server server) {
        this.client = client;
        this.server = server;
    }

    public String getUserLogin() {
        return userLogin;
    }

    @Override
    public void run() {
        try {
            //get input/output
            in = new Scanner(client.getInputStream());
            out = new PrintWriter(client.getOutputStream(), true);

            //get user login and add in user list VBox
            login();

            //check for connection
            while (in.hasNextLine()){
                String line = in.nextLine();
                if (line == null || line.equalsIgnoreCase("exit"))
                    break;
                if (line.equalsIgnoreCase("send"))
                    sendPrivateMsg();
            }
            //close connection
            closeConnection();

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    //function for send private msg
    private void sendPrivateMsg() throws IOException {
        String[] cmd = in.nextLine().split(" ",2);
        for(HandleClient client : server.getClients())
            if(client.getUserLogin().equals(cmd[0])){
                String msg = userLogin + " " + cmd[1]; //cmd[0] user, cmd[1] body msg
                server.getMessages().appendText(userLogin + "->" + cmd[0] + ": " + cmd[1] + "\n");
                client.out.println("get");
                client.out.println(msg);
            }
    }

    //send online status other users and get users status
    private void sendStatus() throws IOException{
        for(HandleClient client : server.getClients()){
            if(!client.equals(this) && client.getUserLogin() != null){
                client.out.println("online");
                client.out.println(userLogin);
            }
            if(!client.equals(this) && client.getUserLogin() != null){
                out.println("online");
                out.println(client.getUserLogin());
            }
        }
    }

    //send message to all user in chat
    private void sendToAll(String msg) throws IOException {
        for(HandleClient client : server.getClients()){
            if(!client.equals(this) && client.getUserLogin() != null){
                client.out.println(msg);
            }
        }
    }

    //login user
    private void login() throws IOException {
        if(in.hasNextLine())
            userLogin = in.nextLine();

        user = new Label(userLogin);
        if (user.getText() != null) {
            Platform.runLater(() -> server.getUserList().getChildren().add(user));
            server.getMessages().appendText(userLogin + " --- logged\n");
            out.println("logged");
            sendStatus();
        }
    }

    //close user connection, and delete him from TextArea
    private void closeConnection() throws IOException {
        server.getMessages().appendText("Disconnected: " + client.getInetAddress() + " " + client.getPort() + "\n");
        synchronized (this) {
            server.getClients().remove(this);
        }
        if(userLogin != null) {
            server.getMessages().appendText(userLogin + " --- left.\n");
            sendToAll("offline");
            sendToAll(userLogin);
            Platform.runLater(() -> server.getUserList().getChildren().remove(user));
        }
        in.close(); out.close(); client.close();
    }

}
