# websocket
                                  该教程适用于新手，授之以鱼不如授之以渔
超详细，手把手教你websocket入门服务推送，模拟课程签到提醒，前端angular测试，springboot+websocket
注意：本项目的学习要结合前端测试，效果会更佳，需要了解angular，ionic即可

第一步：开启websocket的支持WebSocketConfig.java；
package com.kang.websocket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * 开启websocket的支持
 */
@Configuration  
public class WebSocketConfig {  
    @Bean  
    public ServerEndpointExporter serverEndpointExporter(){  
        return new ServerEndpointExporter();  
    }  
}  
第二步：写一个socketserver服务SocketServer.java

package com.kang.websocket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.kang.websocket.bean.entity.Client;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

@ServerEndpoint(value = "/socketServer/{userName}")
@Component
public class SocketServer {

	private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);

	/**
	 *
	 * 用线程安全的CopyOnWriteArraySet来存放客户端连接的信息
	 */
	private static CopyOnWriteArraySet<Client> socketServers = new CopyOnWriteArraySet<>();

	/**
	 *
	 * websocket封装的session,信息推送，就是通过它来信息推送
	 */
	private Session session;

	/**
	 *
	 * 服务端的userName,因为用的是set，每个客户端的username必须不一样，否则会被覆盖。
	 */
	private final static String SYS_USERNAME = "server";


	/**
	 *
	 * 用户连接时触发，我们将其添加到
	 * 保存客户端连接信息的socketServers中
	 *
	 * @param session
	 * @param userName
	 */
	@OnOpen
	public void open(Session session,@PathParam(value="userName")String userName){

			this.session = session;
			socketServers.add(new Client(userName,session));

			logger.info("客户端:【{}】连接成功",userName);
			logger.info("当前连接人数：{}人",SocketServer.getOnlineNum());

	}

	
	@OnMessage
	public void onMessage(String message){

		Client client = socketServers.stream().filter( cli -> cli.getSession() == session)
				.collect(Collectors.toList()).get(0);
		sendMessage(client.getUserName()+"<--"+message,SYS_USERNAME);

		logger.info("客户端:【{}】发送信息:{}",client.getUserName(),message);
	}

	/**
	 *
	 * 连接关闭触发，通过sessionId来移除
	 * socketServers中客户端连接信息
	 */
	@OnClose
	public void onClose(){
		socketServers.forEach(client ->{
			if (client.getSession().getId().equals(session.getId())) {

				logger.info("客户端:【{}】断开连接",client.getUserName());
				socketServers.remove(client);
				logger.info("当前连接人数：{}人",SocketServer.getOnlineNum());

			}
		});
	}

	/**
	 *
	 * 发生错误时触发
	 * @param error
	 */
    @OnError
    public void onError(Throwable error) {
		socketServers.forEach(client ->{
			if (client.getSession().getId().equals(session.getId())) {
				socketServers.remove(client);
				logger.error("客户端:【{}】发生异常",client.getUserName());
				error.printStackTrace();
			}
		});
    }

	/**
	 *
	 * 信息发送的方法，通过客户端的userName
	 * 拿到其对应的session，调用信息推送的方法
	 * @param message
	 * @param userName
	 */
	public synchronized static void sendMessage(String message,String userName) {

		socketServers.forEach(client ->{
			if (userName.equals(client.getUserName())) {
				try {
					client.getSession().getBasicRemote().sendText(message);

					logger.info("服务端推送给客户端 :【{}】的信息：{}",client.getUserName(),message);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 *
	 * 如果server连接，统计时去掉server连接
	 * 所以连接总数还要减去服务端
	 * 本身的一个连接数
	 *
	 * 这里运用三元运算符是因为客户端第一次在加载的时候
	 * 客户端本身也没有进行连接，-1 就会出现总数为-1的情况，
	 * 这里主要就是为了避免出现连接数为-1的情况
	 *
	 * @return
	 */
	public synchronized static int getOnlineNum(){
		return socketServers.stream().filter(client -> !client.getUserName().equals(SYS_USERNAME))
				.collect(Collectors.toList()).size();
	}

	/**
	 *
	 * 获取在线用户名，前端界面需要用到
	 * @return
	 */
	public synchronized static List<String> getOnlineUsers(){

		List<String> onlineUsers = socketServers.stream()
				.filter(client -> !client.getUserName().equals(SYS_USERNAME))
				.map(client -> client.getUserName())
				.collect(Collectors.toList());

	    return onlineUsers;
	}

	/**
	 *
	 * 信息群发，我们要排除服务端自己不接收到推送信息
	 * 所以我们在发送的时候将服务端排除掉
	 * @param message
	 */
	public synchronized static void sendAll(String message) {
		//群发，不能发送给服务端自己
		socketServers.stream().filter(cli -> cli.getUserName() != SYS_USERNAME)
				.forEach(client -> {
			try {
				client.getSession().getBasicRemote().sendText(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		logger.info("服务端推送给所有客户端 :【{}】",message);
	}

	/**
	 *
	 * 多个人发送给指定的几个用户
	 * @param message
	 * @param persons
	 */
	public synchronized static void SendMany(String message,String [] persons) {
		for (String userName : persons) {
			sendMessage(message,userName);
		}
	}
}
第三步：控制层编写，在你需要的请求控制块给客户端发送信息：SocketServer.SendMany(course + "课程已经开始签到", users);
代码如下
package com.kang.websocket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

import com.kang.websocket.bean.entity.User;
import com.kang.websocket.bean.out.ReturnOut;
import com.kang.websocket.server.SocketServer;

/**
 * websocket 消息推送(个人和广播)
 */
@Controller
public class WebSocketController {

    @Autowired
    private SocketServer socketServer;

    /**
     *
     * 此url可以用在后台管理系统获取用户在线人数，和在线列表
     */
    @RequestMapping("/getOnlineNum")
    @ResponseBody
    public ReturnOut all() {
        ReturnOut returnOut = new ReturnOut();
        int num = socketServer.getOnlineNum();
        returnOut.setData(num);
        return returnOut;
    }

    /**
     *
     * 此url可以用在后台管理系统获取用户在线人数，和在线列表
     */
    @RequestMapping("/getOnlineUsers")
    @ResponseBody
    public ReturnOut allUsers() {
        ReturnOut returnOut = new ReturnOut();
        List<String> list = socketServer.getOnlineUsers();
        returnOut.setData(list);
        return returnOut;
    }

    /**
     * 信息推送,模拟签到提醒
     * 
     * @return
     */
    @RequestMapping("/beginqiandao")
    @ResponseBody
    public ReturnOut beginqiandao(@RequestParam String course) {
        ReturnOut returnOut = new ReturnOut();
        // 签到时肯定有你需要签到的课程，其他的提醒类似
        //设置签到标签=1；然后进行下面的
        // 这里省去数据库，定义一个list，模拟数据库查找选这门课的学生
        List<User> list = new ArrayList<>();
        list.add(new User("1", "刘备", course, "诸葛亮"));
        list.add(new User("2", "关羽", course, "诸葛亮"));
        list.add(new User("3", "张飞", course, "诸葛亮"));
        //假如前端1,2,4在线，则只会推送1，2
        String[] users = new String[list.size()];
        int i = 0;
        for (User user : list) {
            users[i++] = user.getId();
        }

        SocketServer.SendMany(course + "课程已经开始签到", users);
        return returnOut;
    }
}


jdk1.8，源码克隆下来，导入maven工程，注意先要配置好maven，自行解决。本项目测试采用前后端分离，前端采用angularjs测试接口，后续见https://github.com/kangwenzhuang/websocketqianduan
