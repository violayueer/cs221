package Indexing;

import Stats.ReduceFrequencyCount;
import Stats.TextTokenizer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Yue on 2/26/17.
 */
public class IndexBuilder {
    private static String baseFilepath;

    static {
        try {
            baseFilepath = new File(".").getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Integer> urlIdMap = new HashMap<String, Integer>();

    //url-title map
    private Map<Integer,String> urlTitleMap = new HashMap<Integer, String>();
    
    //url-path map
    private Map<String,String> urlPathMap = new HashMap<String,String>();
    
    //final-url map
    private Map<Integer,String> finalUrlMap=new HashMap<Integer,String>();
    
    //temporary domain map
    private Map<String,Integer> tempDomainMap=new HashMap<String,Integer>();
    
    //outpage map
    private Map<Integer,HashSet<Integer>> outMap=new HashMap<Integer,HashSet<Integer>>();
    
    //inpage map
     private Map<Integer,HashSet<Integer>> inMap=new HashMap<Integer,HashSet<Integer>>();
    
    // term - termId map
    private Map<String, Integer> termtoTermIdMap = new HashMap<String, Integer>();
    //private Map<Integer, String> termIdtoTermMap = new HashMap<Integer, String>();

    //TFIDF maps
    private TreeMap<Integer, Map<Integer, List<Integer>>> TFMap = new TreeMap<Integer, Map<Integer, List<Integer>>>();
    private Map<String, Integer> DFMap = new HashMap<String, Integer>();
    private TreeMap<Integer, Map<Integer, Double>> TFIDFMap = new TreeMap<Integer, Map<Integer, Double>>();

    private TextTokenizer textTokenizer;
    private ReduceFrequencyCount reduceFrequencyCount;

    private int countSucc = 0;
    private int blockNum = 0;
    private int TFMapMemoUsage = 0;

    private int termIdCount = 0;
    private String filepath;
    
    public IndexBuilder() {
        textTokenizer = new TextTokenizer();
        reduceFrequencyCount = new ReduceFrequencyCount();
        
    }

    public void initialize() {
        //String filepath = new File("").getAbsolutePath();
        filepath = baseFilepath.concat("/WEBPAGES_RAW/");
        
        String bookkeepingFilePath = filepath.concat("bookkeeping.tsv");

        try {
            // read documents according to bookkeeping.tsv
            Scanner scanner = new Scanner(new FileReader(bookkeepingFilePath));

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] strs = line.split("\\t");

                String fileNamePath = strs[0];

                //build a map that maps url with document id(increase by 1)
                String url = strs[1];
                buildUrlIdMap(url);
                buildUrlPathMap(url,fileNamePath);
                }
            }catch (IOException e) {
                 e.printStackTrace();
            }
    }
      
