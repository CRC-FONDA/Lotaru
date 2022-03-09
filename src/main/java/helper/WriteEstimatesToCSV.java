package helper;

import org.javatuples.Sextet;

import java.io.*;
import java.util.List;

public class WriteEstimatesToCSV {

    public static void writeTasksToCSV(String filename, Workflow workflow, int experiment, List<Sextet<String, String, String, Double, Double, double[]>> resultsToSave) throws IOException {

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filename, true));


        File file = new File(filename);
        if (file.length() == 0) {
            bufferedWriter.write("Workflow,TaskName,Estimator,Feature,Predicted,Real,Deviation" + "\n");
        }
        resultsToSave.forEach(entry -> {

            for (int i = 0; i < entry.getValue5().length; i++) {
                try {
                    bufferedWriter.write(workflow + "-" + experiment + "," + entry.getValue0() + "," + entry.getValue1() + "," + entry.getValue2() + "," + entry.getValue3() + "," + entry.getValue4() + "," + entry.getValue5()[i] + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });

        bufferedWriter.close();

    }

    public static void writeCompleteWorkflowToCSV(String filename, Workflow workflow, int experiment, DataProfile dataProfile, TargetMachine predictionForHardwareProfile, TargetMachine targetMachine, String estimator, Double medianDeviation, Double weightedDeviation) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filename, true));

        File file = new File(filename);
        if (file.length() == 0) {
            bufferedWriter.write("Workflow,Metric,ExperimentNr,Data,PredictionFor,Target, Estimator,MedianDeviation" + "\n");
        }

        bufferedWriter.write(workflow + "-" + experiment + ",Median Error" + "," + experiment + "," + dataProfile + "," + predictionForHardwareProfile + "," + targetMachine + "," + estimator + "," + medianDeviation + "\n");
        //bufferedWriter.write(workflow + "-" + experiment + ",Weighted Median Error" + "," + experiment + "," + dataProfile + "," + predictionForHardwareProfile + "," + targetMachine + "," + estimator + "," + weightedDeviation + "\n");
        bufferedWriter.close();

    }
}
