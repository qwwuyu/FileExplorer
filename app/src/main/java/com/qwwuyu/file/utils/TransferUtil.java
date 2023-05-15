package com.qwwuyu.file.utils;

public class TransferUtil {
    public static <T> T transfer(Execute execute) throws Exception {
        final Object obj = new Object();
        final Object[] data = new Object[2];
        execute.run((o1, o2) -> {
            synchronized (obj) {
                data[0] = o1;
                data[1] = o2;
                obj.notifyAll();
            }
        });
        synchronized (obj) {
            if (data[0] == null && data[1] == null) {
                try {
                    obj.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (data[0] != null) {
            return (T) data[0];
        } else if (data[1] != null) {
            throw (Exception) data[1];
        } else {
            throw new Exception();
        }
    }

    public interface ExecuteResult {
        void result(Object o1, Object o2);
    }

    public interface Execute {
        void run(ExecuteResult result);
    }
}
