package com.haichutieu.chatsystem.server.dal;

import com.haichutieu.chatsystem.dto.Customer;
import com.haichutieu.chatsystem.server.util.HibernateUtil;
import org.hibernate.Session;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FriendsService {

    public FriendsService() {
    }

    public static List<Customer> fetchFriends(long userID) {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createQuery("""
                                select distinct c
                                     from Customer c
                                     join FriendList f on (f.customerID = :id and f.friendID = c.id)
                                        or (f.friendID = :id and f.customerID = c.id)
                                     where f.isFriend = true
                            """, Customer.class)
                    .setParameter("id", userID)
                    .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch friends", e);
        }
    }

    public static void removeFriend(long userID, long friendID) {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            session.beginTransaction();
            session.createQuery("""
                                delete from FriendList f
                                     where (f.customerID = :id and f.friendID = :friendID)
                                        or (f.friendID = :id and f.customerID = :friendID)
                            """)
                    .setParameter("id", userID)
                    .setParameter("friendID", friendID)
                    .executeUpdate();
            session.getTransaction().commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove friend", e);
        }
    }
}
