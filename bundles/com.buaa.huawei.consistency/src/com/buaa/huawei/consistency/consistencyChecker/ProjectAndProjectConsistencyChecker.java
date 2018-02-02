package com.buaa.huawei.consistency.consistencyChecker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Shell;

import com.buaa.huawei.consistency.dialog.Mode2SelectionDialog;
import com.buaa.huawei.consistency.hierarchyGetter.projectHierarchyGetter.ProjectHierarchyGetter;
import com.buaa.huawei.consistency.util.Keys;
import com.buaa.huawei.consistency.util.Pair;

public class ProjectAndProjectConsistencyChecker {
	private Pair<String, HashMap<String, HashMap<String, ?>>> base_hierarchy = null;
	private Pair<String, HashMap<String, HashMap<String, ?>>> unchecked_hierarchy = null;
	private Pair<Pair<String, String>, HashMap<String, HashMap<String, HashMap<String, ?>>>> check_result = null;

	public ProjectAndProjectConsistencyChecker(Pair<String, HashMap<String, HashMap<String, ?>>> base_hierarchy, Pair<String, HashMap<String, HashMap<String, ?>>> unchecked_hierarchy) {
		// TODO Auto-generated constructor stub
		this.base_hierarchy = base_hierarchy;
		this.unchecked_hierarchy = unchecked_hierarchy;
	}

	/**
	 * 进行一致性检查
	 * 
	 * @return
	 */
	public Pair<Pair<String, String>, HashMap<String, HashMap<String, HashMap<String, ?>>>> checkConsistency() {
		// TODO Auto-generated method stub
		System.out.println("/*\n * 项目与项目一致性检查 " + "\n */");
		check_result = new Pair<>();
		String base_project_name = base_hierarchy.getLeft();
		String unchecked_project_name = unchecked_hierarchy.getLeft();
		check_result.setLeft(Pair.createPair(base_project_name, unchecked_project_name));
		// 一致性检查基本分为以下三部分
		HashMap<String, HashMap<String, HashMap<String, ?>>> all_results = new HashMap<>();
		all_results.put(Keys.extends_key, checkExtendsConsistency(base_project_name, unchecked_project_name));
		all_results.put(Keys.impl_key, checkImplConsistency(base_project_name, unchecked_project_name));
		all_results.put(Keys.call_key, checkCallConsistency(base_project_name, unchecked_project_name));
		check_result.setRight(all_results);
		return check_result;
	}

