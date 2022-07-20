package ru.yandex.practicum.kanban.managers.inmemory;

import ru.yandex.practicum.kanban.managers.HistoryManager;
import ru.yandex.practicum.kanban.tasks.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private Node head;
    private Node tail;
    private final Map<Integer, Node> nodesById;

    public InMemoryHistoryManager() {
        nodesById = new HashMap<>();
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        Node newNode = linkLast(task);
        Node oldNode = nodesById.put(task.getId(), newNode);
        removeNode(oldNode);   //проверка на null в removeNode
    }

    @Override
    public void remove(int id) {
        //Удаляем Task и из списка, и из словаря
        Node node = nodesById.remove(id);
        removeNode(node);   //проверка на null в removeNode
    }

    @Override
    public List<Task> getHistory() {
        List<Task> result = new ArrayList<>();
        Node node = head;
        while (node != null) {
            result.add(node.getData());
            node = node.getNext();
        }
        return result;
    }

    @Override
    public void clearHistory() {
        nodesById.clear();
        head = null;
        tail = null;
    }

    @Override
    public void update(Task task) {
        if (task == null) {
            return;
        }
        Node node = nodesById.get(task.getId());
        if (node != null) {
            node.setData(task);
        }
    }

    //Добавляет узел в конец списка
    private Node linkLast(Task task) {
        Node node = new Node(task);
        if (head == null) {
            head = node;
        } else {
            tail.setNext(node);
            node.setPrev(tail);
        }
        tail = node;
        return node;
    }

    //Удаляет узел из списка
    private void removeNode(Node node) {
        if (node == null) {
            return;
        }
        Node prev = node.getPrev();
        Node next = node.getNext();
        if (prev != null) {
            prev.setNext(next);
        }
        if (next != null) {
            next.setPrev(prev);
        }
        if (head == node) {
            head = next;
        }
        if (tail == node) {
            tail = prev;
        }
    }

    private static class Node {
        private Node next;
        private Node prev;
        private Task data;

        public Node(Task data) {
            this.data = data;
        }

        public Task getData() {
            return data;
        }

        public void setData(Task data) {
            this.data = data;
        }

        public Node getNext() {
            return next;
        }

        public void setNext(Node next) {
            this.next = next;
        }

        public Node getPrev() {
            return prev;
        }

        public void setPrev(Node prev) {
            this.prev = prev;
        }
    }
}
