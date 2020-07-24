package com.qianlima.demo.redis.cache;

import com.qianlima.demo.redis.SpringBootRedisTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.Scanner;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/7/11 17:50
 * @Version 0.0.1
 */
@Slf4j
public class RedisCacheTest extends SpringBootRedisTest {

    @Autowired
    private ValueOperations<String,Object> valueOperations;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Autowired
    private HashOperations<String,String,Integer> hashOperations;

    @Test
    public void insert(){
//        valueOperations.set("f1",new DataModel("卧槽","嘿嘿","大大大大大","额鹅鹅鹅","我我我我我我"));
//        valueOperations.setIfAbsent("f2","ABCDFASDA");// 实现分布式锁:command line: setnx key value
//        valueOperations.setIfAbsent("f2","ABCDFASDAD12323457");// getset force update old value
//        valueOperations.set("f1",new DataModel("1","2","3","4","5"));
//        log.warn("info:{}",valueOperations.getAndSet("f2","123456789789789"));

//        redisTemplate.opsForList().leftPush("ls",2);
//        redisTemplate.opsForList().leftPush("ls","1");
//        redisTemplate.opsForList().rightPush("ls","three");
//        log.warn("当前Len:{}",redisTemplate.opsForList().rightPush("ls","4.0"));
//        List<Object> ls = redisTemplate.opsForList().range("ls", 0, -1);// get all item in list 0,-1 => lrange key 0 -1
//        Boolean delete = redisTemplate.delete("ls");
//        log.warn("deleted; {}",delete);
//        log.warn("ls: {}",ls);
    }

    @Test
    public void echo(){
        redisTemplate.convertAndSend("first","fu|shan|wu|ying|jue");
        String s = new Scanner(System.in).next();
        log.warn("s: {}", s);
//        Map<String, Integer> contentids = hashOperations.entries("contentids");
//        log.warn("{}",contentids.keySet());
//        try(BufferedWriter bw = new BufferedWriter(new FileWriter(new File("C:\\Users\\DELL\\Desktop\\keys.txt"),false))){
//            for(String key : contentids.keySet()){
//                bw.write(String.format("http://www.qianlima.com/zb/detail/20190927_%s.html\n",key));
//            }
//            bw.flush();
//        }catch (Exception except){
//            log.error("have exp : ", except);
//        }
    }
}
