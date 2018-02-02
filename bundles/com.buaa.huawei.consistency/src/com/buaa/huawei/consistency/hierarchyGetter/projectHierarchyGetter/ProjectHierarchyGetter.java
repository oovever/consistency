package com.buaa.huawei.consistency.hierarchyGetter.projectHierarchyGetter;

import java.util.HashMap;

import org.eclipse.core.resources.IProject;

import com.buaa.huawei.consistency.util.Keys;
import com.buaa.huawei.consistency.util.Pair;

public class ProjectHierarchyGetter {
	private static Pair<String, HashMap<String, HashMap<String, ?>>> project_hierarchy = null;

	public static Pair<String, HashMap<String, HashMap<String, ?>>> getHierarchy(IProject project) {
		// TODO Auto-generated method stub
		System.out.println("/*\n * " + project.getName() + "的继承、实现、调用关系\n */");
		// 获取工程的继承关系。首先是继承自谁（父类）；然后是被谁继承（子类）
		HashMap<String, Pair<String, String[]>> extends_hierarchy = ProjectExtendHierarchyGetter.getExtendsHierarchy(project);
		// 获取工程的实现关系。首先是实现了谁；然后是被谁实现
		HashMap<String, Pair<String[], String[]>> impl_hierarchy = ProjectImplementsHierarchyGetter.getImplHierarchy(project);
		// 获取工程的调用关系。首先是caller（当前类的调用者，即当前类被谁调用）；然后是callee（当前类的被调用者，即当前类调用了谁）
		HashMap<String, Pair<String[], String[]>> call_hierarchy = ProjectCallHierarchyGetter.getCallHierarchy(project);
		if ((extends_hierarchy != null) && (impl_hierarchy != null) && (call_hierarchy != null)) {
			HashMap<String, HashMap<String, ?>> all_hierarchy = new HashMap<>();
			// 整合获取到的三种关系
			all_hierarchy.put(Keys.extends_key, extends_hierarchy);
			all_hierarchy.put(Keys.impl_key, impl_hierarchy);
			all_hierarchy.put(Keys.call_key, call_hierarchy);
			project_hierarchy = new Pair<>();
			project_hierarchy.setLeft(project.getName());
			project_hierarchy.setRight(all_hierarchy);
		} else {
			if (extends_hierarchy == null) {
				System.err.println("Extends hierarchy error");
			}
			if (impl_hierarchy == null) {
				System.err.println("Implements hierarchy error");
			}
			if (call_hierarchy == null) {
				System.err.println("Call hierarchy error");
			}
		}
		return project_hierarchy;
	}
}
