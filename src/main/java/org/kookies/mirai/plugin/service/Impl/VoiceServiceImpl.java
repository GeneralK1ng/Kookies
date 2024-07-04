package org.kookies.mirai.plugin.service.Impl;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.OfflineAudio;
import net.mamoe.mirai.utils.ExternalResource;
import org.kookies.mirai.commen.constant.MsgConstant;
import org.kookies.mirai.commen.constant.VoiceApiConstant;
import org.kookies.mirai.commen.enumeration.VoiceRoleType;
import org.kookies.mirai.commen.exceptions.RequestException;
import org.kookies.mirai.commen.utils.ApiRequester;
import org.kookies.mirai.commen.utils.FormatConverter;
import org.kookies.mirai.plugin.auth.Permission;
import org.kookies.mirai.plugin.service.VoiceService;
import org.kookies.mirai.pojo.entity.VoiceRole;

import java.io.IOException;

/**
 * @author General_K1ng
 */
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

            VoiceRole voiceRole = VoiceRoleType.getRoleByName(VoiceApiConstant.DEFAULT_ROLE);

            byte[] voiceByte = getVoiceByte(content, voiceRole);
            // 将合成的语音上传到群组的离线语音资源中
            OfflineAudio offlineAudio = group.uploadAudio(ExternalResource.create(voiceByte));
            // 发送合成的语音到群组中
            sendVoice(group, offlineAudio);
        }
    }

    /**
     * 根据名称获取语音角色。
     * <p>
     * 本方法通过名称从预定义的语音角色列表中查找并返回相应的语音角色对象。
     * 如果找不到匹配的名称，则返回null或默认角色。
     *
     * @param name 角色的名称，用于查找对应的语音角色。
     * @return 对应的语音角色对象，如果找不到则返回null或默认角色。
     */
    @Override
    public VoiceRole getVoiceRole(String name) {
        // 通过名称获取语音角色实现
        return VoiceRoleType.getRoleByName(name);
    }

    /**
     * 对指定的群组和用户说某句话，可能会根据用户的角色播放不同的语音。
     * <p>
     * 如果用户没有特定的语音角色，则默认处理；如果用户有权限，则根据内容和角色生成语音并播放。
     *
     * @param id 用户ID，用于检查权限和生成语音。
     * @param group 目标群组，用于上传和播放语音。
     * @param voiceRole 用户的语音角色，决定语音的生成方式。
     * @param content 说话的内容，将被转换为语音。
     */
    @Override
    public void say(long id, Group group, VoiceRole voiceRole, String content) {
        // 如果用户没有指定语音角色，则使用默认方式处理
        if (voiceRole == null) {
            say(id, group, content);
        }

        // 检查用户是否有权限在群组中播放语音
        if (Permission.checkPermission(id, group.getId())) {
            // 根据内容和语音角色生成语音数据
            byte[] voiceByte = getVoiceByte(content, voiceRole);
            // 将语音数据上传到群组的离线语音资源
            OfflineAudio offlineAudio = group.uploadAudio(ExternalResource.create(voiceByte));
            // 在群组中发送并播放上传的语音
            sendVoice(group, offlineAudio);
        }
    }


    /**
     * 在指定的群组中发送离线音频消息。
     * <p>
     * 此方法用于当需要向一个群组发送音频消息，但发送方当前处于离线状态时。
     * 它通过调用群组的sendMessage方法，将离线音频消息发送到指定的群组中。
     * </p>
     * @param group 目标群组，音频消息将发送到这个群组中。
     * @param offlineAudio 离线音频消息对象，包含音频文件的相关信息和数据。
     */
    private void sendVoice(Group group, OfflineAudio offlineAudio) {
        group.sendMessage(offlineAudio);
    }


    /**
     * 将文本转换为语音字节数据。
     * <p>
     * 本函数通过调用外部API将文本内容转换为WAV格式的语音数据，然后将WAV格式转换为AMR格式，
     * 以得到更小的文件大小和更高的传输效率。如果转换过程中发生I/O错误，将抛出自定义的RequestException异常。
     *
     * @param content 待转换的文本内容。
     * @return 转换后的AMR格式语音的字节数据。
     * @throws RequestException 如果转换过程中发生I/O错误。
     */
    private byte[] getVoiceByte(String content, VoiceRole voiceRole) {
        try {
            // 调用外部API获取文本对应的WAV格式语音数据
            byte[] wavByte = ApiRequester.getVoiceWithText(content, voiceRole);

            // 将获取的WAV格式语音数据转换为AMR格式
            return FormatConverter.convertWavToAmr(wavByte);
        } catch (IOException e) {
            // 捕获转换过程中可能发生的I/O异常，抛出自定义异常
            throw new RequestException(MsgConstant.VOICE_REQUEST_ERROR);
        }
    }


}
