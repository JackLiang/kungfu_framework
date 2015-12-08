/**
* Filename    : ClassScan.java
* Author      : Jack
* Create time : 2015-4-14 下午2:16:31
* Description :
*/
package com.cndw.rpg.base;

import java.io.File;
import java.io.FileFilter;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClassScan
{
  private Set<Class<?>> classes;
  protected final Log log = LogFactory.getLog(getClass());

  private Set<Class<?>> initClass(String pack) {
    Set classes = new LinkedHashSet();
    boolean recursive = true;
    String packageName = pack;
    String packageDirName = packageName.replace('.', '/');
    try
    {
      Enumeration dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
      while (dirs.hasMoreElements())
        try {
          URL url = (URL)dirs.nextElement();
          String protocol = url.getProtocol();
          if ("file".equals(protocol)) {
            String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
            classes.addAll(findClassesInFile(packageName, filePath, recursive));
          } else if ("jar".equals(protocol)) {
            classes.addAll(findClassesInJar(url, packageDirName, packageName, recursive));
          }
        } catch (Exception localException) {
        }
    }
    catch (Exception localException1) {
    }
    return classes;
  }

  public ClassScan(String pack) {
    this.classes = initClass(pack);
  }

  public Set<Class<?>> findClassesInJar(URL url, String packageDirName, String packageName, boolean recursive) {
    Set classes = new LinkedHashSet();
    try
    {
      JarFile jar = ((JarURLConnection)url.openConnection()).getJarFile();

      Enumeration entries = jar.entries();

      while (entries.hasMoreElements())
      {
        JarEntry entry = (JarEntry)entries.nextElement();
        String name = entry.getName();

        if (name.charAt(0) == '/')
        {
          name = name.substring(1);
        }

        if (name.startsWith(packageDirName)) {
          int idx = name.lastIndexOf('/');

          if (idx != -1) {
            packageName = name.substring(0, idx).replace('/', '.');
          }

          if ((idx != -1) || (recursive))
          {
            if ((name.endsWith(".class")) && (!entry.isDirectory()))
            {
              String className = name.substring(
                packageName.length() + 1, name.length() - 6);
              Class nameForClass = null;
              try {
                nameForClass = Class.forName(packageName + '.' + className);
              } catch (NoClassDefFoundError e) {
                this.log.warn("SCANNING CLASS|WARN|NoClassDefFoundError:" + packageName + '.' + className);
              }

              if (nameForClass != null) classes.add(nameForClass); 
            }
          }
        }
      }
    }
    catch (Exception e) { this.log.error("SCANNING CLASS|ERROR|" + e); }

    return classes;
  }

  public Set<Class<?>> findClassesInFile(String packageName, String packagePath, final boolean recursive) {
    Set classes = new LinkedHashSet();

    File dir = new File(packagePath);
    if ((!dir.exists()) || (!dir.isDirectory()))
      return classes;
    File[] dirfiles = dir.listFiles(new FileFilter() {
      public boolean accept(File file) {
        return ((recursive) && (file.isDirectory())) || (file.getName().endsWith(".class"));
      }
    });
    for (File file : dirfiles) {
      if (file.isDirectory()) {
        classes.addAll(findClassesInFile(packageName + "." + 
          file.getName(), file.getAbsolutePath(), recursive));
      } else {
        String className = file.getName().substring(0, file.getName().length() - 6);
        try {
          classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }
      }
    }
    return classes;
  }

  public Set<Class<?>> getClassesByAnnotation(Class<? extends Annotation> annClazz) {
    Set classes = new LinkedHashSet();
    for (Class clazz : this.classes) {
      if (clazz.getAnnotation(annClazz) != null)
        classes.add(clazz);
    }
    return classes;
  }
}