/**
 * 
 */
package SearchEngine.Service;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import SearchEngine.Dao.PageDao;
/**
 * @author SIHAN XU 3/14/17
 *
 */
public class NDGC {
	List<Integer> rank;
	List<String> google;
	List<Integer> standardRank;
	List<Double> ndgc;
	public NDGC(){
		google=new ArrayList<String>();
		standardRank=new ArrayList<Integer>();
		rank=new ArrayList<Integer>();
		ndgc=new ArrayList<Double>();
	}
	public void initial(List<PageDao> pages,String query){
		String path="GoogleResult.txt";
		try{
			File f=new File(path);
			FileReader fr=new FileReader(f);
			Scanner scanner = new Scanner(fr);
			String pre="http://";
			int count=pages.size()-1;
			while(count>=0&&scanner.hasNextLine()){
				String q = scanner.nextLine();	
				if(!q.equals(query)){
					continue;
				}else{		
					scanner.nextLine();
					String url=scanner.nextLine().substring(pre.length());					
					google.add(url);
			        count--;
				}
			}
		}catch(Exception e){
			System.err.println(e);
		}
		
		doRank(pages);
		List<Double> googleDGC=this.calcuDGC(standardRank);
		List<Double> pageDGC=this.calcuDGC(rank);
		this.calcuNDGC(googleDGC, pageDGC);
		
	}
	void doRank(List<PageDao> pages){
		int rk=pages.size()-1;
		boolean[] visited=new boolean[pages.size()];
		for(PageDao p:pages){
			String pg=p.getUrl();
			standardRank.add(rk);
			for(int i=0;i<google.size();i++){
				String gUrls=google.get(i);
				if(!visited[i]){
					if(isEqual(gUrls,pg)){
						rank.add(google.size()-i-1);
						visited[i]=true;
						break;
					}
				}
			}			
			rk--;
			if(rank.size()!=standardRank.size()){
				rank.add(0);
			}
		}
			
	}
	boolean isEqual(String gUrls,String Urls){
		String[] g=gUrls.split("/");
		String[] p=Urls.split("/");
		int gp=0,pp=0;
		while(gp<g.length&&pp<p.length){
			String s1=g[gp];
			String s2=p[pp];
			if(!s1.equals(s2)){
				return false;
			}
			gp++;
			pp++;
		}
		return true;
	}
	List<Double> calcuDGC(List<Integer> r){
		List<Double> dgc=new ArrayList<Double>();
		dgc.add((double)r.get(0));
		for(int i=1;i<r.size();i++){
			double last=dgc.get(i-1);
			double curr=(double)r.get(i)/(Math.log((double)(i+1))/Math.log(2.0));
			dgc.add(last+curr);
		}
		return dgc;
	}
	void calcuNDGC(List<Double> google,List<Double> pages){
		for(int i=0;i<pages.size();i++){
			ndgc.add(pages.get(i)/google.get(i));
		}
	}
	
	public List<Double> getNDGCList(){
		return ndgc;
	}
	public Double getKthNDGC(){
		return ndgc.get(ndgc.size()-1);
	}

}
