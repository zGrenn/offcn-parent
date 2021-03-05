package com.offcn.order.service.impl;

import com.offcn.dycommon.enums.OrderStatusEnumes;
import com.offcn.dycommon.response.AppResponse;
import com.offcn.order.mapper.TOrderMapper;
import com.offcn.order.po.TOrder;
import com.offcn.order.service.OrderService;
import com.offcn.order.service.ProjectServiceFeign;
import com.offcn.order.vo.req.OrderInfoSubmitVo;
import com.offcn.order.vo.resp.TReturn;
import com.offcn.utils.AppDateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ProjectServiceFeign projectServiceFeign;
    @Autowired
    private TOrderMapper orderMapper;
    @Override
    public TOrder saveOrder(OrderInfoSubmitVo vo) {
        //1.创建订单对象
        TOrder order = new TOrder();
        //通过令牌获取用户的id
        String memberIdString = stringRedisTemplate.opsForValue().get(vo.getAccessToken());
        int memberId = Integer.parseInt(memberIdString);
        //设置订单的用户id
        order.setMemberid(memberId);
        BeanUtils.copyProperties(vo,order);
        //设置订单号
        String orderNum = UUID.randomUUID().toString().replace("-", "");
        order.setOrdernum(orderNum);
        //创建时间
        order.setCreatedate(AppDateUtils.getFormatTime());
        //设置支付的状态
        order.setStatus(OrderStatusEnumes.UNPAY.getCode()+"");
        //设置发票状态
        order.setInvoice(vo.getInvoice().toString());
        //远程调用
        AppResponse<List<TReturn>> response = projectServiceFeign.getReturnList(vo.getProjectid());
        List<TReturn> returnList = response.getData();
        TReturn myReturn = null;
        for (TReturn tReturn : returnList) {
            if(tReturn.getId().intValue() == vo.getReturnid().intValue()){
                myReturn = tReturn;
                break;
            }
        }
        //算钱 支持的数量*每笔支持的金额+运费
        Integer money = order.getRtncount()*myReturn.getSupportmoney()+myReturn.getFreight();
        order.setMoney(money);
        orderMapper.insertSelective(order);

        return order;
    }
}
