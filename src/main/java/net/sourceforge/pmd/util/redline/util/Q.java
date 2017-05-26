package net.sourceforge.pmd.util.redline.util;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Q
{
  public static String getClassName(Object obj)
  {
    String fullClassName = obj.getClass().getName();
    return fullClassName.substring(fullClassName.lastIndexOf(".") + 1, fullClassName.length());
  }
  
  public static String getLocalHostAddress()
  {
    String strIp = "";
    try
    {
      Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
      InetAddress ip = null;
      while (allNetInterfaces.hasMoreElements())
      {
        NetworkInterface netInterface = (NetworkInterface)allNetInterfaces.nextElement();
        
        Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
        while (addresses.hasMoreElements())
        {
          ip = (InetAddress)addresses.nextElement();
          if ((ip != null) && ((ip instanceof Inet4Address))) {
            strIp = ip.getHostAddress();
          }
        }
      }
    }
    catch (SocketException e)
    {
      e.printStackTrace();
    }
    return strIp;
  }
  
  public static void println(String content)
  {
    System.out.println(content);
  }
  
  public static void println()
  {
    System.out.println();
  }
  
  public static void printlnErr(String content)
  {
    System.err.println(content);
  }
  
  public static void printlnErr(String content, Exception e)
  {
    System.err.println(content);
    e.printStackTrace();
  }
  
  public static void print(String content)
  {
    System.out.print(content);
  }
  
  private static Random r = new Random();
  
  public static int random(int min, int max)
  {
    r.setSeed(System.currentTimeMillis());
    int ret = Math.abs(r.nextInt()) % (max - min);
    return ret + min;
  }
  
  public static int randomInt()
  {
    return r.nextInt();
  }
  
  public static long randomLong()
  {
    return r.nextLong();
  }
  
  public static boolean isContainAllTexts(String source, String[] texts)
  {
    for (int i = 0; i < texts.length; i++) {
      if (!source.contains(texts[i])) {
        return false;
      }
    }
    return true;
  }
  
  public static ArrayList<Integer> getAllDigitalsFromText(String text)
  {
    ArrayList<Integer> list = new ArrayList();
    
    char[] c = text.toCharArray();
    
    int tmp = 0;
    boolean start = false;
    for (int i = 0; i < c.length; i++) {
      if ((c[i] >= '0') && (c[i] <= '9'))
      {
        start = true;
        tmp = 10 * tmp + (c[i] - '0');
      }
      else if (start)
      {
        list.add(Integer.valueOf(tmp));
        tmp = 0;
        start = false;
      }
    }
    if (start)
    {
      list.add(Integer.valueOf(tmp));
      tmp = 0;
      start = false;
    }
    return list;
  }
  
  public static String getSimpleDateFormatString()
  {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
    sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
    return sdf.format(new Date());
  }
  
  public static String toTimeString(long time)
  {
    String timeStr = "";
    
    long millisecond = time % 1000L;
    timeStr = millisecond + "毫秒";
    time /= 1000L;
    if (time == 0L) {
      return timeStr;
    }
    long second = time % 60L;
    timeStr = second + "秒" + timeStr;
    time /= 60L;
    if (time == 0L) {
      return timeStr;
    }
    long minute = time % 60L;
    timeStr = minute + "分" + timeStr;
    time /= 60L;
    if (time == 0L) {
      return timeStr;
    }
    long hour = time % 24L;
    timeStr = hour + "小时" + timeStr;
    time /= 24L;
    if (time == 0L) {
      return timeStr;
    }
    long day = time % 365L;
    timeStr = day + "天" + timeStr;
    time /= 365L;
    if (time == 0L) {
      return timeStr;
    }
    long year = time;
    timeStr = year + "年" + timeStr;
    
    return timeStr;
  }
  
  public static String getSplitChar(String splitString)
  {
    String splitChar = ";";
    if (splitString.contains(",")) {
      splitChar = ",";
    } else if (splitString.contains("+")) {
      splitChar = "\\+";
    }
    return splitChar;
  }
  
  public static boolean isEmptyString(String str)
  {
    if ((str != null) && (!"".equals(str.trim()))) {
      return false;
    }
    return true;
  }
  
  public static boolean isEmptyArray(Object[] objs)
  {
    if ((objs != null) && (objs.length > 0)) {
      return false;
    }
    return true;
  }
  
  public static boolean isEmptyList(List<?> list)
  {
    if ((list != null) && (list.size() > 0)) {
      return false;
    }
    return true;
  }
  
  public static boolean isEmptyMap(Map<?, ?> map)
  {
    if ((map != null) && (!map.isEmpty())) {
      return false;
    }
    return true;
  }
  
  public static boolean isGreaterThanZero(Integer i)
  {
    if ((i != null) && (i.intValue() > 0)) {
      return true;
    }
    return false;
  }
  
  public static String getImageFormatName(File file)
    throws IOException
  {
    String formatName = null;
    
    ImageInputStream iis = ImageIO.createImageInputStream(file);
    Iterator<ImageReader> imageReader = ImageIO.getImageReaders(iis);
    if (imageReader.hasNext())
    {
      ImageReader reader = (ImageReader)imageReader.next();
      formatName = reader.getFormatName();
    }
    return formatName;
  }
}
