package PSO;

public class Start {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PSO_double p = new PSO_double();
		p.setVisible(true);
		p.setBounds(100, 100, 800, 600);
		p.optimizeParms(new double[] { 0.2 }, new double[] { 0.8 }, new Evaluator(), true, true, true);
		//p.PrintAllParticles();
	}

}
