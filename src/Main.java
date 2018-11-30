import sun.security.provider.MD5;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        connectToServer();
    }

    public static void connectToServer() {
        //Try connect to the server on an unused port eg 9991. A successful connection will return a socket
        try (ServerSocket serverSocket = new ServerSocket(1337)) {
            Socket connectionSocket = serverSocket.accept();
            
            //Create Input&Outputstreams for the connection
            InputStream inputToServer = connectionSocket.getInputStream();
            OutputStream outputFromServer = connectionSocket.getOutputStream();

            PrintWriter serverPrintOut = new PrintWriter(new OutputStreamWriter(outputFromServer, "UTF-8"), true);

            serverPrintOut.println("HELO Hello World! Enter Peace to exit.");

            //Have the server take input from the client and echo it back
            //This should be placed in a loop that listens for a terminator text e.g. bye
            serverPrintOut.println("HELO Echo from <Your Name Here> Server: " + inputToServer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
