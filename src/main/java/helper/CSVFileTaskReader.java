package helper;

import domain.HistoricTask;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVFileTaskReader {

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

            if (csv_arr.length == 20) {
                historicTask = new HistoricTask(csv_arr[0], csv_arr[1], csv_arr[2], Double.valueOf(csv_arr[3]), Double.valueOf(csv_arr[5]), csv_arr[4],
                        Double.valueOf(csv_arr[6]), Double.valueOf(csv_arr[7]), Double.valueOf(csv_arr[11]), Double.valueOf(csv_arr[9]),
                        Double.valueOf(csv_arr[10]), Double.valueOf(csv_arr[12]), Double.valueOf(csv_arr[13]), Double.valueOf(csv_arr[8]),
                        Double.valueOf(csv_arr[14]), Double.valueOf(csv_arr[16]), Double.valueOf(csv_arr[15]), Double.valueOf(csv_arr[17]),
                        Double.valueOf(csv_arr[18]), Double.valueOf(csv_arr[19]));
            } else {
                historicTask = new HistoricTask(csv_arr[0], csv_arr[1], csv_arr[2], Double.valueOf(csv_arr[3]), Double.valueOf(csv_arr[5]), csv_arr[4],
                        Double.valueOf(csv_arr[6]), Double.valueOf(csv_arr[8]), Double.valueOf(csv_arr[12]), Double.valueOf(csv_arr[10]),
                        Double.valueOf(csv_arr[11]), Double.valueOf(csv_arr[13]), Double.valueOf(csv_arr[14]), Double.valueOf(csv_arr[9]),
                        Double.valueOf(csv_arr[15]), Double.valueOf(csv_arr[17]), Double.valueOf(csv_arr[16]));
            }
            taskList.add(historicTask);
        }

        br.close();
        return taskList;
    }
}
