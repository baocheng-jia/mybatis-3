/*
 *    Copyright 2009-2021 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.io;

import java.io.InputStream;
import java.net.URL;

/**
 * A class to wrap access to multiple class loaders making them work as one
 *
 * 一个类来包装对多个类加载器的访问，使它们作为一个工作
 *
 * @author Clinton Begin
 */
public class ClassLoaderWrapper {
  /**
   * 默认类加载器
   */
  ClassLoader defaultClassLoader;
  /**
   * 系统类加载器
   */
  ClassLoader systemClassLoader;

  /**
   * 构造方法，只能被相同包内的class进行构建
   */
  ClassLoaderWrapper() {
    try {
      //获取当前类的系统类加载器
      systemClassLoader = ClassLoader.getSystemClassLoader();
    } catch (SecurityException ignored) {
      // AccessControlException on Google App Engine
    }
  }

  /**
   * 使用当前类路径获取资源作为 URL
   * @param resource - 需要定位的资源
   */
  public URL getResourceAsURL(String resource) {
    return getResourceAsURL(resource, getClassLoaders(null));
  }

  /**
   * 从类路径中获取资源，从特定的类加载器开始
   * @param resource  要查找的资源
   * @param classLoader 第一个尝试的类加载器
   */
  public URL getResourceAsURL(String resource, ClassLoader classLoader) {
    return getResourceAsURL(resource, getClassLoaders(classLoader));
  }

  /**
   * 从类路径里面获取资源
   *
   * @param resource 需要查找的资源
   */
  public InputStream getResourceAsStream(String resource) {
    return getResourceAsStream(resource, getClassLoaders(null));
  }

  /**
   * 从类路径中获取资源，从特定的类加载器开始
   * @param resource  要查找的资源
   * @param classLoader 第一个尝试的类加载器
   * @return the stream or null
   */
  public InputStream getResourceAsStream(String resource, ClassLoader classLoader) {
    return getResourceAsStream(resource, getClassLoaders(classLoader));
  }

  /**
   * 在类路径上找到一个类
   * @param name - 需要查找的类名称
   * @throws ClassNotFoundException 找不到的时候抛出的异常
   */
  public Class<?> classForName(String name) throws ClassNotFoundException {
    return classForName(name, getClassLoaders(null));
  }

  /**
   * 在类路径上找到一个类，从特定的类加载器开始（或尝试尝试）
   * @param name   需要查找的资源
   * @param classLoader - 第一个尝试的类加载器
   * @return - the class
   * @throws ClassNotFoundException 找不到的时候抛出的异常
   */
  public Class<?> classForName(String name, ClassLoader classLoader) throws ClassNotFoundException {
    return classForName(name, getClassLoaders(classLoader));
  }

  /**
   * 尝试从一组类加载器中获取资源
   *
   * @param resource    - the resource to get
   * @param classLoader - the classloaders to examine
   * @return the resource or null
   */
  InputStream getResourceAsStream(String resource, ClassLoader[] classLoader) {
    //从类加载器列表里进行尝试加载资源
    for (ClassLoader cl : classLoader) {
      //当前类加载器不为空，尝试加载
      if (null != cl) {

        //尝试查找通过的资源文件
        InputStream returnValue = cl.getResourceAsStream(resource);
        //查找的是空，加上路径/进行尝试
        if (null == returnValue) {
          returnValue = cl.getResourceAsStream("/" + resource);
        }
        //查找到了就返回对应的数据，不在使用后面的类加载器进行资源的查找
        if (null != returnValue) {
          return returnValue;
        }
      }
    }
    //没有找到
    return null;
  }

  /**
   * Get a resource as a URL using the current class path
   *
   * @param resource    - the resource to locate
   * @param classLoader - the class loaders to examine
   * @return the resource or null
   */
  URL getResourceAsURL(String resource, ClassLoader[] classLoader) {
    URL url;
    for (ClassLoader cl : classLoader) {
      if (null != cl) {
        //获取路径资源
        url = cl.getResource(resource);
        if (null == url) {
          url = cl.getResource("/" + resource);
        }
        if (null != url) {
          return url;
        }
      }
    }
    return null;
  }

  /**
   * 尝试加载类
   * @param name  需要加载的类名字
   * @param classLoader 类加载器
   */
  Class<?> classForName(String name, ClassLoader[] classLoader) throws ClassNotFoundException {
    for (ClassLoader cl : classLoader) {
      if (null != cl) {
        try {
          return Class.forName(name, true, cl);
        } catch (ClassNotFoundException e) {
        }
      }
    }
    throw new ClassNotFoundException("Cannot find class: " + name);
  }

  /**
   *
   * 获取当前类的加载器列表，一共返回五个类加载器，分别是：
   * 1、请求里面传递过来的类加载器
   * 2、默认加载器
   * 3、当前线程上下文类加载器
   * 4、当前类的类加载器
   * 5、系统类加载器
   *
   * 加载当前类的就是通过这个类加载器列表逐个进行尝试加载该类
   * @param classLoader
   * @return
   */
  ClassLoader[] getClassLoaders(ClassLoader classLoader) {
    return new ClassLoader[]{
        //指定的加载器
        classLoader,
        //默认加载器
        defaultClassLoader,
        //当前线程的上下文类加载器
        Thread.currentThread().getContextClassLoader(),
        //当前类的类加载器
        getClass().getClassLoader(),
        //系统类加载器
        systemClassLoader};
  }

}
