import java.net.*;
import java.io.*;

public class ATMClient {
    public static void main(String[] args) throws IOException {
        try (
                Socket socket = new Socket("localhost", 12345);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                BufferedReader stdIn = new BufferedReader(
                        new InputStreamReader(System.in))
        ) {
            System.out.println("Server: " + in.readLine()); // Read welcome message

            out.println("START");
            System.out.println("Server: " + in.readLine());

            //Authentication
            System.out.print("Enter your PIN: ");
            String pin = stdIn.readLine();
            out.println("AUTH " + pin);
            String authResponse = in.readLine();
            System.out.println("Server: " + authResponse);

            if (!authResponse.startsWith("OK")) {
                System.out.println("Authentication failed. Disconnecting.");
                return;
            }

            // Main menu would go here (Phase 3)
            System.out.println("\nAuthentication successful!");

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }
}