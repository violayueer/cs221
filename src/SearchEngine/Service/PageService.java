package SearchEngine.Service;

import SearchEngine.Dao.PageDao;
import Stats.TextTokenizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

/**
 * Created by Yue on 3/5/17.
 */
public class PageService {
    private TextTokenizer textTokenizer;

    // term - termId map
    private Map<String, Integer> termtoTermIdMap = new HashMap<String, Integer>();
    private Map<Integer, String> termIdtoTermMap = new HashMap<Integer, String>();

    // url - urlId map
    private Map<String, Integer> urltoUrlIdMap = new HashMap<String, Integer>();
    private Map<Integer, String> urlIdtoUrlMap = new HashMap<>();

    // Map<termId, Map<urlId, tfidf>>
    private Map<Integer, Map<Integer, Double>> TFIDFMap = new HashMap<Integer, Map<Integer, Double>>();

    public PageService(TextTokenizer textTokenizer) {
        this.textTokenizer = textTokenizer;
        loadTermAndIdMapFromDisk();
        loadTFIDFMapFromDisk();
        loadUrlAndIdMapFromDisk();
    }

    public List<PageDao> getPageList(String query, int topK) {
        List<PageDao> pageList = new ArrayList<>();

        // store urlId, page according to higher totalScore
        TreeMap<Integer, PageDao> pageMap = new TreeMap(new Comparator<PageDao>() {
            @Override
            public int compare(PageDao p1, PageDao p2) {
                double score1 = p1.getTotalScore();
                double score2 = p2.getTotalScore();
                return -1 * Double.compare(score1, score2);
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
            if (pageList.size() == topK) {
                break;
            }
            pageList.add(pageMap.get(urlId));
        }

        return pageList;
    }

    public void loadTermAndIdMapFromDisk() {
        String baseFilepath = new File("").getAbsolutePath();

        String termToTermIdFilePath = baseFilepath + "/termToTermIdMap.txt";

        try {
            Scanner in =  new Scanner(new FileReader(termToTermIdFilePath));

            while (in.hasNextLine()) {
                String line = in.nextLine();
                String[] strs = line.split(",");
                termtoTermIdMap.put(strs[0], Integer.parseInt(strs[1]));
                termIdtoTermMap.put(Integer.parseInt(strs[1]), strs[0]);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void loadUrlAndIdMapFromDisk() {
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
    }
}