	private HashMap<String, HashMap<String, ?>> checkExtendsConsistency(String base_project_name, String unchecked_project_name) {
		// TODO Auto-generated method stub
		System.out.println("====继承关系一致性====");
		// 保存结果的HashMap。其中每个key都代表一类问题；value是一个HashMap，记录所有存在该问题的<类,涉及的关系>键值对
		HashMap<String, HashMap<String, ?>> extends_check_result = new HashMap<>();
		// 各类问题对应的HashMap
		HashMap<String, Pair<String, String[]>> deleted_in_unchecked_project = new HashMap<>();
		HashMap<String, Pair<String, String>> no_parent_in_base_project = new HashMap<>();
		HashMap<String, Pair<String, String>> no_parent_in_unchecked_project = new HashMap<>();
		HashMap<String, Pair<String, String>> different_parents = new HashMap<>();
		HashMap<String, Pair<String[], String[]>> no_child_in_unchecked_project = new HashMap<>();
		HashMap<String, Pair<String[], String[]>> no_child_in_base_project = new HashMap<>();
		HashMap<String, Pair<String, String[]>> new_in_unchecked_project = new HashMap<>();
		// 遍历基准版本中的每一个类，检查待查版本中是否还有此类、父类是否一致、子类是否一致
		Iterator<String> base_java_file_names = this.base_hierarchy.getRight().get(Keys.extends_key).keySet().iterator();
		while (base_java_file_names.hasNext()) {
			String java_file_name = base_java_file_names.next();
			if (!this.unchecked_hierarchy.getRight().get(Keys.extends_key).containsKey(java_file_name)) {
				System.out.println(java_file_name + "在" + unchecked_project_name + "中被删除");
				// 记录删除的类。删除的类只需在这里记录一次其在基准版本中的父类、子类即可。后面impl检查、call检查中不用再记录
				deleted_in_unchecked_project.put(java_file_name, Pair.createPair((String) ((Pair) this.base_hierarchy.getRight().get(Keys.extends_key).get(java_file_name)).getLeft(),
						(String[]) ((Pair) this.base_hierarchy.getRight().get(Keys.extends_key).get(java_file_name)).getRight()));
			} else {
				// 检查当前类在基准版本中的父类是否和待查版本中的父类一致
				String base_parent = (String) ((Pair) this.base_hierarchy.getRight().get(Keys.extends_key).get(java_file_name)).getLeft();
				String unchecked_parent = (String) ((Pair) this.unchecked_hierarchy.getRight().get(Keys.extends_key).get(java_file_name)).getLeft();
				if (base_parent == null && unchecked_parent != null) {
					System.out.println(java_file_name + "在" + base_project_name + "中没有继承," + unchecked_project_name + "中继承" + unchecked_parent);
					// 记录基准版本中没有父类，而待查版本中有父类的情况
					no_parent_in_base_project.put(java_file_name, Pair.createPair(null, unchecked_parent));
				} else if (base_parent != null && unchecked_parent == null) {
					System.out.println(java_file_name + "在" + base_project_name + "中继承" + base_parent + "," + unchecked_project_name + "中没有继承");
					// 记录基准版本中有父类，而待查版本中没有父类的情况
					no_parent_in_unchecked_project.put(java_file_name, Pair.createPair(base_parent, null));
				} else if (base_parent != null && unchecked_parent != null) {
					if (!base_parent.equals(unchecked_parent)) {
						System.out.println(java_file_name + "在" + base_project_name + "中继承" + base_parent + "," + unchecked_project_name + "中继承" + unchecked_parent);
						// 记录基准版本和待查版本父类不一样的情况
						different_parents.put(java_file_name, Pair.createPair(base_parent, unchecked_parent));
					}
				}
				// 检查当前类在基准版本中的子类是否和待查版本中的子类一致
				String[] base_children = (String[]) ((Pair) this.base_hierarchy.getRight().get(Keys.extends_key).get(java_file_name)).getRight();
				String[] unchecked_children = (String[]) ((Pair) this.unchecked_hierarchy.getRight().get(Keys.extends_key).get(java_file_name)).getRight();
				// 将String[]转存为HashMap，方便查询
				HashMap<String, String> base_children_map = new HashMap<>();
				for (String base_child : base_children) {
					base_children_map.put(base_child, null);
				}
				HashMap<String, String> unchecked_children_map = new HashMap<>();
				for (String unchecked_child : unchecked_children) {
					unchecked_children_map.put(unchecked_child, null);
				}
				// 开始检查子类一致性
				ArrayList temp = new ArrayList<String>();// 记录不一致的子类，后面转成String[]
				Iterator<String> base_children_names = base_children_map.keySet().iterator();
				while (base_children_names.hasNext()) {
					String base_children_name = base_children_names.next();
					if (!unchecked_children_map.containsKey(base_children_name)) {
						System.out.println(java_file_name + "在" + base_project_name + "中有子类" + base_children_name + "," + unchecked_project_name + "中没有了");
						// 记录基准版本中有该子类，而待查版本中没有该子类的情况
						temp.add(base_children_name);
					}
				}
				// 将temp转成String[]，并记录到no_child_in_unchecked_project中，然后清空temp
				if (temp.size() > 0) {
					no_child_in_unchecked_project.put(java_file_name, Pair.createPair((String[]) temp.toArray(new String[temp.size()]), null));
				}
				temp.clear();
				Iterator<String> unchecked_children_names = unchecked_children_map.keySet().iterator();
				while (unchecked_children_names.hasNext()) {
					String unchecked_children_name = unchecked_children_names.next();
					if (!base_children_map.containsKey(unchecked_children_name)) {
						System.out.println(java_file_name + "在" + base_project_name + "中没有子类" + unchecked_children_name + "," + unchecked_project_name + "中添加了");
						// 记录基准版本中没有该子类，而待查版本中有该子类的情况
						temp.add(unchecked_children_name);
					}
				}
				// 将temp转成String[]，并记录到no_child_in_base_project中
				if (temp.size() > 0) {
					no_child_in_base_project.put(java_file_name, Pair.createPair(null, (String[]) temp.toArray(new String[temp.size()])));
				}
			}
		}
		// 遍历待查版本中的每一个类，检查其是否是新添加的类
		Iterator<String> unchecked_java_file_names = this.unchecked_hierarchy.getRight().get(Keys.extends_key).keySet().iterator();
		while (unchecked_java_file_names.hasNext()) {
			String java_file_name = unchecked_java_file_names.next();
			if (!this.base_hierarchy.getRight().get(Keys.extends_key).containsKey(java_file_name)) {
				System.out.println(java_file_name + "是" + unchecked_project_name + "中新添加的类");
				String unchecked_parent = (String) ((Pair) this.unchecked_hierarchy.getRight().get(Keys.extends_key).get(java_file_name)).getLeft();
				String[] unchecked_children = (String[]) ((Pair) this.unchecked_hierarchy.getRight().get(Keys.extends_key).get(java_file_name)).getRight();
				if (unchecked_parent != null) {
					System.out.println(java_file_name + "是" + unchecked_project_name + "中新添加的类，继承" + unchecked_parent);
				}
				for (String unchecked_child : unchecked_children) {
					System.out.println(java_file_name + "是" + unchecked_project_name + "中新添加的类，有子类" + unchecked_child);
				}
				// 记录下来。新添加的类只需在这里记录一次其在待查版本中的父类、子类即可。后面impl检查、call检查中不用再记录
				new_in_unchecked_project.put(java_file_name, Pair.createPair((String) ((Pair) this.unchecked_hierarchy.getRight().get(Keys.extends_key).get(java_file_name)).getLeft(),
						(String[]) ((Pair) this.unchecked_hierarchy.getRight().get(Keys.extends_key).get(java_file_name)).getRight()));
			}
		}
		System.out.println();
		extends_check_result.put(Keys.deleted_in_unchecked_project, deleted_in_unchecked_project);
		extends_check_result.put(Keys.no_parent_in_base_project, no_parent_in_base_project);
		extends_check_result.put(Keys.no_parent_in_unchecked_project, no_parent_in_unchecked_project);
		extends_check_result.put(Keys.different_parents, different_parents);
		extends_check_result.put(Keys.no_child_in_unchecked_project, no_child_in_unchecked_project);
		extends_check_result.put(Keys.no_child_in_base_project, no_child_in_base_project);
		extends_check_result.put(Keys.new_in_unchecked_project, new_in_unchecked_project);
		return extends_check_result;
	}

