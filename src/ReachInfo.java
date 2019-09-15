public class ReachInfo implements Comparable<ReachInfo>{
    
    final int id;
    final int hopCount;
    int nextHop;
    
    public ReachInfo(int id, int hopCount, int nextHop) {
        this.id = id;
        this.hopCount = hopCount;
        this.nextHop = nextHop;
    }

    @Override
    public int compareTo(ReachInfo o) {
        return ((Integer)hopCount).compareTo((Integer)o.hopCount);
    }
}
