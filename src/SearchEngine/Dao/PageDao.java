package SearchEngine.Dao;

import java.util.List;

/**
 * Created by Yue on 3/5/17.
 */
public class PageDao {
    private int urlId;
    private String url;
    private String title;
    private double tfidf;
    private double pageRank;
    private double titleScore;
    private double totalScore;

    private List<String> snippets;

    public PageDao() {
    }

    public PageDao(int urlId, String url, String title, double tfidf, double pageRank, double titleScore, double totalScore, List<String> snippets) {
        this.urlId = urlId;
        this.url = url;
        this.title = title;
        this.tfidf = tfidf;
        this.pageRank = pageRank;
        this.titleScore = titleScore;
        this.totalScore = totalScore;
        this.snippets = snippets;
    }

    public double computeTotalScore() {
        totalScore = tfidf;
        return totalScore;
    }

    public int getUrlId() {
        return urlId;
    }

    public void setUrlId(int urlId) {
        this.urlId = urlId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getTfidf() {
        return tfidf;
    }

    public void setTfidf(double tfidf) {
        this.tfidf = tfidf;
    }

    public double getPageRank() {
        return pageRank;
    }

    public void setPageRank(double pageRank) {
        this.pageRank = pageRank;
    }

    public double getTitleScore() {
        return titleScore;
    }

    public void setTitleScore(double titleScore) {
        this.titleScore = titleScore;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(double totalScore) {
        this.totalScore = totalScore;
    }

    public List<String> getSnippets() {
        return snippets;
    }

    public void setSnippets(List<String> snippets) {
        this.snippets = snippets;
    }
}
