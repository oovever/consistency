package com.buaa.huawei.consistency.hierarchyGetter.projectHierarchyGetter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.search.JavaSearchScope;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;

import com.buaa.huawei.consistency.util.Pair;

public class ProjectCallHierarchyGetter {
	private static HashMap<String, Pair<String[], String[]>> call_hierarchy = null;
	private static HashMap<String, String[]> caller_hierarchy = null;
	private static HashMap<String, ArrayList<String>> callee_hierarchy = null;

	/**
	 * 获取工程的调用关系
	 */
	protected static HashMap<String, Pair<String[], String[]>> getCallHierarchy(IProject project) {
		System.out.println("=======调用关系=======");
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
				// 记录call hierarchy，包括caller和callee
				call_hierarchy = new HashMap<>();
				// 记录caller hierarchy，只包括caller
				caller_hierarchy = new HashMap<>();
				// 通过遍历caller_hierarchy，转化成callee_hierarchy，最后统一存到call_hierarchy中
				callee_hierarchy = new HashMap<>();
				// 设置调用关系搜寻范围
				JavaSearchScope scope = new JavaSearchScope();
				scope.add(java_project);
				CallHierarchy call_hierarchy_searcher = CallHierarchy.getDefault();
				call_hierarchy_searcher.setSearchScope(scope);
				// 获取工程的caller关系
				extractCallerHierarchy(packages, call_hierarchy_searcher);
				// 通过遍历caller_hierarchy，将callers转化成callees，最后统一存到call_hierarchy中
				convertCallerHierarchyToCalleeHierarchy();
				// 将caller_hierarchy和callee_hierarchy整合为call_hierarchy
				mergeCallersAndCallees();
			} else {
				System.out.println(project.getName() + "不是一个java工程");
			}
		} catch (JavaModelException e) {
			// TODO Ato-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println();
		return call_hierarchy;
	}

	/**
	 * 获取工程的caller关系
	 */
	private static void extractCallerHierarchy(HashMap<String, ICompilationUnit[]> packages, CallHierarchy call_hierarchy_searcher) {
		Iterator<String> key_iterator = packages.keySet().iterator();
		while (key_iterator.hasNext()) {
			String package_name = key_iterator.next();
			ICompilationUnit[] java_files = packages.get(package_name);
			if (java_files.length != 0) {
				for (ICompilationUnit java_file_unit : java_files) {
					try {
						String java_unit_full_name = null;
						// 记录当前java_file的所有caller
						HashMap<String, String> current_java_file_callers_map = new HashMap<>();
						// 获取该java文件中的field、method，分别放在各个IType中
						IType[] types = java_file_unit.getAllTypes();
						if (types.length != 0) {
							for (IType type : types) {
								java_unit_full_name = type.getFullyQualifiedName();
								// 用temp_field_list记录该java_file的所有field
								ArrayList<IMember> temp_field_list = new ArrayList<>();
								// 用temp_method_list记录该java_file的所有method
								ArrayList<IMember> temp_method_list = new ArrayList<>();
								IField[] fields = type.getFields();
								for (IField field : fields) {
									temp_field_list.add(field);
								}
								IMethod[] methods = type.getMethods();
								for (IMethod method : methods) {
									temp_method_list.add(method);
								}
								// 用HashMap临时记录当前java_file的type中每个field、method的caller，即被调用情况
								HashMap<String, String> temp_callers = new HashMap<>();
								// 找到并记录调用当前java_file中所有fields的所有地方
								IMember[] all_fields = new IMember[temp_field_list.size()];
								temp_field_list.toArray(all_fields);
								MethodWrapper[] all_fields_all_callers = call_hierarchy_searcher.getCallerRoots(all_fields);
								// 遍历当前java_file中每一个field，记录调用每一个field的所有地方
								// System.out.println("Field call relationships with java file " +
								// java_file_unit.getElementName());
								for (MethodWrapper current_field_all_callers : all_fields_all_callers) {
									// 调用当前field的所有地方
									// System.out.println("All locations call " +
									// current_field_all_callers.getName());
									MethodWrapper[] all_callers = current_field_all_callers.getCalls(new NullProgressMonitor());
									for (MethodWrapper caller : all_callers) {
										/*
										 * 记录当前caller对应的java_file_unit。当caller.getMember().getParent().getClass().getName().equals(
										 * "org.eclipse.jdt.internal.core.SourceType")时，意味着caller对应源码中的Class，Interface，Anonation，Enum等类型
										 */
										if (!caller.getMember().getParent().getClass().getName().equals("org.eclipse.jdt.internal.core.CompilationUnit")) {
											if (caller.getMember().getParent().getClass().getName().equals("org.eclipse.jdt.internal.core.SourceType")) {
												temp_callers.put(caller.getMember().getParent().getParent().getParent().getElementName() + "."
														+ caller.getMember().getParent().getParent().getElementName().replaceAll(".java", ""), type.getFullyQualifiedName());
											}
										} else {
											temp_callers.put(
													caller.getMember().getParent().getParent().getElementName() + "." + caller.getMember().getParent().getElementName().replaceAll(".java", ""),
													type.getFullyQualifiedName());
										}
									}
								}
								// 找到并记录调用当前java_file中所有methods的所有地方
								IMember[] all_methods = new IMember[temp_method_list.size()];
								temp_method_list.toArray(all_methods);
								MethodWrapper[] all_methods_all_callers = call_hierarchy_searcher.getCallerRoots(all_methods);
								// 遍历当前java_file中每一个method，记录调用每一个method的所有地方
								// System.out.println("Method call relationships with java file " +
								// java_file_unit.getElementName());
								for (MethodWrapper current_method_all_callers : all_methods_all_callers) {
									// 调用当前method的所有地方
									// System.out.println("All locations call " +
									// current_method_all_callers.getName());
									MethodWrapper[] all_callers = current_method_all_callers.getCalls(new NullProgressMonitor());
									for (MethodWrapper caller : all_callers) {
										/*
										 * 记录当前caller对应的java_file_unit。当caller.getMember().getParent().getClass().getName().equals(
										 * "org.eclipse.jdt.internal.core.SourceType")时，意味着caller对应源码中的Class，Interface，Anonation，Enum等类型
										 */
										if (!caller.getMember().getParent().getClass().getName().equals("org.eclipse.jdt.internal.core.CompilationUnit")) {
											if (caller.getMember().getParent().getClass().getName().equals("org.eclipse.jdt.internal.core.SourceType")) {
												temp_callers.put(caller.getMember().getParent().getParent().getParent().getElementName() + "."
														+ caller.getMember().getParent().getParent().getElementName().replaceAll(".java", ""), type.getFullyQualifiedName());
											}
										} else {
											temp_callers.put(
													caller.getMember().getParent().getParent().getElementName() + "." + caller.getMember().getParent().getElementName().replaceAll(".java", ""),
													type.getFullyQualifiedName());
										}
									}
								}
								// 将temp_callers中存的caller转存到current_java_file_callers_map里，期间会剔除自调用情况
								Iterator<String> caller_names = temp_callers.keySet().iterator();
								while (caller_names.hasNext()) {
									String caller_name = caller_names.next();
									// 不保存被自身调用的情况
									if (!caller_name.equals(type.getFullyQualifiedName())) {
										current_java_file_callers_map.put(caller_name, type.getFullyQualifiedName());
									}
								}
							}
						}
						// 将current_java_file_callers_map中存的caller转存到caller_hierarchy里
						caller_hierarchy.put(java_unit_full_name, current_java_file_callers_map.keySet().toArray(new String[current_java_file_callers_map.size()]));
						// 顺便把java_file存到callee_hierarchy和call_hierarchy里
						callee_hierarchy.put(java_unit_full_name, new ArrayList<String>());
						call_hierarchy.put(java_unit_full_name, new Pair<String[], String[]>());
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
	 * 获取工程的callee关系
	 */
	private static void convertCallerHierarchyToCalleeHierarchy() {
		// 通过遍历caller_hierarchy，将callers转化成callees，最后统一存到call_hierarchy中
		Iterator<String> java_file_names_in_caller_map = caller_hierarchy.keySet().iterator();
		while (java_file_names_in_caller_map.hasNext()) {
			// 获取当前java_file的所有caller
			String current_java_file_name = java_file_names_in_caller_map.next();
			String[] current_java_file_callers = caller_hierarchy.get(current_java_file_name);
			// 将current_java_file_callers中每一个类都放到对应类的callee中
			for (String current_java_file_caller : current_java_file_callers) {
				// 如果该对应类是当前java工程里的类，则找到该对应类；否则，即该对应类非本工程文件，则不记录
				if (callee_hierarchy.containsKey(current_java_file_caller)) {
					callee_hierarchy.get(current_java_file_caller).add(current_java_file_name);
				}
			}
		}
	}

	/**
	 * 将caller_hierarchy和callee_hierarchy整合为call_hierarchy
	 */
	private static void mergeCallersAndCallees() {
		Iterator<String> java_file_names = call_hierarchy.keySet().iterator();
		while (java_file_names.hasNext()) {
			String current_java_file_name = java_file_names.next();
			if (current_java_file_name == null) {
				// 清理call_hierarchy中key为null的项
				java_file_names.remove();
			} else {
				// 记录
				call_hierarchy.get(current_java_file_name).setLeft(caller_hierarchy.get(current_java_file_name));
				call_hierarchy.get(current_java_file_name).setRight(callee_hierarchy.get(current_java_file_name).toArray(new String[callee_hierarchy.get(current_java_file_name).size()]));
				// 顺便打印一下
				int caller_size = call_hierarchy.get(current_java_file_name).getLeft().length;
				if (caller_size <= 0) {
					System.out.println(current_java_file_name + "没有被调用");
				} else {
					for (int i = 0; i < caller_size; i++) {
						System.out.println(current_java_file_name + "被" + call_hierarchy.get(current_java_file_name).getLeft()[i] + "调用");
					}
				}
				int callee_size = call_hierarchy.get(current_java_file_name).getRight().length;
				if (callee_size <= 0) {
					System.out.println(current_java_file_name + "没有调用其他类");
				} else {
					for (int i = 0; i < callee_size; i++) {
						System.out.println(current_java_file_name + "调用了" + call_hierarchy.get(current_java_file_name).getRight()[i]);
					}
				}
			}
		}
	}
}
