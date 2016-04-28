package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import play.db.jpa.JPA;

@Entity
public class UserInfo {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;
	
    @ManyToOne
    public Location location;
	
	@Column(length=2000)
	public String aboutMe;
	
	public UserInfo() {
	}
	
	public void merge(UserInfo userInfo) {
	    // TODO - keith
	}
	
	public static boolean findByUserId(Long id) {
		Query q = JPA.em().createQuery("SELECT u FROM UserInfo u where user_id = ?1");
		q.setParameter(1, id);
		try {
			q.getSingleResult();
			return true;
		} catch(NoResultException e) {
			return false;
		}
	}

	public void save() {
		JPA.em().persist(this);
		JPA.em().flush();
	}
}