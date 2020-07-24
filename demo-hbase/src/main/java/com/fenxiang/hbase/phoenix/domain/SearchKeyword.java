package com.fenxiang.hbase.phoenix.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName SearchKeyword
 * @Author lqs
 * @Date 2020/5/9 17:57
 */
@Setter
@Getter
@ToString
public class SearchKeyword {
    String keyword, cnt;
}
