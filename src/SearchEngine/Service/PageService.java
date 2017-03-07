package SearchEngine.Service;

import SearchEngine.Dao.PageDao;
import Stats.TextTokenizer;

import java.io.*;
import java.util.*;

/**
 * Created by Yue on 3/5/17.
 */
public class PageService {
    private static String baseFilepath = new File("").getAbsolutePath();
    private static String TFIDFMapFilePath = baseFilepath + "/TFIDFMap.ser";
    private static String termtoTermIdMapFilePath = baseFilepath + "/termToTermIdMap.ser";
    private static String urltoUrlIdMapFilePath = baseFilepath + "/urlToUrlIdMap.ser";

    // term - termId map
    private static Map<String, Integer> termtoTermIdMap = deserializeMap(termtoTermIdMapFilePath);
    private static Map<Integer, String> termIdtoTermMap = new HashMap<Integer, String>();

    // url - urlId map
    private static Map<String, Integer> urltoUrlIdMap = deserializeMap(urltoUrlIdMapFilePath);
    private static Map<Integer, String> urlIdtoUrlMap = new HashMap<>();

    // Map<termId, Map<urlId, tfidf>>
    private static Map<Integer, Map<Integer, Double>> TFIDFMap = deserializeMap(TFIDFMapFilePath);

    private TextTokenizer textTokenizer;

    public PageService() {
        this.textTokenizer = new TextTokenizer();

        //construct urltoUrlIdMap
        constructIdMap(urlIdtoUrlMap, urltoUrlIdMap);
    }

    public List<PageDao> getPageList(String query, int topK) {
        List<PageDao> pageList = new ArrayList<>();

        // store urlId, page according to higher totalScore
        Map<Integer, PageDao> pageMap = new HashMap<>();

        PriorityQueue<PageDao> pageQueue = new PriorityQueue(topK, new Comparator<PageDao>() {
            @Override
            public int compare(PageDao p1, PageDao p2) {
                double score1 = p1.getTotalScore();
                double score2 = p2.getTotalScore();
                return Double.compare(score1, score2);
            }
        });

        List<String> queryTerms = textTokenizer.tokenize(query);

        for (String queryTerm : queryTerms) {
            int queryTermId = termtoTermIdMap.get(queryTerm);
            Map<Integer, Double> TFIDFValues = TFIDFMap.get(queryTermId);

            for (Integer urlId : TFIDFValues.keySet()) {
                double tfidf = TFIDFValues.get(urlId);
                PageDao page = null;

                if (pageMap.containsKey(urlId)) {
                    page = pageMap.get(urlId);

                    // update tfidf according to new tfidf
                    page.setTfidf(page.getTfidf() + tfidf);

                }else {
                    page = new PageDao();
                    String url = urlIdtoUrlMap.get(urlId);

                    page.setUrlId(urlId);
                    page.setUrl(url);
                    page.setTfidf(tfidf);

                    pageMap.put(urlId, page);
                }
                page.computeTotalScore();
            }
        }

        // add topK pages to pageList
        for (Integer urlId : pageMap.keySet()) {
            if (pageQueue.size() == topK) {
                pageQueue.poll();
            }
            pageQueue.add(pageMap.get(urlId));

        }
        for (int i = 0; i < topK; i++) {
            pageList.add(0, pageQueue.poll());
        }
        //pageList.addAll(pageQueue);
        //Collections.reverse(pageList);
        return pageList;
    }

    public static Map deserializeMap(String mapFilePath) {
        Map map = null;
        try {
            FileInputStream fis = new FileInputStream(mapFilePath);

            ObjectInputStream ois = new ObjectInputStream(fis);

            map = (Map)ois.readObject();
            ois.close();
            fis.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return map;
    }

    public void constructIdMap(Map<Integer, String> targetMap, Map<String, Integer> sourceMap) {
        for (String s : sourceMap.keySet()) {
            targetMap.put(sourceMap.get(s), s);
        }
    }

    public static void main(String[] args) {
        PageService p = new PageService();
        List<PageDao> list = p.getPageList("machine learning", 5);

        for (PageDao pageDao : list) {
            System.out.println(pageDao.getUrl() + "," + pageDao.getTotalScore());
        }
    }


    /*public void loadUrlAndIdMapFromDisk() {
        String baseFilepath = new File("").getAbsolutePath();

        String termToTermIdFilePath = baseFilepath + "/urlToUrlIdMap.txt";

        try {
            Scanner in =  new Scanner(new FileReader(termToTermIdFilePath));

            while (in.hasNextLine()) {
                String line = in.nextLine();
                String[] strs = line.split(",");
                urltoUrlIdMap.put(strs[0], Integer.parseInt(strs[1]));
                urlIdtoUrlMap.put(Integer.parseInt(strs[1]), strs[0]);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void loadTFIDFMapFromDisk() {
        String baseFilepath = new File("").getAbsolutePath();

        // InvertedIndex: termId, TF, urlId, TFIDF
        String TFIDFMapFilePath = baseFilepath + "/invertedIndex.txt";

        try {
            Scanner in =  new Scanner(new FileReader(TFIDFMapFilePath));

            while (in.hasNextLine()) {
                String line = in.nextLine();
                String strs[] = line.split(",");

                int termId = Integer.parseInt(strs[0]);
                int TF = Integer.parseInt(strs[1]);

                int i = 2;
                while (i < strs.length) {
                    int urlId = Integer.parseInt(strs[i++]);
                    double tfidf = Double.parseDouble(strs[i++]);

                    Map<Integer, Double> map = new HashMap<>();
                    map.put(urlId, tfidf);

                    TFIDFMap.put(termId, map);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }*/
}
