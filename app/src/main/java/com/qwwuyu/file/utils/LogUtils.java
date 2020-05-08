package com.qwwuyu.file.utils;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.qwwuyu.file.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import androidx.annotation.IntDef;

/**
 * Created by qiwei on 2017/7/12
 * 改于https://github.com/Blankj/AndroidUtilCode
 */
public class LogUtils {
    public static final int V = Log.VERBOSE, D = Log.DEBUG, I = Log.INFO, W = Log.WARN, E = Log.ERROR, A = Log.ASSERT;
    private static final char[] T    = new char[]{'V', 'D', 'I', 'W', 'E', 'A'};
    private static final int    JSON = 0x10, XML = 0x20;

    @IntDef({V, D, I, W, E, A})
    @Retention(RetentionPolicy.SOURCE)
    private @interface TYPE {
    }

    @SuppressLint("SimpleDateFormat")
    private static final Format FORMAT        = new SimpleDateFormat("MM-dd HH:mm:ss.SSS ");
    private static final String LINE_SEP      = System.getProperty("line.separator");
    private static final String TOP_BORDER    = "╔═══════════════════════════════════════════════════════════════════════════════════════════════════";
    private static final String LEFT_BORDER   = "║ ";
    private static final String BOTTOM_BORDER = "╚═══════════════════════════════════════════════════════════════════════════════════════════════════";
    private static final int    MAX_LEN       = 4000;
    private static final String NULL          = "null";

    public static final boolean LOG  = BuildConfig.DEBUG;
    private static String logTag    = null;
    private static boolean logHead   = false;
    private static boolean logBorder = false;
    private static int     logFilter = V;
    private static ExecutorService executor;
    private static OnErrorListener onErrorListener;
    private static String dir      = null;
    private static String head_sep = LINE_SEP;

    /** ======================== 使用Log工具 ======================== */
    public static class Builder {
        public Builder() {
        }

        public Builder setLogDir(String dir) {
            LogUtils.dir = dir;
            return this;
        }

        public Builder setLogTag(String tag) {
            LogUtils.logTag = tag;
            return this;
        }

        public Builder enableLogHead(boolean enable) {
            LogUtils.logHead = enable;
            return this;
        }

        public Builder enableLogBorder(boolean enable) {
            LogUtils.logBorder = enable;
            return this;
        }

        public Builder setLogFilter(@TYPE final int level) {
            LogUtils.logFilter = level;
            return this;
        }

        public Builder setHeadSep(String head_sep) {
            LogUtils.head_sep = head_sep;
            return this;
        }

        public Builder onErrorListener(OnErrorListener onErrorListener) {
            LogUtils.onErrorListener = onErrorListener;
            return this;
        }
    }

    public static void v(Object contents) {
        log4(V, logTag, contents);
    }

    public static void d(Object contents) {
        log4(D, logTag, contents);
    }

    public static void i(Object contents) {
        log4(I, logTag, contents);
    }

    public static void w(Object contents) {
        log4(W, logTag, contents);
    }

    public static void e(Object contents) {
        log4(E, logTag, contents);
    }

    public static void a(Object contents) {
        log4(A, logTag, contents);
    }

    public static void v(String tag, String format, Object... args) {
        log4(V, tag, format, args);
    }

    public static void d(String tag, String format, Object... args) {
        log4(D, tag, format, args);
    }

    public static void i(String tag, String format, Object... args) {
        log4(I, tag, format, args);
    }

    public static void w(String tag, String format, Object... args) {
        log4(W, tag, format, args);
    }

    public static void e(String tag, String format, Object... args) {
        log4(E, tag, format, args);
    }

    public static void a(String tag, String format, Object... args) {
        log4(A, tag, format, args);
    }

    public static void json(@TYPE int type, String tag, String contents) {
        log4(JSON | type, tag, contents);
    }

    public static void xml(@TYPE int type, String tag, String contents) {
        log4(XML | type, tag, contents);
    }

    public static void onError(final Throwable e) {
        onError(e, -1);
    }

    public static void onError(final Throwable e, int flag) {
        if (LOG && logFilter < E) {
            e.printStackTrace();
            if (onErrorListener != null) {
                onErrorListener.onError(e, flag);
            }
        }
    }

    public static void printStackTrace(Throwable e) {
        if (LOG && logFilter < E) {
            e.printStackTrace();
        }
    }

    public static void logError(Throwable e) {
        if (LOG && logFilter < E) {
            log(E, logHead, "StackTrace", 3, Log.getStackTraceString(e));
            e.printStackTrace();
        }
    }

    public static void logThread(String tag) {
        if (LOG) {
            log(I, logHead, tag, 3, "isMainThread=%b", Looper.myLooper() == Looper.getMainLooper());
        }
    }

    /** 处理日志 */
    private static void log4(int type, String tag, Object format, Object... args) {
        log(type, logHead, tag, 4, format, args);
    }

