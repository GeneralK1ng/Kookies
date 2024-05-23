package org.kookies.mirai.commen.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FileManager {
    private static final Gson gson = new Gson();

    public static String readTemplateFile(String templatePath) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(FileManager.class.getResourceAsStream(templatePath))))) {
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append(System.lineSeparator());
            }
        }

        return contentBuilder.toString();
    }

    public static JsonObject readJsonFile(String filePath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    public static void writeJsonFile(String filePath, JsonObject jsonObject) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
            gson.toJson(jsonObject, writer);
        }
    }

    public static void write(String filePath, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(filePath))) {
            writer.write(content);
        }
    }


    public static Map<Integer, String> readAnswerBook(String filePath) throws IOException{
        Map<Integer, String> answerBook = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(FileManager.class.getResourceAsStream(filePath))))) {
            String line;
            int index = 1;
            while ((line = reader.readLine()) != null) {
                answerBook.put(index++, line);
            }
        }
        return answerBook;
    }
}