	private HashMap<String, HashMap<String, ?>> checkImplConsistency(String base_project_name, String unchecked_project_name) {
		// TODO Auto-generated method stub
		System.out.println("====实现关系一致性====");
		// 保存结果的HashMap。其中每个key都代表一类问题；value是一个HashMap，记录所有存在该问题的<类,涉及的关系>键值对
		HashMap<String, HashMap<String, ?>> impl_check_result = new HashMap<>();
		// 各类问题对应的HashMap
		HashMap<String, Pair<String[], String[]>> no_impls_in_unchecked_project = new HashMap<>();
		HashMap<String, Pair<String[], String[]>> no_impls_in_base_project = new HashMap<>();
		HashMap<String, Pair<String[], String[]>> no_impleds_in_unchecked_project = new HashMap<>();
		HashMap<String, Pair<String[], String[]>> no_impleds_in_base_project = new HashMap<>();
		HashMap<String, Pair<String[], String[]>> new_in_unchecked_project = new HashMap<>();
		// 遍历基准版本中的每一个类，检查待查版本中仍然保存的该类的实现、被实现关系是否与基准版本一致
		Iterator<String> base_java_file_names = this.base_hierarchy.getRight().get(Keys.impl_key).keySet().iterator();
		while (base_java_file_names.hasNext()) {
			String java_file_name = base_java_file_names.next();
			if (this.unchecked_hierarchy.getRight().get(Keys.impl_key).containsKey(java_file_name)) {
				// 检查在待查版本中未被删除的类的实现一致性
				String[] base_implements = (String[]) ((Pair) this.base_hierarchy.getRight().get(Keys.impl_key).get(java_file_name)).getLeft();
				String[] unchecked_implements = (String[]) ((Pair) this.unchecked_hierarchy.getRight().get(Keys.impl_key).get(java_file_name)).getLeft();
				HashMap<String, String> base_implements_map = new HashMap<>();
				for (String base_implement : base_implements) {
					base_implements_map.put(base_implement, null);
				}
				HashMap<String, String> unchecked_implements_map = new HashMap<>();
				for (String unchecked_implement : unchecked_implements) {
					unchecked_implements_map.put(unchecked_implement, null);
				}
				// 开始检查实现一致性
				ArrayList temp = new ArrayList<String>();// 记录不一致的实现、被实现关系，后面转成String[]
				Iterator<String> base_implements_names = base_implements_map.keySet().iterator();
				while (base_implements_names.hasNext()) {
					String base_implement_name = base_implements_names.next();
					if (!unchecked_implements_map.containsKey(base_implement_name)) {
						System.out.println(java_file_name + "在" + base_project_name + "中实现了" + base_implement_name + "," + unchecked_project_name + "中没有实现");
						// 记录下来
						temp.add(base_implement_name);
					}
				}
				// 将temp转成String[]，并记录到no_impls_in_unchecked_project中，然后清空temp
				if (temp.size() > 0) {
					no_impls_in_unchecked_project.put(java_file_name, Pair.createPair((String[]) temp.toArray(new String[temp.size()]), null));
				}
				temp.clear();
				Iterator<String> unchecked_implements_names = unchecked_implements_map.keySet().iterator();
				while (unchecked_implements_names.hasNext()) {
					String unchecked_implement_name = unchecked_implements_names.next();
					if (!base_implements_map.containsKey(unchecked_implement_name)) {
						System.out.println(java_file_name + "在" + base_project_name + "中没有实现" + unchecked_implement_name + "," + unchecked_project_name + "中实现了");
						// 记录下来
						temp.add(unchecked_implement_name);
					}
				}
				if (temp.size() > 0) {
					no_impls_in_base_project.put(java_file_name, Pair.createPair(null, (String[]) temp.toArray(new String[temp.size()])));
				}
				temp.clear();
				// 被实现一致性
				String[] base_implemented_bys = (String[]) ((Pair) this.base_hierarchy.getRight().get(Keys.impl_key).get(java_file_name)).getRight();
				String[] unchecked_implemented_bys = (String[]) ((Pair) this.unchecked_hierarchy.getRight().get(Keys.impl_key).get(java_file_name)).getRight();
				// 将String[]转存为HashMap，方便查询
				HashMap<String, String> base_implemented_by_map = new HashMap<>();
				for (String base_implemented_by : base_implemented_bys) {
					base_implemented_by_map.put(base_implemented_by, null);
				}
				HashMap<String, String> unchecked_implemented_by_map = new HashMap<>();
				for (String unchecked_implemented_by : unchecked_implemented_bys) {
					unchecked_implemented_by_map.put(unchecked_implemented_by, null);
				}
				// 开始检查被实现一致性
				Iterator<String> base_implemented_by_names = base_implemented_by_map.keySet().iterator();
				while (base_implemented_by_names.hasNext()) {
					String base_implemented_by_name = base_implemented_by_names.next();
					if (!unchecked_implemented_by_map.containsKey(base_implemented_by_name)) {
						System.out.println(java_file_name + "在" + base_project_name + "中被" + base_implemented_by_name + "实现了," + unchecked_project_name + "中没有被其实现");
						// 记录下来
						temp.add(base_implemented_by_name);
					}
				}
				if (temp.size() > 0) {
					no_impleds_in_unchecked_project.put(java_file_name, Pair.createPair((String[]) temp.toArray(new String[temp.size()]), null));
				}
				temp.clear();
				Iterator<String> unchecked_implemented_by_names = unchecked_implemented_by_map.keySet().iterator();
				while (unchecked_implemented_by_names.hasNext()) {
					String unchecked_implemented_by_name = unchecked_implemented_by_names.next();
					if (!base_implemented_by_map.containsKey(unchecked_implemented_by_name)) {
						System.out.println(java_file_name + "在" + base_project_name + "中没有被" + unchecked_implemented_by_name + "实现," + unchecked_project_name + "中被其实现了");
						// 记录下来
						temp.add(unchecked_implemented_by_name);
					}
				}
				if (temp.size() > 0) {
					no_impleds_in_base_project.put(java_file_name, Pair.createPair(null, (String[]) temp.toArray(new String[temp.size()])));
				}
			}
		}
		// 记录待查版本中新添加类的信息
		Iterator<String> unchecked_java_file_names = this.unchecked_hierarchy.getRight().get(Keys.impl_key).keySet().iterator();
		while (unchecked_java_file_names.hasNext()) {
			String java_file_name = unchecked_java_file_names.next();
			if (!this.base_hierarchy.getRight().get(Keys.impl_key).containsKey(java_file_name)) {
				System.out.println(java_file_name + "是" + unchecked_project_name + "中新添加的类");
				String[] unchecked_implements = (String[]) ((Pair) this.unchecked_hierarchy.getRight().get(Keys.impl_key).get(java_file_name)).getLeft();
				String[] unchecked_implemented_bys = (String[]) ((Pair) this.unchecked_hierarchy.getRight().get(Keys.impl_key).get(java_file_name)).getRight();
				for (String unchecked_implement : unchecked_implements) {
					System.out.println(java_file_name + "是" + unchecked_project_name + "中新添加的类，实现了" + unchecked_implement);
				}
				for (String unchecked_implemented_by : unchecked_implemented_bys) {
					System.out.println(java_file_name + "是" + unchecked_project_name + "中新添加的类，被" + unchecked_implemented_by + "实现了");
				}
				// 记录下来
				new_in_unchecked_project.put(java_file_name, Pair.createPair(unchecked_implements, unchecked_implemented_bys));
			}
		}
		System.out.println();
		impl_check_result.put(Keys.no_impls_in_unchecked_project, no_impls_in_unchecked_project);
		impl_check_result.put(Keys.no_impls_in_base_project, no_impls_in_base_project);
		impl_check_result.put(Keys.no_impleds_in_unchecked_project, no_impleds_in_unchecked_project);
		impl_check_result.put(Keys.no_impleds_in_base_project, no_impleds_in_base_project);
		impl_check_result.put(Keys.new_in_unchecked_project, new_in_unchecked_project);
		return impl_check_result;
	}

