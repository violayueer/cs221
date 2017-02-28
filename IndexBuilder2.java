package Indexing;

import Stats.TextTokenizer;
import Stats.TextTokenizer2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.w3c.dom.Text;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class IndexBuilder2 {
	 Map<String, Integer> urlIdMap = new HashMap<String, Integer>();
	 TreeMap<String,TreeMap<Integer,Integer>> tokenDoc=new TreeMap<String,TreeMap<Integer,Integer>>();
	// Map<String,ArrayList<Integer>> tf=new TreeMap<String,ArrayList<Integer>>();
	 TextTokenizer2 textTokenizer;
//	 TextTokenizer textTokenizer1;
	 int counter;
	 public IndexBuilder2() {
		 TextTokenizer textTokenizer1=new  TextTokenizer();
	       counter=0;
	 }
	 
	 public void initialize() {
	      //  String filepath = new File("").getAbsolutePath();

	      //  filepath = filepath.concat("/src/main/java/WEBPAGES_RAW/");
	    	
	    	String filepath="C:/Users/john/Desktop/WEBPAGES_RAW/";

	        String bookkeepingFilePath = filepath.concat("bookkeeping.tsv");
	        
	        try{
	        	Scanner scanner = new Scanner(new FileReader(bookkeepingFilePath));
	        //	int counter=0;
	        	while (scanner.hasNextLine()) {
	                String line = scanner.nextLine();
	                String[] strs = line.split("\\t");

	                String fileNamePath = strs[0];

	                //build a map that maps url with document id(increase by 1)
	                 String url = strs[1];
	                 buildUrlIdMap(url);

	                fileNamePath = filepath + fileNamePath;
	                String contents = new String(Files.readAllBytes(Paths.get(fileNamePath)), StandardCharsets.UTF_8);
	                if (contents != null && contents.length() != 0) {
	                    //     System.out.println(fileNamePath + "-----" + contents);
	                         
	                 try{
	                      Document doc = Jsoup.parse(contents);
	                      String docText = doc.text();	                         
	                      buildInvertedIndex(urlIdMap.get(url), docText);
	                   //    computeTFIDF(wordNum);
	                }catch(Exception e){
	                  System.out.println("Illegal,skip");
	                 }
	                 
	               if(Runtime.getRuntime().freeMemory()<1000){
	            	   		System.out.println(Runtime.getRuntime().freeMemory());
	            	   		output();
	            	   		counter++;
	            	   		System.out.println(Runtime.getRuntime().freeMemory());
	                 }
	              
	                 
	              }
	           }
	        	if(tokenDoc.size()>0){	        		
	        		output();
	        	}
	        } catch (Exception e) {
	             e.printStackTrace();
	        }
	        

	  }
	 void output()throws Exception{
			File f=new File(new File("").getAbsolutePath()+"/"+Integer.toString(counter)+".txt");
          	 FileWriter out=new FileWriter(f,true);
          	 Iterator iter=tokenDoc.entrySet().iterator();
          	 while(tokenDoc.size()>0){
          		 Map.Entry entry=tokenDoc.pollFirstEntry(); 
         // 		 System.out.println(tokenDoc.size());
          		 String key=(String)entry.getKey();
          		 TreeMap doc=(TreeMap)entry.getValue();
          		 Iterator iter2=doc.entrySet().iterator();
          		 out.write(key+" ");
          		 while(doc.size()>0){
          			 Map.Entry entry2=doc.pollFirstEntry();
          		//	 System.out.println(doc.size());
          			 int did=(Integer)entry2.getKey();
          			 int freq=(Integer)entry2.getValue();
          			 out.write(Integer.toString(did)+","+Integer.toString(freq)+" ");
          		 }
          		 out.write("\t\n");
          		 
          	 }
          	 out.close();
	 }
	 public void buildUrlIdMap(String url) {
	   if (!urlIdMap.containsKey(url)) {
	         urlIdMap.put(url, urlIdMap.size());
	      }
	  }
	 
	void buildInvertedIndex(int docId,String text){
		textTokenizer=new TextTokenizer2(text);
	//	List<String> tokenList = textTokenizer1.tokenize(text);
	while(textTokenizer.hasNext()){	// 
			// if(textTokenizer.isValid()){
			 String token=textTokenizer.next();//	=tokenList.get(i);
			if(!token.equals("")){
				 System.out.println(token);
				 if(tokenDoc.containsKey(token)){
					 TreeMap<Integer,Integer> map=tokenDoc.get(token);
					 int freq=1;
					 if(map.containsKey(docId)){
						 freq=map.get(docId)+1;				
					 }
					 map.put(docId, freq);
				 }else{
					 TreeMap<Integer,Integer> map=new TreeMap<Integer,Integer>();
					 map.put(docId,1);
					 tokenDoc.put(token, map);
				}
			}
		//	 }else{
		//		 textTokenizer.next();
		//	 }
			
			
		}
	}
    public static void main(String[] args) {
        IndexBuilder2 ib = new IndexBuilder2();
        ib.initialize();
    }
	 	
}
