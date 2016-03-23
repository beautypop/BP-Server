package insta.importer.dto;

public class MediaDto{
	String imageUrl;
	String caption;
	String imageId;
	boolean isImported;
	
	public boolean getIsImported() {
		return isImported;
	}
	public void setIsImported(boolean b) {
		this.isImported = b;
	}
	public String getImageId() {
		return imageId;
	}
	public void setImageId(String imageId) {
		this.imageId = imageId;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
	
}