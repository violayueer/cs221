package Indexing;

import Stats.TextTokenizer;
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

/**
 * Created by Yue on 2/26/17.
 */
public class IndexBuilder {
    private Map<String, Integer> urlIdMap = new HashMap<String, Integer>();

    //TFIDF maps
    private Map<String, Map<Integer, List<Integer>>> TFMap = new HashMap<String, Map<Integer, List<Integer>>>();
    private Map<String, Integer> DFMap = new HashMap<String, Integer>();
    private Map<String, Map<Integer, Double>> TFIDFMap = new HashMap<String, Map<Integer, Double>>();

    private TextTokenizer textTokenizer;

    public IndexBuilder() {
        textTokenizer = new TextTokenizer();
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
                //contents = contents.trim();

                //using jsoup to remove tags from html
                //String cleanHtml = Jsoup.clean(contents, Whitelist.relaxed());
                if (contents != null && contents.length() != 0) {
                    System.out.println(fileNamePath + "-----" + contents);

                    Document doc = Jsoup.parse(contents);
                    String docText = doc.text();

                    int wordNum = buildInvertedIndex(urlIdMap.get(url), docText);
                    computeTFIDF(wordNum);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
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

            Map<Integer, List<Integer>> posMap = null;

            if (!TFMap.containsKey(token)) {
                posMap = new HashMap<Integer, List<Integer>>();
                posMap.put(urlId, new ArrayList<Integer>(i));

                TFMap.put(token, posMap);
                //DFMap.put(token, 1);

                //currDocTokenSet.add(token);

            }else {
                posMap = TFMap.get(token);

                if (posMap.containsKey(urlId)) {
                    posMap.get(urlId).add(i);
                }else {
                    posMap.put(urlId, new ArrayList<Integer>(i));
                }

                TFMap.put(token, posMap);

                /*if (!currDocTokenSet.contains(token)) {
                    DFMap.put(token, DFMap.get(token) + 1);
                    currDocTokenSet.add(token);
                }*/

            }
        }
        return tokenList.size();
    }

    public void computeTFIDF(int wordNum) {
        for (String token : TFMap.keySet()) {
            Map<Integer, List<Integer>> posMap = TFMap.get(token);

            int df = TFMap.get(token).size();
            Map<Integer, Double> map = new HashMap<Integer, Double>();

            for (Integer urlId : posMap.keySet()) {
                int tf = posMap.get(urlId).size();
                double tfidf = Math.log10((double)wordNum / (double)df) * Math.log(1 + (double)tf);

                map.put(urlId, tfidf);
            }

            TFIDFMap.put(token, map);
        }
    }

    public void outputResult() {

        try {
            File outPutFile = new File("invertedIndex.txt");
            FileOutputStream fos = new FileOutputStream(outPutFile);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            for (String token : TFIDFMap.keySet()) {
                StringBuilder sb = new StringBuilder(token);
                sb.append(" ,TF: " + TFMap.get(token).size());

                Map<Integer, Double> map = TFIDFMap.get(token);
                sb.append(" ,TFIDF: ");

                for (Integer urlId : map.keySet()) {
                    sb.append("in " + urlId + " is: " + map.get(urlId));
                }

                bw.write(sb.toString());
                bw.newLine();
            }

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
    }
}
