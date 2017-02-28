package domain;

/**
 * Created by Yue on 2/27/17.
 */
public class TermId {
    String term;
    int termId;

    public TermId() {
    }

    public TermId(String term, int termId) {
        this.term = term;
        this.termId = termId;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public int getTermId() {
        return termId;
    }

    public void setTermId(int termId) {
        this.termId = termId;
    }
}
