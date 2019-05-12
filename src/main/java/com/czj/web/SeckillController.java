package com.czj.web;

import com.czj.dto.Exposer;
import com.czj.dto.SeckillExecution;
import com.czj.dto.SeckillResult;
import com.czj.entity.Seckill;
import com.czj.enums.SeckillStateEnum;
import com.czj.exception.RepeatKillException;
import com.czj.exception.SeckillCloseException;
import com.czj.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 实现restful接口的秒杀controller
 * url:模块/资源/{}/细分
 * @author czj
 */
@Controller
@RequestMapping("/seckill")
public class SeckillController {

    //日志对象
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    //显示秒杀商品列表
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public String list(Model model){
        List<Seckill> list = seckillService.getSeckillList();
        model.addAttribute("list",list);
        return "list";
    }

    //显示秒杀商品详情
    @RequestMapping(value = "/{seckillId}/detail",method = RequestMethod.GET)
    public String detail(@PathVariable("seckillId") Long seckillId,Model model){
        if(seckillId == null){
            return "redirect:/seckill/list";    //请求重定向
        }

        Seckill seckill = seckillService.queryById(seckillId);
        if(seckill == null) {
            return "forward:/seckill/list";     //请求转发
        }
        model.addAttribute("seckill",seckill);
        return "detail";
    }

    //ajax、json暴露秒杀接口
    @RequestMapping(value = "/{seckillId}/exposer",
            method = RequestMethod.GET,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<Exposer> exposer(@PathVariable("seckillId")Long seckillId){
        SeckillResult<Exposer> result;
        try{
            Exposer exposer = seckillService.exportSeckillUrl(seckillId);
            result = new SeckillResult<Exposer>(true,exposer);
        }catch (Exception e){
            logger.error(e.getMessage());
            result = new SeckillResult<Exposer>(false,e.getMessage());
        }
        return result;
    }

    //执行秒杀过程
    @RequestMapping(value = "/{seckillId}/{md5}/execution",
            method = RequestMethod.POST,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<SeckillExecution> execute(@PathVariable("seckillId")Long seckillId,
                                                   @PathVariable("md5")String md5,
                                                   @CookieValue(value = "userPhone",required = false) Long userPhone){
        if(userPhone == null){
            return new SeckillResult<SeckillExecution>(false,"未注册");
        }
        SeckillResult<SeckillExecution> result;
        try{
            //SeckillExecution seckillExecution = seckillService.executeSeckill(seckillId,userPhone,md5);
            SeckillExecution seckillExecution = seckillService.executeSeckillProcedure(seckillId,userPhone,md5);
            return new SeckillResult<SeckillExecution>(true,seckillExecution);
        }catch (RepeatKillException e1){
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStateEnum.REPEAT_KILL);
            return new SeckillResult<SeckillExecution>(true,execution);
        }catch (SeckillCloseException e2){
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStateEnum.END);
            return new SeckillResult<SeckillExecution>(true,execution);
        }catch (Exception e3){
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStateEnum.INNER_ERROR);
            return new SeckillResult<SeckillExecution>(true,execution);
        }
    }

    //获取当前系统时间
    @RequestMapping(value = "/time/now",method = RequestMethod.GET)
    @ResponseBody
    public SeckillResult<Long> time(){
        Date now = new Date();
        return new SeckillResult<Long>(true,now.getTime());
    }

}
