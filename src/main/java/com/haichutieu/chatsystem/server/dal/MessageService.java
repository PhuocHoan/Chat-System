package com.haichutieu.chatsystem.server.dal;

import com.haichutieu.chatsystem.dto.ChatList;
import com.haichutieu.chatsystem.dto.Message;
import com.haichutieu.chatsystem.dto.MessageConversation;
import com.haichutieu.chatsystem.dto.MessageDisplay;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class MessageService {
    public static List<ChatList> getChatList(int customerID) {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createNativeQuery("""
                    WITH LatestMessages AS (
                         SELECT
                             m.conversation_id,
                             m.customer_id AS sender_id,
                             m.message,
                             m.time,
                             lm.status
                         FROM
                             message m
                         JOIN (
                            SELECT DISTINCT ON (m.conversation_id)
                                 m.conversation_id,
                                 m.time AS latest_time,
                                 md.status
                             FROM message m
                             JOIN message_display md
                                 ON m.id = md.message_id
                             WHERE md.customer_id = :customerID
                             ORDER BY m.conversation_id, m.time DESC
                         ) lm ON m.time = lm.latest_time
                     )
                     SELECT
                         c.id AS conversation_id,
                         CASE
                             WHEN c.is_group = TRUE THEN c.name
                             ELSE (
                                 SELECT
                                     cu.name
                                 FROM
                                     customer cu
                                 JOIN
                                     conversation_member cm2 ON cu.id = cm2.customer_id
                                 WHERE
                                     cm2.conversation_id = c.id AND cu.id != :customerID
                                 LIMIT 1
                             )
                         END AS conversation_name,
                         cu_sender.name AS sender_name,
                         lm.message AS latest_message,
                         lm.time AS latest_time,
                         c.is_group,
                         lm.status AS is_read
                     FROM
                         conversation c
                     JOIN
                         conversation_member cm ON c.id = cm.conversation_id
                     JOIN
                         LatestMessages lm ON c.id = lm.conversation_id
                     LEFT JOIN
                         customer cu_sender ON lm.sender_id = cu_sender.id
                     WHERE
                         cm.customer_id = :customerID
                     ORDER BY
                         lm.time;
                    """, ChatList.class).setParameter("customerID", customerID).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // get list member id of a conversation, for user
    public static List<Integer> getMemberConversation(long conversationID) {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createQuery("""
                        select customerID 
                        from ConversationMember 
                        where conversationID = :conversationID
                    """, Integer.class).setParameter("conversationID", conversationID).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // get list member id of a conversation except me
    public static List<Integer> getMemberConversation(long conversationID, int userID) {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createQuery("""
                        select customerID 
                        from ConversationMember 
                        where conversationID = :conversationID and customerID <> :userID
                    """, Integer.class).setParameter("conversationID", conversationID).setParameter("userID", userID).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // get list of conversation that the user joined.
    public static List<Long> getAllConversation(int userID) {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createQuery("""
                        select conversationID
                        from ConversationMember 
                        where customerID = :userID
                    """, Long.class).setParameter("userID", userID).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<MessageConversation> getMessageConversation(long conversationID, int userID) {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createNativeQuery("""
                    select m.id, m.conversation_id, m.customer_id, c.name, m.time, m.message
                    from message m
                    join (select message_id from message_display where customer_id = :userID) md
                    on m.id = md.message_id left join customer c on m.customer_id = c.id
                    where m.conversation_id = :conversationID
                    order by m.time
                    """, MessageConversation.class).setParameter("conversationID", conversationID).setParameter("userID", userID).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void updateStatusConversation(long conversationID, int userID) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Query q = session.createNativeQuery("""
                      update message_display
                      set status = true
                      from (
                        select id from message m where m.conversation_id = :conversationID
                      ) m
                      where m.id = message_id and customer_id = :userID and status = false
                    """).setParameter("conversationID", conversationID).setParameter("userID", userID);
            System.out.println(q.executeUpdate());
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public static Long addMessage(MessageConversation message, List<Integer> members) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Message messagePersist = new Message(message.conversation_id, message.senderID, message.time, message.message);
            session.persist(messagePersist);

            for (int member_id : members) {
                if (member_id == message.senderID) {
                    session.persist(new MessageDisplay(messagePersist, member_id, true));
                    continue;
                }
                session.persist(new MessageDisplay(messagePersist, member_id, false));
            }
            transaction.commit();
            return messagePersist.getId(); // return new message id
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
        return -1L;
    }

    // remove on my side
    public static void removeMessage(Long messageID, int userID) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Query q = session.createNativeQuery("""
                      delete from message_display
                      where message_id = :messageID and customer_id = :userID
                    """).setParameter("messageID", messageID).setParameter("userID", userID);
            System.out.println(q.executeUpdate());
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    // remove for all members
    public static void removeMessage(Long messageID) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Message message = session.find(Message.class, messageID);
            if (message != null) {
                // Remove the message (cascade deletes messageDisplays)
                session.remove(message);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    // remove only on my side
    public static void removeAllMessage(Long conversationID, int userID) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Query q = session.createNativeQuery("""
                      delete from message_display md
                     using message m
                     where m.id = md.message_id and m.conversation_id = :conversation_id and md.customer_id = :userID
                    """).setParameter("conversation_id", conversationID).setParameter("userID", userID);
            System.out.println(q.executeUpdate());
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
}
