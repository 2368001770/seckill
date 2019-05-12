package com.czj.enums;

/**
 * 秒杀执行状态枚举类
 * @author czj
 */
public enum SeckillStateEnum {

    SUCCESS(1,"秒杀成功"),
    END(0,"秒杀结束"),
    REPEAT_KILL(-1,"重复秒杀"),
    INNER_ERROR(-2,"系统异常"),
    DATE_REWRITE(-3,"数据篡改");

    private int state;
    private String info;

    SeckillStateEnum(int state, String info) {
        this.state = state;
        this.info = info;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public static SeckillStateEnum stateOf(int index){
        for(SeckillStateEnum stateEnum : values()){
            if(stateEnum.getState() == index){
                return stateEnum;
            }
        }
        return null;
    }
}
