package PSO;

import java.util.Random;

public class Evaluator implements EvaluationFunction_double {

	public static Double oldValue = null;

	@Override
	public double evaluate(double[] solution) {
		for (double d : solution) {
			//return d;
			if (oldValue == null) {
				oldValue = d;
			} else {
				oldValue = Math.max(oldValue, d);
			}
		}
		return oldValue;
//		Random rand = new Random();
//		return oldValue + rand.nextDouble();
	}
}
