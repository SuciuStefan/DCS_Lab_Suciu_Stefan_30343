package PSO;

@FunctionalInterface
public interface EvaluationFunction_double {
    double evaluate(double[] solution);
}