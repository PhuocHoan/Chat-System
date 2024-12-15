package com.haichutieu.chatsystem.server.dal;

import com.haichutieu.chatsystem.dto.*;
import org.hibernate.Session;

import java.sql.Timestamp;
import java.util.List;

public class AdminService {
    public static List<SpamList> fetchAllSpamList() {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createQuery("select new SpamList(S.personID, C1.username, C1.email, C2.username, S.time) " +
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
            return session.createNativeQuery("""
                        select c.id, c.name, cm.is_admin
                        from conversation_member cm join customer c
                        on cm.customer_id = c.id
                        where cm.conversation_id = :conversationID
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

    // get number of years have new users
    public static Integer getYearsNewUsers() {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createNativeQuery("""
                            select date_part('year', create_date) as year 
                            from customer 
                            group by year
                            """, Integer.class)
                    .getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // get number of new users by month and year
    public static Integer getNewUsersByMonthYear(int year, int month) {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createNativeQuery("""
                            select count(create_date)
                             from customer
                             where date_part('month', create_date) = :month 
                             and date_part('year', create_date) = :year
                            """, Integer.class)
                    .setParameter("month", month)
                    .setParameter("year", year)
                    .getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
