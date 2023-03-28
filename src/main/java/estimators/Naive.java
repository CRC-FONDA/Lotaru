package estimators;

import domain.SilhouetteScore;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.javatuples.Septet;
import org.javatuples.Sextet;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class Naive implements Estimator {

    boolean median;

    String estimatorName;

    public Naive(boolean median, String estimatorName) {
        this.median = median;
        this.estimatorName = estimatorName;
    }

    @Override
    public Septet<String, String, String, double[], double[], double[], double[]> estimateWith1DInput(String taskname, String resourceToPredict, double[] ids, double[] train_x, double[] train_y, double[] test_x, double[] test_y, double factor) {

        if (factor != 1) {
            throw new IllegalArgumentException();
        }

        var actual = test_y[0];
        System.out.println("-----Naive-----");

        ArrayList<Double> ratios = new ArrayList<>();

        for (int i = 0; i < train_x.length; i++) {
            ratios.add(train_y[i] / train_x[i]);
        }

        //var targetRatio = test_y[0] / test_x[0];

        var ratio = ratios.stream().mapToDouble(Double::valueOf).average().orElseThrow();


        double[] toReturnError = new double[test_y.length];
        double[] toReturnPred = new double[test_y.length];
        for (int i = 0; i < toReturnError.length; i++) {
            toReturnError[i] = Math.abs((ratio * test_x[i] - test_y[i]) / test_y[i]);
            toReturnPred[i] = ratio * test_x[i];
        }

        return new Septet<>(taskname, estimatorName, resourceToPredict, ids, toReturnPred, test_y, toReturnError);

    }

    @Override
    public Sextet<String, String, String, Double, Double, Double> estimateWith2DInput(String taskname, String resourceToPredict, double[][] train_x, double[] train_y, double[][] test_x, double[] test_y, double factor) {
        return null;
    }

    public static double calculatePearson(double[] x, double[] y) {
        PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();

        return pearsonsCorrelation.correlation(x, y);
    }

    private static Cluster cluster(ArrayList<DoublePoint> points) {

        SortedMap<Integer, Cluster<DoublePoint>> clusterMap = new TreeMap<>();
        SortedMap<Integer, Double> mapWithScore = new TreeMap<>();

        Cluster<DoublePoint> cluster;

        SilhouetteScore silhouetteScore = new SilhouetteScore();

        var runs = points.size() < 10 ? points.size() : 10;

        for (int i = 3; i < runs; i++) {

            KMeansPlusPlusClusterer kMeansPlusPlusClusterer = new KMeansPlusPlusClusterer(i);
            List<Cluster<DoublePoint>> list = kMeansPlusPlusClusterer.cluster(points);
            int finalI = i;
            list.forEach(c -> {
                clusterMap.put(finalI, c);
            });

            mapWithScore.put(i, silhouetteScore.score(list));
        }

        cluster = clusterMap.get(mapWithScore.firstKey());
        return cluster;
    }
}
