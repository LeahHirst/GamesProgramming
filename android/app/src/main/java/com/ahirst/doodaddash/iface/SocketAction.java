package com.ahirst.doodaddash.iface;

import io.socket.client.Socket;

public interface SocketAction {

    void run(Socket socket);

}
