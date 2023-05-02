package jpql;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Team team = new Team();
            team.setName("teamA");
            em.persist(team);

            Member member = new Member();
            member.setUsername("member");
            member.setAge(10);

            member.setTeam(team);
            em.persist(member);


            em.flush();
            em.clear();

//            String query = "select m from Member m inner join m.team t";
//            String query = "select m from Member m, Team t where m.username = t.name";
            String query = "select m from Member m left join m.team t on t.name = 'teamA'";

            List<Member> result = em.createQuery(query, Member.class)
                .getResultList();

            System.out.println("result.size = " + result.size());
            for (Member member1 : result) {
                System.out.println("member1 = " + member1);
            }
            tx.commit();

        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
}
