package cn.com.glsx.admin.common.liaotian;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * @author taoyr
 */
public class ChatServer {

    private Selector selector;

    private ServerSocketChannel listenChannel;

    private static final int PORT = 9999;

    public ChatServer() {
        try {
            // 得到选择器
            selector = Selector.open();
            // serverSocketChannel
            listenChannel = ServerSocketChannel.open();
            // 绑定端口
            listenChannel.socket().bind(new InetSocketAddress(PORT));
            // 设置非阻塞模式
            listenChannel.configureBlocking(false);
            // 将该listenChannel 注册到Selector
            listenChannel.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen() {
        try {
            while (true) {
                int select = selector.select(2000);
                if (select > 0) {
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        // 取出selectionKey
                        SelectionKey key = iterator.next();
                        // 监听到OP_ACCEPT
                        if (key.isAcceptable()) {
                            SocketChannel sc = listenChannel.accept();
                            sc.configureBlocking(false);
                            // 将该socketChannel注册到Selector
                            sc.register(selector, SelectionKey.OP_READ);
                            // 提示
                            System.out.println(sc.getRemoteAddress() + " connected to the chat");
                        }
                        // 通道可读
                        if (key.isReadable()) {
                            // TODO处理读
                            readData(key);
                        }
                        // 当前的key删除, 防止重复处理
                        iterator.remove();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 发生异常处理
        }
    }

    /**
     *  读取客户端消息
     * @param key
     */
    private void readData(SelectionKey key){
        // 定义一个SocketChannel
        SocketChannel channel = null;
        try {
            // 得到channel
            channel = (SocketChannel) key.channel();

            // 创建缓冲buffer
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            int count = channel.read(buffer);
            // 根据count的值做处理
            if (count > 0){
                // 把缓冲区数据转为字符串并输出
                String msg = new String(buffer.array());
                // 输出该消息
                System.out.println("from Client: " + msg);

                // 向其他的客户端转发消息(去掉自己)
                sendInfoToOtherClients(msg,channel);
            }
        } catch (IOException e){
            try {
                System.out.println(channel.getRemoteAddress() + " is offline");
                // 取消注册
                key.cancel();
                // 关闭通道
                channel.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


    /**
     *  转发消息给其他客户端(channel）
     * @param msg
     * @param self
     */
    private void sendInfoToOtherClients(String msg, SocketChannel self) throws IOException {
        System.out.println("Server is transferring messages......");
        // 遍历, 所有注册到selector上的 SocketChannel, 并派出 自己
        for (SelectionKey key : selector.keys()){
            // 通过key取出对应的SocketChannel
            Channel targetChannel = key.channel();
            // 排除自己
            if (targetChannel instanceof SocketChannel && targetChannel != self){
                // 转型
                SocketChannel dest = (SocketChannel) targetChannel;
                // 将消息存储到buffer
                ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
                // 将buffer数据写入到通道
                dest.write(buffer);
            }
        }
    }


    public static void main(String[] args) {
        // 创建服务器对象
        ChatServer groupChatServer = new ChatServer();
        groupChatServer.listen();
    }
}
