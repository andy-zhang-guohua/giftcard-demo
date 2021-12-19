package io.axoniq.demo.giftcard.command;

import io.axoniq.demo.giftcard.api.CancelCommand;
import io.axoniq.demo.giftcard.api.CancelEvent;
import io.axoniq.demo.giftcard.api.IssueCommand;
import io.axoniq.demo.giftcard.api.IssuedEvent;
import io.axoniq.demo.giftcard.api.RedeemCommand;
import io.axoniq.demo.giftcard.api.RedeemedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.context.annotation.Profile;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

/**
 * 2021-12-19 学习总结 :
 * 1. 这里定义了一个聚合，也是一个聚合根，也是一个 Spring bean 组件，但是是原型模式组件而不是单例组件
 * 2. 该组件提供了处理针对礼品卡的命令Command和事件Event处理逻辑, 命令处理逻辑中会最终委托给事件处理逻辑执行最终的业务逻辑
 * 3. 学习关键字
 * 3.1 注解 @Aggregate, @AggregateIdentifier,@CommandHandler,@EventSourcingHandler
 * 3.3 axon 静态工具方法 apply : org.axonframework.modelling.command.AggregateLifecycle.apply
 * 4. 问题
 * 4.1 Q : 处理命令或者事件之后,礼品卡的持久化在哪里做的 ?
 */
@Profile("command")
@Aggregate(cache = "giftCardCache") // 定义一个聚合, 表示当前类是 聚合根， 也是一个 Spring bean 组件(采用了原型模式,而不是单例模式)
public class GiftCard {

    @AggregateIdentifier // 表明该属性是聚合的ID
    private String giftCardId; // 礼品卡ID
    private int remainingValue; // 余额

    // 构造方法，注意该构造方法也是一个命令处理器，能处理的命令是 IssueCommand
    @CommandHandler
    public GiftCard(IssueCommand command) {
        // 1. 参数合法性校验
        if (command.getAmount() <= 0) {
            throw new IllegalArgumentException("amount <= 0");
        }

        // 2. 应用命令，注意这里统一委托给 axon 静态工具 apply 方法，而不是当前方法自己执行命令处理逻辑
        apply(new IssuedEvent(command.getId(), command.getAmount()));
    }

    // 礼品卡消费 命令处理方法
    @CommandHandler
    public void handle(RedeemCommand command) {
        // 1. 参数合法性校验
        if (command.getAmount() <= 0) {
            throw new IllegalArgumentException("amount <= 0");
        }
        if (command.getAmount() > remainingValue) {
            throw new IllegalStateException("amount > remaining value");
        }

        // 2. 应用命令，注意这里统一委托给 axon 静态工具 apply 方法，而不是当前方法自己执行命令处理逻辑
        apply(new RedeemedEvent(giftCardId, command.getAmount()));
    }

    // 礼品卡作废 命令处理方法
    @CommandHandler
    public void handle(CancelCommand command) {
        // 1. 参数合法性校验
        //// 这里命令本身表明了意图，无其他参数，无需校验

        // 2. 应用命令，注意这里统一委托给 axon 静态工具 apply 方法，而不是当前方法自己执行命令处理逻辑
        apply(new CancelEvent(giftCardId));
    }

    // 事件处理方法 : 发行礼品卡事件
    @EventSourcingHandler
    public void on(IssuedEvent event) {
        giftCardId = event.getId();
        remainingValue = event.getAmount();
    }

    // 事件处理方法 : 礼品卡消费事件
    @EventSourcingHandler
    public void on(RedeemedEvent event) {
        remainingValue -= event.getAmount();
    }

    // 事件处理方法 : 作废礼品卡事件
    @EventSourcingHandler
    public void on(CancelEvent event) {
        remainingValue = 0;
    }

    public GiftCard() {
        // Required by Axon to construct an empty instance to initiate Event Sourcing.
    }
}

