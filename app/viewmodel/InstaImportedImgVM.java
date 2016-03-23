package viewmodel;


import models.InstaImportedImg;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InstaImportedImgVM {
	@JsonProperty("user") public Long userId;
	@JsonProperty("imageId") public String imageId;
	
	public InstaImportedImgVM(InstaImportedImg message) {
		this.userId = message.user.id;
		this.imageId = message.imageid;
	}
}
