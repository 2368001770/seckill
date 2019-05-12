package com.czj.dao.cache;

import com.czj.dao.SeckillDao;
import com.czj.entity.Seckill;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class RedisDaoTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private long id =1004;

    @Autowired
    private RedisDao redisDao;

    @Autowired
    private SeckillDao seckillDao;

    @Test
    public void testSeckill() {
        Seckill seckill = redisDao.getSeckill(id);
        if(seckill == null){
            seckill = seckillDao.queryById(id);
            if(seckill != null) {
                String result = redisDao.putSeckill(seckill);
                logger.info("result={}",result);
                seckill = redisDao.getSeckill(id);
                logger.info("seckill={}",seckill);
            }
        }
    }
}