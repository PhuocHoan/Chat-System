package com.haichutieu.chatsystem.server.dal;

import com.haichutieu.chatsystem.dto.ChatList;
import com.haichutieu.chatsystem.server.util.HibernateUtil;
import org.hibernate.Session;

import java.util.List;

public class MessageService {
    public static List<ChatList> getChatList(int customerId) {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createNativeQuery("""
                    WITH LatestMessages AS (
                         SELECT
                             m.conversation_id,
                             m.customer_id AS sender_id,
                             m.message,
                             m.time
                         FROM
                             message m
                         JOIN (
                             SELECT
                                 MAX(time) AS latest_time
                             FROM
                                 message
                             GROUP BY
                                 conversation_id
                         ) lm ON m.time = lm.latest_time
                     )
                     SELECT
                         c.id AS conversation_id,
                         CASE
                             WHEN c.is_group = TRUE THEN c.name
                             ELSE
                                 (SELECT cu.name
                                  FROM customer cu
                                  JOIN conversation_member cm2 ON cu.id = cm2.customer_id
                                  WHERE cm2.conversation_id = c.id AND cu.id != :customerId)
                         END AS conversation_name,
                         cu_sender.name AS sender_name,
                         lm.message AS latest_message,
                         lm.time AS latest_time
                     FROM
                         conversation c
                     LEFT JOIN
                         conversation_member cm ON c.id = cm.conversation_id
                     LEFT JOIN
                         LatestMessages lm ON c.id = lm.conversation_id
                     LEFT JOIN
                         customer cu_sender ON lm.sender_id = cu_sender.id
                     WHERE
                         cm.customer_id = :customerId
                         AND (c.is_group = TRUE OR lm.conversation_id IS NOT NULL)
                     GROUP BY
                         c.id, c.name, lm.message, lm.time, cu_sender.name
                    """, ChatList.class).setParameter("customerId", customerId).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
