package helper;

import org.javatuples.Septet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class WriteEstimatesToCSV {

    public static void writeTasksToCSV(String filename, String machine, Workflow workflow, int experiment, List<Septet<String, String, String, double[] ,double[], double[], double[]>> resultsToSave) throws IOException {

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filename, true));


        File file = new File(filename);
        if (file.length() == 0) {
            bufferedWriter.write("Machine,Workflow,TaskName,Estimator,Feature,SizeInput,Predicted,Real,Deviation" + "\n");
        }
        resultsToSave.forEach(entry -> {

            for (int i = 0; i < entry.getValue5().length; i++) {
                try {
                    bufferedWriter.write(machine + "," +workflow + "-" + experiment + "," + entry.getValue0() + "," + entry.getValue1() + "," + entry.getValue2() + "," +  entry.getValue3()[i]  +","+ entry.getValue4()[i] + "," + entry.getValue5()[i] + "," + entry.getValue6()[i] + "\n");
                } catch (Exception e) {
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
