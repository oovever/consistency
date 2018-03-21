package com.buaa.huawei.consistency.handler;

import java.util.HashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.buaa.huawei.consistency.consistencyChecker.ModelAndProjectConsistencyChecker;
import com.buaa.huawei.consistency.consistencyChecker.ProjectAndProjectConsistencyChecker;
import com.buaa.huawei.consistency.dialog.Mode1SelectionDialog;
import com.buaa.huawei.consistency.dialog.Mode2SelectionDialog;
import com.buaa.huawei.consistency.dialog.ModeSelectionDialog;
import com.buaa.huawei.consistency.hierarchyGetter.modelHierarchyGetter.ModelHierarchyGetter;
import com.buaa.huawei.consistency.hierarchyGetter.projectHierarchyGetter.ProjectHierarchyGetter;
import com.buaa.huawei.consistency.results.ResultRecorder;
import com.buaa.huawei.consistency.util.FileEditor;
import com.buaa.huawei.consistency.util.Mode;
import com.buaa.huawei.consistency.util.Model;
import com.buaa.huawei.consistency.util.Pair;

/**
 * This handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class ConsistencyHandler extends AbstractHandler {
	// TODO Auto-generated method stub

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		/*
		 * 获取workspace中的所有projects
		 */
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		if (projects.length <= 0) {
			MessageDialog.openInformation(window.getShell(), "错误", "请导入项目。");
		} else {
			Pair<Pair<String, String>, HashMap<String, HashMap<String, HashMap<String, ?>>>> consistency_check_result = null;
			// 获取model explorer中的model
			Model model = getModel();
			// 用户选择模式：1、模型与代码对比；2、代码与代码对比
			String mode = selectMode(window.getShell(), model != null);
			// String mode = selectMode(window.getShell(), false);// 测试代码，只进行代码间对比
			if (mode == null) {
				System.err.println("由于没有选择模式，所以接下来什么也不做");
				System.out.println();
			} else {
				switch (mode) {
				case Mode.mode1:
					consistency_check_result = checkModelAndProject(window.getShell(), model, projects);
					break;
				case Mode.mode2:
					consistency_check_result = checkProjectAndProject(window.getShell(), projects);
					break;
				default:
					System.err.println("跳到这里说明选择模式事件发生未知错误！去检查Mode和ModeSelectionDialog");
					break;
				}
				if (consistency_check_result != null) {
					// 将检查结果持久化存储，注意这里的路径
					ResultRecorder recorder = recordResults(consistency_check_result, Platform.getInstanceLocation().getURL().getFile() + "/Consistency Check/");
					// 展示持久化存储的结果
					show(recorder.getResults());
				} else {
					System.err.println("检查结果变成null了！");
				}
			}
		}
		return null;
	}

	/**
	 * 获取Model Explorer中打开的模型
	 * 
	 * @param string
	 * @param consistency_check_result
	 */
	
	private Model getModel() {
		// TODO Auto-generated method stub
		Model model = new Model();
		/*
		 * IWorkbenchPage workbench_page =
		 * PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(); IViewReference[] view_refs
		 * = workbench_page.getViewReferences(); for (IViewReference view_ref : view_refs) { IViewPart
		 * view_part = view_ref.getView(false); // 获取Model Explorer视图 if (view_part instanceof
		 * ModelExplorerPageBookView) { workbench_page.activate(view_part); ModelExplorerView
		 * model_explorer_view = (ModelExplorerView) ((ModelExplorerPageBookView)
		 * view_part).getActiveView(); if (model_explorer_view != null) { TreeItem[] models =
		 * model_explorer_view.getCommonViewer().getTree().getItems(); if (models.length <= 0) {
		 * System.err.println("获取模型失败"); } else { model = (Model) (((EObjectTreeElement)
		 * models[0].getData()).getEObject()); } } } }
		 */
		return model;
	}

	/**
	 * 用户选择一致性检查模式：1、模型与项目对比；2、项目与项目对比
	 * 
	 * @param shell
	 * @param model_is_opened
	 */
	private String selectMode(Shell shell, boolean model_is_opened) {
		// TODO Auto-generated method stub
		ModeSelectionDialog mode_selection_dialog = new ModeSelectionDialog(shell, model_is_opened);
		return mode_selection_dialog.getModeSelection();
	}

	/**
	 * 检查一个工程的模型和版本之间一致性
	 * 
	 * @param shell
	 * @param model
	 * @param projects
	 * @return
	 */
	private Pair<Pair<String, String>, HashMap<String, HashMap<String, HashMap<String, ?>>>> checkModelAndProject(Shell shell, Model model, IProject[] projects) {
		// TODO Auto-generated method stub
		// 用户在弹出的SelectionDialog中进行操作
		Mode1SelectionDialog selection_dialog = new Mode1SelectionDialog(shell, model.getName(), projects);
		Pair<String, Integer> index_pair = selection_dialog.getSelection();
		if (index_pair != null) {
			// 获取模型的各种关系。此处使用穆鹏飞的模型文件解析
			Pair<String, HashMap<String, HashMap<String, ?>>> base_hierarchy = ModelHierarchyGetter.getHierarchy(model);
			// 获取待查版本的调用、继承、实现关系
			Pair<String, HashMap<String, HashMap<String, ?>>> unchecked_hierarchy = ProjectHierarchyGetter.getHierarchy(projects[index_pair.getRight().intValue()]);
			// 一致性检查。解析穆鹏飞的模型后，仿照代码检查即可
			ModelAndProjectConsistencyChecker mp_checker = new ModelAndProjectConsistencyChecker(base_hierarchy, unchecked_hierarchy, index_pair.getLeft());
			return mp_checker.checkConsistency();
		} else {
			System.err.println("由于没有选择模型图类型或项目版本，所以接下来什么也不做");
			return null;
		}
	}

	/**
	 * 检查一个工程的基准版本和待查版本之间一致性
	 * 
	 * @param shell
	 * @param projects
	 * @return
	 */
	private Pair<Pair<String, String>, HashMap<String, HashMap<String, HashMap<String, ?>>>> checkProjectAndProject(Shell shell, IProject[] projects) {
		// TODO Auto-generated method stub
		// 用户在弹出的SelectionDialog中进行操作
		Mode2SelectionDialog selection_dialog = new Mode2SelectionDialog(shell, projects);
		Pair<Integer, Integer> index_pair = selection_dialog.getSelection();
		if (index_pair != null) {
			// 获取基准版本的调用、继承、实现关系
			Pair<String, HashMap<String, HashMap<String, ?>>> base_hierarchy = ProjectHierarchyGetter.getHierarchy(projects[index_pair.getLeft().intValue()]);
			// 获取待查版本的调用、继承、实现关系
			Pair<String, HashMap<String, HashMap<String, ?>>> unchecked_hierarchy = ProjectHierarchyGetter.getHierarchy(projects[index_pair.getRight().intValue()]);
			// 进行一致性检查
			ProjectAndProjectConsistencyChecker pp_checker = new ProjectAndProjectConsistencyChecker(base_hierarchy, unchecked_hierarchy);
			// 返回检查结果
			return pp_checker.checkConsistency();
		} else {
			System.err.println("由于没有选择足够的项目版本，所以接下来什么也不做");
			return null;
		}
	}

	/**
	 * 将检查结果持久化存储，便于分析、展示
	 * 
	 * @param consistency_check_result
	 * @param dir
	 * @return
	 */
	private ResultRecorder recordResults(Pair<Pair<String, String>, HashMap<String, HashMap<String, HashMap<String, ?>>>> consistency_check_result, String dir) {
		ResultRecorder recorder = new ResultRecorder(consistency_check_result, dir);
		return recorder;
	}

	/**
	 * 展示一致性检查效果
	 * 
	 * @param results
	 */
	private void show(HashMap<String, FileEditor> results) {
		// TODO Auto-generated method stub
		System.err.println("展示还没有做");
	}

}
