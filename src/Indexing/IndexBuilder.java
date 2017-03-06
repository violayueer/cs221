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

/**
 * Created by Yue on 2/26/17.
 */
public class IndexBuilder {
    private Map<String, Integer> urlIdMap = new HashMap<String, Integer>();

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

    public IndexBuilder() {
        textTokenizer = new TextTokenizer();
        reduceFrequencyCount = new ReduceFrequencyCount();
    }

    public void initialize() {
        String filepath = new File("").getAbsolutePath();
        filepath = filepath.concat("/src/main/java/WEBPAGES_RAW/");

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

                fileNamePath = filepath + fileNamePath;

                //read contents from document
                String contents = new String(Files.readAllBytes(Paths.get(fileNamePath)), StandardCharsets.UTF_8);
                //filter large size file
                if (8 * (int) ((((contents.length()) * 2) + 45) / 8) > 1000000) {
                    continue;
                }

                //using jsoup to remove tags from html
                try {
                    //System.out.println(fileNamePath);
                    String cleanHtml = Jsoup.clean(contents, Whitelist.relaxed());

                    if (cleanHtml != null && cleanHtml.length() != 0) {
                        //System.out.println(fileNamePath + "-----" + contents);
                        Document doc = Jsoup.parse(cleanHtml);
                        String docText = doc.text();

                        buildInvertedIndex(urlIdMap.get(url), docText);

                        // calculate the TFMap size
                        //long noBytes = MemoryUtil.deepMemoryUsageOf(TFMap);
                        // if TFMap size reach block size limit, then write block to disk
                        if (Runtime.getRuntime().freeMemory() < 1000 || !scanner.hasNextLine()) {
                            writeBlockToDisk();
                        }
                    }
                }catch (Exception e) {
                    System.out.println("Illegal, skip");
                }

            }
            int roundNum = mergeBlock(0, blockNum);
            computeTFIDF(urlIdMap.size(), roundNum);

            writeTermAndIdMaptoDisk();
            writeUrlIdMapToDisk();

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
        String baseFilepath = new File("").getAbsolutePath();
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
        String baseFilepath = new File("").getAbsolutePath();
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

        try {
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
        }

    }

    public void writeTermAndIdMaptoDisk() {
        String termToTermIdFilePath = "termToTermIdMap.txt";

        try {
            File termToTermIdFile = new File(termToTermIdFilePath);
            FileOutputStream fos = new FileOutputStream(termToTermIdFile);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            for (String term : termtoTermIdMap.keySet()) {
                String s = term + "," + termtoTermIdMap.get(term);
                bw.write(s);
                bw.newLine();
            }
            bw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void writeUrlIdMapToDisk() {
        String urlToUrlIdFilePath = "urlToUrlIdMap.txt";

        try {
            File urlToUrlIdFile = new File(urlToUrlIdFilePath);
            FileOutputStream fos = new FileOutputStream(urlToUrlIdFile);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            for (String url : urlIdMap.keySet()) {
                String s = url + "," + urlIdMap.get(url);
                bw.write(s);
                bw.newLine();
            }
            bw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        IndexBuilder id = new IndexBuilder();
        id.initialize();
        id.outputResult();
        //id.computeTFIDF(10000, 0);
        //id.mergeBlock(0, 2);
    }
}
