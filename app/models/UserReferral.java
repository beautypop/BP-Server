package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import play.data.validation.Constraints.Required;
import play.db.jpa.JPA;

import java.util.List;

@Entity
public class UserReferral extends domain.Entity {
    private static final play.api.Logger logger = play.api.Logger.apply(UserReferral.class);

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	@Required
    @ManyToOne
    public User user;

	@Required
    @ManyToOne
    public User signedUpUser;

	@Column(nullable = false, columnDefinition = "TINYINT(1)")
    public boolean deleted = false; 
    
	public UserReferral() {}

    ///////////////////// Find APIs /////////////////////
    public static List<UserReferral> getSignedUpUsers(User user) {
        try {
            Query q = JPA.em().createQuery(
                    "SELECT r FROM UserReferral r where user = ?1 and deleted = false order by id");
            q.setParameter(1, user);
            return (List<UserReferral>) q.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
}