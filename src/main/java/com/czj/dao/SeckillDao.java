package com.czj.dao;

import com.czj.entity.Seckill;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 秒杀商品Dao接口
 * @author czj
 */
public interface SeckillDao {

    /**
     * 减库存
     * 判断条件和减少库存量在一个原子操作里面，避免出现数据不一致
     * seckillId 若为Long类型时传入1000会报错，因为1000（int）自动转化为Long包装类
     */
    int reduceNumber(@Param("seckillId")long seckillId, @Param("killTime")Date killTime);

    //根据id查询秒杀商品
    Seckill queryById(Long seckillId);

    //根据偏移量查询秒杀商品列表
    List<Seckill> queryAll(@Param("offset") int offset,@Param("limit") int limit);

    //使用储存过程执行秒杀
    void killByProcedure(Map<String, Object> paramMap);
}
