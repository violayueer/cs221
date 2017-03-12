package Indexing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by Yue on 2/26/17.
 */
public class Test {

    public void test() {
        String filepath = null;
        try {
            filepath = new File(".").getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        filepath = filepath.concat("/src/main/java/WEBPAGES_RAW/");
        String fileNamePath = "/Users/Yue/Downloads/view-source_www.ics.uci.edu.txt";

        try {
            String contents = new String(Files.readAllBytes(Paths.get(fileNamePath)), StandardCharsets.UTF_8);
            Document doc = Jsoup.parse(contents);
            String docText = doc.text();

            System.out.println(docText);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //Test t = new Test();
        //t.test();
        try {
            System.out.println(new File(".").getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
