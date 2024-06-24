package com.lld.im.service.message.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lld.im.service.message.dao.ImOfflineMessageEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OfflineMessageMapper extends BaseMapper<ImOfflineMessageEntity> {
}
