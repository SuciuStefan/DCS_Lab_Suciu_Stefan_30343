import java.io.*;
import java.net.*;

public class Server {
    

    private static final int PORT = 1900;

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serverul de calcul matematic a pornit pe portul: " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket);
                handler.start();
            }
        } catch (IOException e) {
            System.err.println("Eroare critica la pornirea serverului: " + e.getMessage());
        }
    }
}


class ClientHandler extends Thread {
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true)
        ) {
            String payload = in.readLine();
            String clientAddress = socket.getRemoteSocketAddress().toString();
            if (payload != null) {
                // Procesam string-ul si calculam rezultatul
            	System.out.println("[REQUEST] Clientul " + clientAddress + " a cerut calculul: " + payload);
                String result = processCalculation(payload);
                out.println(result);
                System.out.println("[RESPONSE] Trimis catre " + clientAddress + " rezultatul: " + result);
            }

        } catch (IOException e) {
            System.err.println("Eroare la comunicarea cu clientul: " + e.getMessage());
        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Eroare la inchiderea socket-ului: " + e.getMessage());
            }
        }
    }

    private String processCalculation(String payload) {
        String[] parts = payload.split(",");
        if (parts.length != 3) {
            return "Eroare: Format cerere invalid.";
        }

        try {
            double num1 = Double.parseDouble(parts[0]);
            double num2 = Double.parseDouble(parts[1]);
            String operation = parts[2];
            double result = 0;

            switch (operation) {
                case "+":
                    result = num1 + num2;
                    break;
                case "-":
                    result = num1 - num2;
                    break;
                case "*":
                    result = num1 * num2;
                    break;
                case "/":
                    if (num2 == 0) {
                        return "Eroare: Impartire la zero!";
                    }
                    result = num1 / num2;
                    break;
                default:
                    return "Eroare: Operator matematic necunoscut.";
            }
            
            return String.valueOf(result);

        } catch (NumberFormatException e) {
            return "Eroare: Datele introduse nu sunt numere valide.";
        }
    }
}