import java.net.*;
import java.io.*;
import java.util.Map;

class Customer {
    private String pin;
    private String accountNumber;
    private double balance;
    private String name;

    public Customer(String pin, String accountNumber, double balance, String name) {
        this.pin = pin;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.name = name;
    }

    // Getters
    public String getPin() { return pin; }
    public String getAccountNumber() { return accountNumber; }
    public double getBalance() { return balance; }
    public String getName() { return name; }
}
public class ATMServer {

    private static final Map<String, Customer> customers = Map.of(
            "1234", new Customer("1234", "1111222233334444", 5000.0, "Alice"),
            "5678", new Customer("5678", "5555666677778888", 3000.0, "Bob"),
            "9012", new Customer("9012", "9999000011112222", 10000.0, "Charlie")
    );
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
    private Customer currentCustomer;

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
