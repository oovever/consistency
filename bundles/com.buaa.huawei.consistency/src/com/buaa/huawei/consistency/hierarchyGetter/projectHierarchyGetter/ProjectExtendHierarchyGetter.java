package com.buaa.huawei.consistency.hierarchyGetter.projectHierarchyGetter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.buaa.huawei.consistency.util.Pair;

public class ProjectExtendHierarchyGetter {
	private static HashMap<String, Pair<String, String[]>> extends_hierarchy = null;
	private static HashMap<String, String> parents_hierarchy = null;
	private static HashMap<String, ArrayList<String>> children_hierarchy = null;

	/**
	 * 获取工程的继承和被继承关系
	 */
	public static HashMap<String, Pair<String, String[]>> getExtendsHierarchy(IProject project) {
		// TODO Auto-generated method stub
		System.out.println("=======继承关系=======");
		try {
			if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
				IJavaProject java_project = JavaCore.create(project);
				// 获取所有package信息
				HashMap<String, ICompilationUnit[]> packages = ProjectPackageGetter.getAllPackages(java_project);
				// 清理packages，只保存main中的源码，去掉test等其他包中的源码
				Iterator<String> key_iterator = packages.keySet().iterator();
				while (key_iterator.hasNext()) {
					String package_name = key_iterator.next();
					// 获取当前包内的所有java_files
					ICompilationUnit[] java_files = packages.get(package_name);
					if (java_files.length != 0) {
						int i = 0;
						boolean found_test = false;
						do {
							ICompilationUnit java_file_unit = java_files[i];
							if (java_file_unit.getResource().getFullPath().toString().contains("src/test")) {
								found_test = true;
							}
							i++;
						} while (!found_test && i < java_files.length);
						if (found_test == true) {
							key_iterator.remove();
						}
					}
				}
				// 记录extends hierarchy。首先是继承自谁（父类）；然后是被谁继承（子类）
				extends_hierarchy = new HashMap<>();
				// 记录继承自谁（父类）
				parents_hierarchy = new HashMap<>();
				// 记录被谁继承（子类）
				children_hierarchy = new HashMap<>();
				// 获取工程的继承关系，存在parents_hierarchy中
				extractParentsHierarchy(packages);
				// 通过遍历parent_hierarchy，将parents转化成children
				convertParentsHierarchyToChildrenHierarchy();
				// 将parent_hierarchy和children_hierarchy整合为extends_hierarchy
				mergeParentsAndChildren();
			} else {
				System.out.println(project.getName() + "不是一个java工程");
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println();
		return extends_hierarchy;
	}

	/**
	 * 获取工程的继承关系
	 */
	private static void extractParentsHierarchy(HashMap<String, ICompilationUnit[]> packages) {
		Iterator<String> key_iterator = packages.keySet().iterator();
		while (key_iterator.hasNext()) {
			String package_name = key_iterator.next();
			// 获取当前包内的所有java_files
			ICompilationUnit[] java_files = packages.get(package_name);
			if (java_files.length != 0) {
				for (ICompilationUnit java_file_unit : java_files) {
					try {
						String java_unit_full_name = null;
						// 获取当前java_file的父类信息
						IType[] types = java_file_unit.getAllTypes();
						if (types.length != 0) {
							for (IType type : types) {
								java_unit_full_name = type.getFullyQualifiedName();
								if (type.getSuperclassName() == null) {
									// 没有父类
									parents_hierarchy.put(type.getFullyQualifiedName(), null);
								} else {
									// 有父类。通过解析imports获取父类的包名
									boolean is_same_package = true;
									IImportDeclaration[] imports = java_file_unit.getImports();
									int i = 0;
									while (i < imports.length) {
										String[] strs = imports[i].getElementName().split("\\.");
										if (strs.length > 0) {
											// 如果import里找到同名类，就说明父类引自不同包
											if (type.getSuperclassName().equals(strs[strs.length - 1])) {
												is_same_package = false;
												break;
											} else {
												i++;
											}
										} else {
											i++;
										}
									}
									// 如果import里没有同名类，就说明父类引自同一包
									if (is_same_package) {
										StringBuilder sb = new StringBuilder(type.getFullyQualifiedName());
										sb.delete(sb.length() - 1 - type.getElementName().length(), sb.length());
										parents_hierarchy.put(type.getFullyQualifiedName(), sb.append("." + type.getSuperclassName()).toString());
									} else {
										// 不同包下的父类
										parents_hierarchy.put(type.getFullyQualifiedName(), imports[i].getElementName());
									}
								}
							}
						}
						// 顺便把java_file存到children_hierarchy和extends_hierarchy中
						children_hierarchy.put(java_unit_full_name, new ArrayList<String>());
						extends_hierarchy.put(java_unit_full_name, new Pair<String, String[]>());
					} catch (JavaModelException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else {
				if (package_name != "") {
					System.out.println(package_name + "不包含java文件");
				}
			}
		}
	}

	/**
	 * 获取工程的被继承关系
	 */
	private static void convertParentsHierarchyToChildrenHierarchy() {
		// 通过遍历parent_hierarchy，将parents转化成children
		Iterator<String> java_file_names_in_parents_map = parents_hierarchy.keySet().iterator();
		while (java_file_names_in_parents_map.hasNext()) {
			// 获取当前java_file的parent
			String current_java_file_name = java_file_names_in_parents_map.next();
			String current_java_file_parent = parents_hierarchy.get(current_java_file_name);
			// 将current_java_file_parent放到对应类的children中
			if (current_java_file_parent != null) {
				// 如果该对应类是当前java工程里的类，则找到该对应类；否则，即该对应类非本工程文件，则不记录
				if (children_hierarchy.containsKey(current_java_file_parent)) {
					children_hierarchy.get(current_java_file_parent).add(current_java_file_name);
				}
			}
		}
	}

	/**
	 * 将parent_hierarchy和children_hierarchy整合为extends_hierarchy
	 */
	private static void mergeParentsAndChildren() {
		Iterator<String> java_file_names = extends_hierarchy.keySet().iterator();
		while (java_file_names.hasNext()) {
			String current_java_file_name = java_file_names.next();
			if (current_java_file_name == null) {
				// 清理extends_hierarchy中key为null的项
				java_file_names.remove();
			} else {
				extends_hierarchy.get(current_java_file_name).setLeft(parents_hierarchy.get(current_java_file_name));
				extends_hierarchy.get(current_java_file_name).setRight(children_hierarchy.get(current_java_file_name).toArray(new String[children_hierarchy.get(current_java_file_name).size()]));
				// 顺便打印一下
				if (extends_hierarchy.get(current_java_file_name).getLeft() != null) {
					System.out.println(current_java_file_name + "继承" + extends_hierarchy.get(current_java_file_name).getLeft());
				} else {
					System.out.println(current_java_file_name + "没有继承");
				}
				int children_size = extends_hierarchy.get(current_java_file_name).getRight().length;
				if (children_size <= 0) {
					System.out.println(current_java_file_name + "没有子类");
				} else {
					for (int i = 0; i < children_size; i++) {
						System.out.println(current_java_file_name + "被" + extends_hierarchy.get(current_java_file_name).getRight()[i] + "继承");
					}
				}
			}
		}
	}
}
