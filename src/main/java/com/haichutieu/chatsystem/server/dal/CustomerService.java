package com.haichutieu.chatsystem.server.dal;

import com.haichutieu.chatsystem.dto.Customer;
import com.haichutieu.chatsystem.dto.LoginTime;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;


public class CustomerService {
    public static boolean addCustomer(Customer customer) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(customer);
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

    public static boolean deleteCustomer(int id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Customer customer = session.get(Customer.class, id);
            if (customer != null) {
                session.delete(customer);
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

    public static boolean editCustomer(Customer customer) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(customer);
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

    public static boolean updateCustomer(Customer customer) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Customer existingCustomer = session.get(Customer.class, customer.getId());
            if (customer.getUsername() != null) {
                existingCustomer.setUsername(customer.getUsername());
            }
            if (customer.getPassword() != null) {
                existingCustomer.setPassword(customer.getPassword());
            }
            if (customer.getName() != null) {
                existingCustomer.setName(customer.getName());
            }
            if (customer.getAddress() != null) {
                existingCustomer.setAddress(customer.getAddress());
            }
            if (customer.getBirthdate() != null) {
                existingCustomer.setBirthdate(customer.getBirthdate());
            }
            if (customer.getSex() != null) {
                existingCustomer.setSex(customer.getSex());
            }
            if (customer.getEmail() != null) {
                existingCustomer.setEmail(customer.getEmail());
            }
            session.update(existingCustomer);
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

    public static Customer getCustomerByID(int userID) {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createQuery("select C from Customer C where C.id = :id", Customer.class).setParameter("id", userID).uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

    public static List<Customer> fetchAllCustomers() {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createQuery("select C from Customer C", Customer.class).list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean toggleLockStatusAccount(int id) {
        Transaction transaction = null;
        int result = 0;
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Query q = session.createQuery("update Customer " +
                    "set isLock = case when isLock = true then false else true end " +
                    "where id = :id"
            ).setParameter("id", id);
            result = q.executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
                return false;
            }
        }
        return result > 0;
    }

    public static boolean changePassword(int userId, String newHashedPassword) {
        Transaction transaction = null;
        int result = 0;
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Query q = session.createQuery("update Customer " +
                    "set password = :newHashedPassword " +
                    "where id = :id"
            ).setParameter("id", userId).setParameter("newHashedPassword", newHashedPassword);
            result = q.executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
                return false;
            }
        }
        return result > 0;
    }
}
