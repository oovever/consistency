package com.buaa.huawei.consistency.test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import com.buaa.huawei.consistency.results.ResultRecorder;
import com.buaa.huawei.consistency.util.FileEditor;
import com.buaa.huawei.consistency.util.Keys;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class Analyzer {
	private String file_tail = " analysis.xls";
	private final int title_row = 0;

	/**
	 * 遍历目录下所有文件夹、文件夹内所有检查结果。 将每个文件夹名作为列项，记录每类问题下，每列中产生改变的类，以及每列中所有类的总数
	 */
	public void analyze(String dir) {
		/*
		 * 遍历所有结果。 Key是目录，Value是<Keys,对应Keys的文件>
		 */
		HashMap<String, HashMap<String, File>> check_results_map = new HashMap<>();
		this.getAllFiles(dir, check_results_map);
		analyzeExtends(dir, check_results_map, Keys.extends_key);
		analyzeImpl(dir, check_results_map, Keys.impl_key);
		analyzeCall(dir, check_results_map, Keys.call_key);
	}

	private void analyzeExtends(String dir, HashMap<String, HashMap<String, File>> check_results_map, String key) {
		FileEditor extends_analysis_file = new FileEditor(dir + File.separator + key + this.file_tail);
		try {
			// 分析结果工作簿
			WritableWorkbook extends_analysis_workbook = Workbook.createWorkbook(extends_analysis_file.getFile());
			// 对应每个问题的工作表
			extends_analysis_workbook.createSheet(Keys.deleted_in_unchecked_project, 0);
			extends_analysis_workbook.createSheet(Keys.no_parent_in_base_project, 1);
			extends_analysis_workbook.createSheet(Keys.no_parent_in_unchecked_project, 2);
			extends_analysis_workbook.createSheet(Keys.different_parents, 3);
			extends_analysis_workbook.createSheet(Keys.no_child_in_unchecked_project, 4);
			extends_analysis_workbook.createSheet(Keys.no_child_in_base_project, 5);
			extends_analysis_workbook.createSheet(Keys.new_in_unchecked_project, 6);
			this.record(check_results_map, key, extends_analysis_workbook);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void analyzeImpl(String dir, HashMap<String, HashMap<String, File>> check_results_map, String key) {
		FileEditor impl_analysis_file = new FileEditor(dir + File.separator + key + this.file_tail);
		try {
			// 分析结果工作簿
			WritableWorkbook impl_analysis_workbook = Workbook.createWorkbook(impl_analysis_file.getFile());
			// 对应每个问题的工作表
			impl_analysis_workbook.createSheet(Keys.no_impls_in_unchecked_project, 0);
			impl_analysis_workbook.createSheet(Keys.no_impls_in_base_project, 1);
			impl_analysis_workbook.createSheet(Keys.no_impleds_in_unchecked_project, 2);
			impl_analysis_workbook.createSheet(Keys.no_impleds_in_base_project, 3);
			impl_analysis_workbook.createSheet(Keys.new_in_unchecked_project, 4);
			this.record(check_results_map, key, impl_analysis_workbook);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void analyzeCall(String dir, HashMap<String, HashMap<String, File>> check_results_map, String key) {
		FileEditor call_analysis_file = new FileEditor(dir + File.separator + key + this.file_tail);
		try {
			// 分析结果工作簿
			WritableWorkbook call_analysis_workbook = Workbook.createWorkbook(call_analysis_file.getFile());
			// 对应每个问题的工作表
			call_analysis_workbook.createSheet(Keys.no_callers_in_unchecked_project, 0);
			call_analysis_workbook.createSheet(Keys.no_callers_in_base_project, 1);
			call_analysis_workbook.createSheet(Keys.no_callees_in_unchecked_project, 2);
			call_analysis_workbook.createSheet(Keys.no_callees_in_base_project, 3);
			call_analysis_workbook.createSheet(Keys.new_in_unchecked_project, 4);
			this.record(check_results_map, key, call_analysis_workbook);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 遍历目录下所有文件夹、文件夹内所有检查结果。按照继承、实现、调用三类存储，注意记录每个file对应的两个版本号
	 */
	private void getAllFiles(String dir, HashMap<String, HashMap<String, File>> result_map) {
		File root_dir = new File(dir);
		File[] dirs = root_dir.listFiles();
		HashMap<String, File> three_results = new HashMap<>();
		for (File d : dirs) {
			if (d.isDirectory()) {
				// 递归调用自身，从而完成遍历
				this.getAllFiles(d.getAbsolutePath(), result_map);
			} else {
				// 存储遍历结果
				String name = d.getName();
				if (name.contains(Keys.extends_key)) {
					three_results.put(Keys.extends_key, d);
				} else if (name.contains(Keys.impl_key)) {
					three_results.put(Keys.impl_key, d);
				} else if (name.contains(Keys.call_key)) {
					three_results.put(Keys.call_key, d);
				} else {
					System.out.println("文件出错，检查" + root_dir);
				}
				String[] str = root_dir.getAbsolutePath().split("\\/");
				result_map.put(str[str.length - 1], three_results);
			}
		}
	}

	/**
	 * 遍历文件夹内所有检查结果。将每个文件夹名作为列项，记录每类问题下，每列中产生改变的类，以及每列中所有类的总数
	 * 
	 * @param analysis_workbook
	 * @param key
	 * @param check_results_map
	 */
	private void record(HashMap<String, HashMap<String, File>> check_results_map, String key, WritableWorkbook analysis_workbook) {
		// 方便后面查询用
		HashMap<String, WritableSheet> analysis_sheet_map = new HashMap<>();
		WritableSheet[] analysis_sheets = analysis_workbook.getSheets();
		for (WritableSheet s : analysis_sheets) {
			analysis_sheet_map.put(s.getName(), s);
		}
		// 遍历检查结果
		Iterator<String> check_result_dirs = check_results_map.keySet().iterator();
		int column_index = 0;
		try {
			while (check_result_dirs.hasNext()) {
				// 当前检查结果文件夹
				String current_check_result_dir = check_result_dirs.next();
				// 在analysis_workbook的所有sheet中，将检查结果文件夹名作为列项
				String[] str = current_check_result_dir.split("\\\\");
				for (WritableSheet s : analysis_sheets) {
					s.addCell(new Label(column_index, this.title_row, str[str.length - 1]));
				}
				// 读取当前检查结果文件夹下对应当前key的文件，将其中所有问题存到对应sheet中
				Workbook check_result_workbook = Workbook.getWorkbook(check_results_map.get(current_check_result_dir).get(key));
				if (check_result_workbook != null) {
					// 获得所有工作表
					Sheet[] sheets = check_result_workbook.getSheets();
					// 遍历工作表
					if (sheets != null) {
						for (Sheet problem_sheet : sheets) {
							String problem_sheet_name = problem_sheet.getName();
							// 获得行数。按照第一列来逐行遍历，而不是遍历整个sheet
							Cell[] problem_classes = problem_sheet.getColumn(ResultRecorder.class_name_column);
							int blank_cell = 0;
							if (problem_classes.length > 0) {
								// 读取数据
								for (int row = 0; row < problem_classes.length; row++) {
									Cell current_cell = problem_sheet.getCell(ResultRecorder.class_name_column, row);
									// 去除BlankCell
									if (!current_cell.getClass().getName().equals("jxl.read.biff.BlankCell")) {
										String current_class = current_cell.getContents();
										// 将结果存到analysis_workbook中的对应sheet。row+2是因为上面存project_title和problem_classes数目
										analysis_sheet_map.get(problem_sheet_name).addCell(new Label(column_index, row + 2, current_class));
									} else {
										blank_cell++;
									}
								}
							}
							// 记录当前问题下，产生改变的类的总数。即总行数-总blank_cell数
							analysis_sheet_map.get(problem_sheet_name).addCell(new Label(column_index, this.title_row + 1, String.valueOf(problem_classes.length - blank_cell)));
						}
					} else {
						System.out.println("检查" + current_check_result_dir + "下的" + key + "文件的sheet");
					}
				} else {
					System.out.println("检查" + current_check_result_dir + "下的" + key + "文件是否丢失");
				}
				column_index++;
			}
			// 写入文件
			analysis_workbook.write();
			analysis_workbook.close();
		} catch (RowsExceededException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
