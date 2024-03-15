package com.brocol.im.server.netty;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.brocol.im.common.protobuf.MessageProtobuf;
import com.brocol.im.server.connection.ConnectionManager;
import com.brocol.im.server.netty.entity.ChannelInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息中转站
 *
 * @author Brocol
 * @date 2024/3/15
 * @description
 */
public class MessageTransferHandle extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageTransferHandle.class);
    private static final Map<String, ChannelInfo> CHANNELS = new ConcurrentHashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        LOGGER.info("MessageTransferHandle channelActive():{}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        LOGGER.info("MessageTransferHandle channelInactive()");
        // 用户断开连接后，移除channel
        ConnectionManager.getInstance().removeChannelIfConnectNoActive(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        LOGGER.error("MessageTransferHandle exceptionCaught()", cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        LOGGER.error("MessageTransferHandle userEventTriggered()");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MessageProtobuf.Msg message = (MessageProtobuf.Msg) msg;
        int msgType = message.getHead().getMsgType();
        switch (msgType) {
            // 握手消息
            case 1001: {
                String sender = message.getHead().getSender();
                JSONObject jsonObj = JSON.parseObject(message.getHead().getExtend());
                String token = jsonObj.getString("token");
                JSONObject resp = new JSONObject();
                if (token.equals("token_" + sender)) {
                    resp.put("status", 1);
                    // 握手成功后，保存用户通道
                    ConnectionManager.getInstance().saveChannel(new ChannelInfo(sender, ctx.channel()));
                } else {
                    resp.put("status", -1);
                    ConnectionManager.getInstance().removeChannelIfConnectNoActive(ctx.channel());
                }

                message = message.toBuilder().setHead(message.getHead().toBuilder().setExtend(resp.toString()).build()).build();
                ConnectionManager.getInstance().getActiveChannelByUserId(sender).getChannel().writeAndFlush(message);
                break;
            }

            // 心跳消息
            case 1002: {
                // 收到心跳消息，原样返回
                String sender = message.getHead().getSender();
                ConnectionManager.getInstance().getActiveChannelByUserId(sender).getChannel().writeAndFlush(message);
                break;
            }

            case 2001: {
                // 收到2001或3001消息，返回给客户端消息发送状态报告
                String sender = message.getHead().getSender();
                MessageProtobuf.Msg.Builder sentReportMsgBuilder = MessageProtobuf.Msg.newBuilder();
                MessageProtobuf.Head.Builder sentReportHeadBuilder = MessageProtobuf.Head.newBuilder();
                sentReportHeadBuilder.setMsgId(message.getHead().getMsgId());
                sentReportHeadBuilder.setMsgType(1010);
                sentReportHeadBuilder.setTimestamp(System.currentTimeMillis());
                sentReportHeadBuilder.setReport(1);
                sentReportMsgBuilder.setHead(sentReportHeadBuilder.build());
                ConnectionManager.getInstance().getActiveChannelByUserId(sender).getChannel().writeAndFlush(sentReportMsgBuilder.build());

                // 同时转发消息到接收方
                String receiver = message.getHead().getReceiver();
                ConnectionManager.getInstance().getActiveChannelByUserId(receiver).getChannel().writeAndFlush(message);
                break;
            }
            case 3001: {
                break;
            }
            default:
                break;
        }
    }
}
