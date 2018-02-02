package com.buaa.huawei.consistency.util;

public class Keys {
	/* 三种关系对应的代码体系结构 */
	public final static String extends_key = "extends";
	public final static String impl_key = "impl";
	public final static String call_key = "call";
	/*
	 * 继承关系中出现的问题
	 */
	// This class is deleted in unchecked project
	public final static String deleted_in_unchecked_project = "deleted in unchecked project";
	// This class has no parent in base project, but has parent in unchecked project
	public final static String no_parent_in_base_project = "no parent in base project";
	// This class has parent in base project, but has no parent in unchecked project
	public final static String no_parent_in_unchecked_project = "no parent in unchecked project";
	// This class has different parents between base project and unchecked project
	public final static String different_parents = "different parents";
	// This class has the specific child in base project, but doesn't have the specific child in unchecked project
	public final static String no_child_in_unchecked_project = "no child in unchecked project";
	// This class doesn't have the specific child in base project, but has the specific child in unchecked project
	public final static String no_child_in_base_project = "no child in base project";
	// This class is newed in unchecked project
	// 这个key会在三种关系中都用到
	public final static String new_in_unchecked_project = "new in unchecked project";
	/*
	 * 实现关系中出现的问题
	 */
	// This class implements the specific class in base project, but doesn't implement the specific class in unchecked project
	public final static String no_impls_in_unchecked_project = "no impls in unchecked project";
	// This class doesn't implement the specific class in base project, but implements the specific class in unchecked project
	public final static String no_impls_in_base_project = "no impls in base project";
	// This class is implemented by the specific class in base project, but isn't implemented by the specific class in unchecked project
	public final static String no_impleds_in_unchecked_project = "no impleds in unchecked project";
	// This class isn't implemented by the specific class in base project, but is implemented by the specific class in unchecked project
	public final static String no_impleds_in_base_project = "no impleds in base project";
	/*
	 * 调用关系中出现的问题
	 */
	// This class is called by the specific class in base project, but isn't called by the specific class in unchecked project
	public final static String no_callers_in_unchecked_project = "no callers in unchecked project";
	// This class isn't called by the specific class in base project, but is called by the specific class in unchecked project
	public final static String no_callers_in_base_project = "no callers in base project";
	// This class calls the specific class in base project, but doesn't call the specific class in unchecked project
	public final static String no_callees_in_unchecked_project = "no callees in unchecked project";
	// This class doesn't call the specific class in base project, but calls the specific class in unchecked project
	public final static String no_callees_in_base_project = "no callees in base project";
}
