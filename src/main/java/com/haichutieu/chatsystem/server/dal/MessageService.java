package com.haichutieu.chatsystem.server.dal;

import com.haichutieu.chatsystem.dto.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.sql.Timestamp;
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

    // get list member of a conversation full information
    public static List<MemberConversation> getMemberConversationFullInfo(long conversationID) {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createQuery("""
                        select new MemberConversation(c.id, c.name, c.username, cm.isAdmin)
                        from ConversationMember cm join Customer c
                        on cm.customerID = c.id
                        where cm.conversationID = :conversationID
                    """, MemberConversation.class).setParameter("conversationID", conversationID).getResultList();
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
            Message messagePersist = null;
            if (message.senderID == null) {
                messagePersist = new Message(message.conversation_id, message.time, message.message);
            } else {
                messagePersist = new Message(message.conversation_id, message.senderID, message.time, message.message);
            }

            session.persist(messagePersist);

            for (int member_id : members) {
                if (message.senderID != null && member_id == message.senderID) {
                    session.persist(new MessageDisplay(messagePersist, member_id, true));
                    continue;
                }
                session.persist(new MessageDisplay(messagePersist, member_id, false));
            }
            transaction.commit();
            return messagePersist.getId(); // return new message id
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
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

    public static long createGroupConversation(int userID, Conversation newConversation, List<Integer> memberIDs) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(newConversation);
            session.persist(new ConversationMember(newConversation.getId(), userID, true));
            for (var memberID : memberIDs) {
                session.persist(new ConversationMember(newConversation.getId(), memberID, false));
            }

            transaction.commit();
            return newConversation.getId();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            return -1;
        }
    }

    public static boolean addMembersToGroupConversation(long conversationID, List<Integer> memberIDs) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            for (var memberID : memberIDs) {
                session.persist(new ConversationMember(conversationID, memberID, false));
            }
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    public static boolean removeMemberFromGroupConversation(long conversationID, int memberID) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Query q = session.createQuery("""
                      delete from ConversationMember
                      where conversationID = :conversationID and customerID = :memberID
                    """).setParameter("conversationID", conversationID).setParameter("memberID", memberID);
            int result1 = q.executeUpdate();

            // Delete message_display for the member
            Query q2 = session.createNativeQuery("""
                      delete from message_display
                      where message_id in (
                        select id from message
                        where conversation_id = :conversationID
                      ) and customer_id = :memberID
                    """).setParameter("conversationID", conversationID).setParameter("memberID", memberID);

            int result2 = q2.executeUpdate();
            transaction.commit();

            return result1 > 0 && result2 > 0;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateGroupName(long conversationID, String newName) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Query q = session.createQuery("""
                      update Conversation
                      set name = :newName
                      where id = :conversationID
                    """).setParameter("newName", newName).setParameter("conversationID", conversationID);
            int result = q.executeUpdate();
            transaction.commit();
            return result > 0;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    public static boolean assignGroupAdmin(long conversationID, int userId, boolean isAdmin) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Query q = session.createQuery("""
                      update ConversationMember
                      set isAdmin = :isAdmin
                      where conversationID = :conversationID and customerID = :userId
                    """).setParameter("isAdmin", isAdmin).setParameter("conversationID", conversationID).setParameter("userId", userId);
            int result = q.executeUpdate();
            transaction.commit();
            return result > 0;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }

    }

    public static long getSingleConversation(int userID, int friendID) {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            List<Long> conversationIDs = session.createNativeQuery("""
                    select c.id
                         from conversation c
                         join conversation_member cm on c.id = cm.conversation_id
                            and c.is_group = false and cm.customer_id = :userID
                         join conversation_member cm2 on c.id = cm2.conversation_id
                            and cm2.customer_id = :friendID
                    """, Long.class).setParameter("userID", userID).setParameter("friendID", friendID).getResultList();
            if (!conversationIDs.isEmpty()) {
                return conversationIDs.getFirst();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1L;
    }

    public static long createSingleConversation(int userID, int friendID) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            // create a new conversation
            transaction = session.beginTransaction();
            Conversation conversation = new Conversation();
            conversation.setIsGroup(false);
            conversation.setCreateDate(new Timestamp(System.currentTimeMillis()));
            session.persist(conversation);
            session.persist(new ConversationMember(conversation.getId(), userID, true));
            session.persist(new ConversationMember(conversation.getId(), friendID, false));
            transaction.commit();

            return conversation.getId();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            return -1L;
        }
    }
}
