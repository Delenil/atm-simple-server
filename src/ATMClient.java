import java.net.*;
import java.io.*;
import java.util.*;

public class ATMClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to ATM Server");
            System.out.println(in.readLine()); // Read server welcome message

            // Initial START command
            out.println("START");
            System.out.println("Server: " + in.readLine());

            // Authentication loop
            boolean authenticated = false;
            while (!authenticated) {
                System.out.print("Enter your 4-digit PIN (or 'exit' to quit): ");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("exit")) {
                    out.println("CLOSE");
                    return;
                }

                out.println("AUTH " + input);
                String response = in.readLine();
                System.out.println("Server: " + response);

                if (response.startsWith("OK Welcome")) {
                    authenticated = true;
                }
            }

            // Main menu (to be implemented in Phase 3)
            System.out.println("\nAuthentication successful!");
            System.out.println("Available commands: BALANCE, DEBIT, CREDIT, CLOSE");

            // Command loop
            while (authenticated) {
                System.out.print("\nEnter command: ");
                String command = scanner.nextLine().trim().toUpperCase();

                out.println(command);
                String response = in.readLine();
                System.out.println("Server: " + response);

                if (command.equals("CLOSE")) {
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }
}