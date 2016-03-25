package insta.importer.dto;

public class MediaDto{
	String imageUrl;
	String caption;
	String mediaId;
	boolean isImported;
	
	public boolean getIsImported() {
		return isImported;
	}
	public void setIsImported(boolean b) {
		this.isImported = b;
	}
	public String getMediaId() {
		return mediaId;
	}
	public void setMediaId(String mediaId) {
		this.mediaId = mediaId;
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