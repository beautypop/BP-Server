package models;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import play.db.jpa.JPA;
import domain.AuditListener;
import domain.DefaultValues;

@Entity
@EntityListeners(AuditListener.class)
public class InstagramImportedImage extends domain.Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;
    
	@ManyToOne
	public User user;
	
	public String mediaId;
	
	@Column(nullable = false, columnDefinition = "TINYINT(1)")
    public boolean deleted = false;
	
	public InstagramImportedImage() {}
	
	public static InstagramImportedImage findById(Long id) {
        try { 
            Query q = JPA.em().createQuery("SELECT i FROM InstagramImportedImage i where id = ?1 and deleted = false");
            q.setParameter(1, id);
            return (InstagramImportedImage) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
	
	public static List<InstagramImportedImage> getImportedImages(User user) {
        Query q = JPA.em().createQuery("SELECT i FROM InstagramImportedImage i where user = ?1 and deleted = false order by CREATED_DATE desc");
        q.setMaxResults(DefaultValues.MAX_ACTIVITIES_COUNT);    // safety measure as no infinite scroll
        q.setParameter(1, user);
        try {
            return (List<InstagramImportedImage>) q.getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }
	
	public static boolean isImported(User user, String mediaId) {
        Query q = JPA.em().createQuery("SELECT count(i) FROM InstagramImportedImage i where user = ?1 and mediaId = ?2 and deleted = false");
        q.setParameter(1, user);
        q.setParameter(2, mediaId);
        try {
            Long count = (Long)q.getSingleResult();
            return count > 0;
        } catch (NoResultException nre) {
            return false;
        }
    }
	
	public static List<String> findByUser(User user) {
        Query q = JPA.em().createQuery("SELECT i.mediaId FROM InstagramImportedImage i where user = ?1 and deleted = false");
        q.setParameter(1, user);
        try {
            return q.getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }
}
