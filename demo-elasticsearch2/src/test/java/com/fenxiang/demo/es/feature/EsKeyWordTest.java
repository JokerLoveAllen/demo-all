package com.fenxiang.demo.es.feature;

import com.fenxiang.demo.es.SpringbootElasticTest;
import com.fenxiang.demo.es.repoistory.KeywordSuggestEsRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.TreeSet;

/**
 * @ClassName EsKeyWordTest
 * @Author lqs
 * @Date 2020/5/25 20:05
 */
public class EsKeyWordTest extends SpringbootElasticTest {
    @Autowired
    KeywordSuggestEsRepository keywordSuggestEsRepository;

    @Test
    public void create_Test(){
        final TreeSet treeSet = new TreeSet();
        keywordSuggestEsRepository.upsert("项羽打刘备");
    }
}
