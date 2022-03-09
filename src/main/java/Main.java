import domain.HistoricTask;
import estimators.*;
import helper.*;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.javatuples.Quartet;
import org.javatuples.Sextet;

import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class Main {


    public static void main(String[] args) throws IOException {

        DataProfile dataProfile = DataProfile.ALL_DATA;

        for (TargetMachine targetM : TargetMachine.values()) {

            for (Workflow wf : Workflow.values()) {

                System.out.println("Workflow: " + wf);

                for (int i = 1; i <= 2; i++) {

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
        List<HistoricTask> wally_tasks = readCSVFile("execution_reports/wally/results_" + workflow.toString().toLowerCase() + "/execution_report_" + experiment_number + "_wally.csv");

        List<HistoricTask> wally_tasksForTest = readCSVFile("execution_reports/wally/results_" + workflow.toString().toLowerCase() + "/execution_report_" + control_number + "_wally.csv");

        List<HistoricTask> wally_tasks_lower_frequ;
        List<HistoricTask> c2_tasks;
        List<HistoricTask> c2_tasksForTest;

        if (targetMachine != TargetMachine.LOCAL) {

            wally_tasks_lower_frequ = readCSVFile("execution_reports/wallyRedCpu/results_" + workflow.toString().toLowerCase() + "/execution_report_" + experiment_number + "_wallyRedCpu.csv");

            c2_tasks = readCSVFile("execution_reports/" + targetMachine.toString().toLowerCase() + "/results_" + workflow.toString().toLowerCase() + "/execution_report_" + experiment_number + "_"+ targetMachine.toString().toLowerCase() +".csv");

            c2_tasksForTest = readCSVFile("execution_reports/" + targetMachine.toString().toLowerCase() + "/results_" + workflow.toString().toLowerCase() + "/execution_report_" + control_number + "_" + targetMachine.toString().toLowerCase() + ".csv");
        } else {
            wally_tasks_lower_frequ = new ArrayList<>(wally_tasks);
            c2_tasks = new ArrayList<>(wally_tasks);
            c2_tasksForTest = new ArrayList<>(wally_tasks);
        }


        String to_predict = "Realtime";


        if (dataProfile == DataProfile.SAMPLED) {

        }


        Map<String, List<HistoricTask>> wally_tasks_grouped = wally_tasks.stream()
                .collect(Collectors.groupingBy(HistoricTask::getTaskName));

        Map<String, List<HistoricTask>> wally_tasks_ForTest_grouped = wally_tasksForTest.stream()
                .collect(Collectors.groupingBy(HistoricTask::getTaskName));

        Map<String, List<HistoricTask>> wally_tasks_lower_freq_grouped = wally_tasks_lower_frequ.stream()
                .collect(Collectors.groupingBy(HistoricTask::getTaskName));

        Map<String, List<HistoricTask>> c2_tasks_grouped = c2_tasks.stream()
                .collect(Collectors.groupingBy(HistoricTask::getTaskName));

        Map<String, List<HistoricTask>> c2_tasksForTest_grouped = c2_tasksForTest.stream()
                .collect(Collectors.groupingBy(HistoricTask::getTaskName));


        System.out.println("Feature to predict: " + to_predict);
        System.out.println("------------------------------");

        ArrayList<Sextet<String, String, String, Double, Double, double[]>> estimates = new ArrayList<>();

        wally_tasks_grouped.keySet().forEach(key -> {
            var listTasksWally = wally_tasks_grouped.get(key);
            var listTasksWallyLowerFrequ = wally_tasks_lower_freq_grouped.get(key);
            var listTasksC2 = c2_tasks_grouped.get(key);

            var listWallyTaskForTest = wally_tasks_ForTest_grouped.get(key);
            var listTasksC2ForTest = c2_tasksForTest_grouped.get(key);


            Quartet<double[], double[], double[], double[]> trainTestDataLotare = getTrainTestDataLotare(listTasksWally);


            var x_train_lotare = trainTestDataLotare.getValue0();
            var y_train_lotare = trainTestDataLotare.getValue1();


            var x_test_lotare = DoubleStream.concat(Arrays.stream(getTrainTestDataLotare(listTasksWally).getValue2()), Arrays.stream(getTrainTestDataLotare(listWallyTaskForTest).getValue2())).toArray();
            var y_test_lotare = DoubleStream.concat(Arrays.stream(getTrainTestDataLotare(listTasksWally).getValue3()), Arrays.stream(getTrainTestDataLotare(listWallyTaskForTest).getValue3())).toArray();


            if (targetMachine != TargetMachine.LOCAL) {
                Quartet<double[], double[], double[], double[]> trainTestDataLotareC2 = getTrainTestDataLotare(listTasksC2);
                x_test_lotare = DoubleStream.concat(Arrays.stream(getTrainTestDataLotare(listTasksC2).getValue2()), Arrays.stream(getTrainTestDataLotare(listTasksC2ForTest).getValue2())).toArray();
                y_test_lotare = DoubleStream.concat(Arrays.stream(getTrainTestDataLotare(listTasksC2).getValue3()), Arrays.stream(getTrainTestDataLotare(listTasksC2ForTest).getValue3())).toArray();


            }

            System.out.println("Task:        " + key + ":");
            Estimator ridge = new Lotaru();

            if (targetMachine != TargetMachine.LOCAL) {
                estimates.add(ridge.estimateWith1DInput(key, to_predict, x_train_lotare, y_train_lotare, x_test_lotare, y_test_lotare, defineFactor(listTasksWally.subList(0, listTasksWally.size() - 1), listTasksWallyLowerFrequ, 1.0 - (3000.0 / 3700.0), targetMachine)));

            } else {
                estimates.add(ridge.estimateWith1DInput(key, to_predict, x_train_lotare, y_train_lotare, x_test_lotare, y_test_lotare, 1));
            }

            Estimator onlineP = new Online(false, "Online-P");
            Estimator onlineM = new Online(true, "Online-M");
            Estimator naive = new Naive(true, "Naive");

            Quartet<double[], double[], double[], double[]> trainTestDataSilva = getTrainTestDataLotare(listTasksWally);


            estimates.add(onlineP.estimateWith1DInput(key, to_predict, x_train_lotare, y_train_lotare, x_test_lotare, y_test_lotare, 1));

            estimates.add(onlineM.estimateWith1DInput(key, to_predict, x_train_lotare, y_train_lotare, x_test_lotare, y_test_lotare, 1));

            estimates.add(naive.estimateWith1DInput(key, to_predict, x_train_lotare, y_train_lotare, x_test_lotare, y_test_lotare, 1));


            System.out.println("------------------------------");

        });

        try {
            WriteEstimatesToCSV.writeTasksToCSV("results/tasks_lotaru_" + targetMachine.toString().toLowerCase() + ".csv", workflow, experiment_number, estimates);

            //Lotaru

            DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(estimates.stream().filter(sextet -> sextet.getValue1().equalsIgnoreCase("Lotaru")).map(sextet -> sextet.getValue5()).flatMapToDouble(d -> DoubleStream.of(d)).toArray());

            double median_predicted_lotaru = descriptiveStatistics.getPercentile(50);
            System.out.println("Lotaru median deviation: " + median_predicted_lotaru);

            WriteEstimatesToCSV.writeCompleteWorkflowToCSV("results/lotare-wf-" + targetMachine.toString().toLowerCase() + ".csv", workflow, experiment_number, dataProfile, targetMachine, targetMachine, "Lotaru", median_predicted_lotaru, descriptiveStatistics.getSum());

            //Online-M

            descriptiveStatistics = new DescriptiveStatistics(estimates.stream().filter(sextet -> sextet.getValue1().equalsIgnoreCase("Online-M")).map(sextet -> sextet.getValue5()).flatMapToDouble(d -> DoubleStream.of(d)).toArray());

            double median_predicted_online_m = descriptiveStatistics.getPercentile(50);
            System.out.println("Online-M median deviation: " + median_predicted_online_m);


            WriteEstimatesToCSV.writeCompleteWorkflowToCSV("results/lotare-wf-" + targetMachine.toString().toLowerCase() + ".csv", workflow, experiment_number, dataProfile, targetMachine, targetMachine, "Online-M", median_predicted_online_m, descriptiveStatistics.getSum());

            //Online-P

            descriptiveStatistics = new DescriptiveStatistics(estimates.stream().filter(sextet -> sextet.getValue1().equalsIgnoreCase("Online-P")).map(sextet -> sextet.getValue5()).flatMapToDouble(d -> DoubleStream.of(d)).toArray());

            double median_predicted_online_p = descriptiveStatistics.getPercentile(50);
            System.out.println("Online-P median deviation: " + median_predicted_online_p);


            WriteEstimatesToCSV.writeCompleteWorkflowToCSV("results/lotare-wf-" + targetMachine.toString().toLowerCase() + ".csv", workflow, experiment_number, dataProfile, targetMachine, targetMachine, "Online-P", median_predicted_online_p, descriptiveStatistics.getSum());

            //Naive

            descriptiveStatistics = new DescriptiveStatistics(estimates.stream().filter(sextet -> sextet.getValue1().equalsIgnoreCase("Naive")).map(sextet -> sextet.getValue5()).flatMapToDouble(d -> DoubleStream.of(d)).toArray());

            double median_predicted_naive = descriptiveStatistics.getPercentile(50);
            System.out.println("Naive median deviation: " + median_predicted_naive);


            WriteEstimatesToCSV.writeCompleteWorkflowToCSV("results/lotare-wf-" + targetMachine.toString().toLowerCase() + ".csv", workflow, experiment_number, dataProfile, targetMachine, targetMachine, "Naive", median_predicted_naive, descriptiveStatistics.getSum());

        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }


    public static List<HistoricTask> readCSVFile(String filename) throws IOException {

        ArrayList<HistoricTask> taskList = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String s;

        while ((s = br.readLine()) != null) {
            if (s.contains("InputSize")) {
                continue;
            }
            String[] csv_arr = s.split(",");
            HistoricTask historicTask;

            if(s.contains("-")) {
                System.out.println("---");
            }


            if (csv_arr.length == 19) {
                historicTask = new HistoricTask(csv_arr[0], csv_arr[1], Double.valueOf(csv_arr[2]), Double.valueOf(csv_arr[4]), csv_arr[3],
                        Double.valueOf(csv_arr[5]), Double.valueOf(csv_arr[6]), Double.valueOf(csv_arr[10]), Double.valueOf(csv_arr[8]),
                        Double.valueOf(csv_arr[9]), Double.valueOf(csv_arr[11]), Double.valueOf(csv_arr[12]), Double.valueOf(csv_arr[7]),
                        Double.valueOf(csv_arr[13]), Double.valueOf(csv_arr[15]), Double.valueOf(csv_arr[14]), Double.valueOf(csv_arr[16]),
                        Double.valueOf(csv_arr[17]), Double.valueOf(csv_arr[18]));
            } else {
                historicTask = new HistoricTask(csv_arr[0], csv_arr[1], Double.valueOf(csv_arr[2]), Double.valueOf(csv_arr[4]), csv_arr[3],
                        Double.valueOf(csv_arr[5]), Double.valueOf(csv_arr[7]), Double.valueOf(csv_arr[11]), Double.valueOf(csv_arr[9]),
                        Double.valueOf(csv_arr[10]), Double.valueOf(csv_arr[12]), Double.valueOf(csv_arr[13]), Double.valueOf(csv_arr[8]),
                        Double.valueOf(csv_arr[14]), Double.valueOf(csv_arr[16]), Double.valueOf(csv_arr[15]));
            }
            taskList.add(historicTask);
        }

        br.close();
        return taskList;
    }


    public static Quartet<double[], double[], double[], double[]> getTrainTestDataLotare(List<HistoricTask> listTasks) {
        listTasks.sort((o1, o2) -> {
            if (o1.getNumberReads() > o2.getNumberReads()) {
                return 1;
            } else if (o1.getNumberReads() < o2.getNumberReads()) {
                return -1;
            } else {
                return 0;
            }
        });

        double x_train[] = new double[listTasks.size() - 1];
        double y_train[] = new double[listTasks.size() - 1];

        double x_test[] = new double[listTasks.size() - x_train.length];
        double y_test[] = new double[listTasks.size() - x_train.length];

        var mappedListOfTasks = listTasks.stream().map(task -> {
            double[][] arr = new double[1][2];
            arr[0][0] = task.getTaskInputSizeUncompressed();
            arr[0][1] = task.getRealtime();
            return arr;
        }).collect(Collectors.toList());

        for (int i = 0; i < x_train.length; i++) {
            x_train[i] = mappedListOfTasks.get(i)[0][0];
            y_train[i] = mappedListOfTasks.get(i)[0][1];
        }

        for (int i = 0; i < x_test.length; i++) {
            x_test[i] = mappedListOfTasks.get(i + x_train.length)[0][0];
            y_test[i] = mappedListOfTasks.get(i + x_train.length)[0][1];
        }

        return new Quartet<>(x_train, y_train, x_test, y_test);
    }


    static double defineFactor(List<HistoricTask> tasks_profiling, List<HistoricTask> tasks_reducedCPU, double freq_diff, TargetMachine targetMachine) {


        double n1_score_cpu_linpack = 3620426;
        double n2_score_cpu_linpack = 4045289;
        double c2_score_cpu_linpack = 4602096;
        double wally_score_cpu_linpack = 3959800;

        double asok01_score_cpu_sysbench = 223;
        double asok02_score_cpu_sysbench = 223;
        double n1_score_cpu_sysbench = 369;
        double n2_score_cpu_sysbench = 467;
        double c2_score_cpu_sysbench = 523;
        double wally_score_cpu_sysbench = 458;


        double n1_score_mem = 13400;
        double n2_score_mem = 17000;
        double c2_score_mem = 18900;
        double wally_score_mem = 18600;

        double asok01_score_io = 303;
        double asok02_score_io = 339;
        double n1_score_io = 483;
        double n2_score_io = 483;
        double c2_score_io = 483;
        double wally_score_io = 415;

        double wally_ratio_cpu = 0;
        double wally_ratio_mem = 0;
        double wally_ration_io = 0;


        if (targetMachine == TargetMachine.C2) {

            wally_ratio_cpu = 0.5 * (wally_score_cpu_linpack / c2_score_cpu_linpack) + 0.5 * (wally_score_cpu_sysbench / c2_score_cpu_sysbench);
            //wally_ratio_cpu =  (wally_score_cpu_sysbench / wally_score_cpu_sysbench);
            wally_ratio_mem = wally_score_mem / c2_score_mem;
            wally_ration_io = wally_score_io / c2_score_io;

        } else if (targetMachine == TargetMachine.N1) {

            wally_ratio_cpu = 0.5 * (wally_score_cpu_linpack / n1_score_cpu_linpack) + 0.5 * (wally_score_cpu_sysbench / n1_score_cpu_sysbench);
            //wally_ratio_cpu = wally_score_cpu / n1_score_cpu;
            wally_ratio_mem = wally_score_mem / n1_score_mem;
            wally_ration_io = wally_score_io / n1_score_io;

        } else if (targetMachine == TargetMachine.N2) {

            wally_ratio_cpu = 0.5 * (wally_score_cpu_linpack / n2_score_cpu_linpack) + 0.5 * (wally_score_cpu_sysbench / n2_score_cpu_sysbench);
            //wally_ratio_cpu = wally_score_cpu / n2_score_cpu;
            wally_ratio_mem = wally_score_mem / n2_score_mem;
            wally_ration_io = wally_score_io / n2_score_io;
        } else if (targetMachine == TargetMachine.ASOK02) {

            wally_ratio_cpu = wally_score_cpu_sysbench / asok02_score_cpu_sysbench;
            wally_ration_io = wally_score_io / asok02_score_io;

        } else if ( targetMachine == TargetMachine.ASOK01) {
            wally_ratio_cpu = wally_score_cpu_sysbench / asok01_score_cpu_sysbench;
            wally_ration_io = wally_score_io / asok01_score_io;
        }


        ArrayList<Double> deviation_freq = new ArrayList<>();

        for (HistoricTask historicTask : tasks_profiling) {
            for (HistoricTask historicTaskReducedFrequ : tasks_reducedCPU) {
                if (historicTask.getTaskInputSizeUncompressed() == historicTaskReducedFrequ.getTaskInputSizeUncompressed() ||
                        historicTask.getNumberReads() == historicTaskReducedFrequ.getNumberReads()) {
                    // System.out.println("Abweichung Frequ:" + (historicTask.getRealtime() - historicTaskReducedFrequ.getRealtime()) / historicTask.getRealtime());
                    deviation_freq.add((historicTaskReducedFrequ.getRealtime() / historicTask.getRealtime()) - 1);
                    //System.out.println("Wally/WallyFreq Runtime: " + historicTask.getTaskInputSizeUncompressed() + " # " + historicTaskReducedFrequ.getRealtime() / historicTask.getRealtime());

                }
            }
        }

        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(deviation_freq.stream().mapToDouble(Double::valueOf).toArray());

        double med_deviation = descriptiveStatistics.getPercentile(50);

        double factor = Math.max(0, Math.min(med_deviation / ((3700.0 / 3000.0) - 1.0), 1)) * wally_ratio_cpu + ((1 - Math.max(0, (Math.min(med_deviation / ((3700.0 / 3000.0) - 1.0), 1)))) * wally_ration_io);


        return factor;
    }
}
