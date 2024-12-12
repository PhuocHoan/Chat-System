package com.haichutieu.chatsystem.server.dal;

import com.haichutieu.chatsystem.dto.LoginTime;
import com.haichutieu.chatsystem.dto.UserLoginTime;
import org.hibernate.Session;

import java.util.List;

public class HistoryService {
    public static List<LoginTime> fetchUserLoginHistory(int id) {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createQuery("select L from LoginTime L where L.customerID = :id and L.isOnline = true", LoginTime.class).setParameter("id", id).list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<UserLoginTime> fetchAllLoginHistory() {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createQuery("select new UserLoginTime(C.id, C.username, C.name, L.time) " +
                    "from Customer C join LoginTime L on C.id = L.customerID " +
                    "where L.isOnline = true " +
                    "order by L.time DESC", UserLoginTime.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
