package lab.scd.net.socket;

import java.net.*;
import java.io.*;
import java.net.*;
import java.io.*;

public class SimpleClient {

	public static void main(String[] args) throws Exception {
		Socket socket = null;
		try {
			// creating object address that identifies the server address
			InetAddress address = InetAddress.getByName("localhost");
			// the alternative could be used: InetAddress.getByName ("127.0.0.1")
			socket = new Socket(address, 1900);

			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			// Output is automatically flushed
			// by PrintWriter:
			PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),
					true);

			for (int i = 0; i < 10; i++) {
				out.println("message " + i);
				String str = in.readLine(); // send message
				System.out.println(str); // waits for response
			}
			out.println("END"); // sends a message that causes the server to close the connection

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			socket.close();
		}
	}
}