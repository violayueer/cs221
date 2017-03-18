package Searching;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lixiang on 3/16/2017.
 */
public class GoogleResult {

    public static void main(String[] args) throws Exception {

        List<String>  queries =new ArrayList<>();

        String key0="mondego";
        String key1="machine+learning";
        String key2="software+engineering";
        String key3="security";
        String key4="student+affairs";
        String key5="graduate+courses";
        String key6="Crista+Lopes";
        String key7="REST";
        String key8="computer+games";
        String key9="information+retrieval";

        queries.add(key0);
        queries.add(key1);
        queries.add(key2);
        queries.add(key3);
        queries.add(key4);
        queries.add(key5);
        queries.add(key6);
        queries.add(key7);
        queries.add(key8);
        queries.add(key9);

        // write the google results in txt file
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("GoogleResult.txt"), "utf-8"));

        // search for google results, use jsoup to parse the results and get the title & url
        for(String query : queries) {
            String request = "https://www.google.com/search?q=site:ics.uci.edu+" + query + "&num=5";
            System.out.println("Search for:" + query);

            Document doc = Jsoup.connect(request).userAgent("Mozilla/5.0").get();
            Elements results = doc.select("h3.r > a");
            writer.write(query + ":" + System.getProperty("line.separator"));
            for (Element result : results) {
                String linkHref = result.attr("href");
                String linkText = result.text();
                System.out.println("Text::" + linkText + ", URL:" + linkHref.substring(7, linkHref.indexOf("&")));
                writer.write( linkHref.substring(7, linkHref.indexOf("&")) + System.getProperty("line.separator"));
            }
            writer.write(System.getProperty("line.separator"));
        }
        writer.close();

    }
}
