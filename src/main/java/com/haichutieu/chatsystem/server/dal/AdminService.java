package com.haichutieu.chatsystem.server.dal;

import com.haichutieu.chatsystem.dto.*;
import jakarta.persistence.Tuple;
import org.hibernate.Session;

import java.sql.Timestamp;
import java.util.*;

public class AdminService {
    public static List<SpamList> fetchAllSpamList() {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createQuery("select new SpamList(C2.id, S.personID, C1.username, C1.email, C2.username, S.time) " +
                    "from SpamList S join Customer C1 on S.personID = C1.id " +
                    "join Customer C2 on S.customerID = C2.id " +
                    "where C1.isLock = false", SpamList.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<FriendCount> fetchFriendCountList() {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createNativeQuery("""
                    WITH DirectFriends AS (
                        SELECT
                            c.id AS customer_id,
                            COUNT(DISTINCT f.friend_id) AS direct_friends_count
                        FROM customer c
                        LEFT JOIN friend_list f ON c.id = f.customer_id AND f.is_friend = TRUE
                        GROUP BY c.id
                    ),
                    ReverseDirectFriends AS (
                        SELECT
                            c.id AS customer_id,
                            COUNT(DISTINCT f.customer_id) AS reverse_direct_friends_count
                        FROM customer c
                        LEFT JOIN friend_list f ON c.id = f.friend_id AND f.is_friend = TRUE
                        GROUP BY c.id
                    ),
                    FriendsOfFriends AS (
                        SELECT
                            c.id AS customer_id,
                            COUNT(DISTINCT fof.friend_id) AS friends_of_friends_count
                        FROM customer c
                        LEFT JOIN friend_list f1 ON c.id = f1.customer_id AND f1.is_friend = TRUE
                        LEFT JOIN friend_list fof ON f1.friend_id = fof.customer_id AND fof.is_friend = TRUE
                        WHERE fof.friend_id != c.id -- Exclude the customer themselves
                        GROUP BY c.id
                    ),
                    ReverseFriendsOfFriends AS (
                        SELECT
                            c.id AS customer_id,
                            COUNT(DISTINCT fof.customer_id) AS reverse_friends_of_friends_count
                        FROM customer c
                        LEFT JOIN friend_list f1 ON c.id = f1.friend_id AND f1.is_friend = TRUE
                        LEFT JOIN friend_list fof ON f1.customer_id = fof.friend_id AND fof.is_friend = TRUE
                        WHERE fof.customer_id != c.id -- Exclude the customer themselves
                        GROUP BY c.id
                    )
                    SELECT
                        c.id, c.username, c.create_date as created_date,
                        COALESCE(df.direct_friends_count, 0) + COALESCE(rdf.reverse_direct_friends_count, 0) AS friends_count,
                        COALESCE(fof.friends_of_friends_count, 0) + COALESCE(rfof.reverse_friends_of_friends_count, 0) AS friend_of_friends_count
                    FROM customer c
                    LEFT JOIN DirectFriends df ON c.id = df.customer_id
                    LEFT JOIN ReverseDirectFriends rdf ON c.id = rdf.customer_id
                    LEFT JOIN FriendsOfFriends fof ON c.id = fof.customer_id
                    LEFT JOIN ReverseFriendsOfFriends rfof ON c.id = rfof.customer_id;
                    """, FriendCount.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // get list of group chat, not dual group
    public static List<Conversation> getConversation() {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createQuery("""
                        from Conversation c 
                        where c.isGroup = true
                    """, Conversation.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // get list member of a conversation
    public static List<MemberConversation> getMemberConversation(long conversationID) {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createQuery("""
                        select new MemberConversation(c.id, c.name, cm.isAdmin)
                        from ConversationMember cm join Customer c
                        on cm.customerID = c.id
                        where cm.conversationID = :conversationID
                    """, MemberConversation.class).setParameter("conversationID", conversationID).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // get list of login user
    public static List<OnlineUserCount> getOnlineUserCountList() {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createNativeQuery("""
                    select c.id, 
                           c.name,
                           c.create_date,
                           count(l.time) as login_times,
                           sum(l.number_people_chat_with) as number_people_chat_with,
                           sum(l.number_group_chat_with) as number_group_chat_with 
                    from login_time l 
                    join customer c
                    on c.id = l.customer_id
                    group by c.id
                    """, OnlineUserCount.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // get list of login user with time range
    public static List<OnlineUserCount> getOnlineUserCountList(Timestamp from, Timestamp to) {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createNativeQuery("""
                    select c.id, 
                           c.name,
                           c.create_date,
                           count(l.time) as login_times,
                           sum(l.number_people_chat_with) as number_people_chat_with,
                           sum(l.number_group_chat_with) as number_group_chat_with
                    from login_time l
                    join customer c
                    on c.id = l.customer_id
                    where l.time >= :from and l.time <= :to
                    group by c.id
                    """, OnlineUserCount.class).setParameter("from", from).setParameter("to", to).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<Integer, List<Long>> getNewUsersMonthly() {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            List<Tuple> results = session.createNativeQuery("""
                    SELECT date_part('year', create_date) AS year, date_part('month', create_date) AS month, COUNT(create_date) AS count
                    FROM customer
                    GROUP BY year, month
                    ORDER BY year, month
                    """, Tuple.class).getResultList();

            Map<Integer, List<Long>> newUsersCount = new HashMap<>();

            results.forEach(result -> {
                int year = ((Number) result.get("year")).intValue();
                int month = ((Number) result.get("month")).intValue();
                long count = ((Number) result.get("count")).longValue();

                newUsersCount.computeIfAbsent(year, k -> new ArrayList<>(Collections.nCopies(12, 0L)));
                newUsersCount.get(year).set(month - 1, count);
            });

            return newUsersCount;
        }
    }

    public static Map<Integer, List<Long>> getAppUsageMonthly() {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            List<Tuple> results = session.createNativeQuery("""
                    SELECT date_part('year', time) AS year, date_part('month', time) AS month, COUNT(time) AS count
                    FROM login_time
                    GROUP BY year, month
                    ORDER BY year, month
                    """, Tuple.class).getResultList();

            Map<Integer, List<Long>> appUsageCount = new HashMap<>();

            results.forEach(result -> {
                int year = ((Number) result.get("year")).intValue();
                int month = ((Number) result.get("month")).intValue();
                long count = ((Number) result.get("count")).longValue();

                appUsageCount.computeIfAbsent(year, k -> new ArrayList<>(Collections.nCopies(12, 0L))); // create list of 12 months if not exist to value 0 type Long
                appUsageCount.get(year).set(month - 1, count); // set count to the corresponding month
            });

            return appUsageCount;
        }
    }

    public static boolean deleteSpam(int customerId, int spamId) {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            session.beginTransaction();
            session.createNativeQuery("""
                    DELETE FROM spam_list
                    WHERE customer_id = :customerId AND person_id = :spamId
                    """).setParameter("customerId", customerId).setParameter("spamId", spamId).executeUpdate();
            session.getTransaction().commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Customer> fetchNewAccounts(int rows) {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createQuery("""
                    from Customer c
                    order by c.createDate desc
                    """, Customer.class).setMaxResults(rows).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Customer getAdminAccount(String username) {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createQuery("""
                    from Customer c
                    where c.username = :username and c.isAdmin = true
                    """, Customer.class).setParameter("username", username).uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<SpamList> fetchSpamList(Timestamp fromDate, Timestamp toDate) {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createQuery("""
                    select new SpamList(C2.id, S.personID, C1.username, C1.email, C2.username, S.time) 
                    from SpamList S join Customer C1 on S.personID = C1.id 
                    join Customer C2 on S.customerID = C2.id 
                    where S.time >= :fromDate and S.time <= :toDate
                    """, SpamList.class).setParameter("fromDate", fromDate).setParameter("toDate", toDate).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
