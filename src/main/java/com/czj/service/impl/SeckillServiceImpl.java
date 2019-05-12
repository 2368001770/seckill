package com.czj.service.impl;

import com.czj.dao.SeckillDao;
import com.czj.dao.SuccessKilledDao;
import com.czj.dao.cache.RedisDao;
import com.czj.dto.Exposer;
import com.czj.dto.SeckillExecution;
import com.czj.entity.Seckill;
import com.czj.entity.SuccessKilled;
import com.czj.enums.SeckillStateEnum;
import com.czj.exception.RepeatKillException;
import com.czj.exception.SeckillCloseException;
import com.czj.exception.SeckillException;
import com.czj.service.SeckillService;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeckillServiceImpl implements SeckillService {

    //获取日志对象
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    @Autowired
    private RedisDao redisDao;

    private final String salt = "asdsfghjdfsd";

    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0,4);
    }

    public Seckill queryById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    public Exposer exportSeckillUrl(long seckillId) {
       /* Seckill seckill = seckillDao.queryById(seckillId);
        if(seckill == null){
            return new Exposer(false,seckillId);
        }*/

        //优化点：超时缓存，减低数据库的访问量
        Seckill seckill = redisDao.getSeckill(seckillId);
        if(seckill == null){
            seckill = seckillDao.queryById(seckillId);
            if(seckill == null){
                return new Exposer(false,seckillId);
            }else {
                redisDao.putSeckill(seckill);
            }
        }


        //若秒杀未开启
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        Date nowTime = new Date();
        if(startTime.getTime() > nowTime.getTime() || endTime.getTime() < nowTime.getTime()){
            return new Exposer(false,seckillId,nowTime.getTime(),startTime.getTime(),endTime.getTime());
        }

        //秒杀开启
        String md5 = getMD5(seckillId);
        return new Exposer(true,md5,seckillId);
    }

    /**
     * 执行秒杀过程
     * 成功：减库存，增加明细
     * 失败：抛出异常，事务回滚
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     */
   /* @Transactional
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) {
        if(md5 == null || !md5.equals(getMD5(seckillId))){
            throw new SeckillException("seckill data rewrite"); //秒杀数据被重写
        }

        //执行秒杀逻辑：减库存+增加购买明细
        Date nowTime = new Date();
        try {
            int updateCount = seckillDao.reduceNumber(seckillId,nowTime);
            if(updateCount <= 0){
                //秒杀结束
                throw new SeckillCloseException("seckill is closed");
            }else {
                //增加明细
                int insertCount = successKilledDao.insertSuccessKilled(seckillId,userPhone);
                if(insertCount <= 0){
                    throw new RepeatKillException("seckill repeated");
                }else {
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                    return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS,successKilled);
                }
            }
        } catch (SeckillCloseException e) {
            throw e;
        } catch (RepeatKillException e) {
            throw e;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            //将其他异常转化为运行期异常，进行事务回滚
            throw new SeckillException("seckill inner error :" + e.getMessage());
        }
    }*/

    /**
     * 优化点
     * 先添加购买明细再减库存
     * 减少事务中行级锁的持有时间
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     * @throws SeckillException
     * @throws RepeatKillException
     * @throws SeckillCloseException
     */
   @Transactional
   public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
           throws SeckillException, RepeatKillException, SeckillCloseException {
       if (md5 == null || !md5.equals(getMD5(seckillId))) {
           throw new SeckillException("seckill data rewrite");// 秒杀数据被重写了
       }
       // 执行秒杀逻辑:减库存+增加购买明细
       Date nowTime = new Date();
       /**
        * 将 减库存 插入购买明细  提交
        * 改为 插入购买明细 减库存 提交
        * 降低了网络延迟和GC影响，同时减少了rowLock的时间
        */
       try {
           // 否则更新了库存，秒杀成功,增加明细
           int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
           // 看是否该明细被重复插入，即用户是否重复秒杀
           if (insertCount <= 0) {
               //重复秒杀
               throw new RepeatKillException("seckill repeated");
           } else {
               // 减库存，热点商品竞争
               int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
               if (updateCount <= 0) {
                   // 没有更新库存记录，说明秒杀结束 rollback
                   throw new SeckillCloseException("seckill is closed");
               } else {
                   // 秒杀成功,得到成功插入的明细记录,并返回成功秒杀的信息 commit
                   SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                   return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS, successKilled);
               }
           }
       } catch (SeckillCloseException e1) {
           throw e1;
       } catch (RepeatKillException e2) {
           throw e2;
       } catch (Exception e) {
           logger.error(e.getMessage(), e);
           // 将编译期异常转化为运行期异常
           throw new SeckillException("seckill inner error :" + e.getMessage());
       }
   }

    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) {
        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            return new SeckillExecution(seckillId, SeckillStateEnum.DATE_REWRITE);
        }
        Date killTime = new Date();
        Map<String, Object> map = new HashMap<>();
        map.put("seckillId", seckillId);
        map.put("phone", userPhone);
        map.put("killTime", killTime);
        map.put("result", null);
        // 执行储存过程,result被复制
        try {
            seckillDao.killByProcedure(map);
            // 获取result
            int result = MapUtils.getInteger(map, "result", -2);
            if (result == 1) {
                SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS, successKilled);
            } else {
                return new SeckillExecution(seckillId, SeckillStateEnum.stateOf(result));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new SeckillExecution(seckillId, SeckillStateEnum.INNER_ERROR);
        }
    }

    /**
     * 使用Spring工具类实现md5加密
     * @param seckillId
     * @return
     */
    private String getMD5(long seckillId){
        String base = seckillId + "/" + salt;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }
}
