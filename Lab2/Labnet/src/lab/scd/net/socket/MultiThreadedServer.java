package lab.scd.net.socket;

import java.io.*;
import java.net.*;
import java.io.*;
import java.net.*;

public class MultiThreadedServer {
	public static final int PORT = 1900;

	void startServer() {
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(PORT);
			System.out.println("Server is waiting for connections...");
			while (true) {
				Socket socket = ss.accept();
				new TreatClient(socket).start();
			}

		} catch (IOException ex) {
			System.err.println("Error :" + ex.getMessage());
		} finally {
			try {
				ss.close();
			} catch (IOException ex2) {
			}
		}
	}

	public static void main(String args[]) {
		MultiThreadedServer smf = new MultiThreadedServer();
		smf.startServer();
	}
}

class TreatClient extends Thread {
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;

	TreatClient(Socket socket) throws IOException {
		this.socket = socket;
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
	}

	public void run() {
		String line="";
		try {
			 while(!line.equals("END")){
			        line = in.readLine(); //read data from the client
			        System.out.println("Server a receptionat:"+line);
			        out.println("ECHO "+line); //send data to the client
			        out.flush();
			      }
			// .while
			System.out.println("closing...");
		} catch (IOException e) {
			System.err.println("IO Exception");
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				System.err.println("Socket not closed");
			}
		}
	}// .run
}