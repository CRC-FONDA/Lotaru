package domain;

import helper.TargetMachine;
import helper.Workflow;

import java.util.ArrayList;

public class Microbenchmark {

    private Workflow workflow;

    private String taskName;

    private TargetMachine targetMachine;

    private ArrayList<Double> realtimes = new ArrayList<>();

    public Microbenchmark(Workflow workflow, String taskName, TargetMachine targetMachine) {
        this.workflow = workflow;
        this.taskName = taskName;
        this.targetMachine = targetMachine;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public TargetMachine getTargetMachine() {
        return targetMachine;
    }

    public void setTargetMachine(TargetMachine targetMachine) {
        this.targetMachine = targetMachine;
    }

    public ArrayList<Double> getRealtimes() {
        return realtimes;
    }

    public void setRealtimes(ArrayList<Double> realtimes) {
        this.realtimes = realtimes;
    }

    public void addRealtimeToList(Double realtime) {
        this.realtimes.add(realtime);
    }

    @Override
    public String toString() {
        return "Microbenchmark{" +
                "workflow=" + workflow +
                ", taskName='" + taskName + '\'' +
                ", targetMachine=" + targetMachine +
                ", realtimes=" + realtimes +
                '}';
    }
}
