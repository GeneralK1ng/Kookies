package org.kookies.mirai.pojo.entity.api.request.baidu.ai;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class ChatRequestBody implements Serializable {

    /*  聊天上下文信息。说明：
        （1）messages成员不能为空，1个成员表示单轮对话，多个成员表示多轮对话
        （2）最后一个message为当前请求的信息，前面的message为历史对话信息
        （3）必须为奇数个成员，成员中message的role必须依次为user、assistant
        （4）message中的content总长度不能超过8000 个字符
    */
    private List<Message> messages;

    // 是否以流式接口的形式返回数据，默认false
    private boolean stream;

    /*
    * 说明：
        （1）较高的数值会使输出更加随机，而较低的数值会使其更加集中和确定
        （2）范围 (0, 1.0]，不能为0
    */
    private float temperature;

    /*  Top-K 采样参数，在每轮token生成时，保留k个概率最高的token作为候选。说明：
        （1）影响输出文本的多样性，取值越大，生成文本的多样性越强
        （2）取值范围：正整数
    */
    //private Integer top_k;

    /*  说明：
        （1）影响输出文本的多样性，取值越大，生成文本的多样性越强
        （2）取值范围 [0, 1.0]
    */
    private float top_p;

    /*通过对已生成的token增加惩罚，减少重复生成的现象。说明：
        （1）值越大表示惩罚越大
        （2）取值范围：[1.0, 2.0]
    */
    private float penalty_score;

    private List<String> stop;

    private String user_id;
}
