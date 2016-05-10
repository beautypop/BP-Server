package models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NoResultException;
import javax.persistence.OneToOne;
import javax.persistence.Query;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import play.data.validation.Constraints.Required;
import play.db.jpa.JPA;
import domain.Creatable;
import domain.Updatable;

@Entity
public class Review extends domain.Entity implements Serializable, Creatable, Updatable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	@Required
	@OneToOne
	public ConversationOrder conversationOrder;

	@Temporal(TemporalType.TIMESTAMP)
	public Date sellerReviewDate;

	public Double sellerScore = 0.0;
	   
	@Column(length=2000)
	public String sellerReview;

	@Temporal(TemporalType.TIMESTAMP)
	public Date buyerReviewDate;
	
	public Double buyerScore = 0.0;
	
	@Column(length=2000)
	public String buyerReview;
	
	public Review() {
	}
	
	public Review(ConversationOrder conversationOrder) {
		this.conversationOrder = conversationOrder;
	}

	@SuppressWarnings("unchecked")
	public static List<Review> getReviewsAsBuyer(Long id) {
		try {
            Query q = JPA.em().createQuery("SELECT r FROM Review r, ConversationOrder co where co.user1 = ?1 and co.id = r.conversationOrder.id ");
            q.setParameter(1, User.findById(id));
            return q.getResultList();
        } catch (NoResultException nre) {
            return null;
        }
	}
	
	@SuppressWarnings("unchecked")
	public static List<Review> getReviewsAsSeller(Long id) {
		try {
            Query q = JPA.em().createQuery("SELECT r FROM Review r, ConversationOrder co where co.user2 = ?1 and co.id = r.conversationOrder.id ");
            q.setParameter(1, User.findById(id));
            return q.getResultList();
        } catch (NoResultException nre) {
            return null;
        }
	}
	
    public static Review findById(Long id) {
        try { 
            Query q = JPA.em().createQuery("SELECT r FROM Review r where id = ?1");
            q.setParameter(1, id);
            return (Review) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

	public static Review getByConvesationId(Long conversationOrderId) {
		try { 
            Query q = JPA.em().createQuery("SELECT r FROM Review r where conversationOrder.id = ?1");
            q.setParameter(1, conversationOrderId);
            return (Review) q.getSingleResult();
        } catch (NoResultException e) {
        	return null;
        }
	}

}
