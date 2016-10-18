package com.qwwuyu.server.utils;

import com.qwwuyu.server.bean.FileBean;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortUtil {
    public static void sortFileBean(List<? extends FileBean> list) {
        Collections.sort(list, new Comparator<FileBean>() {
            private int i = -1;
            private int j = 1;

            @Override
            public int compare(FileBean lhs, FileBean rhs) {
                if (lhs.isDirectory() && !rhs.isDirectory()) return i;
                if (!lhs.isDirectory() && rhs.isDirectory()) return j;
                return lhs.getName().compareTo(rhs.getName());
            }
        });
    }
}
