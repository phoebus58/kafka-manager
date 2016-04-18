/**
 * Copyright 2015 Yahoo Inc. Licensed under the Apache License, Version 2.0
 * See accompanying LICENSE file.
 */

package controllers.api

import controllers.KafkaManagerContext
import features.ApplicationFeatures
import kafka.manager.utils.JsonHelpers
import models.navigation.Menus
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json._
import play.api.mvc._


/**
 * @author jisookim0513
 */

class KafkaStateCheck (val messagesApi: MessagesApi, val kafkaManagerContext: KafkaManagerContext)
                      (implicit af: ApplicationFeatures, menus: Menus) extends Controller with I18nSupport {

  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  private[this] val kafkaManager = kafkaManagerContext.getKafkaManager

  def brokers(c: String) = Action.async { implicit request =>
    kafkaManager.getBrokerList(c).map { errorOrBrokerList =>
      errorOrBrokerList.fold(
        error => BadRequest(Json.obj("msg" -> error.msg)),
        brokerList => Ok(Json.obj("brokers" -> brokerList.list.map(bi => bi.id).sorted))
      )
    }
  }

  def topics(c: String) = Action.async { implicit request =>
    kafkaManager.getTopicList(c).map { errorOrTopicList =>
      errorOrTopicList.fold(
        error => BadRequest(Json.obj("msg" -> error.msg)),
        topicList => Ok(Json.obj("topics" -> topicList.list.sorted))
      )
    }
  }

  def underReplicatedPartitions(c: String, t: String) = Action.async { implicit request =>
    kafkaManager.getTopicIdentity(c,t).map { errorOrTopicIdentity =>
      errorOrTopicIdentity.fold(
        error => BadRequest(Json.obj("msg" -> error.msg)),
        topicIdentity => Ok(Json.obj("topic" -> t, "underReplicatedPartitions" -> topicIdentity.partitionsIdentity.filter(_._2.isUnderReplicated).map{case (num, pi) => pi.partNum}))
      )
    }
  }

  def unavailablePartitions(c: String, t: String) = Action.async { implicit request =>
    kafkaManager.getTopicIdentity(c,t).map { errorOrTopicIdentity =>
      errorOrTopicIdentity.fold(
        error => BadRequest(Json.obj("msg" -> error.msg)),
        topicIdentity => Ok(Json.obj("topic" -> t, "unavailablePartitions" -> topicIdentity.partitionsIdentity.filter(_._2.isr.isEmpty).map{case (num, pi) => pi.partNum}))
      )
    }
  }

  def consumerGroups(c: String, t: String) = Action.async { implicit request =>
    kafkaManager.getConsumersForTopic(c, t).map { maybeTopics =>
      maybeTopics.fold(BadRequest(Json.obj("msg" -> s"No consumers found for cluster $c and topic $t"))) { topics =>
        val topicsJson = topics.map {
          case (name, consumerType) => Json.obj("name" -> name, "type" -> consumerType.toString)
        }

        Ok(Json.obj("groups" -> topicsJson))
      }
    }
  }

  def lag(c: String, t: String, cg: String, ct: String) = Action.async { implicit request =>
    import JsonHelpers._

    kafkaManager.getConsumedTopicState(c, cg, t, ct).map { errorOrConsumedTopicState =>
      errorOrConsumedTopicState.fold(
        error => BadRequest(Json.obj("msg" -> error.msg)),
        consumedTopicState => Ok(Json.obj("lag" -> consumedTopicState.totalLag.getOrElse(0L).toJsNumber))
      )
    }
  }
}

