package kk.xwords;

public interface Node<T extends Node<T>> extends Iterable<T> {

    T next(char ch);
    boolean isFinal();
}
