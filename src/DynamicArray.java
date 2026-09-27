public class DynamicArray {
    private int[] data;
    private int size;

    private long accessCount;
    private long comparisonCount;
    private long movementCount;

    public DynamicArray() {
        data = new int[10];
        size = 0;
    }

    public int size() {
        return size;
    }

    public void add(int x) {
        ensureCapacity();
        data[size] = x;
        size++;
    }

    public void add(int index, int x) {
        checkPositionIndex(index);
        ensureCapacity();

        for (int i = size; i > index; i--) {
            data[i] = data[i - 1];
            movementCount++;
        }

        data[index] = x;
        size++;
    }

    public int remove(int index) {
        checkElementIndex(index);
        int removed = data[index];

        for (int i = index; i < size - 1; i++) {
            data[i] = data[i + 1];
            movementCount++;
        }

        size--;
        return removed;
    }

    public int get(int index) {
        checkElementIndex(index);
        accessCount++;
        return data[index];
    }

    public boolean contains(int x) {
        for (int i = 0; i < size; i++) {
            comparisonCount++;
            if (data[i] == x) {
                return true;
            }
        }
        return false;
    }

    private void ensureCapacity() {
        if (size == data.length) {
            int[] newData = new int[data.length * 2];
            for (int i = 0; i < size; i++) {
                newData[i] = data[i];
                movementCount++;
            }
            data = newData;
        }
    }

    private void checkElementIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Invalid index: " + index);
        }
    }

    private void checkPositionIndex(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Invalid index: " + index);
        }
    }

    public void resetMetrics() {
        accessCount = 0;
        comparisonCount = 0;
        movementCount = 0;
    }

    public long getAccessCount() {
        return accessCount;
    }

    public long getComparisonCount() {
        return comparisonCount;
    }

    public long getMovementCount() {
        return movementCount;
    }
}
