package viewmodel;

import models.InstagramImportedImage;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InstagramImportedImageVM {
	@JsonProperty("user") public Long userId;
	@JsonProperty("imageId") public String imageId;
	
	public InstagramImportedImageVM(InstagramImportedImage image) {
		this.userId = image.user.id;
		this.imageId = image.imageId;
	}
}
