package beautypop.events.handler;

import beautypop.events.listener.CommentEventListener;
import beautypop.events.listener.ConversationEventListener;
import beautypop.events.listener.FollowEventListener;
import beautypop.events.listener.LikeEventListener;
import beautypop.events.listener.MessageEventListener;
import beautypop.events.listener.PostEventListener;
import beautypop.events.listener.SoldEventListener;
import beautypop.events.listener.StoryEventListener;
import beautypop.events.listener.ViewEventListener;

import com.google.common.eventbus.EventBus;

public class EventHandler {

	Class[] listeners = {
	        LikeEventListener.class, 
			FollowEventListener.class, 
			ViewEventListener.class,
			CommentEventListener.class,
			PostEventListener.class,
			StoryEventListener.class,
			SoldEventListener.class,
			ConversationEventListener.class,
			MessageEventListener.class
			};
	
	private static EventHandler eventHandler = new EventHandler();
	private EventBus eventBus = new EventBus();
	
	private EventHandler() {
		registerSubscribers();
	}
	
	public static EventHandler getInstance () {
		return eventHandler;
	}

	public EventBus getEventBus(){
		return eventBus;
	}

	void registerSubscribers() {
		for (Class listener : listeners) {
			try {
				eventBus.register(listener.newInstance());
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void unRegisterSubscribers() {
		for (Class listener : listeners) {
			try {
				eventBus.unregister(listener.newInstance());
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	
}
