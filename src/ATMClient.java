import java.net.*;
import java.io.*;

public class ATMClient {
    public static void main(String[] args) throws IOException {
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        try {
            socket = new Socket("localhost", 12345); // Connect to server
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Connected to ATM Server");

            // Simple test - send START command
            out.println("START");
            String response = in.readLine();
            System.out.println("Server response: " + response);

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: localhost");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: localhost");
            System.exit(1);
        } finally {
            out.close();
            in.close();
            socket.close();
        }
    }
}