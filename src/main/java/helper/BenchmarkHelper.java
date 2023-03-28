package helper;

import domain.Microbenchmark;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class BenchmarkHelper {


    public static Microbenchmark readBenchValue(String taskName, TargetMachine targetMachine, Workflow workflow) {

        try {


            BufferedReader bufferedReader = new BufferedReader(new FileReader("benchmarks/bio-bench-" + targetMachine.name().toLowerCase() + ".csv"));
            bufferedReader.readLine();
            String s;

            while ((s = bufferedReader.readLine()) != null) {
                String[] splittedLine = s.split(",");

                if (splittedLine[0].equalsIgnoreCase(workflow.name()) && splittedLine[1].equalsIgnoreCase(taskName)) {

                    Microbenchmark microbenchmark = new Microbenchmark(workflow, taskName, targetMachine);
                    microbenchmark.addRealtimeToList(Double.valueOf(splittedLine[3]));
                    microbenchmark.addRealtimeToList(Double.valueOf(splittedLine[4]));
                    microbenchmark.addRealtimeToList(Double.valueOf(splittedLine[5]));

                    return microbenchmark;
                }

            }
        } catch (IOException exception) {

            exception.printStackTrace();
        }

        return null;
    }

    public static double medianBenchmarkValue(ArrayList<Double> values) {
        Collections.sort(values);

        try {


            if (values.size() % 2 == 1)
                return values.get((values.size() + 1) / 2 - 1);
            else {
                double lower = values.get(values.size() / 2 - 1);
                double upper = values.get(values.size() / 2);

                return (lower + upper) / 2.0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;

    }

    public static double estimateAverageForWorkflowMachine(Workflow workflow, TargetMachine targetMachine, Set<String> historicalTasks) {

        System.out.println("Sysout" + historicalTasks.toString());

        ArrayList<Double> factors = new ArrayList<>();

        for (String taskName : historicalTasks) {
            System.out.println(taskName);
            System.out.println(targetMachine);
            double localVal = BenchmarkHelper.medianBenchmarkValue(readBenchValue(taskName, TargetMachine.LOCAL, workflow).getRealtimes());
            double targetVal = BenchmarkHelper.medianBenchmarkValue(readBenchValue(taskName, targetMachine, workflow).getRealtimes());

            double fac = targetVal / localVal;

            if (fac != 1) {
                factors.add(fac);
            }


        }
        if (!factors.isEmpty()) {
            System.out.println("FacPrint: " + medianBenchmarkValue(factors));
            return medianBenchmarkValue(factors);
        } else {
            // bei lokaler Ausführung, da factors oben immer 1 und nie Elemente in Liste
            return 1;
        }

    }

    public static double estimateAverageTask(Workflow workflow, TargetMachine targetMachine, Set<String> historicalTasks, String taskName) {

        System.out.println("Sysout" + taskName.toString());

        double localVal = BenchmarkHelper.medianBenchmarkValue(readBenchValue(taskName, TargetMachine.LOCAL, workflow).getRealtimes());
        double targetVal = BenchmarkHelper.medianBenchmarkValue(readBenchValue(taskName, targetMachine, workflow).getRealtimes());

        double fac = targetVal / localVal;


        if (fac != 1) {
            // wird quasi nie gebraucht, da schon in Mail durchgeführt
            return fac;
        } else {
            return estimateAverageForWorkflowMachine(workflow, targetMachine, historicalTasks);
        }

    }

    public static double defineFactor(TargetMachine targetMachine) {

        double asok01_score_cpu_sysbench = 223;
        double asok02_score_cpu_sysbench = 223;
        double n1_score_cpu_sysbench = 369;
        double n2_score_cpu_sysbench = 467;
        double c2_score_cpu_sysbench = 523;
        double wally_score_cpu_sysbench = 437;


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

            wally_ratio_cpu = (wally_score_cpu_sysbench / c2_score_cpu_sysbench);
            //wally_ratio_cpu =  (wally_score_cpu_sysbench / wally_score_cpu_sysbench);
            wally_ratio_mem = wally_score_mem / c2_score_mem;
            wally_ration_io = wally_score_io / c2_score_io;

        } else if (targetMachine == TargetMachine.N1) {

            wally_ratio_cpu = (wally_score_cpu_sysbench / n1_score_cpu_sysbench);
            //wally_ratio_cpu = wally_score_cpu / n1_score_cpu;
            wally_ratio_mem = wally_score_mem / n1_score_mem;
            wally_ration_io = wally_score_io / n1_score_io;

        } else if (targetMachine == TargetMachine.N2) {

            wally_ratio_cpu = (wally_score_cpu_sysbench / n2_score_cpu_sysbench);
            //wally_ratio_cpu = wally_score_cpu / n2_score_cpu;
            wally_ratio_mem = wally_score_mem / n2_score_mem;
            wally_ration_io = wally_score_io / n2_score_io;
        } else if (targetMachine == TargetMachine.ASOK02) {

            wally_ratio_cpu = wally_score_cpu_sysbench / asok02_score_cpu_sysbench;
            wally_ration_io = wally_score_io / asok02_score_io;

        } else if (targetMachine == TargetMachine.ASOK01) {
            wally_ratio_cpu = wally_score_cpu_sysbench / asok01_score_cpu_sysbench;
            wally_ration_io = wally_score_io / asok01_score_io;
        }


        double factor = 0.5 * wally_ratio_cpu + 0.5 * wally_ration_io;
        return factor;
    }
}
