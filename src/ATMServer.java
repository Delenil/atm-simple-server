import java.net.*;
import java.io.*;

public class ATMServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(12345);
            System.out.println("ATM Server started...");

             while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");
                // Code to handle the client will be added
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port: 12345.");
            System.exit(1);
        } finally {
            serverSocket.close();
        }
    }
}
