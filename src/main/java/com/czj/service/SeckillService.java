package com.czj.service;

import com.czj.dto.Exposer;
import com.czj.dto.SeckillExecution;
import com.czj.entity.Seckill;

import java.util.List;

/**
 * 秒杀service层接口
 * @author czj
 */
public interface SeckillService {

    //查询全部秒杀记录
    List<Seckill> getSeckillList();

    //查询单个秒杀记录
    Seckill queryById(long seckillId);

    //在秒杀开始时输出秒杀接口的地址，否则输出系统时间和秒杀时间
    Exposer exportSeckillUrl(long seckillId);

    //执行秒杀操作
    SeckillExecution executeSeckill(long seckillId, long userPhone, String md5);

    //调用存储过程来执行秒杀操作
    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5);
}
