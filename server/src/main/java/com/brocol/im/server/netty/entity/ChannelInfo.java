package com.brocol.im.server.netty.entity;

import io.netty.channel.Channel;

/**
 * @author Brocol
 * @date 2024/3/15
 * @description
 */
public class ChannelInfo {

    private String userId;
    private Channel channel;

    public ChannelInfo(String userId, Channel channel) {
        this.userId = userId;
        this.channel = channel;
    }

    public ChannelInfo() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getChannelId() {
        return this.channel.id().toString();
    }

    public boolean isActive() {
        return this.channel.isActive();
    }
}
