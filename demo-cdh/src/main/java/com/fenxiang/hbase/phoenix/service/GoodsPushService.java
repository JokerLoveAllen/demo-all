package com.fenxiang.hbase.phoenix.service;

import com.fenxiang.hbase.phoenix.common.utils.SimpleIdUtils;
import com.fenxiang.hbase.phoenix.dao.phoenix.PhoenixGoodsPushMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName GoodsPushHbaseService
 * @Author lqs
 * @Date 2020/4/28 17:17
 */
@Service
public class GoodsPushService {
    @Autowired
    private PhoenixGoodsPushMapper phoenixGoodsPushMapper;

    public int count(int year, int month, int day) {
        List<Long[]> range = SimpleIdUtils.day(year, month, day);
        int cnt = range.parallelStream().map(e -> phoenixGoodsPushMapper.count(e[0], e[1])).reduce(0, Integer::sum);
        return cnt;
    }
}
