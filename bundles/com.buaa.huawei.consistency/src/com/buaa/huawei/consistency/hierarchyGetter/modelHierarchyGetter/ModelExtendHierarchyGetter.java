package com.buaa.huawei.consistency.hierarchyGetter.modelHierarchyGetter;

import java.util.HashMap;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.ui.internal.Model;

import com.buaa.huawei.consistency.util.Pair;

public class ModelExtendHierarchyGetter {

	public static HashMap<String, Pair<String, String[]>> getExtendsHierarchy(Model model) {
		// TODO Auto-generated method stub
		System.out.println("=======继承关系=======");
		/*EList<PackageableElement> packaged_elements = model.getPackagedElements();
		for (PackageableElement e : packaged_elements) {
			if (e.getName().equals("generated")) {
				// 从这里开始遍历模型，想想怎么存！！！！！！！！！！！！！！！！！
				EList<EObject> eList = e.eContents();
				eList.forEach(eObj -> traverse(eObj, "\t"));
			}
		}*/
		return null;
	}

	private static void traverse(EObject eObject, String string) {
		// 打印信息。其实这里是可以存到一个树状结构里的
		System.out.println(string + eObject.toString());
		// 递归调用，完成遍历
		EList<EObject> eList = eObject.eContents();
		eList.forEach(eObj -> traverse(eObj, string + "\t"));
	}
}
