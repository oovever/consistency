package com.buaa.huawei.consistency.dialog;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.buaa.huawei.consistency.util.Pair;

public class Mode2SelectionDialog extends Dialog {
	private IProject[] projects = null;
	private Pair<Integer, Integer> selection = new Pair<>();
	private Pair<Integer, Integer> temp_selection = new Pair<>();

	public Mode2SelectionDialog(Shell parentShell, IProject[] projects) {
		// TODO Auto-generated constructor stub
		super(parentShell);
		this.projects = projects;
		this.open();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialog_area = new Composite(parent, SWT.NONE);
		dialog_area.setLayout(new GridLayout(2, true));
		/* 对话框有两个Group，每个Group里有一个项目列表，分别用于选择项目基准版本和待查版本 */
		// 选择基准版本的Group
		Group base_group = new Group(dialog_area, SWT.NONE);
		base_group.setLayout(new GridLayout(1, true));
		Label base_label = new Label(base_group, SWT.NONE);
		base_label.setText("请选择项目的基准版本：");
		// 读取projects，填充下拉列表
		Combo base_list = new Combo(base_group, SWT.DROP_DOWN | SWT.READ_ONLY);
		for (int i = 0; i < this.projects.length; i++) {
			base_list.add(this.projects[i].getName());
			base_list.setData(String.valueOf(i), i);
		}
		base_list.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String selected_item_index = String.valueOf(base_list.getSelectionIndex());
				int selected_project_index = (int) base_list.getData(selected_item_index);
				temp_selection.setLeft(new Integer(selected_project_index));
			}
		});
		base_label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		base_list.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		// 选择待查版本的Group
		Group unchecked_group = new Group(dialog_area, SWT.NONE);
		unchecked_group.setLayout(new GridLayout(1, true));
		Label unchecked_label = new Label(unchecked_group, SWT.NONE);
		unchecked_label.setText("请选择项目的待查版本：");
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
		base_group.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		unchecked_group.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		return dialog_area;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		// 如果单击确定按钮，则将值保存到Pair中
		if (buttonId == IDialogConstants.OK_ID) {
			this.selection.setLeft(this.temp_selection.getLeft());
			this.selection.setRight(this.temp_selection.getRight());
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * 返回用户选择的项目基准版本和项目待查版本
	 */
	public Pair<Integer, Integer> getSelection() {
		// TODO Auto-generated method stub
		if ((this.selection.getLeft() == null) || (this.selection.getRight() == null)) {
			if (this.selection.getLeft() == null) {
				System.err.println("没有选择项目基准版本");
			}
			if (this.selection.getRight() == null) {
				System.err.println("没有选择项目待查版本");
			}
			return null;
		} else {
			System.out.println("项目基准版本: " + this.projects[this.selection.getLeft().intValue()].getName());
			System.out.println("选择待查版本: " + this.projects[this.selection.getRight().intValue()].getName());
			System.out.println();
			return this.selection;
		}
	}
}
