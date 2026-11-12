package labul3;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class ComparatorInput_Ex1 {
    public static void main(String[] args) throws InterruptedException, IOException {
        File file = new File("C:\\Users\\stefa\\Documents\\comparator.txt");
        Files.deleteIfExists(file.toPath());
        FileWriter fw = new FileWriter(file.getPath());
        
        for (int i = 0; i < 1360; i++) {
            // Conversie index în radiani pentru o perioadă vizibilă pe grafic
            float f1 = (float) Math.sin(Math.toRadians(i)); 
            float f2 = (float) Math.cos(Math.toRadians(i)); 
            
            // Formatul strict impus de framework: P0:valoareF, P1:valoareF
            fw.write("P0:" + f1 + "F," + "P1:" + f2 + "F\n");
        }
        
        fw.close();
        System.out.println("Fișier generat cu semnale sin/cos.");
    }
}