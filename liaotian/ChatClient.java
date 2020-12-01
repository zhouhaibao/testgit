package cn.com.glsx.admin.common.liaotian;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;

/**
 * @author taoyr
 */
public class ChatClient {

    // 定义相关属性

    private final int PORT = 9999;
    private Selector selector;
    private SocketChannel socketChannel;
    private java.lang.String username;

    /**
     * 构造器, 完成初始化工作
     */
    public ChatClient() throws IOException {

        selector = Selector.open();

        // 连接服务器
        socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", PORT));

        // 设置非阻塞
        socketChannel.configureBlocking(false);

        // 将channel 注册到selector
        socketChannel.register(selector, SelectionKey.OP_READ);

        // 得到username
        username = socketChannel.getLocalAddress().toString().substring(1);

        System.out.println(username + " is fine");

    }

    /**
     * 向服务器发送消息
     *
     * @param info
     */
    public void sendInfo(java.lang.String info) {
        info = username + " said: " + info;
        try {
            socketChannel.write(ByteBuffer.wrap(info.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void readInfo() {
        try {
            int readChannels = selector.select();
            // 有可用的通道
            if (readChannels > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    // 客户端只考虑可读
                    if (key.isReadable()) {
                        // 得到相关通道
                        SocketChannel sc = (SocketChannel) key.channel();
                        // 得到一个Buffer
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        // 读取
                        sc.read(buffer);
                        // 把读到的缓冲区数据转成字符串
                        String msg1 = new String(buffer.array());
                        String msg = Arrays.toString((buffer.array()));
                        System.out.println(msg1.trim());
                    }
                }
                iterator.remove(); // 删除当前的selectionKey, 防止重复操作
            } else {
                System.out.println("No channel available");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
