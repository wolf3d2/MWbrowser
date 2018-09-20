package ru.mail.webimage;

import ru.mail.webimage.ImageDownloader.LoadBitmap;

public interface OnImageLoad
{
    public void onImageLoad(LoadBitmap lb);
}