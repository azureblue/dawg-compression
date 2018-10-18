package kk.xwords;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import static kk.xwords.DictionaryUtils.indexNodes;

public class DictionaryCompressor {

    static class PartitioningTraverser {
        HashMap<Integer, ArrayList<Dictionary.Node>> partitionMap;

        private int partition(Dictionary.Node node) {
            int maxSubHeight = 0;

            for (Dictionary.Node subNode : node.subNodes) {
                if (subNode == null)
                    continue;
                maxSubHeight = Math.max(maxSubHeight, partition(subNode));
            }
            partitionMap.putIfAbsent(maxSubHeight, new ArrayList<>());
            partitionMap.get(maxSubHeight).add(node);
            return maxSubHeight + 1;
        }

        PartitioningTraverser(HashMap<Integer, ArrayList<Dictionary.Node>> partitionMap) {
            this.partitionMap = partitionMap;
        }
    }

    private static void partition(Dictionary.Node node, HashMap<Integer, ArrayList<Dictionary.Node>> output) {
        new PartitioningTraverser(output).partition(node);
    }

    void compress(Dictionary dict) {
        char[] alpha = dict.getAlphabet().getCopy();
        Dictionary.Node root = dict.root;
        HashMap<Integer, ArrayList<Dictionary.Node>> partition = new HashMap<>();
        HashMap<Dictionary.Node, Integer> ids = new HashMap<>();
        HashMap<Dictionary.Node, Dictionary.Node> mergeMap = new HashMap<>();

        indexNodes(root, ids);
        partition(root, partition);

        int maxHeight = Collections.max(partition.keySet());
        ArrayList<Dictionary.Node> nodes;
        for (int h = 0; h <= maxHeight; h++) {
            nodes = partition.get(h);
            if (h > 0) {
                nodes.forEach(node -> {
                    for (int i = 0; i < node.subNodes.length; i++) {
                        Dictionary.Node subNode = node.subNodes[i];
                        if (subNode != null && mergeMap.containsKey(subNode))
                            node.subNodes[i] = mergeMap.remove(subNode);
                    }
                });
            }
            Comparator<Dictionary.Node> nodeComparator = createNodeComparator(alpha, ids);
            nodes.sort(nodeComparator);
            Dictionary.Node first = nodes.get(0);
            for (int i = 1; i < nodes.size(); i++) {
                Dictionary.Node node = nodes.get(i);
                if (nodeComparator.compare(node, first) == 0)
                    mergeMap.put(node, first);
                else
                    first = node;
            }
        }
    }

    private static Comparator<Dictionary.Node> createNodeComparator(char[] alphabet, HashMap<Dictionary.Node, Integer> ids) {
        return (nodeA, nodeB) -> {
            int isFinalCompare = Boolean.compare(nodeA.isFinal, nodeB.isFinal);
            if (isFinalCompare != 0) return isFinalCompare;
            for (int i = 0; i < alphabet.length; i++) {
                Dictionary.Node subNodeA = nodeA.subNodes[i];
                Dictionary.Node subNodeB = nodeB.subNodes[i];
                if (subNodeA == null) {
                    if (subNodeB != null)
                        return -1;
                } else if (subNodeB == null)
                    return 1;
                else {
                    int mergedCompare = Integer.compare(ids.get(subNodeA), ids.get(subNodeB));
                    if (mergedCompare != 0)
                        return mergedCompare;
                }
            }
            return 0;
        };
    }
}
