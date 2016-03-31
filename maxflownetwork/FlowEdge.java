public class FlowEdge{
    private final int v, w;         //from and to
    private final double capacity;  //capacity
    private double flow;            //flow
    
    public FlowEdge(int v, int w, double capacity){
        this.v = v;
        this.w = w;
        this.capacity = capacity;
    }
    
    public int from(){
        return this.v;
    }
    
    public int to(){
        return this.w;
    }
    
    public double capacity(){
        return this.capacity;
    }
    
    public double flow(){
        return this.flow;
    }
    
    public int other(int vertex){
        if(vertex == v){
            return w;
        } else if(vertex == w){
            return v;
        } else {
            throw new RuntimeException("Illegal endpoint");
        }
    }
    
    public double residualCapacityTo(int vertex){
        if(vertex == v){
            return flow;
        } else if(vertex == w){
            return capacity - flow;
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    public void addResidualFlowTo(int vertex, double delta){
        if(vertex == v){
            flow -= delta;
        } else if(vertex == w){
            flow += delta;
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    public String toString() {
        return v + "->" + w + " " + flow + "/" + capacity;
    }
}