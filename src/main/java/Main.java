import domain.HistoricTask;
import estimators.*;
import helper.*;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.javatuples.Septet;
import org.javatuples.Triplet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class Main {


    public static void main(String[] args) throws IOException {

        DataProfile dataProfile = DataProfile.ALL_DATA;

        for (TargetMachine targetM : TargetMachine.values()) {

            for (Workflow wf : Workflow.values()) {

                System.out.println("Workflow: " + wf);

                for (int i = 0; i <= 1; i++) {

                    int crtNbr;

                    if (i == 1) {
                        crtNbr = 2;
                    } else {
                        crtNbr = 1;
                    }

                    executePrediction(wf, i, crtNbr, targetM, dataProfile);

                }


            }


        }


    }

    private static void executePrediction(Workflow workflow, int experiment_number, int control_number, TargetMachine targetMachine, DataProfile dataProfile) throws IOException {
        // To configure each run
        List<HistoricTask> localMachineTraining = CSVFileTaskReader.readCSVFile("execution_reports/local/results_" + workflow.toString().toLowerCase() + "/execution_report_" + "local" + ".csv").stream().filter(task -> task.getLabel().contains("train-" + control_number)).collect(Collectors.toList());

        List<HistoricTask> LocalMachineTrainingReducedCPUFreq;

        List<HistoricTask> target_tasks = CSVFileTaskReader.readCSVFile("execution_reports/" + targetMachine.toString().toLowerCase() + "/results_" + workflow.toString().toLowerCase() + "/execution_report_" + targetMachine.toString().toLowerCase() + ".csv").stream().filter(task -> task.getLabel().contains("test")).collect(Collectors.toList());


        if (targetMachine != TargetMachine.LOCAL) {

            LocalMachineTrainingReducedCPUFreq = CSVFileTaskReader.readCSVFile("execution_reports/wallyRedCpu/results_" + workflow.toString().toLowerCase() + "/execution_report_wallyRedCpu.csv").stream().filter(task -> task.getLabel().contains("train")).collect(Collectors.toList());

        } else {
            LocalMachineTrainingReducedCPUFreq = new ArrayList<>(localMachineTraining);
        }


        String to_predict = "Realtime";


        if (dataProfile == DataProfile.SAMPLED) {

        }

        // Entweder an der Stelle oder später noc zwischen den beiden Test Labeln unterscheider
        Map<String, List<HistoricTask>> localTrainingGrouped = localMachineTraining.stream()
                .collect(Collectors.groupingBy(HistoricTask::getTaskName));

        Map<String, List<HistoricTask>> localTrainingReducedFrquencyGrouped = LocalMachineTrainingReducedCPUFreq.stream()
                .collect(Collectors.groupingBy(HistoricTask::getTaskName));

        Map<String, List<HistoricTask>> targetTestGrouped = target_tasks.stream()
                .collect(Collectors.groupingBy(HistoricTask::getTaskName));


        ArrayList<Septet<String, String, String, double[], double[], double[], double[]>> estimates = new ArrayList<>();

        localTrainingGrouped.keySet().forEach(key -> {
            var trainingDataPoints = localTrainingGrouped.get(key);
            var trainingDataPointsReducedFrequency = localTrainingReducedFrquencyGrouped.get(key);
            var testDataPoints = targetTestGrouped.get(key);


            // Das sollte jetzt die Liste mit X/Y Paaren sein fürs Training
            var train_taskInputSizeUncompressed = trainingDataPoints.stream().map(task -> new Triplet<Double, Double, Double>(task.getTaskInputSizeUncompressed(), task.getRealtime(), task.getWorkflowInputSizeUncompressed())).collect(Collectors.toList());

            // Das sollte die Liste mit X/Y Paaren für das testSet sein
            var test_taskInputSizeUncompressed = testDataPoints.stream().map(task -> new Triplet<Double, Double, Double>(task.getTaskInputSizeUncompressed(), task.getRealtime(), task.getWorkflowInputSizeUncompressed())).collect(Collectors.toList());

            var x_train_lotare = train_taskInputSizeUncompressed.stream().map(task -> task.getValue0()).collect(Collectors.toList()).stream().limit(6).mapToDouble(d -> d).toArray();
            var y_train_lotare = train_taskInputSizeUncompressed.stream().map(task -> task.getValue1()).collect(Collectors.toList()).stream().limit(6).mapToDouble(d -> d).toArray();


            var x_test_lotare = test_taskInputSizeUncompressed.stream().map(task -> task.getValue0()).collect(Collectors.toList()).stream().mapToDouble(d -> d).toArray();
            var y_test_lotare = test_taskInputSizeUncompressed.stream().map(task -> task.getValue1()).collect(Collectors.toList()).stream().mapToDouble(d -> d).toArray();

            var lotare_id_WfInputSize = test_taskInputSizeUncompressed.stream().map(task -> task.getValue2()).collect(Collectors.toList()).stream().mapToDouble(d -> d).toArray();

            var train_taskRChar = trainingDataPoints.stream().map(task -> new Triplet<Double, Double, Double>(task.getRchar(), task.getRealtime(), task.getWorkflowInputSizeUncompressed())).collect(Collectors.toList());
            var test_taskRChar = testDataPoints.stream().map(task -> new Triplet<Double, Double, Double>(task.getRchar(), task.getRealtime(), task.getWorkflowInputSizeUncompressed())).collect(Collectors.toList());

            var x_train_rchar = train_taskRChar.stream().map(task -> task.getValue0()).collect(Collectors.toList()).stream().mapToDouble(d -> d).limit(5).toArray();
            var y_train_rchar = train_taskRChar.stream().map(task -> task.getValue1()).collect(Collectors.toList()).stream().mapToDouble(d -> d).limit(5).toArray();

            var x_test_rchar = test_taskRChar.stream().map(task -> task.getValue0()).collect(Collectors.toList()).stream().mapToDouble(d -> d).toArray();
            var y_test_rchar = test_taskRChar.stream().map(task -> task.getValue1()).collect(Collectors.toList()).stream().mapToDouble(d -> d).toArray();

            var rchar_id_WfInputSize = test_taskRChar.stream().map(task -> task.getValue2()).collect(Collectors.toList()).stream().mapToDouble(d -> d).toArray();

            System.out.println("Task:        " + key + ":");
            Estimator lotaruG = new LotaruG();

            if (targetMachine != TargetMachine.LOCAL) {
                estimates.add(lotaruG.estimateWith1DInput(key, to_predict, lotare_id_WfInputSize, x_train_lotare, y_train_lotare, x_test_lotare, y_test_lotare, BenchmarkHelper.defineFactor(targetMachine)));

            } else {
                estimates.add(lotaruG.estimateWith1DInput(key, to_predict, lotare_id_WfInputSize, x_train_lotare, y_train_lotare, x_test_lotare, y_test_lotare, 1));
            }

            Estimator onlineP = new Online(false, "OnlineP");
            Estimator onlineM = new Online(true, "OnlineM");
            Estimator naive = new Naive(true, "Naive");
            Estimator lotaruA = new LotaruA();
            Estimator perfect = new Perfect();


            estimates.add(onlineP.estimateWith1DInput(key, to_predict, lotare_id_WfInputSize, x_train_lotare, y_train_lotare, x_test_lotare, y_test_lotare, 1));

            estimates.add(onlineM.estimateWith1DInput(key, to_predict, lotare_id_WfInputSize, x_train_lotare, y_train_lotare, x_test_lotare, y_test_lotare, 1));

            estimates.add(naive.estimateWith1DInput(key, to_predict, lotare_id_WfInputSize, x_train_lotare, y_train_lotare, x_test_lotare, y_test_lotare, 1));

            double microbenchmarkLocal = BenchmarkHelper.medianBenchmarkValue(BenchmarkHelper.readBenchValue(key, TargetMachine.LOCAL, workflow).getRealtimes());
            double microbenchmarkTarget = BenchmarkHelper.medianBenchmarkValue(BenchmarkHelper.readBenchValue(key, targetMachine, workflow).getRealtimes());

            double lotaruAFactor = microbenchmarkTarget / microbenchmarkLocal;
            if (lotaruAFactor == 1.0) {
                lotaruAFactor = BenchmarkHelper.estimateAverageForWorkflowMachine(workflow, targetMachine, localTrainingGrouped.keySet());
            }


            estimates.add(lotaruA.estimateWith1DInput(key, to_predict,lotare_id_WfInputSize, x_train_lotare, y_train_lotare, x_test_lotare, y_test_lotare, lotaruAFactor));

            estimates.add(perfect.estimateWith1DInput(key, to_predict, lotare_id_WfInputSize, x_train_lotare, y_train_lotare ,x_test_lotare, y_test_lotare, 1));
            System.out.println("------------------------------");

        });

        try {

            // All Tasks

            WriteEstimatesToCSV.writeTasksToCSV("results/tasks_lotaru_" + targetMachine.toString().toLowerCase() + ".csv",targetMachine.toString().toLowerCase(), workflow, experiment_number, estimates);

        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }




}
