import java.util.ArrayList;
import java.util.PriorityQueue;

public class Tests {
    public static void main(String[] args) {
        testDynamicArray();
        testLinkedList();
        testMinHeap();
        testLargeInputs();
        System.out.println("All tests passed.");
    }

    private static void testDynamicArray() {
        DynamicArray array = new DynamicArray();
        check(array.size() == 0, "DynamicArray empty size");
        expectIndexError(() -> array.get(0));

        array.add(10);
        check(array.get(0) == 10, "DynamicArray one element");

        array.add(20);
        array.add(1, 15);
        check(array.get(0) == 10, "DynamicArray first value");
        check(array.get(1) == 15, "DynamicArray middle insertion");
        check(array.get(2) == 20, "DynamicArray last value");

        array.add(15);
        check(array.contains(15), "DynamicArray duplicates/search");
        check(array.remove(0) == 10, "DynamicArray boundary remove");
        expectIndexError(() -> array.get(100));

        ArrayList<Integer> expected = new ArrayList<>();
        DynamicArray actual = new DynamicArray();
        for (int i = 0; i < 200; i++) {
            expected.add(i);
            actual.add(i);
        }
        for (int i = 0; i < 200; i++) {
            check(expected.get(i) == actual.get(i), "DynamicArray vs ArrayList");
        }
    }

    private static void testLinkedList() {
        LinkedList list = new LinkedList();
        check(list.size() == 0, "LinkedList empty size");
        expectIndexError(() -> list.remove(0));

        list.add(10);
        list.add(20);
        list.add(1, 15);
        list.add(15);

        check(list.get(0) == 10, "LinkedList first value");
        check(list.get(1) == 15, "LinkedList middle insertion");
        check(list.contains(15), "LinkedList duplicates/search");
        check(list.remove(0) == 10, "LinkedList boundary remove");
        expectIndexError(() -> list.add(100, 1));

        java.util.LinkedList<Integer> expected = new java.util.LinkedList<>();
        LinkedList actual = new LinkedList();
        for (int i = 0; i < 200; i++) {
            expected.add(i);
            actual.add(i);
        }
        for (int i = 0; i < 200; i++) {
            check(expected.get(i) == actual.get(i), "LinkedList vs java.util.LinkedList");
        }
    }

    private static void testMinHeap() {
        MinHeap heap = new MinHeap();
        expectHeapError(heap::peekMin);
        expectHeapError(heap::extractMin);

        int[] values = {5, 3, 8, 1, 1, 7, 2};
        for (int value : values) {
            heap.insert(value);
            check(heap.isValidHeap(), "Heap property after insertion");
        }

        check(heap.peekMin() == 1, "Heap peekMin");

        PriorityQueue<Integer> expected = new PriorityQueue<>();
        for (int value : values) {
            expected.add(value);
        }

        int previous = Integer.MIN_VALUE;
        while (heap.size() > 0) {
            int actual = heap.extractMin();
            int expectedValue = expected.remove();
            check(actual == expectedValue, "MinHeap vs PriorityQueue");
            check(actual >= previous, "MinHeap non-decreasing order");
            check(heap.isValidHeap(), "Heap property after extraction");
            previous = actual;
        }
    }

    private static void testLargeInputs() {
        DynamicArray array = new DynamicArray();
        LinkedList list = new LinkedList();
        MinHeap heap = new MinHeap();

        for (int i = 0; i < 100_000; i++) {
            array.add(i);
            list.add(i);
            heap.insert(100_000 - i);
        }

        check(array.get(99_999) == 99_999, "DynamicArray large input");
        check(list.get(99_999) == 99_999, "LinkedList large input");
        check(heap.peekMin() == 1, "MinHeap large input");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Test failed: " + message);
        }
    }

    private static void expectIndexError(Runnable action) {
        try {
            action.run();
            throw new AssertionError("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
        }
    }

    private static void expectHeapError(Runnable action) {
        try {
            action.run();
            throw new AssertionError("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }
}
