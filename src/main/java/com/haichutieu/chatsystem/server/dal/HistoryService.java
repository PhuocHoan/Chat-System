package com.haichutieu.chatsystem.server.dal;

import com.haichutieu.chatsystem.dto.LoginTime;
import com.haichutieu.chatsystem.dto.UserLoginTime;
import org.hibernate.Session;

import java.util.List;

public class HistoryService {
    public static List<LoginTime> fetchUserLoginHistory(int id) {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createQuery("""
                            select L from LoginTime L 
                            where L.customerID = :id 
                            order by L.time DESC
                            """, LoginTime.class).setParameter("id", id)
                    .setMaxResults(40).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<UserLoginTime> fetchAllLoginHistory(int rows) {
        try (Session session = HibernateUtil.getInstance().getSessionFactory().openSession()) {
            return session.createQuery("select new UserLoginTime(C.id, C.username, C.name, L.time) " +
                    "from Customer C join LoginTime L on C.id = L.customerID " +
                    "order by L.time DESC", UserLoginTime.class).setMaxResults(rows).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
