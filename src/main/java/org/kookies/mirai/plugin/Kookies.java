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
import org.kookies.mirai.plugin.service.AnswerBookService;
import org.kookies.mirai.plugin.service.EvaluationService;
import org.kookies.mirai.plugin.service.Impl.AnswerBookServiceImpl;
import org.kookies.mirai.plugin.service.Impl.EvaluationServiceImpl;
import org.kookies.mirai.plugin.service.Impl.LuckyDayServiceImpl;
import org.kookies.mirai.plugin.service.LuckyDayService;
import org.kookies.mirai.pojo.entity.MessageCache;


public final class Kookies extends JavaPlugin {
    public static final Kookies INSTANCE = new Kookies();
    private static final Log log = LogFactory.getLog(Kookies.class);

    // 答案之书
    private final AnswerBookService answerBookService = new AnswerBookServiceImpl();

    // 今日运势
    private final LuckyDayService luckyDayService = new LuckyDayServiceImpl();

    // 评价一下
    private final EvaluationService evaluationService = new EvaluationServiceImpl();

    private Kookies() {
        super(new JvmPluginDescriptionBuilder(AuthorInfo.ID, AuthorInfo.VERSION)
                .info(AuthorInfo.INFO)
                .author(AuthorInfo.AUTHOR)
                .build());
    }

    @Override
    public void onEnable() {
        getLogger().info("Kookie 加载完成！");

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

            switch (msgArr[0]){
                // 答案之书
                case FunctionInfo.ANSWER_BOOK:
                    answerBookService.answer(sender.getId(), group);
                    break;
                // 今日运势
                case FunctionInfo.LUCKY_TODAY:
                    luckyDayService.luckyDay(sender.getId(), group);
                    break;
                case FunctionInfo.EVALUATE_SOMEBODY:
                    String somebody = msg.serializeToMiraiCode().split(" ")[1];
                    getLogger().info(somebody);
                    evaluationService.evaluateSomebody(sender, group, somebody);
                    break;
            }

        });

        eventChannel.subscribeAlways(FriendMessageEvent.class, f -> {
            // 监听好友消息
            getLogger().info(f.getMessage().contentToString());
        });

    }

}
