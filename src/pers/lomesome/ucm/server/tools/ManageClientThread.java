package pers.lomesome.ucm.server.tools;

/**
 * 功能：管理客户端线程
 */

import java.util.*;

public class ManageClientThread {
    // 客户端通信线程集合
    public static HashMap<String, ServerConClientThread> hm = new HashMap<String, ServerConClientThread>();

    // 向hm添加一个客户端通讯线程
    public static void addClientThread(String uid, ServerConClientThread ct) {
        hm.put(uid, ct);
    }

    // 获取一个客户端通讯线程
    public static ServerConClientThread getClientThread(String uid) {
        return (ServerConClientThread) hm.get(uid);
    }

    // 获取一个客户端通讯线程
    public static void delClientThread(String uid) {
        hm.remove(uid);
    }


    // 获取所有客户端通讯线程
    public static List<ServerConClientThread> getAllClientThread() {
        List<ServerConClientThread> list = new ArrayList<>();
        Iterator<ServerConClientThread> it = hm.values().iterator();
        while (it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }
}
