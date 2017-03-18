package SearchEngine.Dao;

import Stats.TextTokenizer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private double cosineSimilarity;
    private double urlScore;

    private List<String> snippets;

    public PageDao() {
    }

    public PageDao(int urlId, String url, String title, double tfidf, double pageRank, double titleScore, double totalScore, double cosineSimilarity, List<String> snippets) {
        this.urlId = urlId;
        this.url = url;
        this.title = title;
        this.tfidf = tfidf;
        this.pageRank = pageRank;
        this.titleScore = titleScore;
        this.totalScore = totalScore;
        this.cosineSimilarity = cosineSimilarity;
        this.snippets = snippets;
    }

    public double computeTotalScore() {
        this.totalScore = this.cosineSimilarity + Math.min(this.pageRank, 1.0) + 2.5 * this.titleScore + this.urlScore;

        return this.totalScore;
    }

    public double getCosineSimilarity() {
        return cosineSimilarity;
    }

    public void setCosineSimilarity(double cosineSimilarity) {
        this.cosineSimilarity = cosineSimilarity;
    }

    public double getUrlScore() {
        return urlScore;
    }

    public void setUrlScore(double urlScore) {
        this.urlScore = urlScore;
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

    public double computeTitleScore(String query) {
        if (query == null || query.length() == 0 || this.title == null || this.title.length() == 0) {
            return 0.0;

        }
        TextTokenizer textTokenizer = new TextTokenizer();

        List<String> titleTerms = textTokenizer.tokenize(this.title);
        List<String> queryTerms = textTokenizer.tokenize(query);

        if (titleTerms.size() == 0 || queryTerms.size() == 0) {
            this.titleScore = 0.0;

        }else {
            int intersectCount = 0;
            Set<String> titleTermSet = new HashSet<>(titleTerms);

            for (String queryTerm : queryTerms) {
                if (titleTermSet.contains(queryTerm)) {
                    intersectCount++;
                }
            }

            titleTermSet.addAll(queryTerms);
            this.titleScore = (double)intersectCount / (double)titleTermSet.size();
        }
        return this.titleScore;
    }
}
