package kk.xwords;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class Dictionary {
    private final Alphabet alphabet;
    final Node root;

    Dictionary(Alphabet alphabet) {
        this.alphabet = alphabet;
        root = new Node();
    }

    public Alphabet getAlphabet() {
        return alphabet;
    }

    public class Node {
        final Node[] subNodes;
        boolean isFinal;

        Node() {
            this.subNodes = new Node[alphabet.length()];
        }

        final Node next(char ch) {
            return subNodes[alphabet.index(ch)];
        }
    }

    public Node nodeForPrefix(String prefix) {
        int len = prefix.length();
        Node node = root;
        for (int i = 0; i < len && node != null; i++)
            node = node.next(prefix.charAt(i));
        return node;
    }

    class Traverser {
        int limit = Integer.MAX_VALUE;
        int current = 0;
        Consumer<String> action;

        void traverseRcv(Node node, StringBuilder path) {
            current++;
            if (current >= limit)
                return;
            if (node.isFinal)
                action.accept(path.toString());
            int al = alphabet.length();
            for (int i = 0; i < al; i++) {
                Node sub = node.subNodes[i];
                if (sub == null)
                    continue;
                path.append(alphabet.get(i));
                traverseRcv(sub, path);
                path.deleteCharAt(path.length() - 1);
            }
        }

        void traverse(String prefix, int limit, Consumer<String> action) {
            this.limit = limit;
            this.current = 0;
            this.action = action;
            traverseRcv(nodeForPrefix(prefix), new StringBuilder(prefix));
        }
    }

    public void forEachWord(String prefix, int limit, Consumer<String> action) {
        new Traverser().traverse(prefix, limit, action);
    }


    public static void main(String[] args) throws IOException {
//        Dictionary dict = DictionaryUtils.linesWordsFromURL("https://raw.githubusercontent.com/" +
//                        "first20hours/google-10000-english/master/google-10000-english-usa.txt",
//                new Alphabet("abcdefghijklmnopqrstuvwxyz".toCharArray()));
        Dictionary dict = DictionaryUtils.loadWordsAndCompress("K:\\slowa.txt", new Alphabet("aąbcćdeęfghijklłmnńoópqrsśtuvwyzźż".toCharArray()));
//        Dictionary dict = Dictionary.loadFromAutomaton(Files.lines(Paths.get("/media/docs_ram/out.txt")).iterator());
        //new DictionaryCompressor().compress(dict);
        DictionaryUtils.writeDictionaryAutomatonToFile(dict, Paths.get("K:\\out4.txt"));
    }


}
