package pers.lomesome.ucm.server.models;

/**
 * 功能：处理用户登录请求
 */

import pers.lomesome.ucm.common.Message;
import pers.lomesome.ucm.common.MessageType;
import pers.lomesome.ucm.common.PeopleInformation;
import pers.lomesome.ucm.common.User;
import pers.lomesome.ucm.server.db.SqlHelper;
import pers.lomesome.ucm.server.tools.ManageClientThread;
import pers.lomesome.ucm.server.tools.ServerConClientThread;
import java.net.*;
import java.io.*;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UCMServer {
    SqlHelper sh = null;
    // 关闭资源
    public void close() {
        // 关闭连接
        try {
            if (sh != null) {
                sh.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // 构造方法
    public UCMServer() {
        System.out.println("服务器已启动");
        try {
            // 在8080端口监听
            ServerSocket ss = new ServerSocket(8080);
            while (true) {
                try{
                    // 阻塞，直到用户连接
                    // 这里用局部变量的原因是因为要为每一个与服务器连接的客户端建立一个独立的Socket
                    Socket s = ss.accept();
                    // 接收客户端发来的账号信息
                    ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
                    User u = (User) ois.readObject();
                    // 测试服务器是否收到消息
                    Message m = new Message();
                    ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                    // 验证信息
                    // 通过数据库验证
                    sh = new SqlHelper();
                    String sql = "select * from userinformation where userid=?";
                    String[] paras = { u.getUserId() };
                    ResultSet rs = sh.queryExecute(sql, paras);
                    String password = "";
                    Boolean hava = false;
                    if (rs.next()) {
                        hava = true;
                        password = rs.getString("password").trim();
                    }
                    if(hava){
                        if (password.equals(u.getPasswd())) {
                            // 返回一个成功登录的信息包
                            m.setMesType(MessageType.MESSAGE_SUCCEED);
                            PeopleInformation my = new PeopleInformation(rs.getString("userid"), rs.getString("nickname"), null, rs.getString("head"), rs.getString("sex"), rs.getString("age"), rs.getString("signature"), rs.getString("phonenumber"), rs.getString("birthday"));
                            List<PeopleInformation> mylist = new ArrayList<>();
                            mylist.add(my);
                            m.setLists(mylist);
                            // 向客户端发送Message对象
                            oos.writeObject(m);
                            // 单开一个线程，让该线程与该客户端保持通讯
                            ServerConClientThread scct = new ServerConClientThread(s, u.getUserId());
                            // 加入通讯线程集合
                            ManageClientThread.addClientThread(u.getUserId(), scct);
                            // 启动与该客户端通讯的线程
                            scct.start();
                        } else {
                            // 返回一个失败的信息包
                            m.setMesType(MessageType.MESSAGE_WRONG_PASSWORD);
                            // 向客户端发送Message对象
                            oos.writeObject(m);
                            // 关闭连接
                            s.close();
                        }
                    }else {
                        // 返回一个没有帐号的信息包
                        m.setMesType(MessageType.MESSAGE_NO_ACCOUNT);
                        // 向客户端发送Message对象
                        oos.writeObject(m);
                        // 关闭连接
                        s.close();
                    }
                    close();
                }catch (Exception e){ }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }
}
