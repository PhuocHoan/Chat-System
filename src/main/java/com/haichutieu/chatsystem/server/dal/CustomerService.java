package com.haichutieu.chatsystem.server.dal;

import com.haichutieu.chatsystem.dto.Customer;
import com.haichutieu.chatsystem.dto.LoginTime;
import com.haichutieu.chatsystem.server.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.concurrent.CompletableFuture;

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
            return session.createQuery("select C from Customer C where C.username = :username", Customer.class).setParameter("username", username).uniqueResult();
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

            LoginTime loginTime = session.createQuery("select L from LoginTime L where L.customerID = :id and L.isOnline = true", LoginTime.class).setParameter("id", id).uniqueResult();
            loginTime.setIsOnline(false);
            loginTime.setNumberPeopleChatWith(numberPeopleChatWith);
            loginTime.setNumberGroupChatWith(numberGroupChatWith);

            session.merge(loginTime);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
}
