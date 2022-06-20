package com.miaoshaproject.miaosha.service.impl;

import com.miaoshaproject.miaosha.dao.SequenceDOMapper;
import com.miaoshaproject.miaosha.dataobject.SequenceDO;
import com.miaoshaproject.miaosha.service.SequenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author yangLe
 * @Description TODO
 * @Date 2022/6/20 16:13
 * @Version 1.0
 */
@Service("sequence")
public class SequenceServiceImpl implements SequenceService {
    private SequenceDOMapper sequenceDOMapper;

    public SequenceServiceImpl(SequenceDOMapper sequenceDOMapper) {
        this.sequenceDOMapper = sequenceDOMapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public Integer getAndUpdateSequence(String name) {
        SequenceDO sequenceDO = sequenceDOMapper.selectByPrimaryKey(name);
        Integer res = sequenceDO.getSequence();
        sequenceDO.setSequence(sequenceDO.getSequence() + sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);
        return res;
    }
}
