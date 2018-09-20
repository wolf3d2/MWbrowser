package com.jbak.superbrowser;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import ru.mail.mailnews.st;
import ru.mail.mailnews.st.UniObserver;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import com.android.vending.billing.IInAppBillingService;
import com.jbak.utils.Utils;

public class Payment {
	/** Ошибка биллинга и нет данных про покупку */
	public static final int STAT_ERROR = 0;
	/** У юзера версия Pro  */
	public static final int STAT_PRO = 1;
	/** Ошибка проверки, но есть данные, что юзер купил версию Pro */
	public static final int STAT_PRO_OFFLINE = 2;
	/** Юзер может купить версию Pro */
	public static final int STAT_CAN_BUY_PRO = 3;
	
	private static final String SKU_PRO_VERSION = "jbak_browser_pro_version";
	private static final String RESPONSE_CODE = "RESPONSE_CODE";
	private static final String BUY_INTENT = "BUY_INTENT";
	public static final String INAPP = "inapp";
	private static int proState = STAT_ERROR;
	public static Payment INSTANCE;
	ServiceConnection mServiceConn;
	IInAppBillingService mService;
	public static boolean isBillingSupported = false;
	public static void init() {
		INSTANCE = new Payment();
		setProState(Prefs.getString(Prefs.HISTORY_TEST, null)!=null?STAT_PRO_OFFLINE:STAT_ERROR);
	}
	public static void check()
	{
		try{
			if(INSTANCE.mService==null)
				INSTANCE.createConnection();
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}
	
	private void createConnection() {
		try {
			mServiceConn = new ServiceConnection() {
				@Override
				public void onServiceDisconnected(ComponentName name) {
					mService = null;
				}

				@Override
				public void onServiceConnected(ComponentName name,IBinder service) {
					mService = IInAppBillingService.Stub.asInterface(service);
					checkProVersionAsync();
				}
			};
			Intent serviceIntent = new Intent(
					"com.android.vending.billing.InAppBillingService.BIND");
			serviceIntent.setPackage("com.android.vending");
			BrowserApp.INSTANCE.bindService(serviceIntent, mServiceConn,
					Context.BIND_AUTO_CREATE);
		} catch (Throwable e) {
			e.printStackTrace();
				proState=STAT_ERROR;
		}
	}
	public static void checkProVersionAsync()
	{
		if(INSTANCE.mService==null)
		{
			if(!isPro())
				setProState(STAT_ERROR);
			return;
		}
		new st.SyncAsycOper() {
			
			@Override
			public void makeOper(UniObserver obs) throws Throwable {
				int result = INSTANCE.mService.isBillingSupported(3, getPackageName(), INAPP);
				isBillingSupported = result==0;
				if(isBillingSupported)
				{
					checkIsProVersionNow();
					if(isPro())
						return;
					checkProVersionExistSync();
				}
				else
					setProState(STAT_ERROR);
			}
			protected void onPostExecute(Void result) 
			{
				BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_BUY_PRO_CHANGED, null);
			};
		}.startAsync();
	}
	static void disconnect()
	{
		try{
		BrowserApp.INSTANCE.unbindService(INSTANCE.mServiceConn);
		INSTANCE.mService=null;
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}
	static void checkIsProVersionNow() throws Throwable
	{
		Bundle ownedItems = INSTANCE.mService.getPurchases(3, getPackageName(), INAPP, null);
		if(ownedItems!=null)
		{
			int response = ownedItems.getInt(RESPONSE_CODE);
			if(response==0)
			{
				ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
				ArrayList<String>  purchaseDataList =ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
				if(ownedSkus!=null)
				{
					int index = ownedSkus.indexOf(SKU_PRO_VERSION);
					if(index>-1)
					{
						setProState(STAT_PRO);
						if(index<purchaseDataList.size())
							getOrderIdFromPurchaseData(purchaseDataList.get(index));
						return;
					}
				}
				Prefs.setString(Prefs.HISTORY_TEST, null);
				setProState(STAT_ERROR);
			}
		}
		
	}
	public static final String getPackageName()
	{
		return BrowserApp.INSTANCE.getPackageName();
	}
	public static String getOrderIdFromPurchaseData(String purchaseData)
	{
		try{
			Utils.log(SKU_PRO_VERSION, "purchaseData="+purchaseData.toString());
			JSONObject jo = new JSONObject(purchaseData);
			String token = jo.optString("orderId", null);
			if(token==null)
				token = "bad order id ";
			Prefs.setString(Prefs.HISTORY_TEST, token);
			setProState(STAT_PRO);
			return token;
		}
		catch(Throwable e)
		{
			Utils.log(e);
		}
		return null;
	}
	public static void processBuyProActivityResult(MainActivity mainActivity, int requestCode, int resultCode, Intent data)
	{
		try{
			int responseCode = data.getIntExtra(RESPONSE_CODE, 0);
			Utils.log(SKU_PRO_VERSION, "result = "+resultCode+", "+RESPONSE_CODE+'='+responseCode);
			if(resultCode==Activity.RESULT_OK&&responseCode==0)
			{
				String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
				getOrderIdFromPurchaseData(purchaseData);
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}
	public static boolean buyPro(MainActivity act)
	{
		if(!canBuyPro()||INSTANCE.mService==null||isPro())
			return false;
		try{
			Bundle buyIntentBundle = INSTANCE.mService.getBuyIntent(3, getPackageName(),SKU_PRO_VERSION, INAPP, SKU_PRO_VERSION);
			PendingIntent pendingIntent = buyIntentBundle.getParcelable(BUY_INTENT);
			act.startIntentSenderForResult(pendingIntent.getIntentSender(), IConst.CODE_BUY_PRO, new Intent(), 0, 0, 0);
			return true;
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		return false;
	}
	private static void checkProVersionExistSync() throws Throwable
	{
		ArrayList<String> skuList = new ArrayList<String> ();
		skuList.add(SKU_PRO_VERSION);
		Bundle querySkus = new Bundle();
		querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
		Bundle skuDetails = INSTANCE.mService.getSkuDetails(3, getPackageName(), INAPP, querySkus);
		if(skuDetails==null)
		{
			if(!isPro())
				setProState(STAT_ERROR);
			return;
		}
		int result = skuDetails.getInt(RESPONSE_CODE);
		if(!isPro())
			setProState(result==0?STAT_CAN_BUY_PRO:STAT_ERROR);
	}
	public static final boolean canBuyPro() {
		return getProState()==STAT_CAN_BUY_PRO;
	}
	public static final int getProState() {
		return proState;
	}
	public static final void setProState(int proState) {
//		Payment.proState = proState;
// делаем браузер версией pro
		Payment.proState = STAT_PRO;
	}
	public static final boolean isPro()
	{
		return getProState()==STAT_PRO||getProState()==STAT_PRO_OFFLINE;
	}
	static Location getLocation()
	{
		try{
			LocationManager lm = (LocationManager) BrowserApp.INSTANCE.getSystemService(Service.LOCATION_SERVICE);
			List<String> providers = lm.getProviders(true);
			for(String p:providers)
			{
				try{
					Location loc = lm.getLastKnownLocation(p);
					if(loc!=null)
						return loc;
				}
				catch(Throwable ignor)
				{}
			}
		}
		catch(Throwable e)
		{
		}
		return null;
	}
}
