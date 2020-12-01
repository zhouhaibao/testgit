package cn.com.glsx.admin.common.liaotian;

import java.io.IOException;
import java.util.Scanner;

/**
 * @author taoyr
 */
public class test1 {
    public static void main(String[] args) throws IOException {
        // 启动客户端
        ChatClient chaClient = new ChatClient();
        // 启动一个线程
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    chaClient.readInfo();
                    try {
                        Thread.currentThread().sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        // 发送数据给服务器端
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String s = scanner.nextLine();
            chaClient.sendInfo(s);
        }
    }
}
