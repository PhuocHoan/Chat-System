package com.haichutieu.chatsystem.server.dal;

import com.haichutieu.chatsystem.dto.Customer;
import com.haichutieu.chatsystem.dto.LoginTime;
import com.haichutieu.chatsystem.server.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

public class CustomerService {
    public static void addCustomer(Customer customer) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(customer);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public static Customer getCustomerByUsername(String username) {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createQuery("from Customer C where C.username = :username", Customer.class).setParameter("username", username).uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Customer getCustomerByEmail(String email) {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createQuery("select C from Customer C where C.email = :email", Customer.class).setParameter("email", email).uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void addLoginCustomer(LoginTime loginTime) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(loginTime);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public static void updateLogoutCustomer(int id, int numberPeopleChatWith, int numberGroupChatWith) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Query q = session.createQuery("""
                        update LoginTime
                        set isOnline = false, numberPeopleChatWith = :numberPeopleChatWith, numberGroupChatWith = :numberGroupChatWith
                        where customerID = :id and isOnline = true
                    """).setParameter("id", id).setParameter("numberPeopleChatWith", numberPeopleChatWith).setParameter("numberGroupChatWith", numberGroupChatWith);
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
