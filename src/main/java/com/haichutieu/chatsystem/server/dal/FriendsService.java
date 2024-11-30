package com.haichutieu.chatsystem.server.dal;

import com.haichutieu.chatsystem.server.dto.Customer;
import com.haichutieu.chatsystem.server.util.HibernateUtil;
import com.haichutieu.chatsystem.server.util.SessionManager;
import org.hibernate.Session;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FriendsService {

    public FriendsService() {
    }

    public List<Customer> fetchFriends(long userID) {
        // SessionManager.getInstance().getCurrentUser().getId()
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Customer c where c.id IN (" + "select f.friendID from FriendList f where f.customerID = :id and f.isFriend = true)", Customer.class).
                    setParameter("id", userID).
                    getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch friends", e);
        }
    }
}
