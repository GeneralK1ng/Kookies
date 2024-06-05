package org.kookies.mirai.plugin;

import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kookies.mirai.commen.config.ConfigurationLoader;
import org.kookies.mirai.commen.constant.MsgConstant;
import org.kookies.mirai.commen.info.AuthorInfo;
import org.kookies.mirai.commen.info.FunctionInfo;
import org.kookies.mirai.commen.utils.CacheManager;
import org.kookies.mirai.plugin.service.*;
import org.kookies.mirai.plugin.service.Impl.*;
import org.kookies.mirai.pojo.entity.VoiceRole;


public final class Kookie extends JavaPlugin {
    public static final Kookie INSTANCE = new Kookie();
    private static final Log log = LogFactory.getLog(Kookie.class);

    // 答案之书
    private final AnswerBookService answerBookService = new AnswerBookServiceImpl();

    // 今日运势
    private final LuckyDayService luckyDayService = new LuckyDayServiceImpl();

    // 评价一下
    private final EvaluationService evaluationService = new EvaluationServiceImpl();

    // 吃什么
    private final EatWhatService eatWhatService = new EatWhatServiceImpl();

    // 语音模块
    private final VoiceService voiceService = new VoiceServiceImpl();

    private Kookie() {
        super(new JvmPluginDescriptionBuilder(AuthorInfo.ID, AuthorInfo.VERSION)
                .info(AuthorInfo.INFO)
                .author(AuthorInfo.AUTHOR)
                .build());
    }

    @Override
    public void onEnable() {
        getLogger().info("Kookie V" + AuthorInfo.VERSION +" 加载完成！");

        // 初始化并更新配置文件
        try {
            ConfigurationLoader.init();
        } catch (Exception e) {
            getLogger().error(MsgConstant.CONFIG_LOAD_ERROR, e);
        }

        EventChannel<Event> eventChannel = GlobalEventChannel.INSTANCE.parentScope(this);
        eventChannel.subscribeAlways(GroupMessageEvent.class, g -> {
            //监听群消息
            MessageSource source = g.getSource();
            MessageChain msg = g.getMessage();
            Member sender = g.getSender();
            String userName = g.getSenderName();
            Group group = g.getGroup();

            //getLogger().info(msg);
            CacheManager.readCache(sender.getId(), msg.contentToString());

            String[] msgArr = msg.contentToString().split(" ");

            // 角色语音调用，优先判断
            if (isRoleVoiceCall(msgArr[0])) {
                // 获取名字
                String name = msgArr[0].substring(0, msgArr[0].length() - 3);
                // 获取角色对象
                VoiceRole voiceRole = voiceService.getVoiceRole(name);
                // 调用方法

                getLogger().info("角色语音调用, 调用者：" + userName + " 角色：" + name);
                voiceService.say(sender.getId(), group, voiceRole, msgArr[1]);
            }

            switch (msgArr[0]){
                // 答案之书
                case FunctionInfo.ANSWER_BOOK:
                    getLogger().info("答案之书, 调用者：" + userName);
                    answerBookService.answer(sender.getId(), group);
                    break;
                // 今日运势
                case FunctionInfo.LUCKY_TODAY:
                    getLogger().info("今日运势, 调用者：" + userName);
                    luckyDayService.luckyDay(sender.getId(), group);
                    break;
                // 评价一下
                case FunctionInfo.EVALUATE_SOMEBODY:
                    String somebody = msg.serializeToMiraiCode().split(" ")[1];
                    //getLogger().info(somebody);
                    getLogger().info("评价一下, 调用者：" + userName + " 被评价者：" + somebody);
                    evaluationService.evaluateSomebody(sender, group, somebody);
                    break;
                // 吃什么
                case FunctionInfo.EAT_WHAT:
                    getLogger().info("吃什么, 调用者：" + userName);
                    if (msgArr.length == 2) {
                        eatWhatService.eatWhat(sender.getId(), group, msgArr[1], null);
                    } else if (msgArr.length == 3){
                        eatWhatService.eatWhat(sender.getId(), group, msgArr[1], msgArr[2]);
                    }
                    break;
                // 语音模块
                case FunctionInfo.VOICE_SAY:
                    getLogger().info("语音模块, 调用者：" + userName);
                    voiceService.say(sender.getId(), group, msgArr[1]);
                    break;
            }
        });

        eventChannel.subscribeAlways(FriendMessageEvent.class, f -> {
            // 监听好友消息
            getLogger().info(f.getMessage().contentToString());
        });

    }


    /**
     * 判断消息是否为角色语音呼叫指令。
     * <p>
     * 本函数用于识别传入的消息是否符合特定的语音呼叫指令格式。具体而言，它检查消息的长度是否超过3个字符
     * 并且消息是否以预定义的语音呼叫指令结尾。这个功能的设计目的是为了在处理语音呼叫指令时提供一种
     * 简单的过滤机制，确保只有符合特定格式的消息才会被进一步处理。
     *
     * @param msg 待检查的消息字符串。
     * @return 如果消息长度超过3个字符且以语音呼叫指令结尾，则返回true；否则返回false。
     */
    private boolean isRoleVoiceCall(String msg) {
        // 检查消息长度是否超过3个字符并且是否以语音呼叫指令结尾
        return msg.length() > 3 && msg.endsWith(FunctionInfo.VOICE_SAY);
    }

}
