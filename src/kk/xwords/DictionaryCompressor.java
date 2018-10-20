package kk.xwords;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;


public class DictionaryCompressor {
        
    static class PartitioningTraverser {

        HashMap<Integer, ArrayList<CompressorNode>> partitionMap;

        private int partition(CompressorNode node) {
            int maxSubHeight = 0;

            for (CompressorNode subNode : node) {
                if (subNode == null)
                    continue;
                maxSubHeight = Math.max(maxSubHeight, partition(subNode));
            }
            partitionMap.putIfAbsent(maxSubHeight, new ArrayList<>());
            partitionMap.get(maxSubHeight).add(node);
            return maxSubHeight + 1;
        }

        PartitioningTraverser(HashMap<Integer, ArrayList<CompressorNode>> partitionMap) {
            this.partitionMap = partitionMap;
        }

        private static HashMap<Integer, ArrayList<CompressorNode>> startPartition(CompressorNode node) {
            final HashMap<Integer, ArrayList<CompressorNode>> res = new HashMap<>();
            new PartitioningTraverser(res).partition(node);
            return res;
        }
    }

    private static HashMap<Integer, ArrayList<CompressorNode>> partition(CompressorNode node) {
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

    void compress(CompressorNode root, boolean isTree, boolean parallel) {
        HashMap<Integer, ArrayList<CompressorNode>> partition = partition(root);

        int maxHeight = Collections.max(partition.keySet());
        ArrayList<CompressorNode> nodes;
        DictionaryUtils.indexNodes(root, (no, idx) -> ((CompressorNode)no).id = idx);
        for (int h = 0; h <= maxHeight; h++) {
            nodes = partition.get(h);
            if (h > 0)
                nodes.forEach(node -> {
                    for (int i = 0; i < node.alphabet.length(); i++) {
                        CompressorNode subnode = node.getSubnode(i);
                        if (subnode != null && subnode.mergedTo != null)
                            node.setSubnode(i, subnode.mergedTo);
                    }
                });
            
            if (parallel) {
                CompressorNode[] nodesArray = nodes.toArray(new CompressorNode[0]);
                Arrays.parallelSort(nodesArray);
                for (int i = 0; i < nodesArray.length; i++)
                    nodes.set(i, nodesArray[i]);
            } else
                nodes.sort((a, b) -> a.compareTo(b));
            CompressorNode first = nodes.get(0);
            for (int i = 1; i < nodes.size(); i++) {
                CompressorNode node = nodes.get(i);
                if (node.compareTo(first) == 0)
                    node.mergedTo =  first;
                else
                    first = node;
            }
        }
    }
}