	private HashMap<String, HashMap<String, ?>> checkCallConsistency(String base_project_name, String unchecked_project_name) {
		// TODO Auto-generated method stub
		System.out.println("====调用关系一致性====");
		// 保存结果的HashMap。其中每个key都代表一类问题；value是一个HashMap，记录所有存在该问题的<类,涉及的关系>键值对
		HashMap<String, HashMap<String, ?>> call_check_result = new HashMap<>();
		// 各类问题对应的HashMap
		HashMap<String, Pair<String[], String[]>> no_callers_in_unchecked_project = new HashMap<>();
		HashMap<String, Pair<String[], String[]>> no_callers_in_base_project = new HashMap<>();
		HashMap<String, Pair<String[], String[]>> no_callees_in_unchecked_project = new HashMap<>();
		HashMap<String, Pair<String[], String[]>> no_callees_in_base_project = new HashMap<>();
		HashMap<String, Pair<String[], String[]>> new_in_unchecked_project = new HashMap<>();
		// 遍历基准版本中的每一个类，检查待查版本中仍然保存的该类的被调用、调用关系是否与基准版本一致
		Iterator<String> base_java_file_names = this.base_hierarchy.getRight().get(Keys.call_key).keySet().iterator();
		while (base_java_file_names.hasNext()) {
			String java_file_name = base_java_file_names.next();
			if (this.unchecked_hierarchy.getRight().get(Keys.call_key).containsKey(java_file_name)) {
				// 检查待查版本中未被删除的类的被调用一致性
				String[] base_callers = (String[]) ((Pair) this.base_hierarchy.getRight().get(Keys.call_key).get(java_file_name)).getLeft();
				String[] unchecked_callers = (String[]) ((Pair) this.unchecked_hierarchy.getRight().get(Keys.call_key).get(java_file_name)).getLeft();
				// 将String[]转存为HashMap，方便查询
				HashMap<String, String> base_callers_map = new HashMap<>();
				for (String base_caller : base_callers) {
					base_callers_map.put(base_caller, null);
				}
				HashMap<String, String> unchecked_callers_map = new HashMap<>();
				for (String unchecked_caller : unchecked_callers) {
					unchecked_callers_map.put(unchecked_caller, null);
				}
				// 开始检查被调用一致性
				ArrayList temp = new ArrayList<String>();// 记录不一致的被调用关系，后面转成String[]
				Iterator<String> base_callers_names = base_callers_map.keySet().iterator();
				while (base_callers_names.hasNext()) {
					String base_caller_name = base_callers_names.next();
					if (!unchecked_callers_map.containsKey(base_caller_name)) {
						System.out.println(java_file_name + "在" + base_project_name + "中被" + base_caller_name + "调用," + unchecked_project_name + "中没有被其调用");
						// 记录下来
						temp.add(base_caller_name);
					}
				}
				// 将temp转成String[]，并记录到no_callers_in_unchecked_project中，然后清空temp
				if (temp.size() > 0) {
					no_callers_in_unchecked_project.put(java_file_name, Pair.createPair((String[]) temp.toArray(new String[temp.size()]), null));
				}
				temp.clear();
				Iterator<String> unchecked_callers_names = unchecked_callers_map.keySet().iterator();
				while (unchecked_callers_names.hasNext()) {
					String unchecked_caller_name = unchecked_callers_names.next();
					if (!base_callers_map.containsKey(unchecked_caller_name)) {
						System.out.println(java_file_name + "在" + base_project_name + "中没有被" + unchecked_caller_name + "调用," + unchecked_project_name + "中被其调用了");
						// 记录下来
						temp.add(unchecked_caller_name);
					}
				}
				if (temp.size() > 0) {
					no_callers_in_base_project.put(java_file_name, Pair.createPair(null, (String[]) temp.toArray(new String[temp.size()])));
				}
				temp.clear();
				// 调用一致性
				String[] base_callees = (String[]) ((Pair) this.base_hierarchy.getRight().get(Keys.call_key).get(java_file_name)).getRight();
				String[] unchecked_callees = (String[]) ((Pair) this.unchecked_hierarchy.getRight().get(Keys.call_key).get(java_file_name)).getRight();
				// 将String[]转存为HashMap，方便查询
				HashMap<String, String> base_callees_map = new HashMap<>();
				for (String base_callee : base_callees) {
					base_callees_map.put(base_callee, null);
				}
				HashMap<String, String> unchecked_callees_map = new HashMap<>();
				for (String unchecked_callee : unchecked_callees) {
					unchecked_callees_map.put(unchecked_callee, null);
				}
				// 开始检查调用一致性
				Iterator<String> base_callees_names = base_callees_map.keySet().iterator();
				while (base_callees_names.hasNext()) {
					String base_callee_name = base_callees_names.next();
					if (!unchecked_callees_map.containsKey(base_callee_name)) {
						System.out.println(java_file_name + "在" + base_project_name + "中调用" + base_callee_name + "," + unchecked_project_name + "中没有调用");
						// 记录下来
						temp.add(base_callee_name);
					}
				}
				if (temp.size() > 0) {
					no_callees_in_unchecked_project.put(java_file_name, Pair.createPair((String[]) temp.toArray(new String[temp.size()]), null));
				}
				temp.clear();
				Iterator<String> unchecked_callees_names = unchecked_callees_map.keySet().iterator();
				while (unchecked_callees_names.hasNext()) {
					String unchecked_callee_name = unchecked_callees_names.next();
					if (!base_callees_map.containsKey(unchecked_callee_name)) {
						System.out.println(java_file_name + "在" + base_project_name + "中没有调用" + unchecked_callee_name + "," + unchecked_project_name + "中调用了");
						// 记录下来
						temp.add(unchecked_callee_name);
					}
				}
				if (temp.size() > 0) {
					no_callees_in_base_project.put(java_file_name, Pair.createPair(null, (String[]) temp.toArray(new String[temp.size()])));
				}
			}
		}
		Iterator<String> unchecked_java_file_names = this.unchecked_hierarchy.getRight().get(Keys.call_key).keySet().iterator();
		while (unchecked_java_file_names.hasNext()) {
			String java_file_name = unchecked_java_file_names.next();
			if (!this.base_hierarchy.getRight().get(Keys.call_key).containsKey(java_file_name)) {
				System.out.println(java_file_name + "是" + unchecked_project_name + "中新添加的类");
				String[] unchecked_callers = (String[]) ((Pair) this.unchecked_hierarchy.getRight().get(Keys.call_key).get(java_file_name)).getLeft();
				String[] unchecked_callees = (String[]) ((Pair) this.unchecked_hierarchy.getRight().get(Keys.call_key).get(java_file_name)).getRight();
				for (String unchecked_caller : unchecked_callers) {
					System.out.println(java_file_name + "是" + unchecked_project_name + "中新添加的类，被" + unchecked_caller + "调用了");
				}
				for (String unchecked_callee : unchecked_callees) {
					System.out.println(java_file_name + "是" + unchecked_project_name + "中新添加的类，调用了" + unchecked_callee);
				}
				// 记录下来
				new_in_unchecked_project.put(java_file_name, Pair.createPair(unchecked_callers, unchecked_callees));
			}
		}
		System.out.println();
		call_check_result.put(Keys.no_callers_in_unchecked_project, no_callers_in_unchecked_project);
		call_check_result.put(Keys.no_callers_in_base_project, no_callers_in_base_project);
		call_check_result.put(Keys.no_callees_in_unchecked_project, no_callees_in_unchecked_project);
		call_check_result.put(Keys.no_callees_in_base_project, no_callees_in_base_project);
		call_check_result.put(Keys.new_in_unchecked_project, new_in_unchecked_project);
		return call_check_result;
	}
}
