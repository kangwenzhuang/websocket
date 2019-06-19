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