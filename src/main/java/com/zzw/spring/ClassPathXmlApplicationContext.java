package com.zzw.spring;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import com.zzw.annotation.ExtResource;
import com.zzw.annotation.ExtService;
import com.zzw.uitls.ClassUtil;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ClassPathXmlApplicationContext {
	// 扫包范围
	private String packageName;
	// 自定义spring IOC容器
	ConcurrentHashMap<String, Object> initBean = null;

	public ClassPathXmlApplicationContext(String packageName) {
		this.packageName = packageName;
	}

	// 使用beanID查找对象
	public Object getBean(String beanId) throws Exception {
		// 1.使用反射机制获取该包下所有的类已经存在bean的注解类
		List<Class> listExitsAnnotation = findClassExitsService();
		if (listExitsAnnotation == null || listExitsAnnotation.isEmpty()) {
			throw new Exception("没有需要初始化的bean");
		}
		// 2.使用Java反射机制初始化对象
		initBean = initBean(listExitsAnnotation);
		if (initBean == null || initBean.isEmpty()) {
			throw new Exception("初始化bean为空!");
		}
		// 3.使用beanId查找对应得bean对象
		Object object = initBean.get(beanId);
		// 4.使用反射读取类的属性,赋值信息
		attriAssign(object);

		return object;
	}

	// 使用反射读取类的属性,赋值信息
	public void attriAssign(Object object) throws IllegalArgumentException, IllegalAccessException {
		Class<? extends Object> classInfo = object.getClass();
		Field[] declaredFields = classInfo.getDeclaredFields();
		for (Field field : declaredFields) {
			// 1.获取类的属性是否存在 获取bean注解
			ExtResource extResource = field.getDeclaredAnnotation(ExtResource.class);
			if (extResource != null) {
				// 2.使用属性名称查找bean容器赋值
				Object bean = initBean.get(field.getName());
				if (bean != null) {
					// 私有访问允许访问
					field.setAccessible(true);
					// 给属性赋值
					field.set(object, bean);
				}
				continue;
			}


		}
	}

	// 初始化bean对象
	public ConcurrentHashMap<String, Object> initBean(List<Class> listExitsAnnotation)
			throws InstantiationException, IllegalAccessException {
		ConcurrentHashMap<String, Object> concurrent = new ConcurrentHashMap<String, Object>();
		for (Class classInfo : listExitsAnnotation) {
			// 初始化对象
			Object newInstance = classInfo.newInstance();
			// 获取父类名称
			String beanId = toLowerCaseFirstOne(classInfo.getSimpleName());
			concurrent.put(beanId, newInstance);
		}

		return concurrent;
	}

	// 使用反射机制获取该包下所有的类已经存在bean的注解类
	public List<Class> findClassExitsService() throws Exception {
		// 1.使用反射机制获取该包下所有的类
		if (StringUtils.isEmpty(packageName)) {
			throw new Exception("扫包地址不能为空！");
		}
		// 2.使用反射技术获取当前包下所有的类
		List<Class<?>> classesByPackageName = ClassUtil.getClasses(packageName);
		// 3.存放类上有bean注入注解
		List<Class> exitsClassesAnnotation = new ArrayList<Class>();
		// 4.判断该类上属否存在注解
		for (Class classInfo : classesByPackageName) {
			ExtService extService = (com.zzw.annotation.ExtService) classInfo.getDeclaredAnnotation(ExtService.class);
			if (extService != null) {
				exitsClassesAnnotation.add(classInfo);
				continue;
			}
		}
		return exitsClassesAnnotation;
	}

	// 首字母转小写
	public static String toLowerCaseFirstOne(String s) {
		if (Character.isLowerCase(s.charAt(0)))
			return s;
		else
			return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
	}

}
