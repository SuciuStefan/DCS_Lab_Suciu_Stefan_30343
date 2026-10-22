package lab.scd.net.socket;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;



public class SimpleServer {
  public static void main(String[] args) throws IOException{

    ServerSocket ss=null;
    Socket s=null;
    
    try{
      String line="";
      ss = new ServerSocket(1900); //create a serversocket object
      System.out.println("Server is waiting for connections...");
      s = ss.accept(); //start waiting for connections at port 1900
      //When a client connects, `ss.accept()` returns.
      //a Socket object that identifies the connection

      //Create the input and output streams.
      BufferedReader in = new BufferedReader(
            new InputStreamReader(s.getInputStream()));

      PrintWriter out = new PrintWriter(
            new BufferedWriter(new OutputStreamWriter(
              s.getOutputStream())),true);
      
      //extract the IP address and port from which the client connected
      InetSocketAddress remoteadr = (InetSocketAddress)s.getRemoteSocketAddress();
      String remotehost = remoteadr.getHostName();
      int remoteport = remoteadr.getPort();
      
      System.out.println("Client nou conectat: "+remotehost+":"+remoteport);
      
      while(!line.equals("END")){
        line = in.readLine(); //read data from the client
        System.out.println("Server a receptionat:"+line);
        out.println("ECHO "+line); //send data to the client
        out.flush();
      }
      
      System.out.println("Aplicatie server gata.");
      
    }catch(Exception e){e.printStackTrace();}
     finally{
      ss.close();
      if(s!=null) s.close();
     }
  }
}
