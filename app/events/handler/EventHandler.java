package events.handler;

import events.listener.CommentEventListener;
import events.listener.ConversationEventListener;
import events.listener.FollowEventListener;
import events.listener.LikeEventListener;
import events.listener.MessageEventListener;
import events.listener.PostEventListener;
import events.listener.SoldEventListener;
import events.listener.StoryEventListener;
import events.listener.ViewEventListener;

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
