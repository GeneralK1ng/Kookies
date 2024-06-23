package org.kookies.mirai.plugin.service.Impl;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;
import org.kookies.mirai.commen.constant.MsgConstant;
import org.kookies.mirai.commen.exceptions.DataLoadException;
import org.kookies.mirai.commen.info.DataPathInfo;
import org.kookies.mirai.commen.utils.FileManager;
import org.kookies.mirai.plugin.auth.Permission;
import org.kookies.mirai.plugin.service.EntertainmentService;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

public class EntertainmentServiceImpl implements EntertainmentService {
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
     * 向指定群组发送消息。
     * @param at 指定的消息@对象，标识消息的接收者。
     * @param group 消息发送的目标群组。
     * @param chain 消息构建器，用于组装消息内容。
     * @param answer 要发送的消息内容。
     */
    private void sendMsg(MessageChain at, Group group, MessageChainBuilder chain, String answer) {
        // 将消息@对象添加到消息链中
        chain.add(at);
        // 在消息链后添加一个空格，为消息内容做分隔
        chain.append(" ");
        // 添加消息内容到消息链
        chain.append(new PlainText(answer));
        // 构建消息并发送到指定群组
        group.sendMessage(chain.build());
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
