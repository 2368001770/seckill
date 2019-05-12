package com.czj.entity;

import java.util.Date;

/**
 * 购买明细（订单）
 * @author czj
 */
public class SuccessKilled {

    private Short state;        //用户秒杀商品状态

    private Date createTime;    //用户秒杀商品时间

    private Long seckillId;     //商品id

    private Long userPhone;     //用户手机号

    private Seckill seckill;    //秒杀商品信息，多对一

    @Override
    public String toString() {
        return "SuccessKilled{" +
                "state=" + state +
                ", createTime=" + createTime +
                ", seckillId=" + seckillId +
                ", userPhone=" + userPhone +
                ", seckill=" + seckill +
                '}';
    }

    public Short getState() {
        return state;
    }

    public void setState(Short state) {
        this.state = state;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Long getSeckillId() {
        return seckillId;
    }

    public void setSeckillId(Long seckillId) {
        this.seckillId = seckillId;
    }

    public Long getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(Long userPhone) {
        this.userPhone = userPhone;
    }

    public Seckill getSeckill() {
        return seckill;
    }

    public void setSeckill(Seckill seckill) {
        this.seckill = seckill;
    }
}
