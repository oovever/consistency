package com.buaa.huawei.consistency.dialog;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.buaa.huawei.consistency.util.Pair;
import com.buaa.huawei.consistency.util.UMLDiagramType;

public class Mode1SelectionDialog extends Dialog {
	private String model_name = null;
	private IProject[] projects = null;
	private Pair<String, Integer> selection = new Pair<>();
	private Pair<String, Integer> temp_selection = new Pair<>();

	public Mode1SelectionDialog(Shell parentShell, String model_name, IProject[] projects) {
		// TODO Auto-generated constructor stub
		super(parentShell);
		this.model_name = model_name;
		this.projects = projects;
		this.open();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialog_area = new Composite(parent, SWT.NONE);
		dialog_area.setLayout(new GridLayout(2, true));
		/* 对话框有两个Group，第一个选择当前模型UML类型，第二个选择项目待查版本 */
		// 选择当前模型UML图类型的Group
		Group model_group = new Group(dialog_area, SWT.NONE);
		model_group.setLayout(new GridLayout(1, true));
		Label model_label = new Label(model_group, SWT.NONE);
		model_label.setText("模型文件：");
		Text model_text = new Text(model_group, SWT.NONE);
		model_text.setText(this.model_name);
		model_text.setEditable(false);
		/*
		 * // 让用户选择模型所用UML图的类型（单选） Button graph_type1 = new Button(model_group, SWT.RADIO); Button
		 * graph_type2 = new Button(model_group, SWT.RADIO); graph_type1.setText("组件图");
		 * graph_type2.setText("类图"); graph_type1.addSelectionListener(new SelectionAdapter() {
		 * 
		 * @Override public void widgetSelected(SelectionEvent e) {
		 * temp_selection.setLeft(UMLDiagramType.type1); } }); graph_type2.addSelectionListener(new
		 * SelectionAdapter() {
		 * 
		 * @Override public void widgetSelected(SelectionEvent e) {
		 * temp_selection.setLeft(UMLDiagramType.type2); } });
		 */
		model_label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		// 选择项目待查版本的Group
		Group unchecked_group = new Group(dialog_area, SWT.NONE);
		unchecked_group.setLayout(new GridLayout(1, true));
		Label unchecked_label = new Label(unchecked_group, SWT.NONE);
		unchecked_label.setText("请选择项目的一个版本：");
		// 读取projects，填充下拉列表
		Combo unchecked_list = new Combo(unchecked_group, SWT.DROP_DOWN | SWT.READ_ONLY);
		for (int i = 0; i < this.projects.length; i++) {
			unchecked_list.add(this.projects[i].getName());
			unchecked_list.setData(String.valueOf(i), i);
		}
		unchecked_list.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String selected_item_index = String.valueOf(unchecked_list.getSelectionIndex());
				int selected_project_index = (int) unchecked_list.getData(selected_item_index);
				temp_selection.setRight(new Integer(selected_project_index));
			}
		});
		unchecked_label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		unchecked_list.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		// 最后设置两个Group在dialog_area中的布局数据
		model_group.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		unchecked_group.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		return dialog_area;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		// 如果单击确定按钮，则将值保存到selection中
		if (buttonId == IDialogConstants.OK_ID) {
			this.selection.setLeft(this.temp_selection.getLeft());
			this.selection.setRight(this.temp_selection.getRight());
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * 返回用户选择的模型UML图类型和项目待查版本
	 */
	public Pair<String, Integer> getSelection() {
		// TODO Auto-generated method stub
		if (this.selection.getRight() == null) {
			System.err.println("没有选择项目待查版本");
			return null;
		} else {
			System.out.println("模型文件: " + this.model_name);
			System.out.println("选择待查版本: " + this.projects[this.selection.getRight().intValue()].getName());
			System.out.println();
			return this.selection;
		}
	}
}
