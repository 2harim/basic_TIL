package hellojpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            //비영속
            Member member = new Member();
            member.setId(101L);
            member.setName("HelloJPA");

            //영속
            em.persist(member);
            Member member1 = em.find(Member.class, 101L);

            System.out.println("member1.getId() = " + member1.getId());
            System.out.println("member1.getName() = " + member1.getName());
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }
}
