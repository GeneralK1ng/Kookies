package org.kookies.mirai.plugin.service.Impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.CircleBackground;
import com.kennycason.kumo.font.FontWeight;
import com.kennycason.kumo.font.KumoFont;
import com.kennycason.kumo.font.scale.SqrtFontScalar;
import com.kennycason.kumo.nlp.FrequencyFileLoader;
import com.kennycason.kumo.palette.ColorPalette;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;
import okhttp3.Response;
import org.json.JSONException;
import org.kookies.mirai.commen.adapter.LocalDateAdapter;
import org.kookies.mirai.commen.constant.LolimiApiConstant;
import org.kookies.mirai.commen.constant.MsgConstant;
import org.kookies.mirai.commen.constant.WordCloudConstant;
import org.kookies.mirai.commen.enumeration.AIRoleType;
import org.kookies.mirai.commen.enumeration.EmojiType;
import org.kookies.mirai.commen.enumeration.JokeType;
import org.kookies.mirai.commen.exceptions.DataLoadException;
import org.kookies.mirai.commen.exceptions.DataWriteException;
import org.kookies.mirai.commen.exceptions.RequestException;
import org.kookies.mirai.commen.info.DataPathInfo;
import org.kookies.mirai.commen.utils.*;
import org.kookies.mirai.plugin.auth.DuplicatePermission;
import org.kookies.mirai.plugin.auth.Permission;
import org.kookies.mirai.plugin.service.EntertainmentService;
import org.kookies.mirai.pojo.dto.EvaluateSomebodyDTO;
import org.kookies.mirai.pojo.dto.WordStatisticsDTO;
import org.kookies.mirai.pojo.entity.PersonalMessage;
import org.kookies.mirai.pojo.entity.api.request.baidu.ai.Message;
import org.kookies.mirai.pojo.entity.api.response.baidu.ai.ChatResponse;
import org.kookies.mirai.pojo.entity.api.response.joke.JokeResponse;
import org.kookies.mirai.pojo.entity.api.response.joke.SingleResponse;
import org.kookies.mirai.pojo.entity.api.response.joke.TwoPartResponse;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author General_K1ng
 */
public class EntertainmentServiceImpl implements EntertainmentService {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .serializeNulls()
            .create();

    /**
     * 对于来自特定用户的群组消息，给出一个随机的答案。
     *
     * @param sender 消息发送者的ID，类型为Long。
     * @param group 消息所属的群组，类型为Group。
     * 该方法不返回任何内容，即void类型。
     */
    @Override
    public void answer(Long sender, Group group) {
        // 初始化消息链构建器
        MessageChainBuilder chain = new MessageChainBuilder();

        // 创建随机数生成器并根据答案书的大小生成一个随机索引
        Random random = new Random();
        Map<Integer, String> answerBook = readAnswerBook();
        int randomIndex = random.nextInt(answerBook.size());

        // 序列化一个AT消息（@某个用户）的MiraiCode
        MessageChain at = MiraiCode.deserializeMiraiCode("[mirai:at:" + sender + "]");

        // 根据随机索引获取一个随机的答案
        String answer = answerBook.get(randomIndex);

        // 权限鉴定
        if (checkPermission(sender, group)) {
            // 发送包含AT和随机答案的消息
            sendMsg(at, group, chain, answer);
        }
    }

    /**
     * 评估某人在群组中的信息并发送相关评价消息。
     * @param sender 发送请求的成员
     * @param group 目标群组
     * @param somebody 需要被评估的成员名称或ID
     */
    @Override
    public void evaluateSomebody(Member sender, Group group, String somebody) {
        MessageChainBuilder chain = new MessageChainBuilder();
        MessageChain at = MiraiCode.deserializeMiraiCode("[mirai:at:" + sender.getId() + "]");

        if (Permission.checkPermission(sender.getId(), group.getId())) {
            NormalMember member = getSomebody(group, somebody);

            EvaluateSomebodyDTO dto = EvaluateSomebodyDTO.builder()
                    .nameCard(member.getNameCard())
                    .nick(member.getNick())
                    .historyMsg(getHistoryMessage(member.getId(), group.getId()))
                    .build();

            List<Message> botMsg = createEvaluateBotMsg(dto);

            ChatResponse response = getResponse(botMsg, sender.getId());
            sendMsg(at, group, chain, response.getResult());
        }
    }

    /**
     * 在指定的群组中发送今日词云图。
     * <p>
     * 此方法首先验证发送者是否有权限在该群组中发送消息，然后生成并发送今日词云图。
     *
     * @param sender 发送者ID，用于权限验证。
     * @param group 目标群组，用于获取群组ID和在该群组中发送消息。
     */
    @Override
    public void todayWord(Long sender, Group group) {
        // 断言发送者是否有权限在该群组中发送消息。
        assert Permission.checkPermission(sender, group.getId());

        // 初始化消息构建器，用于构建发送的消息。
        MessageChainBuilder chain = new MessageChainBuilder();

        // 从缓存中获取今日词云数据文件。
        File todayWordCount = CacheManager.getTodayWordCountFile(group.getId());
        // 根据词云数据文件生成词云图像文件。
        File wordCloudImg = generateWordCloudImgFile(todayWordCount);

        // 读取词云图像文件的数据，准备上传。
        byte[] imgData = getWordCloudImg(wordCloudImg);
        // 将图像数据转换为群组可发送的消息对象。
        Image image = group.uploadImage(ExternalResource.create(Objects.requireNonNull(imgData)));

        // 使用消息构建器发送图像消息。
        sendMsg(chain, group, image);

    }

