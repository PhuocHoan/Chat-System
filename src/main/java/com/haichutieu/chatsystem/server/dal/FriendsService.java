package com.haichutieu.chatsystem.server.dal;

import com.haichutieu.chatsystem.dto.Customer;
import com.haichutieu.chatsystem.server.util.HibernateUtil;
import org.hibernate.Session;

import java.util.List;

public class FriendsService {

    public FriendsService() {
    }

    public List<Customer> fetchFriends(int userID) {
        // SessionManager.getInstance().getCurrentUser().getId()
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
}
