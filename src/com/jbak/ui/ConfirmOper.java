package com.jbak.ui;

public abstract class ConfirmOper
{
	public int confirmTitle = 0;
	public abstract void onConfirm(Object userParam);
	public ConfirmOper setTitle(int title)
	{
		confirmTitle = title;
		return this;
	}
	public void onCancel(Object  userParam)
	{
		
	}
}