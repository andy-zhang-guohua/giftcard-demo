package io.axoniq.demo.giftcard.api

import org.axonframework.modelling.command.TargetAggregateIdentifier

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery

// Commands 命令

//// 命令 : 发行礼品卡
data class IssueCommand(@TargetAggregateIdentifier val id: String, val amount: Int)
//// 命令 : 礼品卡消费
data class RedeemCommand(@TargetAggregateIdentifier val id: String, val amount: Int)
//// 命令 : 作废礼品卡 (实际逻辑 : 将礼品卡中的金额设置为0)
data class CancelCommand(@TargetAggregateIdentifier val id: String)

// Events 事件

//// 礼品卡发行事件
data class IssuedEvent(val id: String, val amount: Int)
//// 礼品卡消费事件
data class RedeemedEvent(val id: String, val amount: Int)
//// 礼品卡作废事件
data class CancelEvent(val id: String)

// Queries 查询参数对象定义

//// 礼品卡查询过滤器，用于查询的过滤参数
data class CardSummaryFilter(val idStartsWith: String = "")
//// 礼品卡数量查询 [参数1:filter]
class CountCardSummariesQuery(val filter: CardSummaryFilter = CardSummaryFilter()) {
    override fun toString(): String = "CountCardSummariesQuery"
}

//// 礼品卡分页查询 [参数1:偏移,参数2:数量限制,参数3:filter]
data class FetchCardSummariesQuery(val offset: Int, val limit: Int, val filter: CardSummaryFilter)

//// ???
class CountChangedUpdate

// Query Responses 查询结果对象定义

//// 数据库表/实体定义 : 实体名称 CardSummary ==> 表名 card_summary
//// 三个字段 : id : ID, initialValue : 初始金额 , remainingValue : 余额
@Entity
@NamedQueries(
        NamedQuery(
                name = "CardSummary.fetch",
                query = "SELECT c FROM CardSummary c WHERE c.id LIKE CONCAT(:idStartsWith, '%') ORDER BY c.id"
        ),
        NamedQuery(
                name = "CardSummary.count",
                query = "SELECT COUNT(c) FROM CardSummary c WHERE c.id LIKE CONCAT(:idStartsWith, '%')"
        )
)
data class CardSummary(@Id var id: String, var initialValue: Int, var remainingValue: Int) {
    constructor() : this("", 0, 0)
}

data class CountCardSummariesResponse(val count: Int, val lastEvent: Long)
