package org.kookies.mirai.commen.utils;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 词性标注
 * <p>
 * - v：动词
 * <p>
 * - en：英文
 * <p>
 * - nz：其他专有名词
 * <p>
 * - n：名词
 * <p>
 * - p：介词
 * <p>
 * - r：代词
 * <p>
 * - c：连词
 * <p>
 * - d：副词
 * <p>
 * - a：形容词
 * <p>
 * - m：数词
 * <p>
 * - l：习惯用语
 *
 * @author General_K1ng
 */

public class TextAnalyzer {

    /**
     * 统计句子中每个单词出现的次数。
     * <p>
     * 该方法首先将句子分割成单词，然后统计每个单词出现的次数。
     *
     * @param sentence 待统计的句子。
     * @return 返回一个映射，其中每个单词与其出现次数相对应。
     */
    public static Map<String, Integer> countWords(String sentence) {
        // 使用分词方法将句子分割成单词
        Map<String, String> words = filter(segmentSentence(sentence));

        // 通过流处理方式对单词进行统计
        return words.entrySet().stream()
                // 按照单词值（即分词结果）进行分组
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                // 对每个分组计算出现的总次数，这里使用summingInt函数来实现
                Collectors.summingInt(e -> 1)));
    }

    /**
     * 统计句子中满足特定长度条件的单词出现次数。
     * <p>
     * 该方法首先对句子进行分词，然后筛选出长度大于指定值的单词，最后统计这些单词的出现频率。
     *
     * @param sentence 待处理的句子。
     * @param wordSize 单词长度的阈值，只有长度大于该值的单词才会被计入统计。
     * @return 返回一个映射，其中每个单词与其出现次数相对应。
     */
    public static Map<String, Integer> countWords(String sentence, Integer wordSize) {
        // 对句子进行分词，得到单词与对应频率的映射
        Map<String, String> words = segmentSentence(sentence);
        // 筛选长度大于wordSize的单词，并统计这些单词的出现次数
        return words.entrySet().stream()
                .filter(entry -> entry.getKey().length() > wordSize)
                .collect(Collectors.groupingBy(Map.Entry::getValue,
                Collectors.summingInt(e -> 1)));
    }

    /**
     * 统计句子中单词的出现次数，忽略指定的停用词和停用词性。
     *
     * @param sentence 待处理的句子。
     * @param stopWords 停用词列表，用于过滤句子中的常见无意义词。
     * @param stopNature 停用词性列表，用于过滤特定词性的词。
     * @return 返回一个映射，其中键是单词，值是该单词在句子中出现的次数。
     */
    public static Map<String, Integer> countWords(String sentence, String[] stopWords, String[] stopNature) {
        // 使用分词方法将句子分割成单词
        Map<String, String> words = segmentSentence(sentence);

        return words.entrySet().stream()
                // 过滤掉停用词和停用词性
                .filter(entry -> !ArrayUtils.contains(stopWords, entry.getKey()) && !ArrayUtils.contains(stopNature, entry.getValue()))
                // 按照单词值（即分词结果）进行分组
                .collect(Collectors.groupingBy(Map.Entry::getValue,
                // 对每个分组计算出现的总次数，这里使用summingInt函数来实现
                Collectors.summingInt(e -> 1)));
    }

    /**
     * 统计句子中除停用词外每个词出现的次数。
     *
     * @param sentence 待处理的句子。
     * @param stopWords 停用词列表，这些词在统计时会被忽略。
     * @return 返回一个映射，其中每个词映射到它在句子中出现的次数。
     */
    public static Map<String, Integer> countWords(String sentence, String[] stopWords) {
        // 对句子进行分词，得到一个词与词频的映射
        Map<String, String> words = segmentSentence(sentence);

        // 过滤掉停用词，然后按词频进行聚合
        return words.entrySet().stream()
                .filter(entry -> !ArrayUtils.contains(stopWords, entry.getKey()))
                .collect(Collectors.groupingBy(Map.Entry::getValue,
                        Collectors.summingInt(e -> 1)));
    }

    /**
     * 分词句子并返回分词结果的映射表。
     * <p>
     * 该方法接收一个句子字符串作为输入，使用结巴分词库对其进行分词处理。
     * 返回一个映射表，其中键是分词后的词汇，值是词汇的词性标注。
     *
     * @param sentence 输入的待分词句子。
     * @return 返回一个Map，包含分词结果的词汇及其词性标注。
     */
    private static Map<String, String> segmentSentence(String sentence) {
        Result parsed = ToAnalysis.parse(sentence);
        Map<String, String> words = new HashMap<>();
        for (Term term : parsed) {
            words.put(term.getName(), term.getNatureStr());
        }
        return words;
    }


    /**
     * 过滤掉Map中值为"m"的条目。
     * 该方法通过流操作对输入的Map进行过滤，移除所有值为"m"的键值对，返回一个新的Map。
     *
     * @param words 输入的Map，其中键为字符串，值也为字符串。
     * @return 返回一个新的Map，其中不包含值为"m"的条目。
     */
    private static Map<String, String> filter(Map<String, String> words) {
        return words.entrySet().stream()
                .filter(entry -> !"m".equals(entry.getValue()))
                .filter(entry -> !"r".equals(entry.getValue()))
                .filter(entry -> !"p".equals(entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }



}
