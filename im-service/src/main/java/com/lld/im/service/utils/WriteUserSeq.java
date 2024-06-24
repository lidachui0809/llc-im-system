package com.lld.im.service.utils;

import com.lld.im.common.constant.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @description:
 */
@Service
public class WriteUserSeq {

    //redis
    //uid friend 10
    //    group 12
    //    conversation 123
    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 设计思路： 客户端数据同步策略 采用增量同步策略
     *      再数据库中维护每一个用户对于group conversion friendship 序列信息 (也就是每一次的操作的执行序列)
     *      再用户进行同步之前 先获得序列信息 在对比自身序列信息 如果<服务序列 则更新拉取该模块信息
     *      【因此 对于与之相关的每一个数据行 都会有一个seq 客户端会获得最大的seq并返回给服务端 拉取数据】
     *
     *      对于群组 设计稍有差别 这里只会将每个群的seq写入数据库 客户端提供群组id 在数据库中拉取数据 进行对比
     *
     */


    public void writeUserSeq(Integer appId,String userId,String type,Long seq){
        String key = appId + ":" + Constants.RedisConstants.SeqPrefix + ":" + userId;
        redisTemplate.opsForHash().put(key,type,seq);
    }

}
