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
import org.kookies.mirai.commen.enumeration.CodeLanguageType;
import org.kookies.mirai.commen.exceptions.SchedulerJobException;
import org.kookies.mirai.commen.info.AuthorInfo;
import org.kookies.mirai.commen.info.FunctionInfo;
import org.kookies.mirai.commen.utils.CacheManager;
import org.kookies.mirai.commen.utils.JobScheduler;
import org.kookies.mirai.commen.utils.ProbabilityTrigger;
import org.kookies.mirai.plugin.service.*;
import org.kookies.mirai.plugin.service.Impl.*;
import org.kookies.mirai.pojo.entity.VoiceRole;


/**
 * @author General_K1ng
 */
public final class Kookie extends JavaPlugin {
    public static final Kookie INSTANCE = new Kookie();
    private static final Log log = LogFactory.getLog(Kookie.class);

    // 娱乐功能
    private final EntertainmentService entertainmentService = new EntertainmentServiceImpl();

    // 签到接口
    private final SignInService signInService = new SignInServiceImpl();

    // 便利功能接口
    private final ConvenienceService convenienceService = new ConvenienceServiceImpl();

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
         log.info("Kookie V" + AuthorInfo.VERSION +" 加载完成！");

        // 加载配置文件并初始化定时任务
        try {
            ConfigurationLoader.init();
            JobScheduler.start();
            System.setProperty("java.awt.headless", "true");
            System.setProperty("file.encoding", "UTF-8");
        } catch (SchedulerJobException e) {
            getLogger().error(MsgConstant.SCHEDULER_EXCEPTION, e);
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

            // at me [mirai:at:111111111]


            String content = "";
            if (!msg.serializeToMiraiCode().startsWith("[mirai:")) {
                content = msg.contentToString();
            }

            CacheManager.setCache(sender.getId(), group.getId(), content);


            if (ProbabilityTrigger.shouldTrigger(0.1)) {
                getLogger().info("随机表情, 触发者：" + userName);
                entertainmentService.randomEmoji(sender.getId(), group);

            }

            String[] msgArr = content.split(" ");

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
                    entertainmentService.answer(sender.getId(), group);
                    break;

                // 今日运势
                case FunctionInfo.LUCKY_TODAY:
                    getLogger().info("今日运势, 调用者：" + userName);
                    signInService.luckyDay(sender.getId(), group);
                    break;

                // 评价一下
                case FunctionInfo.EVALUATE_SOMEBODY:
                    String somebody = msg.serializeToMiraiCode().split(" ")[1];
                    //getLogger().info(somebody);
                    getLogger().info("评价一下, 调用者：" + userName + " 被评价者：" + somebody);
                    entertainmentService.evaluateSomebody(sender, group, somebody);
                    break;

                // 吃什么
                case FunctionInfo.EAT_WHAT:
                    getLogger().info("吃什么, 调用者：" + userName);
                    if (msgArr.length == 2) {
                        convenienceService.eatWhat(sender.getId(), group, msgArr[1], null);
                    } else if (msgArr.length == 3){
                        convenienceService.eatWhat(sender.getId(), group, msgArr[1], msgArr[2]);
                    }
                    break;

                // 语音模块
                case FunctionInfo.VOICE_SAY:
                    getLogger().info("语音模块, 调用者：" + userName);
                    voiceService.say(sender.getId(), group, msgArr[1]);
                    break;

                // 今日老婆
                case FunctionInfo.TODAY_GIRL_FRIEND:
                    getLogger().info("今日老婆, 调用者：" + userName);
                    signInService.todayGirlFriend(sender.getId(), group);
                    break;

                // 代码运行
                case FunctionInfo.CODE_RUN:
                    getLogger().info("代码运行, 调用者：" + userName + "，语言：" + msgArr[1]);
                    // 第二个空格后的所有字符串
                    String code = msg.contentToString().substring(msg.contentToString().indexOf(" ", 5) + 1);
                    String lang = CodeLanguageType.getLanguageByName(msgArr[1]);
                    convenienceService.codeRun(sender.getId(), group, code, lang);
                    break;

                // 今日词云
                case FunctionInfo.TODAY_WORD:
                    getLogger().info("今日词云, 调用者：" + userName);
                    entertainmentService.todayWord(sender.getId(), group);
                    break;

                // 昨日词云
                case FunctionInfo.YESTERDAY_WORD:
                    getLogger().info("昨日词云, 调用者：" + userName);
                    entertainmentService.yesterdayWord(sender.getId(), group);
                    break;

                // 词频统计
                case FunctionInfo.WORD_STATISTICS:
                    getLogger().info("词频统计, 调用者：" + userName);
                    entertainmentService.wordStatistics(sender.getId(), group);
                    break;

                // 地狱笑话
                case FunctionInfo.DARK_JOKE:
                    getLogger().info("地狱笑话, 调用者：" + userName);
                    entertainmentService.darkJoke(sender.getId(), group);
                    break;

                // 美女
                case FunctionInfo.BEAUTIFUL_GIRL:
                    getLogger().info("先打胶吧, 调用者：" + userName);
                    entertainmentService.beautifulGirl(sender.getId(), group);
                    break;

                // 本周词云
                case FunctionInfo.WEEK_WORD:
                    getLogger().info("本周词云, 调用者：" + userName);
                    entertainmentService.weekWord(sender.getId(), group);
                    break;

                // 摸鱼日报
                case FunctionInfo.MESS_AROUND_DAILY:
                    getLogger().info("摸鱼日报, 调用者：" + userName);
                    signInService.messAroundDaily(sender.getId(), group);
                    break;
                // 奥运日报
//                case FunctionInfo.OLYMPIC_DAILY:
//                    getLogger().info("奥运日报, 调用者：" + userName);
//                    convenienceService.olympicDaily(sender.getId(), group);
//                    break;
            }
        });

        eventChannel.subscribeAlways(FriendMessageEvent.class, f -> {
            // 监听好友消息
            getLogger().info(f.getMessage().contentToString());
        });

        // TODO 敏感词检测的群管理功能

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
