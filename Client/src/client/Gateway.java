package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Gateway{
    private Socket client;
    private Scanner serverIn;
    private PrintWriter serverOut;
    private String login;

    public Scanner getServerIn() {
        return serverIn;
    }

    public Gateway(String ip, String port) {
        try {
            client = new Socket(ip, Integer.parseInt(port));
            serverIn = new Scanner(client.getInputStream());
            serverOut = new PrintWriter(client.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Can't connect to server: " + e.toString() + "\n");
        }
    }

    public void closeConnection() throws IOException {
        client.close();
        serverIn.close();
        serverOut.close();
    }

    public void login(String login){
        if(login != null && !login.equals("")) {
            this.login = login;
            serverOut.println(login);
            System.out.println(serverIn.nextLine());
        } else{
            try {
                closeConnection();
            } catch (IOException e){
                System.out.println("Error: " + e);
            }
            System.exit(0);
        }

    }

    public synchronized void send(String toUser, String msg) {
        serverOut.println("send");
        serverOut.println(toUser + " " + msg);
    }
    public synchronized void sendToAll(String msg){
        serverOut.println("general");
        serverOut.println(msg);
    }

    public String getLogin(){
        return login;
    }
}
