package estimators;

import org.javatuples.Sextet;

public interface Estimator {

    Sextet<String, String, String, double[], double[], double[]> estimateWith1DInput(String taskname, String resourceToPredict, double[] train_x, double[] train_y, double[] test_x, double[] test_y, double factor);

    Sextet<String, String, String, Double, Double, Double> estimateWith2DInput(String taskname, String resourceToPredict, double[][] train_x, double[] train_y, double[][] test_x, double[] test_y, double factor);
}
