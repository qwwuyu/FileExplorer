package com.qwwuyu.file.nano;

import com.qwwuyu.file.entity.FileExistException;
import com.qwwuyu.file.helper.FileHelper;
import com.qwwuyu.file.utils.CommUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class TempFileManagerImpl implements NanoHTTPD.TempFileManagerFactory {
    @Override
    public NanoHTTPD.TempFileManager create() throws Exception {
        return new MyFileManager();
    }

    public static class MyFileManager implements NanoHTTPD.TempFileManager {
        private final File fileDir;
        private final List<File> tempFiles = new ArrayList<>();

        public MyFileManager() throws FileNotFoundException {
            String cachePath = FileHelper.getInstance().getCachePath();
            if (cachePath == null) throw new FileNotFoundException();
            fileDir = new File(FileHelper.getInstance().getCachePath());
        }

        @Override
        public void clear() {
            for (File file : this.tempFiles) {
                try {
                    file.delete();
                } catch (Exception ignored) {
                }
            }
            this.tempFiles.clear();
        }

        @Override
        public RandomAccessFile randomAccessFile() throws Exception {
            File tempFile = File.createTempFile("FileManageTemp", "", fileDir);
            this.tempFiles.add(tempFile);
            return new RandomAccessFile(tempFile, "rw");
        }

        @Override
        public NanoHTTPD.TempFile createTempFile(ProxyFile directory, String filename) throws Exception {
            return new MyTempFile(directory, filename);
        }
    }

    public static class MyTempFile implements NanoHTTPD.TempFile {
        private final ProxyFile file;
        private final OutputStream stream;

        public MyTempFile(ProxyFile dir, String filename) throws IOException {
            ProxyFile child = dir.child(filename);
            if (child != null && child.exists()) {
                throw new FileExistException(child.getPath());
            }
            file = dir.createFile(filename);
            if (file == null) {
                throw new IOException();
            }
            stream = file.outputStream();
        }

        @Override
        public void delete() throws Exception {
            CommUtils.closeStream(stream);
            if (!file.delete()) {
                throw new Exception("could not delete temporary file");
            }
        }

        @Override
        public String getName() {
            return file.getPath();
        }

        @Override
        public OutputStream open() throws Exception {
            return stream;
        }
    }
}
