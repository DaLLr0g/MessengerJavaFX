package network;

import java.io.IOException;

public interface TCPConnectionListener {

    void onConnectionReady(TCPConnection tcpConnection);

    void onReceiveString(TCPConnection tcpConnection, String readLine);

    void onException(TCPConnection tcpConnection, IOException e);

    void onDisconnect(TCPConnection tcpConnection);
}
