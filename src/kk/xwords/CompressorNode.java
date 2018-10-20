package kk.xwords;

class CompressorNode extends ArrayBasedNode<CompressorNode> implements Comparable<CompressorNode> {

    int id = -1;
    CompressorNode mergedTo = null;

    public CompressorNode(Alphabet alphabet) {
        super(alphabet);
    }

    @Override
    public int compareTo(CompressorNode nodeB) {
        CompressorNode nodeA = this;
        int isFinalCompare = Boolean.compare(nodeA.isFinal, nodeB.isFinal);
        if (isFinalCompare != 0)
            return isFinalCompare;
        int edgesCompare = Integer.compare(nodeA.edges, nodeB.edges);
        if (edgesCompare != 0)
            return edgesCompare;
        int maxSubnodes = alphabet.length();
        for (int i = 0; i < maxSubnodes; i++) {
            CompressorNode subNodeA = nodeA.getSubnode(i);
            CompressorNode subNodeB = nodeB.getSubnode(i);
            if (subNodeA == null) {
                if (subNodeB != null)
                    return -1;
            } else if (subNodeB == null)
                return 1;
        }
        for (int i = 0; i < maxSubnodes; i++) {
            if (nodeA.getSubnode(i) == null)
                continue;
            int idCompare = Integer.compare(nodeA.getSubnode(i).id, nodeB.getSubnode(i).id);
            if (idCompare != 0)
                return idCompare;
        }
        return 0;
    }    
}
