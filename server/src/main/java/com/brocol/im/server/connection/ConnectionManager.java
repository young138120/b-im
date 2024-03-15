package com.brocol.im.server.connection;

import com.brocol.im.server.netty.entity.ChannelInfo;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 链接管理
 *
 * @author Brocol
 * @date 2024/3/15
 * @description
 */
public class ConnectionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);
    private final Map<String, ChannelInfo> CHANNELS = new ConcurrentHashMap<>();

    private ConnectionManager() {

    }

    private static final ConnectionManager INSTANCE = new ConnectionManager();

    public static ConnectionManager getInstance() {
        return INSTANCE;
    }

    public void saveChannel(ChannelInfo channel) {
        if (channel == null) {
            return;
        }
        CHANNELS.put(channel.getChannelId(), channel);
    }

    public ChannelInfo removeChannelIfConnectNoActive(Channel channel) {
        if (channel == null) {
            return null;
        }

        String channelId = channel.id().toString();

        return removeChannelIfConnectNoActive(channelId);
    }

    public ChannelInfo removeChannelIfConnectNoActive(String channelId) {
        if (CHANNELS.containsKey(channelId) && !CHANNELS.get(channelId).isActive()) {
            return CHANNELS.remove(channelId);
        }

        return null;
    }

    public String getUserIdByChannel(Channel channel) {
        return getUserIdByChannel(channel.id().toString());
    }

    public String getUserIdByChannel(String channelId) {
        if (CHANNELS.containsKey(channelId)) {
            return CHANNELS.get(channelId).getUserId();
        }

        return null;
    }

    public ChannelInfo getActiveChannelByUserId(String userId) {
        for (Map.Entry<String, ChannelInfo> entry : CHANNELS.entrySet()) {
            if (entry.getValue().getUserId().equals(userId) && entry.getValue().isActive()) {
                return entry.getValue();
            }
        }
        return null;
    }
}
