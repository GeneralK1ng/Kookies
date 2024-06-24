package org.kookies.mirai.commen.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.kookies.mirai.commen.enumeration.AIRoleType;
import org.kookies.mirai.pojo.entity.api.request.baidu.ai.Message;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author General_K1ng
 */
public class FileManager {
    private static final Gson GSON = new Gson();

    /**
     * 从指定路径读取模板文件的内容。
     *
     * @param templatePath 模板文件的路径，相对于项目资源目录。
     * @return 文件的字符串内容，包括换行符。
     * @throws IOException 如果读取文件时发生错误。
     */
    public static String readTemplateFile(String templatePath) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();

        // 使用BufferedReader从模板路径读取资源文件，行-by-行地读取并构建内容。
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(FileManager.class.getResourceAsStream(templatePath))))) {
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append(System.lineSeparator());
            }
        }

        return contentBuilder.toString();
    }


    /**
     * 从指定的文件路径读取JSON文件内容，并将其解析为一个JsonObject。
     *
     * @param filePath 要读取的JSON文件的路径。
     * @return 解析后的JsonObject，如果文件读取或解析失败则抛出IOException。
     */
    public static JsonObject readJsonFile(String filePath) throws IOException {
        // 使用try-with-resources语句确保BufferedReader在使用完毕后能够自动关闭
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            // 将文件内容解析为JSON对象
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    /**
     * 将JsonObject写入指定的文件路径。
     *
     * @param filePath 要写入的JSON文件的路径。
     * @param jsonObject 要写入的JsonObject。
     * @throws IOException 如果写入文件时发生错误。
     */
    public static void writeJsonFile(String filePath, JsonObject jsonObject) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
            GSON.toJson(jsonObject, writer);
        }
    }

    /**
     * 将字符串内容写入指定的文件路径。
     *
     * @param filePath 要写入的文件路径。
     * @param content 要写入的字符串内容。
     * @throws IOException 如果写入文件时发生错误。
     */
    public static void write(String filePath, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        }
    }


    /**
     * 从指定的文件路径读取答案书，并返回一个Map，其中键是答案书条目的索引，值是答案书条目的内容。
     *
     * @param filePath 要读取的答案书文件的路径。
     * @return 一个Map，其中键是答案书条目的索引，值是答案书条目的内容。
     * @throws IOException 如果读取文件时发生错误。
     */
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

    /**
     * 将Map对象的内容写入到指定路径的文本文件中。
     * <p>
     * 每个键值对占一行，以冒号分隔键和值。
     *
     * @param filePath 要写入的文本文件的路径。
     * @param map 要写入的Map对象，其键值对将被写入文件。
     * @throws IOException 如果文件写入过程中发生错误。
     */
    public static void writeWordMap2Txt(String filePath, Map<String, Integer> map) throws IOException {
        // 使用BufferedWriter来优化文件写入操作，通过 FileWriter 将数据写入到指定文件。
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // 遍历map的每个键值对，并将它们写入文件中。
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                // 写入键值对，以冒号分隔键和值，每对键值对占一行。
                writer.write(entry.getValue() + ":" + entry.getKey() + "\n");
            }
        }
        // try-with-resources语句确保BufferedWriter在操作完成后被正确关闭。
    }



    /**
     * 从指定文件路径读取信息，构造并返回一个包含消息的列表。
     * <p>
     * 每两行被视为一对对话，其中奇数行代表用户的消息，偶数行代表助手的消息。
     *
     * @param filePath 要读取的文件的路径。文件应包含一对对的用户和助手的对话。
     * @return 一个包含读取到的消息的列表，其中每个消息都标明了是用户还是助手发出的。
     * @throws IOException 如果读取文件时发生输入输出异常。
     */
    public static List<Message> readBotInfo (String filePath) throws IOException {
        List<Message> messages = new ArrayList<>();
        // 使用BufferedReader从指定的文件路径读取信息
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(FileManager.class.getResourceAsStream(filePath))))) {
            String line;
            int index = 1;
            // 遍历文件的每一行
            while ((line = reader.readLine()) != null) {
                Message message;
                // 根据行的索引奇偶性，区分用户消息和助手消息
                if (index % 2 == 1) {
                    message = Message.builder()
                            .role(AIRoleType.USER.getRole())
                            .content(line)
                            .build();
                } else {
                    message = Message.builder()
                            .role(AIRoleType.ASSISTANT.getRole())
                            .content(line)
                            .build();
                }
                messages.add(message);
                index++;
            }
        }
        return messages;
    }

    /**
     * 从指定的文件路径读取JSON数组。
     * <p>
     * 此方法通过文件路径创建一个BufferedReader，用于读取文件内容。它使用try-with-resources语句，
     * 确保在操作完成后自动关闭BufferedReader，有效地管理资源，避免了资源泄露。
     *
     * @param filePath 文件路径，指定要读取的JSON数组的文件位置。
     * @return JsonArray 对象，表示从文件中读取的JSON数组。
     * @throws IOException 如果在读取文件过程中发生错误，则抛出此异常。
     */
    public static JsonArray readJsonArray(String filePath) throws IOException {
        // 使用Files.newBufferedReader创建一个BufferedReader，用于读取指定路径的文件。
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            // 使用JsonParser.parseReader解析BufferedReader中的JSON内容，并将其转换为JsonArray。
            return JsonParser.parseReader(reader).getAsJsonArray();
        }
    }

    /**
     * 从指定的文件路径读取单词映射。
     * 每行映射文件内容格式为 "单词:计数"，本方法将解析这些内容并构建一个单词到计数的映射。
     *
     * @param wordMapFilePath 映射文件的路径，相对于类路径。
     * @return 包含单词及其出现次数的映射。
     * @throws IOException 如果读取文件时发生错误。
     */
    public static Map<String, Integer> readWordMap(String wordMapFilePath) throws IOException {
        // 初始化一个HashMap来存储单词和它们的出现次数。
        Map<String, Integer> wordMap = new HashMap<>();
        // 使用try-with-resources语句确保文件资源在使用后能被正确关闭。
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                // 通过类路径获取资源流，确保文件路径是有效的类路径资源。
                Objects.requireNonNull(FileManager.class.getResourceAsStream(wordMapFilePath))))) {
            String line;
            // 循环读取文件的每一行直到文件结束。
            while ((line = reader.readLine()) != null) {
                // 使用冒号分隔每行的单词和计数。
                String[] parts = line.split(":");
                // 确保分割结果包含两个部分：单词和计数。
                if (parts.length == 2) {
                    // 移除空白字符，以确保单词和计数的准确性。
                    int count = Integer.parseInt(parts[0].trim());
                    String word = parts[1].trim();
                    // 将单词和计数添加到映射中
                    wordMap.put(word, count);
                }
            }
            return wordMap;
        }
    }

}
