package ru.mail.webimage;

import java.io.File;
import java.io.FileFilter;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Comparator;

import android.util.Xml.Encoding;
/** Класс для работы с файловым кешем, загружаемым с web */
public class ImageCache
{
    public static long MAX_CACHE_SIZE = 1024*1024*7;
    static final String EXT = ".0";
    File directory;
    long cacheSize = -1;
    public static String UTF8_NAME = Encoding.UTF_8.name();
    FileFilter fileFilter = new FileFilter()
    {
        @Override
        public boolean accept(File pathname)
        {
            if(pathname.isDirectory())
                return false;
            String name = pathname.getName();
            if(name.endsWith(EXT))
                return true;
            return false;
        }
    };
/** Сортировщик файлов по дате последней модификации */    
    public static Comparator<File> lastModifiedComparator = new Comparator<File>()
    {
        @Override
        public int compare(File f1, File f2)
        {
            if(f1.lastModified()<f2.lastModified())
                return -1;
            else if(f1.lastModified()>f2.lastModified())
                return 1;
            return 0;
        }
    };
/** Функция вызывается при успешной загрузке файла path. Выполняется расчет кеша */
    public void putCacheFile(String path)
    {
        File f = new File(path);
        if(!f.exists())
            return;
        if(cacheSize<0)
            cacheSize = getCacheSize();
        else
            cacheSize+=f.length();
        if(cacheSize>MAX_CACHE_SIZE)
            cacheSize = cleanCache(MAX_CACHE_SIZE);
    }
/** Возвращает путь к файлу, куда нужно загрузить файл url */    
    public String getCachePath(String url) 
    {
        try{
            return directory.getAbsolutePath()+'/'+URLEncoder.encode(url, UTF8_NAME)+EXT;
        }
        catch (Throwable e) {
        }
        return null;
    }
    public ImageCache(String cacheDir)
    {
        directory = new File(cacheDir);
        directory.mkdirs();
    }
    public final File[] getCacheFiles()
    {
        FileFilter ff = getFileFilter();
        return ff==null?directory.listFiles():directory.listFiles(ff);
    }
/** Очищает кеш так, чтобы его размер был не более, чем maxSize-1Mb*/    
    public long cleanCache(long maxSize)
    {
        File ar[] = getCacheFiles();
        Arrays.sort(ar, lastModifiedComparator);
        long sz = FileUtils.getFilesSize(ar);
        if(sz>maxSize)
        {
            maxSize-=1024*1024;
            for(File f:ar)
            {
                sz-=f.length();
                f.delete();
                if(sz<=maxSize)
                    break;
            }
        }
        return sz;
    }
/** Возвращает директорию кеша */    
    public final File getDir()
    {
        return directory;
    }
    public final long getCacheSize()
    {
        return FileUtils.getFilesSize(getCacheFiles());
    }
    public FileFilter getFileFilter()
    {
        return null;
    }
}
