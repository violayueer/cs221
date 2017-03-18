package SearchEngine.Service;

import SearchEngine.Dao.PageDao;
import Stats.TextTokenizer;


import java.io.*;
import java.util.*;

/**
 * Created by Yue on 3/5/17.
 */
public class PageService {
    private static String baseFilepath = "/Users/Yue/IdeaProjects/cs221_new";

    private static String TFIDFMapFilePath = baseFilepath + "/TFIDFMap.ser";
    private static String termtoTermIdMapFilePath = baseFilepath + "/termToTermIdMap.ser";
    private static String urltoUrlIdMapFilePath = baseFilepath + "/urlToUrlIdMap.ser";
    private static String urlTitleMapFilePath = baseFilepath + "/urlTitleMap.ser";
    private static String pageRankMapFilePath = baseFilepath + "/pageRankMap.ser";
    private static String outMapFilePath = baseFilepath + "/outMap.ser";
    private static String termFrequencyMapFilePath = baseFilepath + "/termFrequencyMap.ser";
    private static String urlDocLengthMapFilePath = baseFilepath + "/urlDocLengthMap.ser";

    private static String duplicateUrlsFilePath = baseFilepath + "/duplicateUrls.txt";

    // term - termId map
    private static Map<String, Integer> termtoTermIdMap = deserializeMap(termtoTermIdMapFilePath);
    private static Map<Integer, String> termIdtoTermMap = new HashMap<Integer, String>();

    // url - urlId map
    private static Map<String, Integer> urltoUrlIdMap = deserializeMap(urltoUrlIdMapFilePath);
    private static Map<Integer, String> urlIdtoUrlMap = new HashMap<>();

    // Map<termId, Map<urlId, tfidf>>
    private static Map<Integer, Map<Integer, Double>> TFIDFMap = deserializeMap(TFIDFMapFilePath);

    // urlId-title map
    private static Map<Integer,String> urlTitleMap = deserializeMap(urlTitleMapFilePath);

    // pageRank Map
    public static Map<Integer, Double> pageRankMap = deserializeMap(pageRankMapFilePath);

    //outpage map, used to filter redirect url
    private static Map<Integer,HashSet<Integer>> outMap = deserializeMap(outMapFilePath);

    private static Map<Integer, Integer> termFrequencyMap = deserializeMap(termFrequencyMapFilePath);
    private static Map<Integer, Integer> urlDocLengthMap = deserializeMap(urlDocLengthMapFilePath);

    private static Set<String> duplicateUrls = loadDuplicateUrls(duplicateUrlsFilePath);

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

        Set<Integer> urlIdSetWithAllQueryTerms = constructUrlIdSetWithAllQueryTerms(queryTerms);

        for (int i = 0; i < queryTerms.size(); i++) {
            String queryTerm = queryTerms.get(i);

            int queryTermId = termtoTermIdMap.get(queryTerm);
            Map<Integer, Double> TFIDFValues = TFIDFMap.get(queryTermId);

            double queryTermTFIDF = computeQueryTFIDF(queryTerm, TFIDFValues.size() + 1);

            for (Integer urlId : urlIdSetWithAllQueryTerms) {
                // filter redirected url or large url
                if (!outMap.containsKey(urlId)) {
                    continue;
                }

                if (i != 0 && !pageMap.containsKey(urlId)) {
                    continue;
                }

                double tfidf = TFIDFValues.get(urlId);
                PageDao page = null;

                if (pageMap.containsKey(urlId)) {
                    page = pageMap.get(urlId);

                    // update tfidf according to new tfidf
                    page.setTfidf(page.getTfidf() + tfidf);

                }else {
                    page = new PageDao();
                    String url = urlIdtoUrlMap.get(urlId);
                    String title = urlTitleMap.get(urlId);

                    page.setUrlId(urlId);
                    page.setUrl(url);
                    page.setTfidf(tfidf);
                    page.setTitle(title);
                    page.setPageRank(pageRankMap.get(urlId));

                    computePageUrlScore(page, queryTerms);

                    pageMap.put(urlId, page);
                }
                page.computeTitleScore(query);
                double cosineSimilarity = page.getCosineSimilarity() + queryTermTFIDF * page.getTfidf();
                page.setCosineSimilarity(cosineSimilarity);
            }
        }

        // add topK pages to pageList
        for (Integer urlId : pageMap.keySet()) {
            int docLength = urlDocLengthMap.get(urlId);

            PageDao page = pageMap.get(urlId);

            boolean checkDuplicate = false;
            for (String duplicateUrl : duplicateUrls) {
                if (page.getUrl().contains(duplicateUrl)) {
                    checkDuplicate = true;
                    break;
                }
            }
            if (checkDuplicate) {
                continue;
            }

            page.setCosineSimilarity(page.getCosineSimilarity() / (double)docLength);

            page.computeTotalScore();

            if (pageQueue.size() == topK) {
                pageQueue.poll();
            }
            pageQueue.add(page);

        }
        for (int i = 0; i < topK; i++) {
            pageList.add(0, pageQueue.poll());
        }
        //pageList.addAll(pageQueue);
        //Collections.reverse(pageList);
        return pageList;
    }

    public void computePageUrlScore(PageDao page, List<String> queryTerms) {
        String url = page.getUrl();
        String[] domains = url.split("/");
        int count = 0;
        for (String term : queryTerms) {
            for (String domain : domains) {
                if (domain.contains(term)) {
                    count++;
                    break;
                }
            }
        }
        page.setUrlScore(0.5 * (double)count / (double)domains.length);
    }

    public double computeQueryTFIDF(String queryTerm, int df) {
        int docmentSize = outMap.size();

        int queryTermId = termtoTermIdMap.get(queryTerm);
        int tf = termFrequencyMap.get(queryTermId);

        double tfidf = Math.log10((double)docmentSize / (double)df) * Math.log(1 + (double)tf);

        return tfidf;
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

    private static Set<String> loadDuplicateUrls(String filePath) {
        Set<String> set = new HashSet<>();
        try {
            File duplicateUrlFile = new File(filePath);
            Scanner sc = new Scanner(duplicateUrlFile);

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                set.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return set;
    }

    public void constructIdMap(Map<Integer, String> targetMap, Map<String, Integer> sourceMap) {
        for (String s : sourceMap.keySet()) {
            int urlId = sourceMap.get(s);

            //outMap here is used to filter redirected url
            if (outMap.containsKey(urlId)) {
                targetMap.put(sourceMap.get(s), s);
            }

        }
    }

    public Set<Integer> constructUrlIdSetWithAllQueryTerms(List<String> queryTerms) {
        Set<Integer> urlIdSet = null;

        for (int i = 0; i < queryTerms.size(); i++) {
            String queryTerm = queryTerms.get(i);
            int queryTermId = termtoTermIdMap.get(queryTerm);

            Set<Integer> tempSet = new HashSet<>();

            //Map<Integer, Double> TFIDFValues = TFIDFMap.get(queryTermId);
            Set<Integer> urlIdsInTFIDFMap = TFIDFMap.get(queryTermId).keySet();

            if (i == 0) {
                urlIdSet = new HashSet<>(urlIdsInTFIDFMap);

            }else {
                for (Integer urlId : urlIdSet) {
                    if (urlIdsInTFIDFMap.contains(urlId)) {
                        tempSet.add(urlId);
                    }
                }
                urlIdSet = tempSet;
            }

        }
        return urlIdSet;
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
