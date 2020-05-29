package com.qwwuyu.file.entity;

import java.util.ArrayList;
import java.util.List;

public class FileResultEntity {
    public List<String> successFile;
    public List<String> failureFile;
    public List<String> existFile;
    public String message;

    public void setSuccessFile(String name) {
        if (successFile == null) successFile = new ArrayList<>();
        successFile.add(name);
    }

    public void setFailureFile(String name) {
        if (failureFile == null) failureFile = new ArrayList<>();
        failureFile.add(name);
    }

    public void setExistFile(String name) {
        if (existFile == null) existFile = new ArrayList<>();
        existFile.add(name);
    }
}
