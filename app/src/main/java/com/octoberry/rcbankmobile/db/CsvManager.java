package com.octoberry.rcbankmobile.db;

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by ruinvtyu on 22.01.2015.
 */
public class CsvManager {
    FileWriter writer;
    File csvFile;

    public CsvManager(String fileName) {
        try {
            csvFile = new File(Environment.getExternalStorageDirectory() + "/octoberry", fileName);
            writer = new FileWriter(csvFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getFilePath() {
        if (csvFile != null) {
            return csvFile.getAbsolutePath();
        }
        return "";
    }

    public void writeCsvHeader(ArrayList<String> headers) {
        StringBuilder line = new StringBuilder();
        for (String header: headers) {
            line.append(header);
            line.append(",");
        }
        line.replace(line.length() - 1, line.length(), "\n");
        try {
            writer.write(line.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeCsvLine(ArrayList<String> values) {
        StringBuilder line = new StringBuilder();
        for (String value: values) {
            line.append(value);
            line.append(",");
        }
        line.replace(line.length() - 1, line.length(), "\n");
        try {
            writer.write(line.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveToFile() {
        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
