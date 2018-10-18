package kk.xwords;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DictionaryUtils {
    static class DictionaryBuilder {
        private final Dictionary dict;

        public DictionaryBuilder(Alphabet alpha) {
            this.dict = new Dictionary(alpha);
        }

        public void addWord(String word) {
            Alphabet alpha = dict.getAlphabet();
            Dictionary.Node node = dict.root;
            char[] chars = word.toCharArray();
            for (char ch : chars)
                if (!alpha.contains(ch)) {
                    System.err.println("omitting word: " + word);
                    return;
                }
//                    throw new IllegalArgumentException("word '" + word + "' contains letter(s) that are not in the alphabet");
            for (char ch : chars) {
                if (node.subNodes[alpha.index(ch)] == null)
                    node = node.subNodes[alpha.index(ch)] = dict.new Node();
                else
                    node = node.subNodes[alpha.index(ch)];
            }
            node.isFinal = true;
        }

        public Dictionary getDictionary() {
            return dict;
        }
    }

    public static void indexNodes(Dictionary.Node node, HashMap<Dictionary.Node, Integer> output) {
        Queue<Dictionary.Node> queue = new ArrayDeque<>();
        HashSet<Dictionary.Node> seen = new HashSet<>();
        queue.add(node);
        int id = 0;
        while (!queue.isEmpty()) {
            Dictionary.Node no = queue.remove();
            output.put(no, id++);
            for (Dictionary.Node sub : no.subNodes) {
                if (sub != null && !seen.contains(sub)) {
                    queue.add(sub);
                    seen.add(sub);
                }
            }
        }
    }

    public static void writeDictionaryAutomatonToFile(Dictionary dict, Path path) throws IOException {
        Dictionary.Node root = dict.root;
        Alphabet alpha = dict.getAlphabet();
        HashMap<Dictionary.Node, Integer> ids = new HashMap<>();
        DictionaryUtils.indexNodes(root, ids);
        PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path));
        writer.println(alpha.getCopy());
        writer.println(ids.size());

        Queue<Dictionary.Node> queue = new ArrayDeque<>();
        HashSet<Dictionary.Node> visited = new HashSet<>();

        queue.add(root);
        visited.clear();
        while (!queue.isEmpty()) {
            Dictionary.Node node = queue.remove();
            if (visited.contains(node))
                continue;
            visited.add(node);
            writer.print(ids.get(node));
            writer.print(" " + (node.isFinal ? "1" : "0"));
            for (int i = 0; i < alpha.length(); i++) {
                Dictionary.Node sub = node.subNodes[i];
                if (sub == null) continue;
                writer.print(" " + alpha.get(i));
                writer.print(" ");
                writer.print(ids.get(sub));
                queue.add(sub);
            }
            writer.println();
        }
        writer.close();
    }

    public static Dictionary loadFromAutomaton(Iterator<String> lines) {
        Alphabet alphabet = new Alphabet(lines.next().toCharArray());
        int numberOfNodes = Integer.parseInt(lines.next());
        HashMap<Integer, Dictionary.Node> states = new HashMap<>(numberOfNodes);
        Dictionary dict = new Dictionary(alphabet);
        states.put(0, dict.root);
        IntStream.range(1, numberOfNodes).forEach(i -> states.put(i, dict.new Node()));
        lines.forEachRemaining(line -> {
            StringTokenizer tokens = new StringTokenizer(line);
            Dictionary.Node state = states.get(Integer.parseInt(tokens.nextToken()));
            state.isFinal = Integer.parseInt(tokens.nextToken()) == 1;
            while (tokens.hasMoreTokens()) {
                char edge = tokens.nextToken().charAt(0);
                int targetState = Integer.parseInt(tokens.nextToken());
                state.subNodes[alphabet.index(edge)] = states.get(targetState);
            }
        });
        return dict;
    }

    public static Dictionary loadWords(String path, Alphabet alphabet) throws IOException {
        DictionaryBuilder dictionaryBuilder = new DictionaryBuilder(alphabet);

        try (Stream<String> lines = Files.lines(Paths.get(path))) {
            lines.forEach(dictionaryBuilder::addWord);
        }
        return dictionaryBuilder.getDictionary();
    }

    public static Dictionary linesWordsFromURL(String urlAddress, Alphabet alphabet) throws IOException {
        DictionaryBuilder dictionaryBuilder = new DictionaryBuilder(alphabet);
        URL url = new URL(urlAddress);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
            br.lines().forEach(dictionaryBuilder::addWord);
        }
        return dictionaryBuilder.getDictionary();
    }
}
