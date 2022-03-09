package domain;

public class HistoricTask {

    private String machine;

    private String workflow;

    private double numberReads;

    private double taskInputSize;

    private double taskInputSizeUncompressed;

    private double workflowInputSize;

    private double workflowInputSizeUncompressed;

    private String taskName;

    private double realtime;

    private double cpu_usage;

    private double cpus_assigned;

    private double rchar;

    private double wchar;

    private double read_bytes;

    private double write_bytes;

    private double rss;

    private double vmem;

    private double peak_rss;

    private double memory_assigned;

    public HistoricTask(String machine, String workflow, double numberReads, double workflowInputSize, String taskName, double realtime, double cpu_usage, double cpus_assigned, double rchar, double wchar, double syscr, double syscw, double rss, double vmem, double peak_rss, double memory_assigned, double taskInputSize, double taskInputSizeUncompressed, double workflowInputSizeUncompressed) {
        this.machine = machine;
        this.workflow = workflow;
        this.numberReads = numberReads;
        this.workflowInputSize = workflowInputSize;
        this.taskName = taskName;
        this.realtime = realtime;
        this.cpu_usage = cpu_usage;
        this.cpus_assigned = cpus_assigned;
        this.rchar = rchar;
        this.wchar = wchar;
        this.read_bytes = syscr;
        this.write_bytes = syscw;
        this.rss = rss;
        this.vmem = vmem;
        this.peak_rss = peak_rss;
        this.memory_assigned = memory_assigned;
        this.taskInputSize = taskInputSize;
        this.taskInputSizeUncompressed = taskInputSizeUncompressed;
        this.workflowInputSizeUncompressed = workflowInputSizeUncompressed;
    }

    public HistoricTask(String machine, String workflow, double numberReads, double workflowInputSize, String taskName, double realtime, double cpu_usage, double cpus_assigned, double rchar, double wchar, double syscr, double syscw, double rss, double vmem, double peak_rss, double memory_assigned) {
        this.machine = machine;
        this.workflow = workflow;
        this.numberReads = numberReads;
        this.workflowInputSize = workflowInputSize;
        this.taskName = taskName;
        this.realtime = realtime;
        this.cpu_usage = cpu_usage;
        this.cpus_assigned = cpus_assigned;
        this.rchar = rchar;
        this.wchar = wchar;
        this.read_bytes = syscr;
        this.write_bytes = syscw;
        this.rss = rss;
        this.vmem = vmem;
        this.peak_rss = peak_rss;
        this.memory_assigned = memory_assigned;
    }

    public String getMachine() {
        return machine;
    }

    public void setMachine(String machine) {
        this.machine = machine;
    }

    public String getWorkflow() {
        return workflow;
    }

    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

    public double getWorkflowInputSize() {
        return workflowInputSize;
    }

    public void setWorkflowInputSize(double workflowInputSize) {
        this.workflowInputSize = workflowInputSize;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public double getRealtime() {
        return realtime;
    }

    public void setRealtime(double realtime) {
        this.realtime = realtime;
    }

    public double getCpu_usage() {
        return cpu_usage;
    }

    public void setCpu_usage(double cpu_usage) {
        this.cpu_usage = cpu_usage;
    }

    public double getCpus_assigned() {
        return cpus_assigned;
    }

    public void setCpus_assigned(double cpus_assigned) {
        this.cpus_assigned = cpus_assigned;
    }

    public double getRchar() {
        return rchar;
    }

    public void setRchar(double rchar) {
        this.rchar = rchar;
    }

    public double getWchar() {
        return wchar;
    }

    public void setWchar(double wchar) {
        this.wchar = wchar;
    }

    public double getRead_bytes() {
        return read_bytes;
    }

    public void setRead_bytes(double read_bytes) {
        this.read_bytes = read_bytes;
    }

    public double getWrite_bytes() {
        return write_bytes;
    }

    public void setWrite_bytes(double write_bytes) {
        this.write_bytes = write_bytes;
    }

    public double getRss() {
        return rss;
    }

    public void setRss(double rss) {
        this.rss = rss;
    }

    public double getVmem() {
        return vmem;
    }

    public void setVmem(double vmem) {
        this.vmem = vmem;
    }

    public double getPeak_rss() {
        return peak_rss;
    }

    public void setPeak_rss(double peak_rss) {
        this.peak_rss = peak_rss;
    }

    public double getMemory_assigned() {
        return memory_assigned;
    }

    public void setMemory_assigned(double memory_assigned) {
        this.memory_assigned = memory_assigned;
    }

    public double getNumberReads() {
        return numberReads;
    }

    public void setNumberReads(double numberReads) {
        this.numberReads = numberReads;
    }

    public double getTaskInputSize() {
        return taskInputSize;
    }

    public void setTaskInputSize(double taskInputSize) {
        this.taskInputSize = taskInputSize;
    }

    public double getTaskInputSizeUncompressed() {
        return taskInputSizeUncompressed;
    }

    public void setTaskInputSizeUncompressed(double taskInputSizeUncompressed) {
        this.taskInputSizeUncompressed = taskInputSizeUncompressed;
    }

    public double getWorkflowInputSizeUncompressed() {
        return workflowInputSizeUncompressed;
    }

    public void setWorkflowInputSizeUncompressed(double workflowInputSizeUncompressed) {
        this.workflowInputSizeUncompressed = workflowInputSizeUncompressed;
    }
}
