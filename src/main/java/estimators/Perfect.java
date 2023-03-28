package estimators;

import org.javatuples.Septet;
import org.javatuples.Sextet;

public class Perfect implements Estimator{
    @Override
    public Septet<String, String, String, double[], double[], double[], double[]> estimateWith1DInput(String taskname, String resourceToPredict, double[] ids, double[] train_x, double[] train_y, double[] test_x, double[] test_y, double factor) {

        double[] toReturnError = new double[test_y.length];


        return new Septet<>(taskname, "Perfect", resourceToPredict, ids, test_y, test_y, toReturnError);
    }

    @Override
    public Sextet<String, String, String, Double, Double, Double> estimateWith2DInput(String taskname, String resourceToPredict, double[][] train_x, double[] train_y, double[][] test_x, double[] test_y, double factor) {
        return null;
    }
}
