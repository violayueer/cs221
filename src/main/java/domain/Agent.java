package domain;

import Indexing.IndexBuilder;

import java.lang.instrument.Instrumentation;

/**
 * Created by Yue on 2/27/17.
 */
public class Agent {
    public static void premain(String args, Instrumentation inst) {
        IndexBuilder obj = new IndexBuilder();
        long size = inst.getObjectSize(obj);
        System.out.println("Bytes used by object: " + size);
    }
}
