package models;

import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.ManyToOne;

@Entity
public class InstaImportedImg extends SocialObject {

	@ManyToOne
	public User user;
	
	@Column(name="imageId")
	public String imageid;
}
