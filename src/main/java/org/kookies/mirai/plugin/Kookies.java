package org.kookies.mirai.plugin;

import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.contact.ContactList;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.BotOnlineEvent;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.MessageSource;
import org.kookies.mirai.commen.config.ConfigurationLoader;
import org.kookies.mirai.commen.constant.MsgConstant;
import org.kookies.mirai.commen.info.AuthorInfo;
import org.kookies.mirai.plugin.service.AnswerBookService;
import org.kookies.mirai.plugin.service.Impl.AnswerBookServiceImpl;


public final class Kookies extends JavaPlugin {
    public static final Kookies INSTANCE = new Kookies();

    private final AnswerBookService answerBookService = new AnswerBookServiceImpl();

    private Kookies() {
        super(new JvmPluginDescriptionBuilder(AuthorInfo.ID, AuthorInfo.VERSION)
                .info(AuthorInfo.INFO)
                .author(AuthorInfo.AUTHOR)
                .build());
    }

    @Override
    public void onEnable() {
        getLogger().info("Kookie 开始加载！");

        try {
            ConfigurationLoader.init();
        } catch (Exception e) {
            getLogger().error(MsgConstant.CONFIG_LOAD_ERROR, e);
        }

        EventChannel<Event> eventChannel = GlobalEventChannel.INSTANCE.parentScope(this);
        eventChannel.subscribeAlways(GroupMessageEvent.class, g -> {
            //监听群消息
            MessageSource source = g.getSource();

            String msg = g.getMessage().contentToString();
            Long sender = g.getSender().getId();
            String userName = g.getSenderName();
            Group group = g.getGroup();

            getLogger().info(msg);

            String[] msgArr = msg.split(" ");

            switch (msgArr[0]){
                case "答案之书":
                    answerBookService.answer(sender, group);
                    break;
            }


        });
        eventChannel.subscribeAlways(FriendMessageEvent.class, f -> {
            //监听好友消息
            getLogger().info(f.getMessage().contentToString());
        });

    }

}
