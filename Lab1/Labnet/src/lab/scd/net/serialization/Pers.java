package lab.scd.net.serialization;

import java.io.Serializable;

public class Pers implements Serializable {
	String nume;
	//transient String nume;
	int varsta;

	Pers(String n, int v) {
		nume = n;
		varsta = v;
	}

	public String toString() {
		return "Persoana: " + nume + " vasrta: " + varsta;
	}
}
