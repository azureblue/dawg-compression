package kk.xwords;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DictionaryUtils {

    static class NodePathBuilder<T extends Node<T>> {

        private final Alphabet alpha;
        private final BiFunction<T, Character, T> subnodeMaker;
        private final Consumer<T> finalSetter;

        public NodePathBuilder(Alphabet alpha, BiFunction<T, Character, T> subnodeMaker, Consumer<T> finalSetter) {
            this.alpha = alpha;
            this.subnodeMaker = subnodeMaker;
            this.finalSetter = finalSetter;
        }

        public void addWord(T node, String word) {
            char[] chars = word.toCharArray();
            for (char ch : chars)
                if (!alpha.contains(ch)) {
                    System.err.println("omitting word: " + word);
                    return;
                }
//                    throw new IllegalArgumentException("word '" + word + "' contains letter(s) that are not in the alphabet");
            for (char ch : chars)
                if (node.next(ch) == null)
                    node = subnodeMaker.apply(node, ch);
                else
                    node = node.next(ch);
            finalSetter.accept(node);
        }
    }

    public static void indexNodes(Node<?> node, BiConsumer<Node<?>, Integer> action) {
        Queue<Node<?>> queue = new ArrayDeque<>();
        HashSet<Node<?>> seen = new HashSet<>();
        queue.add(node);
        int id = 0;

        while (!queue.isEmpty()) {
            Node<?> no = queue.remove();
            action.accept(no, id++);
            no.forEach(sub -> {
                if (sub != null && !seen.contains(sub)) {
                    queue.add(sub);
                    seen.add(sub);
                }
            });
        }
    }

    public static void writeDictionaryAutomatonToFile(Node<?> root, Alphabet alpha, Path path) throws IOException {
        HashMap<Node<?>, Integer> ids = new HashMap<>();
        DictionaryUtils.indexNodes(root, ids::put);
        PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path));
        writer.println(alpha.getCopy());
        writer.println(ids.size());

        Queue<Node<?>> queue = new ArrayDeque<>();
        HashSet<Node<?>> visited = new HashSet<>();

        queue.add(root);
        visited.clear();
        while (!queue.isEmpty()) {
            Node<?> node = queue.remove();
            if (visited.contains(node))
                continue;
            visited.add(node);
            writer.print(ids.get(node));
            writer.print(" " + (node.isFinal() ? "1" : "0"));
            for (int i = 0; i < alpha.length(); i++) {
                Node<?> sub = node.next(alpha.get(i));
                if (sub == null)
                    continue;
                writer.print(" " + alpha.get(i));
                writer.print(" ");
                writer.print(ids.get(sub));
                queue.add(sub);
            }
            writer.println();
        }
        writer.close();
    }

    public static Node<?> loadFromAutomaton(Iterator<String> lines) {
        Alphabet alphabet = new Alphabet(lines.next().toCharArray());
        int numberOfNodes = Integer.parseInt(lines.next());
        HashMap<Integer, ArrayBasedNode> states = new HashMap<>(numberOfNodes);
        ArrayNode root = new ArrayNode(alphabet);
        states.put(0, root);
        IntStream.range(1, numberOfNodes).forEach(i -> states.put(i, new ArrayNode(alphabet)));
        lines.forEachRemaining(line -> {
            StringTokenizer tokens = new StringTokenizer(line);
            ArrayBasedNode state = states.get(Integer.parseInt(tokens.nextToken()));
            state.setFinal(Integer.parseInt(tokens.nextToken()) == 1);
            while (tokens.hasMoreTokens()) {
                char edge = tokens.nextToken().charAt(0);
                int targetState = Integer.parseInt(tokens.nextToken());
                state.setSubnode(alphabet.index(edge), states.get(targetState));
            }
        });
        return root;
    }

    public static Node<?> loadWordsAndCompress(String path, Alphabet alphabet) throws IOException {
        NodePathBuilder<CompressorNode> db = new NodePathBuilder<>(alphabet,
                (node, ch)
                -> node.setSubnode(alphabet.index(ch), new CompressorNode(alphabet)),
                node -> node.setFinal(true));

        DictionaryCompressor dc = new DictionaryCompressor();
        Set<Map.Entry<Character, List<String>>> wordLists = Files.lines(Paths.get(path))
                .sorted()
                .collect(Collectors.groupingBy(str -> str.charAt(0))).entrySet();

        CompressorNode root = new CompressorNode(alphabet);
        wordLists.stream().parallel().forEach(wordList -> {
            System.out.println("'" + wordList.getKey() + "': thread: " + Thread.currentThread().getName());
            wordList.getValue().forEach(word -> db.addWord(root, word));
            dc.compress(root.next(wordList.getKey()), true, false);
        });

        dc.compress(root, false, true);
        HashMap a;
        TreeMap<Integer, String> as;
        return root;
    }

//    public static Dictionary linesWordsFromURL(String urlAddress, Alphabet alphabet) throws IOException {
//        NodePathBuilder dictionaryBuilder = new NodePathBuilder(alphabet);
//        URL url = new URL(urlAddress);
//        try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
//            br.lines().forEach(dictionaryBuilder::addWord);
//        }
//        return dictionaryBuilder.getDictionary();
//    }
}
