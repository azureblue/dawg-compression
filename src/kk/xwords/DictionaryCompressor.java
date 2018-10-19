package kk.xwords;

import java.util.ArrayList;
import java.util.Arrays;
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

        private static HashMap<Integer, ArrayList<Dictionary.Node>> startPartition(Dictionary.Node node) {
            final HashMap<Integer, ArrayList<Dictionary.Node>> res = new HashMap<>();
            new PartitioningTraverser(res).partition(node);
            return res;
        }
    }

    private static HashMap<Integer, ArrayList<Dictionary.Node>> partition(Dictionary.Node node) {
//        HashMap<Integer, ArrayList<Dictionary.Node>> partitionMap = Arrays.stream(node.subNodes)
//                .filter(Objects::nonNull)
//                .parallel()
//                .map(PartitioningTraverser::startPartition)
//                .reduce((acc, el) -> {
//                    el.entrySet().forEach(entry -> acc.merge(entry.getKey(), entry.getValue(),
//                            (accArray, elArray) -> {
//                                accArray.addAll(elArray);
//                                return accArray;
//                            }));
//                    return acc;
//                }).get();
//        partitionMap.put(Collections.max(partitionMap.keySet()) + 1, 
//                new ArrayList(Collections.singletonList(node)));
        return PartitioningTraverser.startPartition(node);
    }

    void compress(Dictionary.Node root, Alphabet al, boolean isTree, boolean parallel) {
        char[] alpha = al.getCopy();
        HashMap<Dictionary.Node, Dictionary.Node> mergeMap = new HashMap<>();
        HashMap<Dictionary.Node, Integer> ids = new HashMap<>();

        indexNodes(root, ids);
        HashMap<Integer, ArrayList<Dictionary.Node>> partition = partition(root);

        int maxHeight = Collections.max(partition.keySet());
        ArrayList<Dictionary.Node> nodes;
        for (int h = 0; h <= maxHeight; h++) {
            nodes = partition.get(h);
            if (h > 0)
                nodes.forEach(node -> {
                    for (int i = 0; i < node.subNodes.length; i++) {
                        Dictionary.Node subNode = node.subNodes[i];
                        if (subNode != null && mergeMap.containsKey(subNode))
                            node.subNodes[i] = isTree
                                    ? mergeMap.remove(subNode)
                                    : mergeMap.get(subNode);
                    }
                });
//            Comparator<Dictionary.Node> nodeComparator = createLabelingNodeComparator(ids);
            Comparator<Dictionary.Node> nodeComparator = createNodeComparator(ids);

            if (parallel) {
                Dictionary.Node[] nodesArray = nodes.toArray(new Dictionary.Node[0]);
                Arrays.parallelSort(nodesArray, nodeComparator);
                for (int i = 0; i < nodesArray.length; i++)
                    nodes.set(i, nodesArray[i]);
            } else
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

    private static Comparator<Dictionary.Node> createNodeComparator(HashMap<Dictionary.Node, Integer> ids) {
        return (nodeA, nodeB) -> {
            int isFinalCompare = Boolean.compare(nodeA.isFinal, nodeB.isFinal);
            if (isFinalCompare != 0)
                return isFinalCompare;
            int subNodesCountDiff = 0;
            for (int i = 0; i < nodeA.subNodes.length; i++) {
                Dictionary.Node subNodeA = nodeA.subNodes[i];
                Dictionary.Node subNodeB = nodeB.subNodes[i];
                if (subNodeA == null) {
                    if (subNodeB != null)
                        return -1;
                } else if (subNodeB == null)
                    return 1;
            }
            for (int i = 0; i < nodeA.subNodes.length; i++) {
                if (nodeA.subNodes[i] == null)
                    continue;
                int mergedCompare = Integer.compare(ids.get(nodeA.subNodes[i]),
                        ids.get(nodeB.subNodes[i]));
                if (mergedCompare != 0)
                    return mergedCompare;
            }
            return 0;
        };
    }

    static class NodeLabelel {

        private final HashMap<Dictionary.Node, Integer> ids;

        public NodeLabelel(HashMap<Dictionary.Node, Integer> ids) {
            this.ids = ids;
        }

        public int[] labelNode(Dictionary.Node node) {
            int[] label = new int[node.subNodes.length + 1];
            int subNodesCount = 0;
            for (Dictionary.Node subNode : node.subNodes)
                if (subNode != null)
                    subNodesCount++;
            if (node.isFinal)
                subNodesCount *= -1;
            label[0] = subNodesCount;
            for (int i = 1; i < label.length; i++) {
                Dictionary.Node subNode = node.subNodes[i - 1];
                label[i] = subNode == null ? -1 : ids.get(subNode);
            }
            return label;
        }
    }

    private static Comparator<Dictionary.Node> createLabelingNodeComparator(HashMap<Dictionary.Node, Integer> ids) {
        HashMap<Dictionary.Node, int[]> labels = new HashMap<>();
        NodeLabelel labelel = new NodeLabelel(ids);
        return (nodeA, nodeB) -> {
            int[] labelA = labels.get(nodeA);
            int[] labelB = labels.get(nodeB);
            if (labelA == null) {
                labelA = labelel.labelNode(nodeA);
                labels.put(nodeA, labelA);
            }
            if (labelB == null) {
                labelB = labelel.labelNode(nodeB);
                labels.put(nodeB, labelB);
            }
            for (int i = 0; i < labelA.length; i++) {
                int cmp = Integer.compare(labelA[i], labelB[i]);
                if (cmp != 0)
                    return cmp;
            }
            return 0;
        };
    }
}