    /** 处理日志 */
    public static void log(@TYPE int type, boolean logHead, String tag, int stackTrace, Object format, Object... args) {
        if (!LOG) return;
        int type_low = type & 0x0f, type_high = type & 0xf0;
        if (type_low < logFilter) return;
        final String[] tagAndHead = processTagAndHead(logHead, tag, stackTrace);
        String body = processBody(type_high, format, args);
        print2Console(type_low, tagAndHead[0], tagAndHead[1] + body);
        if (dir != null) print2File(type_low, tagAndHead[0], tagAndHead[2] + body);
    }

    /** 处理TAG和位子 */
    private static String[] processTagAndHead(boolean logHead, String tag, int stackTrace) {
        if (tag == null) tag = logTag;
        if (logHead || tag == null) {
            StackTraceElement targetElement = new Throwable().getStackTrace()[stackTrace];
            String className = targetElement.getClassName();
            String[] classNameInfo = className.split("\\.");
            if (classNameInfo.length > 0) {
                className = classNameInfo[classNameInfo.length - 1];
            }
            if (className.contains("$")) {
                className = className.split("\\$")[0];
            }
            if (tag == null) tag = className;
            String fileName = targetElement.getFileName();
            if (fileName == null) fileName = className + ".java";
            if (logHead) {
                String head = String.format(Locale.getDefault(), "%s, %s(%s:%d)",
                        Thread.currentThread().getName(), targetElement.getMethodName(), fileName, targetElement.getLineNumber());
                return new String[]{tag, head + head_sep, " [" + head + "]: "};
            }
        }
        return new String[]{tag, "", ": "};
    }

    /** 处理内容 */
    private static String processBody(final int type, Object format, final Object... args) {
        if (format == null) {
            return NULL;
        }
        String body = format.toString();
        if (type == JSON) {
            return formatJson(body);
        } else if (type == XML) {
            return formatXml(body);
        } else if (args.length == 0) {
            return body;
        } else {
            return String.format(body, args);
        }
    }

    /** 打印日志到控制台 */
    private static void print2Console(final int type, final String tag, String msg) {
        if (logBorder) print(type, tag, TOP_BORDER);
        if (logBorder) msg = addLeftBorder(msg);
        int len = msg.length();
        int countOfSub = len / MAX_LEN;
        if (countOfSub > 0) {
            print(type, tag, msg.substring(0, MAX_LEN));
            String sub;
            int index = MAX_LEN;
            for (int i = 1; i < countOfSub; i++) {
                sub = msg.substring(index, index + MAX_LEN);
                print(type, tag, logBorder ? LEFT_BORDER + sub : sub);
                index += MAX_LEN;
            }
            sub = msg.substring(index, len);
            print(type, tag, logBorder ? LEFT_BORDER + sub : sub);
        } else {
            print(type, tag, msg);
        }
        if (logBorder) print(type, tag, BOTTOM_BORDER);
    }

    private static void print(final int type, final String tag, final String msg) {
        Log.println(type, tag, msg);
    }

    /** 打印日志到文件 */
    private static void print2File(final int type, final String tag, final String msg) {
        String format = FORMAT.format(new Date(System.currentTimeMillis()));
        String date = format.substring(0, 5);
        String time = format.substring(6);
        final String fullPath = dir + File.separator + date + ".txt";
        if (!createOrExistsFile(fullPath)) {
            return;
        }
        final String content = time + T[type - V] + "/" + tag + msg + LINE_SEP;
        if (executor == null) {
            synchronized (LogUtils.class) {
                if (executor == null) {
                    executor = Executors.newSingleThreadExecutor();
                }
            }
        }
        executor.execute(() -> {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(fullPath, true))) {
                bw.write(content);
            } catch (IOException ignored) {
            }
        });
    }

    private static boolean createOrExistsFile(final String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) return file.isFile();
            return createOrExistsDir(file.getParentFile()) && file.createNewFile();
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean createOrExistsDir(final File file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    private static String formatJson(String json) {
        try {
            if (json.startsWith("{")) return new JSONObject(json).toString(4);
            else if (json.startsWith("[")) return new JSONArray(json).toString(4);
        } catch (JSONException ignored) {
        }
        return json;
    }

    private static String formatXml(String xml) {
        try {
            Source xmlInput = new StreamSource(new StringReader(xml));
            StreamResult xmlOutput = new StreamResult(new StringWriter());
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(xmlInput, xmlOutput);
            xml = xmlOutput.getWriter().toString().replaceFirst(">", ">" + LINE_SEP);
        } catch (Exception ignored) {
        }
        return xml;
    }

    private static String addLeftBorder(final String msg) {
        StringBuilder sb = new StringBuilder();
        String[] lines = msg.split(LINE_SEP);
        for (String line : lines) {
            sb.append(LEFT_BORDER).append(line).append(LINE_SEP);
        }
        return sb.toString();
    }

    private static String getStackTraceString(Throwable tr) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        return sw.toString();
    }

    public interface OnErrorListener {
        void onError(Throwable throwable, int flag);
    }

    public static class CrashErrorListener implements OnErrorListener {
        @Override
        public void onError(final Throwable throwable, int flag) {
            new Handler(Looper.getMainLooper()).post(() -> {
                throw new RuntimeException(throwable);
            });
        }
    }
}