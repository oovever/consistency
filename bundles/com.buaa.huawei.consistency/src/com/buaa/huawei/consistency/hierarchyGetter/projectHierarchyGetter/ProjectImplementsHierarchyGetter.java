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

public class ProjectImplementsHierarchyGetter {
	private static HashMap<String, Pair<String[], String[]>> impl_hierarchy = null;
	private static HashMap<String, String[]> implements_hierarchy = null;
	private static HashMap<String, ArrayList<String>> implemented_by_hierarchy = null;

	/**
	 * 获取工程的实现和被实现关系
	 */
	public static HashMap<String, Pair<String[], String[]>> getImplHierarchy(IProject project) {
		// TODO Auto-generated method stub
		System.out.println("=======实现关系=======");
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
				// 记录extends hierarchy
				impl_hierarchy = new HashMap<>();
				// 记录实现的接口
				implements_hierarchy = new HashMap<>();
				// 记录被谁实现
				implemented_by_hierarchy = new HashMap<>();
				// 获取工程实现关系
				extractImplementsHierarchy(packages);
				// 通过遍历implements_hierarchy，将实现的接口转化成被谁实现
				convertImplementsHierarchyToImplementedByHierarchy();
				// 将implements_hierarchy和implemented_by_hierarchy整合为impl_hierarchy
				mergeImplementsAndImplementedBy();
			} else {
				System.out.println(project.getName() + "不是一个java工程");
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println();
		return impl_hierarchy;
	}

	/**
	 * 获取工程实现关系
	 */
	private static void extractImplementsHierarchy(HashMap<String, ICompilationUnit[]> packages) {
		Iterator<String> key_iterator = packages.keySet().iterator();
		while (key_iterator.hasNext()) {
			String package_name = key_iterator.next();
			// 获取当前包内的所有java_files
			ICompilationUnit[] java_files = packages.get(package_name);
			if (java_files.length != 0) {
				for (ICompilationUnit java_file_unit : java_files) {
					try {
						String java_unit_full_name = null;
						// 获取当前java_file实现的接口信息
						IType[] types = java_file_unit.getAllTypes();
						if (types.length != 0) {
							for (IType type : types) {
								java_unit_full_name = type.getFullyQualifiedName();
								if (type.getSuperInterfaceNames() == null) {
									// 没有实现接口
									impl_hierarchy.put(type.getFullyQualifiedName(), null);
								} else {
									// 实现接口。通过解析imports获取每一个接口的包名
									ArrayList<String> interfaces = new ArrayList<>();
									for (String inter : type.getSuperInterfaceNames()) {
										boolean is_same_package = true;
										IImportDeclaration[] imports = java_file_unit.getImports();
										int i = 0;
										while (i < imports.length) {
											String[] strs = imports[i].getElementName().split("\\.");
											if (strs.length > 0) {
												// 如果import里找到同名类，就说明接口引自不同包
												if (inter.equals(strs[strs.length - 1])) {
													is_same_package = false;
													break;
												} else {
													i++;
												}
											} else {
												i++;
											}
										}
										// 如果import里没有同名类，就说明接口引自同一包
										if (is_same_package) {
											StringBuilder sb = new StringBuilder(type.getFullyQualifiedName());
											sb.delete(sb.length() - 1 - type.getElementName().length(), sb.length());
											interfaces.add(sb.append("." + inter).toString());
										} else {
											// 不同包下的接口
											interfaces.add(imports[i].getElementName());
										}
									}
									implements_hierarchy.put(type.getFullyQualifiedName(), interfaces.toArray(new String[interfaces.size()]));
								}

							}
						}
						// 顺便把java_file放到implemented_by_hierarchy和impl_hierarchy中
						implemented_by_hierarchy.put(java_unit_full_name, new ArrayList<String>());
						impl_hierarchy.put(java_unit_full_name, new Pair<String[], String[]>());
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
	 * 获取工程的被实现关系
	 */
	private static void convertImplementsHierarchyToImplementedByHierarchy() {
		// 通过遍历implements_hierarchy，将实现的接口转化成被谁实现
		Iterator<String> java_file_names_in_implements_map = implements_hierarchy.keySet().iterator();
		while (java_file_names_in_implements_map.hasNext()) {
			// 获取当前java_file的所有caller
			String current_java_file_name = java_file_names_in_implements_map.next();
			String[] current_java_file_implements = implements_hierarchy.get(current_java_file_name);
			// 将current_java_file_implements中每一个类都放到对应类的implemented_by中
			for (String current_java_file_implement : current_java_file_implements) {
				// 如果该对应类是当前java工程里的类，则找到该对应类；否则，即该对应类非本工程文件，则不记录
				if (implemented_by_hierarchy.containsKey(current_java_file_implement)) {
					implemented_by_hierarchy.get(current_java_file_implement).add(current_java_file_name);
				}
			}
		}
	}

	/**
	 * 将implements_hierarchy和implemented_by_hierarchy整合为impl_hierarchy
	 */
	private static void mergeImplementsAndImplementedBy() {
		Iterator<String> java_file_names = impl_hierarchy.keySet().iterator();
		while (java_file_names.hasNext()) {
			String current_java_file_name = java_file_names.next();
			if (current_java_file_name == null) {
				// 清理impl_hierarchy中key为null的项
				java_file_names.remove();
			} else {
				impl_hierarchy.get(current_java_file_name).setLeft(implements_hierarchy.get(current_java_file_name));
				impl_hierarchy.get(current_java_file_name)
						.setRight(implemented_by_hierarchy.get(current_java_file_name).toArray(new String[implemented_by_hierarchy.get(current_java_file_name).size()]));
				// 顺便打印一下
				int caller_size = impl_hierarchy.get(current_java_file_name).getLeft().length;
				if (caller_size <= 0) {
					System.out.println(current_java_file_name + "没有实现接口");
				} else {
					for (int i = 0; i < caller_size; i++) {
						System.out.println(current_java_file_name + "实现了" + impl_hierarchy.get(current_java_file_name).getLeft()[i]);
					}
				}
				int callee_size = impl_hierarchy.get(current_java_file_name).getRight().length;
				if (callee_size <= 0) {
					System.out.println(current_java_file_name + "没有被实现");
				} else {
					for (int i = 0; i < callee_size; i++) {
						System.out.println(current_java_file_name + "被" + impl_hierarchy.get(current_java_file_name).getRight()[i] + "实现");
					}
				}
			}
		}
	}
}
