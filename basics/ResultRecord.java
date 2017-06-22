package basics;

/**
 * Created by alexm on 25-May-17.
 */
public class ResultRecord {

    private int docID;
    private int hitCount;
    private String docURL;

    public ResultRecord(int docID, int hitCount, String docURL) {
        this.docID = docID;
        this.hitCount = hitCount;
        this.docURL = docURL;
    }

    public int getDocID() {
        return docID;
    }

    public void setDocID(int docID) {
        this.docID = docID;
    }

    public int getHitCount() {
        return hitCount;
    }

    public void setHitCount(int hitCount) {
        this.hitCount = hitCount;
    }

    public String getDocURL() {
        return docURL;
    }

    public void setDocURL(String docURL) {
        this.docURL = docURL;
    }
}
