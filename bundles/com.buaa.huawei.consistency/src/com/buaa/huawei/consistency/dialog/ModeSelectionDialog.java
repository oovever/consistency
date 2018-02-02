package com.buaa.huawei.consistency.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.buaa.huawei.consistency.util.Mode;

public class ModeSelectionDialog extends Dialog {
	private boolean model_is_opened = false;
	private String selection = null;
	private String temp_selection = null;

	public ModeSelectionDialog(Shell parentShell, boolean model_is_opened) {
		// TODO Auto-generated constructor stub
		super(parentShell);
		this.model_is_opened = model_is_opened;
		this.open();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialog_area = new Composite(parent, SWT.NONE);
		dialog_area.setLayout(new GridLayout(1, true));
		/* 对话框一个Group，Group里有两种可选模式（模型对比代码、代码对比代码），用户只能选择一种模式。假如没有打开模型，则不能选择模型对比代码模式 */
		Group group = new Group(dialog_area, SWT.NONE);
		group.setLayout(new GridLayout(1, true));
		Label label = new Label(group, SWT.NONE);
		label.setText("请选择一致性检查模式：");
		// 读取model，决定对用户开放选择的模式
		Button mode1 = new Button(group, SWT.RADIO);
		Button mode2 = new Button(group, SWT.RADIO);
		mode1.setText("模型与项目一致性检查");
		mode2.setText("项目与项目一致性检查");
		// 假如没有打开模型，则不能选择模型对比代码模式
		if (!this.model_is_opened) {
			mode1.setEnabled(false);
		}
		mode1.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				temp_selection = Mode.mode1;
			}
		});
		mode2.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				temp_selection = Mode.mode2;
			}
		});
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		// 最后设置两个Group在dialog_area中的布局数据
		group.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		return dialog_area;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		// 如果单击确定按钮，则将值保存到Pair中
		if (buttonId == IDialogConstants.OK_ID) {
			this.selection = this.temp_selection;
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * 返回用户选择模式
	 */
	public String getModeSelection() {
		// TODO Auto-generated method stub
		if (this.selection == null) {
			System.err.println("没有选择模式");
			return null;
		} else {
			System.out.println("选择模式: " + this.selection);
			System.out.println();
			return this.selection;
		}
	}
}
