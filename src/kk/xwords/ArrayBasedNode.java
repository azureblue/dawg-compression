package kk.xwords;

import java.util.Iterator;

public abstract class ArrayBasedNode<T extends ArrayBasedNode<T>> implements Node<T> {
    
    boolean isFinal;
    int edges = 0;
    protected final Alphabet alphabet;
    private final Object[] subnodes;

    T setSubnode(int idx, T node) {
        if (subnodes[idx] == null && node != null)
            edges++;
        else if (subnodes[idx]  != null && node == null)
            edges--;
        subnodes[idx] = node;
        return node;
    }

    public ArrayBasedNode(Alphabet alphabet) {
        this.alphabet = alphabet;
        int alphaLen = alphabet.length();
        this.subnodes = new Object[alphaLen];
    }

    @Override
    public T next(char ch) {
        return (T) subnodes[alphabet.index(ch)];
    }
    
    public T getSubnode(int idx) {
        return (T) subnodes[idx];
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            int idx = 0;
            int visitedEdges = 0;

            @Override
            public boolean hasNext() {
                return visitedEdges < edges;
            }

            @Override
            public T next() {
                Object node;
                while ((node = subnodes[idx++]) == null);
                visitedEdges++;
                return (T) node;
            }
        };
    }
    
}
