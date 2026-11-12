package PSO;

import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;

import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.Color;

public class PSO_double extends JFrame {
	
	JPanel panel;
	public PSO_double() {
		
	    panel = new JPanel();
		panel.setBackground(new Color(0, 0, 0));
		
		JButton btnNewButton = new JButton("Start");
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(btnNewButton)
						.addComponent(panel, GroupLayout.PREFERRED_SIZE, 514, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(133, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(4)
					.addComponent(btnNewButton)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 370, GroupLayout.PREFERRED_SIZE)
					.addGap(158))
		);
		getContentPane().setLayout(groupLayout);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				optimize(min,max,evaluationFunction,isMaximize,printLog,runUntilNoImprovementFor10Steps);
			}
		});
	}
	
	// Parameters
	private static final int NUM_PARTICLES = 50;
	private static final double C1 = 9.0; // Cognitive parameter
	private static final double C2 = 5.0; // Social parameter
	private static final double W = 0.4; // Inertia weight

	static int number_dimensions;
	public PSO_Particle[] particles;
	static double[] gBest;
	static double gBestFitness;

	public void initParticles(double[] min, double[] max, EvaluationFunction_double evaluationFunction,
			boolean isMaximize, boolean printLog) {
		particles = new PSO_Particle[NUM_PARTICLES];
		number_dimensions = min.length;
		gBest = new double[number_dimensions];
		gBestFitness = isMaximize ? Double.MIN_VALUE : Double.MAX_VALUE;
		// Initialize particles
		for (int i = 0; i < NUM_PARTICLES; i++) {
			particles[i] = new PSO_Particle(number_dimensions, min, max);
			double fitness = particles[i].evaluateFitness(evaluationFunction);
			if (percentBetter(fitness, gBestFitness, isMaximize) > 0) {
				gBestFitness = fitness;
				System.arraycopy(particles[i].position, 0, gBest, 0, number_dimensions);
			}
			if (printLog)
				printSolution("particle:" + i, particles[i].position, particles[i].evaluateFitness(evaluationFunction));
		}
	}

	
	double[] min;
	double[] max;
	EvaluationFunction_double evaluationFunction;
	boolean isMaximize;
	boolean printLog;
	boolean runUntilNoImprovementFor10Steps;
	public void optimizeParms(double[] min, double[] max, EvaluationFunction_double evaluationFunction,
			boolean isMaximize, boolean printLog, boolean runUntilNoImprovementFor10Steps)
	{
		this.min=min;
		this.max=max;
		this.evaluationFunction=evaluationFunction;
		this.isMaximize=isMaximize;
		this.printLog=printLog;
		this.runUntilNoImprovementFor10Steps=runUntilNoImprovementFor10Steps;
	}
	
	public double[] optimize(double[] min, double[] max, EvaluationFunction_double evaluationFunction,
			boolean isMaximize, boolean printLog, boolean runUntilNoImprovementFor10Steps) {
		initParticles(min, max, evaluationFunction, isMaximize, printLog);
		// PSO loop
		int iter = 0;
		int iterSinceNoImprovement = 0;
		while (iter < 100 && (!runUntilNoImprovementFor10Steps || (iterSinceNoImprovement < 10))) {
			
			
			iter = iter + 1;
			iterSinceNoImprovement = iterSinceNoImprovement + 1;

			for (PSO_Particle particle : particles) {
				drawOETPN(panel);
				double fitness = particle.evaluateFitness(evaluationFunction);
				if (percentBetter(fitness, particle.pBestFitness, isMaximize) > 0.0) {
					particle.pBestFitness = fitness;
					System.arraycopy(particle.position, 0, particle.pBest, 0, number_dimensions);
				}
				double generalPercentBetter = percentBetter(fitness, gBestFitness, isMaximize);
				if (generalPercentBetter > 0.0) {
					iterSinceNoImprovement = generalPercentBetter > 0.01 ? 0 : iterSinceNoImprovement;
					gBestFitness = fitness;
					System.arraycopy(particle.position, 0, gBest, 0, number_dimensions);
				}
				particle.updateVelocity(W, C1, C2, gBest);
				particle.updatePosition();
				particle.Print();
				drawOETPN(panel);
			}
			if (printLog)
				printSolution("iteration" + iter + ", ", gBest, gBestFitness);
		}
		// Output the best solution\
		printSolution("Best", gBest, gBestFitness);
		return gBest;
	}

	public double percentBetter(double fitness, double baseForComparing, boolean isMaximize) {
		// returneaza procentul de improvement. Procent > 0 => improvement
		if (isMaximize)
			return (fitness - baseForComparing) / Math.abs(baseForComparing);
		else
			return (baseForComparing - fitness) / Math.abs(fitness);
	}

	public void printSolution(String prefix, double[] solution, double fitness) {
		System.out.print(prefix + " solution: [");
		for (int i = 0; i < solution.length; i++) {
			System.out.print(String.format("%.3f", solution[i]) + (i == solution.length - 1 ? "]" : ", "));
		}
		System.out.println(", fitness: " + String.format("%.4f", fitness));
	}

	public void PrintAllParticles() {
		int index = 0;
		for (PSO_Particle p : particles) {
			System.out.println("Particle:" + index++);
			p.Print();
		}
	}
	
	public void drawOETPN(JPanel pnlGraphics) 
	{
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Graphics g = pnlGraphics.getGraphics();
		g.setFont(new Font("Consolas", Font.BOLD, 13));
		g.setColor(Color.black);
		g.fillRect(0, 0, pnlGraphics.getWidth(), pnlGraphics.getHeight());
		g.setColor(Color.white);
		int val=200;
		for (PSO_Particle particle : particles) {
			int p=(int)(particle.position[0]*val);
			int v=(int)(particle.velocity[0]*val);
			//g.drawLine(p,v,p+5,v+5);
			g.drawOval(p,v,10,10);
			//System.out.println (p+"- "+v);
		}
	}
}