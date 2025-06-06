//Multi-threaded ATM Server Application
//Handles concurrent client connections and banking operations

import java.net.*;
import java.io.*;
import java.util.*;

public class ATMServer {

    //In-memory database using Map<Pin, Customer>
    static final Map<String, Customer> customers = new HashMap<>();

    //Static initialization of sample customers
    static {
        customers.put("1234", new Customer("1234", "1235222231334444",
                5000.0, "Alice"));
        customers.put("5678", new Customer("5678", "5555126667871888",
                3000.0, "Bob"));
        customers.put("9012", new Customer("9012", "9919040071112262",
                10000.0, "Charlie"));
        customers.put("3456", new Customer("3456", "3313444251553166",
                7500.0, "Diana"));
        customers.put("7890", new Customer("7890", "7512625161991250",
                2000.0, "Eve"));
    }

    public static void main(String[] args) {
        //Added ShutdownHook for clean termination
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
                System.out.println("\n[Server shutting down...]")));

        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("ATM Server started on port 12345...");
        //Main server loop accepting connections
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("\nNew client connected: " + clientSocket.getInetAddress());
                new Thread(new ClientHandler(clientSocket)).start();
                //New thread for every client
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
}


//CLientHandler class
//Handles individual connections
class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private Customer currentCustomer; //User currently authenticated

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    //Main client processing unit
    public void run() {
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

            out.println("OK ATM Server Ready. Send START to begin.");

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received from " + clientSocket.getInetAddress()
                        + ": " + inputLine);
                //Routing logic commands
                if ("START".equalsIgnoreCase(inputLine)) {
                    out.println("OK Please authenticate using AUTH <4-digit PIN>");
                }
                else if (inputLine.toUpperCase().startsWith("AUTH ")) {
                    handleAuth(inputLine, out);
                }
                else if ("BALANCE".equalsIgnoreCase(inputLine)) {
                    handleBalance(out);
                }
                else if (inputLine.toUpperCase().startsWith("DEBIT "))
                {
                    handleDebit(inputLine, out);
                }
                else if (inputLine.toUpperCase().startsWith("CREDIT "))
                {
                    handleCredit(inputLine, out);
                }
                else if ("LOGOUT".equalsIgnoreCase(inputLine)) {
                    handleLogout(out);
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
            //Cleanup and connection closure
            try {
                clientSocket.close();
                System.out.println("Client disconnected: " + clientSocket.getInetAddress());
            }
            catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    //Authentication handling method
    private void handleAuth(String command, PrintWriter out) {
        String pin = command.substring(5).trim();

        if (!pin.matches("\\d{4}")) { //regex to match 4 digits
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

    //Balance handling method
    private void handleBalance(PrintWriter out) {
        if (currentCustomer == null) {
            out.println("NOTOK Please authenticate first");
            return;
        }
        String response = String.format("OK Name: %s, Account: %s, Balance: $%.2f",
                currentCustomer.getName(),
                currentCustomer.getAccountNumber(),
                currentCustomer.getBalance());
        out.println(response);
    }

    //Debit handling method
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
                out.printf("OK $%.2f debited. New balance: $%.2f%n", amount, currentCustomer.getBalance());
                System.out.printf("[LOG] %s: Debited $%.2f. New balance: $%.2f%n",
                        currentCustomer.getName(), amount, currentCustomer.getBalance());
            } else {
                out.printf("NOTOK Insufficient funds. Current balance: $%.2f%n", currentCustomer.getBalance());
            }
        } catch (NumberFormatException e) {
            out.println("NOTOK Invalid amount format");
        }
    }

    //Credit handling method
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
                out.printf("OK $%.2f credited. New balance: $%.2f%n", amount, currentCustomer.getBalance());
                System.out.printf("[LOG] %s: Credited $%.2f. New balance: $%.2f%n",
                        currentCustomer.getName(), amount, currentCustomer.getBalance());
            }
        } catch (NumberFormatException e) {
            out.println("NOTOK Invalid amount format");
        }
    }

    //Logout handling method
    private void handleLogout(PrintWriter out) {
        if (currentCustomer != null) {
            out.println("OK Logged out. Please authenticate again.");
            System.out.println("[LOG] User " + currentCustomer.getName() + " logged out.");
            currentCustomer = null;
        } else {
            out.println("NOTOK No user is currently logged in.");
        }
    }
}

//Customer class/model - representing customer data
//Utilizing synchronized methods for thread safety in balance updates
class Customer {
    private final String pin;
    private final String accountNumber;
    private double balance; //mutable balance field
    private final String name;

    public Customer(String pin, String accountNumber, double balance, String name) {
        this.pin = pin;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.name = name;
    }
    public String getAccountNumber() { return accountNumber; }
    public double getBalance() { return balance; }
    public String getName() { return name; }

    //synchronized methods
    public synchronized void debit(double amount) { balance -= amount; }
    public synchronized void credit(double amount) { balance += amount; }
}