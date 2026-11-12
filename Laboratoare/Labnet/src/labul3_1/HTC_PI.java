package labul3_1;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import Components.Activation;
import Components.Condition;
import Components.GuardMapping;
import Components.PetriNet;
import Components.PetriNetWindow;
import Components.PetriTransition;
import DataObjects.DataFuzzy;
import DataObjects.DataTransfer;
import DataOnly.FLRS;
import DataOnly.FV;
import DataOnly.Fuzzy;
import DataOnly.PlaceNameWithWeight;
import DataOnly.TransferOperation;
import Enumerations.FZ;
import Enumerations.LogicConnector;
import Enumerations.TransitionCondition;
import Enumerations.TransitionOperation;

/*
 * HTC cu regulator PI (Exercitiul 2, Fuzzy Lab 3).
 * Pornit de la HTC.java (modelul P). Formula PI din lab:
 *   du(k) = K*e(k) + Ki*e(k-1)      (aici K = 0.8, Ki = 0.2)
 *   u(k)  = u(k-1) + du(k)
 *
 * Ce s-a adaugat fata de HTC.java:
 *   P9  = e(k-1), eroarea de la pasul anterior (porneste cu 0.0)
 *   P10 = du(k), variatia comenzii
 *   T3  trimite acum eroarea intarziata si in P9
 *   T6  (nou) calculeaza du(k) = 0.8*e(k) + 0.2*e(k-1)
 *   T4  aduna 1.2*du(k) cu u(k-1) (P6)
 *
 * Ordinea de rulare: WheatherInput, RoomPlant, HeaterTankPlant, RTC, apoi aceasta clasa.
 * Foloseste portul 1083, deci nu o rula in acelasi timp cu HTC.java.
 */
