
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class Client extends JFrame {
    
    //UI
    private JTextField num1Field;
    private JTextField num2Field;
    private JTextField resultField;
    
    //Network Config
    private static final String SERVER_ADDRESS = "10.132.72.153";
    private static final int SERVER_PORT = 1900;

    public Client() {
        setTitle("Network Calculator");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 2, 10, 10));

        //Primul numar
        add(new JLabel("Numar 1:"));
        num1Field = new JTextField();
        add(num1Field);

        //Al doilea numar
        add(new JLabel("Numar 2:"));
        num2Field = new JTextField();
        add(num2Field);

        //Panoul pentru cele 4 butoane de operatii
        add(new JLabel("Operatie:"));
        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 5, 0));
        JButton btnAdd = new JButton("+");
        JButton btnSub = new JButton("-");
        JButton btnMul = new JButton("*");
        JButton btnDiv = new JButton("/");
        
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnSub);
        buttonPanel.add(btnMul);
        buttonPanel.add(btnDiv);
        add(buttonPanel);

        //Rezultatul
        add(new JLabel("Rezultat:"));
        resultField = new JTextField();
        resultField.setEditable(false);
        add(resultField);

        ActionListener operationListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String operation = e.getActionCommand();
                sendCalculationRequest(operation);
            }
        };

        btnAdd.addActionListener(operationListener);
        btnSub.addActionListener(operationListener);
        btnMul.addActionListener(operationListener);
        btnDiv.addActionListener(operationListener);
    }

    private void sendCalculationRequest(String operation) {
        String num1Str = num1Field.getText().trim();
        String num2Str = num2Field.getText().trim();

        if (num1Str.isEmpty() || num2Str.isEmpty()) {
            resultField.setText("Eroare: Completati ambele numere!");
            return;
        }

        // Folosim try-with-resources pentru a garanta inchiderea socket-ului si a fluxurilor de date
        try (
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            // Se serializeaza cererea intr-un format prestabilit pe care serverul il va parsa
            String payload = num1Str + "," + num2Str + "," + operation;
            out.println(payload);

            // Firul de executie asteapta sincron (se blocheaza) pana cand serverul returneaza un raspuns
            String response = in.readLine();
            
            if (response != null) {
                resultField.setText(response);
            } else {
                resultField.setText("Eroare: Conexiune inchisa de server.");
            }

        } catch (IOException ex) {
            resultField.setText("Eroare conexiune: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Client client = new Client();
            client.setVisible(true);
            System.out.print("Frontend pornit");
            client.setLocationRelativeTo(null); // Centreaza fereastra pe ecran
        });
    }
}