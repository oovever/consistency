package com.buaa.huawei.consistency.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 用于编辑File，可以实现读、写任意数据的操作
 * <p>
 * 读的方法有<code>readAll()</code>、<code>readLine()</code>
 * <p>
 * 写的方法名字均为write，可以写入{@code byte[]}、{@code InputStream}、{@code String}
 * 
 * @author 陈方伟
 */
public class FileEditor {
	private BufferedReader bufferedReader;
	private File file;
	private byte[] fileContentBytes;
	/**
	 * 构造新的{@code FileEditor}时，默认为<code>false</code>。
	 * <p>
	 * 调用<code>readLine()</code>方法时，如果此标识为<code>false</code>，则会创建新的bufferedReader；如果此标识为<code>true</code>，则会使用当前的bufferedReader读取下一行
	 */
	private boolean hasReadByLine = false;
	/**
	 * 构造新的{@code FileEditor}时，默认为<code>true</code>。每调用一次任意一个write方法，都会将此标识改为<code>true</code>。每调用一次<code>readAll()</code>方法，都会将此标识改为<code>false</code>
	 */
	private boolean isChanged = true;

	/**
	 * 构造新的{@code FileEditor}，可实现对filePath所指的文件读、写操作
	 * 
	 * @param filePath
	 *            目标文件路径。构造法会自动将路径中的非法字符替换为(_)
	 */
	public FileEditor(String filePath) {
		filePath = filePath.replaceAll("\\*", "_").replaceAll("\\?", "_").replaceAll("<", "_").replaceAll(">", "_");
		file = new File(filePath);
		File parent = file.getParentFile();
		if (parent != null && !parent.exists()) {
			parent.mkdirs();
			System.out.println("Directories made: " + parent.getAbsolutePath());
		}
	}

	public File getFile() {
		return file;
	}

	/**
	 * 读取文件的所有内容
	 * 
	 * @return fileContentBytes 文件所有内容，以{@code byte[]}的形式返回
	 */
	public byte[] readAll() {
		if (file.exists()) {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			try {
				byte[] tempBytes = new byte[1024];
				int length = 0;
				FileInputStream fileInputStream = new FileInputStream(file);
				while ((length = fileInputStream.read(tempBytes)) != -1) {
					byteArrayOutputStream.write(tempBytes, 0, length);
				}
				fileInputStream.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fileContentBytes = byteArrayOutputStream.toByteArray();
			isChanged = false;
			hasReadByLine = false;
		}
		return fileContentBytes;
	}

	/**
	 * 读取文件的一行内容。所谓一行，即使用('\n')或('\r')为行末标识的内容。连续使用该方法可以将文件内容“一行一行”地读完
	 * <p>
	 * 每当对文件进行write操作后，此方法都会重新从头读
	 * 
	 * @return line 文件的一行内容，以{@code String}的形式返回
	 */
	public String readLine() {
		String line = null;
		if (file.exists()) {
			if (isChanged) {
				this.readAll();
			}
			if (!hasReadByLine) {
				bufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(fileContentBytes)));
				hasReadByLine = true;
			}
			try {
				if ((line = bufferedReader.readLine()) == null) {
					bufferedReader.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return line;
	}

	/**
	 * 向文件中写入二进制数据。
	 * 
	 * @param bytes
	 *            要写入的二进制数组数据
	 * @param append
	 *            是否续写。如果是<code>false</code>，则会清空原文件所有内容，重新写入；如果是<code>true</code>，则会在原文件的末尾开始写入
	 */
	public void write(byte[] bytes, boolean append) {
		try {
			FileOutputStream outputStream = new FileOutputStream(file, append);
			outputStream.write(bytes);
			outputStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		isChanged = true;
		hasReadByLine = false;
	}

	/**
	 * 向文件中写入数据。数据以{@code InputStream}的形式封存
	 *
	 * @param inputStream
	 *            封存着要写入的数据
	 * @param append
	 *            是否续写。如果是<code>false</code>，则会清空原文件所有内容，重新写入；如果是<code>true</code>，则会在原文件的末尾开始写入
	 */
	public void write(InputStream inputStream, boolean append) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte[] tempBytes = new byte[1024];
		int length = 0;// 每次读取的字符串长度，如果为-1，代表全部读取完毕
		try {
			while ((length = inputStream.read(tempBytes)) != -1) {// 读取inputStream，存到bytes里
				// 把bytes写到byteArrayOutputStream中，中间参数代表从哪个位置开始读，length代表读取的长度
				byteArrayOutputStream.write(tempBytes, 0, length);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.write(byteArrayOutputStream.toByteArray(), append);
	}

	/**
	 * 向文件中写入{@code String}
	 *
	 * @param str
	 *            要写入的{@code String}
	 * @param append
	 *            是否续写。如果是<code>false</code>，则会清空原文件所有内容，重新写入；如果是<code>true</code>，则会在原文件的末尾开始写入
	 */
	public void write(String str, boolean append) {
		this.write(str.getBytes(), append);
	}
}
