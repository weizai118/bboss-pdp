package com.github.sd4324530.fastweixin.servlet;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sd4324530.fastweixin.handle.EventHandle;
import com.github.sd4324530.fastweixin.handle.MessageHandle;
import com.github.sd4324530.fastweixin.message.BaseMsg;
import com.github.sd4324530.fastweixin.message.TextMsg;
import com.github.sd4324530.fastweixin.message.req.BaseEvent;
import com.github.sd4324530.fastweixin.message.req.BaseReqMsg;
import com.github.sd4324530.fastweixin.message.req.TextReqMsg;

public class SimpleWeixinControllerSupport extends WeixinControllerSupport {

	 private static final Logger log = LoggerFactory.getLogger(SimpleWeixinControllerSupport.class);
     private static final String TOKEN = "myToken";
     //设置TOKEN，用于绑定微信服务器
     @Override
     protected String getToken() {
         return TOKEN;
     }
     //使用安全模式时设置：APPID
     //不再强制重写，有加密需要时自行重写该方法
     @Override
     protected String getAppId() {
         return null;
     }
     //使用安全模式时设置：密钥
     //不再强制重写，有加密需要时自行重写该方法
     @Override
     protected String getAESKey() {
         return null;
     }
     //重写父类方法，处理对应的微信消息
     @Override
     protected BaseMsg handleTextMsg(TextReqMsg msg) {
         String content = msg.getContent();
         log.debug("用户发送到服务器的内容:{}", content);
         return new TextMsg("服务器回复用户消息!");
     }
     /*1.1版本新增，重写父类方法，加入自定义微信消息处理器
      *不是必须的，上面的方法是统一处理所有的文本消息，如果业务觉复杂，上面的会显得比较乱
      *这个机制就是为了应对这种情况，每个MessageHandle就是一个业务，只处理指定的那部分消息
      */
     @Override
     protected List<MessageHandle> initMessageHandles() {
             List<MessageHandle> handles = new ArrayList<MessageHandle>();
             handles.add(new MessageHandle(){

				@Override
				public BaseMsg handle(BaseReqMsg message) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public boolean beforeHandle(BaseReqMsg message) {
					// TODO Auto-generated method stub
					return false;
				}
            	 
             });
             return handles;
     }
     //1.1版本新增，重写父类方法，加入自定义微信事件处理器，同上
     @Override
     protected List<EventHandle> initEventHandles() {
             List<EventHandle> handles = new ArrayList<EventHandle>();
             handles.add(new EventHandle(){

				@Override
				public BaseMsg handle(BaseEvent event) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public boolean beforeHandle(BaseEvent event) {
					// TODO Auto-generated method stub
					return false;
				}
            	 
             });
             return handles;
     }

}