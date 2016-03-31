/**
 * Auto Generated Java Class.
 */
public class BaseballElimination {
    private int numberOfTeam;
    private String[] teamName;
    private int[] w;             //Wins
    private int[] l;             //Loses
    private int[] r;             //Games left
    private int[][] g;           //Games
    private FordFulkerson[] fulks;   //Result cache
    private Bag<String>[] certificates;
    
    private int totalNumberOfVertices;   //Vertices count in network flow
    
    private FlowNetwork network;
    
    // create a baseball division from given filename in format specified below
    public BaseballElimination(String filename){
        In in = new In(filename);
        numberOfTeam = in.readInt();
        
        //Initialize 
        teamName = new String[this.numberOfTeam];
        w = new int[this.numberOfTeam];
        l = new int[this.numberOfTeam];
        r = new int[this.numberOfTeam];
        g = new int[this.numberOfTeam][this.numberOfTeam];
        fulks = new FordFulkerson[this.numberOfTeam];
        certificates = (Bag<String>[]) new Bag[this.numberOfTeam];
        
        
        //load all points into array
        for (int i = 0; i < this.numberOfTeam; i++) {
            teamName[i] = in.readString();
            w[i] = in.readInt();
            l[i] = in.readInt();
            r[i] = in.readInt();
            
            for(int j = 0; j < this.numberOfTeam; j++){
                g[i][j] = in.readInt();
            }
        } 
        
        totalNumberOfVertices = (numberOfTeam - 1) + countPair(numberOfTeam - 1) + 3;
    }
    
    // number of teams
    public int numberOfTeams(){
        return this.numberOfTeam;
    }
    
    // all teams
    public Iterable<String> teams(){
        Bag<String> result = new Bag<String>();
        for(int i = 0; i < this.teamName.length; i++){
            result.add(this.teamName[i]);
        }
        
        return result;
    }
    
    // number of wins for given team
    public int wins(String team){
        return w[this.teamIndex(team)];
    }
    
    // number of losses for given team
    public int losses(String team){
        return l[this.teamIndex(team)];
    }
    
    // number of remaining games for given team
    public int remaining(String team){
        return r[this.teamIndex(team)];
    }
    
    // number of remaining games between team1 and team2
    public int against(String team1, String team2){
        return g[this.teamIndex(team1)][this.teamIndex(team2)];
    }
    
    // is given team eliminated?
    public boolean isEliminated(String team){
        //Find index
        int processedTeamIdx = teamIndex(team);
        
        //Trivial test
        for(int i = 0; i < numberOfTeam; i++){
            if(i != processedTeamIdx && (w[processedTeamIdx] + r[processedTeamIdx]) < w[i]){
                Bag<String> cert = new Bag<String>();
                cert.add(this.teamName[i]);
                cert.add("Trivial Elimination");
                this.certificates[processedTeamIdx] = cert;
                
                return true;
            }
        }
        
        //Create flow network
        network = new FlowNetwork(this.totalNumberOfVertices);
        
        //Assemble the flow network
        //Sink vertices number
        int start = 0;
        int end = this.totalNumberOfVertices - 1;
        
        //Vertices for each other teams
        int[] teamVertices = new int[numberOfTeam];
        
        //Add edge from each team to sink end vertex
        for(int i = 1; i <= numberOfTeam; i++){
            if((i-1) != processedTeamIdx){
                teamVertices[i-1] = i;
                //link to end sink
                int capacity = (w[processedTeamIdx] + r[processedTeamIdx]) - w[i-1];
                capacity = capacity < 0 ? 0 : capacity;
                FlowEdge edge = new FlowEdge(i, end, capacity);
                network.addEdge(edge);
            }
        }
        
        //Add edge for each games left to play
        int gameSeed = this.numberOfTeam + 1;    // Continue from numberOfTeam + 1 which is next empty vertex
        int maximumFlow = 0;
        for(int i = 0; i < this.numberOfTeam; i++){
            if(i != processedTeamIdx){      //Processed team is not playing
                for(int j = i; j < this.numberOfTeam; j++){
                    if(j != processedTeamIdx && i != j){    //Make sure that its not playing against processed team
                        maximumFlow += g[i][j];
                        FlowEdge startToGame = new FlowEdge(start, gameSeed, g[i][j]);    //Edge from start to the game
                        FlowEdge gameToTeam1 = new FlowEdge(gameSeed, teamVertices[i], Double.POSITIVE_INFINITY);   //Edge from game to team 1
                        FlowEdge gameToTeam2 = new FlowEdge(gameSeed, teamVertices[j], Double.POSITIVE_INFINITY);   //Edge from game to team 2
                            
                        network.addEdge(startToGame);
                        network.addEdge(gameToTeam1);
                        network.addEdge(gameToTeam2);
                        gameSeed++;
                    }
                }
            }
        }
        
        //Compute maxflow and mincut of network
        fulks[processedTeamIdx] = new FordFulkerson(network, start, end);
        
        //Maxflow result:
        boolean isEliminated = maximumFlow > fulks[processedTeamIdx].value();
        
        if(isEliminated){
            Bag<String> certificate = new Bag<String>();
            for(int i = 0; i < this.teamName.length; i++){
                if(i != processedTeamIdx && fulks[processedTeamIdx].inCut(teamVertices[i])){
                    certificate.add(this.teamName[i]);
                }
            }
            certificates[processedTeamIdx] = certificate;
        }
        
        return isEliminated;
    }
    
    // subset R of teams that eliminates given team; null if not eliminated
    public Iterable<String> certificateOfElimination(String team){
        //Find index
        int processedTeamIdx = teamIndex(team);
        
        //If it hasn't run yet
        if(fulks[processedTeamIdx] == null){
            this.isEliminated(team);
        }
        
        //return those that are in the mincut
        return certificates[processedTeamIdx];
    }
    
    private int countPair(int f) {
        return ((f == 1) ? 0 : (f - 1) + countPair(f - 1)); 
    }  
    
    private int teamIndex(String name){
        for(int i = 0; i < this.teamName.length; i++){
            if(this.teamName[i] == name){
                return i;
            }
        }
        return -1;
    }
    
    //test
    public static void main(String[] args) {
        //String file = args[0];
        String file = "teams42.txt";
        BaseballElimination division = new BaseballElimination(file);
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team))
                    StdOut.print(t + " ");
                StdOut.println("}");
            }
            else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}
