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

                // Handle client in a new thread
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port: 12345.");
            System.exit(1);
        } finally {
            serverSocket.close();
        }
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received: " + inputLine);

                if ("START".equals(inputLine)) {
                    out.println("OK Connection established");
                } else if ("CLOSE".equals(inputLine)) {
                    out.println("OK Closing connection");
                    break;
                } else {
                    out.println("NOTOK Unknown command");
                }
            }

            out.close();
            in.close();
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }
}
