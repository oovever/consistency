package com.buaa.huawei.consistency.hierarchyGetter.projectHierarchyGetter;

import java.util.HashMap;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

public class ProjectPackageGetter {
	public static HashMap<String, ICompilationUnit[]> getAllPackages(IJavaProject java_project) {
		// 记录所有包中的所有java文件
		HashMap<String, ICompilationUnit[]> all_java_files = new HashMap<>();
		try {
			// 从project中提取packages
			IPackageFragment[] packages = java_project.getPackageFragments();
			for (IPackageFragment one_package : packages) {
				if (one_package.getKind() == IPackageFragmentRoot.K_SOURCE) {
					// 获取当前package中的所有java文件
					ICompilationUnit[] java_files = one_package.getCompilationUnits();
					all_java_files.put(one_package.getElementName(), java_files);
				} else {
					// System.out.println(one_package.getElementName() + "contains non-source
					// files");
				}
			}
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return all_java_files;
	}
}
