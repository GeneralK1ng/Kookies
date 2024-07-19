package org.kookies.mirai.commen.utils;

import com.google.gson.*;
import org.kookies.mirai.commen.adapter.LocalDateAdapter;
import org.kookies.mirai.commen.constant.MsgConstant;
import org.kookies.mirai.commen.enumeration.AIRoleType;
import org.kookies.mirai.commen.exceptions.CacheException;
import org.kookies.mirai.commen.exceptions.DataLoadException;
import org.kookies.mirai.commen.info.DataPathInfo;
import org.kookies.mirai.pojo.entity.Config;
import org.kookies.mirai.pojo.entity.api.request.baidu.ai.Message;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

/**
 * @author General_K1ng
 */
public class FileManager {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

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
     * 读取指定路径的图像文件，并返回其字节数据。
     *
     * @param filePath 图像文件的路径。
     * @return 图像文件的字节数据数组。
     * @throws IOException 如果读取文件时发生错误。
     */
    public static byte[] readImageFile(String filePath) throws IOException {
        // 通过FilePath获取文件路径，然后读取所有字节并返回
        return Files.readAllBytes(Paths.get(filePath));
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
        // 使用BufferedWriter来优化文件写入操作，通过 OutputStreamWriter 和 FileOutputStream 指定 UTF-8 编码
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(Files.newOutputStream(Paths.get(filePath)), StandardCharsets.UTF_8))) {
            // 遍历map的每个键值对，并将它们写入文件中。
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                // 写入键值对，以冒号分隔键和值，每对键值对占一行。
                if (!(entry.getKey().isEmpty() || entry.getValue() == null)) {
                    writer.write(entry.getValue() + ":" + entry.getKey() + "\n");
                }
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
        Config config = getConfig();

        List<Message> messages = new ArrayList<>();
        // 使用BufferedReader从指定的文件路径读取信息
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(FileManager.class.getResourceAsStream(filePath))))) {
            String line;
            int index = 1;
            // 遍历文件的每一行
            while ((line = reader.readLine()) != null) {
                line = line.replace("{name}", config.getBotInfo().getName())
                        .replace("{age}", String.valueOf(config.getBotInfo().getAge()))
                        .replace("{owner}", config.getBotInfo().getOwner());

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
        JsonArray jsonArray = null;
        try (FileReader reader = new FileReader(filePath)) {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            if (jsonElement.isJsonArray()) {
                jsonArray = jsonElement.getAsJsonArray();
            }
        }
        return jsonArray;
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
        Map<String, Integer> wordMap = new HashMap<>();

        try (BufferedReader bf = new BufferedReader(new FileReader(wordMapFilePath))) {
            String line;
            while ((line = bf.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    int count = Integer.parseInt(parts[0].trim());
                    String word = parts[1].trim();
                    wordMap.put(word, count);
                }
            }
        }
        System.out.println("Word map loaded successfully.");
        return wordMap;
    }

    private static Config getConfig() {
        Config config;
        try {
            // 从指定路径读取JSON配置文件
            JsonObject jsonObject = FileManager.readJsonFile(DataPathInfo.CONFIG_PATH);
            // 使用GSON从JSON对象解析出Config对象
            config = GSON.fromJson(jsonObject, Config.class);
        } catch (Exception e) {
            // 抓住任何异常，并抛出自定义的CacheException异常
            throw new CacheException(MsgConstant.CACHE_EXCEPTION);
        }
        return config;
    }


    /**
     * 将文件复制到指定目录下。
     * <p>
     * 通过重命名文件的方式来实现文件的复制，即将源文件移动到目标目录下。
     * 如果移动失败，则抛出数据加载异常，表示文件拷贝出错。
     *
     * @param fontFile 需要复制的文件，即源文件。
     * @param dir 目标目录，文件将被复制到这个目录下。
     * @throws IOException 如果文件移动操作失败，则抛出此异常。
     * @throws DataLoadException 如果文件无法移动到目标目录，则抛出此异常，表示字体安装出错。
     */
    public static void copyFile2Directory(File fontFile, File dir) throws IOException {
        boolean result = fontFile.renameTo(new File(dir, fontFile.getName()));
        if (!result) {
            throw new DataLoadException(MsgConstant.FILE_MOVE_ERROR);
        }
    }



    /**
     * 将字节数组写入指定文件。
     * <p>
     * 此方法创建一个新的文件并写入给定的字节数组。如果文件路径已存在，则会抛出异常。
     * 使用 try-with-resources 确保输出流正确关闭，即使发生异常也是如此。
     *
     * @param bytes 要写入文件的字节数组。
     * @param filePath 指定的文件路径，将在此路径下创建新文件。
     * @throws IOException 如果文件创建或写入过程中发生错误。
     */
    public static void saveFile(byte[] bytes, File filePath) throws IOException {
        // 创建一个新的文件。
        Path file = Files.createFile(filePath.toPath());
        // 使用 try-with-resources 确保输出流正确关闭。
        try (OutputStream os = Files.newOutputStream(file)) {
            os.write(bytes);
        }
    }


    /**
     * 从指定的文件路径读取全部字节内容。
     * <p>
     * 此方法提供了一个简单的方法来读取整个文件的内容，将其作为字节数组返回。
     * 这对于处理小型文件或需要直接访问文件字节内容的场景非常有用。
     *
     * @param filePath 文件的路径。此路径应指向一个存在的文件。
     * @return 文件的全部字节内容。
     * @throws IOException 如果发生I/O错误，例如文件不存在或没有读取权限时，将抛出此异常。
     */
    public static byte[] readByteFile(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));
    }



}
