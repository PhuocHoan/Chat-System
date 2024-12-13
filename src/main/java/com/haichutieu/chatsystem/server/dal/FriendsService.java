package com.haichutieu.chatsystem.server.dal;

import com.haichutieu.chatsystem.dto.Customer;
import com.haichutieu.chatsystem.dto.FriendList;
import com.haichutieu.chatsystem.dto.SpamList;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.sql.Timestamp;
import java.util.List;

public class FriendsService {

    public FriendsService() {
    }

    public static List<Customer> fetchFriends(int userID) {
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
            System.out.println("[ERROR] Failed to fetch friends: " + e.getMessage());
            return List.of();
        }
    }

    public static boolean addFriend(int userID, int friendID) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Query q = session.createQuery("""
                                insert into FriendList(customerID, friendID)
                                values (:id, :friendID)
                            """).setParameter("id", userID)
                    .setParameter("friendID", friendID);
            int result = q.executeUpdate();
            transaction.commit();

            return result > 0;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("[ERROR] Failed to add friend: " + e.getMessage());
            return false;
        }
    }

    public static boolean removeFriend(int userID, int friendID) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Query q = session.createQuery("""
                                delete from FriendList f
                                     where (f.customerID = :id and f.friendID = :friendID)
                                        or (f.friendID = :id and f.customerID = :friendID)
                            """).setParameter("id", userID)
                    .setParameter("friendID", friendID);
            int result = q.executeUpdate();
            transaction.commit();

            return result > 0;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("[ERROR] Failed to remove friend: " + e.getMessage());
            return false;
        }
    }

    public static boolean reportSpam(int userID, int spamID) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            SpamList spam = new SpamList();
            spam.setCustomerID(userID);
            spam.setPersonID(spamID);
            spam.setTime(new Timestamp(System.currentTimeMillis()));

            Query q = session.createQuery("""
                                insert into SpamList(customerID, personID, time)
                                values (:id, :spamID, :time)
                            """).setParameter("id", spam.getCustomerID())
                    .setParameter("spamID", spam.getPersonID()).setParameter("time", spam.getTime());
            int result = q.executeUpdate();
            transaction.commit();

            return result > 0;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("[ERROR] Failed to report spam: " + e.getMessage());
            return false;
        }
    }

    public static boolean blockUser(int userID, int blockedID) {
        // Add to block_list and remove from friend_list if exist
        Transaction transaction = null;
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Remove from friend_list if exists
            Query q1 = session.createQuery("""
                                delete from FriendList f
                                     where (f.customerID = :id and f.friendID = :blockedID)
                                        or (f.friendID = :id and f.customerID = :blockedID)
                            """).setParameter("id", userID)
                    .setParameter("blockedID", blockedID);

            // Add to block_list
            Query q = session.createQuery("""
                                insert into BlockList(customerID, personID)
                                values (:id, :blockedID)
                            """).setParameter("id", userID)
                    .setParameter("blockedID", blockedID);

            int result1 = q1.executeUpdate();
            int result = q.executeUpdate();
            transaction.commit();

            return (result > 0 && result1 > 0);
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("[ERROR] Failed to block friend: " + e.getMessage());
            return false;
        }
    }

    // Search for users with name or username like "prompt"
    // not friends with userID and not block userID
    public static List<Customer> fetchUsers(int userID, String prompt) {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createQuery("""
                                select distinct c
                                     from Customer c
                                     where c.id != :id
                                        and not exists (
                                            select 1
                                                from FriendList f
                                                where (f.customerID = :id and f.friendID = c.id)
                                                    or (f.friendID = :id and f.customerID = c.id)
                                        )
                                        and not exists (
                                            select 1
                                                from BlockList b
                                                where (b.personID = :id and b.customerID = c.id)
                                        )
                                        and (c.username like :prompt or c.name like :prompt)
                            """, Customer.class)
                    .setParameter("id", userID)
                    .setParameter("prompt", "%" + prompt + "%")
                    .getResultList();
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to fetch users: " + e.getMessage());
            return null;
        }

    }

    public static List<Customer> fetchFriendRequests(int id) {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createQuery("""
                                select new Customer(f.customerID, c.username, c.name)
                                     from FriendList f
                                     join Customer c on c.id = f.customerID
                                     where f.friendID = :id
                                        and f.isFriend = false
                            """, Customer.class)
                    .setParameter("id", id)
                    .getResultList();
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to fetch friend requests: " + e.getMessage());
            return null;
        }
    }

    public static boolean acceptFriend(int userID, int friendID) {
        Transaction transaction;

        // Get the friend_list row from db
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            FriendList friendList = session.createQuery("""
                                select f
                                     from FriendList f
                                     where f.customerID = :id and f.friendID = :friendID
                            """, FriendList.class)
                    .setParameter("id", friendID)
                    .setParameter("friendID", userID)
                    .getSingleResult();

            if (friendList == null) {
                return false;
            }

            // Update the row to isFriend = true
            transaction = session.beginTransaction();
            friendList.setFriend(true);
            session.merge(friendList);
            transaction.commit();
            return true;
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to accept friend: " + e.getMessage());
            return false;
        }
    }

    public static boolean rejectFriend(int userID, int friendID) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Query q = session.createQuery("""
                                delete from FriendList f
                                     where f.customerID = :id and f.friendID = :friendID
                            """).setParameter("id", friendID)
                    .setParameter("friendID", userID);
            int result = q.executeUpdate();
            transaction.commit();

            return result > 0;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("[ERROR] Failed to reject friend: " + e.getMessage());
            return false;
        }
    }
}
