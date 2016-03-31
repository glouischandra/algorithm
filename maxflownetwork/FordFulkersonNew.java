public class FordFulkersonNew{
    private boolean[] marked;   //true if s->v path in residual network
    private FlowEdge[] edgeTo;  //last edge on s->v path
    private double value;       //value of flow
    
    public FordFulkersonNew(FlowNetwork G, int s, int t) {
        value = 0;
        while(this.hasAugmentingPath(G, s, t)){
            double bottle = Double.POSITIVE_INFINITY;
            for(int v = t; v != s; v = edgeTo[v].other(v)){
                bottle = Math.min(bottle, edgeTo[v].residualCapacityTo(v));
            }
            
            for(int v = t; v != s; v = edgeTo[v].other(v)){
                edgeTo[v].addResidualFlowTo(v, bottle);
            }
            
            value += bottle;
        }
    }
    
    public boolean hasAugmentingPath(FlowNetwork G, int s, int t){
        this.edgeTo = new FlowEdge[G.V()];
        this.marked = new boolean[G.V()];
        
        Queue<Integer> q = new Queue<Integer>();
        q.enqueue(s);
        this.marked[s] = true;
        while(!q.isEmpty()){
            int v = q.dequeue();
            
            for(FlowEdge e : G.adj(v)){
                int w = e.other(v);
                if(e.residualCapacityTo(w) > 0 && !marked[w]) {    //Found path from s to w in the residual network?
                    this.edgeTo[w] = e;         //save last edge on path to w
                    this.marked[w] = true;      //mark w
                    q.enqueue(w);               //add w to the queue
                }
            }
        }
        
        return marked[t];
    }
    
    public double value(){
        return this.value;
    }
    
    public boolean inCut(int v){
        return this.marked[v];
    }
}