public class HTC_PI {
	public static void main(String[] args) throws FileNotFoundException {
		PetriNet pn = new PetriNet();
		pn.PetriNetName = "HTC PI";
		pn.NetworkPort = 1083;

		// Referinta pentru temperatura apei vine din acest fisier (creat de WheatherInput)
		pn.SetInputFile("D:\\PetriInputData\\HTC.txt");

		// ---------------- Tabelele FLRS ----------------
		FLRS reader = new FLRS(new FV(FZ.NL), new FV(FZ.NM), new FV(FZ.ZR), new FV(FZ.PM), new FV(FZ.PL),
				new FV(FZ.NL), new FV(FZ.NM), new FV(FZ.ZR), new FV(FZ.PM), new FV(FZ.PL),
				new FV(FZ.NL), new FV(FZ.NM), new FV(FZ.ZR), new FV(FZ.PM), new FV(FZ.PL),
				new FV(FZ.NL), new FV(FZ.NM), new FV(FZ.ZR), new FV(FZ.PM), new FV(FZ.PL),
				new FV(FZ.NL), new FV(FZ.NM), new FV(FZ.ZR), new FV(FZ.PM), new FV(FZ.PL));

		// Diferenta (referinta - masurare), cu doua iesiri identice
		FLRS doubleChannelDifferentiator = new FLRS(
				new FV(FZ.ZR, FZ.ZR), new FV(FZ.NM, FZ.NM), new FV(FZ.NL, FZ.NL), new FV(FZ.NL, FZ.NL), new FV(FZ.NL, FZ.NL),
				new FV(FZ.PM, FZ.PM), new FV(FZ.ZR, FZ.ZR), new FV(FZ.NM, FZ.NM), new FV(FZ.NL, FZ.NL), new FV(FZ.NL, FZ.NL),
				new FV(FZ.PL, FZ.PL), new FV(FZ.PM, FZ.PM), new FV(FZ.ZR, FZ.ZR), new FV(FZ.NM, FZ.NM), new FV(FZ.NL, FZ.NL),
				new FV(FZ.PL, FZ.PL), new FV(FZ.PL, FZ.PL), new FV(FZ.PM, FZ.PM), new FV(FZ.ZR, FZ.ZR), new FV(FZ.NM, FZ.NM),
				new FV(FZ.PL, FZ.PL), new FV(FZ.PL, FZ.PL), new FV(FZ.PL, FZ.PL), new FV(FZ.PM, FZ.PM), new FV(FZ.ZR, FZ.ZR));

		// Suma cu doua iesiri identice (folosita pentru u(k) = u(k-1) + du(k))
		FLRS doubleChannelAdder = new FLRS(
				new FV(FZ.NL, FZ.NL), new FV(FZ.NL, FZ.NL), new FV(FZ.NL, FZ.NL), new FV(FZ.NM, FZ.NM), new FV(FZ.ZR, FZ.ZR),
				new FV(FZ.NL, FZ.NL), new FV(FZ.NL, FZ.NL), new FV(FZ.NM, FZ.NM), new FV(FZ.ZR, FZ.ZR), new FV(FZ.PM, FZ.PM),
				new FV(FZ.NL, FZ.NL), new FV(FZ.NM, FZ.NM), new FV(FZ.ZR, FZ.ZR), new FV(FZ.PM, FZ.PM), new FV(FZ.PL, FZ.PL),
				new FV(FZ.NM, FZ.NM), new FV(FZ.ZR, FZ.ZR), new FV(FZ.PM, FZ.PM), new FV(FZ.PL, FZ.PL), new FV(FZ.PL, FZ.PL),
				new FV(FZ.ZR, FZ.ZR), new FV(FZ.PM, FZ.PM), new FV(FZ.PL, FZ.PL), new FV(FZ.PL, FZ.PL), new FV(FZ.PL, FZ.PL));

		// Suma cu o singura iesire (folosita pentru du(k), ca in PIController)
		FLRS adder = new FLRS(new FV(FZ.NL), new FV(FZ.NL), new FV(FZ.NL), new FV(FZ.NM), new FV(FZ.ZR),
				new FV(FZ.NL), new FV(FZ.NL), new FV(FZ.NM), new FV(FZ.ZR), new FV(FZ.PM),
				new FV(FZ.NL), new FV(FZ.NM), new FV(FZ.ZR), new FV(FZ.PM), new FV(FZ.PL),
				new FV(FZ.NM), new FV(FZ.ZR), new FV(FZ.PM), new FV(FZ.PL), new FV(FZ.PL),
				new FV(FZ.ZR), new FV(FZ.PM), new FV(FZ.PL), new FV(FZ.PL), new FV(FZ.PL));

		FLRS OneXOneDefaultTable = new FLRS(new FV(FZ.NL), new FV(FZ.NM), new FV(FZ.ZR), new FV(FZ.PM), new FV(FZ.PL));

		// Copiaza o valoare in doua locatii (pentru T3)
		FLRS OneXTwoDefaultTable = new FLRS(new FV(FZ.NL, FZ.NL), new FV(FZ.NM, FZ.NM), new FV(FZ.ZR, FZ.ZR),
				new FV(FZ.PM, FZ.PM), new FV(FZ.PL, FZ.PL));

		reader.Print();
		doubleChannelDifferentiator.Print();
		doubleChannelAdder.Print();
		adder.Print();
		OneXOneDefaultTable.Print();
		OneXTwoDefaultTable.Print();

		// ---------------- Locatiile ----------------
		DataFuzzy p0 = new DataFuzzy(); // declanseaza un nou ciclu de calcul
		p0.SetName("P0");
		p0.SetValue(new Fuzzy(0.0F));
		pn.PlaceList.add(p0);

		DataFuzzy p1 = new DataFuzzy(); // referinta temperaturii apei (din fisier)
		p1.SetName("P1");
		pn.PlaceList.add(p1);

		DataFuzzy p2 = new DataFuzzy();
		p2.SetName("P2");
		pn.PlaceList.add(p2);

		DataFuzzy p3 = new DataFuzzy(); // temperatura apei din HeaterTank
		p3.SetName("P3");
		p3.SetValue(new Fuzzy(0.3F));
		pn.PlaceList.add(p3);

		DataFuzzy p4 = new DataFuzzy(); // eroarea e(k)
		p4.SetName("P4");
		pn.PlaceList.add(p4);

		DataFuzzy p5 = new DataFuzzy(); // copia lui e(k), merge in T3 (intarziere)
		p5.SetName("P5");
		pn.PlaceList.add(p5);

		DataFuzzy p6 = new DataFuzzy(); // comanda anterioara u(k-1)
		p6.SetName("P6");
		p6.SetValue(new Fuzzy(0.0F));
		pn.PlaceList.add(p6);

		DataFuzzy p7 = new DataFuzzy(); // comanda u(k), trimisa spre gascmd
		p7.SetName("P7");
		pn.PlaceList.add(p7);

		DataFuzzy p8 = new DataFuzzy(); // comanda u(k), merge inapoi in P6
		p8.SetName("P8");
		pn.PlaceList.add(p8);

		DataFuzzy p9 = new DataFuzzy(); // NOU: eroarea anterioara e(k-1)
		p9.SetName("P9");
		p9.SetValue(new Fuzzy(0.0F)); // la primul pas nu exista eroare anterioara
		pn.PlaceList.add(p9);

		DataFuzzy p10 = new DataFuzzy(); // NOU: variatia comenzii du(k)
		p10.SetName("P10");
		pn.PlaceList.add(p10);

		DataTransfer gascmd = new DataTransfer();
		gascmd.SetName("gascmd");
		gascmd.Value = new TransferOperation("localhost", "1081", "u"); // spre HeaterTankPlant
		pn.PlaceList.add(gascmd);

		// ---------------- T0 ----------------
		// P0 si P1 -> P2
		PetriTransition t0 = new PetriTransition(pn);
		t0.TransitionName = "T0";
		t0.InputPlaceName.add("P0");
		t0.InputPlaceName.add("P1");

		Condition T0Ct1 = new Condition(t0, "P0", TransitionCondition.NotNull);
		Condition T0Ct2 = new Condition(t0, "P1", TransitionCondition.NotNull);
		T0Ct1.SetNextCondition(LogicConnector.AND, T0Ct2);

		GuardMapping grdT0 = new GuardMapping();
		grdT0.condition = T0Ct1;

		ArrayList<PlaceNameWithWeight> input0 = new ArrayList<>();
		input0.add(new PlaceNameWithWeight("P0", 1F));
		input0.add(new PlaceNameWithWeight("P1", 1F));

		ArrayList<String> Output0 = new ArrayList<>();
		Output0.add("P2");

		grdT0.Activations.add(new Activation(t0, reader, input0, TransitionOperation.FLRS, Output0));
		t0.GuardMappingList.add(grdT0);
		t0.Delay = 0;
		pn.Transitions.add(t0);

		// ---------------- T1 ----------------
		// e(k) = referinta - temperatura apei, pus in P4 si P5
		PetriTransition t1 = new PetriTransition(pn);
		t1.TransitionName = "T1";
		t1.InputPlaceName.add("P2");
		t1.InputPlaceName.add("P3");

		Condition T1Ct1 = new Condition(t1, "P2", TransitionCondition.NotNull);
		Condition T1Ct2 = new Condition(t1, "P3", TransitionCondition.NotNull);
		T1Ct1.SetNextCondition(LogicConnector.AND, T1Ct2);

		GuardMapping grdT1 = new GuardMapping();
		grdT1.condition = T1Ct1;

		ArrayList<PlaceNameWithWeight> input1 = new ArrayList<>();
		input1.add(new PlaceNameWithWeight("P2", 1F));
		input1.add(new PlaceNameWithWeight("P3", 1F));

		ArrayList<String> Output1 = new ArrayList<>();
		Output1.add("P4");
		Output1.add("P5");

		grdT1.Activations.add(new Activation(t1, doubleChannelDifferentiator, input1, TransitionOperation.FLRS, Output1));
		t1.GuardMappingList.add(grdT1);
		t1.Delay = 0;
		pn.Transitions.add(t1);

		// ---------------- T2 ----------------
		// trimite comanda u(k) catre HeaterTankPlant
		PetriTransition t2 = new PetriTransition(pn);
		t2.TransitionName = "T2";
		t2.InputPlaceName.add("P7");

		Condition T2Ct1 = new Condition(t2, "P7", TransitionCondition.NotNull);

		GuardMapping grdT2 = new GuardMapping();
		grdT2.condition = T2Ct1;

		grdT2.Activations.add(new Activation(t2, "P7", TransitionOperation.SendOverNetwork, "gascmd"));
		t2.GuardMappingList.add(grdT2);
		t2.Delay = 0;
		pn.Transitions.add(t2);

		// ---------------- T3 ----------------
		// dupa 1 t.u. e(k) ajunge in P0 (porneste ciclul urmator) si in P9 (devine e(k-1))
		PetriTransition t3 = new PetriTransition(pn);
		t3.TransitionName = "T3";
		t3.InputPlaceName.add("P5");

		Condition T3Ct1 = new Condition(t3, "P5", TransitionCondition.NotNull);

		GuardMapping grdT3 = new GuardMapping();
		grdT3.condition = T3Ct1;

		ArrayList<PlaceNameWithWeight> input3 = new ArrayList<>();
		input3.add(new PlaceNameWithWeight("P5", 1F));

		ArrayList<String> Output3 = new ArrayList<>();
		Output3.add("P0");
		Output3.add("P9");

		grdT3.Activations.add(new Activation(t3, OneXTwoDefaultTable, input3, TransitionOperation.FLRS, Output3));
		t3.GuardMappingList.add(grdT3);
		t3.Delay = 1;
		pn.Transitions.add(t3);

		// ---------------- T6 (nou) ----------------
		// du(k) = 0.8*e(k) + 0.2*e(k-1)  -> P10
		PetriTransition t6 = new PetriTransition(pn);
		t6.TransitionName = "T6";
		t6.InputPlaceName.add("P4");
		t6.InputPlaceName.add("P9");

		Condition T6Ct1 = new Condition(t6, "P4", TransitionCondition.NotNull);
		Condition T6Ct2 = new Condition(t6, "P9", TransitionCondition.NotNull);
		T6Ct1.SetNextCondition(LogicConnector.AND, T6Ct2);

		GuardMapping grdT6 = new GuardMapping();
		grdT6.condition = T6Ct1;

		ArrayList<PlaceNameWithWeight> input6 = new ArrayList<>();
		input6.add(new PlaceNameWithWeight("P4", 0.8F)); // K  * e(k)
		input6.add(new PlaceNameWithWeight("P9", 0.2F)); // Ki * e(k-1)

		ArrayList<String> Output6 = new ArrayList<>();
		Output6.add("P10");

		grdT6.Activations.add(new Activation(t6, adder, input6, TransitionOperation.FLRS, Output6));
		t6.GuardMappingList.add(grdT6);
		t6.Delay = 0;
		pn.Transitions.add(t6);

		// ---------------- T4 ----------------
		// u(k) = 1.2*du(k) + u(k-1), pus in P7 (spre gascmd) si P8 (memorie)
		PetriTransition t4 = new PetriTransition(pn);
		t4.TransitionName = "T4";
		t4.InputPlaceName.add("P10");
		t4.InputPlaceName.add("P6");

		Condition T4Ct1 = new Condition(t4, "P10", TransitionCondition.NotNull);
		Condition T4Ct2 = new Condition(t4, "P6", TransitionCondition.NotNull);
		T4Ct1.SetNextCondition(LogicConnector.AND, T4Ct2);

		GuardMapping grdT4 = new GuardMapping();
		grdT4.condition = T4Ct1;

		ArrayList<PlaceNameWithWeight> input4 = new ArrayList<>();
		input4.add(new PlaceNameWithWeight("P10", 1.2F));
		input4.add(new PlaceNameWithWeight("P6", 1F));

		ArrayList<String> Output4 = new ArrayList<>();
		Output4.add("P7");
		Output4.add("P8");

		grdT4.Activations.add(new Activation(t4, doubleChannelAdder, input4, TransitionOperation.FLRS, Output4));
		t4.GuardMappingList.add(grdT4);
		t4.Delay = 1;
		pn.Transitions.add(t4);

		// ---------------- T5 ----------------
		// u(k) din P8 devine u(k-1) in P6
		PetriTransition t5 = new PetriTransition(pn);
		t5.TransitionName = "T5";
		t5.InputPlaceName.add("P8");
		t5.InputPlaceName.add("P6");

		Condition T5Ct1 = new Condition(t5, "P8", TransitionCondition.NotNull);

		GuardMapping grdT5 = new GuardMapping();
		grdT5.condition = T5Ct1;

		ArrayList<PlaceNameWithWeight> input5 = new ArrayList<>();
		input5.add(new PlaceNameWithWeight("P8", 1F));

		ArrayList<String> Output5 = new ArrayList<>();
		Output5.add("P6");

		grdT5.Activations.add(new Activation(t5, OneXOneDefaultTable, input5, TransitionOperation.FLRS, Output5));
		t5.GuardMappingList.add(grdT5);
		t5.Delay = 0;
		pn.Transitions.add(t5);

		// ---------------- Pornire ----------------
		System.out.println("HTC PI started \n ------------------------------");
		pn.Delay = 500;
		pn.ShowLogInWindow = true;

		PetriNetWindow frame = new PetriNetWindow(false);
		frame.petriNet = pn;
		frame.setVisible(true);
	}
}