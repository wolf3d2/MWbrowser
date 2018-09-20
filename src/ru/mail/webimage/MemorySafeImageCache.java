package ru.mail.webimage;

import android.os.Environment;

/** Кеш, меняющий свое местоположение в зависимости от того, доступна ли карта памяти.
 *  Если карта памяти достуна - очищается кеш в памяти телефона
 */
public class MemorySafeImageCache
{
    ImageCache internal;
    ImageCache external;
    public MemorySafeImageCache(String internalCachePath,String externalCachePath)
    {
        internal = new ImageCache(internalCachePath);
        external = new ImageCache(externalCachePath);
    }
    public ImageCache selectCache()
    {
        ImageCache cache = null;
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
        {
            if(internal.getDir().exists())
                FileUtils.delete(internal.getDir());
            cache = external;
        }
        else
            cache = internal;
        if(!cache.getDir().exists())
            cache.getDir().mkdirs();
        return cache;
    }
}
