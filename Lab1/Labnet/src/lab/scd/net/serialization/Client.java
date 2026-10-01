package lab.scd.net.serialization;

import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Client {

	  public static void main(String[] args) throws Exception{
	       
	        //conectare la server
	        Socket s = new Socket(InetAddress.getByName("localhost"),1977);
	        
	        //construieste fluxul de iesire
	        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
	        Pers p = new Pers("Alin",14);
	        //trimite un obiect prin fluxul de iesire
	        System.out.println("Clientul trimite obiectul: "+p);
	        oos.writeObject(p);
	        //inchide conexiunea/
	        s.close();	

	  }
	}

