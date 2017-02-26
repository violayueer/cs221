package Indexing;

import Stats.TextTokenizer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Yue on 2/26/17.
 */
public class IndexBuilder {

    public void initialize() {
        String filepath = new File("").getAbsolutePath();

        filepath = filepath.concat("/src/main/java/WEBPAGES_RAW/");

        String bookkeepingFilePath = filepath.concat("bookkeeping.tsv");

        TextTokenizer textTokenizer = new TextTokenizer();

        try {
            // read documents according to bookkeeping.tsv
            Scanner scanner = new Scanner(new FileReader(bookkeepingFilePath));

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                String fileNamePath = line.split("\\t")[0];
                fileNamePath = filepath + fileNamePath;

                //read contents from document
                String contents = new String(Files.readAllBytes(Paths.get(fileNamePath)), StandardCharsets.UTF_8);

                //using jsoup to remove tags from html
                Document doc = Jsoup.parse(contents);

                List<String> tokenList = textTokenizer.tokenize(doc.text());

            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buildInvertedIndex() {

    }

    public static void main(String[] args) {
        IndexBuilder id = new IndexBuilder();
        id.initialize();
    }
}
