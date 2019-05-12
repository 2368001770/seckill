package com.czj.dao;

import com.czj.entity.SuccessKilled;
import org.apache.ibatis.annotations.Param;

/**
 * 购买明细Dao接口
 * @author czj
 */
public interface SuccessKilledDao {

    /**
     * 插入购买明细
     * 由于使用联合主键，不可以重复插入，若重复插入返回0（ignore）
     * @param seckillId
     * @param userPhone
     * @return 成功插入的行数
     */
    int insertSuccessKilled(@Param("seckillId") long seckillId,@Param("userPhone") long userPhone);

    /**
     * 根据主键（seckill_id + user_phone）查询携带秒杀商品对象实体的购买明细
     * @param seckillId
     * @param userPhone
     * @return 携带秒杀商品对象实体的购买明细
     */
    SuccessKilled queryByIdWithSeckill(@Param("seckillId") Long seckillId,@Param("userPhone") long userPhone);
}
