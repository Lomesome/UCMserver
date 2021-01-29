package pers.lomesome.ucm.server.tools;

/**
 * 功能：服务器和某个客户端的通讯线程
 */

import pers.lomesome.ucm.common.Message;
import pers.lomesome.ucm.common.MessageType;
import pers.lomesome.ucm.common.PeopleInformation;
import pers.lomesome.ucm.server.db.SqlHelper;
import java.net.*;
import java.sql.ResultSet;
import java.util.*;
import java.io.*;

public class ServerConClientThread extends Thread {
    Socket s;
    String id;

    // 构造方法
    public ServerConClientThread(Socket s, String id){
        // 把服务器和该客户端的连接赋给s
        this.s = s;
        this.id = id;
    }

    public void run() {
        while (true) {
            // 该线程可以接收客户端的信息
            try {
                ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
                Message m = (Message) ois.readObject();

                // 对从客户端取得的消息进行类型判断，然后做相应的处理
                if (m.getMesType().equals(MessageType.MESSAGE_COMM) || m.getMesType().equals(MessageType.MESSAGE_COMM_IMAGE) || m.getMesType().equals(MessageType.MESSAGE_COMM_VOICE) || m.getMesType().equals(MessageType.MESSAGE_GET_ADDPEOPLE) || m.getMesType().equals(MessageType.MESSAGE_MY_IMFORMATION_TO_FRIENDS)) {
                    // 服务器转发给客户端B
                    // 取得接收人的通讯线程

                    ServerConClientThread sc = ManageClientThread.getClientThread(m.getGetter());
                    if (m.getMesType().equals(MessageType.MESSAGE_GET_ADDPEOPLE)) {
                        m.setMesType(MessageType.MESSAGE_RET_ADDPEOPLE);
                        if (m.getContent().equals("added")) {
                            FriendsList.addF_F(m.getSender(),m.getGetter());
                            FriendsList.addF_F(m.getGetter(),m.getSender());
                        }
                    }
                    if(sc != null){
                        ObjectOutputStream oos = new ObjectOutputStream(sc.s.getOutputStream());
                        // 服务器发送给接收人
                        oos.writeObject(m);
                        SqlHelper sh = new SqlHelper();
                        String sql = "insert into historymessage (fromid, toid, message, sendtime, msgtype) values(?, ?, ?, ?, ?)";
                        String[] paras = { m.getSender(), m.getGetter(), m.getContent(), m.getSendTime(), m.getMesType() };
                        sh.changeMsg(sql, paras);
                        sh.close();
                    }else {
                        String sql;
                        if(m.getMesType().equals(MessageType.MESSAGE_RET_ADDPEOPLE)){
                            sql = "insert into historymessage (fromid, toid, message, sendtime, msgtype) values(?, ?, ?, ?, ?)";
                            SqlHelper sh = new SqlHelper();
                            String[] paras = { m.getSender(), m.getGetter(), m.getContent(), m.getSendTime(), m.getMesType() };
                            sh.changeMsg(sql, paras);
                            sh.close();
                        }else if (m.getMesType().equals(MessageType.MESSAGE_COMM) || m.getMesType().equals(MessageType.MESSAGE_COMM_IMAGE) || m.getMesType().equals(MessageType.MESSAGE_COMM_VOICE)){
                            sql = "insert into offlinemessage (fromid, toid, message, sendtime, msgtype) values(?, ?, ?, ?, ?)";
                            SqlHelper sh = new SqlHelper();
                            String[] paras = { m.getSender(), m.getGetter(), m.getContent(), m.getSendTime(), m.getMesType() };
                            sh.changeMsg(sql, paras);
                            sh.close();
                        }
                    }
                } else if (m.getMesType().equals(MessageType.MESSAGE_GET_MYFRIEND)) {
                    // 把在服务器的好友返回给客户端
                    Message m2 = new Message();
                    m2.setMesType(MessageType.MESSAGE_RET_MYFRIEND);
                    List<PeopleInformation> list = FriendsList.getFriends(m.getSender());
                    m2.setLists(list);
                    m2.setGetter(m.getSender());
                    ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                    oos.writeObject(m2);
                    list = null;
                    m2 = null;
                }else if (m.getMesType().equals(MessageType.MESSAGE_EXIT)){
                    ManageClientThread.delClientThread(m.getSender());
                    for(ServerConClientThread sc:ManageClientThread.getAllClientThread()){
                        ObjectOutputStream oos = new ObjectOutputStream(sc.s.getOutputStream());
                        // 服务器发送给接收人
                        oos.writeObject(m);
                    }
                }else if (m.getMesType().equals(MessageType.MESSAGE_GET_NOREAD)){
                    SqlHelper sh = new SqlHelper();
                    String sql = "select * from offlinemessage where toid = ?";
                    String[] paras = { m.getSender()};
                    ResultSet rs = sh.queryExecute(sql, paras);
                    List<Message> list = new ArrayList<>();
                    Message m2 = new Message();
                    m2.setMesType(MessageType.MESSAGE_RET_NOREAD);
                    while (rs.next()) {
                        Message message = new Message();
                        message.setSender(rs.getString("fromid"));
                        message.setGetter(rs.getString("toid"));
                        message.setSendTime(rs.getString("sendtime"));
                        message.setMesType(rs.getString("msgtype"));
                        message.setContent(rs.getString("message"));
                        list.add(message);
                        String newsql = "insert into historymessage (fromid, toid, message, sendtime, msgtype) values(?, ?, ?, ?, ?)";
                        String[] newparas = { message.getSender(), message.getGetter(), message.getContent(), message.getSendTime(), message.getMesType() };
                        sh.changeMsg(newsql, newparas);
                        message = null;
                    }
                    sql = "select * from offlinemessage where fromid = ?";
                    paras = new String[]{m.getSender()};
                    rs = sh.queryExecute(sql, paras);
                    while (rs.next()) {
                        Message message = new Message();
                        message.setSender(rs.getString("fromid"));
                        message.setGetter(rs.getString("toid"));
                        message.setSendTime(rs.getString("sendtime"));
                        message.setMesType(rs.getString("msgtype"));
                        message.setContent(rs.getString("message"));
                        list.add(message);
                    }
                    m2.setLists(list);
                    m2.setGetter(m.getSender());

                    sql = "DELETE FROM offlinemessage WHERE toid = ?";
                    paras = new String[]{m.getSender()};
                    sh.changeMsg(sql, paras);
                    ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                    oos.writeObject(m2);
                    sh.close();
                    list = null;
                    m2 = null;
                }else if (m.getMesType().equals(MessageType.MESSAGE_GET_HISTORY)){
                    SqlHelper sh = new SqlHelper();
                    String sql = "select * from historymessage where toid = ? or fromid = ?";
                    String[] paras = { m.getSender(), m.getSender() };
                    ResultSet rs = sh.queryExecute(sql, paras);
                    List<Message> list = new ArrayList<>();
                    Message m2 = new Message();
                    m2.setMesType(MessageType.MESSAGE_RET_HISTORY);
                    while (rs.next()) {
                        Message message = new Message();
                        message.setSender(rs.getString("fromid"));
                        message.setGetter(rs.getString("toid"));
                        message.setSendTime(rs.getString("sendtime"));
                        message.setMesType(rs.getString("msgtype"));
                        message.setContent(rs.getString("message"));
                        list.add(message);
                    }
                    m2.setLists(list);
                    m2.setGetter(m.getSender());
                    ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                    oos.writeObject(m2);
                    sh.close();
                    list = null;
                    m2 = null;
                }else if (m.getMesType().equals(MessageType.MESSAGE_GET_HISTORY_ADDPROPLE)){
                    SqlHelper sh = new SqlHelper();
                    String sql = "select distinct userinformation.*,historymessage.* from historymessage join userinformation on (historymessage.fromid = userinformation.userid or historymessage.toid = userinformation.userid) where (toid=? or fromid = ?) and msgtype=15";
                    String[] paras = { m.getSender(), m.getSender() };
                    ResultSet rs = sh.queryExecute(sql, paras);
                    List<Message> list = new ArrayList<>();
                    Message m2 = new Message();
                    m2.setMesType(MessageType.MESSAGE_RET_HISTORY_ADDPROPLE);
                    while (rs.next()) {
                        Message message = new Message();
                        message.setSender(rs.getString("fromid"));
                        message.setGetter(rs.getString("toid"));
                        message.setSendTime(rs.getString("sendtime"));
                        message.setMesType(rs.getString("msgtype"));
                        message.setContent(rs.getString("message"));
                        if(message.getMesType().equals(MessageType.MESSAGE_RET_ADDPEOPLE)){
                            List<PeopleInformation> peopleInformations = new ArrayList<>();
                            peopleInformations.add(new PeopleInformation(rs.getString("userid"), rs.getString("nickname"), null, rs.getString("head"), rs.getString("sex"), rs.getString("age"), rs.getString("signature"), rs.getString("phonenumber"), rs.getString("birthday")));
                            message.setLists(peopleInformations);
                        }
                        list.add(message);
                    }
                    m2.setLists(list);
                    m2.setGetter(m.getSender());
                    ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                    oos.writeObject(m2);
                    sh.close();
                    list = null;
                    m2 = null;
                }else if (m.getMesType().equals(MessageType.MESSAGE_GET_FINDPEOPLE)) {
                    Message m2 = new Message();
                    m2.setMesType(MessageType.MESSAGE_RET_FINDPEOPLE);
                    List<PeopleInformation> list = AddObject.getObjects(m.getContent());
                    m2.setLists(list);
                    m2.setGetter(m.getSender());
                    ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                    oos.writeObject(m2);
                    list = null;
                    m2 = null;
                }else if (m.getMesType().equals(MessageType.MESSAGE_CHANGE_MY_IMFORMATION)) {
                    SqlHelper sh = new SqlHelper();
                    String sql="UPDATE userinformation SET sex = ? , age = ?, birthday = ?, nickname = ?, signature = ?, head = ? WHERE userid = ?";
                    PeopleInformation peopleInformation = (PeopleInformation) m.getLists().get(0);
                    String[] paras = { peopleInformation.getSex(), String.valueOf(peopleInformation.getAge()),peopleInformation.getBirthday(),peopleInformation.getNickname(), peopleInformation.getSignature(), peopleInformation.getHead(), peopleInformation.getUserid() };
                    sh.changeMsg(sql, paras);
                    sh.close();
                }else if (m.getMesType().equals(MessageType.MESSAGE_MY_IMFORMATION_TO_FRIENDS)) {
                    // 服务器转发给客户端B
                    // 取得接收人的通讯线程
                    ServerConClientThread sc = ManageClientThread.getClientThread(m.getGetter());
                    if(sc != null){
                        ObjectOutputStream oos = new ObjectOutputStream(sc.s.getOutputStream());
                        // 服务器发送给接收人
                        oos.writeObject(m);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    s.close();
                    ManageClientThread.delClientThread(id);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                break;
            }
        }
    }
}
