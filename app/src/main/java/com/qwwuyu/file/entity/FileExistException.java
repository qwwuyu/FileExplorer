package com.qwwuyu.file.entity;

import java.io.IOException;

public class FileExistException extends IOException {
    public FileExistException(String message) {
        super(message);
    }
}
