package pers.lomesome.ucm.server.view;

/**
 * 功能：服务器端的控制界面，可以完成启动服务器
 */

import pers.lomesome.ucm.server.models.UCMServer;

public class noGUI {
    // 构造方法
    public noGUI() {
        new MyThread().start();
    }
}

class MyThread extends Thread {
    @Override
    public void run() {
        new UCMServer();
    }
}
