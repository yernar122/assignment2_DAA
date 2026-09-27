import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class Benchmark {
    private static final int[] SIZES = {100, 1_000, 10_000, 100_000};
    private static final int REPEATS = 5;
    private static final long SEED = 42;
    private static volatile long blackhole = 0;

    public static void main(String[] args) throws Exception {
        File resultDir = new File("results/tables");
        if (!resultDir.exists() && !resultDir.mkdirs()) {
            throw new IOException("Could not create results directory");
        }

        warmUpJvm();

        workload1RandomAccess();
        workload2Search();
        workload3InsertionRemoval();
        workload4PriorityProcessing();

        System.out.println("Benchmarks finished. Blackhole = " + blackhole);
    }

    private static void warmUpJvm() {
        Random random = new Random(SEED);
        int[] data = randomData(5_000, random);
        int[] indices = randomIndices(5_000, data.length, random);

        DynamicArray array = buildArray(data);
        LinkedList list = buildList(data);
        MinHeap heap = new MinHeap();

        for (int value : data) {
            heap.insert(value);
        }

        long sum = 0;
        for (int index : indices) {
            sum += array.get(index);
            sum += list.get(index);
        }
        for (int i = 0; i < 1_000; i++) {
            array.contains(i);
            list.contains(i);
        }
        while (heap.size() > 0) {
            sum += heap.extractMin();
        }
        blackhole += sum;
    }

    private static void workload1RandomAccess() throws IOException {
        try (PrintWriter out = writer("workload1_random_access.csv")) {
            out.println("n,structure,avg_time_ms,element_accesses,theoretical_complexity");

            for (int n : SIZES) {
                Random random = new Random(SEED);
                int[] data = randomData(n, random);
                int[] indices = randomIndices(10_000, n, random);

                Result arrayResult = benchmarkRandomAccessArray(data, indices);
                Result listResult = benchmarkRandomAccessList(data, indices);

                out.printf("%d,Dynamic Array,%.6f,%d,Theta(1) per get%n",
                        n, arrayResult.timeMs, arrayResult.metric);
                out.printf("%d,Linked List,%.6f,%d,Theta(n) per get%n",
                        n, listResult.timeMs, listResult.metric);

                System.out.println("Workload 1 finished for n=" + n);
            }
        }
    }

    private static Result benchmarkRandomAccessArray(int[] data, int[] indices) {
        double totalTime = 0;
        long totalAccesses = 0;

        for (int repeat = 0; repeat < REPEATS; repeat++) {
            DynamicArray array = buildArray(data);
            array.resetMetrics();

            long start = System.nanoTime();
            long sum = 0;
            for (int index : indices) {
                sum += array.get(index);
            }
            long end = System.nanoTime();

            blackhole += sum;
            totalTime += nanosToMs(end - start);
            totalAccesses += array.getAccessCount();
        }

        return new Result(totalTime / REPEATS, totalAccesses / REPEATS);
    }

    private static Result benchmarkRandomAccessList(int[] data, int[] indices) {
        double totalTime = 0;
        long totalAccesses = 0;

        for (int repeat = 0; repeat < REPEATS; repeat++) {
            LinkedList list = buildList(data);
            list.resetMetrics();

            long start = System.nanoTime();
            long sum = 0;
            for (int index : indices) {
                sum += list.get(index);
            }
            long end = System.nanoTime();

            blackhole += sum;
            totalTime += nanosToMs(end - start);
            totalAccesses += list.getAccessCount();
        }

        return new Result(totalTime / REPEATS, totalAccesses / REPEATS);
    }

    private static void workload2Search() throws IOException {
        try (PrintWriter out = writer("workload2_search.csv")) {
            out.println("n,structure,avg_time_ms,comparisons,theoretical_complexity");

            for (int n : SIZES) {
                Random random = new Random(SEED);
                int[] data = randomData(n, random);
                int[] searchValues = randomSearchValues(1_000, n, random);

                Result arrayResult = benchmarkSearchArray(data, searchValues);
                Result listResult = benchmarkSearchList(data, searchValues);

                out.printf("%d,Dynamic Array,%.6f,%d,Theta(n) average/worst search%n",
                        n, arrayResult.timeMs, arrayResult.metric);
                out.printf("%d,Linked List,%.6f,%d,Theta(n) average/worst search%n",
                        n, listResult.timeMs, listResult.metric);

                System.out.println("Workload 2 finished for n=" + n);
            }
        }
    }

    private static Result benchmarkSearchArray(int[] data, int[] searchValues) {
        double totalTime = 0;
        long totalComparisons = 0;

        for (int repeat = 0; repeat < REPEATS; repeat++) {
            DynamicArray array = buildArray(data);
            array.resetMetrics();

            long start = System.nanoTime();
            int found = 0;
            for (int value : searchValues) {
                if (array.contains(value)) {
                    found++;
                }
            }
            long end = System.nanoTime();

            blackhole += found;
            totalTime += nanosToMs(end - start);
            totalComparisons += array.getComparisonCount();
        }

        return new Result(totalTime / REPEATS, totalComparisons / REPEATS);
    }

    private static Result benchmarkSearchList(int[] data, int[] searchValues) {
        double totalTime = 0;
        long totalComparisons = 0;

        for (int repeat = 0; repeat < REPEATS; repeat++) {
            LinkedList list = buildList(data);
            list.resetMetrics();

            long start = System.nanoTime();
            int found = 0;
            for (int value : searchValues) {
                if (list.contains(value)) {
                    found++;
                }
            }
            long end = System.nanoTime();

            blackhole += found;
            totalTime += nanosToMs(end - start);
            totalComparisons += list.getComparisonCount();
        }

        return new Result(totalTime / REPEATS, totalComparisons / REPEATS);
    }

    private static void workload3InsertionRemoval() throws IOException {
        try (PrintWriter out = writer("workload3_insertion_removal.csv")) {
            out.println("n,structure,operation,position,operations_performed,avg_time_ms,metric,metric_type,theoretical_complexity");

            for (int n : SIZES) {
                Random random = new Random(SEED);
                int[] baseData = randomData(n, random);
                int middleIndex = n / 2;

                writeW3(out, n, "Dynamic Array", "insert", "beginning",
                        benchmarkInsertArray(baseData, 0), "movements", "Theta(n) per insertion");
                writeW3(out, n, "Linked List", "insert", "beginning",
                        benchmarkInsertList(baseData, 0), "node_accesses", "Theta(1) per insertion");

                writeW3(out, n, "Dynamic Array", "insert", "middle",
                        benchmarkInsertArray(baseData, middleIndex), "movements", "Theta(n) per insertion");
                writeW3(out, n, "Linked List", "insert", "middle",
                        benchmarkInsertList(baseData, middleIndex), "node_accesses", "Theta(n) per insertion");

                // The specification asks for 1000 removals even when n = 100, which is impossible
                // without changing the initial input size. Keep the initial structure at exactly n
                // elements and perform as many valid removals as possible: min(1000, n).
                // For middle removal, recalculate the middle index after every removal so it stays valid.
                int removalOperations = Math.min(1_000, n);

                writeW3(out, n, "Dynamic Array", "remove", "beginning",
                        benchmarkRemoveArray(baseData, false, removalOperations), "movements", "Theta(n) per removal");
                writeW3(out, n, "Linked List", "remove", "beginning",
                        benchmarkRemoveList(baseData, false, removalOperations), "node_accesses", "Theta(1) per removal");

                writeW3(out, n, "Dynamic Array", "remove", "middle",
                        benchmarkRemoveArray(baseData, true, removalOperations), "movements", "Theta(n) per removal");
                writeW3(out, n, "Linked List", "remove", "middle",
                        benchmarkRemoveList(baseData, true, removalOperations), "node_accesses", "Theta(n) per removal");

                System.out.println("Workload 3 finished for n=" + n);
            }
        }
    }

    private static void writeW3(PrintWriter out, int n, String structure, String operation,
                                String position, Result result, String metricType, String complexity) {
        int operations = operation.equals("remove") ? Math.min(1_000, n) : 1_000;
        out.printf("%d,%s,%s,%s,%d,%.6f,%d,%s,%s%n",
                n, structure, operation, position, operations, result.timeMs, result.metric, metricType, complexity);
    }

    private static Result benchmarkInsertArray(int[] data, int index) {
        double totalTime = 0;
        long totalMovements = 0;

        for (int repeat = 0; repeat < REPEATS; repeat++) {
            DynamicArray array = buildArray(data);
            array.resetMetrics();

            long start = System.nanoTime();
            for (int i = 0; i < 1_000; i++) {
                array.add(index, i);
            }
            long end = System.nanoTime();

            blackhole += array.size();
            totalTime += nanosToMs(end - start);
            totalMovements += array.getMovementCount();
        }

        return new Result(totalTime / REPEATS, totalMovements / REPEATS);
    }

    private static Result benchmarkInsertList(int[] data, int index) {
        double totalTime = 0;
        long totalAccesses = 0;

        for (int repeat = 0; repeat < REPEATS; repeat++) {
            LinkedList list = buildList(data);
            list.resetMetrics();

            long start = System.nanoTime();
            for (int i = 0; i < 1_000; i++) {
                list.add(index, i);
            }
            long end = System.nanoTime();

            blackhole += list.size();
            totalTime += nanosToMs(end - start);
            totalAccesses += list.getAccessCount();
        }

        return new Result(totalTime / REPEATS, totalAccesses / REPEATS);
    }

    private static Result benchmarkRemoveArray(int[] data, boolean middle, int operations) {
        double totalTime = 0;
        long totalMovements = 0;

        for (int repeat = 0; repeat < REPEATS; repeat++) {
            DynamicArray array = buildArray(data);
            array.resetMetrics();

            long start = System.nanoTime();
            long sum = 0;
            for (int i = 0; i < operations; i++) {
                int index = middle ? array.size() / 2 : 0;
                sum += array.remove(index);
            }
            long end = System.nanoTime();

            blackhole += sum;
            totalTime += nanosToMs(end - start);
            totalMovements += array.getMovementCount();
        }

        return new Result(totalTime / REPEATS, totalMovements / REPEATS);
    }

    private static Result benchmarkRemoveList(int[] data, boolean middle, int operations) {
        double totalTime = 0;
        long totalAccesses = 0;

        for (int repeat = 0; repeat < REPEATS; repeat++) {
            LinkedList list = buildList(data);
            list.resetMetrics();

            long start = System.nanoTime();
            long sum = 0;
            for (int i = 0; i < operations; i++) {
                int index = middle ? list.size() / 2 : 0;
                sum += list.remove(index);
            }
            long end = System.nanoTime();

            blackhole += sum;
            totalTime += nanosToMs(end - start);
            totalAccesses += list.getAccessCount();
        }

        return new Result(totalTime / REPEATS, totalAccesses / REPEATS);
    }

    private static void workload4PriorityProcessing() throws IOException {
        try (PrintWriter out = writer("workload4_priority_processing.csv")) {
            out.println("n,phase,avg_time_ms,comparisons,theoretical_complexity");

            for (int n : SIZES) {
                Random random = new Random(SEED);
                int[] data = randomData(n, random);

                Result insertResult = benchmarkHeapInsert(data);
                Result extractResult = benchmarkHeapExtract(data);

                out.printf("%d,insert_all,%.6f,%d,O(n log n) upper bound; O(log n) per insert%n",
                        n, insertResult.timeMs, insertResult.metric);
                out.printf("%d,extract_all,%.6f,%d,Theta(n log n) total; O(log n) per extract%n",
                        n, extractResult.timeMs, extractResult.metric);

                System.out.println("Workload 4 finished for n=" + n);
            }
        }
    }

    private static Result benchmarkHeapInsert(int[] data) {
        double totalTime = 0;
        long totalComparisons = 0;

        for (int repeat = 0; repeat < REPEATS; repeat++) {
            MinHeap heap = new MinHeap();
            heap.resetMetrics();

            long start = System.nanoTime();
            for (int value : data) {
                heap.insert(value);
            }
            long end = System.nanoTime();

            if (!heap.isValidHeap()) {
                throw new IllegalStateException("Heap property failed after insertion");
            }

            blackhole += heap.peekMin();
            totalTime += nanosToMs(end - start);
            totalComparisons += heap.getComparisonCount();
        }

        return new Result(totalTime / REPEATS, totalComparisons / REPEATS);
    }

    private static Result benchmarkHeapExtract(int[] data) {
        double totalTime = 0;
        long totalComparisons = 0;

        for (int repeat = 0; repeat < REPEATS; repeat++) {
            MinHeap heap = new MinHeap();
            for (int value : data) {
                heap.insert(value);
            }
            heap.resetMetrics();

            long start = System.nanoTime();
            int previous = Integer.MIN_VALUE;
            long sum = 0;
            for (int i = 0; i < data.length; i++) {
                int value = heap.extractMin();
                if (value < previous) {
                    throw new IllegalStateException("Extraction order is not non-decreasing");
                }
                previous = value;
                sum += value;
            }
            long end = System.nanoTime();

            blackhole += sum;
            totalTime += nanosToMs(end - start);
            totalComparisons += heap.getComparisonCount();
        }

        return new Result(totalTime / REPEATS, totalComparisons / REPEATS);
    }

    private static DynamicArray buildArray(int[] data) {
        DynamicArray array = new DynamicArray();
        for (int value : data) {
            array.add(value);
        }
        return array;
    }

    private static LinkedList buildList(int[] data) {
        LinkedList list = new LinkedList();
        for (int value : data) {
            list.add(value);
        }
        return list;
    }

    private static int[] randomData(int n, Random random) {
        int[] data = new int[n];
        int bound = Math.max(1_000, n * 10);
        for (int i = 0; i < n; i++) {
            data[i] = random.nextInt(bound);
        }
        return data;
    }

    private static int[] randomIndices(int count, int n, Random random) {
        int[] indices = new int[count];
        for (int i = 0; i < count; i++) {
            indices[i] = random.nextInt(n);
        }
        return indices;
    }

    private static int[] randomSearchValues(int count, int n, Random random) {
        int[] values = new int[count];
        int bound = Math.max(1_000, n * 10);
        for (int i = 0; i < count; i++) {
            values[i] = random.nextInt(bound);
        }
        return values;
    }

    private static PrintWriter writer(String fileName) throws IOException {
        return new PrintWriter(new FileWriter("results/tables/" + fileName));
    }

    private static double nanosToMs(long nanos) {
        return nanos / 1_000_000.0;
    }

    private static class Result {
        final double timeMs;
        final long metric;

        Result(double timeMs, long metric) {
            this.timeMs = timeMs;
            this.metric = metric;
        }
    }
}
