package estimators;

import domain.SilhouetteScore;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import org.javatuples.Septet;
import org.javatuples.Sextet;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class Online implements Estimator {

    boolean median;

    String estimatorName;

    public Online(boolean median, String estimatorName) {
        this.median = median;
        this.estimatorName = estimatorName;
    }

    @Override
    public Septet<String, String, String, double[], double[], double[], double[]> estimateWith1DInput(String taskname, String resourceToPredict, double[] ids, double[] train_x, double[] train_y, double[] test_x, double[] test_y, double factor) {

        if (factor != 1 || test_x.length != test_x.length) {
            throw new IllegalArgumentException();
        }


        var pearson = calculatePearson(train_x, train_y);
        var actual = test_y[0];
        if (pearson > 0.8) {

            //var targetRatio = test_y[0] / test_x[0];


            double[] ratio = new double[test_y.length];

            for (int i = 0; i < test_y.length; i++) {

                var closestX = -1.0;
                var minDistance = Double.MAX_VALUE;

                int index = -1;

                int y = 0;
                for (double clos : train_x) {
                    if (minDistance > Math.abs(test_x[i] - clos)) {
                        minDistance = Math.abs(test_x[i] - clos);
                        closestX = clos;
                        index = y;
                    }
                    y++;
                }

                ratio[i] = train_y[index] / train_x[index];

            }

            double predicted[] = new double[ratio.length];
            double predictedError[] = new double[ratio.length];


            for (int i = 0; i < ratio.length; i++) {
                predicted[i] = test_x[i] * ratio[i];
                predictedError[i] = Math.abs((predicted[i] - test_y[i]) / test_y[i]);
            }

            return new Septet<>(taskname, estimatorName, resourceToPredict, ids, predicted, test_y, predictedError);
        } else {


            if (median) {


                DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(train_y);

                double median_predicted = descriptiveStatistics.getPercentile(50);

                double[] toReturnError = new double[test_y.length];

                for (int i = 0; i < toReturnError.length; i++) {
                    toReturnError[i] = Math.abs((median_predicted - test_y[i]) / test_y[i]);
                }

                double[] median_predicted_arr = new double[test_y.length];

                for (int i = 0; i < test_y.length; i++) {
                    median_predicted_arr[i] = median_predicted;
                }

                return new Septet<>(taskname, estimatorName, resourceToPredict, ids, median_predicted_arr, test_y, toReturnError);
            }

            ArrayList<DoublePoint> clusterPoints = new ArrayList<>();
            /**
             Arrays.stream(test_x).forEach(doubleArr -> {
             double[] arr = new double[2];
             arr[0] = doubleArr[0][0];
             arr[1] = doubleArr[0][1];
             clusterPoints.add(new DoublePoint(arr));
             });

             var clusterList = cluster(clusterPoints);
             **/
            // es wird ein Cluster basierend auf I/O read value gewählt
            // sollte bei uns eig. nicht vorkommen, da die größeren samples wesentlich mehr I/O haben. daher direk K-S Test

            KolmogorovSmirnovTest kolmogorovSmirnovTest = new KolmogorovSmirnovTest();
            DescriptiveStatistics stats = new DescriptiveStatistics();


            for (double d : train_y) {
                stats.addValue(d);
            }
            double mean = stats.getMean();
            double std = stats.getStandardDeviation();

            double predicted;

            if (std == 0) {
                predicted = stats.getPercentile(50);

                double[] toReturnError = new double[test_y.length];

                for (int i = 0; i < toReturnError.length; i++) {
                    toReturnError[i] = Math.abs((predicted - test_y[i]) / test_y[i]);
                }

                double[] percentile_predicted_arr = new double[test_y.length];

                for (int i = 0; i < test_y.length; i++) {
                    percentile_predicted_arr[i] = predicted;
                }

                return new Septet<>(taskname, estimatorName, resourceToPredict, ids, percentile_predicted_arr, test_y, toReturnError);
            }

            NormalDistribution normalDistribution = new NormalDistribution(mean, std);
            GammaDistribution gammaDistribution = new GammaDistribution(mean, 1);

            double k_s_normal = kolmogorovSmirnovTest.kolmogorovSmirnovTest(normalDistribution, train_y);
            double k_s_gamma = kolmogorovSmirnovTest.kolmogorovSmirnovTest(gammaDistribution, train_y);


            double critical_value = ksLookup(train_x.length);


            if (k_s_normal > critical_value) {
                RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
                predicted = randomDataGenerator.nextGaussian(normalDistribution.getMean(), normalDistribution.getStandardDeviation());
            } else if (k_s_gamma > critical_value) {
                RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
                predicted = randomDataGenerator.nextGamma(gammaDistribution.getShape(), gammaDistribution.getNumericalMean());
            } else {
                predicted = stats.getPercentile(50);
            }

            double[] toReturnError = new double[test_y.length];

            for (int i = 0; i < toReturnError.length; i++) {
                toReturnError[i] = Math.abs((predicted - test_y[i]) / test_y[i]);
            }

            double[] percentile_predicted_arr = new double[test_y.length];

            for (int i = 0; i < test_y.length; i++) {
                percentile_predicted_arr[i] = predicted;
            }

            return new Septet<>(taskname, estimatorName, resourceToPredict, ids, percentile_predicted_arr, test_y, toReturnError);
        }

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

    // https://web.archive.org/web/20160818104718/http://www.mathematik.uni-kl.de/~schwaar/Exercises/Tabellen/table_kolmogorov.pdf
    private double ksLookup(int n) {

        double criticalValue;

        switch (n) {
            case 1:
                criticalValue = 0.950;
                break;
            case 2:
                criticalValue = 0.776;
                break;
            case 3:
                criticalValue = 0.636;
                break;
            case 4:
                criticalValue = 0.565;
                break;
            case 5:
                criticalValue = 0.510;
                break;
            case 6:
                criticalValue = 0.468;
                break;
            case 7:
                criticalValue = 0.436;
                break;
            case 8:
                criticalValue = 0.410;
                break;
            case 9:
                criticalValue = 0.387;
                break;
            case 10:
                criticalValue = 0.369;
                break;
            case 11:
                criticalValue = 0.352;
                break;
            case 12:
                criticalValue = 0.338;
                break;
            case 13:
                criticalValue = 0.325;
                break;
            case 14:
                criticalValue = 0.314;
                break;
            case 15:
                criticalValue = 0.304;
                break;
            case 16:
                criticalValue = 0.295;
                break;
            case 17:
                criticalValue = 0.286;
                break;
            case 18:
                criticalValue = 0.279;
                break;
            case 19:
                criticalValue = 0.271;
                break;
            case 20:
                criticalValue = 0.265;
                break;
            case 21:
                criticalValue = 0.259;
                break;
            case 22:
                criticalValue = 0.253;
                break;
            case 23:
                criticalValue = 0.247;
                break;
            case 24:
                criticalValue = 0.242;
                break;
            default:
                criticalValue = 0.238;
                break;
        }

        return criticalValue;

    }
}
