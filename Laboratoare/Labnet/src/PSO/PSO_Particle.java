package PSO;

import java.util.Random;

public class PSO_Particle {
	public double[] position; // [a, b, c, etc]
	public double[] velocity;
	public double[] pBest;
	public double pBestFitness;
	public double[] min, max;

	PSO_Particle(int dimensions, double[] min, double[] max) {
		position = new double[dimensions];
		velocity = new double[dimensions];
		pBest = new double[dimensions];
		pBestFitness = Double.MIN_VALUE;
		this.min = min;
		this.max = max;
		Random rand = new Random();
		for (int i = 0; i < dimensions; i++) {
			// Random initialization
			position[i] = rand.nextDouble() * (max[i] - min[i]) + min[i];
			velocity[i] = rand.nextDouble() * 0.5 * (max[i] - min[i]) + min[i];
		}
	}

	void updateVelocity(double W, double C1, double C2, double[] gBest) {
		Random rand = new Random();
		for (int i = 0; i < position.length; i++) {
			double r1 = rand.nextDouble();
			double r2 = rand.nextDouble();
			velocity[i] = W * velocity[i] + C1 * r1 * (pBest[i] - position[i]) + C2 * r2 * (gBest[i] - position[i]);
		}
	}

	void updatePosition() {
		for (int i = 0; i < position.length; i++) {
			position[i] = limit(position[i] + velocity[i], min[i], max[i]);
		}
	}

	double limit(double x, double min, double max) {
		if (x < min)
			return min;
		else if (x > max)
			return max;
		else
			return x;
	}

	double evaluateFitness(EvaluationFunction_double evaluationFunction) {
		return evaluationFunction.evaluate(position);
	}

	public void Print() {
//		System.out.print(" pBestFitness ");
//		System.out.print(pBestFitness);
		System.out.print(" positions ");
		for (double p : position) {
			System.out.print(p);
		}
		System.out.print(" velocity ");
		for (double p : velocity) {
			System.out.print(p);
		}
//		System.out.print(" pBest ");
//		for (double p : pBest) {
//			System.out.print(p);
//		}
//		System.out.print(" min ");
//		for (double p : min) {
//			System.out.print(p);
//		}
//		System.out.print(" max ");
//		for (double p : max) {
//			System.out.print(p);
//		}
		System.out.println("--------------------------");
	}

}
