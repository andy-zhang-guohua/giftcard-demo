package io.axoniq.demo.giftcard.gui;

import com.vaadin.annotations.Push;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import io.axoniq.demo.giftcard.api.*;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Profile("gui")
@SpringUI
@Push
public class GiftCardUI extends UI {

    private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final CommandGateway commandGateway; // 命令网关
    private final QueryGateway queryGateway; // 查询网关
    private CardSummaryDataProvider cardSummaryDataProvider;
    private ScheduledFuture<?> updaterThread;

    public GiftCardUI(CommandGateway commandGateway, QueryGateway queryGateway) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
    }

    /**
     * 初始化UI界面 (每当用户在浏览器中输入该页面地址访问该页面时会被调用 )
     * 1. 布局
     * 2. 组件
     * 3. 组件的事件处理逻辑
     *
     * @param vaadinRequest
     */
    @Override
    protected void init(VaadinRequest vaadinRequest) {
        HorizontalLayout commandBar = new HorizontalLayout();
        commandBar.setWidth("100%");
        commandBar.addComponents(issuePanel(), bulkIssuePanel(), redeemPanel());

        Grid<CardSummary> summary = summaryGrid();

        HorizontalLayout statusBar = new HorizontalLayout();
        Label statusLabel = new Label("Status");
        statusBar.setDefaultComponentAlignment(Alignment.MIDDLE_RIGHT);
        statusBar.addComponent(statusLabel);
        statusBar.setWidth("100%");

        VerticalLayout layout = new VerticalLayout();
        layout.addComponents(commandBar, summary, statusBar);
        layout.setExpandRatio(summary, 1f);
        layout.setSizeFull();

        setContent(layout);

        UI.getCurrent().setErrorHandler(new DefaultErrorHandler() {
            @Override
            public void error(com.vaadin.server.ErrorEvent event) {
                Throwable cause = event.getThrowable();
                logger.error("An error occurred", cause);
                while (cause.getCause() != null) {
                    cause = cause.getCause();
                }
                Notification.show("Error", cause.getMessage(), Notification.Type.ERROR_MESSAGE);
            }
        });

        setPollInterval(1000);
        int offset = Page.getCurrent().getWebBrowser().getTimezoneOffset();
        // offset is in milliseconds
        ZoneOffset instantOffset = ZoneOffset.ofTotalSeconds(offset / 1000);
        StatusUpdater statusUpdater = new StatusUpdater(statusLabel, instantOffset);

        // 新建数据更新线程,
        //// 1. 使用了线程池方式，但是池中只有一个线程
        //// 2. 线程新建之后1秒执行一次刷新，之后每隔5秒钟执行一次刷新
        //// 3. 每次的刷新逻辑是 : statusUpdater
        updaterThread = Executors.newScheduledThreadPool(1).scheduleAtFixedRate(statusUpdater, 1000,
                5000, TimeUnit.MILLISECONDS);

        // 设置页面数据拉取间隔 : 1秒
        setPollInterval(1000);

        // 设置用户会话时长 : 60分钟
        getSession().getSession().setMaxInactiveInterval(60 * 60);

        // 页面关闭时的处理逻辑 : 其实只是在该页面的地址栏中重新输入相应URL，或者重新回车时才会被调用 -- 2021-12-19
        //// 问题 : 关闭浏览器TAB或者关闭浏览器时为什么不执行该方法 ?
        addDetachListener((DetachListener) detachEvent -> {
            logger.warn("Closing UI");

            // 取消被调度的页面状态拉取任务
            updaterThread.cancel(true);
        });

    }

    /**
     * 构造 礼品卡发行 功能面板
     *
     * @return
     */
    private Panel issuePanel() {
        TextField id = new TextField("礼品卡号");
        TextField amount = new TextField("面额");
        Button submit = new Button("提交");

        submit.addClickListener(evt -> {
            commandGateway.sendAndWait(new IssueCommand(id.getValue(), Integer.parseInt(amount.getValue())));
            Notification.show("成功", Notification.Type.HUMANIZED_MESSAGE)
                    .addCloseListener(e -> cardSummaryDataProvider.refreshAll());
        });

        FormLayout form = new FormLayout();
        form.addComponents(id, amount, submit);
        form.setMargin(true);

        Panel panel = new Panel("发行礼品卡");
        panel.setContent(form);
        return panel;
    }

    /**
     * 构造 批量礼品卡发行 功能面板
     *
     * @return
     */
    private Panel bulkIssuePanel() {
        TextField number = new TextField("数量");
        TextField amount = new TextField("面额");
        Button submit = new Button("提交");
        Panel panel = new Panel("批量发行礼品卡");

        submit.addClickListener(// 按钮事件处理函数
                evt -> {
                    submit.setEnabled(false); // 禁用提交按钮
                    new BulkIssuer(
                            commandGateway, // 命令网关
                            Integer.parseInt(number.getValue()), // 发行数量
                            Integer.parseInt(amount.getValue()), // 每张礼品卡的面额
                            // 回调函数 : 批量发行进度跟踪器
                            bulkIssuer -> access(() -> {
                                if (bulkIssuer.getRemaining().get() == 0) {
                                    submit.setEnabled(true);
                                    panel.setCaption("批量发行礼品卡");
                                    Notification.show("批量发行礼品卡完成", Notification.Type.HUMANIZED_MESSAGE)
                                            .addCloseListener(e -> cardSummaryDataProvider.refreshAll());
                                } else {
                                    panel.setCaption(String.format(
                                            "进度: %d 成功, %d 失败, %d 待做",
                                            bulkIssuer.getSuccess().get(),
                                            bulkIssuer.getError().get(),
                                            bulkIssuer.getRemaining().get()
                                    ));
                                    cardSummaryDataProvider.refreshAll();
                                }
                            })
                    );
                });

        FormLayout form = new FormLayout();
        form.addComponents(number, amount, submit);
        form.setMargin(true);

        panel.setContent(form);
        return panel;
    }

    /**
     * 构建 礼品卡消费 功能面板
     *
     * @return
     */
    private Panel redeemPanel() {
        TextField id = new TextField("礼品卡号");
        TextField amount = new TextField("消费金额");
        Button submit = new Button("提交");

        submit.addClickListener(evt -> {
            commandGateway.sendAndWait(new RedeemCommand(id.getValue(), Integer.parseInt(amount.getValue())));
            Notification.show("成功", Notification.Type.HUMANIZED_MESSAGE)
                    .addCloseListener(e -> cardSummaryDataProvider.refreshAll());
        });

        FormLayout form = new FormLayout();
        form.addComponents(id, amount, submit);
        form.setMargin(true);

        Panel panel = new Panel("礼品卡消费登记");
        panel.setContent(form);
        return panel;
    }

    /**
     * 构建 礼品卡列表 表格, 查询结果
     * @return
     */
    private Grid<CardSummary> summaryGrid() {
        cardSummaryDataProvider = new CardSummaryDataProvider(queryGateway);
        Grid<CardSummary> grid = new Grid<>();
        grid.addColumn(CardSummary::getId).setCaption("礼品卡号");
        grid.addColumn(CardSummary::getInitialValue).setCaption("面额");
        grid.addColumn(CardSummary::getRemainingValue).setCaption("余额");
        grid.setSizeFull();
        grid.setDataProvider(cardSummaryDataProvider);
        return grid;
    }

    /**
     * UI界面状态更新器
     * 1. 先查询礼品卡统计信息
     * 2. 成功时返回结果，没有异常的话显示上一次查询的时间戳
     */
    private class StatusUpdater implements Runnable {

        private final Label statusLabel;
        private final ZoneOffset instantOffset;

        public StatusUpdater(Label statusLabel, ZoneOffset instantOffset) {
            this.statusLabel = statusLabel;
            this.instantOffset = instantOffset;
        }

        @Override
        public void run() {
            // 执行一次礼品卡查询，
            //// 1. 使用参数对象 : new CountCardSummariesQuery() , 表示查询所有礼品卡
            //// 2. 返回结果类型使用 CountCardSummariesResponse.class
            //// 3. 如果查询正常，在状态条上显示最近一次查询的时间戳
            queryGateway.query(new CountCardSummariesQuery(), CountCardSummariesResponse.class)
                    .whenComplete((r, exception) -> {
                        if (exception == null) {
                            // 如果没有异常，状态条上显示的是一个事件字符串
                            statusLabel.setValue(Instant.ofEpochMilli(r.getLastEvent()).atOffset(instantOffset).toString());
                        }
                    });
        }
    }
}
