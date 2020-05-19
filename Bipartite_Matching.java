import java.io.*;
import java.util.*;

class Edge
{
	public final long cap;
	public long flow;

	public Edge(long _cap){
		cap = _cap;
		flow = 0;
	}
}

class Flow
{
	private int n;
	private List<Map<Integer,Edge>> edges;

	public Flow(int _n, List<Map<Integer,Edge>> _edges){
		n = _n;
		edges = _edges;
	}

	public void run_algo(int s, int t){
		int[] parent = new int[n];

		while(true)
		{
			for(int i = 0; i < n; ++i) parent[i] = -1;

			Queue<Integer> q = new LinkedList<>();

			parent[s] = 0;
			q.add(s);

			while(q.size() > 0){
				int curr = q.remove();
				if(curr == t) break;

				for(Map.Entry<Integer,Edge> entry: edges.get(curr).entrySet()){
					int ng = entry.getKey();
					Edge e = entry.getValue();
					if(parent[ng] != -1 || e.cap - e.flow == 0) continue;

					// this node is valid to take
					parent[ng] = curr;
					q.add(ng);
				}
			}

			if(parent[t] == -1) break;

			long min_f = 10000000000L;

			for(int par = t; par != s; par = parent[par]){
				int ppar = parent[par];
				min_f = Math.min(min_f, edges.get(ppar).get(par).cap - edges.get(ppar).get(par).flow);
			}

			for(int par = t; par != s; par = parent[par]){
				int ppar = parent[par];
				edges.get(ppar).get(par).flow += min_f;
				edges.get(par).get(ppar).flow -= min_f;
			}
		}
	}
}

class Bipartite_Matching
{
	private static int n;

	private static int[] dx = { 0, -1, 1, 0};
	private static int[] dy = {-1,  0, 0, 1};

	static int id(int x, int y) { return (x * n) + y - 1; }

	public static void main(String[] args) throws IOException{
		BufferedReader in;
		BufferedWriter out;
		try{
			in = new BufferedReader(new FileReader("input.txt"));
			out = new BufferedWriter(new FileWriter("output.txt"));
		}
		catch(Exception e){
			System.err.println("File Error!");
			return;
		}

		n = Integer.parseInt(in.readLine());

		if(n < 1){
			out.write("0");
			out.newLine();
			in.close();
			out.close();
			return;
		}

		if(n > 1000){
			System.err.println("n is too large, this program is incapable of handling it!");
			in.close();
			out.close();
			return;
		}

		int[][] grid = new int[n+1][n+1];

		for(int i = 1; i <= n; ++i){
			String s = in.readLine();
			for(int j = 1; j <= n; ++j)
				grid[i][j] = s.charAt(j-1) == '1' ? 1 : 0;
		}

		in.close();

		// s,t
		final int s = 0, t = (n + 1) * (n + 1);

		// no of elements in set A,B
		int blacks = 0, whites = 0;

		// E(G) : Contains <cap,flow>
		List<Map<Integer,Edge>> edges = new ArrayList<>();
		for(int i = 0; i < t + 1; ++i) edges.add(new HashMap<>());

		// create edges bw the 2 sets
		for(int i = 1; i <= n; ++i){
			for(int j = 1; j <= n; ++j){
				if(grid[i][j] == 0)
					continue;

				if((i + j) % 2 == 0)
				{
					++blacks;
					for(int m = 0; m < 4; ++m)
					{
						int nx = i + dx[m], ny = j + dy[m];
						if(1 <= nx && nx <= n && 1 <= ny && ny <= n
						     && grid[nx][ny] == 1){
							edges.get(id(i,j)).put(id(nx,ny), new Edge(10000000000L)); // original
							edges.get(id(nx,ny)).put(id(i,j), new Edge(0)); // residual
						}
					}
				}
				else{
					++whites;
				}
			}
		}

		for(int i = 1; i <= n; ++i)
			for(int j = 1; j <= n; ++j){
				if((i + j) % 2 == 0){
					edges.get(s).put(id(i,j), new Edge(1)); // original
					edges.get(id(i,j)).put(s, new Edge(0)); // residual
				}
				else{
					edges.get(id(i,j)).put(t, new Edge(1)); // original
					edges.get(t).put(id(i,j), new Edge(0)); // residual
				}
			}

		Flow flow = new Flow(t + 1, edges);
		flow.run_algo(s,t);

		long flow_amt = 0;
		for(int i = 0; i < t + 1; ++i)
			if(edges.get(i).containsKey(t))
				flow_amt += edges.get(i).get(t).flow;

		if(flow_amt != blacks || flow_amt != whites){
			out.write("0");
			out.newLine();
			out.close();
			return;
		}

		// OUTPUT ANSWERS

		out.write("1");
		out.newLine();

		for(int i = 1; i <= n; ++i)
			for(int j = 1; j <= n; ++j)
				if((i + j) % 2 == 0)
					for(int m = 0; m < 4; ++m)
					{
						int nx = i + dx[m], ny = j + dy[m];
						if(1 <= nx && nx <= n && 1 <= ny && ny <= n
						     && edges.get(id(i,j)).containsKey(id(nx,ny))
						     && edges.get(id(i,j)).get(id(nx,ny)).flow == 1){
							out.write("(" + i + "," + j + ")(" + nx + "," + ny + ")");
							out.newLine();

							// for sanity check later on
							grid[i][j] -= 1;
							grid[nx][ny] -= 1;
						}
					}

		out.close();


		// SANITY CHECK
		for(int i = 1; i <= n; ++i)
			for(int j = 1; j <= n; ++j)
				if(grid[i][j] != 0)
					System.err.println("F! ==> (" + i + "," + j + ")");
	}
}