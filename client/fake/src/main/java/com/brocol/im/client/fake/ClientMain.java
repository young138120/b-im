package com.brocol.im.client.fake;

import com.alibaba.fastjson.JSONObject;
import com.brocol.im.common.protobuf.MessageProtobuf;
import com.google.protobuf.util.JsonFormat;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

/**
 * 测试客户端-》仅用于测试链接
 *
 * @author Brocol
 * @date 2024/3/15
 * @description
 */
public class ClientMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientMain.class);

    public static void main(String[] args) {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            /**客户端相关配置信息*/
            Bootstrap bootstrap = new Bootstrap();
            //绑定线程组
            bootstrap.group(workerGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("frameEncoder", new LengthFieldPrepender(2));
                    pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(65535,
                            0, 2, 0, 2));
                    pipeline.addLast(new ProtobufDecoder(MessageProtobuf.Msg.getDefaultInstance()));
                    pipeline.addLast(new ProtobufEncoder());
                    //处理类
                    pipeline.addLast(new ClientHandler());
                }
            });
            ChannelFuture future = bootstrap.connect("localhost", 8866).sync();
            //请求报文
            MessageProtobuf.Msg msg = buildMsg();
            future.channel().writeAndFlush(msg);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    private static MessageProtobuf.Msg buildMsg() {
        JSONObject headExtend = new JSONObject();
        headExtend.put("token", "token_memeda");
        return MessageProtobuf.Msg.newBuilder()
                .setHead(MessageProtobuf.Head.newBuilder()
                        .setMsgId("11111")
                        .setMsgType(1001)
                        .setSender("memeda")
                        .setReport(1)
                        .setExtend(headExtend.toJSONString())
                        .build())
                .setBody(MessageProtobuf.Body.newBuilder()
                        .setContent("this is test message")
                        .build())
                .build();
    }

    public static class ClientHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg)
                throws Exception {
            try {
                MessageProtobuf.Msg message = (MessageProtobuf.Msg) msg;
                LOGGER.info("read message ->{}", JsonFormat.printer().print(message));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        private String getMessage(ByteBuf buf) {
            byte[] con = new byte[buf.readableBytes()];
            buf.readBytes(con);
            try {
                return new String(con, "GBK");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
