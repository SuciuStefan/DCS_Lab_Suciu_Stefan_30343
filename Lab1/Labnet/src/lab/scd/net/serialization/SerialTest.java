package lab.scd.net.serialization;

import java.io.*;
import java.net.*;

public class SerialTest extends Thread {

	public static void main(String[] args) throws Exception {
		System.out.println("Serverul asteapta conexiuni...");
		try {
			ServerSocket ss = new ServerSocket(1977);
			Socket s = ss.accept();
			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
			Pers p = (Pers) ois.readObject();
			System.out.println("Serverul a receptionat obiectul:" + p);
			s.close();
			ss.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
