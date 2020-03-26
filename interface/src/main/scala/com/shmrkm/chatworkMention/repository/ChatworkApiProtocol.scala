package com.shmrkm.chatworkMention.repository

import com.shmrkm.chatworkWebhook.domain.model.account.FromAccountId

object ChatworkApiProtocol {
}

sealed trait ChatworkApiResponse

case class AccountResponse(account_id: Int, name: String, avatar_image_url: String)

case class MeResponse(
                       account_id: Int,
                       name: String,
                     ) extends ChatworkApiResponse

case class RoomResponse(
                         room_id: Int,
                         name: String,
                         `type`: String,
                         role: String,
                         sticky: Boolean,
                         unread_num: Int,
                         mention_num: Int,
                         mytask_num: Int,
                         message_num: Int,
                         file_num: Int,
                         task_num: Int,
                         icon_path: String,
                         last_update_time: Int,
                         description: String,
                       ) extends ChatworkApiResponse

case class MemberResponseItem(
                               account_id: Int,
                               role: String,
                               name: String,
                               chatwork_id: String,
                               organization_id: Int,
                               organization_name: String,
                               department: String,
                               avatar_image_url: String,
                             )

case class MemberResponse(
                           values: Seq[MemberResponseItem]
                         ) extends ChatworkApiResponse {
  def filterBy(accountId: FromAccountId): Seq[MemberResponseItem] = values.filter(_.account_id == accountId.value)
}