    /**
     * 给定群组中发送昨天的词云图。
     * 此方法首先验证用户是否有权限查看该群组的数据，然后生成昨天的词云图像并将其发送到指定的群组中。
     *
     * @param id 用户ID，用于权限检查。
     * @param group 目标群组对象，用于获取群组ID和上传图片。
     */
    @Override
    public void yesterdayWord(long id, Group group) {
        // 断言用户是否有权限查看该群组的数据
        assert Permission.checkPermission(id, group.getId());

        // 初始化消息构建器
        MessageChainBuilder chain = new MessageChainBuilder();

        // 从缓存管理器中获取昨天的词云数据文件
        File yesterdayWordCount = CacheManager.getYesterdayWordCountFile(group.getId());

        // 根据昨天的词云数据文件生成词云图像文件
        File wordCloudImg = generateWordCloudImgFile(yesterdayWordCount);

        // 从词云图像文件中获取图像数据
        byte[] imgData = getWordCloudImg(wordCloudImg);

        // 将图像数据转换为群组可上传的Image对象
        Image image = group.uploadImage(ExternalResource.create(Objects.requireNonNull(imgData)));

        // 使用消息构建器发送包含词云图像的消息到群组
        sendMsg(chain, group, image);
    }

    /**
     * 根据群组ID生成并发送一周热词图。
     * <p>
     * 此方法首先验证用户是否有权限操作指定的群组，然后生成一周热词的词云图片，
     * 并将该图片发送到指定的群组中。
     *
     * @param id 用户ID，用于权限验证。
     * @param group 目标群组对象，用于获取群组ID并发送消息。
     * @throws DataLoadException 如果删除一周热词计数文件失败，则抛出此异常。
     */
    @Override
    public void weekWord(long id, Group group) {
        // 断言用户是否有权限操作指定的群组。
        assert Permission.checkPermission(id, group.getId());

        // 初始化消息构建器。
        MessageChainBuilder chain = new MessageChainBuilder();

        // 获取一周热词计数文件。
        File weekWordCount = CacheManager.getWeekWordCountFile(group.getId());

        // 根据热词计数文件生成词云图片文件。
        File weekWordCloudImg = generateWordCloudImgFile(weekWordCount);

        // 读取词云图片数据。
        byte[] imgData = getWordCloudImg(weekWordCloudImg);

        // 将图片数据转换为群组可发送的消息对象。
        Image image = group.uploadImage(ExternalResource.create(Objects.requireNonNull(imgData)));

        // 如果热词计数文件存在，则尝试删除该文件。
        if (weekWordCount.exists()) {
            boolean isSuccess = weekWordCount.delete();
            // 如果删除失败，抛出数据加载异常。
            if (!isSuccess) {
                throw new DataLoadException(MsgConstant.CACHE_EXCEPTION);
            }
        }

        // 发送包含词云图片的消息到群组。
        sendMsg(chain, group, image);
    }

    /**
     * 统计并发送指定群组的单词统计数据。
     * <p>
     * 此方法用于接收群组ID和群组对象，通过调用一系列方法来统计该群组一周内单词的使用次数，
     * 并将统计结果以消息的形式发送回群组。
     *
     * @param id 用户ID，用于权限检查和消息发送。
     * @param group 群组对象，用于获取群组ID和发送消息。
     */
    @Override
    public void wordStatistics(long id, Group group) {
        // 断言用户是否有权限查看该群组的统计数据
        assert Permission.checkPermission(id, group.getId());

        // 初始化消息链构建器，用于组装最终发送的消息
        MessageChainBuilder chain = new MessageChainBuilder();

        // 从缓存中获取群组一周内的单词使用次数统计
        Map<String, Integer> weekCnt = CacheManager.getWeekCount(group.getId());

        try {
            weekCnt = TextAnalyzer.filtrateStopWords(weekCnt);
        } catch (IOException e) {
            throw new DataLoadException(MsgConstant.WORD_MAP_LOAD_ERROR);
        }

        // 根据单词使用次数统计生成DTO对象，用于数据传输和消息创建
        WordStatisticsDTO dto = generateWordStatistics(weekCnt);

        // 根据DTO创建机器人发送的消息列表
        List<Message> botMsg = createWordStatBotMsg(dto);

        // 根据消息列表生成聊天响应，用于实际的消息发送
        ChatResponse response = getResponse(botMsg, id);

        // 发送统计结果到指定群组
        sendMsg(dto, group, chain, response.getResult());
    }

