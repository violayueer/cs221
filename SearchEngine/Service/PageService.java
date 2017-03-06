package SearchEngine.Service;

import SearchEngine.Dao.PageDao;
import Stats.TextTokenizer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by Yue on 3/5/17.
 */
public class PageService {
    private TextTokenizer textTokenizer;

    public PageService(TextTokenizer textTokenizer) {
        this.textTokenizer = textTokenizer;
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



        return pageList;
    }
}
