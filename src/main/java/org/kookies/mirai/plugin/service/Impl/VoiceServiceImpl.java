package org.kookies.mirai.plugin.service.Impl;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.OfflineAudio;
import net.mamoe.mirai.utils.ExternalResource;
import org.kookies.mirai.commen.constant.MsgConstant;
import org.kookies.mirai.commen.exceptions.RequestException;
import org.kookies.mirai.commen.utils.ApiRequester;
import org.kookies.mirai.commen.utils.FormatConverter;
import org.kookies.mirai.plugin.auth.Permission;
import org.kookies.mirai.plugin.service.VoiceService;

import java.io.IOException;

public class VoiceServiceImpl implements VoiceService {
    /**
     * 对指定ID的消息内容进行语音合成并发送给群组。
     * <p>
     * 此方法首先检查消息发送者是否有权限在指定的群组中发送消息。
     * 如果有权限，它将消息内容转换为语音，并将语音文件上传到群组的离线语音资源中，
     * 最后将合成的语音发送到群组中。
     *
     * @param id 消息发送者的ID。
     * @param group 目标群组对象。
     * @param content 消息内容。
     */
    @Override
    public void say(long id, Group group, String content) {
        // 检查发送者是否有权限在群组中发送消息
        if (Permission.checkPermission(id, group.getId())) {
            // 根据语音请求获取合成的语音字节数据
            byte[] voiceByte = getVoiceByte(content);
            // 将合成的语音上传到群组的离线语音资源中
            OfflineAudio offlineAudio = group.uploadAudio(ExternalResource.create(voiceByte));
            // 发送合成的语音到群组中
            sendVoice(group, offlineAudio);
        }
    }


    /**
     * 在指定的群组中发送离线音频消息。
     * <p>
     * 本函数通过构建一个消息链，将离线音频消息添加到链中，然后通过群组发送此消息链。
     * 这种方式适用于在机器人或其他需要批量发送音频消息的场景中。
     *
     * @param group 目标群组，音频消息将发送到这个群组中。
     * @param offlineAudio 离线音频消息对象，包含音频的具体信息。
     */
    private void sendVoice(Group group, OfflineAudio offlineAudio) {
        // 创建一个消息链构建器。
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        // 将离线音频消息添加到消息链中。
        messageChainBuilder.append(offlineAudio);
        // 发送构建好的消息链到指定的群组中。
        group.sendMessage(messageChainBuilder.asMessageChain());
    }


    /**
     * 将文本转换为语音字节数据。
     *
     * 本函数通过调用外部API将文本内容转换为WAV格式的语音数据，然后将WAV格式转换为AMR格式，
     * 以得到更小的文件大小和更高的传输效率。如果转换过程中发生I/O错误，将抛出自定义的RequestException异常。
     *
     * @param content 待转换的文本内容。
     * @return 转换后的AMR格式语音的字节数据。
     * @throws RequestException 如果转换过程中发生I/O错误。
     */
    private byte[] getVoiceByte(String content) {
        try {
            // 调用外部API获取文本对应的WAV格式语音数据
            byte[] wavByte = ApiRequester.getVoiceWithText(content);

            // 将获取的WAV格式语音数据转换为AMR格式
            return FormatConverter.convertWavToAmr(wavByte);
        } catch (IOException e) {
            // 捕获转换过程中可能发生的I/O异常，抛出自定义异常
            throw new RequestException(MsgConstant.VOICE_REQUEST_ERROR);
        }
    }


}