    /**
     * 允许成员在群组内执行 "fuckSomebody" 操作。
     * <p>
     * 此方法覆盖了父类中的方法，实现了其功能。
     *
     * @param sender 执行操作的成员。该成员必须在群组内具有适当的权限。
     * @param group 执行操作所在的群组。群组提供了操作发生的上下文环境。
     * <p>
     * 前置条件：已检查发送者的权限，确保他们有权执行此操作。
     * 后置条件：向群组发送预定义的消息，通知 "fuckSomebody" 操作已被执行。
     * <p>
     * 断言：检查发送者是否在群组中具有必要的权限，否则将抛出 AssertionError。
     * 使用场景：具有适当权限的成员可以使用此方法来通知群组特定的操作。
     */
    @Override
    public void fuckSomebody(Member sender, Group group) {
        // 检查发送者是否在群组中具有必要的权限，否则将抛出 AssertionError。
        assert Permission.checkPermission(sender.getId(), group.getId());

        // 获取预定义的 "fuckSomebody" 消息。
        String msg = getFuckSomebody();

        // 向群组发送消息，通知操作已被执行。
        group.sendMessage(msg);
    }

    @Override
    public void randomEmoji(long id, Group group) {
        assert Permission.checkPermission(id, group.getId());
        byte[] data = null;
        try {
            switch (EmojiType.randomEmoji()) {
                case "anime":
                    data = ApiRequester.getPhoto(LolimiApiConstant.RANDOM_EMOJI_API + "?type=动漫表情");
                    break;
                case "cheshire":
                    data = ApiRequester.getPhoto(LolimiApiConstant.CHESHIRE_API);
                    break;
                case "chiikawa":
                    data = ApiRequester.getPhoto(LolimiApiConstant.RANDOM_EMOJI_API + "?type=小八嘎");
                    break;
                case "long":
                    if (Permission.checkLongturn(group.getId())) {
                        data = ApiRequester.getPhoto(LolimiApiConstant.LONG_API);
                    }
            }

        } catch (IOException e) {
            throw new RequestException(MsgConstant.RANDOM_EMOJI_REQUEST_ERROR);
        }

        if (data != null) {
            Image image = group.uploadImage(ExternalResource.create(data));
            group.sendMessage(image);
        }
    }


    /**
     * 向指定群组发送一个美丽的女孩的短视频。
     * <p>
     * 此方法首先验证发送者的权限，确保他们有权限发送视频，并且没有重复发送相同的视频。
     * 然后，方法会保存视频文件、提取视频及缩略图，并将它们上传到群组中。
     * 最后，方法会通知群组成员，视频已经成功上传。
     *
     * @param id 发送者的身份标识。
     * @param group 目标群组。
     */
    @Override
    public void beautifulGirl(long id, Group group) {
        // 验证发送者是否有权限向该群组发送视频
        assert Permission.checkPermission(id, group.getId());
        // 验证是否已经发送过同样的美丽女孩视频
        if (DuplicatePermission.checkBeautifulGirlPermission(id)) {
            // 保存美丽的女孩视频到本地文件
            File videoFile = saveBGVideo();
            // 从文件中提取视频内容
            byte[] video = getBGVideo(videoFile);
            // 从文件中提取视频缩略图
            byte[] thumbnail = getBGThumbnail(videoFile);

            // 向群组发送消息，通知成员视频正在获取中
            group.sendMessage("正在获取，请稍后...");

            // 将视频和缩略图上传到群组，创建一个短视频对象
            ShortVideo shortVideo = group.uploadShortVideo(
                    ExternalResource.create(Objects.requireNonNull(thumbnail)),
                    ExternalResource.create(Objects.requireNonNull(video)),
                    videoFile.getName());

            // 向群组发送上传成功的短视频
            group.sendMessage(shortVideo);
        } else {
            group.sendMessage(MsgConstant.BEAUTIFUL_GIRL_DUPLICATE_PERMISSION_ERROR);
        }
    }


    /**
     * 发送地狱笑话到指定群组。
     * <p>
     * 此方法重写了父类的darkJoke方法，专门用于向特定群组发送暗黑笑话。
     * 在发送之前，会检查调用者是否有权限在该群组中发送消息。
     *
     * @param id       发送者ID，用于权限检查。
     * @param group    目标群组对象，用于获取群组ID并进行权限检查。
     * @throws RequestException 如果请求暗黑笑话时发生IO异常，则抛出此异常。
     */
    @Override
    public void darkJoke(long id, Group group) {
        assert Permission.checkPermission(id, group.getId());
        String response;
        try {
            response = Objects.requireNonNull(ApiRequester.getDarkJoke().body()).string();
        } catch (IOException e) {
            // 如果在请求过程中发生IO异常，抛出自定义的RequestException异常。
            throw new RequestException(MsgConstant.JOKE_REQUEST_ERROR);
        }

        // 处理API响应，将其转换为JokeResponse对象。
        JokeResponse jokeResponse = processResponse(response);

        handleJokeResponse(jokeResponse, group);
    }

