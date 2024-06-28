import java.util.*;
import java.util.concurrent.*;

class Job implements Comparable<Job> {
    String name;
    int executionTime;
    List<String> requiredResources;
    List<String> dependencies;
    int importance;
    long startTime;

    public Job(String name, int executionTime, List<String> requiredResources, List<String> dependencies, int importance) {
        this.name = name;
        this.executionTime = executionTime;
        this.requiredResources = new ArrayList<>(requiredResources);
        this.dependencies = new ArrayList<>(dependencies);
        this.importance = importance;
    }

    @Override
    public int compareTo(Job other) {
        return other.importance - this.importance;
    }
}

public class JobScheduler {

    private static final Set<String> availableResources = new HashSet<>();
    private static final Map<String, Job> jobsMap = new HashMap<>();
    private static final Map<String, Long> jobCompletionTimes = new HashMap<>();
    private static final PriorityBlockingQueue<Job> jobQueue = new PriorityBlockingQueue<>();

    public static void main(String[] args) throws InterruptedException {
        List<Job> jobs = List.of(
                new Job("Job1", 6, List.of("CPU"), List.of(), 3),
                new Job("Job2", 4, List.of("GPU"), List.of(), 2),
                new Job("Job3", 8, List.of("CPU", "GPU"), List.of(), 4),
                new Job("Job4", 3, List.of("CPU"), List.of("Job1"), 1),
                new Job("Job5", 5, List.of("GPU"), List.of("Job2"), 3),
                new Job("Job6", 7, List.of("CPU", "GPU"), List.of("Job4"), 2),
                new Job("Job7", 2, List.of("CPU"), List.of(), 5),
                new Job("Job8", 4, List.of("GPU"), List.of(), 3),
                new Job("Job9", 6, List.of("CPU", "GPU"), List.of("Job7", "Job8"), 2),
                new Job("Job10", 3, List.of("CPU"), List.of("Job1", "Job7"), 1),
                new Job("Job11", 5, List.of("GPU"), List.of("Job2", "Job8"), 3),
                new Job("Job12", 4, List.of("CPU"), List.of("Job10"), 2),
                new Job("Job13", 6, List.of("GPU"), List.of("Job5"), 4),
                new Job("Job14", 3, List.of("CPU", "GPU"), List.of("Job12", "Job13"), 1),
                new Job("Job15", 7, List.of("CPU"), List.of("Job3", "Job6"), 3),
                new Job("Job16", 5, List.of("GPU"), List.of("Job3", "Job9"), 2),
                new Job("Job17", 4, List.of("CPU", "GPU"), List.of("Job11", "Job14"), 4),
                new Job("Job18", 3, List.of("CPU"), List.of("Job10", "Job12"), 1),
                new Job("Job19", 6, List.of("GPU"), List.of("Job13", "Job16"), 3),
                new Job("Job20", 2, List.of("CPU", "GPU"), List.of("Job17", "Job18"), 2)
        );

        initializeResources();
        initializeJobMap(jobs);

        long startTime = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(4);
        jobQueue.addAll(jobs);

        while (!jobQueue.isEmpty()) {
            Job job = jobQueue.poll();
            if (job != null && canStartJob(job)) {
                availableResources.removeAll(job.requiredResources);
                job.startTime = System.currentTimeMillis() - startTime;
                System.out.println("Starting " + job.name + " at " + job.startTime + " seconds using resources: " + job.requiredResources);
                executor.execute(() -> runJob(job, startTime));
            } else if (job != null) {
                jobQueue.add(job);
            }
            Thread.sleep(100);
        }

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    private static void initializeResources() {
        availableResources.add("CPU");
        availableResources.add("GPU");
    }

    private static void initializeJobMap(List<Job> jobs) {
        for (Job job : jobs) {
            jobsMap.put(job.name, job);
        }
    }

    private static boolean canStartJob(Job job) {
        for (String dependency : job.dependencies) {
            if (!jobCompletionTimes.containsKey(dependency)) {
                return false;
            }
        }
        return availableResources.containsAll(job.requiredResources);
    }

    private static void runJob(Job job, long startTime) {
        try {
            Thread.sleep(job.executionTime * 1000);
            long endTime = System.currentTimeMillis();
            jobCompletionTimes.put(job.name, endTime);
            availableResources.addAll(job.requiredResources);
            System.out.println("Completed " + job.name + " at " + (endTime - startTime) / 1000 + " seconds");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

