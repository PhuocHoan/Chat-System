package com.haichutieu.chatsystem.server.dal;

import com.haichutieu.chatsystem.server.dto.Customer;
import com.haichutieu.chatsystem.server.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class CustomerService {
    public static void addCustomer(Customer customer) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
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

    public static Customer getCustomerByName(String name) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            var res = session.createQuery("select C from Customer C where C.name = :name", Customer.class).setParameter("name", name).uniqueResult();
            System.out.println("name: " + res);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Customer getCustomerByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select C from Customer C where C.username = :username", Customer.class).setParameter("username", username).uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Customer getCustomerByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select C from Customer C where C.email = :email", Customer.class).setParameter("email", email).uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
