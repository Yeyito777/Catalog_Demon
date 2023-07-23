package net.yeyito.util;

import java.util.HashMap;

public class DeltaTime {
    private static HashMap<Integer,Long> IDtoTime = new HashMap<>();
    public static void start(int ID) {
        IDtoTime.put(ID,System.nanoTime());
    }

    public static double stop(int ID) {
        long value = IDtoTime.get(ID);
        IDtoTime.remove(ID);
        double delta = Math.abs(System.nanoTime() - value);
        return delta/10e9;
    }
}
