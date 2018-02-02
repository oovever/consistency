package com.buaa.huawei.consistency.hierarchyGetter.modelHierarchyGetter;

import java.util.HashMap;

import org.eclipse.ui.internal.Model;

import com.buaa.huawei.consistency.util.Pair;

public class ModelHierarchyGetter {
	private static Pair<String, HashMap<String, HashMap<String, ?>>> model_hierarchy = null;

	/*public static Pair<String, HashMap<String, HashMap<String, ?>>> getHierarchy(Model model, String diagram_type) {
		// TODO Auto-generated method stub
		
		// 对不同UML图有不同解析方法
		switch (diagram_type) {
		case UMLDiagramType.type1:
			System.err.println("没做组件图解析");
			// model_hierarchy=getComponentDiagramHierarchy(model);
			break;
		case UMLDiagramType.type2:
			model_hierarchy = getClassDiagramHierarchy(model, diagram_type);
			break;
		default:
			System.err.println("跳到这里说明选择图类型事件发生未知错误！去检查UMLDiagramType和Mode1SelectionDialog");
			break;
		}
		return model_hierarchy;
		
	}*/

	private static Pair<String, HashMap<String, HashMap<String, ?>>> getClassDiagramHierarchy(Model model, String graph_type) {
		// TODO Auto-generated method stub
		//System.out.println("/*\n * " + model.getName() + "（" + graph_type + "）的继承、实现、调用关系\n */");
		/*
		// 获取模型的继承关系。首先是继承自谁（父类）；然后是被谁继承（子类）
		HashMap<String, Pair<String, String[]>> extends_hierarchy = ModelExtendHierarchyGetter.getExtendsHierarchy(model);
		////下面的还得改！！！！！！！！！！！！！！！！！！！！！！！
		// 获取模型的实现关系。首先是实现了谁；然后是被谁实现
		HashMap<String, Pair<String[], String[]>> impl_hierarchy = ModelImplementsHierarchyGetter.getImplHierarchy(model);
		// 获取模型的调用关系。首先是caller（当前类的调用者，即当前类被谁调用）；然后是callee（当前类的被调用者，即当前类调用了谁）
		HashMap<String, Pair<String[], String[]>> call_hierarchy = ModelCallHierarchyGetter.getCallHierarchy(model);
		if ((extends_hierarchy != null) && (impl_hierarchy != null) && (call_hierarchy != null)) {
			HashMap<String, HashMap<String, ?>> all_hierarchy = new HashMap<>();
			// 整合获取到的三种关系
			all_hierarchy.put(HierarchyKey.extends_key, extends_hierarchy);
			all_hierarchy.put(HierarchyKey.impl_key, impl_hierarchy);
			all_hierarchy.put(HierarchyKey.call_key, call_hierarchy);
			model_hierarchy = new Pair<>();
			model_hierarchy.setLeft(model.getName());
			model_hierarchy.setRight(all_hierarchy);
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
		}*/
		return null;
	}
}
