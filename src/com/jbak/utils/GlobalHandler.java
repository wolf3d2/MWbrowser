package com.jbak.utils;
import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Message;
/** Обертка для статического хендлера 
 *  Для отправки сообщений используются ссылки на объекты. Если объект был удален - его не держит в памяти
 *  Позволяет юзать общий статический хендлер, не заморачиваясь каждый раз над его созданием.
 *  Не используйте анонимные имплементации интерфейса! 
 */
public interface GlobalHandler {

	public abstract void onHandlerEvent(int what);
	static Handler gHandler = new Handler()
	{
		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg) 
		{
			if(msg.obj instanceof WeakReference)
			{
				WeakReference<GlobalHandler> wr = (WeakReference<GlobalHandler>)msg.obj;
				if(wr==null||wr.get()==null)
					return;
				wr.get().onHandlerEvent(msg.what);
			}
			else
			{
				((GlobalHandler)msg.obj).onHandlerEvent(msg.what);
			}
		};
	};
	/** Объект для отправки и удаления сообщений */
	public static CustomHandlerCommands command = new CustomHandlerCommands();
	static class CustomHandlerCommands
	{
		private WeakRefArray<GlobalHandler> mRefs = new WeakRefArray<GlobalHandler>();
		private WeakReference<GlobalHandler> getWeakRef(GlobalHandler ch)
		{
			WeakReference<GlobalHandler> ref = mRefs.getByObj(ch);
			if(ref!=null&&ref.get()!=null)
				return ref;
			return new WeakReference<GlobalHandler>(ch);
		}
		/** Отправляет хендлеру сообщение what. Перед отправкой зачищает все сообщения what для указанного CustomHandler'a
		 * @param what Код сообщения
		 * @param ch Обработчик, который получит сообщения. Нельзя использовать анонимные типы*/
		public void send(int what,GlobalHandler ch)
		{
			sendDelayed(what, ch, -1);
		}
		/** Отправляет хендлеру сообщение what с задержкой delay. Перед отправкой зачищает все сообщения what для указанного CustomHandler'a
		 * @param what Код сообщения
		 * @param ch Обработчик, который получит сообщения. Нельзя использовать анонимные типы
		 * @param delay Задержка отправки*/
		public void sendDelayed(int what,GlobalHandler ch,long delay)
		{
			WeakReference<GlobalHandler> obj = getWeakRef(ch);
			gHandler.removeMessages(what, obj);
			Message msg = gHandler.obtainMessage(what, obj);
			mRefs.addWeakRef(obj);
			if(delay<0)
				gHandler.sendMessage(msg);
			else
				gHandler.sendMessageDelayed(msg,delay);
		}
		/** Удаляет все сообщения what для обработчика ch */
		public void removeMessages(int what,GlobalHandler ch)
		{
			gHandler.removeMessages(what, getWeakRef(ch));
		}
		/** Удаляет все сообщения what для обработчика ch */
		public void removeMessagesByObj(int what,GlobalHandler ch)
		{
			gHandler.removeMessages(what, ch);
		}
		public void removeAllMesages(GlobalHandler ch)
		{
			gHandler.removeCallbacksAndMessages(ch);
			gHandler.removeCallbacksAndMessages(getWeakRef(ch));
		}
		public void sendDelayedByObj(int what,GlobalHandler ch,int delay)
		{
			removeMessagesByObj(what, ch);
			Message msg = gHandler.obtainMessage(what, ch);
			gHandler.sendMessageDelayed(msg,delay);
		}
	}
}
