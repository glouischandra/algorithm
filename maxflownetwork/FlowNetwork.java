public class FlowNetwork{
    private static final String NEWLINE = System.getProperty("line.separator");
    
    private final int V;
    private int E;
    private Bag<FlowEdge>[] adj;
    
    public FlowNetwork(int V){
        this.V = V;
        adj = (Bag<FlowEdge>[]) new Bag[V];
        for (int v = 0; v < V; v++){
            adj[v] = new Bag<FlowEdge>();
        }
    }
    
    public void addEdge(FlowEdge e){
        int v = e.from();
        int w = e.to();
        adj[v].add(e);     //Add forward edge
        adj[w].add(e);     //Add backward edge
        E++;
    }
    
    public Iterable<FlowEdge> adj(int v){
        return adj[v];
    }
    
    public int V(){
        return V;
    }
    
    public int E(){
        return E;
    }
    
    public Iterable<FlowEdge> edges(){
        Bag<FlowEdge> list = new Bag<FlowEdge>();
        for(int v = 0; v < V; v++){
            for(FlowEdge e : adj(v)){
                if(e.to() != v){
                    list.add(e);
                }
            }
        }
        
        return list;
    }
    
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(V + " " + E + NEWLINE);
        for (int v = 0; v < V; v++) {
            s.append(v + ":  ");
            for (FlowEdge e : adj[v]) {
                if (e.to() != v) s.append(e + "  ");
            }
            s.append(NEWLINE);
        }
        return s.toString();
    }
}