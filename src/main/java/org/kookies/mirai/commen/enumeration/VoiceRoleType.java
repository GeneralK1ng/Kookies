package org.kookies.mirai.commen.enumeration;

import lombok.Getter;
import org.kookies.mirai.commen.constant.MsgConstant;
import org.kookies.mirai.pojo.entity.VoiceRole;

/**
 * @author General_K1ng
 */

@Getter
public enum VoiceRoleType {
    SPARKLE(new VoiceRole("花火", "可聪明的人从一开始就不会入局。你瞧，我是不是更聪明一点？")),
    FIREFLY(new VoiceRole("流萤", "因为你身上别着星穹列车的徽章呀，我在大荧幕上见过！")),
    MARCH7TH(new VoiceRole("三月七", "名字是我自己取的，大家也叫我三月、小三月…你呢？你想叫我什么？")),
    KLEE(new VoiceRole("可莉", "买东西那天也有一个人帮了开了款式，那个人好像叫")),
    KAMISATOAYAKA(new VoiceRole("神里绫华", "这里有别于神里家的布景，移步之间，处处都有新奇感。")),
    TINGYUN(new VoiceRole("停云", "若是没个合理的解释，一旁这几位云骑大哥怕是不得不押各位一程啦。")),
    SILVERWOLF(new VoiceRole("银狼", "该做的事都做完了么？好，别睡下了才想起来日常没做，拜拜。")),
    BRONYA(new VoiceRole("布洛妮娅","我接收的命令是捉拿你们，至于详细的罪责和判罚，裁判团会向你们解释。")),
    FUXUAN(new VoiceRole("符玄","说来，下次「六御」议政，你该履行举荐我继任将军的诺言了吧……")),
    JINGLIU(new VoiceRole("镜流", "离开罗浮这么久，这府中的杀气不减反增，倒是令人欣慰。")),
    KAFKA(new VoiceRole("卡芙卡","这就是艾利欧所预见的以及你将抵达的未来…喜欢么？")),
    QINGQUE(new VoiceRole("青雀", "杨先生问的好问题，我一时半会儿也答不上来。容我想想……"));

    final VoiceRole role;

    VoiceRoleType(VoiceRole role) {
        this.role = role;
    }

    /**
     * 根据角色名称获取对应的角色对象。
     * <p>
     * 该方法遍历所有VoiceRoleType的枚举值，寻找其角色名称与输入名称匹配的角色。
     * 如果找到匹配的角色，则返回该角色对象；如果没有找到匹配的角色，则返回null。
     *
     * @param name 角色的名称，用于查找匹配的角色对象。
     * @return 匹配的角色对象，如果找不到则返回null。
     */
    public static VoiceRole getRoleByName(String name) {
        // 遍历所有的VoiceRoleType枚举值
        for (VoiceRoleType type : values()) {
            // 检查当前枚举值的角色名称是否与输入名称匹配
            if (type.role.getRole().equals(name)) {
                // 如果匹配，则返回当前枚举值的角色对象
                return type.role;
            }
        }
        // 如果没有找到匹配的角色，则返回null
        System.err.println(MsgConstant.CANT_FIND_ROLE);
        return null;
    }


}
