package de.saschat.reflectiontest;

import java.util.Comparator;

public class PackageComparator implements Comparator<Package> {
    @Override
    public int compare(Package o1, Package o2) {
        String n1 = o1.getName();
        String n2 = o2.getName();

        String[] n1s = n1.split("\\.");
        String[] n2s = n2.split("\\.");

        int len = Math.min(n1s.length, n2s.length);
        int max = Math.max(n1s.length, n2s.length);
        for (int i = 0; i < len; i++) {
            String now1 = n1s[i];
            String now2 = n2s[i];
            if(now1.equals(now2))
                continue;
            return now1.compareTo(now2);
        }

        return 0;
    }
}
