package Stats;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

/**
 * Created by Yue on 2/26/17.
 */
public class TextTokenizer {
    private Set<String> stopWords;

    public TextTokenizer() {
        stopWords = new HashSet<String>();
    }

    public List<String> tokenize(String inputString) {
        String filepath = new File("").getAbsolutePath();
        File stopWordListFile = new File(filepath + "/src/main/java/longStopWordList.txt");
        loadStopWordList(stopWordListFile);

        List<String> tokenList = new ArrayList<String>();

        int i = 0;
        while (i < inputString.length()) {
            StringBuilder token = new StringBuilder("");

            while (i < inputString.length() && (Character.isAlphabetic(inputString.charAt(i)) ||
                    Character.isDigit(inputString.charAt(i)))) {

                token.append(Character.toLowerCase(inputString.charAt(i)));
                i++;
            }
            if (token.length() > 0 && !stopWords.contains(token)) {
                tokenList.add(token.toString());
            }
            i++;
        }
        return tokenList;
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
