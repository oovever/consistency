package com.buaa.huawei.consistency.results;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import com.buaa.huawei.consistency.util.FileEditor;
import com.buaa.huawei.consistency.util.Keys;
import com.buaa.huawei.consistency.util.Pair;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class ResultRecorder {
	private String result_file_tail = " check result.xls";
	private FileEditor extends_result_file_editor = null;
	private FileEditor impl_result_file_editor = null;
	private FileEditor call_result_file_editor = null;
	public static final int class_name_column = 0;
	private int pair_left_column = 1;
	private int pair_right_column = 2;
	private String base_project_name = null;
	private String unchecked_project_name = null;

	public ResultRecorder(Pair<Pair<String, String>, HashMap<String, HashMap<String, HashMap<String, ?>>>> result, String dir) {
		record(result, dir);
	}

	private void record(Pair<Pair<String, String>, HashMap<String, HashMap<String, HashMap<String, ?>>>> consistency_check_result, String file_dir) {
		// TODO Auto-generated method stub
		HashMap<String, HashMap<String, ?>> extends_result = consistency_check_result.getRight().get(Keys.extends_key);
		HashMap<String, HashMap<String, ?>> impl_result = consistency_check_result.getRight().get(Keys.impl_key);
		HashMap<String, HashMap<String, ?>> call_result = consistency_check_result.getRight().get(Keys.call_key);
		this.base_project_name = consistency_check_result.getLeft().getLeft();
		this.unchecked_project_name = consistency_check_result.getLeft().getRight();
		recordExtendsResult(extends_result, file_dir);
		recordImplResult(impl_result, file_dir);
		recordCallResult(call_result, file_dir);
	}

	private void recordExtendsResult(HashMap<String, HashMap<String, ?>> extends_result, String file_dir) {
		this.extends_result_file_editor = new FileEditor(file_dir + this.base_project_name + "&" + this.unchecked_project_name + File.separator + Keys.extends_key + this.result_file_tail);
		try {
			// 创建一个工作簿
			WritableWorkbook workbook = Workbook.createWorkbook(this.extends_result_file_editor.getFile());
			// 遍历当前检查结果中的所有不一致问题，将其存到同一个工作簿的不同工作表中
			Iterator<String> extends_problems = extends_result.keySet().iterator();
			int sheet_index = 0;
			while (extends_problems.hasNext()) {
				// 当前的不一致问题
				String current_problem = extends_problems.next();
				// 创建一个工作表，记录具有当前问题的类和关系
				WritableSheet current_problem_sheet = workbook.createSheet(current_problem, sheet_index);
				sheet_index++;
				// 具有当前问题的所有类的map，键值对为<具有当前不一致问题的类名, 该类所对应的不一致关系>。具体格式见ProjectAndProjectConsistencyChecker
				HashMap<String, ?> current_problem_class_map = extends_result.get(current_problem);
				// 遍历具有当前问题的类，向工作表中添加数据
				Iterator<String> current_problem_classes = current_problem_class_map.keySet().iterator();
				int current_class_row = 0;
				// 根据不同问题的格式，决定存储方式
				switch (current_problem) {
				// 格式相同的问题共用一种存储方式
				case Keys.deleted_in_unchecked_project:
				case Keys.new_in_unchecked_project: {
					while (current_problem_classes.hasNext()) {
						// 具有当前问题的类
						String current_class = current_problem_classes.next();
						// 在第一列依序添加类名
						current_problem_sheet.addCell(new Label(ResultRecorder.class_name_column, current_class_row, current_class));
						// 当前的问题的信息对
						Pair<String, String[]> parent_children = (Pair<String, String[]>) current_problem_class_map.get(current_class);
						// 信息对左
						current_problem_sheet.addCell(new Label(this.pair_left_column, current_class_row, parent_children.getLeft()));
						// 信息对右
						int child_row = current_class_row;
						for (int child_index = 0; child_index < parent_children.getRight().length; child_row++, child_index++) {
							current_problem_sheet.addCell(new Label(this.pair_right_column, child_row, parent_children.getRight()[child_index]));
						}
						// 下一个具有当前问题的类的起始行。注意即使信息对任意一端长度为0时，也要将current_class_row+1
						int next_class_row = current_class_row + max(1, parent_children.getRight().length, 1);
						/*
						 * 合并当前类的类名、信息对中非数组端的单元格，分别是：要合并的所有单元格（最左上角的列号，最左上角的行号，最右下角的列号，最右下角的行号）
						 */
						current_problem_sheet.mergeCells(ResultRecorder.class_name_column, current_class_row, ResultRecorder.class_name_column, next_class_row - 1);
						current_problem_sheet.mergeCells(this.pair_left_column, current_class_row, this.pair_left_column, next_class_row - 1);
						// 修改current_class_row
						current_class_row = next_class_row;
					}
					break;
				}
				case Keys.no_parent_in_base_project:
				case Keys.no_parent_in_unchecked_project:
				case Keys.different_parents: {
					while (current_problem_classes.hasNext()) {
						// 具有当前问题的类
						String current_class = current_problem_classes.next();
						// 在第一列依序添加类名
						current_problem_sheet.addCell(new Label(ResultRecorder.class_name_column, current_class_row, current_class));
						// 当前的问题的信息对
						Pair<String, String> base_parent_unchecked_parent = (Pair<String, String>) current_problem_class_map.get(current_class);
						// 信息对左
						current_problem_sheet.addCell(new Label(this.pair_left_column, current_class_row, base_parent_unchecked_parent.getLeft()));
						// 信息对右
						current_problem_sheet.addCell(new Label(this.pair_right_column, current_class_row, base_parent_unchecked_parent.getRight()));
						// 下一个具有当前问题的类的起始行。注意即使信息对任意一端长度为0时，也要将current_class_row+1
						int next_class_row = current_class_row + max(1, 1, 1);
						// 修改current_class_row
						current_class_row = next_class_row;
					}
					break;
				}
				case Keys.no_child_in_unchecked_project:
				case Keys.no_child_in_base_project: {
					while (current_problem_classes.hasNext()) {
						// 具有当前问题的类
						String current_class = current_problem_classes.next();
						// 在第一列依序添加类名
						current_problem_sheet.addCell(new Label(ResultRecorder.class_name_column, current_class_row, current_class));
						// 当前的问题的信息对
						Pair<String[], String[]> base_children_unchecked_children = (Pair<String[], String[]>) current_problem_class_map.get(current_class);
						// 信息对左
						int base_child_row = current_class_row;
						for (int child_index = 0; child_index < base_children_unchecked_children.getLeft().length; base_child_row++, child_index++) {
							current_problem_sheet.addCell(new Label(this.pair_left_column, base_child_row, base_children_unchecked_children.getLeft()[child_index]));
						}
						// 信息对右
						int unchecked_child_row = current_class_row;
						for (int child_index = 0; child_index < base_children_unchecked_children.getRight().length; unchecked_child_row++, child_index++) {
							current_problem_sheet.addCell(new Label(this.pair_right_column, unchecked_child_row, base_children_unchecked_children.getRight()[child_index]));
						}
						// 下一个具有当前问题的类的起始行。注意即使信息对任意一端长度为0时，也要将current_class_row+1
						int next_class_row = current_class_row + max(base_children_unchecked_children.getLeft().length, base_children_unchecked_children.getRight().length, 1);
						/*
						 * 合并当前类的类名、信息对中非数组端的单元格，分别是：要合并的所有单元格（最左上角的列号，最左上角的行号，最右下角的列号，最右下角的行号）
						 */
						current_problem_sheet.mergeCells(ResultRecorder.class_name_column, current_class_row, ResultRecorder.class_name_column, next_class_row - 1);
						// 修改current_class_row
						current_class_row = next_class_row;
					}
					break;
				}
				default:
					System.err.println("跳到这里说明存储一致性检查问题事件发生未知错误！去检查ProjectAndProjectConsistencyChecker");
					break;
				}
			}
			// 写入文件
			workbook.write();
			workbook.close();
		} catch (RowsExceededException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void recordImplResult(HashMap<String, HashMap<String, ?>> impl_result, String file_dir) {
		this.impl_result_file_editor = new FileEditor(file_dir + this.base_project_name + "&" + this.unchecked_project_name + File.separator + Keys.impl_key + this.result_file_tail);
		try {
			// 创建一个工作簿
			WritableWorkbook workbook = Workbook.createWorkbook(this.impl_result_file_editor.getFile());
			// 遍历当前检查结果中的所有不一致问题，将其存到同一个工作簿的不同工作表中
			Iterator<String> impl_problems = impl_result.keySet().iterator();
			int sheet_index = 0;
			while (impl_problems.hasNext()) {
				// 当前的不一致问题
				String current_problem = impl_problems.next();
				// 创建一个工作表，记录具有当前问题的类和关系
				WritableSheet current_problem_sheet = workbook.createSheet(current_problem, sheet_index);
				sheet_index++;
				// 具有当前问题的所有类的map，键值对为<具有当前不一致问题的类名, 该类所对应的不一致关系>。具体格式见ProjectAndProjectConsistencyChecker
				HashMap<String, ?> current_problem_class_map = impl_result.get(current_problem);
				// 遍历具有当前问题的类，向工作表中添加数据
				Iterator<String> current_problem_classes = current_problem_class_map.keySet().iterator();
				int current_class_row = 0;
				// 根据不同问题的格式，决定存储方式
				switch (current_problem) {
				// 格式相同的问题共用一种存储方式
				case Keys.new_in_unchecked_project:
				case Keys.no_impls_in_unchecked_project:
				case Keys.no_impls_in_base_project:
				case Keys.no_impleds_in_unchecked_project:
				case Keys.no_impleds_in_base_project: {
					while (current_problem_classes.hasNext()) {
						// 具有当前问题的类
						String current_class = current_problem_classes.next();
						// 在第一列依序添加类名
						current_problem_sheet.addCell(new Label(ResultRecorder.class_name_column, current_class_row, current_class));
						// 当前的问题的信息对
						Pair<String[], String[]> base_impl_unchecked_impl = (Pair<String[], String[]>) current_problem_class_map.get(current_class);
						// 信息对左
						int base_impl_row = current_class_row;
						for (int impl_index = 0; impl_index < base_impl_unchecked_impl.getLeft().length; base_impl_row++, impl_index++) {
							current_problem_sheet.addCell(new Label(this.pair_left_column, base_impl_row, base_impl_unchecked_impl.getLeft()[impl_index]));
						}
						// 信息对右
						int unchecked_impl_row = current_class_row;
						for (int impl_index = 0; impl_index < base_impl_unchecked_impl.getRight().length; unchecked_impl_row++, impl_index++) {
							current_problem_sheet.addCell(new Label(this.pair_right_column, unchecked_impl_row, base_impl_unchecked_impl.getRight()[impl_index]));
						}
						// 下一个具有当前问题的类的起始行。注意即使信息对任意一端长度为0时，也要将current_class_row+1
						int next_class_row = current_class_row + max(base_impl_unchecked_impl.getLeft().length, base_impl_unchecked_impl.getRight().length, 1);
						/*
						 * 合并当前类的类名、信息对中非数组端的单元格，分别是：要合并的所有单元格（最左上角的列号，最左上角的行号，最右下角的列号，最右下角的行号）
						 */
						current_problem_sheet.mergeCells(ResultRecorder.class_name_column, current_class_row, ResultRecorder.class_name_column, next_class_row - 1);
						// 修改current_class_row
						current_class_row = next_class_row;
					}
					break;
				}
				default:
					System.err.println("跳到这里说明存储一致性检查问题事件发生未知错误！去检查ProjectAndProjectConsistencyChecker");
					break;
				}
			}
			// 写入文件
			workbook.write();
			workbook.close();
		} catch (RowsExceededException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void recordCallResult(HashMap<String, HashMap<String, ?>> call_result, String file_dir) {
		this.call_result_file_editor = new FileEditor(file_dir + this.base_project_name + "&" + this.unchecked_project_name + File.separator + Keys.call_key + this.result_file_tail);
		try {
			// 创建一个工作簿
			WritableWorkbook workbook = Workbook.createWorkbook(this.call_result_file_editor.getFile());
			// 遍历当前检查结果中的所有不一致问题，将其存到同一个工作簿的不同工作表中
			Iterator<String> call_problems = call_result.keySet().iterator();
			int sheet_index = 0;
			while (call_problems.hasNext()) {
				// 当前的不一致问题
				String current_problem = call_problems.next();
				// 创建一个工作表，记录具有当前问题的类和关系
				WritableSheet current_problem_sheet = workbook.createSheet(current_problem, sheet_index);
				sheet_index++;
				// 具有当前问题的所有类的map，键值对为<具有当前不一致问题的类名, 该类所对应的不一致关系>。具体格式见ProjectAndProjectConsistencyChecker
				HashMap<String, ?> current_problem_class_map = call_result.get(current_problem);
				// 遍历具有当前问题的类，向工作表中添加数据
				Iterator<String> current_problem_classes = current_problem_class_map.keySet().iterator();
				int current_class_row = 0;
				// 根据不同问题的格式，决定存储方式
				switch (current_problem) {
				// 格式相同的问题共用一种存储方式
				case Keys.new_in_unchecked_project:
				case Keys.no_callers_in_unchecked_project:
				case Keys.no_callers_in_base_project:
				case Keys.no_callees_in_unchecked_project:
				case Keys.no_callees_in_base_project: {
					while (current_problem_classes.hasNext()) {
						// 具有当前问题的类
						String current_class = current_problem_classes.next();
						// 在第一列依序添加类名
						current_problem_sheet.addCell(new Label(ResultRecorder.class_name_column, current_class_row, current_class));
						// 当前的问题的信息对
						Pair<String[], String[]> base_call_unchecked_call = (Pair<String[], String[]>) current_problem_class_map.get(current_class);
						// 信息对左
						int base_call_row = current_class_row;
						for (int call_index = 0; call_index < base_call_unchecked_call.getLeft().length; base_call_row++, call_index++) {
							current_problem_sheet.addCell(new Label(this.pair_left_column, base_call_row, base_call_unchecked_call.getLeft()[call_index]));
						}
						// 信息对右
						int unchecked_call_row = current_class_row;
						for (int call_index = 0; call_index < base_call_unchecked_call.getRight().length; unchecked_call_row++, call_index++) {
							current_problem_sheet.addCell(new Label(this.pair_right_column, unchecked_call_row, base_call_unchecked_call.getRight()[call_index]));
						}
						// 下一个具有当前问题的类的起始行。注意即使信息对任意一端长度为0时，也要将current_class_row+1
						int next_class_row = current_class_row + max(base_call_unchecked_call.getLeft().length, base_call_unchecked_call.getRight().length, 1);
						/*
						 * 合并当前类的类名、信息对中非数组端的单元格，分别是：要合并的所有单元格（最左上角的列号，最左上角的行号，最右下角的列号，最右下角的行号）
						 */
						current_problem_sheet.mergeCells(ResultRecorder.class_name_column, current_class_row, ResultRecorder.class_name_column, next_class_row - 1);
						// 修改current_class_row
						current_class_row = next_class_row;
					}
					break;
				}
				default:
					System.err.println("跳到这里说明存储一致性检查问题事件发生未知错误！去检查ProjectAndProjectConsistencyChecker");
					break;
				}
			}
			// 写入文件
			workbook.write();
			workbook.close();
		} catch (RowsExceededException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private int max(int left_length, int right_length, int min_add) {
		// TODO Auto-generated method stub
		int temp = left_length > right_length ? left_length : right_length;
		return temp > min_add ? temp : min_add;
	}

	public HashMap<String, FileEditor> getResults() {
		HashMap<String, FileEditor> results = new HashMap<>();
		results.put(Keys.extends_key, this.extends_result_file_editor);
		results.put(Keys.impl_key, this.impl_result_file_editor);
		results.put(Keys.call_key, this.call_result_file_editor);
		return results;
	}
}
