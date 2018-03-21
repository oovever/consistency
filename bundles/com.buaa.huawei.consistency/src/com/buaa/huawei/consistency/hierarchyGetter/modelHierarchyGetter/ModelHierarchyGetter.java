package com.buaa.huawei.consistency.hierarchyGetter.modelHierarchyGetter;

import java.util.HashMap;

import com.buaa.huawei.consistency.util.Model;
import com.buaa.huawei.consistency.util.Pair;

public class ModelHierarchyGetter {
	private static Pair<String, HashMap<String, HashMap<String, ?>>> model_hierarchy = null;

	public static Pair<String, HashMap<String, HashMap<String, ?>>> getHierarchy(Model model) {
		// TODO Auto-generated method stub
		model_hierarchy = new Pair<>();
		return model_hierarchy;
	}
}
