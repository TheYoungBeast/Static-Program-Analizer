package utils;

import java.util.Objects;

public class Pair<K, V> {

    private final K element0;
    private final V element1;

    @SuppressWarnings("unused")
    public static <K, V> Pair<K, V> createPair(K element0, V element1) {
        return new Pair<>(element0, element1);
    }

    public Pair(K element0, V element1) {
        this.element0 = element0;
        this.element1 = element1;
    }

    public K getFirst() {
        return element0;
    }

    public V getSecond() {
        return element1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(element0, pair.element0) && Objects.equals(element1, pair.element1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(element0, element1);
    }
}