        public void buildUrlPathMap(String url,String path){
        	if(!urlPathMap.containsKey(url)){
        		urlPathMap.put(url, path);
        	}
        }
          public void readData(){
        	int k=0;
        	try{
              	for(String url:urlIdMap.keySet()){
              		k++;
              		String fileNamePath=urlPathMap.get(url);
              		fileNamePath=filepath + fileNamePath;
              		String contents = new String(Files.readAllBytes(Paths.get(fileNamePath)), StandardCharsets.UTF_8);        
              		buildFinalUrlMap(url); 	
                if (8 * (int) ((((contents.length()) * 2) + 45) / 8) > 1000000) {
                    continue;
                }

                //using jsoup to remove tags from html
                try {
                	System.out.println(fileNamePath);
                    String cleanHtml = Jsoup.clean(contents, Whitelist.relaxed());

                    if (cleanHtml != null && cleanHtml.length() != 0) {
                        //System.out.println(fileNamePath + "-----" + contents);
                    	Document doc = Jsoup.parse(contents);//cleanHtml
                    	String docText = doc.text();
                    	 buildInvertedIndex(urlIdMap.get(url), docText);
                    	 buildUrlTitleMap(url,contents);
                    	 buildoutlinkMap(url,contents);
                       // calculate the TFMap size
                    //  long noBytes = MemoryUtil.deepMemoryUsageOf(TFMap);
                        // if TFMap size reach block size limit, then write block to disk
                         if (Runtime.getRuntime().freeMemory() < 1000 || k==urlIdMap.size()) {
                             writeBlockToDisk();
                         }
                    	}
                	
                }catch (Exception e) {
                    System.out.println("Illegal, skip");
                }
        	}

            }catch(Exception e){
            	e.printStackTrace();
            }
            writeUrlTitleMapToDisk();

            int roundNum = mergeBlock(0, blockNum);
            computeTFIDF(urlIdMap.size(), roundNum);

            writeTermAndIdMaptoDisk();
            writeUrlIdMapToDisk();

           
        	writeInMapToDisk();
            writeOutMapToDisk();
            

        }
                }

            }
            writeUrlTitleMapToDisk();

            int roundNum = mergeBlock(0, blockNum);
            computeTFIDF(urlIdMap.size(), roundNum);

            writeTermAndIdMaptoDisk();
            writeUrlIdMapToDisk();

            writeUrlTitleMapToDisk();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public void writeBlockToDisk() {
        try {
            String outputBlockPath = "round0_block" + blockNum + ".txt";

            File outPutBlock = new File(outputBlockPath);

            FileOutputStream fos = new FileOutputStream(outPutBlock);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            //block: 1,1,3,[1,2,3] || termId, urlId, frequency, positions
            for (Integer termId : TFMap.keySet()) {
                Map<Integer, List<Integer>> posMap = TFMap.get(termId);

                reduceFrequencyCount.writePosMaptoFile(bw, termId, posMap);
            }
            bw.close();
            blockNum++;
            TFMap.clear();
            TFMapMemoUsage = 0;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public int mergeBlock(int roundNum, int blockCount) {
        if (blockCount == 1) {
            return roundNum;
        }
        //String baseFilepath = new File("").getAbsolutePath();
        int count = 0;

        for (int i = 0; i < blockCount - 1; i += 2) {
            String filePath1 = baseFilepath + "/round" + roundNum + "_block" + i + ".txt";
            String filePath2 = baseFilepath + "/round" + roundNum + "_block" + (i + 1) + ".txt";


            String outputFilePath = baseFilepath + "/round" + (roundNum + 1) + "_block" + count + ".txt";
            count++;
            reduceFrequencyCount.reduceCounts(filePath1, filePath2, outputFilePath);

        }
        if (blockCount % 2 != 0) {
            String oldFileName = baseFilepath + "/round" + roundNum + "_block" + (blockCount - 1) + ".txt";
            String newFileName = baseFilepath + "/round" + (roundNum + 1) + "_block" + count + ".txt";
            File oldFile = new File(oldFileName);
            File newFile = new File(newFileName);
            oldFile.renameTo(newFile);
            count++;
        }
        roundNum++;
        mergeBlock(roundNum, count);
        return 0;
    }

    public void buildUrlIdMap(String url) {
        if (!urlIdMap.containsKey(url)) {
            urlIdMap.put(url, urlIdMap.size());
        }
    }
    public void buildUrlTitleMap(String url, String contents){
        /*Document doc = null;
        try {
            doc = Jsoup.connect("http://" + url).get();
        } catch (IOException e) {
            //e.printStackTrace();

            System.out.println("Illegal url: " + url + ", skip");
        }
        String title = doc.title();*/
        contents = contents.replaceAll("\\s+", " ");
        Pattern p = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(contents);

        String title = "";
        while (m.find() == true) {
            title = m.group(1);
        }
        int urlId=urlIdMap.get(url);

        if(!urlTitleMap.containsKey(urlId)){
      		urlTitleMap.put(urlId,title);
        } 
    }
public void buildFinalUrlMap(String url){
    	String domain=(url.split("/"))[0];
    	String p="http://";
    	String finalUrl=domain;
    	int urlId=urlIdMap.get(url);
    	if(tempDomainMap.containsKey(domain)){
    		int Id=tempDomainMap.get(domain);
    		finalUrl=finalUrlMap.get(Id);
    	}else{
    		 try{
			 HttpURLConnection con = (HttpURLConnection) new URL(p+domain).openConnection();
			 con.setInstanceFollowRedirects(false);
			 con.connect();	
			 finalUrl=con.getHeaderField("Location").toString().substring(p.length());			 
		 }catch(Exception e){
			 System.err.println(e);
		 }
    	}		
    	finalUrlMap.put(urlId,finalUrl);
    	tempDomainMap.put(domain, urlId);
    }
    public void buildoutlinkMap(String url,String contents){
        int urlId=urlIdMap.get(url);
        if(!outMap.containsKey(urlId)){
        	outMap.put(urlId, new HashSet<Integer>());
        }
        if(!inMap.containsKey(urlId)){
        	inMap.put(urlId, new HashSet<Integer>());
        }
        String pro="http://";
    	try{
        	  if(contents!=null&&!contents.isEmpty()){
        		  Document doc = Jsoup.parse(contents); 
        		  Elements links=doc.select("a[href]");       		  
        		  URL baseUrl = new URL(pro+url);    
        		  String finalUrl=finalUrlMap.get(urlId);
        		  for(Element link:links){
        			  URL newUrl=new URL(baseUrl,link.attr("href"));          			 
        			  String outurl=newUrl.toString().substring(pro.length());             			
        			  if(!urlIdMap.containsKey(outurl)){        				 
        				 int restLoc=outurl.indexOf('/');
        				 if(restLoc>=0){
        					 String restUrl=outurl.substring(restLoc);
        					 outurl=finalUrl+restUrl;
        				 }       			        				 
        			  }
        			  if(!outurl.equals(url)&&urlIdMap.containsKey(outurl)){
        				int Id=urlIdMap.get(outurl);
        				outMap.get(urlId).add(Id);        	       					
        				if(!inMap.containsKey(Id)){
        						inMap.put(Id, new HashSet<Integer>());
        					}
        					inMap.get(Id).add(urlId);        	     					     					       					  
        			  }        				        			       		
        		  }
        		  if(outMap.get(urlId).size()!=0){
        			  System.out.println(urlId+": "+outMap.get(urlId).size());
        		  }
        		  
        	  }	  	
          }catch(Exception e){
          	 e.printStackTrace();
          }  
    }

    public int buildInvertedIndex(int urlId, String text) {
        List<String> tokenList = textTokenizer.tokenize(text);
        //Set<String> currDocTokenSet = new HashSet<String>();

        for (int i = 0; i < tokenList.size(); i++) {
            String token = tokenList.get(i);

            if (token.equals("")) {
                continue;
            }
            int termId = 0;

            // build term-termId map
            if (!termtoTermIdMap.containsKey(token)) {
                termId = termIdCount++;
                termtoTermIdMap.put(token, termId);
                //termIdtoTermMap.put(termId, token);
            }else {
                termId = termtoTermIdMap.get(token);
            }

            if (!TFMap.containsKey(termId)) {
                Map<Integer, List<Integer>> posMap = new HashMap<Integer, List<Integer>>();
                List<Integer> posList = new ArrayList<Integer>();
                posList.add(i);
                posMap.put(urlId, posList);

                TFMap.put(termId, posMap);

                TFMapMemoUsage += 36;
                //DFMap.put(token, 1);
                //currDocTokenSet.add(token);

            }else {
                //posMap = TFMap.get(termId);

                if (TFMap.get(termId).containsKey(urlId)) {
                    TFMap.get(termId).get(urlId).add(i);
                }else {
                    List<Integer> posList = new ArrayList<Integer>();
                    posList.add(i);
                    TFMap.get(termId).put(urlId, posList);
                }
                TFMapMemoUsage += 4;

                //TFMap.put(termId, posMap);

                /*if (!currDocTokenSet.contains(token)) {
                    DFMap.put(token, DFMap.get(token) + 1);
                    currDocTokenSet.add(token);
                }*/

            }
            //posMap.clear();
        }
        return tokenList.size();
    }

    public void computeTFIDF(int urlCount, int roundNum) {
        //String baseFilepath = new File("").getAbsolutePath();
        String filePath = baseFilepath + "/round" + roundNum + "_block0.txt";
        //int count = 0;

        try {
            Scanner in =  new Scanner(new FileReader(filePath));

            while (in.hasNextLine()) {
                //System.out.println(count);
                //count++;
                String line = in.nextLine();
                String[] strs = line.split(",");

                Integer termId = Integer.parseInt(strs[0]);
                Map<Integer, List<Integer>> posMap = new HashMap<Integer, List<Integer>>();

                reduceFrequencyCount.buildPosMap(posMap, strs, 1);
                int df = posMap.size();
                Map<Integer, Double> map = new HashMap<Integer, Double>();

                for (Integer urlId : posMap.keySet()) {
                    int tf = posMap.get(urlId).size();
                    double tfidf = Math.log10((double)urlCount / (double)df) * Math.log(1 + (double)tf);

                    map.put(urlId, tfidf);
                }
                posMap.clear();
                TFIDFMap.put(termId, map);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void outputResult() {
        // InvertedIndex: termId, TF, docId, TFIDF
        try {
            FileOutputStream fos = new FileOutputStream("TFIDFMap.ser");

            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(TFIDFMap);
            oos.close();
            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*try {
            File outPutFile = new File("invertedIndex.txt");
            FileOutputStream fos = new FileOutputStream(outPutFile);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            // InvertedIndex: termId, TF, docId, TFIDF
            for (Integer termId : TFIDFMap.keySet()) {
                StringBuilder sb = new StringBuilder("");
                sb.append(termId + "," + TFIDFMap.get(termId).size() + ",");

                Map<Integer, Double> map = TFIDFMap.get(termId);
                //sb.append(", TFIDF: ");

                for (Integer urlId : map.keySet()) {
                    sb.append(urlId + "," + map.get(urlId) + ",");
                }

                bw.write(sb.toString());
                bw.newLine();
            }

            bw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }

    public void writeTermAndIdMaptoDisk() {

        try {
            FileOutputStream fos = new FileOutputStream("termToTermIdMap.ser");

            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(termtoTermIdMap);
            oos.close();
            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void writeUrlIdMapToDisk() {
        try {
            FileOutputStream fos = new FileOutputStream("urlToUrlIdMap.ser");

            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(urlIdMap);
            oos.close();
            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeUrlTitleMapToDisk() {
        try {
            FileOutputStream fos = new FileOutputStream("urlTitleMap.ser");

            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(urlTitleMap);
            oos.close();
            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void writeInMapToDisk() {
        try {
            FileOutputStream fos = new FileOutputStream("inMap.ser");
         
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(inMap);
            oos.close();
            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
}
    public void writeOutMapToDisk() {
        try {
            FileOutputStream fos = new FileOutputStream("outMap.ser");
         
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(outMap);
            oos.close();
            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
}

    public static void main(String[] args) {
        IndexBuilder id = new IndexBuilder();
        id.initialize();
        id.readData();
        id.outputResult();
        //id.computeTFIDF(10000, 0);
        //id.mergeBlock(0, 2);
    }
}
