package Stats;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class TextTokenizer2 {
    private Set<String> stopWords;
    String doctext;
    int index;
    public TextTokenizer2(String doctext) {

        stopWords = new HashSet<String>();
        this.doctext=doctext;
        index=0;
    }
    public void initialize(){
    	String filepath="C:/Users/john/Desktop/longStopWordList.txt";
        File stopWordListFile = new File(filepath);
        loadStopWordList(stopWordListFile);
    }
    

    String tokenize() {
    	StringBuilder token = new StringBuilder("");
    	
    	while(true){
    		char c=Character.toLowerCase(doctext.charAt(index));
    		if(isValid(c)){
    			token.append(c);
    			index++;
    		}else{
    			break;
    		}
    		
    	}    	
    	return token.toString();
    }
    public boolean hasNext(){
    	return index<doctext.length();
    }
    public boolean isValid(char c){
    	return Character.isAlphabetic(c)||Character.isDigit(c);
    }
    public String next(){
    	String out= tokenize();
    	index++;
    	return out;
    }

    public void loadStopWordList(File stopWordListFile) {
        try {
            Scanner sc = new Scanner(stopWordListFile);

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                stopWords.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