    /**
     * 根据一周内单词出现的次数生成单词统计信息。
     * <p>
     * 此方法接收一个映射，其中包含单词和它们在一周期间的出现次数，然后生成一个WordStatisticsDTO对象，
     * 该对象包含了出现次数最多的前十个单词及其对应的出现次数。
     *
     * @param weekCnt 一周内单词出现次数的映射，键为单词，值为出现次数。
     * @return WordStatisticsDTO 对象，包含了出现次数最多的前十个单词及其对应的出现次数。
     */
    private WordStatisticsDTO generateWordStatistics(Map<String, Integer> weekCnt) {
        // 初始化列表以存储出现次数最多的前十个单词和它们的出现次数
        List<String> top10Words = new ArrayList<>();
        List<Integer> top10Cnt = new ArrayList<>();

        // 找到次数最多的前十个词，存入对应的链表
        weekCnt.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    top10Words.add(entry.getKey());
                    top10Cnt.add(entry.getValue());
                });

        // 使用Builder模式构建WordStatisticsDTO对象，包含出现次数最多的前十个单词和它们的出现次数
        return WordStatisticsDTO.builder()
                .top10Words(top10Words)
                .top10Cnt(top10Cnt)
                .build();
    }


    /**
     * 获取背景视频数据。
     * <p>
     * 通过此方法，从指定的文件路径中读取视频数据，以字节数组的形式返回。
     * 如果在读取文件过程中发生IO异常，将抛出自定义的数据加载异常。
     *
     * @param videoFile 背景视频的文件对象，用于获取视频文件的路径。
     * @return 返回读取到的背景视频的字节数组。
     * @throws DataLoadException 如果在读取视频文件时发生IO异常，则抛出此异常。
     */
    private byte[] getBGVideo(File videoFile) {
        try {
            // 尝试从文件路径中读取字节数据。
            return FileManager.readByteFile(videoFile.getPath());
        } catch (IOException e) {
            // 在发生IO异常时，抛出自定义的数据加载异常。
            throw new DataLoadException(MsgConstant.BEAUTIFUL_GIRL_VIDEO_GET_ERROR);
        }
    }


    /**
     * 下载并保存美女视频。
     * <p>
     * 该方法尝试从一个远程API获取一段美女视频，并将其保存到应用的数据目录中。
     * 如果保存成功，方法将返回保存视频的文件对象；如果保存失败，将抛出一个请求异常。
     *
     * @return File 保存视频的文件对象。
     * @throws RequestException 如果保存视频时发生IO异常。
     */
    private File saveBGVideo() {
        try {
            // 从API请求美女视频数据
            byte[] video = ApiRequester.getBeautifulGirlVideo();
            File videoDir = new File(DataPathInfo.BEAUTIFUL_GIRL_VIDEO_PATH);

            if (!videoDir.exists()) {
                boolean isSuccess = videoDir.mkdirs();
                if (!isSuccess) {
                    throw new DataLoadException(MsgConstant.BEAUTIFUL_GIRL_VIDEO_SAVE_ERROR);
                }
            }
            // 生成一个唯一的文件名，以避免文件覆盖
            File videoFile = new File(videoDir, UUID.randomUUID() + ".mp4");

            // 将视频数据保存到文件系统中
            FileManager.saveFile(video, videoFile);
            return videoFile;
        } catch (IOException e) {
            // 如果在保存过程中发生IO异常，抛出一个自定义的请求异常
            throw new RequestException(MsgConstant.BEAUTIFUL_GIRL_VIDEO_SAVE_ERROR);
        }
    }

    /**
     * 通过 API 请求获取 "fuck somebody" 消息。
     * <p>
     * 此方法封装了请求特定消息的逻辑，从外部服务获取信息，并将成功的结果转换为字符串格式。如果请求失败，
     * 则会抛出一个自定义异常来通知调用者发生了错误。
     */
    private static String getFuckSomebody() {
        try {
            // 向 API 发起请求以获取 "fuck somebody" 消息
            return ApiRequester.getFuckSomebody();
        } catch (IOException e) {
            // 如果在请求过程中发生 IOException，抛出自定义 RequestException
            throw new RequestException(MsgConstant.FUCK_SOMEBODY_REQUEST_ERROR);
        }
    }


    /**
     * 获取视频的背景缩略图。
     * <p>
     * 该方法尝试从指定的视频文件中提取一帧作为背景缩略图。它首先创建一个临时文件来存储提取的帧，
     * 然后将该帧转换为字节数组以供使用，最后删除临时文件。
     * 如果在提取帧的过程中发生I/O错误，将抛出一个自定义的异常。
     *
     * @param videoFile 视频文件，从中提取背景缩略图。
     * @return 背景缩略图的字节数组。
     * @throws DataLoadException 如果提取视频帧时发生I/O错误，则抛出此异常。
     */
    private byte[] getBGThumbnail(File videoFile) {
        try {
            // 创建一个临时文件来存储视频的背景缩略图。
            File tempThumbFile = new File(DataPathInfo.CONFIG_DIR_PATH, "tempThumb.jpg");
            // 调用VideoThumbTaker类的方法提取视频的第一帧并保存到临时文件中。
            VideoThumbTaker.fetchFrame(videoFile.getPath(), tempThumbFile.getPath());
            // 读取临时文件中的缩略图数据到字节数组。
            byte[] imgData = FileManager.readByteFile(tempThumbFile.getPath());
            // 删除不再需要的临时文件。
            if (tempThumbFile.exists()) {
                boolean delete = tempThumbFile.delete();
                if (!delete) {
                    throw new DataWriteException(MsgConstant.TEMP_THUMB_DELETE_ERROR);
                }
            }
            // 返回缩略图的字节数组。
            return imgData;
        } catch (IOException e) {
            // 如果发生I/O错误，抛出自定义的异常。
            throw new DataLoadException(MsgConstant.VIDEO_THUMB_TAKE_ERROR);
        }
    }

    /**
     * 处理笑话响应。
     * <p>
     * 根据笑话响应的类型，分别处理单个笑话响应和两部分笑话响应。
     *
     * @param jokeResponse 笑话响应对象，可能是一个单个笑话响应或两部分笑话响应。
     * @param group 相关的群组信息，用于上下文处理。
     */
    private void handleJokeResponse(JokeResponse jokeResponse, Group group) {
        if (jokeResponse instanceof SingleResponse) {
            // 如果是单个笑话响应，进行单个笑话的处理
            SingleResponse singleResponse = (SingleResponse) jokeResponse;
            handleSingleJoke(group, singleResponse);
        } else if (jokeResponse instanceof TwoPartResponse) {
            // 如果是两部分笑话响应，进行两部分笑话的处理
            TwoPartResponse twoPartResponse = (TwoPartResponse) jokeResponse;
            handleTwoPartJoke(group, twoPartResponse);
        }
    }


    /**
     * 处理单个笑话的响应。
     * <p>
     * 此方法用于解析单个笑话的响应对象，并通过消息链构建器将笑话文本组装成消息，
     * 最后将消息发送到指定的群组中。
     * </p>
     * @param group 目标群组对象，用于指定发送消息的群组。
     * @param jokeResponse 单个笑话的响应对象，包含获取到的笑话内容。
     */
    private void handleSingleJoke(Group group, SingleResponse jokeResponse) {
        MessageChainBuilder chain = new MessageChainBuilder();
        chain.append(new PlainText(jokeResponse.getJoke()));
        group.sendMessage(chain.build());
    }

    /**
     * 处理两部分笑话。
     * <p>
     * 这个方法用于组装并发送一个包含两部分的笑话：设置部分和交付部分。
     * 使用MessageChainBuilder来构建消息链，以便在群组中以分段形式发送笑话。
     *
     * @param group        笑话将被发送到的群组。
     * @param twoPartResponse 包含笑话设置和交付的两部分响应对象。
     */
    private void handleTwoPartJoke(Group group, TwoPartResponse twoPartResponse) {
        MessageChainBuilder chain = new MessageChainBuilder();
        chain.append(new PlainText(twoPartResponse.getSetup() + "\n"));
        chain.append(new PlainText(twoPartResponse.getDelivery()));
        group.sendMessage(chain.build());
    }


    /**
     * 根据响应内容处理笑话响应。
     *
     * @param response 从服务器接收到的笑话响应字符串。
     * @return 解析后的笑话响应对象，可能是单个笑话响应或两部分笑话响应。
     * @throws IllegalArgumentException 如果响应中的笑话类型无效。
     * @throws RequestException 如果响应解析过程中发生JSON解析错误。
     */
    private JokeResponse processResponse(String response) {
        try {
            // 使用GSON从字符串解析出JsonObject。
            JsonObject jsonObject = GSON.fromJson(response, JsonObject.class);
            // 提取JsonObject中的"type"字段，用于判断笑话类型。
            String type = jsonObject.get("type").getAsString();
            // 根据"type"字段值，转换为对应的笑话类型枚举。
            JokeType jokeType = JokeType.fromType(type);

            // 根据笑话类型，解析相应的笑话响应对象。
            switch (jokeType) {
                case SINGLE:
                    // 如果是单个笑话，解析为SingleResponse对象。
                    return GSON.fromJson(response, SingleResponse.class);
                case TWO_PART:
                    // 如果是两部分笑话，解析为TwoPartResponse对象。
                    return GSON.fromJson(response, TwoPartResponse.class);
                default:
                    // 如果类型无效，抛出异常。
                    throw new IllegalArgumentException("Invalid joke type: " + type);
            }
        } catch (JSONException e) {
            // 如果解析过程中发生JSON异常，抛出请求异常。
            throw new RequestException(MsgConstant.JOKE_PARSE_ERROR);
        }
    }


    /**
     * 发送消息到群组中。
     * <p>
     * 使用MessageChainBuilder构建消息链，其中包括一段文本消息和一张图片消息。
     * 这个方法展示了如何通过链式操作构造复杂的消息并发送到指定的群组中。
     *
     * @param chain MessageChainBuilder实例，用于构建消息链。
     * @param group 目标群组，接收消息的群组对象。
     * @param image 图片消息，待发送的图片对象。
     */
    private void sendMsg(MessageChainBuilder chain, Group group, Image image) {
        // 向消息链中添加一段文本消息，内容为"正在统计分析..."
        chain.append(new PlainText("正在统计分析..."));
        // 发送构建好的消息链到群组中
        group.sendMessage(chain.build());
        // 发送图片消息到群组中
        group.sendMessage(image);
    }

    /**
     * 向指定群组发送消息。
     * @param at 指定的消息@对象，标识消息的接收者。
     * @param group 消息发送的目标群组。
     * @param chain 消息构建器，用于组装消息内容。
     * @param content 要发送的消息内容。
     */
    private void sendMsg(MessageChain at, Group group, MessageChainBuilder chain, String content) {
        // 将消息@对象添加到消息链中
        chain.add(at);
        // 在消息链后添加一个空格，为消息内容做分隔
        chain.append(" ");
        // 添加消息内容到消息链
        chain.append(new PlainText(content));
        // 构建消息并发送到指定群组
        group.sendMessage(chain.build());
    }

    /**
     * 发送本周本群热词排行榜消息。
     * <p>
     * 通过解析WordStatisticsDTO对象中的热词和次数数据，构建并发送包含热词排行榜信息的消息。
     * 使用MessageChainBuilder逐步构建消息内容，首先添加排行榜标题，然后遍历前10个热词及其次数，最后添加额外的结果信息。
     * 此方法专用于向指定群组发送热词排行榜消息，是群组管理功能的一部分。
     *
     * @param dto WordStatisticsDTO对象，包含热词和对应次数的数据。
     * @param group 目标群组，用于指定消息的发送对象。
     * @param chain MessageChainBuilder实例，用于构建消息内容。
     * @param result 额外的结果信息，将被追加到消息末尾。
     */
    private void sendMsg(WordStatisticsDTO dto, Group group, MessageChainBuilder chain, String result) {
        // 添加排行榜标题
        chain.append(new PlainText("本周本群热词排行榜为：\n"));

        // 遍历前10个热词及其次数，构建并添加到消息链
        for (int i = 0; i < 10; i++) {
            chain.append(new PlainText(String.format("%d. %s 次数：%d \n",
                    i + 1, dto.getTop10Words().get(i), dto.getTop10Cnt().get(i))));
        }

        // 添加额外的结果信息
        chain.append(new PlainText(result));

        // 发送构建完成的消息
        group.sendMessage(chain.build());
    }



    /**
     * 获取词云图像的字节数据
     * <p>
     * 此方法尝试从指定的文件路径中读取词云图像文件，将其以字节数据的形式返回，
     * 如果在读取文件过程中发生IO异常，将抛出一个自定义的DataLoadException异常，
     * 异常信息指明了图像获取失败
     *
     * @param wordCloudImg 文件对象，表示待读取的词云图像文件
     * @return 词云图像的字节数据数组
     * @throws DataLoadException 如果读取图像文件时发生IO异常，则抛出此异常
     */
    private static byte[] getWordCloudImg(File wordCloudImg) {
        try {
            // 尝试根据文件路径读取图像文件。
            return FileManager.readImageFile(wordCloudImg.getPath());
        } catch (IOException e) {
            // 发生IO异常时，抛出自定义异常。
            throw new DataLoadException(MsgConstant.IMAGE_GET_ERROR);
        }
    }

    /**
     * 根据词频数据生成词云图像文件。
     * <p>
     * 此方法首先加载词频数据，然后基于这些数据生成词云对象，并最终保存词云图像。
     * 这一过程包括了数据的加载、词云的生成配置以及图像的保存。
     *
     * @param wordCount 词频数据文件，用于生成词云的基础数据。
     * @return 返回保存词云图像的文件对象。
     */
    private static File generateWordCloudImgFile(File wordCount) {
        // 加载词频数据，为词云生成准备必要的信息。
        List<WordFrequency> wordFrequencies = loadWordFrequencies(wordCount);
        // 创建词云对象，配置词云的样式、布局等。
        WordCloud wordCloud = generateWordCloud();
        // 基于加载的词频数据，构建词云对象。
        wordCloud.build(wordFrequencies);

        // 保存词云图像文件。
        return saveWordCloud(wordCloud);
    }


    /**
     * 保存词云图。
     * <p>
     * 该方法用于将给定的词云对象保存为PNG图像文件。文件名基于当前日期生成，确保唯一性。
     * 词云图保存在预定义的路径下，路径在DataPathInfo类中定义。
     *
     * @param wordCloud 词云对象，包含词云的布局和样式信息。
     * @return 返回保存后的词云图文件对象。
     */
    private static File saveWordCloud(WordCloud wordCloud) {
        // 生成基于当前日期的文件名，确保文件名的唯一性。
        String fileName = LocalDate.now() + ".png";
        // 创建词云图文件对象，指定文件保存的路径和文件名。
        File wordCloudImg = new File(DataPathInfo.WORD_CLOUD_PATH, fileName);
        // 将词云对象写入到文件中，实现词云图的保存。
        wordCloud.writeToFile(wordCloudImg.getPath());
        // 返回保存后的词云图文件对象。
        return wordCloudImg;
    }

    /**
     * 生成词云实例。
     * <p>
     * 此方法根据预定义的常量初始化并配置词云实例，包括词云的大小、内边距、颜色调色板、背景形状及字体大小缩放规则。
     * 目的是创建一个具有特定视觉效果和布局规则的词云，以便进一步处理和渲染词语数据。
     *
     * @return WordCloud 返回已配置的词云实例。
     */
    private static WordCloud generateWordCloud() {
        Dimension dimension = new Dimension(WordCloudConstant.IMAGE_WIDTH, WordCloudConstant.IMAGE_HEIGHT);
        WordCloud wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);

        ColorManager colorManager = new ColorManager();
        Color bkgColor = colorManager.generateBkgColor();
        Color[] colorPalette = colorManager.generateTextColor(bkgColor);

        wordCloud.setPadding(WordCloudConstant.PADDING);
        wordCloud.setColorPalette(new ColorPalette(colorPalette));
        wordCloud.setBackground(new CircleBackground(WordCloudConstant.BACKGROUND_RADIUS));
        wordCloud.setKumoFont(new KumoFont(WordCloudConstant.randomFont(), FontWeight.PLAIN));
        wordCloud.setFontScalar(new SqrtFontScalar(WordCloudConstant.FONT_SCALAR_MIN, WordCloudConstant.FONT_SCALAR_MAX));
        wordCloud.setBackgroundColor(bkgColor);
        return wordCloud;
    }

    /**
     * 加载单词频率列表。
     * <p>
     * 从指定的文件中加载单词及其出现的频率，用于后续的单词频率统计和处理。
     *
     * @param wordCountFile 包含单词频率数据的文件。此文件应由FrequencyFileLoader能够解析的格式定义。
     * @return 返回一个List，包含加载的每个单词及其频率的WordFrequency对象。
     * @throws DataLoadException 如果加载文件过程中发生IO异常，则抛出此异常。
     */
    private static List<WordFrequency> loadWordFrequencies(File wordCountFile) {
        // 创建FrequencyFileLoader实例，用于加载单词频率文件。
        FrequencyFileLoader frequencyFileLoader = new FrequencyFileLoader();
        try {
            TextAnalyzer.filtrateStopWords(wordCountFile);
            // 调用load方法加载单词频率文件，并返回加载的结果。
            return frequencyFileLoader.load(wordCountFile);
        } catch (IOException e) {
            // 如果在加载过程中发生IO异常，抛出自定义的DataLoadException异常。
            throw new DataLoadException(MsgConstant.WORD_FREQUENCY_LOAD_ERROR);
        }
    }

    /**
     * 根据单词统计数据对象创建聊天机器人消息。
     *
     * @param dto 单词统计数据传输对象，包含群聊中单词的排名和频率信息。
     * @return 包含生成的报告消息的列表。
     * <p>
     * 该方法首先获取机器人自身的信息，然后根据传入的DTO构建一份关于群聊中单词使用情况的报告，
     * 并将该报告作为消息添加到消息列表中。报告内容包括群聊中使用频率最高的前十個单词及其出现次数。
     */
    private static List<Message> createWordStatBotMsg(WordStatisticsDTO dto) {
        // 获取机器人信息，可能包含机器人名称、头像等，用于构建消息列表
        List<Message> messages = getBotInfo();

        StringBuilder sb = new StringBuilder();
        sb.append("以下是群聊里群友们在这一周中说的最多的前十个词语，请你生成一份可爱的报告：\n");
        for (int i = 0; i < 10; i++) {
            sb.append(String.format("%d. 词语：%s 次数：%d \n", i + 1, dto.getTop10Words().get(i), dto.getTop10Cnt().get(i)));
        }
        sb.append("报告要可爱，简短，用一段话给出一个表述即可");

        // 根据报告内容构建消息对象
        Message message = Message.builder()
                .role(AIRoleType.USER.getRole())
                .content(sb.toString())
                .build();
        // 将消息对象添加到消息列表中
        messages.add(message);

        // 返回包含报告消息的消息列表
        return messages;
    }


    /**
     * 创建评价某人机器人消息列表。
     * <p>
     * 此方法会首先尝试从指定路径读取机器人的信息，如果读取成功，将基于传入的DTO生成一条用户消息，并将其添加到机器人信息列表中。
     * 如果在读取机器人信息过程中发生IO异常，将抛出数据加载异常。
     *
     * @param dto 包含需要评价的人的信息的数据传输对象，包括nameCard、nick和历史消息。
     * @return 返回包含机器人信息和用户评价消息的列表。
     * @throws DataLoadException 如果读取机器人信息时发生IO异常。
     */
    private static List<Message> createEvaluateBotMsg(EvaluateSomebodyDTO dto) {
        List<Message> messages = getBotInfo();

        Message message = Message.builder()
                .role(AIRoleType.USER.getRole())
                .content("请你可爱天真的说出对这个人的感受，他是一个什么样的人呢，你对他有什么感觉呢，表达你的情感：" +
                        " 这是他的名片:" + dto.getNameCard() +
                        " 这是他的昵称" + dto.getNick() +
                        " 这是他最近30条发言记录：" + dto.getHistoryMsg() +
                        " 说出感受，表达你的情感，说三段话")
                .build();
        messages.add(message);
        return messages;
    }

    /**
     * 获取机器人信息。
     * <p>
     * 本方法尝试从指定的文件路径中读取机器人配置信息，并返回这些信息。
     * 如果在读取过程中发生IO异常，方法将抛出一个自定义的数据加载异常。
     *
     * @return List<Message> 包含机器人信息的列表。每个消息对象代表一个特定类型的机器人信息。
     * @throws DataLoadException 如果读取机器人信息文件失败，将抛出此异常。
     */
    private static List<Message> getBotInfo() {
        List<Message> messages;
        try {
            // 尝试从指定路径读取机器人信息
            return messages = FileManager.readBotInfo(DataPathInfo.BOT_INFO_PATH);
        } catch (IOException e) {
            // 如果读取过程中发生IO异常，抛出运行时异常
            throw new DataLoadException(MsgConstant.BOT_INFO_LOAD_ERROR);
        }
    }


    /**
     * 获取指定用户的聊天历史消息。
     *
     * @param somebody 用户ID，表示要获取聊天历史的用户。
     * @return 返回该用户的一条聊天历史消息列表。如果找不到相关消息，返回null。
     */
    private static List<String> getHistoryMessage(Long somebody, Long groupId) {
        return CacheManager.getPersonalMessageCache(
                somebody,
                groupId,
                LocalDate.now(),
                PersonalMessage.EVALUATION_HISTORY_SIZE);
    }

    /**
     * 从指定群组中查找指定QQ号的成员。
     *
     * @param group 指定的群组对象，不可为null。
     * @param somebody 需要查找的成员的 MiraiCode，以字符串形式提供，不可为null或空。
     * @return 如果找到对应QQ号的成员，则返回该成员的NormalMember对象；如果没有找到或输入无效，则返回null。
     */
    private static NormalMember getSomebody(Group group, String somebody) {
        String regex = "\\[mirai:at:(\\d+)]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(somebody);

        if (matcher.find()) {
            somebody = matcher.group(1);
        } else {
            throw new IllegalArgumentException(MsgConstant.INVALID_QQ_NUMBER);
        }

        // 将输入的字符串形式的QQ号转换为Long类型
        long somebodyQQ = Long.parseLong(somebody);
        // 通过流操作遍历群组中的所有成员，筛选出ID与输入QQ号匹配的成员，如果存在则返回第一个匹配的成员，否则返回null
        return group.getMembers().stream()
                .filter(member -> member.getId() == somebodyQQ)
                .findFirst().orElse(null);
    }

    /**
     * 根据传入的消息列表获取百度聊天机器人的响应结果。
     *
     * @param messages 用户向机器人发送的消息列表。
     * @param sender   发送请求的用户ID。
     * @return ChatResponse 从百度API获取的聊天响应对象，包含具体的响应内容。
     * @throws RequestException 如果请求过程中发生IO异常，则抛出请求异常。
     */
    private ChatResponse getResponse(List<Message> messages, Long sender) {
        Response originalResponse;
        String json;
        try {
            // 向百度API发送请求并获取原始响应
            originalResponse = ApiRequester.sendBaiduRequest(messages, sender);
            // 将原始响应的主体内容转换为字符串
            json = originalResponse.body().string();
        } catch (IOException e) {
            // 若发生IO异常，抛出自定义的请求异常
            throw new RequestException(MsgConstant.REQUEST_ERROR);
        }

        // 使用Gson将JSON字符串解析为BaiduChatResponse对象
        return GSON.fromJson(json, ChatResponse.class);
    }


    /**
     * 读取答案之书并返回其内容。
     * 该方法不接受参数，但会尝试从指定路径读取答案文件。
     * 如果读取成功，将返回一个包含答案的Map；如果读取失败或答案为空，则抛出AnswerBookException异常。
     *
     * @return Map<Integer, String> 包含答案的Map，其中键为问题编号，值为对应问题的答案。
     * @throws DataLoadException 如果答案文件无法读取或为空，则抛出此异常。
     */
    private Map<Integer, String> readAnswerBook() {
        Map<Integer, String> answerBook;
        // 尝试从指定路径读取答案文件
        try {
            answerBook = FileManager.readAnswerBook(DataPathInfo.ANSWER_BOOK_PATH);
        } catch (IOException e) {
            // 当读取答案文件发生IO异常时，抛出答案书加载错误异常
            throw new DataLoadException(MsgConstant.ANSWER_BOOK_LOAD_ERROR);
        }
        // 如果读取到的答案书为空，则抛出答案书加载错误异常
        if (answerBook.isEmpty()) {
            throw new DataLoadException(MsgConstant.ANSWER_BOOK_LOAD_ERROR);
        }
        return answerBook;
    }

    /**
     * 检查发送者的权限。
     *
     * @param sender 消息发送者的ID，类型为Long。
     * @param group 消息所属的群组，类型为Group。
     * @return 如果发送者的权限符合要求，则返回true；否则返回false。
     */
    private boolean checkPermission(Long sender, Group group) {
        return Permission.checkPermission(sender, group.getId());
    }
}
