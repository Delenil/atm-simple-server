import java.net.*;
import java.io.*;
import java.util.*;

public class ATMServer {
    // Static customer database
    static final Map<String, Customer> customers = new HashMap<>();

    static {
        // Initialize with 5 customers as per requirements
        customers.put("1234", new Customer("1234", "1111222233334444", 5000.0, "Alice"));
        customers.put("5678", new Customer("5678", "5555666677778888", 3000.0, "Bob"));
        customers.put("9012", new Customer("9012", "9999000011112222", 10000.0, "Charlie"));
        customers.put("3456", new Customer("3456", "3333444455556666", 7500.0, "Diana"));
        customers.put("7890", new Customer("7890", "7777888899990000", 2000.0, "Eve"));
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("ATM Server started on port 12345...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("\nNew client connected: " + clientSocket.getInetAddress());

                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
}

class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private Customer currentCustomer;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(clientSocket.getInputStream()))) {

            out.println("OK ATM Server Ready. Send START to begin.");

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received from " + clientSocket.getInetAddress() + ": " + inputLine);

                if ("START".equalsIgnoreCase(inputLine)) {
                    handleStart(out);
                }
                else if (inputLine.toUpperCase().startsWith("AUTH ")) {
                    handleAuth(inputLine, out);
                }
                else if ("BALANCE".equalsIgnoreCase(inputLine)) {
                    handleBalance(out);
                }
                else if (inputLine.toUpperCase().startsWith("DEBIT ")) {
                    handleDebit(inputLine, out);
                }
                else if (inputLine.toUpperCase().startsWith("CREDIT ")) {
                    handleCredit(inputLine, out);
                }
                else if ("CLOSE".equalsIgnoreCase(inputLine)) {
                    out.println("OK Connection closed");
                    break;
                }
                else {
                    out.println("NOTOK Invalid command");
                }
            }
        } catch (IOException e) {
            System.err.println("Client handler error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("Client disconnected: " + clientSocket.getInetAddress());
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    private void handleStart(PrintWriter out) {
        out.println("OK Please authenticate using AUTH <4-digit PIN>");
    }

    private void handleAuth(String command, PrintWriter out) {
        String pin = command.substring(5).trim();

        if (!pin.matches("\\d{4}")) {
            out.println("NOTOK PIN must be 4 digits");
            return;
        }

        Customer customer = ATMServer.customers.get(pin);
        if (customer != null) {
            currentCustomer = customer;
            out.println("OK Welcome, " + customer.getName());
        } else {
            out.println("NOTOK Invalid PIN");
        }
    }

    private void handleBalance(PrintWriter out) {
        if (currentCustomer == null) {
            out.println("NOTOK Please authenticate first");
            return;
        }
        out.printf("OK Account Holder: %s\nAccount Number: %s\nBalance: $%.2f%n",
                currentCustomer.getName(),
                currentCustomer.getAccountNumber(),
                currentCustomer.getBalance());
    }

    private void handleDebit(String command, PrintWriter out) {
        if (currentCustomer == null) {
            out.println("NOTOK Please authenticate first");
            return;
        }

        try {
            double amount = Double.parseDouble(command.substring(6).trim());
            if (amount <= 0) {
                out.println("NOTOK Amount must be positive");
            } else if (currentCustomer.getBalance() >= amount) {
                currentCustomer.debit(amount);
                out.printf("OK $%.2f debited. New balance: $%.2f%n",
                        amount, currentCustomer.getBalance());
            } else {
                out.printf("NOTOK Insufficient funds. Current balance: $%.2f%n",
                        currentCustomer.getBalance());
            }
        } catch (NumberFormatException e) {
            out.println("NOTOK Invalid amount format");
        }
    }

    private void handleCredit(String command, PrintWriter out) {
        if (currentCustomer == null) {
            out.println("NOTOK Please authenticate first");
            return;
        }

        try {
            double amount = Double.parseDouble(command.substring(7).trim());
            if (amount <= 0) {
                out.println("NOTOK Amount must be positive");
            } else {
                currentCustomer.credit(amount);
                out.printf("OK $%.2f credited. New balance: $%.2f%n",
                        amount, currentCustomer.getBalance());
            }
        } catch (NumberFormatException e) {
            out.println("NOTOK Invalid amount format");
        }
    }
}

class Customer {
    private final String pin;
    private final String accountNumber;
    private double balance;
    private final String name;

    public Customer(String pin, String accountNumber, double balance, String name) {
        this.pin = pin;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.name = name;
    }

    public String getPin() { return pin; }
    public String getAccountNumber() { return accountNumber; }
    public double getBalance() { return balance; }
    public String getName() { return name; }

    public void debit(double amount) { balance -= amount; }
    public void credit(double amount) { balance += amount; }
}
