package kk.xwords;

import java.util.*;


public class DictionaryCompressor {

    private int partition(CompressorNode node, HashMap<Integer, ArrayList<CompressorNode>> res) {
        int maxSubHeight = 0;

        for (CompressorNode subnode : node) {
            if (subnode == null)
                continue;
            maxSubHeight = Math.max(maxSubHeight, partition(subnode, res));
        }

        ArrayList<CompressorNode> nodes = res.get(maxSubHeight);
        if (nodes == null)
            res.put(maxSubHeight, new ArrayList<>(List.of(node)));
        else
            nodes.add(node);
        return maxSubHeight + 1;
    }

    private HashMap<Integer, ArrayList<CompressorNode>> partition(CompressorNode node) {
        HashMap<Integer, ArrayList<CompressorNode>> partitionMap = new HashMap<>();
        partition(node, partitionMap);
        return partitionMap;
    }

    void compress(CompressorNode root, boolean parallel) {
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
                nodes.sort(CompressorNode::compareTo);
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
