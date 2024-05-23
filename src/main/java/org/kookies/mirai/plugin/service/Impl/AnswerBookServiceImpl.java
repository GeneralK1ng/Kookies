package org.kookies.mirai.plugin.service.Impl;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;
import org.kookies.mirai.commen.constant.MsgConstant;
import org.kookies.mirai.commen.exceptions.AnswerBookException;
import org.kookies.mirai.commen.info.ConfigurationInfo;
import org.kookies.mirai.commen.utils.FileManager;
import org.kookies.mirai.plugin.service.AnswerBookService;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

public class AnswerBookServiceImpl implements AnswerBookService {
    @Override
    public void answer(Long sender, Group group) {
        MessageChainBuilder chain = new MessageChainBuilder();


        Random random = new Random();
        Map<Integer, String> answerBook = readAnswerBook();
        int randomIndex = random.nextInt(answerBook.size());

        MessageChain at = MiraiCode.deserializeMiraiCode("[mirai:at:" + sender + "]");

        String answer = answerBook.get(randomIndex);

        sendMsg(at, group, chain, answer);
    }

    private void sendMsg(MessageChain at, Group group, MessageChainBuilder chain, String answer) {
        chain.add(at);
        chain.append(new PlainText(answer));
        group.sendMessage(chain.build());
    }

    private Map<Integer, String> readAnswerBook() {
        Map<Integer, String> answerBook;
        // 读取答案之书
        try {
            answerBook = FileManager.readAnswerBook(ConfigurationInfo.ANSWER_BOOK_PATH);
        } catch (IOException e) {
            throw new AnswerBookException(MsgConstant.ANSWER_BOOK_LOAD_ERROR);
        }
        if (answerBook.isEmpty()) {
            throw new AnswerBookException(MsgConstant.ANSWER_BOOK_LOAD_ERROR);
        }
        return answerBook;
    }
}
