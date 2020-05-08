package com.qwwuyu.file.server;

import com.qwwuyu.file.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class TempFileManagerImpl implements NanoHTTPD.TempFileManagerFactory {
    @Override
    public NanoHTTPD.TempFileManager create() {
        return new MyFileManager();
    }

    public static class MyFileManager implements NanoHTTPD.TempFileManager {
        private final File dir;
        private final List<NanoHTTPD.TempFile> tempFiles = new ArrayList<>();

        public MyFileManager() {
            this.dir = new File(FileUtils.getInstance().getCachePath());
        }

        @Override
        public void clear() {
            for (NanoHTTPD.TempFile file : this.tempFiles) {
                try {
                    file.delete();
                } catch (Exception ignored) {
                }
            }
            this.tempFiles.clear();
        }

        @Override
        public NanoHTTPD.TempFile createTempFile(File directory, String filename) throws Exception {
            MyTempFile tempFile = new MyTempFile(directory == null ? dir : directory, filename);
            if (filename == null) this.tempFiles.add(tempFile);
            return tempFile;
        }
    }

    public static class MyTempFile implements NanoHTTPD.TempFile {
        private final File file;
        private final OutputStream stream;

        public MyTempFile(File dir, String filename) throws IOException {
            if (filename == null) file = File.createTempFile("server-", "", dir);
            else file = new File(dir, filename);
            if (filename != null && file.exists()) throw new IOException(filename + ":exist, not cover.");
            stream = new FileOutputStream(this.file);
        }

        @Override
        public void delete() throws Exception {
            FileUtils.safeClose(stream);
            if (!this.file.delete()) {
                throw new Exception("could not delete temporary file");
            }
        }

        @Override
        public String getName() {
            return this.file.getAbsolutePath();
        }

        @Override
        public OutputStream open() throws Exception {
            return stream;
        }
    }
}
