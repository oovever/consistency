package com.buaa.huawei.consistency.util;

public class Pair<Type1, Type2> {
	private Type1 left;
	private Type2 right;

	public Pair() {
	}

	public Pair(Type1 left, Type2 right) {
		this.left = left;
		this.right = right;
	}

	public Type1 getLeft() {
		return left;
	}

	public void setLeft(Type1 left) {
		this.left = left;
	}

	public Type2 getRight() {
		return right;
	}

	public void setRight(Type2 right) {
		this.right = right;
	}

	public static <Type1, Type2> Pair<Type1, Type2> createPair(Type1 left, Type2 right) {
		return new Pair<Type1, Type2>(left, right);
	}
}
