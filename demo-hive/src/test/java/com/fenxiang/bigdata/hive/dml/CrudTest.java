package com.fenxiang.bigdata.hive.dml;

import cn.hutool.core.bean.BeanUtil;
import com.fenxiang.bigdata.hive.HiveTestLoad;
import com.fenxiang.bigdata.hive.domain.StuBuck;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * @ClassName CrudTest
 * @Author lqs
 * @Date 2020/6/10 17:02
 */
@Slf4j
public class CrudTest extends HiveTestLoad {

    @Autowired
    private JdbcTemplate hiveJdbcTemplate;

    @Test
    public void find_Test(){
        final List<Map<String, Object>> maps = hiveJdbcTemplate.queryForList("select Sno, Sname, Sex, Sage from stu_buck");
        for (Map<String, Object> map : maps) {
//            log.info("curr: ({})", map);
            final StuBuck stuBuck = BeanUtil.mapToBeanIgnoreCase(map, StuBuck.class, false);
            log.info("stuBuck:({})", stuBuck);
        }
    }

    @Test
    public void Agg_Test(){
        final List<Map<String, Object>> maps = hiveJdbcTemplate.queryForList("select a.Sno Sno, a.Sname Sname, a.Sex Sex, a.Sage Sage " +
                "from stu_buck as a, (select max(Sage) as Sage from stu_buck) as b" +
                " where a.Sage = b.Sage");
        for (Map<String, Object> map : maps) {
            log.info("curr: ({})", map);
            final StuBuck stuBuck = BeanUtil.mapToBeanIgnoreCase(map, StuBuck.class, false);
            log.info("stuBuck:({})", stuBuck);
        }
    }

}
