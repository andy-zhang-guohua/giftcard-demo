package io.axoniq.demo.giftcard.gui;

import io.axoniq.demo.giftcard.api.IssueCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * 礼品卡批量发行器
 */
public class BulkIssuer {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // 计数器 : 记录一次礼品卡批量发行事件中发行成功的礼品卡数量
    private final AtomicInteger success = new AtomicInteger();
    // 计数器 : 记录一次礼品卡批量发行事件中发行失败的礼品卡数量
    private final AtomicInteger error = new AtomicInteger();
    // 计数器 : 一次礼品卡批量发行会被指定发行多少张，这里记录剩余发行张数 , 该计数器 会被初始化成 计划发行总张数
    private final AtomicInteger remaining = new AtomicInteger();

    public BulkIssuer(CommandGateway commandGateway, // 命令网关
                      int number, // 计划发行总张数
                      int amount, // 计划发行的礼品卡的面额
                      Consumer<BulkIssuer> callback // 发行过程中的回调函数，应该是主要用于进度跟踪
    ) {
        remaining.set(number); // 初始化为计划发行总张数

        // 使用一个线程进行异步发行
        new Thread(() -> {
            for (int i = 0; i < number; i++) {
                String id = UUID.randomUUID().toString().substring(0, 11).toUpperCase(); // 使用UUID作为新建礼品卡的ID
                commandGateway.send(new IssueCommand(id, amount)) // 往命令网关上发送一个礼品卡发行命令
                        .whenComplete( // 一个礼品卡发行结束时的回调处理逻辑
                                (Object result, Throwable throwable) -> {
                                    if (throwable == null) {
                                        success.incrementAndGet();
                                    } else {
                                        error.incrementAndGet();
                                    }
                                    remaining.decrementAndGet();
                                });
            }
        }).start();


        // 使用另外一个线程等待发行完成:
        // 1. 该线程在批量发行未完成时每隔一秒调用一次回调函数 callback
        // 2. 该线程在批量发行完成时调用一次回调函数 callback
        // 3. 该线程再批量发行完成时会结束
        new Thread(() -> {
            try {
                while (remaining.get() > 0) {
                    callback.accept(this);
                    Thread.sleep(1000);
                }
                callback.accept(this);
            } catch (InterruptedException ex) {
                logger.error("Interrupted", ex);
            }
        }).start();
    }

    public AtomicInteger getSuccess() {
        return success;
    }

    public AtomicInteger getError() {
        return error;
    }

    public AtomicInteger getRemaining() {
        return remaining;
    }
}
