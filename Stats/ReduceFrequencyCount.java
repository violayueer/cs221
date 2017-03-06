package Stats;

import java.io.*;
import java.util.*;

/**
 * Created by Yue on 1/17/17.
 */
public class ReduceFrequencyCount {
    class Frequency {
        String word;
        Integer count;

        public Frequency(String word, Integer count) {
            this.word = word;
            this.count = count;
        }
    }

    /*
    * time complexity: O(m + n), where m is size of input1 and n is size of input2
    * space complexity: O(1)
    * */
    public void reduceCounts(String filePath1, String filePath2, String outputFilePath) {
        try {
            Scanner in1 =  new Scanner(new FileReader(filePath1));
            Scanner in2 =  new Scanner(new FileReader(filePath2));

            String line1 = "", line2 = "";

            FileOutputStream fos = new FileOutputStream(outputFilePath);

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            while (in1.hasNextLine() || !line1.equals("") || in2.hasNextLine() || !line2.equals("")) {

                if (in1.hasNextLine() && line1.equals("")) {
                    line1 = in1.nextLine();
                }

                if (in2.hasNextLine() && line2.equals("")) {
                    line2 = in2.nextLine();
                }

                StringBuilder termIdSB1 = new StringBuilder("");
                StringBuilder termIdSB2 = new StringBuilder("");

                if (!line1.equals("")) {
                    for (int i = 0; i < line1.length(); i++) {
                        if (line1.charAt(i) == ',') {
                            break;
                        }
                        termIdSB1.append(line1.charAt(i));
                    }
                }

                if (!line2.equals("")) {
                    for (int i = 0; i < line2.length(); i++) {
                        if (line2.charAt(i) == ',') {
                            break;
                        }
                        termIdSB2.append(line2.charAt(i));
                    }
                }

                String termIdString1 = termIdSB1.toString();
                String termIdString2 = termIdSB2.toString();
                int termId1 = termIdString1.equals("") ? -1 : Integer.parseInt(termIdString1);
                int termId2 = termIdString2.equals("") ? -1 : Integer.parseInt(termIdString2);

                if (termId1 == termId2) {
                    String[] strs1 = line1.split(",");
                    String[] strs2 = line2.split(",");

                    Map<Integer, List<Integer>> posMap = new HashMap<Integer, List<Integer>>();
                    buildPosMap(posMap, strs1, 1);
                    buildPosMap(posMap, strs2, 1);

                    writePosMaptoFile(bw, termId1, posMap);

                    line1 = "";
                    line2 = "";

                }else if (line2.equals("") || (termId1 != -1 && termId1 < termId2)) {
                    bw.write(line1);
                    bw.newLine();

                    line1 = "";

                }else if (line1.equals("") || (termId2 != -1 && termId2 < termId1)){
                    bw.write(line2);
                    bw.newLine();

                    line2 = "";

                }else {
                    line1 = "";
                    line2 = "";
                }
            }
            bw.close();

        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void buildPosMap(Map<Integer, List<Integer>> posMap, String[] strs, int i) {
        while (i < strs.length - 1) {
            int docId = Integer.parseInt(strs[i++]);
            int frequency = Integer.parseInt(strs[i++]);

            List<Integer> posList = posMap.containsKey(docId) ? posMap.get(docId) : new ArrayList<Integer>();
            for (int j = 0; j < frequency; j++) {
                posList.add(Integer.parseInt(strs[i++]));
            }
            posMap.put(docId, posList);
        }
    }

    public void writePosMaptoFile(BufferedWriter bw, int termId, Map<Integer, List<Integer>> posMap) {
        StringBuilder sb = new StringBuilder("");
        sb.append(termId + ",");
        for (Integer urlId : posMap.keySet()) {

            List<Integer> posList = posMap.get(urlId);
            sb.append(urlId + "," + posList.size() + ",");

            for (Integer pos : posList) {
                sb.append(pos + ",");
            }
        }
        try {
            bw.write(sb.toString());
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
