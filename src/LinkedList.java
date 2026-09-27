public class LinkedList {
    private static class Node {
        int value;
        Node next;

        Node(int value) {
            this.value = value;
        }
    }

    private Node head;
    private Node tail;
    private int size;

    private long accessCount;
    private long comparisonCount;

    public int size() {
        return size;
    }

    public void add(int x) {
        Node newNode = new Node(x);

        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }

        size++;
    }

    public void add(int index, int x) {
        checkPositionIndex(index);

        if (index == size) {
            add(x);
            return;
        }

        Node newNode = new Node(x);

        if (index == 0) {
            newNode.next = head;
            head = newNode;
            if (tail == null) {
                tail = newNode;
            }
        } else {
            Node previous = nodeAt(index - 1);
            newNode.next = previous.next;
            previous.next = newNode;
        }

        size++;
    }

    public int remove(int index) {
        checkElementIndex(index);
        int removed;

        if (index == 0) {
            removed = head.value;
            head = head.next;
            size--;
            if (size == 0) {
                tail = null;
            }
            return removed;
        }

        Node previous = nodeAt(index - 1);
        removed = previous.next.value;
        previous.next = previous.next.next;
        size--;

        if (index == size) {
            tail = previous;
        }

        return removed;
    }

    public int get(int index) {
        checkElementIndex(index);
        return nodeAt(index).value;
    }

    public boolean contains(int x) {
        Node current = head;

        while (current != null) {
            accessCount++;
            comparisonCount++;
            if (current.value == x) {
                return true;
            }
            current = current.next;
        }

        return false;
    }

    private Node nodeAt(int index) {
        Node current = head;

        for (int i = 0; i < index; i++) {
            accessCount++;
            current = current.next;
        }

        accessCount++;
        return current;
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
    }

    public long getAccessCount() {
        return accessCount;
    }

    public long getComparisonCount() {
        return comparisonCount;
    }
}
