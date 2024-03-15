package com.brocol.im.server.handle;

import com.brocol.im.common.protobuf.MessageProtobuf;

/**
 * 消息处理的上层抽象
 *
 * @author Brocol
 * @date 2024/3/15
 * @description
 */
public interface MessageHandle {

    MessageProtobuf process();

}
