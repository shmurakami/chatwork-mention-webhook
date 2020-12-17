package com.shmrkm.chatworkWebhook.mention.protocol.query

import com.shmrkm.chatworkWebhook.domain.model.mention.MentionList

case class MentionListResponse(mentionList: MentionList)

object MentionListResponse {
  import io.circe.Encoder
  import io.circe.Json._

  implicit val encoder: Encoder[MentionListResponse] = (list: MentionListResponse) => {
    obj(
      "list" -> arr(list.mentionList.list.map { message =>
        obj(
          "id"                      -> fromString(message.id.value),
          "room_id"                 -> fromLong(message.roomId.value),
          "room_name"               -> fromString(message.roomName.value),
          "room_icon_url"           -> fromString(message.roomIconUrl.value),
          "from_account_id"         -> fromLong(message.fromAccountId.value),
          "from_account_name"       -> fromString(message.fromAccountName.value),
          "from_account_avatar_url" -> fromString(message.fromAccountAvatarUrl.value),
          "to_account_id"           -> fromLong(message.toAccountId.value),
          "body"                    -> fromString(message.body.value),
          "send_time"               -> fromInt(message.sendTime.value),
          "update_time"             -> fromInt(message.updateTime.value)
        )
      }: _*)
    )
  }
}
