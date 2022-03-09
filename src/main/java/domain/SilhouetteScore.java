package domain;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.evaluation.ClusterEvaluator;
import org.apache.commons.math3.ml.distance.EuclideanDistance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SilhouetteScore extends ClusterEvaluator<DoublePoint> {

    @Override
    public double score(List<? extends Cluster<DoublePoint>> clusters) {

        EuclideanDistance euclideanDistance = new EuclideanDistance();

        ArrayList<Double> smallest_mean_distance_list = new ArrayList<>();

        for (int i = 0; i < clusters.size(); i++) {
            for (int j = 0; j < clusters.get(i).getPoints().size(); j++) {

                if (clusters.get(i).getPoints().size() == 1) {
                    smallest_mean_distance_list.add(0.0);
                    continue;
                }

                int finalI2 = i;
                int finalJ1 = j;
                double average_intra_distance = clusters.get(i).getPoints().stream()
                        .map((s) -> euclideanDistance.compute(s.getPoint(), clusters.get(finalI2).getPoints().get(finalJ1).getPoint()))
                        .reduce(0.0, (p1, p2) -> p1 + p2) / (clusters.get(i).getPoints().size() - 1);

                ArrayList<Double> average_nearest_distanceList = new ArrayList<>();


                int finalI = i;
                int finalI1 = i;
                int finalJ = j;
                clusters.stream().filter((cc) -> cc != clusters.get(finalI)).forEach((c) -> {
                    average_nearest_distanceList.add(
                            (c.getPoints().stream().map((s) -> euclideanDistance.compute(s.getPoint(), clusters.get(finalI1).getPoints().get(finalJ).getPoint()))
                                    .reduce(0.0, (p1, p2) -> p1 + p2)) / c.getPoints().size());
                });

                var min_average_nearest_distance = Collections.min(average_nearest_distanceList);

                var smallest_mean_distance = (min_average_nearest_distance - average_intra_distance) / (Math.max(min_average_nearest_distance, average_intra_distance));
                smallest_mean_distance_list.add(smallest_mean_distance);
            }

        }

        return smallest_mean_distance_list.stream().mapToDouble(a -> a).average().orElse(10.0);

    }
}



