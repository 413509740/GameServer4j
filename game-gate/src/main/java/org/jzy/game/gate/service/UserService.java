package org.jzy.game.gate.service;

import com.jzy.javalib.network.io.message.MsgUtil;
import io.netty.channel.Channel;
import org.jzy.game.common.constant.OfflineType;
import org.jzy.game.gate.struct.User;
import org.jzy.game.gate.tcp.user.UserTcpServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户管理
 *
 * @author jzy
 */
@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    /**
     * 用户id map
     */
    private Map<Long, User> userIds = new ConcurrentHashMap<>();

    /**
     * channel id map
     */
    private Map<Long, User> channelIds = new ConcurrentHashMap<>();

    /**
     * 玩家id id map
     */
    private Map<Long, User> playerIds = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {

    }

    public User getUserByChannelId(Long channelId) {
        return channelIds.get(channelId);
    }

    public User getUserByUserId(Long userId) {
        return userIds.get(userId);
    }

    public User getUserByPlayerId(Long playerId) {
        return playerIds.get(playerId);
    }


    /**
     * 用户连接创建
     *
     * @param user
     */
    public void onSocketConnect(User user) {
        user.setChannelId(MsgUtil.getChannelId(user.getClientChannel()));
        user.setIp(MsgUtil.getIp(user.getClientChannel()));
        channelIds.put(user.getChannelId(), user);
    }

    /**
     * 用户登录成功
     * @param user
     */
    public void onUserLoginSuccess(User user){
        userIds.put(user.getUserId(),user);
    }

    /**
     * 玩家加载成功
     * @param user
     */
    public void onPlayerLoadSuccess(User user){
        playerIds.put(user.getPlayerId(),user);
    }


    /**
     * 玩家离线
     *
     * @param clientChannel
     * @param offlineType
     */
    public void offLine(Channel clientChannel, OfflineType offlineType) {
        User user = clientChannel.attr(UserTcpServerHandler.USER).get();
        if (user != null) {
            //TODO

            //关闭消息压缩回调
            if(user.getPackMessageFuture()!=null){
                user.getPackMessageFuture().cancel(true);
            }
            userIds.remove(user.getUserId());
            channelIds.remove(user.getChannelId());
            playerIds.remove(user.getPlayerId());
            clientChannel.close();
            LOGGER.info("{}-{}-{} 离线{}", user.getAccount(), user.getUserId(), offlineType.toString(), MsgUtil.getIp(clientChannel));
        } else {
            LOGGER.warn("{} 无用户信息", MsgUtil.getIp(clientChannel));
        }
    }


}
