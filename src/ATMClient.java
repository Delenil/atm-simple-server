// ATMClient.java
import java.net.*;
import java.io.*;
import java.util.*;

public class ATMClient {
    public static final String GREEN = "\u001B[32m";
    public static final String RED = "\u001B[31m";
    public static final String RESET = "\u001B[0m";

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to ATM Server");
            System.out.println(in.readLine()); // Welcome message

            // START command
            out.println("START");
            System.out.println("Server: " + in.readLine());

            // Authentication loop
            boolean authenticated = false;
            int attempts = 0;
            while (!authenticated && attempts < 3) {
                System.out.print("Enter your 4-digit PIN (or CLOSE to quit): ");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("CLOSE")) {
                    out.println("CLOSE");
                    System.out.println("Server: " + in.readLine());
                    return;
                }

                out.println("AUTH " + input);
                String response = in.readLine();
                System.out.println((response.startsWith("OK") ? GREEN : RED) + "Server: " + response + RESET);

                if (response.startsWith("OK Welcome")) {
                    authenticated = true;
                } else {
                    attempts++;
                    if (attempts >= 3) {
                        System.out.println(RED + "Too many failed attempts. Closing connection." + RESET);
                        out.println("CLOSE");
                        System.out.println("Server: " + in.readLine());
                        return;
                    }
                }
            }

            // Main command loop
            while (true) {
                System.out.println("\nAvailable commands:");
                System.out.println("1. BALANCE");
                System.out.println("2. DEBIT");
                System.out.println("3. CREDIT");
                System.out.println("4. CLOSE");
                System.out.println("5. LOGOUT");
                System.out.print("Enter command number or name: ");
                String input = scanner.nextLine().trim().toUpperCase();

                String command;
                switch (input) {
                    case "1", "BALANCE" -> command = "BALANCE";
                    case "2", "DEBIT" -> {
                        System.out.print("Enter amount to debit: ");
                        command = "DEBIT " + scanner.nextLine().trim();
                    }
                    case "3", "CREDIT" -> {
                        System.out.print("Enter amount to credit: ");
                        command = "CREDIT " + scanner.nextLine().trim();
                    }
                    case "4", "CLOSE" -> {
                        command = "CLOSE";
                        out.println(command);
                        System.out.println("Server: " + in.readLine());
                        return;
                    }
                    case "5", "LOGOUT" -> command = "LOGOUT";
                    default -> {
                        System.out.println("Invalid option. Try again.");
                        continue;
                    }
                }

                out.println(command);
                String response = in.readLine();
                if (response == null) break;
                System.out.println((response.startsWith("OK") ? GREEN : RED) + "Server: " + response + RESET);

                if (command.equals("LOGOUT")) {
                    authenticated = false;
                    // Re-authentication
                    attempts = 0;
                    while (!authenticated) {
                        System.out.print("Enter your 4-digit PIN (or 'CLOSE' to quit): ");
                        String pin = scanner.nextLine().trim();
                        if (pin.equalsIgnoreCase("CLOSE")) {
                            out.println("CLOSE");
                            System.out.println("Server: " + in.readLine());
                            return;
                        }

                        out.println("AUTH " + pin);
                        response = in.readLine();
                        System.out.println((response.startsWith("OK") ? GREEN : RED) + "Server: "
                                + response + RESET);

                        if (response.startsWith("OK Welcome")) {
                            authenticated = true;
                        } else {
                            attempts++;
                            if (attempts >= 3) {
                                System.out.println(RED + "Too many failed attempts. Closing connection." + RESET);
                                out.println("CLOSE");
                                System.out.println("Server: " + in.readLine());
                                return;
                            }
                        }
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }
}