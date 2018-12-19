package com.miolean.arena.framework;

import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Debug {

    private static long lastRefresh;
    private static long cycleTime = 1;

    private static Map<String, Long> log = new HashMap<>();
    private static Map<String, Long> oldLog;

    public static void logTime(String category, long value) {
        if(category == null) return;
        if(log.get(category) != null) log.put(category, log.get(category) + value);
        else log.put(category, value);
    }

    public static String getDebugLog() {

        StringBuilder b = new StringBuilder();

        for(String category: log.keySet()) {
            b.append(category).append(": ").append(log.get(category) / 1000).append("ps [").append(log.get(category) / 1000000).append("%]\n");
        }

        return b.toString();
    }

    public static void drawDebugLog(Graphics g) {

        if(oldLog == null) return;
        int i = 0;
        long totalTime = 0;


        for (Map.Entry<String, Long> entry : oldLog.entrySet()) {
            g.setColor(Color.getHSBColor(((i * 79) % 256)/255.0f, 200/255.0f, 200/255.0f));

            if (entry.getValue() > 0) {
                g.fillRect((int) (10 + (totalTime * 300) / cycleTime), 30, (int) ((entry.getValue() * 300) / cycleTime), 20);
                totalTime += entry.getValue();
            }

            i++;

            long value = Math.abs(entry.getValue());

            if(entry.getValue() > 0) g.fillRect(10, 40 + 45 * i, 5, 20);
            else {
                g.drawRect(10, 40 + 45 * i, 5, 20);
                g.drawRect(11, 41 + 45 * i, 3, 18);
            }
            g.setColor(Color.black);
            g.drawString(entry.getKey(), 20, 40 + 45 * i);
            g.drawString((value * 100)/cycleTime + "%", 20, 53 + 45 * i);
            g.drawString(value/1000 + "µs", 20, 66 + 45 * i);
        }

        g.drawRect(10, 30, 300, 20);

        i++;

        g.setColor(Color.gray);
        g.fillRect(10, 40 + 45 * i, 5, 20);
        g.setColor(Color.black);
        g.drawString("Total used", 20, 40 + 45 * i);
        g.drawString((totalTime * 100)/cycleTime + "%", 20, 53 + 45 * i);
        g.drawString(totalTime/1000 + "µs", 20, 66 + 45 * i);

        i++;
        g.setColor(Color.black);
        g.fillRect(10, 40 + 45 * i, 5, 20);
        g.drawString("Total in cycle", 20, 40 + 45 * i);
        g.drawString("100%", 20, 53 + 45 * i);
        g.drawString(cycleTime/1000 + "µs", 20, 66 + 45 * i);

    }

    public static void refresh() {
        oldLog = log;
        log = new HashMap<>();
        for(String s: oldLog.keySet()) log.put(s, 0L);
        cycleTime = (System.nanoTime() - lastRefresh);
        lastRefresh = System.nanoTime();
    }

    public static void breakpoint() {
        //This is useful in the context of IntelliJ
        System.out.println("Hit breakpoint");
    }
}
