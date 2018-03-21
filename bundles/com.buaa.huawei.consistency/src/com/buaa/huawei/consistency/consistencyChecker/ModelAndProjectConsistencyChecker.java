package com.buaa.huawei.consistency.consistencyChecker;

import java.util.HashMap;

import com.buaa.huawei.consistency.util.Pair;

public class ModelAndProjectConsistencyChecker {
	private Pair<String, HashMap<String, HashMap<String, ?>>> base_hierarchy = null;
	private Pair<String, HashMap<String, HashMap<String, ?>>> unchecked_hierarchy = null;

	public ModelAndProjectConsistencyChecker(Pair<String, HashMap<String, HashMap<String, ?>>> base_hierarchy, Pair<String, HashMap<String, HashMap<String, ?>>> unchecked_hierarchy, String string) {
		// TODO Auto-generated constructor stub
		this.base_hierarchy = base_hierarchy;
		this.unchecked_hierarchy = unchecked_hierarchy;
	}

	public Pair<Pair<String, String>, HashMap<String, HashMap<String, HashMap<String, ?>>>> checkConsistency() {
		// TODO Auto-generated method stub
		ProjectAndProjectConsistencyChecker pp_checker = new ProjectAndProjectConsistencyChecker(this.base_hierarchy, this.unchecked_hierarchy);
		return pp_checker.checkConsistency();
	}

}
