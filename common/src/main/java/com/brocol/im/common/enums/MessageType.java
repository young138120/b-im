package com.brocol.im.common.enums;

/**
 * 消息类型
 *
 * @author Brocol
 * @date 2024/3/15
 * @description
 */
public enum MessageType {

    /**
     * 基础服务本身状态： [10000,20000)
     */
    HEARTBEAT(10000);

    /**
     * 业务消息类型: [20000,+无穷大)
     */


    private int typeNum;

    MessageType(int i) {
    }
}
