package com.czj.exception;

/**
 * 自定义秒杀异常，供其他自定义异常继承
 * @author czj
 */
public class SeckillException extends RuntimeException {

    public SeckillException(String message) {
        super(message);
    }

    public SeckillException(String message, Throwable cause) {
        super(message, cause);
    }
}
