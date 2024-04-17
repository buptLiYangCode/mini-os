package bupt.os.component.cpu;

public class ProcessExecutionTask implements Runnable {
    private final String processName;
    private final String[] instructions;

    public ProcessExecutionTask(String processName, String[] instructions) {
        this.processName = processName;
        this.instructions = instructions;
    }

    @Override
    public void run() {
        for (String instruction : instructions) {
            executeInstruction(instruction);
        }
    }

    private void executeInstruction(String instruction) {
        String[] parts = instruction.split(" ");
        String command = parts[0];

        try {
            switch (command) {
                case "C" -> {
                    int computeTime = Integer.parseInt(parts[1]);
                    System.out.println("Computing for " + computeTime + " units of time.");
                    Thread.sleep(computeTime * 1000L); // Simulate computation
                    System.out.println(processName + "：" + instruction + "执行完成");
                }
                case "K" -> {
                    int inputTime = Integer.parseInt(parts[1]);
                    System.out.println("Waiting for keyboard input for " + inputTime + " units of time.");
                    Thread.sleep(inputTime * 1000L); // Simulate keyboard input
                    System.out.println(processName + "：" + instruction + "执行完成");
                }
                case "P" -> {
                    int printTime = Integer.parseInt(parts[1]);
                    System.out.println("Printing for " + printTime + " units of time.");
                    Thread.sleep(printTime * 1000L); // Simulate printing
                    System.out.println(processName + "：" + instruction + "执行完成");
                }
                case "R" -> {
                    String readFile = parts[1];
                    int readTime = Integer.parseInt(parts[2]);
                    System.out.println("Reading from file " + readFile + " for " + readTime + " units of time.");
                    Thread.sleep(readTime * 1000L); // Simulate file reading
                    System.out.println(processName + "：" + instruction + "执行完成");
                }
                case "W" -> {
                    String writeFile = parts[1];
                    int writeTime = Integer.parseInt(parts[2]);
                    int fileSize = Integer.parseInt(parts[3]);
                    System.out.println("Writing to file " + writeFile + " of size " + fileSize + " blocks for " + writeTime + " units of time.");
                    Thread.sleep(writeTime * 1000L); // Simulate file writing
                    System.out.println(processName + "：" + instruction + "执行完成");
                }
                case "Q" -> {
                    System.out.println("Terminating the program.");
                    System.out.println(processName + "：" + instruction + "执行完成");
                }
                default -> System.out.println("Unknown command.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Task was interrupted.");
        }
    }

}
